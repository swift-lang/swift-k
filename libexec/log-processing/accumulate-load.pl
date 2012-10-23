#!/usr/bin/perl

# Accumulate the load level at each point in time
# INPUT:  lines formatted as "timestamp text"
#         where timestamp is a number
# OUTPUT: lines formatted as "timestamp load"
#         where timestamp and load are numbers

$time = 0.0;
$load = 0;

foreach $n (<STDIN>) {
  $n =~ /^([^ ]*) (.*)$/ ;
  $time = $1;
  $info = $2;
  if ($info =~ /START/) {
      $load++;
  }
  elsif ($info =~ /END/) {
      $load--;
  }
  printf("%.4f %i\n", $time, $load);
}
