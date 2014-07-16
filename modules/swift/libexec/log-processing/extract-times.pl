#!/usr/bin/perl -n

# Extract job runtimes for each job from Coasters 
# Cpu log lines

# usage: extract-times.pl < <LOGFILE>

# Requires numerical timestamps (cf. iso-to-secs)
# Uses the Cpu id to determine Cpu start and stop times
# Assumes all jobs are the same
# Output is simple column of runtimes

BEGIN
{
    # Map from Cpu id to job start time on that Cpu
    %cpus = ();
}

if (/Cpu/)
{
    if (/submitting/)
    {
	/(\d*\.\d*) .* Cpu (.*) submitting/;
	my $time = $1;
	my $cpu  = $2;
	$cpus{$cpu} = $time;
    }
    if (/jobTerminated/)
    {
	/(\d*\.\d*) .* Cpu (.*) jobTerminated/;
	my $time = $1;
	my $cpu  = $2;
	my $start    = $cpus{$cpu};
	my $duration = $time - $start;
	printf("%.3f\n", $duration);
    }
}

# Local Variables:
# perl-basic-offset: 2
# End:
