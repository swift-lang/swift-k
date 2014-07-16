#!/usr/bin/perl

$acc = 0;
$lastbucket="";

while(<STDIN>) {

 $n = $_;
 if($n =~ /^([0-9\.\-]+) (.*)$/) {
  $newbucket=$2;
  $time=$1;
  if($newbucket eq $lastbucket) {
    $acc = $acc + $time;
  } else {
    print($lastbucket, " ", $acc, "\n");
    $acc=0;
    $lastbucket = $newbucket;
  }
 } else {
    print STDERR "FAIL: $n";
  }
}
print($lastbucket, " ", $acc, "\n");


