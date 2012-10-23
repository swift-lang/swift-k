set terminal png

set view map

set output 'slt2.png'

set ylabel 'arbitrary job number'
set xlabel 'realtime / s'

set style arrow 1 nohead

set xrange [0:100]
set yrange [-1:120]

splot 'slt2.data' using 1:2:3:4 title "vdl:execute2" with lines, 'slt3.data' using 1:2:3:4 title "vdl:execute2" with lines

