#!/usr/bin/perl
use IO::Socket;
use strict;
use warnings;


my $REPLY_FLAG = 0x00000001;
my $FINAL_FLAG = 0x00000002;
my $ERROR_FLAG = 0x00000004;

my $COMPLETED = 7;
my $FAILED = 5;
my $ACTIVE = 2;

my $TAG = 0;
my $RETRIES = 3;
my $REPLYTIMEOUT = 60;
my $MAXFRAGS = 16;

my $IDLETIMEOUT = 300; #Seconds
my $LASTRECV = 0;

my $BUFSZ = 2048;

my %REQUESTS = ();
my %REPLIES  = ();

my $LOG = "$ENV{HOME}/worker$ARGV[0].log";


my %HANDLERS = (
	"SHUTDOWN" 	=> \&shutdown,
	"SUBMITJOB" => \&submitjob,
	"REGISTER"  => \&register,
);

my @CMDQ = ();

my $ID=$ARGV[0];
my $URI=$ARGV[1];
my $SCHEME;
my $HOSTNAME;
my $PORT;
if ($URI =~ /(.*):\/\//) { $SCHEME = $1; } else { die "Could not parse url scheme: $URI"; }
if ($URI =~ /.*:\/\/(.*):/) { $HOSTNAME = $1; } else { die "Could not parse url hostname: $URI"; }
if ($URI =~ /.*:\/\/.*:(.*)/) { $PORT = $1; } else { die "Could not parse url port: $URI"; }
my $SOCK;

my $JOBID;
my %JOB;
my %JOBENV;
my @JOBARGS;

sub wlog {
	my $msg;
	foreach $msg (@_) {
		print LOG time(), " ", $msg;
		#print $msg;
	}
}

sub init() {
	my $fail = 0;
	
	open(LOG, ">$LOG") or die "Failed to open log file: $!";
	my $b = select(LOG);
	$| = 1;
	select($b);
	print LOG time(), " Logging started\n";

	wlog "uri=$URI, scheme=$SCHEME, host=$HOSTNAME, port=$PORT, id=$ID\n";
	for ($_ = 0; $_ < 10; $_++) {
		$SOCK = IO::Socket::INET->new(Proto=>'tcp', PeerAddr=>$HOSTNAME, PeerPort=>$PORT) || ($fail = 1);
		if (!$fail) {
			$SOCK->setsockopt(SOL_SOCKET, SO_RCVBUF, 16384);
			$SOCK->setsockopt(SOL_SOCKET, SO_SNDBUF, 32768);
			last;
		}		
	}
	if ($fail) {
		die "Failed to create sockets: $!";	
	}
}


sub sendm {
	my ($tag, $flags, $msg) = @_;
	my $len = length($msg);
	my $buf = pack("VVV", $tag, $flags, $len);
	$buf = $buf.$msg;
	wlog("> len=$len, tag=$tag, flags=$flags, $msg\n");
	$SOCK->send($buf) == length($buf) or die "cannot send to $SOCK: $!";
}

sub sendFrags {
	my ($tag, $flg, @msgs) = @_;
	
	for (my $i = 0; $i <= $#msgs; $i++) {
		sendm($tag, ($i < $#msgs) ? $flg : ($FINAL_FLAG | $flg), $msgs[$i]);
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
	
	sendFrags($tag, $REPLY_FLAG, @msgs);
}

sub sendError {
	my ($tag, @msgs) = @_;
	
	sendFrags($tag, $REPLY_FLAG | $ERROR_FLAG, @msgs);
}

sub unpackData {
	my ($data) = @_;

	my $lendata = length($data);
	if ($lendata < 12) {
		die "Received faulty message (length < 12: $lendata)";
	}
	my $tag = unpack("V", substr($data, 0, 4));
	my $flg = unpack("V", substr($data, 4, 4));
	my $len = unpack("V", substr($data, 8, 4));
	my $msg;
	$SOCK->recv($msg, $len);
	
	wlog("< len=$len, tag=$tag, flags=$flg, $data\n");
	return ($tag, $flg, $msg);
}

sub processRequest {
	my ($tag, $timeout, $err, $request) = @_;
	
	if ($timeout) {
		sendError($tag, ("Timed out waiting for all fragments"));
	}
	else {
		wlog "Processing request\n";
		my $cmd = shift(@$request);
		wlog "Cmd is $cmd\n";
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
	
	
	my $reply = $flg & $REPLY_FLAG;
	my ($record, $cont, $start, $frags);
	
	if ($reply) {
		if (exists($REPLIES{$tag})) {
			$record = $REPLIES{$tag};
			($cont, $start, $frags) = ($record->[0], $record->[1], $record->[2]);
		}
		else {
			wlog("Warning: received reply to unregistered command (tag=$tag). Discarding.\n");
			return;
		}
	}
	else {
		if (!exists($REQUESTS{$tag})) {
			$REQUESTS{$tag} = [\&processRequest, time(), []];
			wlog "New request ($tag)\n";
		}
		$record = $REQUESTS{$tag};
		($cont, $start, $frags) = ($$record[0], $$record[1], $$record[2]);
	}
		
	my $fin = $flg & $FINAL_FLAG;
	my $err = $flg & $ERROR_FLAG;
		
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
}

sub checkTimeouts2 {
	my ($hash) = @_;
	
	my $now = time();
	my @del = ();
	
	my $k;
	my $v;
	
	while (($k, $v) = each(%$hash)) {
		if ($now - $$v[1] > $REPLYTIMEOUT) {
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
		my $dif = time() - $LASTRECV;
		if ($dif >= $IDLETIMEOUT) {
			die "Idle time exceeded";	
		}
	}
}

sub recvOne {
	my $data;
	$SOCK->recv($data, 12);
	if (length($data) > 0) {
		wlog "Received $data\n";
		eval { process(unpackData($data)); } || wlog "$@\n";
		$LASTRECV = time();
	}
	else {
		#sleep 250ms
		select(undef, undef, undef, 0.25);
		checkTimeouts();
	}
}

sub registerCmd {
	my ($tag, $cont) = @_;
	
	$REPLIES{$tag} = [$cont, time(), ()];
}


sub mainloop {
	my $cmd;
	while(1) {
		foreach $cmd (@CMDQ) {
			sendCmd(@$cmd);
		}
		@CMDQ = ();
		recvOne();
	}
}

sub queueCmd {
	push @CMDQ, [@_];
}

sub printreply {
	my ($tag, $timeout, $err, $reply) = @_;
	if ($timeout) {
		wlog "Timed out waiting for reply to $tag\n";
	}
	else {
		wlog "$$reply[0]\n";
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
	if ($err) {
		die "Failed to register (service returned error: ".join("\n", @$reply).")";
	}
}

sub register {
	my ($tag, $timeout, $reply) = @_;
	sendReply($tag, ("OK"));
}


sub shutdown {
	my ($tag, $timeout, $msgs) = @_;
	
	sendReply($tag, ("OK"));
	wlog "Shutdown command received. Exiting\n";
	exit 0;
}

sub submitjob {
	my ($tag, $timeout, $msgs) = @_;
	my $desc = $$msgs[0];
	my @lines = split(/\n/, $desc);
	my $line;
	$JOBID = undef;
	%JOB = ();
	@JOBARGS = ();
	%JOBENV = (); 
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
		else {
			$JOB{$pair[0]} = $pair[1];
		}
	}
	if (checkJob($tag)) {
		sendCmd((\&nullCB, "JOBSTATUS", $JOBID, "$ACTIVE", "0", ""));
		forkjob();
	}
}

sub checkJob() {
	my $tag = shift;
	my $executable = $JOB{"executable"};
	if (!(defined $JOBID)) {
		sendReply($tag, ("Missing job identity"));
		return 0;
	}
	elsif (!(defined $executable)) {
		sendReply($tag, ("Missing executable"));
		return 0;
	}
	else {
		sendReply($tag, ("OK"));
		return 1;
	}
}

sub forkjob {
	my ($pid, $status);
	$pid = fork();
	if (defined($pid)) {
		if ($pid == 0) {
			runjob();
		}
		else {
			wlog "Forked process $pid. Waiting for its completion\n";
			waitpid($pid, 0);
			$status = $? & 0xff;
			wlog "Child process $pid terminated. Status is $status. $!\n";
			queueCmd(\&nullCB, "JOBSTATUS", $JOBID, "$COMPLETED", "$status", "");
		}
	}
	else {
		queueCmd(\&nullCB, "JOBSTATUS", $JOBID, "$FAILED", "512", "Could not fork child process");
	}
}

sub runjob {
	my $executable = $JOB{"executable"};
	my $stdout = $JOB{"stdout"};
	my $stderr = $JOB{"stderr"};

	wlog "Running $executable\n";
	my $ename;
	foreach $ename (keys %JOBENV) {
		$ENV{$ename} = $JOBENV{$ename};
	}
	unshift @JOBARGS, $executable;
	if (defined $stdout) {
		close STDOUT;
		open STDOUT, $stdout;
	}
	if (defined $stderr) {
		close STDERR;
		open STDERR, $stderr;
	}
	exec { $executable } @JOBARGS or queueCmd(\&nullCB, "JOBSTATUS", $JOBID, "$FAILED", "513", "Could not execute $executable: $!");		
	die "Could not execute $executable: $!";
}

my $MSG="0";

my $myhost=`hostname -i`;
$myhost =~ s/\s+$//;
init();

queueCmd(\&registerCB, "REGISTER", $ID, "wid://$ID");

mainloop();
