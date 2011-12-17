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
use File::Basename;
use File::Path;
use File::Copy;
use Getopt::Std;
use FileHandle;
use Cwd;
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
use constant REPLYTIMEOUT => 180;
use constant MAXFRAGS => 16;
use constant MAX_RECONNECT_ATTEMPTS => 3;
use constant NEVER => 9999999999;

use constant JOB_CHECK_SKIP => 32;

my $JOBS_RUNNING = 0;

my $JOB_COUNT = 0;

use constant BUFSZ => 2048;
use constant IOBUFSZ => 32768;
use constant IOBLOCKSZ => 8;

# 60 seconds by default. Note that since there is no configuration handshake
# this would have to match the default interval in the service in order to avoid
# "lost heartbeats".
use constant HEARTBEAT_INTERVAL => 30;

# If true, enable a profile result that is written to the log
my $PROFILE = 0;
# Contains tuples (EVENT, PID, TIMESTAMP) (flattened)
my @PROFILE_EVENTS = ();

my $ID = "-";

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
#	time:  last communication time (used to determine timeouts)
#

my $LOG = logfilename($LOGDIR, $BLOCKID);

my %HANDLERS = (
	"SHUTDOWN"  => \&shutdownw,
	"SUBMITJOB" => \&submitjob,
	"REGISTER"  => \&register,
	"HEARTBEAT" => \&heartbeat,
	"WORKERSHELLCMD" => \&workershellcmd,
);

my @CMDQ = ();

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
	my $i;
	my $j;
	for ($i = 0; $i < MAX_RECONNECT_ATTEMPTS; $i++) {
		wlog INFO, "Connecting ($i)...\n";
		my $sz = @HOSTNAME;
		$success = 0;
		for ($j = 0; $j < $sz; $j++) {
			wlog DEBUG, "Trying $HOSTNAME[$j]:$PORT[$j]...\n";
			$fail = 0;
			$SOCK = IO::Socket::INET->new(Proto=>'tcp', PeerAddr=>$HOSTNAME[$j], PeerPort=>$PORT[$j], Blocking=>1) || ($fail = 1);
			if (!$fail) {
				$success = 1;
				last;
			}
			else {
				wlog DEBUG, "Connection failed: $!. Trying other addresses\n";
			}
		}
		if ($success) {
			$SOCK->setsockopt(SOL_SOCKET, SO_RCVBUF, 32768);
			$SOCK->setsockopt(SOL_SOCKET, SO_SNDBUF, 32768*8);
			wlog INFO, "Connected\n";
			$SOCK->blocking(0);
			# myhost is used by the CoasterService for MPI tasks
			queueCmd(registerCB(), "REGISTER", $BLOCKID, $myhost, "maxwalltime = $MAXWALLTIME");
			last;
		}
		else {
			my $delay = 2 ** $i;
			wlog ERROR, "Connection failed for all addresses. Retrying in $delay seconds\n";
			select(undef, undef, undef, $delay);
		}
	}
	if (!$success) {
		die "Failed to connect: $!";
	}
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
	reconnect();
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

sub trim {
	my ($arg) = @_;
	$arg =~ s/^\s+|\s+$//g ;
	return $arg;
}

sub sendm {
	my ($tag, $flags, $msg) = @_;
	my $len = length($msg);
	my $buf = pack("VVVVV", $tag, $flags, $len, ($tag ^ $flags ^ $len), 0);
	$buf = $buf.$msg;

	wlog(DEBUG, "OUT: len=$len, tag=$tag, flags=$flags\n");
	wlog(TRACE, "$msg\n");

	$SOCK->blocking(1);
	eval { defined($SOCK->send($buf)); } or wlog(WARN, "Send failed: $!\n") and die "Send failed: $!";

	#eval {defined($SOCK->send($buf))} or wlog(WARN, "Send failed: $!\n");
}

