#!/usr/bin/perl

#ACCUMULATOR=0
#while read time delta rest ; do
#  ACCUMULATOR=$(echo $ACCUMULATOR + $delta | bc )
#  echo $time $ACCUMULATOR
#done < events-in-progress2.tmp > events-in-progress3.tmp

$accumulator = 0;

while (<STDIN>) {

 $n = $_;
 if($n =~ /^([0-9\.\-]+) +([0-9\.\-]+).*$/) {
   $time = $1;
   $delta = $2;
   $accumulator=$accumulator + $delta;
   print "$time $accumulator\n";
 } else {
   print STDERR "Can't parse line $n\n";
 }
}
