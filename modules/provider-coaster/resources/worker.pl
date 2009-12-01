#!/usr/bin/perl
use IO::Socket;
use Cwd;
use POSIX ":sys_wait_h";
use strict;
use warnings;

use constant {
	DEBUG => 0,
	INFO => 1,
	WARN => 2,
	ERROR => 3,
};

my $LOGLEVEL = DEBUG;

my @LEVELS = ("DEBUG", "INFO ", "WARN ", "ERROR"); 

use constant {
	REPLY_FLAG => 0x00000001,
	FINAL_FLAG => 0x00000002,
	ERROR_FLAG => 0x00000004,
};

use constant {
	COMPLETED => 0x07,
	FAILED => 0x05,
	ACTIVE => 0x02,
	STAGEIN => 0x10,
	STAGEOUT => 0x11,
};

my $TAG = 0;
use constant RETRIES => 3;
use constant REPLYTIMEOUT => 60;
use constant MAXFRAGS => 16;
use constant MAX_RECONNECT_ATTEMPTS => 3;

use constant IDLETIMEOUT => 4 * 60; #Seconds; 2 minutes
my $LASTRECV = 0;
my $JOBS_RUNNING = 0;

my $JOB_COUNT = 0;

use constant BUFSZ => 2048;

# 60 seconds by default. Note that since there is no configuration handshake
# this would have to match the default interval in the service in order to avoid
# "lost heartbeats".
use constant HEARTBEAT_INTERVAL => 2 * 60;

my %REQUESTS = ();
my %REPLIES  = ();

my $BLOCKID=$ARGV[1];

my $LOGDIR=$ARGV[2];
my $LOG = "$LOGDIR/worker-$BLOCKID.log";


my %HANDLERS = (
	"SHUTDOWN" 	=> \&shutdownw,
	"SUBMITJOB" => \&submitjob,
	"REGISTER"  => \&register,
	"HEARTBEAT" => \&heartbeat,
	"WORKERSHELLCMD" => \&workershellcmd,
);

my @CMDQ = ();

