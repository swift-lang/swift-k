set terminal png
set output 'everylog-versions-cumulative.png'

set ylabel 'cumulative invocations'
set xlabel 'svn revision'

set key outside below

plot 'everylog-versions-cumulative.data' with lines

