set terminal png
set output 'everylog-vs-versions.png'

set ylabel 'Swift SVN revision'
set xlabel 'time of Swift invocation'

set key outside below

plot 'everylog-vs-versions.data'

