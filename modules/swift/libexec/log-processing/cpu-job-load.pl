#!/usr/bin/perl

# Accumulate the load level at each point in time
#
# INPUT:  A Swift log file with Coasters messages:
#                  timestamp ... Cpu ... submitting ...
#                  timestamp ... Cpu ... jobTerminated ...
#         where timestamp is a number
# OUTPUT: lines formatted as "timestamp load"
#         where timestamp and load are numbers

$load = 0;

sub report() {
    @tokens = split;
    printf("$tokens[0] $load\n");
}

while (<STDIN>) {
    if (/Cpu/) {
	if (/submitting/) {
	    $load++;
	    report;
	}
	elsif (/jobTerminated/) {
	    $load--;
	    report;
	}
    }
}
