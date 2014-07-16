set terminal png
set output 'p1.png'

set style arrow 1 nohead

#set xrange [0:100]
#set yrange [-1:120]

plot 'p1.data' with vector arrowstyle 1

