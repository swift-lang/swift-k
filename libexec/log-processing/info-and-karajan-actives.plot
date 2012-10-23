set terminal png
set output 'info-and-karajan-actives.png'

set ylabel 'offset from Active notification'
set xlabel 'time of start according to info'

set key outside below

plot 'info-and-karajan-actives.data' using 1:3 title 'start times (info - karajan)', 'info-and-karajan-actives.end.data' using 1:3 title 'end times (info - karajan)'

