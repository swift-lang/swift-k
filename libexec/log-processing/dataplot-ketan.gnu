reset
set terminal png
set output 'a.png'

#
#set xrange [0:70]
#set yrange [0:400]
#

set ytic 200

set title "modftdock task completion plot"
set xlabel "Hourly slots"
set ylabel "Tasks completed"

plot 'plot.dat' title "tasks" with boxes fs solid 0.4
#plot 'active.dat' title "cores" with boxes fs solid 0.4
