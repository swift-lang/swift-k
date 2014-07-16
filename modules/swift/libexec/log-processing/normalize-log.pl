#!/usr/bin/perl

# INPUT: two files: the first contains the start time
# and the second contains the log file with unix timestamps

# OUTPUT: stdout: the log file with timestamps normalized
# to the start time

$TIME = $ARGV[0];
$LOG  = $ARGV[1];

my $earliesttime;

open(START, $TIME) || die "$!\n";
read(START, $earliesttime, 64);
close START;

print "earliest time: $earliesttime\n";

open(LOG, $LOG) || die "$!\n";

foreach $n (<LOG>) {
  $n =~ /^([^ ]*) (.*)$/ ;
  $delta = $1 - $earliesttime;
  print "$delta $2\n";
}

close LOG;
