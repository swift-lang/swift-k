#!/usr/bin/perl
# Args:
# 	<URIs> <blockid> <logdir>
#	where:
#		<URIs> - comma separated list of URIs for the coaster service; they
#				will be tried in order
#		<blockid> - some block id (the log file will be named based on this)
#		<logdir> - some directory in which the logs should go
#                  Set to "NOLOGGING" to turn off logging.
#                  Env var ENABLE_WORKER_LOGGING also turns on logging.
#

use IO::Socket;
use Socket qw(IPPROTO_TCP TCP_NODELAY);
use IO::Select;
use File::Basename;
use File::Path;
use File::Copy;
use Getopt::Std;
use FileHandle;
use Cwd "realpath";
use POSIX;
use POSIX ":sys_wait_h";
use strict;
use warnings;

BEGIN { eval "use Time::HiRes qw(time); 1" or print "Hi res time not available. Log timestamps will have second granularity\n"; }

# Maintain a stack of job slot ids for auxiliary services:
#   Each slot has a small integer id 0..n-1
#   Obtain a slot from the stack when starting a job
#   Return the slot to the stack (making it available) when a job ends
#   Pass a slot id to launched jobs via env var SWIFT_JOB_SLOT;
#   jobs can use this to reach a persistent process associated with the slot.
#   Initial use is to send app() R eval requests to persistent R worker processes.

use constant MAXJOBSLOTS => 128; # Arbitrary fixed limit on job slot stack.
my @jobslots=();
for( my $jobslot=MAXJOBSLOTS-1; $jobslot>=0; $jobslot--) {
  push @jobslots, $jobslot;
}

# If ASYNC is on, the following will be done:
#   1. Stageouts will be done in parallel
#   2. The job status will be set to "COMPLETED" as soon as the last
#      file is staged out (and before any cleanup is done).
use constant ASYNC => 1;

use constant {
	TRACE => 0,
	DEBUG => 1,
	INFO => 2,
	WARN => 3,
	ERROR => 4,
	NONE => 999,
};

use constant {
	MODE_ALWAYS => 1,
	MODE_IF_PRESENT => 2,
	MODE_ON_ERROR => 4,
	MODE_ON_SUCCESS => 8,
};

use constant {
	CONTINUE => 0,
	YIELD => 1,
};

use constant {
	PUT_START => 0,
	PUT_CMD_SENT => 1,
	PUT_SIZE_SENT => 2,
	PUT_LNAME_SENT => 3,
	PUT_SENDING_DATA => 4,
};

use constant {
	ERROR_STAGEIN_RECEIVE => 520,
	ERROR_STAGEIN_TIMEOUT => 521,
	ERROR_STAGEIN_FILE_WRITE => 522,
	ERROR_STAGEIN_IO => 524,
	ERROR_STAGEIN_REQUEST => 525,
	ERROR_STAGEOUT_IO => 528,
	ERROR_STAGEOUT_SEND => 515,
	ERROR_STAGEOUT_TIMEOUT => 516,
	ERROR_PROCESS_FORK => 512,
	ERROR_PROCESS_WALLTIME_EXCEEDED => 513
};

my $LOGLEVEL = NONE;

my @LEVELS = ("TRACE", "DEBUG", "INFO ", "WARN ", "ERROR");
my %LEVELMAP = (
	"TRACE" => TRACE,
	"DEBUG" => DEBUG,
	"INFO" => INFO,
	"WARN" => WARN,
	"ERROR" => ERROR,
	"NONE" => NONE,
	"OFF" => NONE,
);

use constant {
	REPLY_FLAG => 0x00000001,
	FINAL_FLAG => 0x00000002,
	ERROR_FLAG => 0x00000004,
	PROGRESSIVE_FLAG => 0x00000008,
	SIGNAL_FLAG => 0x00000010,
	INITIAL_FLAG => 0x00000020,
};

use constant {
	COMPLETED => 0x07,
	FAILED => 0x05,
	ACTIVE => 0x02,
	STAGEIN => 0x10,
	STAGEOUT => 0x11,
};

my $TAG = int(rand(10000));
use constant RETRIES => 3;
use constant CHANNEL_TIMEOUT => 180;
use constant HEARTBEAT_INTERVAL => 60;
use constant MAXFRAGS => 16;
# TODO: Make this configurable (#537)
use constant MAX_RECONNECT_ATTEMPTS => 3;
use constant NEVER => 9999999999;
use constant NULL_TIMESTAMP => "\x00\x00\x00\x00\x00\x00\x00\x00";

use constant SUBPROCESS_CHECK_INTERVAL => 0.1; # seconds

my $JOBS_RUNNING = 0;
my $ASYNC_RUNNING = 0;
my $LAST_SUBPROCESS_CHECK_TIME = 0;
my $JOB_COUNT = 0;

use constant {
	LOCK_SH => 1,
	LOCK_EX => 2,
	LOCK_NB => 4,
	LOCK_UN => 8,
};

my $SOFT_IMAGE_MAIN_LOCK;
my $SOFT_IMAGE_CREATE_LOCK;
my $SOFT_IMAGE_USE_LOCK;
my $SOFT_IMAGE_DIR;
# true if this is the first worker on a node
my $SOFT_IMAGE_LEAD_PROCESS = 0;
# true if this is the first job in this worker
my $SOFT_IMAGE_FIRST_IN_PROCESS = 1;
# keep track of the job that stages in the soft image
# any errors that occur with this job should cause the worker
# to signal all other workers on this node to fail and then quit 
my $SOFT_IMAGE_JOB_ID;

use constant BUFSZ => 2048;
use constant IOBUFSZ => 32768;
use constant IOBLOCKSZ => 8;

# Maximum size of re-directed output
use constant MAX_OUT_REDIR_SIZE => 2048;

# If true, enable a profile result that is written to the log
my $PROFILE = 0;
# Contains tuples (EVENT, PID, TIMESTAMP) (flattened)
my @PROFILE_EVENTS = ();

my $PROBE_INTERVAL = 60;
my $LAST_PROBE_TIME = time() - $PROBE_INTERVAL + 1;

my $ID = "-";
my $CONNECTED = 0;

my $myhost=`hostname`;
$myhost =~ s/\s+$//;

sub wlog {
	my $msg;
	my $level = shift;
	if ($level >= $LOGLEVEL) {
		foreach $msg (@_) {
			my $timestamp = timestring();
			my $msgline = sprintf("%s %s %s %s",
								  $timestamp,
								  $LEVELS[$level],
								  $ID, $msg);
			print LOG $msgline;
		}
	}
	return 1;
}

# Command-line arguments:
my %OPTS=();
getopts("c:w:h", \%OPTS);

if (defined $OPTS{"h"}) {
	print "worker.pl [-w <maxwalltime>] [-c <concurrency>] <serviceURL> <blockID> <logdir>\n";
	exit(1);
}

my $CONCURRENCY;
if (defined $OPTS{"c"}) {
	$CONCURRENCY = $OPTS{"c"};
}

my $URISTR=$ARGV[0];
my $BLOCKID=$ARGV[1];
my $LOGDIR=$ARGV[2];

my $MAXWALLTIME = $OPTS{"w"};

defined $URISTR  || die "Not given: URI\n";
defined $BLOCKID || die "Not given: BLOCKID\n";
defined $LOGDIR  || die "Not given: LOGDIR\n";
defined $MAXWALLTIME || ($MAXWALLTIME = "-1");

# REQUESTS holds a map of incoming requests
my %REQUESTS = ();

# REPLIES stores the state of (outgoing) commands for which replies are expected
my %REPLIES  = ();

my %SUSPENDED_TRANSFERS = ();

my $LAST_RECEIVE_TIME = 0;

# partial message being sent when
# writing to the socket would have blocked
my %partialSend;

# the structure of the above maps is (fields marked with "*" are optional):
#	tag: [state, time]
#
#	state: {} - valid keys:
#		tag: the current command/request tag
#		dataIn: proc(state, tag, timeout, err, fin, msg) - invoked when data is received
#		nextData: proc(state) - invoked to get the next data chunk
#								returns: (flags, data, yieldFlag), where
#									flags: the protocol flags to send (e.g. err, fin)
#									data: the actual data
#									yieldFlag: if CONTINUE then it instructs the sending procedure
#												to loop sending data until YIELD is returned
#
#		dataSent: proc(state, tag) - invoked when all data was sent
#		PUT file specific state:
#			state: a numeric state number:
#				0 - new request
#				1 - command sent
#				2 - file size sent
#				3 - local name sent (i.e. sending data)
#			size: file size
#			lname: local file name
#			rname: remote file name
#			sent: total bytes sent from this file
#			bindex: block index - multiple I/O buffer size worth of data are sent
#								  before yielding to other commands/requests (up to IOBLOCKSZ).
#								  This number counts how many buffer sizes in the current block have
#								  been sent so far.
#			handle: file handle
#
#		GET file specific state:
#			state:
#				0 - new request
#				1 - size received
#			handle: file handle
#			size: file size
#			lname: local file name
#
#		state when sending array data:
#			index: the current index in the data array
#			data: an array containing the data chunks
#
#
#
#

my $LOG = logfilename($LOGDIR, $BLOCKID);

my %HANDLERS = (
	"SHUTDOWN"  => \&shutdownw,
	"SUBMITJOB" => \&submitjob,
	"REGISTER"  => \&register,
	"HEARTBEAT" => \&heartbeat,
	"WORKERSHELLCMD" => \&workershellcmd,
);

my @SENDQ = ();

