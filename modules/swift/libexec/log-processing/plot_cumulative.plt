set terminal png enhanced
#set term postscript eps enhanced 
set output "cumulativeplot.png"
set nokey
set xlabel "Time in seconds"
set ylabel "number of completed jobs"
set title "Cumulative jobs"
plot "plot_cumulative.txt" using 1:2 with lines