sub sendFrags {
	my ($tag, $flg, $data) = @_;

	my $flg2;
	my $msg;
	my $yield;
	
	do {
		($flg2, $msg, $yield) = $$data{"nextData"}($data);
		if (defined($msg)) {
			sendm($tag, $flg | $flg2, $msg);
		}
	} while (($flg2 & FINAL_FLAG) == 0 && !$yield);

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
		die "Index out of bounds";
	}
	return ($index >= $#$data ? FINAL_FLAG : 0, $$data[$index], CONTINUE);
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
		return (0, $$state{"cmd"}, CONTINUE);
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


sub sendCmdInt {
	my ($cont, $state) = @_;
	my $ctag = $$state{"tag"};
	if (!defined $ctag) {
		$ctag =  $TAG++;
		registerCmd($ctag, $cont);
		# make the tag accessible to the data generator
		$$state{"tag"} = $ctag;
	}
	sendFrags($ctag, 0, $state);
	return $ctag;
}

sub sendCmd {
	my @cmd = @_;
	my $cont = shift(@cmd);
	return sendCmdInt($cont, arrayData(@cmd));
}

sub queueCmd {
	my @cmd = @_;
	my $cont = shift(@cmd);
	# $cont is the continuation (what gets called when a reply is received)
	push @CMDQ, [$cont, arrayData(@cmd)];
}

sub queueCmdCustomDataHandling {
	my ($cont, $state) = @_;
	push @CMDQ, [$cont, $state];
}

sub sendReply {
	my ($tag, @msgs) = @_;
	sendFrags($tag, REPLY_FLAG, arrayData(@msgs));
}

sub sendError {
	my ($tag, @msgs) = @_;
	sendFrags($tag, REPLY_FLAG | ERROR_FLAG, arrayData(@msgs));
}

sub unpackData {
	my ($data) = @_;

	my $lendata = length($data);
	if ($lendata < 20) {
		wlog WARN, "Received faulty message (length < 20: $lendata)\n";
		die "Received faulty message (length < 20: $lendata)";
	}
	my $tag = unpack("V", substr($data, 0, 4));
	my $flg = unpack("V", substr($data, 4, 4));
	my $len = unpack("V", substr($data, 8, 4));
	my $hcsum = unpack("V", substr($data, 12, 4));
	my $csum = unpack("V", substr($data, 16, 4));
	
	my $chcsum = ($tag ^ $flg ^ $len);
	
	if ($chcsum != $hcsum) {
		wlog WARN, "Header checksum failed. Computed checksum: $chcsum, checksum: $hcsum\n";
		return;
	}
	
	my $msg;
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
		wlog(WARN, "len != actuallen\n");
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
		sendError($tag, ("Timed out waiting for all fragments"));
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
			sendError($tag, ("Unknown command: $cmd"));
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

sub checkTimeouts2 {
	my ($hash) = @_;

	my $now = time();
	my @del = ();

	my $k;
	my $v;

	while (($k, $v) = each(%$hash)) {
		if ($now - $$v[1] > REPLYTIMEOUT) {
			push(@del, $k);
			my $cont = $$v[0];
			$$cont{"dataIn"}($cont, $k, 1, 0, 0, "Reply timeout");
		}
	}

	foreach $k (@del) {
		delete $$hash{$k};
	}
}

my $LASTTIMEOUTCHECK = 0;

sub checkTimeouts {
	my $time = time();
	if ($time - $LASTTIMEOUTCHECK < 1) {
		return;
	}
	$LASTTIMEOUTCHECK = $time;
	checkTimeouts2(\%REQUESTS);
	checkTimeouts2(\%REPLIES);
}

my $DATA = "";

sub recvOne {
	my $buf;
	$SOCK->blocking(0);
	$SOCK->recv($buf, 20 - length($DATA));
	if (length($buf) > 0) {
		$DATA = $DATA . $buf;
		if (length($DATA) == 20) {
			# wlog DEBUG, "Received " . unpackData($DATA) . "\n";
			eval { process(unpackData($DATA)); } || (wlog ERROR, "Failed to process data: $@\n" && die "Failed to process data: $@");
			$DATA = "";
			return;
		}
	}
	else {
		#sleep 1ms
		select(undef, undef, undef, 0.001);
		checkTimeouts();
	}
}

sub registerCmd {
	my ($tag, $cont) = @_;

	wlog DEBUG, "Replies: ".hts(\%REPLIES)."\n";

	$REPLIES{$tag} = [$cont, time(), ()];
}


sub mainloop {
	while(1) {
		loopOne();
	}
}

sub loopOne {
	my $cmd;
	if (time() - $LAST_HEARTBEAT > HEARTBEAT_INTERVAL) {
		queueCmd(heartbeatCB(), "HEARTBEAT");
		$LAST_HEARTBEAT = time();
	}
	# send whatever is now queued; don't clear the queue, since
	# things may be added to it while stuff is being sent
	my $sz = scalar(@CMDQ);
	for (my $i = 0; $i < $sz; $i++)  {
		$cmd = shift(@CMDQ);
		sendCmdInt(@$cmd);
	}
	checkJobs();
	recvOne();
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
		die "Failed to register (timeout)\n";
	}
	elsif ($flags & ERROR_FLAG) {
		die "Failed to register (service returned error: ".join("\n", $reply).")";
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
			wlog WARN, "No heartbeats received in a while. Dying.\n";
			die "Lost heartbeat\n";
		}
	}
	elsif ($flags & ERROR_FLAG) {
		wlog WARN, "Heartbeat failed: $reply\n";
		die "Heartbeat failed: $reply\n";
	}
	else {
		wlog DEBUG, "Heartbeat acknowledged\n";
	}
}


