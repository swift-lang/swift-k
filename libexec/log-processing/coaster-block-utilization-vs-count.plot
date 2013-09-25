set terminal png
set output "coaster-block-utilization-vs-count.png"

set title "Block Utilization vs. Worker Count"
plot "coaster-block-utilization-vs-count.data" u 1:2 title ""