my @URIS = split(/,/, $URISTR);
my @SCHEME;
my @HOSTNAME;
my @PORT;
my $URI;
foreach $URI (@URIS) {
	if ($URI =~ /(.*):\/\//) { push(@SCHEME, $1); } else { die "Could not parse url scheme: $URI"; }
	if ($URI =~ /.*:\/\/(.*):/) { push(@HOSTNAME, $1); } else { die "Could not parse url hostname: $URI"; }
	if ($URI =~ /.*:\/\/.*:(.*)/) { push(@PORT, $1); } else { die "Could not parse url port: $URI"; }
}
my $SOCK;
my $LAST_HEARTBEAT = 0;

my %ASYNC_WAIT_DATA = ();
my %JOBDATA = ();
my %ACTIVECMDS = ();
my $SHELLCWD = getcwd();

# CDM variables:
my $PINNED_READY = 0;
# Map file names to INFLIGHT or COMPLETE
my %PINNED = ();
use constant {
	INFLIGHT => 1,
	COMPLETE => 2,
};
my %PINNED_WAITING = ();

sub crash {
	wlog ERROR, @_;
	die @_;
}

sub logfilename {
	$LOGDIR = shift;
	$BLOCKID = shift;
	my $result = undef;
	my $uci;
	if (-r "/proc/personality.sh") {
		$uci = get_bg_uci();
		$result = "$LOGDIR/worker-$BLOCKID-$uci.log";
	}
	else {
		$result = "$LOGDIR/worker-$BLOCKID.log";
	}
	return $result;
}

# Get the BlueGene Universal Component Identifier from Zepto
sub get_bg_uci() {
	my %vars = file2hash("/proc/personality.sh");
	my $uci = $vars{"BG_UCI"};
	return $uci;
}

# Read a file into a hash, with file formatted as:
# KEY=VALUE
sub file2hash() {
	my $file = shift;
	my %hash;
	open FILE, "<$file";
	while (<FILE>) {
		chomp;
		my ($key, $val) = split /=/;
		$hash{$key} = $val;
	}
	close FILE;
	return %hash;
}

sub timestring() {
	#my $t = sprintf("%.3f", time());
	my $now = time();
	my @d = localtime($now);
        my $t = sprintf("%i/%02i/%02i %02i:%02i:%02i.%03i", $d[5]+1900, $d[4]+1, $d[3], $d[2], $d[1], $d[0], ($now*1000) % 1000);
	return $t;
}

sub hts {
	my ($H) = @_;

	my $k;
	my $s = "{";
	my $first = 1;

	for $k (keys %$H) {
		if (!$first) {
			$s = $s.", ";
		}
		else {
			$first = 0;
		}
		$s = $s."$k = $$H{$k}";
	}

	return $s."}";
}

sub reconnect() {
	my $fail;
	my $success;
	my $attempt;
	my $j;
	for ($attempt = 0; $attempt < MAX_RECONNECT_ATTEMPTS; $attempt++) {
		wlog INFO, "Connect attempt: $attempt ...\n";
		my $sz = @HOSTNAME;
		$success = 0;
		for ($j = 0; $j < $sz; $j++) {
			wlog INFO, "Trying $HOSTNAME[$j]:$PORT[$j] ...\n";
			$fail = 0;
			$SOCK = IO::Socket::INET->new(Proto=>'tcp', PeerAddr=>$HOSTNAME[$j], PeerPort=>$PORT[$j], Blocking=>1) || ($fail = 1);
			if (!$fail) {
				$success = 1;
				last;
			}
			else {
				wlog INFO, "Connection failed: $!. Trying other addresses\n";
			}
		}
		if ($success) {
			$SOCK->setsockopt(SOL_SOCKET, SO_RCVBUF, 32768);
			$SOCK->setsockopt(SOL_SOCKET, SO_SNDBUF, 32768*8);
			$SOCK->setsockopt(IPPROTO_TCP, TCP_NODELAY, 1);
			wlog INFO, "Connected\n";
			$SOCK->blocking(0);
			$SOCK->autoflush(1);
			# myhost is used by the CoasterService for MPI tasks
			if (defined $CONCURRENCY) {
				wlog DEBUG, "Registering with concurrency=$CONCURRENCY\n";
				queueCmd(registerCB(), "REGISTER", $BLOCKID, $myhost, "maxwalltime = $MAXWALLTIME, concurrency = $CONCURRENCY");
			}
			else {
				wlog DEBUG, "Registering without concurrency\n";
				queueCmd(registerCB(), "REGISTER", $BLOCKID, $myhost, "maxwalltime = $MAXWALLTIME");
			}
			last;
		}
		else {
			my $delay = 2 ** $attempt;
			wlog ERROR, "Connection failed for all addresses.\n";
			if ($attempt < MAX_RECONNECT_ATTEMPTS-1) {
				wlog ERROR, "Retrying in $delay seconds\n";
				select(undef, undef, undef, $delay);
			}
		}
	}
	if (!$success) {
		dieNicely("Failed to connect: $!");
	}
	$CONNECTED = 1;
	$LAST_HEARTBEAT = time();
}

sub initlog() {
	my $slevel = $ENV{"WORKER_LOGGING_LEVEL"};
 	if (defined $slevel) {
		if (!defined $LEVELMAP{$slevel}) {
			die "Invalid worker logging level requested: $slevel";
		}
		$LOGLEVEL = $LEVELMAP{$slevel};
	}
	if ($LOGLEVEL != NONE) {
		if ($LOGLEVEL < WARN) {
			# This message may help people find the log
			print "LOG: $LOG\n";
		}
		mkpath($LOGDIR);
		open(LOG, ">>$LOG") or die "Failed to open log file ($LOG): $!";
		LOG->autoflush(1);
		my $date = localtime;
		wlog INFO, "$BLOCKID Logging started: $date\n";
	}
}

sub init() {
	if ($PROFILE) {
		push(@PROFILE_EVENTS, "START", "N/A", time());
	}
	logsetup();
	if (defined $ENV{"WORKER_COPIES"}) {
		workerCopies($ENV{"WORKER_COPIES"});
	}

	reconnect();

	if(defined $ENV{"WORKER_INIT_CMD"}) {
		worker_init_cmd($ENV{"WORKER_INIT_CMD"});
	}
}

sub logsetup() {
    my $schemes = join(", ", @SCHEME);
	my $hosts = join(", ", @HOSTNAME);
	my $ports = join(", ", @PORT);
	wlog DEBUG, "uri=$URISTR\n";
	wlog DEBUG, "scheme=$schemes\n";
	wlog DEBUG, "host=$hosts\n";
	wlog DEBUG, "port=$ports\n";
	wlog DEBUG, "blockid=$BLOCKID\n";
}

# Accepts comma-separated paths, e.g., "/d1/f1,/d2/f2,/d1/f3,/d4/g4"
# Copies /d1/f1 to /d2/f2 and copies /d1/f3 to /d4/g4
sub workerCopies {
	my ($arg) = @_;
	my @tokens = split(/,|\n/, $arg);
	for (my $i = 0; $i < scalar(@tokens); $i+=2) {
		my $src = trim($tokens[$i]);
		my $dst = trim($tokens[$i+1]);
		wlog DEBUG, "workerCopies: $src -> $dst\n";
		copy($src, $dst) or
			crash "workerCopies: copy failed: $src -> $dst\n";
	}
}

sub worker_init_cmd {
  my ($cmd) = @_;
  wlog DEBUG, "worker_init_cmd: $cmd\n";
  my $rc = system($cmd);
  print "worker_init_cmd exit code: $rc\n";
}

sub trim {
	my ($arg) = @_;
	$arg =~ s/^\s+|\s+$//g ;
	return $arg;
}

sub sockSend {
	my ($buf) = @_;
	
	my $start = time();
	my $r = $SOCK->send($buf, 0);
	if (!defined $r) {
		if ($! == POSIX::EWOULDBLOCK) {
			wlog(TRACE, "Send would block\n");
			$r = 0;
		}
		else {
			$CONNECTED = 0;
			dieNicely("Send failed: $!");
		}
	}
	my $diff = sprintf("%.8f", time() - $start);
	
	my $left = length($buf) - $r;
	
	wlog(DEBUG, "sent: $r, left: $left, time: $diff\n");

	return $left;
}

sub sendm {
	my ($tag, $flags, $msg, $data) = @_;
	my $len = length($msg);
	my $buf = pack("VVVVV", $tag, $flags, $len, ($tag ^ $flags ^ $len), 0);
	wlog(TRACE, "hdr: $buf\n");
	$buf = $buf.$msg;

	wlog(DEBUG, "OUT: len=$len, tag=$tag, flags=$flags\n");
	wlog(TRACE, "$msg\n");
	
	my $msgBytesLeft = sockSend($buf);
	if ($msgBytesLeft != 0) {
		%partialSend = ("buf" => substr($buf, length($buf) - $msgBytesLeft), "data" => $data);
	}
	return $msgBytesLeft; 
}

sub encodeInt {
	my ($value) = @_;
	
	return pack("V", $value);
}

sub sendFrags {
	my ($tag, $flg, $data) = @_;

	my $flg2;
	my $msg;
	my $yield;
	my $msgBytesLeft;

	do {
		($flg2, $msg, $yield) = $$data{"nextData"}($data);
		if (defined($msg)) {
			$msgBytesLeft = sendm($tag, $flg | $flg2, $msg, $data);
			if ($msgBytesLeft != 0) {
				$partialSend{"tag"} = $tag;
				$partialSend{"flg2"} = $flg2;				
				$yield = 1;
			}
		}
	} while (($flg2 & FINAL_FLAG) == 0 && !$yield);
	
	if ($msgBytesLeft == 0) {
		sendmDone($tag, $flg2, $data);
		# would not block
		return 0;
	}
	else {
		# would block
		return 1;
	}
}

sub sendmDone {
	my ($tag, $flg2, $data) = @_;
	
	if (($flg2 & FINAL_FLAG) == 0) {
		# final flag not set; put it back in the queue
		wlog TRACE, "$tag yielding\n";

		# update last time
		my $record = $REPLIES{$tag};
		$$record[1] = time();

		queueCmdCustomDataHandling($REPLIES{$tag}, $data);
	}
	else {
		if (exists($REPLIES{$tag})) {
			my $record = $REPLIES{$tag};
			my ($cont, $lastTime) = ($$record[0], $$record[1]);
			if (defined($$cont{"dataSent"})) {
				$$cont{"dataSent"}($cont, $tag);
			}
		}
		wlog(DEBUG, "done sending frags for $tag\n");
	}
}

sub nextArrayData {
	my ($state) = @_;

	my $index = $$state{"index"};
	$$state{"index"} = $index + 1;
	my $data = $$state{"data"};
	if ($index > $#$data) {
		dieNicely("Index out of bounds in nextArrayData");
	}
	my $flags = 0;
	if ($index == 0) {
		$flags = INITIAL_FLAG;
	}
	if ($index >= $#$data) {
		$flags += FINAL_FLAG;
	}
	wlog DEBUG, "nextArrayData - flags: $flags, index: $index, data: $data\n";
	return ($flags, $$data[$index], CONTINUE);
}

sub arrayData {
	return {
		"index" => 0,
		"nextData" => \&nextArrayData,
		"data" => \@_
	};
}

sub nextFileData {
	my ($state) = @_;

	my $s = $$state{"state"};

	my $tag = $$state{"tag"};

	wlog TRACE, "$tag nextFileData state=$s\n";

	if ($s == PUT_START) {
		$$state{"state"} = $s + 1;
		return (INITIAL_FLAG, $$state{"cmd"}, CONTINUE);
	}
	elsif ($s == PUT_CMD_SENT) {
		$$state{"state"} = $s + 1;
		return (0, pack("VV", $$state{"size"}, 0), CONTINUE);
	}
	elsif ($s == PUT_SIZE_SENT) {
		$$state{"state"} = $s + 1;
		return (0, $$state{"lname"}, CONTINUE);
	}
	elsif ($s == PUT_LNAME_SENT) {
		$$state{"state"} = $s + 1;
		$$state{"sent"} = 0;
		$$state{"bindex"} = 0;
		return ($$state{"size"} == 0 ? FINAL_FLAG : 0, $$state{"rname"}, CONTINUE);
	}
	elsif ($s == PUT_SENDING_DATA) {
		if (defined $SUSPENDED_TRANSFERS{"$tag"}) {
			wlog TRACE, "$tag Transfer suspendend; yielding\n";
			return (0, undef, YIELD);
		}

		my $handle = $$state{"handle"};
		my $buffer;
		my $sz = read($handle, $buffer, IOBUFSZ);
		if (!defined $sz) {
			my $err = "Failed to read data from file: $!";
			my $jobid = $$state{"jobid"};
			wlog INFO, "$tag $err\n";
			abortStageouts($jobid);
			queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_IO, $err);
			return (FINAL_FLAG + ERROR_FLAG, "$err", CONTINUE);
		}
		elsif ($sz == 0 && $$state{"sent"} < $$state{"size"}) {
			my $err = "File size mismatch. Expected $$state{'size'}, got $$state{'sent'}";
			my $jobid = $$state{"jobid"};
			wlog INFO, "$tag $err\n";
			abortStageouts($jobid);
			queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_IO, $err);
			return (FINAL_FLAG + ERROR_FLAG, $err, CONTINUE);
		}
		$$state{"sent"} += $sz;
		wlog DEBUG, "$tag size: $$state{'size'}, sent: $$state{'sent'}\n";
		if ($$state{"sent"} == $$state{"size"}) {
			close $handle;
		}
		# try to send multiple buffers at a time
		$$state{"chunk"} = ($$state{"bindex"} + 1) % IOBLOCKSZ;
		if ($$state{"bindex"} == 0) {
			return (($$state{"sent"} < $$state{"size"}) ? 0 : FINAL_FLAG, $buffer, YIELD);
		}
		else {
			return (($$state{"sent"} < $$state{"size"}) ? 0 : FINAL_FLAG, $buffer, CONTINUE);
		}
	}
}

sub fileData {
	my ($cmd, $jobid, $lname, $rname) = @_;

	my $desc;
	if (!open($desc, "<", "$lname")) {
		wlog WARN, "Failed to open $lname\n";
		# let it go on for now. The next read from the descriptor will fail
	}
	return {
		"cmd" => $cmd,
		"jobid" => $jobid,
		"state" => 0,
		"handle" => $desc,
		"nextData" => \&nextFileData,
		"size" => -s $lname,
		"lname" => $lname,
		"rname" => $rname
	};
}