my $ID = "-";
my $URI=$ARGV[0];
my $SCHEME;
my $HOSTNAME;
my $PORT;
if ($URI =~ /(.*):\/\//) { $SCHEME = $1; } else { die "Could not parse url scheme: $URI"; }
if ($URI =~ /.*:\/\/(.*):/) { $HOSTNAME = $1; } else { die "Could not parse url hostname: $URI"; }
if ($URI =~ /.*:\/\/.*:(.*)/) { $PORT = $1; } else { die "Could not parse url port: $URI"; }
my $SOCK;
my $LAST_HEARTBEAT = 0;

my %JOBWAITDATA = ();

sub wlog {
	my $msg;
	my $level = shift;
	if ($level >= $LOGLEVEL) {
		foreach $msg (@_) {
			print LOG time(), " $LEVELS[$level] $ID $msg";
			#print $msg;
		}
	}
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
	my $fail = 0;
	my $i;
	for ($i = 0; $i < MAX_RECONNECT_ATTEMPTS; $i++) {
		wlog DEBUG, "Connecting ($i)...\n";
		$SOCK = IO::Socket::INET->new(Proto=>'tcp', PeerAddr=>$HOSTNAME, PeerPort=>$PORT, Blocking=>1) || ($fail = 1);
		if (!$fail) {
			$SOCK->setsockopt(SOL_SOCKET, SO_RCVBUF, 16384);
			$SOCK->setsockopt(SOL_SOCKET, SO_SNDBUF, 32768);
			wlog DEBUG, "Connected\n";
			$SOCK->blocking(0);
			queueCmd(\&registerCB, "REGISTER", $BLOCKID, "");
			last;
		}
		else {
			wlog ERROR, "Connection failed: $!\n";
			select(undef, undef, undef, 2 ** $i);
		}
	}
	if ($fail) {
		die "Failed to connect: $!";
	}
}

sub initlog() {
	if (defined $ENV{"WORKER_LOGGING_ENABLED"}) {
		open(LOG, ">>$LOG") or die "Failed to open log file: $!";
		my $b = select(LOG);
		$| = 1;
		select($b);
		wlog INFO, "$BLOCKID Logging started\n";
	}
	else {
		$LOGLEVEL = 999;
	}
}


sub init() {
	wlog DEBUG, "uri=$URI, scheme=$SCHEME, host=$HOSTNAME, port=$PORT, blockid=$BLOCKID\n";
	reconnect();
}

sub sendm {
	my ($tag, $flags, $msg) = @_;
	my $len = length($msg);
	my $buf = pack("VVV", $tag, $flags, $len);
	$buf = $buf.$msg;

	wlog(DEBUG, "> len=$len, tag=$tag, flags=$flags, $msg\n");

	#($SOCK->send($buf) == length($buf)) || reconnect();
	eval {defined($SOCK->send($buf))} or wlog(WARN, "Send failed: $!\n");
}

sub sendFrags {
	my ($tag, $flg, @msgs) = @_;
	
	for (my $i = 0; $i <= $#msgs; $i++) {
		sendm($tag, ($i < $#msgs) ? $flg : (FINAL_FLAG | $flg), $msgs[$i]);
	}
}

sub sendCmd {
	my @cmd = @_;
	my $cont = shift(@cmd);
	my $ctag = $TAG++;
	
	registerCmd($ctag, $cont);
	sendFrags($ctag, 0, @cmd);
}

sub sendReply {
	my ($tag, @msgs) = @_;
	
	sendFrags($tag, REPLY_FLAG, @msgs);
}

sub sendError {
	my ($tag, @msgs) = @_;
	sendFrags($tag, REPLY_FLAG | ERROR_FLAG, @msgs);
}

sub unpackData {
	my ($data) = @_;

	my $lendata = length($data);
	if ($lendata < 12) {
		wlog WARN, "Received faulty message (length < 12: $lendata)\n";
		die "Received faulty message (length < 12: $lendata)";
	}
	my $tag = unpack("V", substr($data, 0, 4));
	my $flg = unpack("V", substr($data, 4, 4));
	my $len = unpack("V", substr($data, 8, 4));
	my $msg;
	my $frag;
	my $alen = 0;
	while ($alen < $len) {
		$SOCK->recv($frag, $len - $alen);
		$alen = $alen + length($frag);
		$msg = $msg.$frag;
	}
	
	my $actuallen = length($msg);
	wlog(DEBUG, "< len=$len, actuallen=$actuallen, tag=$tag, flags=$flg, $msg\n");
	if ($len != $actuallen) {
		wlog(WARN, "len != actuallen\n");
	}
	return ($tag, $flg, $msg);
}

sub processRequest {
	my ($tag, $timeout, $err, $request) = @_;
	
	if ($timeout) {
		sendError($tag, ("Timed out waiting for all fragments"));
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
	my ($record, $cont, $start, $frags);
	
	if ($reply) {
		if (exists($REPLIES{$tag})) {
			$record = $REPLIES{$tag};
			($cont, $start, $frags) = ($record->[0], $record->[1], $record->[2]);
		}
		else {
			wlog(WARN, "received reply to unregistered command (tag=$tag). Discarding.\n");
			return;
		}
	}
	else {
		$LASTRECV = time();
		if (!exists($REQUESTS{$tag})) {
			$REQUESTS{$tag} = [\&processRequest, time(), []];
			wlog DEBUG, "New request ($tag)\n";
		}
		$record = $REQUESTS{$tag};
		($cont, $start, $frags) = ($$record[0], $$record[1], $$record[2]);
	}
		
	my $fin = $flg & FINAL_FLAG;
	my $err = $flg & ERROR_FLAG;
		
	push @$frags, $msg;
		
	if ($fin) {
		if ($reply) {
			delete($REPLIES{$tag});
		}
		else {
			delete($REQUESTS{$tag});
		}
		$cont->($tag, 0, $err, $frags);
	}
	
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
			$cont->($k, 1, 0, ());
		}
	}
	
	foreach $k (@del) {
		delete $$hash{$k};
	}
}

sub checkTimeouts {
	checkTimeouts2(\%REQUESTS);
	checkTimeouts2(\%REPLIES);
	if ($LASTRECV != 0) {
		my $time = time();
		my $dif = $time - $LASTRECV;
		wlog DEBUG, "time: $time, lastrecv: $LASTRECV, dif: $dif\n"; 
		if ($dif >= IDLETIMEOUT && $JOBS_RUNNING == 0) {
			wlog INFO, "Idle time exceeded (time=$time, LASTRECV=$LASTRECV, dif=$dif)\n";
			die "Idle time exceeded";
		}
	}
}

sub recvOne {
	my $data;
	$SOCK->recv($data, 12);
	if (length($data) > 0) {
		wlog DEBUG, "Received $data\n";
		eval { process(unpackData($data)); } || (wlog ERROR, "Failed to process data: $@\n" && die "Failed to process data: $@");
	}
	else {
		#sleep 250ms
		select(undef, undef, undef, 0.25);
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
		queueCmd(\&heartbeatCB, "HEARTBEAT");
		$LAST_HEARTBEAT = time();
	}
	foreach $cmd (@CMDQ) {
		sendCmd(@$cmd);
	}
	@CMDQ = ();
	checkJobs();
	recvOne();
}

sub queueCmd {
	push @CMDQ, [@_];
}

sub printreply {
	my ($tag, $timeout, $err, $reply) = @_;
	if ($timeout) {
		wlog WARN, "Timed out waiting for reply to $tag\n";
	}
	else {
		wlog DEBUG, "$$reply[0]\n";
	}
}

sub nullCB {
	my ($tag, $timeout, $err, $reply) = @_;
}

sub registerCB {
	my ($tag, $timeout, $err, $reply) = @_;
	
	if ($timeout) {
		die "Failed to register (timeout)\n";
	}
	elsif ($err) {
		die "Failed to register (service returned error: ".join("\n", @$reply).")";
	}
	else {
		$ID = $$reply[0];
		wlog INFO, "Registration successful. ID=$ID\n";
	}
}

sub heartbeatCB {
	my ($tag, $timeout, $err, $reply) = @_;
	
	if ($timeout) {
		if (time() - $LAST_HEARTBEAT > 2 * HEARTBEAT_INTERVAL) {
			wlog WARN, "No heartbeat replies in a while. Dying.\n";
			die "No response to heartbeat\n";
		}
	}
	elsif ($err) {
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


sub shutdownw {
	my ($tag, $timeout, $msgs) = @_;
	wlog DEBUG, "Shutdown command received\n";
	sendReply($tag, ("OK"));
	select(undef, undef, undef, 1);
	wlog INFO, "Acknowledged shutdown. Exiting\n";
	wlog INFO, "Ran a total of $JOB_COUNT jobs\n";
	exit 0;
}

sub heartbeat {
	my ($tag, $timeout, $msgs) = @_;
	sendReply($tag, ("OK"));
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
		$out = `$cmd 2>&1`;
		sendReply($tag, ("OK", "$out"));
	}
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
	my @STAGEOUT = (); 
	foreach $line (@lines) {
		$line =~ s/\\n/\n/;
		$line =~ s/\\\\/\\/;
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
			push @STAGEIN, $pair[1];
		}
		elsif ($pair[0] eq "stageout") {
			push @STAGEOUT, $pair[1];
		}
		else {
			$JOB{$pair[0]} = $pair[1];
		}
	}
	if (checkJob($tag, $JOBID, \%JOB)) {
		
		sendCmd((\&nullCB, "JOBSTATUS", $JOBID, ACTIVE, "0", "workerid=$ID"));
		forkjob($JOBID, \%JOB, \@JOBARGS, \%JOBENV);
	}
}

sub checkJob() {
	my ($tag, $JOBID, $JOB) = @_;
	
	my $executable = $$JOB{"executable"};
	if (!(defined $JOBID)) {
		my $ds = hts($JOB);
		
		wlog DEBUG, "Job details $ds\n";
		
		sendError($tag, ("Missing job identity"));
		return 0;
	}
	elsif (!(defined $executable)) {
		sendError($tag, ("Missing executable"));
		return 0;
	}
	else {
		chdir $$JOB{directory};
		wlog DEBUG, "Job check ok\n";
		sendReply($tag, ("OK"));
		return 1;
	}
}

sub forkjob {
	my ($JOBID, $JOB, $JOBARGS, $JOBENV) = @_;
	my ($pid, $status);
	pipe(PARENT_R, CHILD_W);
	$pid = fork();
	if (defined($pid)) {
		if ($pid == 0) {
			close PARENT_R;
			runjob(\*CHILD_W, $JOB, $JOBARGS, $JOBENV);
			close CHILD_W;
		}
		else {
			wlog DEBUG, "Forked process $pid. Waiting for its completion\n";
			close CHILD_W;
			$JOBS_RUNNING++;
			$JOBWAITDATA{$JOBID} = {
				pid => $pid,
				pipe => \*PARENT_R
			};
		}
	}
	else {
		queueCmd(\&nullCB, "JOBSTATUS", $JOBID, FAILED, "512", "Could not fork child process");
	}
	$LASTRECV = time();
}

sub checkJobs {
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
	
	wlog DEBUG, "Checking pid $pid\n";
	
	$tid = waitpid($pid, &WNOHANG);
	if ($tid != $pid) {
		# not done
		wlog DEBUG, "Job $pid still running\n";
		return 0;
	}
	else {
		# exit code is in MSB and signal in LSB, so
		# switch them such that status & 0xff is the
		# exit code
		$status = $? >> 8 + (($? & 0xff) << 8);
	}

	wlog DEBUG, "Child process $pid terminated. Status is $status. $!\n";
	my $s = <$RD>;
	wlog DEBUG, "Got output from child. Closing pipe.\n";
	close $RD;
	wlog DEBUG, "Queuing status command.\n";
	if (defined $s) {
		queueCmd(\&nullCB, "JOBSTATUS", $JOBID, FAILED, "$status", $s);
	}
	else {
		queueCmd(\&nullCB, "JOBSTATUS", $JOBID, COMPLETED, "$status", "");
	}
	wlog DEBUG, "Status command queued.\n";
	$JOB_COUNT++;
	$JOBS_RUNNING--;
	return 1;
}

sub runjob {
	my ($WR, $JOB, $JOBARGS, $JOBENV) = @_;
	my $executable = $$JOB{"executable"};
	my $stdout = $$JOB{"stdout"};
	my $stderr = $$JOB{"stderr"};

	my $cwd = getcwd();
	wlog DEBUG, "CWD: $cwd\n";
	wlog DEBUG, "Running $executable\n";
	wlog DEBUG, "Directory: $$JOB{directory}\n";
	my $ename;
	foreach $ename (keys %$JOBENV) {
		$ENV{$ename} = $$JOBENV{$ename};
	}
	unshift @$JOBARGS, $executable;
	if (defined $$JOB{directory}) {
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
	wlog DEBUG, "Command: @$JOBARGS\n";
	exec { $executable } @$JOBARGS or print $WR "Could not execute $executable: $!\n";
	die "Could not execute $executable: $!";
}

initlog();

my $MSG="0";

my $myhost=`hostname`;
$myhost =~ s/\s+$//;

wlog(DEBUG, "Initialized coaster worker\n");
wlog(INFO, "Running on node $myhost\n");

init();

mainloop();
wlog INFO, "Worker finished. Exiting.\n";
exit(0);
