#!/usr/bin/perl

# Put data into buckets for a histogram

# usage: buckets.pl <WIDTH> <FILE>

# Given a file containing numbers, produces 
# a plottable histogram file containing buckets 
# and counts 

# Useful for obtaining job runtime distribution plots

# Example: 
# cat file.txt
# 0.1
# 0.2
# 1.1
# buckets.pl 1 file.txt 
# 1 2 
# 2 1

use POSIX;

# Bucket width:
my $width = $ARGV[0];

my $file = $ARGV[1];

open FILE, "<", $file or die "could not open: $file\n";

my %bucket = ();
while (<FILE>)
{
  # Round up to nearest bucket
  my $v = ceil($_);
  while (($v % $width) != 0) {
    $v++;
  }
  if (exists $bucket{$v}) {
    $bucket{$v} = $bucket{$v} + 1;
  }
  else {
    $bucket{$v} = 1;
  }
}

@s = sort { $a <=> $b } keys %bucket;

for (@s) {
  print "$_ $bucket{$_}\n";
}

print "\n";

# Local Variables:
# perl-basic-offset: 2
# End:
