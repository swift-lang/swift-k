set terminal png

set output 'kj-vs-loadavg.png'

set key outside below

set ylabel '5m loadavg of gatekeeper host'
set xlabel 'number of karajan jobs in progress'

set border 0

starttime = 1225905859
endtime = 1225945175
timedelta = endtime - starttime

kolr(t) = int(256 *(t-starttime) / timedelta)
# ko(t) = (kolr(t)*65536+256+(256-kolr(t)))
ko(t) = (256-kolr(t)) + 65536 * kolr(t)

plot \
'/tmp/kj-vs-loadavg' using 1:2:(ko($3)) with dots lc rgb variable
