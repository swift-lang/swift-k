#!/usr/bin/perl

use strict;

my @characters=('0'..'9');
my $string="";
my $length = $ARGV[0];
my $width_count=0;

foreach (1..$length) 
{
   if($width_count == 5) {
      $string .= "\n";
      $width_count=0;
      next;
   }
   $string .= $characters[rand @characters];
   $width_count++;
}

print $string;