sub sendInternal {
	my ($tag, $cont, $flags, $state) = @_;
	
	if (!defined $tag) {
		$tag = $$state{"tag"};
		if (!defined $tag) {
			$tag =  $TAG++;
			registerCmd($tag, $cont);
			# make the tag accessible to the data generator
			$$state{"tag"} = $tag;
		}
	}
	return sendFrags($tag, $flags, $state);
}

sub resumeSend {
	if (%partialSend) {
		wlog(DEBUG, "Resuming partial send\n");
		my $buf = $partialSend{"buf"};
		my $msgBytesLeft = sockSend($buf);
		if ($msgBytesLeft != 0) {
			$partialSend{"buf"} = substr($buf, length($buf) - $msgBytesLeft);
			return 1; 
		}
		else {
			sendmDone($partialSend{"tag"}, $partialSend{"flg2"}, $partialSend{"data"});
			undef %partialSend;
			return 0;
		}
	}
	else {
		# i.e. would not block
		return 0;
	}
}

sub queueCmd {
	my @cmd = @_;
	my $cont = shift(@cmd);
	# $cont is the continuation (what gets called when a reply is received)
	push @SENDQ, [undef, $cont, 0, arrayData(@cmd)];
}

sub queueCmdCustomDataHandling {
	my ($cont, $state) = @_;
	push @SENDQ, [undef, $cont, 0, $state];
}

sub queueReply {
	my ($tag, @msgs) = @_;
	push @SENDQ, [$tag, undef, REPLY_FLAG, arrayData(@msgs)];
}

sub queueSignal {
	my ($tag, @msgs) = @_;
	push @SENDQ, [$tag, undef, SIGNAL_FLAG, arrayData(@msgs)];
}

sub queueReplySignal {
	my ($tag, @msgs) = @_;
	push @SENDQ, [$tag, undef, REPLY_FLAG | SIGNAL_FLAG, arrayData(@msgs)];
}

sub queueError {
	my ($tag, @msgs) = @_;
	push @SENDQ, [$tag, undef, REPLY_FLAG | ERROR_FLAG, arrayData(@msgs)];
}

sub unpackData {
	my ($data) = @_;

	my $lendata = length($data);
	if ($lendata < 20) {
		dieNicely("Received faulty message (length < 20: $lendata)");
	}
	my $tag = unpack("V", substr($data, 0, 4));
	my $flg = unpack("V", substr($data, 4, 4));
	my $len = unpack("V", substr($data, 8, 4));
	my $hcsum = unpack("V", substr($data, 12, 4));
	my $csum = unpack("V", substr($data, 16, 4));

	my $chcsum = ($tag ^ $flg ^ $len);

	if ($chcsum != $hcsum) {
		dieNicely("Header checksum failed. Computed checksum: $chcsum, checksum: $hcsum");
	}

	my $msg = "";
	my $frag;
	my $alen = 0;
	while ($alen < $len) {
		$SOCK->recv($frag, $len - $alen);
		$alen = $alen + length($frag);
		$msg = $msg.$frag;
	}

	my $actuallen = length($msg);
	wlog(TRACE, " IN: len=$len, actuallen=$actuallen, tag=$tag, flags=$flg, $msg\n");
	if ($len != $actuallen) {
		dieNicely("len != actuallen\n");
	}
	return ($tag, $flg, $msg);
}

sub processRequest {
	my ($state, $tag, $timeout, $flags, $msg) = @_;

	my $request = $$state{"request"};
	if (!defined($request)) {
		$request = [];
		$$state{"request"} = $request;
	}
	push(@$request, $msg);

	if ($timeout) {
		queueError($tag, ("Timed out waiting for all fragments"));
	}
	elsif (!($flags & FINAL_FLAG)) {
		return;
	}
	else {
		wlog DEBUG, "Processing request\n";
		my $cmd = shift(@$request);
		wlog DEBUG, "Cmd is $cmd\n";
		if (exists($HANDLERS{$cmd})) {
			$HANDLERS{$cmd}->($tag, 0, $request);
		}
		else {
			queueError($tag, ("Unknown command: $cmd"));
		}
	}
}

sub process {
	my ($tag, $flg, $msg) = @_;


	my $reply = $flg & REPLY_FLAG;
	my ($record, $cont, $lastTime);

	if ($reply) {
		if (exists($REPLIES{$tag})) {
			$record = $REPLIES{$tag};
			($cont, $lastTime) = ($$record[0], $$record[1]);
			# update last time
			$$record[1] = time();
		}
		else {
			wlog(WARN, "received reply to unregistered command (tag=$tag, msg=$msg). Discarding.\n");
			return;
		}
	}
	else {
		if (!exists($REQUESTS{$tag})) {
			$REQUESTS{$tag} = [{"dataIn" => \&processRequest}, time()];
			wlog DEBUG, "New request ($tag)\n";
		}
		$record = $REQUESTS{$tag};
		($cont, $lastTime) = ($$record[0], $$record[1]);
	}

	my $fin = $flg & FINAL_FLAG;
	my $err = $flg & ERROR_FLAG;


	if ($fin) {
		if ($reply) {
			# A reply for a command sent by us has been received, which means that
			# the lifecycle of the command is complete, therefore the state of
			# that command can be deleted.
			delete($REPLIES{$tag});
		}
		else {
			# All fragments of a request have been received. Since the record is
			# stored in $cont, $tag, $err, $fin, $msg, we can remove it from the
			# table of (partial) incoming requests
			delete($REQUESTS{$tag});
		}
		wlog DEBUG, "Fin flag set ($tag)\n";
	}

	$$cont{"dataIn"}($cont, $tag, 0, $flg, $msg);

	return 1;
}

sub checkTimeouts {
	my $time = time();
	if ($time - $LAST_RECEIVE_TIME > CHANNEL_TIMEOUT) {
		crash("Channel timed out. Last receive time: $LAST_RECEIVE_TIME, now: $time");
	}
}

my $DATA = "";

my $CONNECTION_WARNING = 0;

sub recvOne {
	my $buf;
	$SOCK->recv($buf, 20 - length($DATA));
	if (length($buf) > 0) {
		$LAST_RECEIVE_TIME = time();
		$DATA = $DATA . $buf;
		if (length($DATA) == 20) {
			# wlog DEBUG, "Received " . unpackData($DATA) . "\n";
			eval { process(unpackData($DATA)); } || (dieNicely("Failed to process data: $@"));
			$DATA = "";
			return;
		}
	}
	else {
		# chances are that this is a non-yet detected dead connection
		# so wait a bit
		if (!$CONNECTION_WARNING) {
			wlog WARN, "Connection to server lost?\n";
			$CONNECTION_WARNING = 1;
		}
		select(undef, undef, undef, 1);
		checkTimeouts();
	}
}

sub registerCmd {
	my ($tag, $cont) = @_;

	wlog DEBUG, "Replies: ".hts(\%REPLIES)."\n";

	$REPLIES{$tag} = [$cont, time(), ()];
}


sub mainloop {
	my $r = new IO::Select();
	$r->add($SOCK);
	while(1) {
		loopOne($r);
	}
}

sub checkHeartbeat {
	if (time() - $LAST_HEARTBEAT > HEARTBEAT_INTERVAL) {
		queueCmd(heartbeatCB(), "HEARTBEAT");
		$LAST_HEARTBEAT = time();
	}
}

sub loopOne {
	my ($r) = @_;
	my ($rset, $wset, $eset);
	
	checkHeartbeat();
	checkSubprocesses();
	checkCommands();
	checkStartProbes();
	
	if (@SENDQ) {
		# if there are commands to send, don't just wait for data
		# to read from the socket
		($rset, $wset, $eset) = IO::Select->select($r, $r, $r, 0.001);
	}
	else {
		($rset, $wset, $eset) = IO::Select->select($r, undef, $r, 0.001);
	}
	if ($eset && @$eset) {
		wlog(DEBUG, "Has error\n");
		$CONNECTED = 0;
		dieNicely("Connection closed\n");
	}
	if ($rset && @$rset) {
		# can read
		wlog(DEBUG, "Can read\n");
		recvOne();
	}
	
	if ($wset && @$wset) {
		# can write
		wlog(DEBUG, "Can write\n");
		sendQueued();
	}
}

sub sendQueued {
	my $wouldBlock;
	# if last write didn't finish, try to finish it now
	$wouldBlock = resumeSend();
	
	if (!$wouldBlock) {
		my $cmd;
		# send whatever is now queued; don't clear the queue, since
		# things may be added to it while stuff is being sent
		my $sz = scalar(@SENDQ);
		wlog(DEBUG, "SENDQ size: $sz\n");
		for (my $i = 0; $i < $sz; $i++)  {
			$cmd = shift(@SENDQ);
			$wouldBlock = sendInternal(@$cmd);
			if ($wouldBlock) {
				last;
			}
		}
	}
}

sub checkCommands {
	for my $tag (keys %ACTIVECMDS) {
		my $out = $ACTIVECMDS{$tag};
		my $rin = 0;
		vec($rin, fileno($out), 1) = 1;
		if (select($rin, undef, undef, 0)) {
			my $data;
			my $count = sysread($out, $data, 1024);
			if ($count == 0) {
				# eof
				wlog DEBUG, "Command output done for $tag\n";
				queueReply($tag, ("OK", ""));
				delete $ACTIVECMDS{$tag};
			}
			else {
				wlog DEBUG, "Command output $tag: $data\n";
				queueReplySignal($tag, ($data));
			}
		}
	}
}

sub printreply {
	my ($tag, $timeout, $err, $fin, $reply) = @_;
	if ($timeout) {
		wlog WARN, "Timed out waiting for reply to $tag\n";
	}
	else {
		wlog DEBUG, "$$reply[0]\n";
	}
}

sub nullCB {
	return {
		"dataIn" => sub {}
	};
}

sub registerCB {
	return {
		"dataIn" => \&registerCBDataIn
	};
}

sub registerCBDataIn {
	my ($state, $tag, $timeout, $flags, $reply) = @_;

	if ($timeout) {
		dieNicely("Failed to register (timeout)");
	}
	elsif ($flags & ERROR_FLAG) {
		dieNicely("Failed to register (service returned error: ".join("\n", $reply).")");
	}
	else {
		$ID = $reply;
		wlog INFO, "Registration successful. ID=$ID\n";
	}
}

sub heartbeatCB {
	return {
		"dataIn" => \&heartbeatCBDataIn
	};
}

sub heartbeatCBDataIn {
	my ($state, $tag, $timeout, $flags, $reply) = @_;

	if ($timeout) {
		if (time() - $LAST_HEARTBEAT > 2 * HEARTBEAT_INTERVAL) {
			dieNicely("Lost heartbeat");
		}
	}
	elsif ($flags & ERROR_FLAG) {
		dieNicely("Heartbeat failed: $reply");
	}
	else {
		wlog INFO, "Heartbeat acknowledged\n";
	}
}

sub queueJobStatusCmd {
	my ($jobid, $statusCode, $errorCode, $msg, $detail) = @_;
	
	if ($statusCode == FAILED) {
		checkSoftimageJobFailure($jobid, $msg);
	}
	if (!defined $detail) {
		$detail = "";
	}
	queueCmd((nullCB(), "JOBSTATUS", $jobid, 
			encodeInt($statusCode), encodeInt($errorCode), $msg, NULL_TIMESTAMP, $detail));
}

sub queueJobStatusCmdExt {
	my ($jobid, $statusCode, $errorCode, $msg, $out, $err) = @_;
	
	if ($statusCode == FAILED) {
		checkSoftimageJobFailure($jobid, $msg);
	}
	queueCmd((nullCB(), "JOBSTATUS", $jobid, 
			encodeInt($statusCode), encodeInt($errorCode), $msg, NULL_TIMESTAMP, $out, $err));
}

