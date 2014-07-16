set terminal png
set output "coaster-block-timeline.png"

set title "Queued/Active Coaster Workers"
set style data steps
plot "coaster-blocks.data" u 1:2 w steps title "Queued Workers", "coaster-blocks.data" u 1:3 w steps title "Running Workers"