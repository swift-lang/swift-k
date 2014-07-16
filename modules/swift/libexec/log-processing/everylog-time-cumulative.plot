set terminal png
set output 'everylog-time-cumulative.png'

set ylabel 'cumulative invocations'
set xlabel 'time'

set timefmt '%Y-%m-%d'
set xdata time

set format x "%b %y"


set key outside below

plot 'everylog-time-cumulative.data' using 1:2 with lines