sub dieNicely {
	my ($msg) = @_;
	
	wlog ERROR, "$msg\n";
	wlog DEBUG, "dieNicely called\n";
	cleanSoftImage();
	if ($CONNECTED) {
		$CONNECTED = 0; # avoid recursive calls to this method
		queueCmd((nullCB(), "RLOG", "WARN", $msg));
		sendQueued();
	}
	die $msg;
}

sub register {
	my ($tag, $timeout, $reply) = @_;
	queueReply($tag, ("OK"));
}

sub writeprofile {
	if ($PROFILE) {
		wlog(INFO, "PROFILE_INFO:\n");
		while (scalar(@PROFILE_EVENTS)) {
			my $event     = shift(@PROFILE_EVENTS);
			my $pid       = shift(@PROFILE_EVENTS);
			my $timestamp = shift(@PROFILE_EVENTS);
			my $pidnum    = ( $pid =~ /\d+/ ) ? $pid : 0;
			wlog(INFO, sprintf("PROFILE: %-5s %6d %.3f\n", $event, $pidnum, $timestamp));
		}
	}
}

sub shutdownw {
	my ($tag, $timeout, $msgs) = @_;
	wlog DEBUG, "Shutdown command received\n";
	queueReply($tag, ("OK"));
	sendQueued();
	cleanSoftImage();
	wlog INFO, "Acknowledged shutdown.\n";
	wlog INFO, "Ran a total of $JOB_COUNT jobs\n";
	if ($PROFILE) {
		push(@PROFILE_EVENTS, "STOP", "N/A", time());
	}
	writeprofile();
	
	select(undef, undef, undef, 0.2);
	wlog INFO, "Exiting\n";
	
	exit 0;
}

sub heartbeat {
	my ($tag, $timeout, $msgs) = @_;
	$LAST_HEARTBEAT = time();
	my $ts = int(time() * 1000);
	queueReply($tag, pack("VV", ($ts & 0xffffffff), ($ts >> 32)));
}

sub workershellcmd {
	my ($tag, $timeout, $msgs) = @_;
	my $cmd = $$msgs[1];
	my $out;
	if ($cmd =~ m/cd\s*(.*)/) {
		wlog DEBUG, "chdir $1\n";
		if (substr($1, 0, 1) eq "/") {
			$SHELLCWD = $1;
		}
		else {
			$SHELLCWD = realpath("$SHELLCWD/$1");
		}
		queueReply($tag, ("OK", "CWD: $SHELLCWD"));
	}
	elsif ($cmd =~ m/mls\s*(.*)/) {
		wlog DEBUG, "mls $1\n";
		$out = `ls -d $1 2>/dev/null`;
		queueReply($tag, ("OK", "$out"));
	}
	else {
		wlog DEBUG, "workershellcmd $tag: $cmd\n";
		my $err = 0;
		open my $out, "-|", "cd $SHELLCWD && $cmd 2>&1" or $err = 1;
		if ($err) {
			wlog DEBUG, "Cannot launch $cmd ($tag)\n";
			queueError($tag, ("Error starting $cmd"));
		}
		else {
			$ACTIVECMDS{$tag} = $out;
		}
	}
}

sub urisplit {
	my ($name) = @_;

	# accepted forms:
	#   <protocol>://<host>/<path>
	#   <protocol>:<path>
	#   <path>
	#
	if ($name =~ /(\w+):\/\/([^\/]+)\/(.*)/) {
		return ($1, $2, $3);
	}
	if ($name =~ /(\w+):(.*)/) {
		return ($1, "", $2);
	}
	return ("file", "", $name);
}

sub mkfdir {
	my ($jobid, $file) = @_;
	my $dir = dirname($file);
	if (-f $dir) {
		dieNicely("$jobid Cannot create directory $dir. A file with this name already exists");
	}
	if (!-d $dir) {
		wlog DEBUG, "Creating directory $dir\n";
		if (!mkpath($dir)) {
			dieNicely("Cannot create directory $dir. $!");
		}
	}
}

sub getFileCB {
	my ($jobid, $src, $dst) = @_;

	wlog DEBUG, "getFileCB($jobid, $src, $dst)\n";

	$src =~ s/pinned://;
	my ($protocol, $path) = urisplit($src);

	wlog DEBUG, "$jobid src: $src, protocol: $protocol, path: $path\n";

	if (($protocol eq "file") || ($protocol eq "proxy")) {
		wlog DEBUG, "Opening $dst...\n";
		mkfdir($jobid, $dst);
		# don't try open(DESC, ...) (as I did). It will use the same reference
		# and concurrent operations will fail.
		my $handle;
		if (!open($handle, ">", "$dst")) {
			dieNicely("Failed to open $dst: $!");
		}
		else {
			wlog DEBUG, "$jobid Opened $dst\n";
			return {
				"jobid" => $jobid,
				"dataIn" => \&getFileCBDataIn,
				"state" => 0,
				"lfile" => $dst,
				"handle" => $handle
			};
		}
	}
	else {
		return {
			"jobid" => $jobid,
			"dataIn" => \&getFileCBDataInIndirect,
			"lfile" => $dst,
		};
	}
}

sub getFileCBDataInIndirect {
	my ($state, $tag, $timeout, $flags, $reply) = @_;

	my $jobid = $$state{"jobid"};
	wlog DEBUG, "$jobid getFileCBDataInIndirect jobid: $jobid, tag: $tag, flags: $flags\n";
	if ($flags & ERROR_FLAG) {
		if (processCBError($state, $reply, $flags)) {
			my $msg = getErrorMessage($state);
			wlog DEBUG, "$jobid getFileCBDataInIndirect error: $msg\n";
			queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_RECEIVE, 
				"Error staging in file: $msg", getErrorDetail($state));
			delete($JOBDATA{$jobid});	
		}
		return;
	}
	elsif ($timeout) {
		queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_TIMEOUT, "Timeout staging in file");
		delete($JOBDATA{$jobid});
		return;
	}
	if ($flags & FINAL_FLAG) {
		stagein($jobid);
	}
}


sub getFileCBDataIn {
	my ($state, $tag, $timeout, $flags, $reply) = @_;

	my $s = $$state{"state"};
	my $jobid = $$state{"jobid"};
	my $len = length($reply);
	wlog DEBUG, "$jobid getFileCBDataIn jobid: $jobid, state: $s, tag: $tag, flags: $flags, len: $len\n";

	if ($flags & SIGNAL_FLAG) {
		if ($reply eq "QUEUED") {
			$REPLIES{$tag}[1] = NEVER;
			wlog DEBUG, "$jobid transfer queued\n";
		}
		return;
	}
	elsif ($flags & ERROR_FLAG) {
		if ($reply eq "ABORTED") {
			wlog DEBUG, "$jobid client acknowledged abort\n";
		}
		else {
			if (processCBError($state, $reply, $flags)) {
				my $msg = getErrorMessage($state);
				wlog DEBUG, "$jobid getFileCBDataIn error: $msg\n";
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_RECEIVE, 
					"Error staging in file: $msg", getErrorDetail($state));
				delete($JOBDATA{$jobid});
			}
		}
		return;
	}
	elsif ($timeout) {
		queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_TIMEOUT, "Timeout staging in file");
		delete($JOBDATA{$jobid});
		return;
	}
	elsif ($s == 0) {
		$$state{"state"} = 1;
		$$state{"size"} = unpack("V", $reply);
		wlog DEBUG, "$tag $jobid got file size: $$state{'size'}\n";
		my $lfile = $$state{"lfile"};
	}
	else {
		my $handle = $$state{"handle"};
		if (defined $handle) {
			if (!(print {$handle} $reply)) {
				close $handle;
				wlog DEBUG, "$jobid Could not write to file: $!. Descriptor was $handle; lfile: $$state{'lfile'}\n";
				queueSignal($tag, ("ABORT $!"));
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_FILE_WRITE, "Could not write to file: $!");
				delete($$state{"handle"});
				delete($JOBDATA{$jobid});
				return;
			}
		}
		else {
			wlog DEBUG, "$jobid Got data for closed handle. Discarding\n";
		}
	}
	if ($flags & FINAL_FLAG) {
		my $handle = $$state{"handle"};
		close $handle;
		wlog DEBUG, "$jobid Closed $$state{'lfile'}\n";
		if ($PINNED_READY) {
			completePinnedFile($jobid);
		}
		stagein($jobid);
	}
}

sub processCBError {
	my ($state, $data, $flags) = @_;
	
	if (!defined $$state{"error"}) {
		my @array;
		$$state{"error"} = \@array;
	}
	my $dataArray = $$state{"error"};
	push(@$dataArray, $data);
	my $len = scalar $dataArray;
	
	return ($flags & FINAL_FLAG);
}

sub getErrorMessage {
	my ($state) = @_;
	
	my $dataArray = $$state{"error"};
	my $len = scalar $dataArray;
	
	return $$dataArray[0];
}

sub getErrorDetail {
	my ($state) = @_;
	
	my $dataArray = $$state{"error"};
	my $len = scalar $dataArray;
	
	if ($len > 1) {
		return $$dataArray[1];
	}
	else {
		return "";
	}
}

sub completePinnedFile {
	my ($jobid) = @_;

	my $STAGE = $JOBDATA{$jobid}{"stagein"};
	my $STAGED = $JOBDATA{$jobid}{"stageind"};
	my $STAGEINDEX = $JOBDATA{$jobid}{"stageindex"}-1;

	my $rdst = $$STAGED[$STAGEINDEX];
	my $jobdir = $JOBDATA{$jobid}{'job'}{'directory'};
	$rdst =~ s/$jobdir//;

	wlog TRACE, "completePinnedFile(): $rdst\n";

	$PINNED{$rdst} = COMPLETE;
	my $WAITING = $PINNED_WAITING{$rdst};
	if (defined $WAITING) {
		foreach (@$WAITING) {
			wlog TRACE, "completePinnedFile(): $_\n";
			stagein($_);
		}
	}
}

sub stagein {
	my ($jobid) = @_;
	
	wlog TRACE, "stagein(): $jobid\n";

	my $STAGE = $JOBDATA{$jobid}{"stagein"};
	my $STAGED = $JOBDATA{$jobid}{"stageind"};
	my $STAGEINDEX = $JOBDATA{$jobid}{"stageindex"};

	if (scalar @$STAGE <= $STAGEINDEX) {
		wlog INFO, "$jobid Done staging in files ($STAGEINDEX, $STAGE)\n";
		$JOBDATA{$jobid}{"stageindex"} = 0;
		queueJobStatusCmd($jobid, ACTIVE, 0, "workerid=$BLOCKID:$ID");
		forkjob($jobid);
	}
	else {
		if ($STAGEINDEX == 0) {
			queueJobStatusCmd($jobid, STAGEIN, 0, "workerid=$BLOCKID:$ID");
		}
		wlog INFO, "$jobid Staging in $$STAGE[$STAGEINDEX]\n";
		$JOBDATA{$jobid}{"stageindex"} =  $STAGEINDEX + 1;
		my ($protocol, $host, $path) = urisplit($$STAGE[$STAGEINDEX]);
		wlog DEBUG, "$jobid protocol: $protocol\n";
		if ($$STAGE[$STAGEINDEX] =~ "pinned:.*") {
			getPinnedFile($jobid, $$STAGE[$STAGEINDEX], $$STAGED[$STAGEINDEX]);
		}
		elsif ($protocol eq "sfs") {
			my $dst = $$STAGED[$STAGEINDEX];
			mkfdir($jobid, $dst);
			asyncRun($jobid, -1, sub {
					my ($errpipe) = @_;
					if (!copy($path, $dst)) {
						wlog DEBUG, "$jobid Error staging in $path to $dst: $!\n";
						write $errpipe, "Error staging in $path to $dst: $!";
					}
				},
				sub {
					# onStart($pid)
					my ($pid) = @_;
					wlog DEBUG, "$jobid pid: $pid staging $path -> $dst\n";
				},
				sub { # onComplete($err, $msg)
					my ($err, $msg) = @_;
					if (!$err) {
						stagein($jobid);
					}
					else {
						queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_IO, $msg);
						delete($JOBDATA{$jobid});
					}
				}
			);
		}
		else {
			getFile($jobid, $$STAGE[$STAGEINDEX], $$STAGED[$STAGEINDEX]);
		}
	}
}

sub getFile {
	my ($jobid, $src, $dst) = @_;

	wlog TRACE, "getFile($jobid, $src, $dst)\n";

	my $state;
	eval {
		$state = getFileCB($jobid, $src, $dst);
	};
	if ($@) {
		wlog DEBUG, "$jobid Error staging in file: $@\n";
		queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_REQUEST, "$@");
	}
	else {
		queueCmd(($state, "GET", $src, $dst));
	}
}

sub getPinnedFile() {
	my ($jobid, $src, $dst) = @_;

	wlog DEBUG, "Handling pinned file: $src\n";
	my $error;
	$src =~ s/pinned://;
	my $jobdir = $JOBDATA{$jobid}{'job'}{'directory'};
	my $pinned_dir = "$jobdir/../../pinned";
	my $rdst = $dst;
	$rdst =~ s/$jobdir//;

	mkPinnedDirectory($pinned_dir);
	if (! defined $PINNED{$rdst}) {
		downloadPinnedFile($jobid, $src, $dst, $rdst, $pinned_dir);
	}
	else {
		linkToPinnedFile($jobid, $dst, $rdst, $pinned_dir);
	}
}

sub mkPinnedDirectory() {
	my ($pinned_dir) = @_;
	if (! $PINNED_READY) {
		if (! -d $pinned_dir) {
			wlog DEBUG, "mkpath: $pinned_dir\n";
			mkpath($pinned_dir) ||
				dieNicely("mkPinnedDirectory(): Could not mkdir: $pinned_dir ($!)");
		}
		$PINNED_READY = 1;
	}
}

sub downloadPinnedFile() {
	my ($jobid, $src, $dst, $rdst, $pinned_dir) = @_;
	$PINNED{$rdst} = INFLIGHT;
	getFile($jobid, $src, $dst);
	wlog DEBUG, "link: $dst -> $pinned_dir$rdst\n";
	if (! -f "$pinned_dir$rdst") {
		link($dst, "$pinned_dir$rdst") ||
			dieNicely("getPinnedFile(): Could not link: $pinned_dir$rdst ($!)");
	}
}

sub linkToPinnedFile() {
	my ($jobid, $dst, $rdst, $pinned_dir) = @_;
	wlog DEBUG, "link: $pinned_dir$rdst -> $dst\n";
	my $dir = dirname($dst);
	if (! -d $dir) {
		wlog DEBUG, "mkpath: $dir\n";
		mkpath($dir) ||
			dieNicely("getPinnedFile(): Could not mkdir: $dir ($!)");
	}
	link("$pinned_dir$rdst", $dst) ||
		dieNicely("getPinnedFile(): Could not link: $!");
	if ($PINNED{$rdst} == INFLIGHT) {
		waitForPinnedFile($rdst, $jobid);
	}
	else {
		stagein($jobid);
	}
}

# Add this jobid to the list of jobs waiting for in-flight file rdst
sub waitForPinnedFile {
	my ($rdst, $jobid) = @_;

	wlog TRACE, "waitForPinned(): $rdst $jobid\n";

	if (! defined $PINNED_WAITING{$rdst}) {
		$PINNED_WAITING{$rdst} = [];
	}

	my $waiting = $PINNED_WAITING{$rdst};
	push(@$waiting, $jobid);
}

sub abortStageouts {
	my ($jobid) = @_;
	
	# something larger than the number of actual stageouts
	$JOBDATA{$jobid}{"stageindex"} = 1000000;
}

sub stageoutStarted {
	my ($jobid) = @_;
	
	if (!defined $JOBDATA{$jobid}{"stageoutCount"}) {
		$JOBDATA{$jobid}{"stageoutCount"} = 0;
	}
	$JOBDATA{$jobid}{"stageoutCount"} += 1;
	wlog DEBUG, "$jobid Stagecount is $JOBDATA{$jobid}{stageoutCount}\n";
}

sub stageoutEnded {
	my ($jobid) = @_;
	
	wlog DEBUG, "$jobid Stageout done; stagecount is $JOBDATA{$jobid}{stageoutCount}\n";
	$JOBDATA{$jobid}{"stageoutCount"} -= 1;
	if ($JOBDATA{$jobid}{"stageoutCount"} == 0) {
		wlog DEBUG, "$jobid All stageouts acknowledged. Sending notification\n";
		cleanup($jobid);
		sendStatus($jobid);
	}
}

sub activeStageoutCount {
	my ($jobid) = @_;
	
	return $JOBDATA{$jobid}{"stageoutCount"};
}

sub stageout {
	my ($jobid) = @_;

	wlog DEBUG, "$jobid Staging out\n";
	my $STAGE = $JOBDATA{$jobid}{"stageout"};
	my $STAGED = $JOBDATA{$jobid}{"stageoutd"};
	# staging mode
	my $STAGEM = $JOBDATA{$jobid}{"stageoutm"};
	my $STAGEINDEX = $JOBDATA{$jobid}{"stageindex"};

	my $sz = scalar @$STAGE;
	wlog DEBUG, "sz: $sz, STAGEINDEX: $STAGEINDEX\n";
	if (scalar @$STAGE <= $STAGEINDEX) {
		if (activeStageoutCount($jobid) > 0) {
			# let stageoutEnded() handle it
			wlog INFO, "$jobid No more stageouts. Waiting for active stageouts to complete.\n";
		}
		else {
			wlog INFO, "$jobid No more stageouts. Doing cleanup.\n";
			cleanup($jobid);
		}
	}
	else {
		if ($STAGEINDEX == 0) {
			pathExpandStageouts($jobid);
		}
		my $lfile = $$STAGE[$STAGEINDEX];
		my $mode = $$STAGEM[$STAGEINDEX];
		my $skip = 0;

		# it's supposed to be bitwise
		if (($mode & MODE_IF_PRESENT) &&  (! -e $lfile)) {
			$skip = 1;
		}
		if (($mode & MODE_ON_ERROR) && ($JOBDATA{$jobid}{"exitcode"} == 0)) {
			$skip = 2;
		}
		if (($mode & MODE_ON_SUCCESS) && ($JOBDATA{$jobid}{"exitcode"} != 0)) {
			$skip = 3;
		}

		if (!defined($JOBDATA{$jobid}{"stageoutStatusSent"})) {
			wlog DEBUG, "$jobid Sending STAGEOUT status\n";
			queueJobStatusCmd($jobid, STAGEOUT, 0, "");
			$JOBDATA{$jobid}{"stageoutStatusSent"} = 1;
		}
		my $rfile = $$STAGED[$STAGEINDEX];
		$JOBDATA{$jobid}{"stageindex"} = $STAGEINDEX + 1;
		wlog INFO, "$jobid Staging out $lfile->$rfile (mode = $mode).\n";
		my ($protocol, $host, $path) = urisplit($rfile);
		
		if ($skip) {
			# notify client that file will not be staged out
			# this is needed to allow the client to delete
			# old files with the same name
			if ($skip == 1) {
				wlog INFO, "$jobid Empty stageout of missing file ($lfile)\n";
			}
			elsif ($skip == 2) {
				wlog INFO, "$jobid Empty stageout of file ($lfile) (ON_ERROR mode and job succeeded)\n";
			}
			elsif ($skip == 3) {
				wlog INFO, "$jobid Empty stageout of file ($lfile) (ON_SUCCESS mode and job failed)\n";
			}	
		}
		
		if ($protocol eq "file" || $protocol eq "proxy") {
			# make sure we keep track of the total number of actual stageouts
			stageoutStarted($jobid);

			if ($skip) {
				# send length of -1
				queueCmd((putFileCB($jobid), "PUT", pack("ll", -1, -1), $lfile, $rfile));
			}
			else {
				queueCmdCustomDataHandling(putFileCB($jobid), fileData("PUT", $jobid, $lfile, $rfile));
			}
			wlog DEBUG, "$jobid PUT sent.\n";
		}
		elsif ($protocol eq "sfs") {
			stageoutStarted($jobid);
			if ($skip) {
				wlog DEBUG, "$jobid deleting file $path\n";
				unlink($path);
				stageout($jobid);
				stageoutEnded($jobid);
			}
			else {
				mkfdir($jobid, $path);
				asyncRun($jobid, -1, sub {
						my ($errpipe) = @_;
						if (!copy($lfile, $path)) {
							wlog DEBUG, "$jobid Error staging out $lfile to $path: $!\n";
							write $errpipe, "Error staging out $lfile to $path: $!";
						}
					},
					sub {
						# onStart($pid)
						my ($pid) = @_;
						wlog DEBUG, "$jobid pid: $pid staging $path <- $lfile\n";
					},
					sub { # onComplete($err, $msg)
						my ($err, $msg) = @_;
						if (!$err) {
							stageout($jobid);
							stageoutEnded($jobid);
						}
						else {
							queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_IO, $msg);
							delete($JOBDATA{$jobid});
						}
					}
				);
			}
		}
		else {
			if ($skip) {
				# send length of -1
				queueCmd((putFileCB($jobid), "PUT", pack("ll", -1, -1), $lfile, $rfile));
			}
			else {
				queueCmd((putFileCB($jobid), "PUT", pack("VV", 0, 0), $lfile, $rfile));
			}
			wlog DEBUG, "$jobid PUT sent.\n";
		}
	}
}

sub pathExpandStageouts {
	my ($jobid) = @_;
	
	wlog DEBUG, "$jobid: Processing glob stageouts\n";
	
	my $STAGE = $JOBDATA{$jobid}{"stageout"};
	my $STAGED = $JOBDATA{$jobid}{"stageoutd"};
	my $STAGEM = $JOBDATA{$jobid}{"stageoutm"};
	
	my $i = 0;
	while ($i < scalar @$STAGE) {
		my $lfile = $$STAGE[$i];
		wlog DEBUG, "$jobid: $i - lfile = $lfile\n";
		my ($name, $dir, $suffix) = fileparse($lfile); 
		
		if ($name =~ /\?/ || $name =~ /\*/) {
			my $dest = $$STAGED[$i];
			my $mode = $$STAGEM[$i];
			
			my $resrc = toRegexp($name);
			my @lst = list($dir, $resrc);
			
			my $redst = toSubstg($dest);
			
			wlog DEBUG, "$jobid: resrc: $resrc, redst: $redst\n";
			splice(@$STAGE, $i, 1);
			splice(@$STAGED, $i, 1);
			splice(@$STAGEM, $i, 1);
			$i--;
			
			for my $f (@lst) {
				push @$STAGE, "$dir$f";
				$f =~ m/$resrc/;
				my $dt;
				eval "\$dt = qq/$redst/";
				wlog DEBUG, "$jobid: $dir$f -> $dt\n";
				push @$STAGED, $dt;
				push @$STAGEM, $mode;
			}
		}
		
		$i++;
	}
}

sub list {
	my ($dir, $re) = @_;
	
	my @lst;
	my $dirh;
	opendir($dirh, $dir);
	
	while (my $f = readdir($dirh)) {
		if ($f =~ /$re/) {
			push @lst, $f;
		}
	}
	
	return @lst;
}


