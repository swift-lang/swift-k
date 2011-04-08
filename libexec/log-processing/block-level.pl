#!/usr/bin/perl

# Accumulate the load level at each point in time
# INPUT:  A Swift log file with Coasters messages:
#                  timestamp ... Block Starting block: workers=24 ...
#                  timestamp ... Block Shutting down ... (24x ...
#         where timestamp is a number
# OUTPUT: lines formatted as "timestamp level"
#         where timestamp and load are numbers,
#          level being the number of Coasters Cpus available

$level = 0;

sub report() {
    @tokens = split;
    printf("$tokens[0] $level\n");
}

while (<STDIN>) {
    if (/Block/) {
	if (/Block Starting block:/) {
	    /workers=(\d*)/;
	    report;
	    $level += $1;
	    report;
	}
	if (/Block Shutting down block/) {
	    /\((\d*)x/;
	    report;
	    $level -= $1;
	    report;
	}
    }
}
