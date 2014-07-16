set terminal png
set output "coaster-qwait-wtime.png"

set title "Wait times vs. Block Walltime"
plot "coaster-qwait-wtime.data" u 1:2 title ""