set terminal png
set output "coaster-block-utilization.png"

set title "Block Utilization (%)"
plot "coaster-block-utilization.data" w linespoints pt 3 title ""