export LOG

all: clean webpage

include makefile.implicit
include makefile.falkon
include makefile.karatasks
include makefile.webpage
include makefile.kickstart
include makefile.errors
include makefile.coasters

distributable: 
	rm -f *.tmp log *.transitions tmp-*

distribute:
	mkdir -p $(SDL)
	cp *.event *.html *.png $(SDL)

clean:
	rm -f start-times.data kickstart-times.data start-time.tmp end-time.tmp threads.list tasks.list log *.data *.shifted *.png *.event *.coloured-event *.total *.tmp *.transitions *.last karatasks-type-counts.txt index.html *.lastsummary execstages.plot total.plot colour.plot jobs-sites.html jobs.retrycount.summary kickstart.stats execution-counts.txt site-duration.txt jobs.retrycount sp.plot karatasks.coloured-sorted-event *.cedps *.stats t.inf *.seenstates tmp-* clusterstats  trname-summary sites-list.data.nm info-md5sums pse2d-tmp.eip karajan.html falkon.html execute2.html info.html execute.html kickstart.html scheduler.html assorted.html

t.inf: $(LOG)
	compute-t-inf > t.inf < $(LOG)

execution-counts.txt: execute.event execute2.event kickstart.event
	execution-summaries > execution-counts.txt

start-time.tmp: $(LOG)
	extract-start-time > start-time.tmp

end-time.tmp: $(LOG)
	extract-end-time > end-time.tmp

start-times.data: log
	extract-start-times > start-times.data

workflow.data: log start-time.tmp end-time.tmp
	whole-workflow-time > workflow.data

start-last-times.data: start-times.data log combine-start-last-times start-time.tmp end-time.tmp
	combine-start-last-times > start-last-times.data

shifted: start-last-times.data kickstart-times.data karajan-times.data workflow.data
	autospace-data

start-last-completed.shifted:
	split-start-times-shifted COMPLETED < start-last-times.shifted > start-last-completed.shifted

start-last-failed.shifted:
	split-start-times-shifted FAILED < start-last-times.shifted > start-last-failed.shifted

start-last-times.png: shifted start-last-times.plot start-last-failed.shifted start-last-completed.shifted
	gnuplot $(SWIFT_PLOT_HOME)/start-last-times.plot

threads.list: log
	list-known-threads > threads.list

workflow.event: start-time.tmp end-time.tmp
	whole-workflow-event $(LOG) > workflow.event

execute-start-times.data: log
	extract-execute-start-times > execute-start-times.data

sites-list.data: execute2.event
	cat execute2.event | cut -f 6 -d ' ' | sort | uniq > sites-list.data

sites-list.data.nm: sites-list.data
	number-sites-list < $< > $@

jobs-sites.html: sites-list.data execute2.event
	table-jobs-sites > jobs-sites.html

site-duration.txt: execute2.event sites-list.data
	per-site-execute2-durations > site-duration.txt

execute2.coloured-event: execute2.event sites-list.data.nm
	colour-execute2

execute2-coloured.png: execute2.coloured-event sites-list.data start-time.tmp workflow.event
	coloured-event-plot execute2.coloured-event execute2-coloured.png sites-list.data

execute2.transitions: $(LOG)
	log-to-execute2-transitions < $(LOG) > execute2.transitions

execute.transitions: $(LOG)
	log-to-execute-transitions < $(LOG) > execute.transitions

initshareddir.transitions: $(LOG)
	log-to-initshareddir-transitions < $(LOG) > initshareddir.transitions

createdirset.transitions: $(LOG)
	log-to-createdirset-transitions < $(LOG) > createdirset.transitions

dostagein.transitions: $(LOG)
	log-to-dostagein-transitions < $(LOG) > dostagein.transitions

dostageout.transitions: $(LOG)
	log-to-dostageout-transitions < $(LOG) > dostageout.transitions

compound.transitions: $(LOG)
	log-to-compound-transitions $(LOG) > compound.transitions

internalproc.transitions: $(LOG)
	log-to-internal-proc-transitions $(LOG) > internalproc.transitions

## retry analysis

jobs.retrycount.summary: execute2.event
	cat execute2.event  | cut -f 5 -d ' ' | sort | uniq -c | sort | sed 's/^ *\(.*\) .*$$/\1/' | uniq -c > jobs.retrycount.summary

execstages.png: execute2.event dostagein.event dostageout.event start-time.tmp workflow.event 
	execstages-plot

simple-start-time.tmp: $(LOG) 
	simple-start-time.sh $(LOG) $(@)

# weight processing
weights.png: simple-start-time.tmp $(LOG)
	weights.sh $(LOG) $(shell cat simple-start-time.tmp ) 

info.transitions:
	info-to-transitions $(IDIR) > $@

info.zeroed.transitions: start-time.tmp
	info-to-zeroed-transitions $(IDIR) > $@

info-md5sums: execute2.event
	info-to-md5 > $@

karatasks.clusters.transitions: karatasks.JOB_SUBMISSION.transitions
	grep cluster- $< | grep --invert-match Submitted > $@

cluster.report: karatasks.clusters.lastsummary karatasks.clusters.last karatasks.clusters.event
	cluster-report

clusterstats: karatasks.clusters.lastsummary karatasks.clusters.last karatasks.clusters.event
	cluster-stats > clusterstats

trname-summary: execute.event
	cat execute.event | cut -f 5 -d ' ' | sort  | uniq -c > trname-summary

tscores.data: $(LOG)
	tscore.sh $(LOG) | normalise-event-start-time | sed 's/\([^ ]*\) [^ ]* [^ ]* [^ ]* \([^ ]*\) \([^ ]*\).*/\1 \2 \3/' > tscores.data

tscores.png: tscores.data
	gnuplot $(SWIFT_PLOT_HOME)/tscores.plot

aggregate.html: all-kickstart.stats aggregate.html.template
	m4 aggregate.html.template > aggregate.html

aggregate-output: all-kickstart.png all-kickstart-total.png aggregate.html

reports.html:
	overview-reports.sh > reports.html

cluster-jobcount-histogram.png: clusterstats
	plot-cluster-jobcount-histogram

jobid-karajanid-bindings: $(LOG)
	extract-jobid-karajanid-bindings  <$(LOG) >jobid-karajanid-bindings

karatasks.JOB_SUBMISSION.annotated-execute2.transitions: karatasks.JOB_SUBMISSION.transitions execute2.event
	annotate-karatasks-with-execute2-id < karatasks.JOB_SUBMISSION.transitions > karatasks.JOB_SUBMISSION.annotated-execute2.transitions

everylog-vs-versions.data:
	create-everylog-vs-versions-data $(LOG)
