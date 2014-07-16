#!/bin/bash

LOG=$1
TIME=$2

grep 'WeightedHostScoreScheduler CONTACT_SELECTED' $1 | iso-to-secs | normalise-event-start-time-to-any ${TIME} | sed 's/^\([^ ]*\) .* score=\(.*\)$/\1 \2/' |  sed 's/,//g' > weights.tmp

if [ -s weights.tmp ]; then
	gnuplot ${SWIFT_PLOT_HOME}/weights.plot
fi
