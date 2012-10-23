set terminal png
set output "coaster-qwait-count.png"

set title "Wait times vs. Block Worker Count"
plot "coaster-qwait-count.data" u 1:2 title ""