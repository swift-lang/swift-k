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
	ERROR_STAGEIN_COPY => 524,
	ERROR_STAGEIN_REQUEST => 525,
	ERROR_STAGEOUT_COPY => 528,
	ERROR_STAGEOUT_SEND => 515,
	ERROR_STAGEOUT_TIMEOUT => 516,
	ERROR_PROCESS_FORK => 512,
	ERROR_PROCESS_WALLTIME_EXCEEDED => 513,
	ERROR_JOB_RUN_GENERIC_ERROR => 252,
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

use constant JOB_CHECK_INTERVAL => 0.1; # seconds

my $JOBS_RUNNING = 0;
my $LAST_JOB_CHECK_TIME = 0;
my $JOB_COUNT = 0;

my $SOFT_IMAGE_DST;

use constant BUFSZ => 2048;
use constant IOBUFSZ => 32768;
use constant IOBLOCKSZ => 8;

# Maximum size of re-directed output
use constant MAX_OUT_REDIR_SIZE => 2048;

# If true, enable a profile result that is written to the log
my $PROFILE = 0;
# Contains tuples (EVENT, PID, TIMESTAMP) (flattened)
my @PROFILE_EVENTS = ();

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
getopts("w:h", \%OPTS);

if (defined $OPTS{"h"}) {
	print "worker.pl <serviceURL> <blockID> <logdir> [-w <maxwalltime>]\n";
	exit(1);
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

my %JOBWAITDATA = ();
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
			queueCmd(registerCB(), "REGISTER", $BLOCKID, $myhost, "maxwalltime = $MAXWALLTIME");
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
			wlog INFO, "$tag Failed to read data from file: $!\n";
			return (FINAL_FLAG + ERROR_FLAG, "$!", CONTINUE);
		}
		elsif ($sz == 0 && $$state{"sent"} < $$state{"size"}) {
			wlog INFO, "$tag File size mismatch. $$state{'size'} vs. $$state{'sent'}\n";
			return (FINAL_FLAG + ERROR_FLAG, "File size mismatch. Expected $$state{'size'}, got $$state{'sent'}", CONTINUE);
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
	my ($cmd, $lname, $rname) = @_;

	my $desc;
	if (!open($desc, "<", "$lname")) {
		wlog WARN, "Failed to open $lname\n";
		# let it go on for now. The next read from the descriptor will fail
	}
	return {
		"cmd" => $cmd,
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
		wlog DEBUG, "Fin flag set\n";
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
	checkJobs();
	checkCommands();
	
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
	my ($jobid, $statusCode, $errorCode, $msg) = @_;
	
	queueCmd((nullCB(), "JOBSTATUS", $jobid, 
			encodeInt($statusCode), encodeInt($errorCode), $msg, NULL_TIMESTAMP));
}

sub queueJobStatusCmdExt {
	my ($jobid, $statusCode, $errorCode, $msg, $out, $err) = @_;
	
	queueCmd((nullCB(), "JOBSTATUS", $jobid, 
			encodeInt($statusCode), encodeInt($errorCode), $msg, NULL_TIMESTAMP, $out, $err));
}

sub dieNicely {
	my ($msg) = @_;
	
	cleanSoftImage();	
	wlog ERROR, "$msg\n";
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
			wlog(INFO, sprintf("PROFILE: %-5s %6d %.3f\n",
                               $event, $pidnum, $timestamp));
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
	
	select(undef, undef, undef, 1);
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
		wlog DEBUG, "$jobid getFileCBDataInIndirect error: $reply\n";
		queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_RECEIVE, "Error staging in file: $reply");
		delete($JOBDATA{$jobid});
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
			wlog DEBUG, "$jobid getFileCBDataIn FAILED 520 Error staging in file: $reply\n";
			queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_RECEIVE, "Error staging in file: $reply");
			delete($JOBDATA{$jobid});
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
			queueJobStatusCmd($jobid, STAGEIN, 0, "workerid=$ID");
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
			if (!copy($path, $dst)) {
				wlog DEBUG, "$jobid Error staging in $path to $dst: $!\n";
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEIN_COPY, "$@");
			}
			else {
				stagein($jobid);
			}
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
		$JOBDATA{$jobid}{"stageindex"} = 0;
		wlog INFO, "$jobid No more stageouts. Doing cleanup.\n";
		cleanup($jobid);
	}
	else {
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
		if (!$skip) {
			if (!defined($JOBDATA{$jobid}{"stagoutStatusSent"})) {
				wlog DEBUG, "$jobid Sending STAGEOUT status\n";
				queueJobStatusCmd($jobid, STAGEOUT, 0, "workerid=$ID");
				$JOBDATA{$jobid}{"jobStatusSent"} = 1;
			}
			my $rfile = $$STAGED[$STAGEINDEX];
			$JOBDATA{$jobid}{"stageindex"} = $STAGEINDEX + 1;
			wlog INFO, "$jobid Staging out $lfile (mode = $mode).\n";
			my ($protocol, $host, $path) = urisplit($rfile);
			if ($protocol eq "file" || $protocol eq "proxy") {
				# make sure we keep track of the total number of actual stageouts
				if (!defined $JOBDATA{$jobid}{"stageoutCount"}) {
					$JOBDATA{$jobid}{"stageoutCount"} = 0;
				}
				$JOBDATA{$jobid}{"stageoutCount"} += 1;
				wlog DEBUG, "$jobid Stagecount is $JOBDATA{$jobid}{stageoutCount}\n";

				queueCmdCustomDataHandling(putFileCB($jobid), fileData("PUT", $lfile, $rfile));
			}
			elsif ($protocol eq "sfs") {
				mkfdir($jobid, $path);
				if (!copy($lfile, $path)) {
					wlog DEBUG, "$jobid Error staging out $lfile to $path: $!\n";
					queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_COPY, "$!");
					return;
				}
				else {
					stageout($jobid);
				}
			}
			else {
				queueCmd((putFileCB($jobid), "PUT", pack("VV", 0, 0), $lfile, $rfile));
			}
			wlog DEBUG, "$jobid PUT sent.\n";
		}
		else {
			if ($skip == 1) {
				wlog INFO, "$jobid Skipping stageout of missing file ($lfile)\n";
			}
			elsif ($skip == 2) {
				wlog INFO, "$jobid Skipping stageout of file ($lfile) (ON_ERROR mode and job succeeded)\n";
			}
			elsif ($skip == 3) {
				wlog INFO, "$jobid Skipping stageout of file ($lfile) (ON_SUCCESS mode and job failed)\n";
			}
			$JOBDATA{$jobid}{"stageindex"} = $STAGEINDEX + 1;
			stageout($jobid);
		}
	}
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
		if (!defined($JOBDATA{$jobid}{"stageoutCount"}) || ($JOBDATA{$jobid}{"stageoutCount"} == 0)) {
			# there were no stageouts. Notification can be sent now
			wlog DEBUG, "$jobid There were no stageouts. Sending notification immediately\n";
			sendStatus($jobid);
		}
		else {
			# there were stageouts. Wait until all are acknowledged
			# as done by the client. And we keep track of the
			# count of stageouts that weren't acknowledged in
			# $JOBDATA{$jobid}{"stageoutCount"}
		}
	}

	if ($ec != 0) {
		wlog INFO, "$jobid Job data: ".hts($JOBDATA{$jobid})."\n";
		wlog INFO, "$jobid Job: ".hts($JOBDATA{$jobid}{'job'})."\n";
		wlog INFO, "$jobid Job dir ".`ls -al $JOBDATA{$jobid}{'job'}{'directory'}`."\n";
	}

	my $CLEANUP = $JOBDATA{$jobid}{"cleanup"};
	my $c;
	if ($ec == 0) {
		for $c (@$CLEANUP) {
			if ($c =~ /\/\.$/) {
				chop $c;
				chop $c;
			}
			wlog DEBUG, "$jobid Removing $c\n";
			rmtree($c, 0, 0);
			wlog DEBUG, "$jobid Removed $c\n";
		}
	}

	if (!ASYNC) {
		if ($ec == 0) {
			queueJobStatusCmd($jobid, COMPLETED, 0, "");
		}
		else {
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
		wlog DEBUG, "$tag putFileCBDataSent\n";
		my $jobid = $$state{"jobid"};
		if ($jobid != -1) {
			wlog DEBUG, "$tag Data sent, async is on. Staging out next file\n";
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
			wlog DEBUG, "$tag Stage out failed ($reply)\n";
			if ($timeout) {
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_TIMEOUT, "Stage out failed ($reply)");
			}
			else {
				queueJobStatusCmd($jobid, FAILED, ERROR_STAGEOUT_SEND, "Stage out failed ($reply)");
			}
			delete($JOBDATA{$jobid});
		}
		return;
	}
	elsif ($reply eq "STOP") {
		$SUSPENDED_TRANSFERS{"$tag"} = 1;
		wlog DEBUG, "$tag Got stop request. Suspending transfer.\n";
	}
	elsif ($reply eq "CONTINUE") {
		delete $SUSPENDED_TRANSFERS{"$tag"};
		wlog DEBUG, "$tag Got continue request. Resuming transfer.\n";
	}
	elsif ($jobid != -1) {
		# OK reply from client
		if (!ASYNC) {
			wlog DEBUG, "$tag Stageout done; staging out next file\n";
			stageout($jobid);
		}
		else {
			wlog DEBUG, "$jobid Stageout done; stagecount is $JOBDATA{$jobid}{stageoutCount}\n";
			$JOBDATA{$jobid}{"stageoutCount"} -= 1;
			if ($JOBDATA{$jobid}{"stageoutCount"} == 0) {
				wlog DEBUG, "$jobid All stageouts acknowledged. Sending notification\n";
				sendStatus($jobid);
			}
		}
	}
}

