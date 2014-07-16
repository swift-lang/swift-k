set terminal png
set output 'a.png'

set style arrow 1 nohead

set xrange [0:100]
set yrange [-1:120]

plot 'bars' using 1:2:3:4 with vectors arrowstyle 1

