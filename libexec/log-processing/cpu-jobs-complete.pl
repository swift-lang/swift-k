#!/usr/bin/perl

# Accumulate the load level at each point in time
# INPUT:  A Swift log file with Coasters messages:
#                  timestamp ... Cpu ... submitting ...
#                  timestamp ... Cpu ... jobTerminated ...
#         where timestamp is a number
# OUTPUT: lines formatted as "timestamp count"
#         where timestamp and count are numbers,
#          count being the number of completed jobs at that point

$count = 0;

sub report() {
    @tokens = split;
    printf("$tokens[0] $count\n");
}

while (<STDIN>) {
    if (/Cpu/) {
	if (/jobTerminated/) {
	    $count++;
	    report;
	}
    }
}