sub register {
	my ($tag, $timeout, $reply) = @_;
	sendReply($tag, ("OK"));
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
	sendReply($tag, ("OK"));
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
	my $msg = int(time() * 1000);
	sendReply($tag, ("$msg"));
}

sub workershellcmd {
	my ($tag, $timeout, $msgs) = @_;
	my $cmd = $$msgs[1];
	my $out;
	if ($cmd =~ m/cd\s*(.*)/) {
		wlog DEBUG, "chdir $1\n";
		chdir $1;
		if ($! ne '') {
			sendError($tag, ("$!"));
		}
		else {
			sendReply($tag, ("OK", ""));
		}
	}
	elsif ($cmd =~ m/mls\s*(.*)/) {
		wlog DEBUG, "mls $1\n";
		$out = `ls -d $1 2>/dev/null`;
		sendReply($tag, ("OK", "$out"));
	}
	else {
		wlog DEBUG, "workershellcmd: $cmd\n";
		$out = `$cmd 2>&1`;
		wlog TRACE, "result: $out\n";
		sendReply($tag, ("OK", "$out"));
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
		die "$jobid Cannot create directory $dir. A file with this name already exists";
	}
	if (!-d $dir) {
		wlog DEBUG, "Creating directory $dir\n";
		if (!mkpath($dir)) {
			wlog WARN, "Cannot create directory $dir. $!\n";
			die "Cannot create directory $dir. $!";
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
			die "Failed to open $dst: $!";
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
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "520", "Error staging in file: $reply"));
		delete($JOBDATA{$jobid});
		return;
	}
	elsif ($timeout) {
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "521", "Timeout staging in file"));
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
		wlog DEBUG, "$jobid getFileCBDataIn FAILED 520 Error staging in file: $reply\n";
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "520", "Error staging in file: $reply"));
		delete($JOBDATA{$jobid});
		return;
	}
	elsif ($timeout) {
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "521", "Timeout staging in file"));
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
		if (!(print {$handle} $reply)) {
			close $handle;
			wlog DEBUG, "$jobid Could not write to file: $!. Descriptor was $handle; lfile: $$state{'lfile'}\n";
			queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "522", "Could not write to file: $!"));
			delete($JOBDATA{$jobid});
			return;
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
		wlog DEBUG, "$jobid Done staging in files ($STAGEINDEX, $STAGE)\n";
		$JOBDATA{$jobid}{"stageindex"} = 0;
		sendCmd((nullCB(), "JOBSTATUS", $jobid, ACTIVE, "0", "workerid=$ID"));
		forkjob($jobid);
	}
	else {
		if ($STAGEINDEX == 0) {
			sendCmd((nullCB(), "JOBSTATUS", $jobid, STAGEIN, "0", "workerid=$ID"));
		}
		wlog DEBUG, "$jobid Staging in $$STAGE[$STAGEINDEX]\n";
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
				queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "524", "$@"));
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
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "524", "$@"));
	}
	else {
		sendCmd(($state, "GET", $src, $dst));
	}
}

sub getPinnedFile() {
	my ($jobid, $src, $dst) = @_;

	wlog DEBUG, "Handling pinned file: $src\n";
	my $error;
	$src =~ s/pinned://;
	my $jobdir = $JOBDATA{$jobid}{'job'}{'directory'};
	my $pinned_dir = "$jobdir/../pinned";
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
				die "mkPinnedDirectory(): " .
				"Could not mkdir: $pinned_dir\n";
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
			die "getPinnedFile(): Could not link: $pinned_dir$rdst\n";
	}
}

sub linkToPinnedFile() {
	my ($jobid, $dst, $rdst, $pinned_dir) = @_;
	wlog DEBUG, "link: $pinned_dir$rdst -> $dst\n";
	my $dir = dirname($dst);
	if (! -d $dir) {
		wlog DEBUG, "mkpath: $dir\n";
		mkpath($dir) ||
			die "getPinnedFile(): Could not mkdir: $dir\n";
	}
	link("$pinned_dir$rdst", $dst) ||
		die "getPinnedFile(): Could not link!\n";
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
		wlog DEBUG, "$jobid No more stageouts. Doing cleanup.\n";
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
				sendCmd((nullCB(), "JOBSTATUS", $jobid, STAGEOUT, "0", "workerid=$ID"));
				$JOBDATA{$jobid}{"jobStatusSent"} = 1;
			}
			my $rfile = $$STAGED[$STAGEINDEX];
			$JOBDATA{$jobid}{"stageindex"} = $STAGEINDEX + 1;
			wlog DEBUG, "$jobid Staging out $lfile (mode = $mode).\n";
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
					queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "528", "$!"));
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
				wlog DEBUG, "$jobid Skipping stageout of missing file ($lfile)\n";
			}
			elsif ($skip == 2) {
				wlog DEBUG, "$jobid Skipping stageout of file ($lfile) (ON_ERROR mode and job succeeded)\n";
			}
			elsif ($skip == 3) {
				wlog DEBUG, "$jobid Skipping stageout of file ($lfile) (ON_SUCCESS mode and job failed)\n";
			}
			$JOBDATA{$jobid}{"stageindex"} = $STAGEINDEX + 1;
			stageout($jobid);
		}
	}
}

sub sendStatus {
	my ($jobid) = @_;
	
	my $ec = $JOBDATA{$jobid}{"exitcode"};
	
	if ($ec == 0) {
		queueCmd((nullCB(), "JOBSTATUS", $jobid, COMPLETED, "0", ""));
	}
	else {
		queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "$ec", "Job failed with an exit code of $ec"));
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
		wlog DEBUG, "$jobid Job data: ".hts($JOBDATA{$jobid})."\n";
		wlog DEBUG, "$jobid Job: ".hts($JOBDATA{$jobid}{'job'})."\n";
		wlog DEBUG, "$jobid Job dir ".`ls -al $JOBDATA{$jobid}{'job'}{'directory'}`."\n";
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
			queueCmd((nullCB(), "JOBSTATUS", $jobid, COMPLETED, "0", ""));
		}
		else {
			wlog DEBUG, "$jobid Sending failure.\n";
			queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "$ec", "Job failed with and exit code of $ec"));
		}
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
			queueCmd((nullCB(), "JOBSTATUS", $jobid, FAILED, "515", "Stage out failed ($reply)"));
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


sub submitjob {
	my ($tag, $timeout, $msgs) = @_;
	my $desc = $$msgs[0];
	my @lines = split(/\n/, $desc);
	my $line;
	my $JOBID = undef;
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
		};

		stagein($JOBID);
	}
}

sub checkJob() {
	my ($tag, $JOBID, $JOB) = @_;

	wlog DEBUG, "$JOBID Job info received (tag=$tag)\n";
	my $executable = $$JOB{"executable"};
	if (!(defined $JOBID)) {
		my $ds = hts($JOB);

		wlog DEBUG, "$JOBID Job details $ds\n";

		sendError($tag, ("Missing job identity"));
		return 0;
	}
	elsif (!(defined $executable)) {
		sendError($tag, ("Missing executable"));
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
				sendError($tag, ("Cannot clean up outside of the job directory (cleanup: $c, jobdir: $dir)"));
				return 0;
			}
		}
		chdir $dir;
		wlog DEBUG, "$JOBID Job check ok (dir: $dir)\n";
		wlog DEBUG, "$JOBID Sending submit reply (tag=$tag)\n";
		sendReply($tag, ("OK"));
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
			runjob($CHILD_W, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID);
			close $CHILD_W;
		}
		else {
			wlog DEBUG, "$JOBID Forked process $pid. Waiting for its completion\n";
			close $CHILD_W;
			$JOBS_RUNNING++;
			$JOBWAITDATA{$JOBID} = {
				pid => $pid,
				pipe => $PARENT_R
				};
			if ($PROFILE) {
				push(@PROFILE_EVENTS, "FORK", $pid, time());
			}
		}
	}
	else {
		queueCmd(nullCB(), "JOBSTATUS", $JOBID, FAILED, "512", "Could not fork child process");
	}
}

