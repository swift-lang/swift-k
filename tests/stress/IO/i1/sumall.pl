#!/usr/bin/perl                                                           

my $sum = 0;
my $filename = shift;
open(my $fd, "<", $filename) or die "Couldn;t access '$filename' \n";
while ( my $line = <$fd> ) {
    $sum += $line;
}
print "$sum\n";