sub isabsolute {
	my ($fn) = @_;

	return substr($fn, 0, 1) eq "/";
}

sub readUInt32 {
	my ($fd) = @_;
	seek($fd, 0, 0) or wlog ERROR, "Seek failed: $!\n";
	my $n;
	read($fd, $n, 4) or wlog ERROR, "Read failed: $!\n";
	return unpack("V", $n);
}

sub writeUInt32 {
	my ($fd, $n) = @_;
	
	seek($fd, 0, 0) or wlog ERROR, "Seek failed: $!\n";
	if (!(print {$fd} pack("V", $n))) {
		wlog ERROR, "Write failed: $!\n";
	}
	
	return $n;
}

# Ensures that each job has the $src tar.gz file unpacked in
# the directory pointed to by $dst. In addition, ensure that
# the unpacking is only done if necessary so that it can be
# shared between: 1. workers running concurrently on this node
# (if $dst points to a local disk) and 2. subsequent jobs
# running in this worker.
#
# If the image contains commonly used binaries and libraries,
# it can be combined with a $PATH and $LD_LIBRARY_PATH settings
# to minimize access to network file systems that would otherwise
# be the sources for those binaries/libraries.  
sub prepareSoftImage {
	my ($WR, $src, $dst) = @_;
	my $lock;
	my $counter;
	wlog DEBUG, "Preparing soft image...\n";
	if (!mkpath($dst)) {
		if (! -d $dst) {
			jobDie($WR, "Failed to create softimage directory: $!\n");
		}
	}
	if (!open($lock, ">>$dst/.lock")) {
		jobDie($WR, "Cannot open lock file: $!");
	}
	# start critical section
	if (!flock($lock, 2)) { # 2 - exclusive lock
		jobDie($WR, "Cannot get exclusive lock on soft image directory: $!"); 
	}
	if (! -f "$dst/.count") {
		open($counter, "+>$dst/.count");
	}
	else {
		open($counter, "+<$dst/.count");
	}
	
	seek($counter, 0, 2);
	my $pos = tell($counter);
	wlog DEBUG, "Counter file pos: $pos\n";
	if ($pos == 0) {
		wlog INFO, "Lead process. Uncompressing image $src to $dst\n";
		wlog DEBUG, "Running tar -xzf $src -C $dst\n";
		my $out;
		$out = qx/tar -xzf $src -C $dst 2>&1/;
		wlog DEBUG, "EC: $?, out: $out\n"; 
		if ($? != 0) {
			jobDie($WR, "Cannot create soft image: $!\n$out");
		}
		$ENV{SOFTIMAGE} = $dst;
		if (-x "$dst/start") {
			wlog DEBUG, "Running $dst/start\n";
			$out = qx/$dst\/start 2>&1/;
			if ($? != 0) {
				die "Error running soft image startup: $!\n$out";
			}
		}
		
		writeUInt32($counter, 1);
		wlog DEBUG, "Soft image use count updated: 1\n";
		wlog DEBUG, "Soft image initialized\n";
	}
	else {
		wlog DEBUG, "Not lead process\n";
		if (! -f "$dst/.l$BLOCKID") {
			my $n = writeUInt32($counter, readUInt32($counter) + 1);
			wlog DEBUG, "Soft image use count updated: $n\n";
			my $rl;
			open($rl, ">$dst/.l$BLOCKID");
			close($rl);
		}
	}
	close($counter);
	close($lock);
	# end critical section
}

sub cleanSoftImage() {
	my $lock;
	my $counter;
	if (!defined $SOFT_IMAGE_DST) {
		return;
	}
	open($lock, ">>$SOFT_IMAGE_DST/.lock");
	if (!flock($lock, 2)) {
		dieNicely("Cannot get exclusive lock on soft image directory: $!"); 
	}
	
	open($counter, "+<$SOFT_IMAGE_DST/.count");
	if (writeUInt32($counter, readUInt32($counter) - 1) == 0) {
		wlog INFO, "Tail process. Removing image from $SOFT_IMAGE_DST\n";
		
		if (-x "$SOFT_IMAGE_DST/stop") {
			my $out = qx/$SOFT_IMAGE_DST\/stop 2>&1/;
			if ($? != 0) {
				die "Error running soft image shutdown: $!\n$out";
			}
		}
		
		rmtree($SOFT_IMAGE_DST, 0, 0);
	}
	close($counter);
	close($lock);
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
			elsif ($ap[0] eq "softImage") {
				$SOFTIMAGE = $ap[1];
				my @SS = split(/ /, $SOFTIMAGE);
				$SOFT_IMAGE_DST = $SS[1];	
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
	if (checkJob($tag, $JOBID, \%JOB)) {
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
}

sub checkJob() {
	my ($tag, $JOBID, $JOB) = @_;
	
	wlog INFO, "$JOBID Job info received (tag=$tag)\n";
	
	my $executable = $$JOB{"executable"};
	if (!(defined $JOBID)) {
		my $ds = hts($JOB);

		wlog DEBUG, "$JOBID Job details $ds\n";

		queueError($tag, ("Missing job identity"));
		return 0;
	}
	elsif (!(defined $executable)) {
		queueError($tag, ("Missing executable"));
		return 0;
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
				queueError($tag, ("Cannot clean up outside of the job directory (cleanup: $c, jobdir: $dir)"));
				return 0;
			}
		}
		chdir $dir;
		wlog DEBUG, "$JOBID Job check ok (dir: $dir)\n";
		wlog DEBUG, "$JOBID Sending submit reply (tag=$tag)\n";
		queueReply($tag, ("OK"));
		wlog DEBUG, "$JOBID Submit reply sent (tag=$tag)\n";
		return 1;
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

	my ($PARENT_R, $CHILD_W);
	pipe($PARENT_R, $CHILD_W);

	$pid = fork();
	
	if (defined($pid)) {
		if ($pid == 0) {
			close $PARENT_R;
			if ($JOBDATA{$JOBID}{"perftrace"} != 0) {
				stracerunjob($CHILD_W, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA{$JOBID});
			}
			else {
				runjob($CHILD_W, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA{$JOBID});
			}
			close $CHILD_W;
			exit 0;
		}
		else {
			wlog INFO, "$JOBID Forked process $pid. Waiting for its completion\n";
			close $CHILD_W;
			$JOBDATA{$JOBID}{"pid"} = $pid;
			$JOBS_RUNNING++;
			$JOBWAITDATA{$JOBID} = {
				pid => $pid,
				pipe => $PARENT_R,
				startTime => time()
			};
			if ($PROFILE) {
				push(@PROFILE_EVENTS, "FORK", $pid, time());
			}
		}
	}
	else {
		queueJobStatusCmd($JOBID, FAILED, ERROR_PROCESS_FORK, "Could not fork child process");
	}
}

my $JOBSDONE = 0;

sub JOBDONE {
	$JOBSDONE = 1;
	$SIG{CHLD} = \&JOBDONE;
	wlog DEBUG, "Got one SIGCHLD\n";
}

$SIG{CHLD} = \&JOBDONE;

sub checkJobs {
	my $now = time();
	
	if (!$JOBSDONE) {
		if ($now - $LAST_JOB_CHECK_TIME < JOB_CHECK_INTERVAL) {
			return;
		}
	}
	else {
		wlog(INFO, "SIGCHLD received. Checking jobs\n");
	}
	$JOBSDONE = 0;
	$LAST_JOB_CHECK_TIME = $now;
	
	if (!%JOBWAITDATA) {
		return;
	}

	wlog DEBUG, "Checking jobs status ($JOBS_RUNNING active)\n";

	my @DELETEIDS = ();

	for my $JOBID (keys %JOBWAITDATA) {
		if (checkJobStatus($JOBID, $now)) {
			push @DELETEIDS, $JOBID;
		}
	}
	for my $i (@DELETEIDS) {
		delete $JOBWAITDATA{$i};
	}
}

sub checkJobStatus {
	my ($JOBID, $now) = @_;


	my $pid = $JOBWAITDATA{$JOBID}{"pid"};
	my $RD = $JOBWAITDATA{$JOBID}{"pipe"};
	my $startTime = $JOBWAITDATA{$JOBID}{"startTime"};

	my $tid;
	my $status;

	wlog DEBUG, "$JOBID Checking pid $pid\n";

	$tid = waitpid($pid, &WNOHANG);
	wlog DEBUG, "tid: $tid\n";
	if ($tid != $pid) {
		# not done
		my $mwt = $JOBDATA{$JOBID}{"maxwalltime"};
		if ($now > $startTime + $mwt) {
			wlog DEBUG, "$JOBID walltime exceeded (start: $startTime, now: $now, maxwalltime: $mwt); killing\n"; 
			kill 9, $pid;
			# only kill it once
			$JOBWAITDATA{$JOBID}{"startTime"} = -1;
			$JOBWAITDATA{$JOBID}{"walltimeexceeded"} = 1;
			$JOBDATA{$JOBID}{"exitmessage"} = "Walltime exceeded"; 
		}
		else {
			wlog DEBUG, "$JOBID Job $pid still running\n";
		}
		return 0;
	}
	else {
		if ($JOBWAITDATA{$JOBID}{"walltimeexceeded"}) {
			wlog DEBUG, "Walltime exceeded. The status is $?\n";
			$status = ERROR_PROCESS_WALLTIME_EXCEEDED;
		}
		else {
			# exit code is in MSB and signal in LSB, so
			# switch them such that status & 0xff is the
			# exit code
			$status = $? >> 8 + (($? & 0xff) << 8);
		}
	}

	wlog INFO, "$JOBID Child process $pid terminated. Status is $status.\n";
	my $s;
	while (<$RD>) {
		$s = "$s$_";
	}
	if (defined $s) {
		$JOBDATA{$JOBID}{"exitmessage"} = $s;
	}
	wlog DEBUG, "$JOBID Got output from child ($s). Closing pipe.\n";
	close $RD;
	$JOBDATA{$JOBID}{"exitcode"} = $status;

	if ($PROFILE) {
		push(@PROFILE_EVENTS, "TERM", $pid, time());
	}

	my $JOBSLOT = $JOBDATA{$JOBID}{"jobslot"};
	if ( defined $JOBSLOT ) {
		push @jobslots,$JOBSLOT;
	}

	stageout($JOBID);

	$JOB_COUNT++;
	$JOBS_RUNNING--;
	return 1;
}

sub tmpSFile {
	my ($pid, $suffix) = @_;
	
	return "/tmp/$pid.$suffix";
}

sub jobDie {
	my ($WR, $msg) = @_;
	
	print $WR $msg;
	wlog DEBUG, "Job die: $msg\n";
	exit ERROR_JOB_RUN_GENERIC_ERROR;
}

sub runjob {
	my ($WR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID, $JOBDATA) = @_;
	my $executable = $$JOB{"executable"};
	my $sout = $$JOB{"stdout"};
	my $serr = $$JOB{"stderr"};
	
	my $softImage = $$JOBDATA{"softimage"};
	if (defined $softImage) {
		prepareSoftImage($WR, split(/ /, $softImage));
	}

	my $cwd = getcwd();
	# wlog DEBUG, "CWD: $cwd\n";
	# wlog DEBUG, "Running $executable\n";
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
		open STDOUT, ">$sout" or jobDie($WR, "Cannot redirect STDOUT");
	}
	if (defined $serr) {
		wlog DEBUG, "STDERR: $serr\n";
		close STDERR;
		open STDERR, ">$serr" or jobDie($WR, "Cannot redirect STDERR");
	}
	close STDIN;

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
