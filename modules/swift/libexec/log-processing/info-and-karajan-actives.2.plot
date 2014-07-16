set terminal png
set output 'info-and-karajan-actives.2.png'

set xlabel 'offset from Active notification'
set ylabel 'cumulative events below this offset'

set key outside below

plot 'info-and-karajan-actives.2.data' using 1:2 with lines title 'start time (info - karajan)', 'info-and-karajan-actives.end.2.data' using 1:2 with lines title 'end time (info - karajan)'

