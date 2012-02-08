set terminal png enhanced
set output "activeplot.png"
set nokey
set xlabel "Time in sec"
set ylabel "number of active jobs"
set title "Active SciColSim jobs"
plot "plot_active.txt" using 1 with line
