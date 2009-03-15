#!/bin/bash

# timenumberhere INFO  WeightedHostScoreScheduler Sorted: [teraport: 5.200(10.592):69/150]

grep 'WeightedHostScoreScheduler Sorted: ' $1 |\
iso-to-secs |\
sed 's/^\([^ ]*\) INFO  WeightedHostScoreScheduler Sorted: \[\([^\:]*\):\([0-9,.]*\)(\([0-9.,]*\)):\([0-9,.]*\)\/\([0-9,.]*\)\(.*\)].*$/\1 \2 \3 \4 \5 \6 \7/' 

# 1193841727.919 DEBUG WeightedHostScoreScheduler multiplyScore(teraport:75.720(82.423):1212/1155 overloaded, -0.2)

grep 'WeightedHostScoreScheduler multiplyScore(' $1 |\
iso-to-secs |\
sed 's/^\([^ ]*\) DEBUG WeightedHostScoreScheduler multiplyScore(\([^\:]*\):\([0-9,.]*\)(\([0-9.,]*\)):\([0-9,.]*\)\/\([0-9,.]*\)\([^,]*\),.*$/\1 \2 \3 \4 \5 \6 \7/'

# output is
# timestamp sitename score tscore load diddleload overloaded?


# output is
# timestamp sitename score tscore load diddleload overloaded?
