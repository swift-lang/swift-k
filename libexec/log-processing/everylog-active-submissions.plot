set terminal png
set output 'all-logs-active-tasks.png'

set ylabel 'cumulative invocations'
set xlabel 'time (s)'

set timefmt '%Y-%m-%d'
set xdata time

set key outside below

set format x "%b %y"

plot 'all-logs-active-tasks.data' using 1:2 with lines

