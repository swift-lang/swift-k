
To properly generate log plots, you must enable VDL/Karajan logging.

Make sure log4.properties contains:
--------------------------------------
log4j.logger.swift=DEBUG
--------------------------------------

To make a basic load plot:

* Generate the LOG (may set log4j.logger.swift=INFO for this one)
  (assuming the log is titled swift-run.log)
* Make the execute transitions file:
  make LOG=swift-run.log execute.transitions
  These transitions are relative to the Unix epoch
* Make the start time file (this contains the earliest timestamp)
  make LOG=swift-run.log start-time.tmp
* Normalize the transition times
  ./normalise-event-start-time < execute.transitions > execute.norm
* Build up a load data file:
  ./accumulate-load.pl < execute.norm > load.data
* Plot with the JFreeChart-based plotter:
  lines.zsh load.cfg load.eps load.data
