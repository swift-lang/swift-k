
Log Processing
==============

To properly generate log plots, you must enable VDL/Karajan logging.

You should check the scripts that you intend to use to determine 
what log lines they require and ensure that you are generating 
those lines via log4j.properties

Make sure log4.properties contains:
--------------------------------------
log4j.logger.swift=DEBUG
--------------------------------------

Make a basic load plot from Coasters Cpu log lines
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
. Generate the log (may set log4j.logger.swift=INFO for this one)
(assuming the log is titled swift-run.log)

. Convert the log times to Unix time
------------------------------------------
./iso-to-secs < swift-run.log > swift-run.time

. Make the start time file (this contains the earliest timestamp)
------------------------------------------
make LOG=swift-run.log start-time.tmp
------------------------------------------
or 
------------------------------------------
extract-start-time swift-run.log > start-time.tmp
------------------------------------------

. Normalize the transition times
------------------------------------------
./normalise-event-start-time < swift-run.time > swift-run.norm
------------------------------------------

. Build up a load data file:
------------------------------------------
./cpu-job-load.pl < swift-run.norm > load.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
------------------------------------------
lines.zsh load.cfg load.eps load.data
------------------------------------------

Make a basic job completion plot from Coasters Cpu log lines
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Same as above, but:

[start=5]
. Build up a completed data file:
------------------------------------------
./cpu-job-completed.pl < swift-run.norm > completed.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
------------------------------------------
lines.zsh completed.cfg completed.eps completed.data
------------------------------------------

Make a basic Block allocation plot from Coasters Block log lines
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Same as above, but:

[start=5]
. Build up a block allocation data file:
------------------------------------------
./block-level.pl < swift-run.norm > blocks.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
------------------------------------------
lines.zsh blocks.{cfg,eps,data}
------------------------------------------

Make a job runtime distribution plot from Coasters Cpu log lines
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Same as above, but:

[start=5]
. Build up a job runtime file: 
------------------------------------------
./extract-times.pl < swift-run.norm > times.data
------------------------------------------

. Put the job runtimes into 1-second buckets: 
------------------------------------------
./ buckets.pl 1 times.data > buckets.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
------------------------------------------
lines.zsh buckets.cfg buckets.eps buckets.data
------------------------------------------
