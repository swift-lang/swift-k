#!/usr/bin/perl 
use strict;
use Lingua::EN::Numbers::Ordinate;
use File::Basename;

my $basedir=shift;

print <<'END';
Content-type: text/html

<html>
<head>
<title>Swift Release Testing</title>
<link rel="stylesheet" type="text/css" href="style1col.css" />
</head>
<body>
END

my @table_data=();
my $count=1;
my @months = ("Zero", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

foreach my $test_result(<$basedir/run*/*.html>) {
   my $datestring = basename($test_result, ".html");
   my $iteration = (split('\.', basename(dirname($test_result))))[-1];
   (my $ignore, my $year, my $month, my $day) = split('-', $datestring);
   $day =~ s/^0*//;
   $table_data[$count] .= "<td><a href=\"$test_result\">$months[$month] " . ordinate($day) . ", $year - iteration $iteration</a></td>";
   $count++;
}

# Print table
print "<center><table border=\"1\" width=\"600\">\n";
print "<tr>$table_data[0]</tr>\n";
foreach my $td(@table_data[1..$#table_data]) {
   print "<tr>$td</tr>\n";
}
print "</table></center></body></html>\n";