sub toRegexp {
	my ($s) = @_;
	my $r = "";
	
	my $lastWasWildcard = 0;
	for my $c (split(//, $s)) {
		if ($c =~ /[\.\\\[\]\(\)\^\$|\+\{\}\/]/) {
			if ($lastWasWildcard) {
				$r = "$r)";
				$lastWasWildcard = 0;
			}
			$r = "$r\\$c";
		}
		elsif ($c eq "?") {
			if (!$lastWasWildcard) {
				$r = "$r(";
				$lastWasWildcard = 1;
			}
			$r = "$r.";
		}
		elsif ($c eq "*") {
			if (!$lastWasWildcard) {
				$r = "$r(";
				$lastWasWildcard = 1;
			}
			$r = "$r.*";
		}
		else {
			if ($lastWasWildcard) {
				$r = "$r)";
				$lastWasWildcard = 0;
			}
			$r = "$r$c";
		}
	}
	if ($lastWasWildcard) {
		$r = "$r)";
	}
	return $r;
}

sub toSubstg {
	my ($s) = @_;
	my $r = "";
	
	my $ix = 1;
	
	my $lastWasWildcard = 0;
	for my $c (split(//, $s)) {
		if ($c =~ /[\\\{\}\/]/) {
			if ($lastWasWildcard) {
				$r = "$r)";
				$lastWasWildcard = 0;
			}
			$r = "$r\\$c";
		}
		elsif ($c eq "?" || $c eq "*") {
			if (!$lastWasWildcard) {
				$r = "$r\$$ix";
				$lastWasWildcard = 1;
				$ix++;
			}
		}
		else {
			if ($lastWasWildcard) {
				$r = "$r";
				$lastWasWildcard = 0;
			}
			$r = "$r$c";
		}
	}
	return $r;
}

sub readFile {
	my ($jobid, $fname) = @_;
	
	wlog DEBUG, "$jobid Reading $fname\n";
	if (-e $fname) {
		my $fd;
		my $content;
		
		$content = "";
		
		open($fd, "<", $fname) or return "Error: could not open $fname";
		while (<$fd>) {
			$content = $content . $_;
			if (length($content) > MAX_OUT_REDIR_SIZE) {
				close($fd);
				$content = $content . "\n<output truncated>";
				last;
			}
		}
		close($fd);
		wlog DEBUG, "$jobid $fname: $content\n";
		return $content;
	}
	else {
		wlog DEBUG, "$jobid $fname does not exist\n";
		return "";
	}
}

sub readFiles {
	my ($jobid) = @_;
	
	my $pid = $JOBDATA{$jobid}{"pid"};
	
	return (readFile($jobid, tmpSFile($pid, "out")), readFile($jobid, tmpSFile($pid, "err")));
}

sub sendStatus {
	my ($jobid) = @_;

	my $ec = $JOBDATA{$jobid}{"exitcode"};
	
	if ($JOBDATA{$jobid}{"perftrace"}) {
		$LOGLEVEL = WARN;
	}

	
	my $stdoutRedir;
	my $stderrRedir;
	my $redirect;
	
	$redirect = 0;
	
	if (defined $JOBDATA{$jobid}{"job"}{"redirect"}) {
		wlog DEBUG, "$jobid Output is redirected\n";
		($stdoutRedir, $stderrRedir) = readFiles($jobid);
		$redirect = 1;
	}
	else {
		wlog DEBUG, "$jobid Output is NOT redirected\n";
		$stdoutRedir = "";
		$stderrRedir = "";
	}
	
	if ($ec == 0) {
		if ($redirect) {
			queueJobStatusCmdExt($jobid, COMPLETED, 0, "", $stdoutRedir, $stderrRedir);
		}
		else {
			queueJobStatusCmd($jobid, COMPLETED, 0, "");
		}
	}
	else {
		if ($redirect) {
			queueJobStatusCmdExt($jobid, FAILED, $ec, getExitMessage($jobid, $ec), $stdoutRedir, $stderrRedir);
		}
		else {
			queueJobStatusCmd($jobid, FAILED, $ec, getExitMessage($jobid, $ec));
		}
	}
}

sub cleanup {
	my ($jobid) = @_;

	my $ec = $JOBDATA{$jobid}{"exitcode"};
	if (ASYNC) {
		wlog DEBUG, "$jobid async is on. Sending notification before cleanup\n";
		sendStatus($jobid);
	}

	if ($ec != 0) {
		wlog INFO, "$jobid Job data: ".hts($JOBDATA{$jobid})."\n";
		wlog INFO, "$jobid Job: ".hts($JOBDATA{$jobid}{'job'})."\n";
		wlog INFO, "$jobid Job dir ".`ls -al $JOBDATA{$jobid}{'job'}{'directory'}`."\n";
	}

	my $CLEANUP = $JOBDATA{$jobid}{"cleanup"};
	my $c;
	if ($ec == 0) {
		asyncRun($jobid, -1, sub {
				for $c (@$CLEANUP) {
					if ($c =~ /\/\.$/) {
						chop $c;
						chop $c;
					}
					wlog DEBUG, "$jobid Removing $c\n";
					rmtree($c, 0, 0);
					wlog DEBUG, "$jobid Removed $c\n";
				}
			},
			sub {
				my ($pid) = @_;
				wlog DEBUG, "$jobid pid: $pid cleanup\n";
			},
			sub {
				if (!ASYNC) {
					queueJobStatusCmd($jobid, COMPLETED, 0, "");
				}
			}
		);
	}
	else {
		if (!ASYNC) {
			wlog DEBUG, "$jobid Sending failure.\n";
			queueJobStatusCmd($jobid, FAILED, $ec, getExitMessage($jobid, $ec));
		}
	}
}

sub getExitMessage {
	my ($jobid, $ec) = @_;
	
	if (defined $JOBDATA{$jobid}{"exitmessage"}) {
		return $JOBDATA{$jobid}{"exitmessage"};
	}
	else {
		return "Job failed with and exit code of $ec";
	}
}

sub putFileCB {
	my ($jobid) = @_;
	return {
		"jobid" => $jobid,
		"dataIn" => \&putFileCBDataIn,
		"dataSent" => \&putFileCBDataSent
	};
}

sub putFileCBDataSent {
	my ($state, $tag) = @_;

	if (ASYNC) {
		my $jobid = $$state{"jobid"};
		wlog DEBUG, "$jobid tag: $tag putFileCBDataSent\n";
		if ($jobid != -1) {
			wlog DEBUG, "$jobid tag: $tag Data sent, async is on. Staging out next file\n";
			stageout($jobid);
		}
	}
}

sub putFileCBDataIn {
	my ($state, $tag, $timeout, $flags, $reply) = @_;

	wlog DEBUG, "$tag putFileCBDataIn msg=$reply\n";

	my $jobid = $$state{"jobid"};

	if (($flags & ERROR_FLAG) || $timeout) {
		if ($JOBDATA{$jobid}) {
			wlog DEBUG, "$jobid tag: $tag Stage out failed ($reply)\n";
			if ($timeout) {
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_TIMEOUT, "Stage out timed-out");
			}
			else {
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_SEND, "Stage out failed", $reply);
			}
			delete($JOBDATA{$jobid});
		}
		return;
	}
	elsif ($reply eq "STOP") {
		$SUSPENDED_TRANSFERS{"$tag"} = 1;
		wlog DEBUG, "$jobid tag: $tag Got stop request. Suspending transfer.\n";
	}
	elsif ($reply eq "CONTINUE") {
		delete $SUSPENDED_TRANSFERS{"$tag"};
		wlog DEBUG, "$jobid tag: $tag Got continue request. Resuming transfer.\n";
	}
	elsif ($jobid != -1) {
		# OK reply from client
		if (!ASYNC) {
			wlog DEBUG, "$jobid tag: $tag Stageout done; staging out next file\n";
			stageout($jobid);
			stageoutEnded($jobid);
		}
		else {
			stageoutEnded($jobid);
		}
	}
}

sub isabsolute {
	my ($fn) = @_;

	return substr($fn, 0, 1) eq "/";
}

sub unpackSoftImage {
	my ($src) = @_;

	wlog DEBUG, "Running tar -xzf $src -C $SOFT_IMAGE_DIR\n";
	my $out;
	$out = qx/tar -xzf $src -C $SOFT_IMAGE_DIR 2>&1/; 
	if ($? != 0) {
		die "Cannot create soft image: $!\n$out";
	}
	
	if (-x "$SOFT_IMAGE_DIR/start") {
		wlog DEBUG, "Running $SOFT_IMAGE_DIR/start\n";
		$out = qx/$SOFT_IMAGE_DIR\/start 2>&1/;
		if ($? != 0) {
			die "Error running soft image startup: $!\n$out";
		}
		if (dirname($src) eq $SOFT_IMAGE_DIR) {
			wlog DEBUG, "Image was staged in. Removing package.\n";
			unlink($src);
		}
		else {
			wlog DEBUG, "Image was NOT staged in\n";
		}
	}		
}

sub acquireSoftImageLock {
	if (!$SOFT_IMAGE_FIRST_IN_PROCESS) {
		wlog DEBUG, "Not first in process\n";
		return 0;
	}
	mkpath($SOFT_IMAGE_DIR);
	$SOFT_IMAGE_FIRST_IN_PROCESS = 0;
	my $mainLock = writeLock("$SOFT_IMAGE_DIR/.main");
	wlog DEBUG, "First in process\n";
	$SOFT_IMAGE_USE_LOCK = tryWriteLock("$SOFT_IMAGE_DIR/.use");
	if (defined $SOFT_IMAGE_USE_LOCK) {
		wlog DEBUG, "First process\n";
		unlock($SOFT_IMAGE_USE_LOCK);
		# nobody using this yet
		$SOFT_IMAGE_LEAD_PROCESS = 1;
		$SOFT_IMAGE_CREATE_LOCK = writeLock("$SOFT_IMAGE_DIR/.create");
		unlock($SOFT_IMAGE_USE_LOCK);
		$SOFT_IMAGE_USE_LOCK = readLock("$SOFT_IMAGE_DIR/.use");
		
		# make sure no errors from previous runs are there
		if (-f "$SOFT_IMAGE_DIR/.error") {
			unlink("$SOFT_IMAGE_DIR/.error");
		}
		
		return 1;
	}
	else {
		wlog DEBUG, "Not first process\n";
		$SOFT_IMAGE_USE_LOCK = readLock("$SOFT_IMAGE_DIR/.use");
		return 0;
	}
}

sub writeLock {
	my ($file) = @_;
	
	wlog DEBUG, "writeLock($file)\n";
	my $desc;
	open($desc, "+>>$file");
	if (!flock($desc, LOCK_EX)) {
		dieNicely("Failed to get exclusive lock");
	}
	return $desc;
}

sub readLock {
	my ($file) = @_;
	
	wlog DEBUG, "readLock($file)\n";
	my $desc;
	open($desc, "+>>$file");
	if (!flock($desc, LOCK_EX)) {
		dieNicely("Failed to get shared lock");
	}
	return $desc;
}

sub unlock {
	my ($desc) = @_;
	
	flock($desc, LOCK_UN);
	close($desc);
}

sub tryWriteLock {
	my ($file) = @_;
	
	wlog DEBUG, "writeLock($file)\n";
	my $desc;
	open($desc, "+>>$file");
	if (!flock($desc, LOCK_EX + LOCK_NB)) {
		close($desc);
		return undef;
	}
	else {
		return $desc;
	}
}

sub checkSoftimageJobFailure {
	my ($JOBID, $err) = @_;
	
	if ($JOBID == $SOFT_IMAGE_JOB_ID) {
		$SOFT_IMAGE_JOB_ID = -1;
		open(my $ERRF, ">$SOFT_IMAGE_DIR/.error");
		print $ERRF $err;
		close($ERRF);
		unlock($SOFT_IMAGE_CREATE_LOCK);
	}
}

sub cleanSoftImage {
	if (!defined $SOFT_IMAGE_DIR) {
		return;
	}
	my $softImageDir = $SOFT_IMAGE_DIR;
	# prevent recursive calls to this sub
	$SOFT_IMAGE_DIR = undef;
	
	my $mainLock = writeLock("$softImageDir/.main");
		
		unlock($SOFT_IMAGE_USE_LOCK);
		$SOFT_IMAGE_USE_LOCK = tryWriteLock("$softImageDir/.use");
		if (defined $SOFT_IMAGE_USE_LOCK) {
			wlog INFO, "Tail process. Removing image from $softImageDir\n";
		
			if (-x "$softImageDir/stop") {
				my $out = qx/$softImageDir\/stop 2>&1/;
				if ($? != 0) {
					die "Error running soft image shutdown: $!\n$out";
				}
			}
			
			rmtree($softImageDir, 0, 0);
			
			unlock($SOFT_IMAGE_USE_LOCK);
		}
		
	unlock($mainLock);
}

sub submitjob {
	my ($tag, $timeout, $msgs) = @_;
	my $desc = $$msgs[0];
	my @lines = split(/\n/, $desc);
	my $line;
	my $JOBID = undef;
	my $MAXWALLTIME = 600;
	my $PERFTRACE = 0;
	my $SOFTIMAGE;
	my %JOB = ();
	my @JOBARGS = ();
	my %JOBENV = ();
	my @STAGEIN = ();
	my @STAGEIND = ();
	my @STAGEOUT = ();
	my @STAGEOUTD = ();
	my @STAGEOUTM = ();
	my @CLEANUP = ();
	foreach $line (@lines) {
		$line =~ s/\\n/\n/g;
		$line =~ s/\\\\/\\/g;
		my @pair = split(/=/, $line, 2);
		if ($pair[0] eq "arg") {
			push @JOBARGS, $pair[1];
		}
		elsif ($pair[0] eq "env") {
			my @ep = split(/=/, $pair[1], 2);
			$JOBENV{"$ep[0]"} = $ep[1];
		}
		elsif ($pair[0] eq "identity") {
			$JOBID = $pair[1];
		}
		elsif ($pair[0] eq "attr") {
			my @ap = split(/=/, $pair[1], 2);
			if ($ap[0] eq "maxwalltime") {
				$MAXWALLTIME = $ap[1];
			}
			elsif ($ap[0] eq "tracePerformance") {
				$PERFTRACE = $ap[1];
				wlog INFO, "tracePerformance set (id=$PERFTRACE)\n";
				# as long as the job is alive, enable debugging
				$LOGLEVEL = DEBUG;
			}
			elsif ($ap[0] eq "softimage") {
				my @SS = split(/ /, $ap[1]);
				if (!defined $SOFT_IMAGE_DIR) {
					$SOFT_IMAGE_DIR = $SS[1];
				}
				if (acquireSoftImageLock()) {
					my ($proto, $host, $path) = urisplit($SS[0]);
					if ($proto eq "sfs") {
						# don't stage in; unpack directly
						$SOFTIMAGE = $path;
					}
					else {
						# treat the soft image as a normal stage-in file
						push @STAGEIN, $SS[0];
						my $fn = basename($path);
						my $dest = "$SOFT_IMAGE_DIR/$fn";
						push @STAGEIND, $dest;
						$SOFTIMAGE = $dest;
					}
					$SOFT_IMAGE_JOB_ID = $JOBID;
				}
				else {
					# prevent job from trying to unpack the image
					$SOFTIMAGE = "";
				}
			}
			else {
				wlog WARN, "Ignoring attribute $ap[0] = $ap[1]\n";
			}
		}
		elsif ($pair[0] eq "stagein") {
			my @pp = split(/\n/, $pair[1], 3);
			push @STAGEIN, $pp[0];
			if (isabsolute($pp[1])) {
				push @STAGEIND, $pp[1];
			}
			else {
				# there's the assumption here that the directory is sent before
				# the stagein/out data.
				push @STAGEIND, $JOB{directory}."/".$pp[1];
			}
		}
		elsif ($pair[0] eq "stageout") {
			my @pp = split(/\n/, $pair[1], 3);
			if (isabsolute($pp[0])) {
				push @STAGEOUT, $pp[0];
			}
			else {
				push @STAGEOUT, $JOB{directory}."/".$pp[0];
			}
			push @STAGEOUTD, $pp[1];
			push @STAGEOUTM, $pp[2];
		}
		elsif ($pair[0] eq "cleanup") {
			if (isabsolute($pair[1])) {
				push @CLEANUP, $pair[1];
			}
			else {
				push @CLEANUP, $JOB{directory}."/".$pair[1];
			}
		}
		else {
			$JOB{$pair[0]} = $pair[1];
		}
	}
	my $err = checkJob($tag, $JOBID, \%JOB);
	if ($err eq "") {
		$JOBDATA{$JOBID} = {
			stagein => \@STAGEIN,
			stageind => \@STAGEIND,
			stageindex => 0,
			job => \%JOB,
			jobargs => \@JOBARGS,
			jobenv => \%JOBENV,
			stageout => \@STAGEOUT,
			stageoutd => \@STAGEOUTD,
			stageoutm => \@STAGEOUTM,
			cleanup => \@CLEANUP,
			maxwalltime => $MAXWALLTIME,
			perftrace => $PERFTRACE,
			softimage => $SOFTIMAGE,
		};

		stagein($JOBID);
	}
	else {
		queueError($tag, ($err));
		checkSoftimageJobFailure($JOBID, $err);
	}
}

sub checkJob {
	my ($tag, $JOBID, $JOB) = @_;
	
	wlog INFO, "$JOBID Job info received (tag=$tag)\n";
	
	my $executable = $$JOB{"executable"};
	if (!(defined $JOBID)) {
		my $ds = hts($JOB);

		wlog DEBUG, "$JOBID Job details $ds\n";

		return "Missing job identity";
	}
	elsif (!(defined $executable)) {
		return "Missing executable";
	}
	else {
		my $dir = $$JOB{directory};
		if (!defined $dir) {
			$dir = ".";
		}
		my $dirlen = length($dir);
		my $cleanup = $$JOB{"cleanup"};
		my $c;
		foreach $c (@$cleanup) {
			if (substr($c, 0, $dirlen) ne $dir) {
				return "Cannot clean up outside of the job directory (cleanup: $c, jobdir: $dir)";
			}
		}
		chdir $dir;
		wlog DEBUG, "$JOBID Job check ok (dir: $dir)\n";
		wlog DEBUG, "$JOBID Sending submit reply (tag=$tag)\n";
		queueReply($tag, ("OK"));
		wlog DEBUG, "$JOBID Submit reply sent (tag=$tag)\n";
		return "";
	}
}

sub asyncRun {
	my ($logid, $timeout, $proc, $onStart, $onComplete) = @_;
	
	my ($PARENT_R, $CHILD_W);
	pipe($PARENT_R, $CHILD_W);
	
	my $pid = fork();
	
	if (defined($pid)) {
		if ($pid == 0) {
			close $PARENT_R;
			$proc->($CHILD_W);
			close $CHILD_W;
			exit 0;
		}
		else {
			wlog INFO, "$logid Forked process $pid. Waiting for its completion\n";
			if (defined $onStart) {
				$onStart->($pid);
			}
			close $CHILD_W;
			$ASYNC_RUNNING++;
			$ASYNC_WAIT_DATA{$pid} = {
				logid => $logid,
				pipe => $PARENT_R,
				startTime => time(),
				timeout => $timeout,
				onComplete => $onComplete,
			};
		}
	}
	else {
		$onComplete->(ERROR_PROCESS_FORK, "Could not fork child process");
	}
}


sub forkjob {
	my ($JOBID) = @_;
	my ($pid, $status);

	my $JOB = $JOBDATA{$JOBID}{"job"};
	my $JOBARGS = $JOBDATA{$JOBID}{"jobargs"};
	my $JOBENV = $JOBDATA{$JOBID}{"jobenv"};
	my $WORKERPID = $$;

        # allocate a jobslot here because we know we are starting exactly one job here
        # if we verify that we dont have more stageins than slots taking place at once,
        # we can move this to where the rest of the job options are set. Or we can place
        # the slot in the JOBWAITDATA. (FIXME: remove when validated)

	my $JOBSLOT = pop(@jobslots);
	if( ! defined($JOBSLOT) ) {
		wlog DEBUG, "Job $JOBID has undefined jobslot\n";
	}
	$JOBDATA{$JOBID}{"jobslot"} = $JOBSLOT;
	
	asyncRun($JOBID, $JOBDATA{$JOBID}{"maxwalltime"}, sub {
			my ($ERR) = @_;
			if ($JOBDATA{$JOBID}{"perftrace"} != 0) {
				stracerunjob($ERR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA{$JOBID});
			}
			else {
				runjob($ERR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA{$JOBID});
			}
		},
		sub {
			# onStart($pid)
			my ($pid) = @_;
			wlog DEBUG, "$JOBID running in pid $pid\n";
			$JOBDATA{$JOBID}{"pid"} = $pid;
			$JOBS_RUNNING++;
			if ($PROFILE) {
				push(@PROFILE_EVENTS, "FORK", $pid, time());
			}
		},
		sub {
			# onComplete($err, $msg)
			my ($err, $msg) = @_;
			
			$JOBDATA{$JOBID}{"exitcode"} = $err;
			$JOBDATA{$JOBID}{"exitmessage"} = $msg;
			
			if ($PROFILE) {
				push(@PROFILE_EVENTS, "TERM", $pid, time());
			}

			my $JOBSLOT = $JOBDATA{$JOBID}{"jobslot"};
			if ( defined $JOBSLOT ) {
				push @jobslots,$JOBSLOT;
			}
			
			$JOBS_RUNNING--;
			
			$JOB_COUNT++;
			stageout($JOBID);
		}
	);
}

my $SUBPROCESSES_DONE = 0;

sub JOBDONE {
	$SUBPROCESSES_DONE = 1;
	$SIG{CHLD} = \&JOBDONE;
	wlog DEBUG, "Got one SIGCHLD\n";
}

$SIG{CHLD} = \&JOBDONE;

sub checkSubprocesses {
	my $now = time();
	
	if (!$SUBPROCESSES_DONE) {
		if ($now - $LAST_SUBPROCESS_CHECK_TIME < SUBPROCESS_CHECK_INTERVAL) {
			return;
		}
	}
	else {
		wlog(INFO, "SIGCHLD received. Checking jobs\n");
	}
	$SUBPROCESSES_DONE = 0;
	$LAST_SUBPROCESS_CHECK_TIME = $now;
	
	if (!%ASYNC_WAIT_DATA) {
		return;
	}

	wlog DEBUG, "Checking subprocess status (total subprocesses: $ASYNC_RUNNING, jobs: $JOBS_RUNNING)\n";

	my @DELETEIDS = ();

	for my $pid (keys %ASYNC_WAIT_DATA) {
		if (checkSubprocessStatus($pid, $now)) {
			push @DELETEIDS, $pid;
		}
	}
	for my $i (@DELETEIDS) {
		delete $ASYNC_WAIT_DATA{$i};
	}
}

sub checkSubprocessStatus {
	my ($pid, $now) = @_;

	my $RD = $ASYNC_WAIT_DATA{$pid}{"pipe"};
	my $startTime = $ASYNC_WAIT_DATA{$pid}{"startTime"};
	my $logid = $ASYNC_WAIT_DATA{$pid}{"logid"};

	my $tid;
	my $status;
	my $msg;
	
	wlog DEBUG, "$logid Checking pid $pid\n";

	$tid = waitpid($pid, &WNOHANG);
	if ($tid != $pid) {
		# not done
		my $timeout = $ASYNC_WAIT_DATA{$pid}{"timeout"};
		if (($timeout > 0) && ($now > $startTime + $timeout)) {
			wlog DEBUG, "$logid subprocess timed-out (start: $startTime, now: $now, timeout: $timeout); killing\n"; 
			kill 9, $pid;
			# only kill it once
			$ASYNC_WAIT_DATA{$pid}{"startTime"} = -1;
			$ASYNC_WAIT_DATA{$pid}{"timedOut"} = 1;
		}
		else {
			wlog DEBUG, "$logid Job $pid still running\n";
		}
		return 0;
	}
	else {
		if ($ASYNC_WAIT_DATA{$pid}{"timedOut"}) {
			wlog DEBUG, "Walltime exceeded. The status is $?\n";
			$status = ERROR_PROCESS_WALLTIME_EXCEEDED;
			$msg = "Walltime exceeded";
		}
		else {
			# exit code is in MSB and signal in LSB, so
			# switch them such that status & 0xff is the
			# exit code
			$status = $? >> 8 + (($? & 0xff) << 8);
			$msg = "Process completed with exit code $status";
		}
	}

	wlog INFO, "$logid Child process $pid terminated. Status is $status.\n";
	my $s;
	my $l;
	while (!eof($RD)) {
		$l = <$RD>;
		$s = $s.$l;
	}
	wlog DEBUG, "$logid Got output from child. Closing pipe.\n";
	close $RD;
	if (defined $s) {
		$msg = $s;
		if ($status == 0) {
			# force non-zero status if some error message is present
			$status = 1;
		}
	}
	
	my $onComplete = $ASYNC_WAIT_DATA{$pid}{"onComplete"};
	$onComplete->($status, $msg);

	$ASYNC_RUNNING--;
	return 1;
}

