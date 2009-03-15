#!/bin/bash

grep 'WeightedHostScoreScheduler Old score:' $1 | iso-to-secs | normalise-event-start-time | sed 's/^\([^ ]*\) .* new score: \(.*\)$/\1 \2/' |  sed 's/,//g' > weights.tmp

gnuplot ${SWIFT_PLOT_HOME}/weights.plot



