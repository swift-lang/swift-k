#!/usr/bin/perl -n

# Convert the time unit from seconds to hours

# INPUT:  A Swift log file with Unix time in the first columns
#                  timestamp ...
#         where timestamp is a number
# OUTPUT: The same log file with the timestamp divided by 3600

@tokens = split;
$tokens[0] /= 3600.0;
printf("@tokens\n");
