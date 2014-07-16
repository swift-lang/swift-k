set terminal png

set output 'duration-histogram.png'

set key outside below

set ylabel 'duration of event / seconds'
set xlabel 'arbitrary event number'

set style arrow 1 nohead lt 1
set style arrow 2 nohead lt 2
set style arrow 3 nohead lt 3
set style arrow 4 nohead lt 4
set style arrow 5 nohead lt 5
set style arrow 6 nohead lt 6
set style arrow 7 nohead lt 7
set style arrow 8 nohead lt 8
set style arrow 9 nohead lt 9
set style arrow 10 nohead lt 10
set style arrow 11 nohead lt 11
set style arrow 12 nohead lt 12
set style arrow 13 nohead lt 13
set style arrow 14 nohead lt 14
set style arrow 15 nohead lt 15
set style arrow 16 nohead lt 16
set style arrow 17 nohead lt 17
set style arrow 18 nohead lt 18
set style arrow 19 nohead lt 19
set style arrow 20 nohead lt 20
set style arrow 21 nohead lt 21
set style arrow 22 nohead lt 22
set style arrow 23 nohead lt 23
set style arrow 24 nohead lt 24
set style arrow 25 nohead lt 25
set style arrow 26 nohead lt 26
set style arrow 27 nohead lt 27
set style arrow 28 nohead lt 28
set style arrow 29 nohead lt 29
set style arrow 30 nohead lt 30

# set xrange [0:MAXTIME]
# set yrange [0:EVENTCOUNT]

set border 0

plot 'duration-histogram.tmp' with lines title 'CHANNELDESC'

