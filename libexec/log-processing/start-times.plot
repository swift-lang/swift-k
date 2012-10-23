set terminal png
set output 'start-times.png'

set style arrow 1 nohead

#set xrange [0:100]
#set yrange [-1:120]

plot 'start-times.data' using 2