sub tmpSFile {
	my ($pid, $suffix) = @_;
	
	return "/tmp/$pid.$suffix";
}

sub runjob {
	my ($WR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA) = @_;
	my $executable = $$JOB{"executable"};
	my $sout = $$JOB{"stdout"};
	my $serr = $$JOB{"stderr"};
	
	if ($SOFT_IMAGE_LEAD_PROCESS) {
		my $SOFTIMAGE = $$JOBDATA{"softimage"};
		if ($SOFTIMAGE ne "") {
			unpackSoftImage($$JOBDATA{"softimage"});
			wlog DEBUG, "Unlocking soft image\n";
			unlock($SOFT_IMAGE_CREATE_LOCK);
		}
	}
	
	if (defined $SOFT_IMAGE_DIR) {
		# wait until the soft image is created
		wlog DEBUG, "Waiting for soft image\n";
		my $createLock = readLock("$SOFT_IMAGE_DIR/.create");
		wlog DEBUG, "Got soft image\n";
		# no need to hold lock after that
		unlock($createLock);
		if (-f "$SOFT_IMAGE_DIR/.error") {
			open(my $ERRF, "<$SOFT_IMAGE_DIR/.error");
			my $err = "";
			while (<$ERRF>) {
				$err .= $_;
			}
			dieNicely("Soft image deployment failed: $err");
		}
		
		$ENV{SOFTIMAGE} = $SOFT_IMAGE_DIR;
	}
	
	my $cwd = getcwd();
	
	my $ename;
	foreach $ename (keys %$JOBENV) {
		$ENV{$ename} = $$JOBENV{$ename};
	}
    $ENV{"SWIFT_JOB_SLOT"} = $JOBSLOT;
    $ENV{"SWIFT_WORKER_PID"} = $WORKERPID;
	unshift @$JOBARGS, $executable;
	wlog DEBUG, "Command: @$JOBARGS\n";
	if (defined $$JOB{"directory"}) {
		wlog DEBUG, "chdir: $$JOB{directory}\n";
	    chdir $$JOB{directory};
	}
	if (defined $$JOB{"redirect"}) {
		wlog DEBUG, "Redirection is on\n";
		$sout = tmpSFile($$, "out");
		$serr = tmpSFile($$, "err");
	}
	if (defined $sout) {
		wlog DEBUG, "STDOUT: $sout\n";
		close STDOUT;
		open STDOUT, ">$sout" or dieNicely("Cannot redirect STDOUT");
	}
	if (defined $serr) {
		wlog DEBUG, "STDERR: $serr\n";
		close STDERR;
		open STDERR, ">$serr" or dieNicely("Cannot redirect STDERR");
	}
	close STDIN;
	
	#wlog DEBUG, "CWD: $cwd\n";
	#wlog DEBUG, "Running $executable\n";
	
	exec { $executable } @$JOBARGS or print $WR "Could not execute $executable: $!\n";
	die "Could not execute $executable: $!";
}

sub stracerunjob {
	my ($WR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA) = @_;
	my $executable = $$JOB{"executable"};

	my $LOGID = $$JOBDATA{"perftrace"};
	$$JOB{"executable"} = "strace";
	unshift @$JOBARGS, $executable;
	unshift @$JOBARGS, "$LOGDIR/$BLOCKID-$ID-$LOGID.perf";
	unshift @$JOBARGS, "-o";
	unshift @$JOBARGS, "-tt";
	unshift @$JOBARGS, "-f";
	unshift @$JOBARGS, "-T";
	
	runjob($WR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA);
}

sub processProbes {
	my ($in) = @_;
	
	my $current = 0;
	my $time;
	
	wlog DEBUG, "Processing probe data\n";
	
	my @lines = split /\n/, $in;
	foreach my $line (@lines) {
		$line = trim($line);
		wlog TRACE, "Probe line: $line\n";
		if ($line eq "") {
			$current = 0;
		}
		if ($current == 0 && $line =~ /^-/) {
			my @arr = split(/\s+/, $line);
			$time = $arr[1];
			if ($line =~ /^-CPU/) {
				$current = 1;
			}
			elsif ($line =~ /^-DF/) {
				$current = 2;
			}
			elsif ($line =~ /^-DL/) {
				$current = 3;
			}			
		}
		elsif ($current == 1) {
			calculateAndSendCPUStats($time, $line);
		}
		elsif ($current == 2) {
			calculateAndSendDFStats($time, $line);
		}
		elsif ($current == 3) {
			calculateAndSendDLStats($time, $line);
		}
	}
}

my $LAST_CPU_LINE;

sub calculateAndSendCPUStats {
	my ($time, $line) = @_;
	
	# cpu  6687663 4396 12825492 47536746 74378 10 2743 0 0 0
	#      user    nice system   idle     ....
	# the units are not important. Usage since last time is 
	# delta(user + nice + system) / delta(all)
	if (defined $LAST_CPU_LINE) {
		my @e1 = split(/\s+/, $LAST_CPU_LINE);
		my @e2 = split(/\s+/, $line);
		my $t1 = sum(1, -1, @e1);
		my $a1 = sum(1, 3, @e1);
		my $t2 = sum(1, -1, @e2);
		my $a2 = sum(1, 3, @e2);
		my $load = ($a2 - $a1) / ($t2 - $t1);
		
		queueCmd((nullCB(), "RLOG", "INFO", "PROBE type=CPU workerid=$BLOCKID:$ID time=$time load=$load"));
	}
	$LAST_CPU_LINE = $line;
}

sub sum {
	my ($start, $end, @a) = @_;
	
	if ($end == -1) {
		$end = (scalar @a) - 1;
		
	}
	my $s = 0;
	
	for (my $i = $start; $i <= $end; $i++) {
		$s = $s + $a[$i];
	}
	return $s;
}

sub calculateAndSendDFStats {
	my ($time, $line) = @_;
	
	if ($line =~ /^Filesystem/) {
		# header
		return;
	}
	
	my @els = split(/\s+/, $line);
	my $mount = $els[5];
	if (($mount =~ /^\/sys/) || ($mount =~ /^\/dev/) || $mount =~ (/^\/run/)) {
		return;
	}
	queueCmd((nullCB(), "RLOG", "INFO", "PROBE type=DF workerid=$BLOCKID:$ID time=$time mount=$mount fs=$els[0] used=$els[2] avail=$els[3]"));
}


my %LAST_DL_LINES = ();
my %SECTOR_SIZES = ();
sub calculateAndSendDLStats {
	my ($time, $line) = @_;
	
	if (!exists $SECTOR_SIZES{"initialized"}) {
		$SECTOR_SIZES{"initialized"} = 1;
		readSectorSizes();
	}
	my @els2 = split(/\s+/, $line);
	if (defined $LAST_DL_LINES{$els2[2]}) {
		#    0       1     2       3    4           5      6        7      8        9      10   11           12        13
		#    8       0   sda  696479 4064    19412366 241348   852846 631973 43482080 1408616   0        889264   1649296
		#    maj     min name reads  reads   sectors  ms spent writes writes sectors  ms spent  IOs in   ms spent weighted
		#                     compl. merged  read     reading  compl. merged written  writing   progress in IO    ms in IO
		# see https://www.kernel.org/doc/Documentation/iostats.txt
		my @els1 = split(/\s+/, $LAST_DL_LINES{$els2[2]}[0]);
		my $lastTime = $LAST_DL_LINES{$els2[2]}[1];
		
		my $ss = getSectorSize($els2[2]);
		my $wms = ($els2[10] - $els1[10]);
		my $rms = ($els2[6] - $els1[6]);
		
		my $rthroughput;
		my $wthroughput;
		
		# throughput is (bytes read|written) / (time spent reading|writing)
		if ($rms == 0) {
			$rthroughput = 0;
		}
		else {
			$rthroughput = $ss * ($els2[5] - $els1[5]) * 1000 / $rms;
		}
		if ($wms == 0) {
			$wthroughput = 0;
		}
		else {
			$wthroughput = $ss * ($els2[9] - $els1[9]) * 1000 / $wms;
		}
		
		# load is the time doing i/o out of the total time
		my $load = ($els2[12] - $els1[12]) / ($time - $lastTime) / 1000;
		
		queueCmd((nullCB(), "RLOG", "INFO", "PROBE type=DL workerid=$BLOCKID:$ID time=$time dev=$els2[2] wtr=$wthroughput rtr=$rthroughput load=$load"));
	}
	$LAST_DL_LINES{$els2[2]} = [$line, $time];
}

sub getSectorSize {
	my ($key) = @_;
	
	if (defined $SECTOR_SIZES{$key}) {
		return $SECTOR_SIZES{$key};
	}
	else {
		return 512;
	}
}

sub readSectorSizes {
	%SECTOR_SIZES = ();
	my $s = `lsblk -l -o NAME,PHY-SeC` || (wlog DEBUG, "Cannot get disk sector sizes: $!" && return);
	my @els = split(/\n/, $s);
	foreach (@els) {
		my @kv = split(/\s+/, $_);
		$SECTOR_SIZES{$kv[0]} = $kv[1];
	}
}

sub checkStartProbes {
	my $now = time();
	
	if ($now - $LAST_PROBE_TIME > $PROBE_INTERVAL) {
		$LAST_PROBE_TIME = $now;
		startProbes();
	}
}

sub startProbes {
	asyncRun("probes", -1, sub {
			my ($pipe) = @_;
			runProbes($pipe);
		},
		sub {
			my ($pid) = @_;
			wlog DEBUG, "Probes running in pid $pid\n";
		},
		sub {
			my ($err, $msg) = @_;
			processProbes($msg);
		}
	);
}

sub runProbes {
	my ($OUT) = @_;
	runCPUStatsProbe($OUT);
	runDiskUseProbe($OUT);
	runDiskLatencyProbe($OUT);
}

sub runCPUStatsProbe {
	my ($OUT) = @_;
	my $f;
	open($f, "/proc/stat") || (wlog DEBUG, "Cannot open /proc/stat: $!\n" && return);
	my $line = <$f>;
	close($f);
	print $OUT "-CPU ".time()."\n";
	print $OUT $line;
	print $OUT "\n\n";
}

sub runDiskUseProbe {
	my ($OUT) = @_;
	my $s = `df` || {wlog DEBUG, "Cannot run df: $!\n" && return};
	print $OUT "-DF ".time()."\n";
	print $OUT $s;
	print $OUT "\n\n";
}

sub runDiskLatencyProbe {
	my ($OUT) = @_;
	my $f;
	open($f, "/proc/diskstats") || (wlog DEBUG, "Cannot open /proc/diskstats: $!\n" && return);
	print $OUT "-DL ".time()."\n";
	while (my $line = <$f>) {
		# skip ram and loop devices
		if (index($line, "ram") != -1) {
			next;
		}
		if (index($line, "loop") != -1) {
			next;
		}
		print $OUT $line;
	}
	close($f);
	print $OUT "\n\n";
}

$ENV{"LANG"} = "C";

initlog();

my $MSG="0";

wlog(INFO, "Running on node $myhost\n");
# wlog(INFO, "New log name: $LOGNEW \n");

init();

mainloop();

# Code may not reach this point - see shutdownw()
wlog INFO, "Worker finished. Exiting.\n";
exit(0);

# Local Variables:
# indent-tabs-mode: t
# tab-width: 4
# End:

# perl-indent-level: 8