my $JOBCHECKCOUNT = 0;

sub checkJobs {
	$JOBCHECKCOUNT = ($JOBCHECKCOUNT + 1) % JOB_CHECK_SKIP;
	if ($JOBCHECKCOUNT != 0) {
		return;
	}
	if (!%JOBWAITDATA) {
		return;
	}

	wlog DEBUG, "Checking jobs status ($JOBS_RUNNING active)\n";

	my @DELETEIDS = ();

	for my $JOBID (keys %JOBWAITDATA) {
		if (checkJobStatus($JOBID)) {
			push @DELETEIDS, $JOBID;
		}
	}
	for my $i (@DELETEIDS) {
		delete $JOBWAITDATA{$i};
	}
}

sub checkJobStatus {
	my ($JOBID) = @_;


	my $pid = $JOBWAITDATA{$JOBID}{"pid"};
	my $RD = $JOBWAITDATA{$JOBID}{"pipe"};

	my $tid;
	my $status;

	wlog DEBUG, "$JOBID Checking pid $pid\n";

	$tid = waitpid($pid, &WNOHANG);
	if ($tid != $pid) {
		# not done
		wlog DEBUG, "$JOBID Job $pid still running\n";
		return 0;
	}
	else {
		# exit code is in MSB and signal in LSB, so
		# switch them such that status & 0xff is the
		# exit code
		$status = $? >> 8 + (($? & 0xff) << 8);
	}

	wlog DEBUG, "$JOBID Child process $pid terminated. Status is $status.\n";
	my $s;
	if (!eof($RD)) {
		$s = <$RD>;
	}
	wlog DEBUG, "$JOBID Got output from child. Closing pipe.\n";
	close $RD;
	$JOBDATA{$JOBID}{"exitcode"} = $status;

	if ($PROFILE) {
		push(@PROFILE_EVENTS, "TERM", $pid, time());
	}

	my $JOBSLOT = $JOBDATA{$JOBID}{"jobslot"};
	if ( defined $JOBSLOT ) {
		push @jobslots,$JOBSLOT;
	}

	if (defined $s) {
		#queueCmd(nullCB(), "JOBSTATUS", $JOBID, FAILED, "$status", $s);
		stageout($JOBID);
	}
	else {
		#queueCmd(nullCB(), "JOBSTATUS", $JOBID, COMPLETED, "$status", "");
		stageout($JOBID);
	}
	$JOB_COUNT++;
	$JOBS_RUNNING--;
	return 1;
}

sub runjob {
	my ($WR, $JOB, $JOBARGS, $JOBENV, $JOBSLOT, $WORKERPID) = @_;
	my $executable = $$JOB{"executable"};
	my $stdout = $$JOB{"stdout"};
	my $stderr = $$JOB{"stderr"};

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
	if (defined $$JOB{directory}) {
		wlog DEBUG, "chdir: $$JOB{directory}\n";
	    chdir $$JOB{directory};
	}
	if (defined $stdout) {
		wlog DEBUG, "STDOUT: $stdout\n";
		close STDOUT;
		open STDOUT, ">$stdout" or die "Cannot redirect STDOUT";
	}
	if (defined $stderr) {
		wlog DEBUG, "STDERR: $stderr\n";
		close STDERR;
		open STDERR, ">$stderr" or die "Cannot redirect STDERR";
	}
	close STDIN;

	exec { $executable } @$JOBARGS or print $WR "Could not execute $executable: $!\n";
	die "Could not execute $executable: $!";
}

initlog();

my $MSG="0";

wlog(INFO, "Running on node $myhost\n");
# wlog(INFO, "New log name: $LOGNEW \n");

init();

if (defined $ENV{"WORKER_COPIES"}) {
	workerCopies($ENV{"WORKER_COPIES"});
}

mainloop();

# Code may not reach this point - see shutdownw()
wlog INFO, "Worker finished. Exiting.\n";
exit(0);

# Local Variables:
# indent-tabs-mode: t
# tab-width: 4
# End:

# perl-indent-level: 8
