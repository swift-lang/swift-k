
Log Processing
==============

To properly generate log plots, you must enable VDL/Karajan logging.

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
./iso-to-secs < swift-run.log > tmp.log

. Make the start time file (this contains the earliest timestamp)
------------------------------------------
make LOG=tmp.log start-time.tmp
------------------------------------------

. Normalize the transition times
------------------------------------------
./normalise-event-start-time < tmp.log > tmp.norm
------------------------------------------

. Build up a load data file:
------------------------------------------
./cpu-job-load.pl < tmp.norm > load.data
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
./cpu-job-completed.pl < tmp.norm > completed.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
------------------------------------------
lines.zsh completed.cfg completed.eps completed.data
------------------------------------------
