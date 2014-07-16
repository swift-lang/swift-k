set terminal png

set output 'start-last-times.png'

set key outside below

set ylabel 'arbitrary job number'
set xlabel 'realtime / s'

set style arrow 1 nohead lt 1
set style arrow 2 nohead lt 2
set style arrow 3 nohead lt 3
set style arrow 4 nohead lt 4
set style arrow 5 nohead lt 5
set style arrow 6 nohead lt 6

# set xrange [0:100]
#set yrange [-1:200]

plot \
'start-last-completed.shifted' using 1:2:3:4 with vectors arrowstyle 1 \
title 'vdl:execute2 completed',\
'start-last-failed.shifted' using 1:2:3:4 with vectors arrowstyle 5 \
title "vdl:execute2 failed" , \
'karajan-times.shifted' using 1:2:3:4 with vectors arrowstyle 3 \
title "karajan tasks", \
'workflow.shifted' using 1:2:3:4 with vectors arrowstyle 4 \
title "whole execution", \
'kickstart-times.shifted' using 1:2:3:4 with vectors arrowstyle 2 \
title "kickstart" 
