#! /usr/bin/env perl
########################################################
# CHANGES:
#  07.02.03:	Added replies and changed the protocol
########################################################
# The protocol is as follows. 
#	COMMAND:byte
#	DATALEN:int
#	(ARGS)*:NUL terminated strings
# The ARGS are not required, but DATALEN is. In any case DATALEN should
# be the total length of all the bytes in the strings. The strings are 
# ASCII encoded (plain ol' strings).
#
# CMD_CLOSE = 0x00
#	Not sure this is neccessary, but here it is. Closes the connection
#	cleanly. It does not generate a reply!
#
# CMD_READ_GRAPH = 0x01
#	ARGS:
#	  DATA:	 	string, ASCII encoded (an XML string with the
#                       graph structure) must be exactly DATALEN bytes
#                       in length, otherwise the whole thing will go
#                       out of sync.
#
# CMD_UPDATE_PROPERTY = 0x02
#	ARGS:
#	  NODEID:	NUL terminated ASCII string representing the nodeid
#			(as specified in the graph definition)
#	  PROPNAME:	NUL terminated ASCII string with the property name.
#			Some of the properties may include: 
#				o "name",
#				o "iconfile",
#				o "hue", 
#				o "saturation", 
#				o "value".
#	  PROPVALUE:	NUL terminated ASCII string with the new property 
#			value. Conversion is performed for basic types 
#			(String, Integer, Float, Double, Boolean). If 
#			the property is not of one of these types an
#			INVALID_VALUE is returned.
#			
#
# CMD_QUERY_PROPERTIES = 0x03
#	ARGS:
#	  NODEID:	
#
# CMD_VERSION = 0x04
#	ARGS: none
#
#
# CMD_ADD_PROPERTY = 0x05
#	ARGS:
#	  NODEID:
#	  PROPERTY_NAME:	
#
# CMD_REMOVE_PROPERTY = 0x06
#	ARGS:
#	  NODEID:
#	  PROPERTY_NAME:	
#
# Replies have the same format (COMMAND, DATALEN, STRINGS)
# The codes are as follows:
#	REPLY_OK = 0x10
#	REPLY_INVALID_COMMAND = 0x11
#	REPLY_INVALID_NODEID = 0x12
#	REPLY_INVALID_PROPERTY = 0x13
#	REPLY_READ_ONLY_PROPERTY = 0x14
#	REPLY_INVALID_VALUE = 0x15
#	REPLY_UNKNOWN_ERROR = 0x1f
require 5.005;
use strict;
use IO::Socket;			# resource hog, permissable in clients

#
# --- constants--------------------------------------------------
#

%main::cmd = (
	      CMD_CLOSE => pack("C",0),
	      CMD_READ_GRAPH => pack("C",1),
	      CMD_UPDATE_PROPERTY => pack("C",2),
	      CMD_QUERY_PROPERTIES => pack("C", 3),
	      CMD_VERSION => pack("C", 4),
	      CMD_ADD_PROPERTY => pack("C", 5),
	      CMD_REMOVE_PROPERTY => pack("C", 6)
	     );
%main::reply = (
	      REPLY_OK => pack("C", 0x10),
	      REPLY_INVALID_COMMAND => pack("C", 0x11),
	      REPLY_INVALID_NODEID => pack("C", 0x12),
	      REPLY_IVALID_PROPERTY => pack("C", 0x13),
	      REPLY_READ_ONLY_PROPERTY => pack("C", 0x14),
	      REPLY_INVALID_VALUE => pack("C", 0x15),
	      REPLY_UNKNOWN_ERROR => pack("C", 0x1f)
	     );

#
# --- functions--------------------------------------------------
#


sub sendgraph ($) {
    my $sockfd = shift;

    print "File name: ";
    my $name = <STDIN>;
    chomp($name);

    # determine size
    my $size = -s $name;
    unless ( defined $size ) {
	warn "invalid filename \"$name\": $!\n";
	return undef;
    } else {
	print "# sending $size bytes from file\n";
    }

    # start protocol to transfer graph
    $sockfd->print( $main::cmd{CMD_READ_GRAPH} );
    $sockfd->print( pack("N",$size+1) );

    # copy graph from file
    if ( open(GRAPH,"<$name") ) {
	local $_;
	while (<GRAPH>) {
	    $sockfd->print($_);
	}
	$sockfd->print("\000");
	$sockfd->flush();
	close(GRAPH);
    } else {
	# unreachable failure
	warn "unable to open $name: $!\n";
	return undef;
    }
    readreply($sockfd);
}

