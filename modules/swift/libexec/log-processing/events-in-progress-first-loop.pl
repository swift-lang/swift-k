#!/usr/bin/perl

#echo EIP $(date) start first loop >&2
#while read start duration rest ; do
#  echo $start "1"
#  echo $(echo $start + $duration | bc) "-1"
#done > events-in-progress.tmp

while (<STDIN>) {

 $n = $_;
 if($n =~ /^([0-9\.\-]+) +([0-9\.\-]+).*$/) {
   $start = $1;
   $duration = $2;
   $end=$start + $duration;
   print "$start 1\n";
   print "$end -1\n";
 } else {
   print STDERR "Can't parse line $n\n";
 }
}
