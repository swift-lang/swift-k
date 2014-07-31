set terminal png
set output "coaster-qwait-size.png"

set title "Wait times vs. Block Size (walltime x worker count)"
plot "coaster-qwait-size.data" u 1:2 title ""