sub readvalue ($) {
    my $prompt = shift;
    print $prompt;
    local $_ = <STDIN>;
    s/[\r\n]+$//o;		# safer+portabler than chop() and chomp()
    $_;				# return value
}


sub readreply($){
    my $sockfd = shift;
    my $buf; my $msg; my $datalen;
    $sockfd->read($buf, 1);
    my $reply = unpack("C", $buf);
    print "$reply - ";
    #... I know, but it was quick
    if ($reply == 0x10) {print "OK";}
    if ($reply == 0x11) {print "Invalid command";}
    if ($reply == 0x12) {print "Invalid node id";}
    if ($reply == 0x13) {print "Invalid property";}
    if ($reply == 0x14) {print "Read only property";}
    if ($reply == 0x15) {print "Invalid value";}
    if ($reply == 0x1f) {print "Unknown error";}
    print("\n");
    $sockfd->read($buf, 4);
    $datalen = unpack("N", $buf);
    if ($datalen != 0){
	$sockfd->read($msg, $datalen);
	print "$msg\n";
    }
    $reply == 0x10; #success
}

sub command {
    my $sockfd = shift(@_);
    my $cmd = shift(@_);
    my @args;
    my $len = 0;
    my $i;
    my $arg;
    foreach $i (@_){
	$arg = $i;
	$arg .= "\000";
	$len += length($arg);
	push(@args, $arg);
    }
    $sockfd->print($cmd, pack("N", $len));
    foreach $i (@args){
	$sockfd->print($i);
    }
    $sockfd->flush();
    readreply($sockfd);
}

#
# --- main ------------------------------------------------------
#

my $remote = shift || "localhost";
my $port = shift || 9999;
my $sockfd = IO::Socket::INET->new(
	Proto => 'tcp',
	PeerAddr => $remote,
	PeerPort => $port ) or
    die "unable to connect to $remote:$port: $!\n";

my ($c,$nodeid,$value,$filename);
my $quit=0;
command($sockfd, $main::cmd{CMD_VERSION});
while ($quit == 0) {
    print "1 - send graph\n";
    print "2 - set icon\n";
    print "3 - adjust hue\n";
    print "4 - adjust saturation\n";
    print "5 - adjust value\n";
    print "6 - set status\n";
    print "7 - set arbitrary property\n";
    print "8 - query properties\n";
    print "9 - add property\n";
    print "a - remove property\n";
    print "0 - quit\n";
    chomp($c = <STDIN>);
    
    if ($c == '0') {
	$quit = 1;
    } elsif ($c == '1') {
	sendgraph( $sockfd );
    } elsif ($c == '2') {
	$nodeid = readvalue('Node id: ');
	$value = readvalue('Icon : ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, "iconfile", $value);
    } elsif ($c == '3') {
	$nodeid = readvalue('Node id: ');
	$value = readvalue('Hue adjustment: ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, "hue", $value);
    } elsif ($c == '4') {
	$nodeid = readvalue('Node id: ');
	$value = readvalue('Saturation adjustment: ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, "saturation", $value);
    } elsif ($c == '5'){
	$nodeid = readvalue('Node id: ');
	$value = readvalue('Value adjustment: ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, "value", $value);
    } elsif ($c == '6'){
	$nodeid = readvalue('Node id: ');
	$value = readvalue('New status (0=stopped, 1=running, 2=failed, 3=completed): ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, "status", $value);
    } elsif ($c == '7'){
	$nodeid = readvalue('Node id: ');
	my $prop = readvalue('Property name: ');
	$value = readvalue('New value: ');
	command($sockfd, $main::cmd{CMD_UPDATE_PROPERTY}, $nodeid, $prop, $value);
    } elsif ($c == '8'){
	$nodeid = readvalue('Node id: ');
	command($sockfd, $main::cmd{CMD_QUERY_PROPERTIES}, $nodeid);
    } elsif ($c == '9'){
	$nodeid = readvalue('Node id: ');
	my $prop = readvalue('Property name: ');
	command($sockfd, $main::cmd{CMD_ADD_PROPERTY}, $nodeid, $prop);
    } elsif ($c == 'a'){
	$nodeid = readvalue('Node id: ');
	my $prop = readvalue('Property name: ');
	command($sockfd, $main::cmd{CMD_REMOVE_PROPERTY}, $nodeid, $prop);
    } else {
	# always terminate with a "default" 
	warn "illegal input \'$c\', ignoring\n";
    }
}

$sockfd->print( $main::cmd{CMD_CLOSE} );
$sockfd->close();
exit 0;
