#!/usr/bin/perl

open(STARTTIME,"start-time.tmp");
$i=<STARTTIME>;
$i =~ /([0-9.]*)/;
$nowx=$1;
close(STARTTIME);
$nowy=0;

while (<STDIN>) {

 $n = $_;
 if($n =~ /^([0-9\.\-]+) +([0-9\.\-]+)/) {
   $newx = $1;
   $newy = $2;
   print $nowx, " ", $nowy, " ", ($newx - $nowx), " 0\n";
   print $newx, " ", $nowy, " 0 ", ($newy - $nowy), "\n";
   $nowx = $newx;
   $nowy = $newy;
 } else {
   print STDERR "Can't parse line $n\n";
 }

}
