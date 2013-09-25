#!/bin/bash

# specify the time limit for the cron job
#SBATCH --time=00:00:10

# use cron.log and append to it
#SBATCH --output=cron.log
#SBATCH --open-mode=append

# the account, partition, and qos should not be changed
#SBATCH --account=cron-account
#SBATCH --partition=cron
#SBATCH --qos=cron

# Specify a valid cron string for the schedule
# this is daily at 5:05 AM
SCHEDULE='* * * * *'

echo hello on $(hostname) at $(date)

# resubmit this script with --begin set to the next scheduled cron time
# next-cron-time is a script that parses a cron schedule string and returns
# the next execution time
sbatch -w midway001.rcc.uchicago.edu --quiet --begin=$(next-cron-time "$SCHEDULE") cron.sbatch