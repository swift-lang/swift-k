Log Processing
==============

You should check the scripts that you intend to use to determine
what log lines they require and ensure that you are generating
those lines via +swift/etc/log4j.properties+.

To properly generate log plots, you must enable VDL/Karajan logging.
Make sure +log4.properties+ contains:
--------------------------------------
log4j.logger.swift=DEBUG
log4j.logger.org.globus.cog.abstraction.coaster.service.job.manager.Cpu=DEBUG
log4j.logger.org.globus.cog.abstraction.coaster.service.job.manager.Block=DEBUG
--------------------------------------

The script programs referred to below are in
+swift/libexec/log-processing+.

Normalize event times in the log to the run start time
------------------------------------------------------

* Convert timestamps in the log from iso to seconds format

------------------------------------------
./iso-to-secs < original.log > swift-run.log
------------------------------------------

* Generate the log, assuming the log is titled +swift-run.log+

------------------------------------------
./normalize-log.pl file.contains.start.time swift-run.log > swift-run.norm
------------------------------------------
Note: This script not available.

Make a basic load plot from Coasters Cpu log lines
--------------------------------------------------

. Normalize the log.
. Build up a load data file:
+
------------------------------------------
./cpu-job-load.pl < swift-run.norm > load.data
------------------------------------------
Note: This script not available.

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s load.cfg load.eps load.data
------------------------------------------
Note: The load.cfg is available from swift/libexec/log-processing/
Note: This script not available.


Make a basic job completion plot from Coasters Cpu log lines
------------------------------------------------------------

. Normalize the log.

. Build up a completed data file:
+
------------------------------------------
./cpu-job-complete.pl < swift-run.norm > completed.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s completed.cfg completed.eps completed.data
------------------------------------------
Note: This script not available.

Make a basic Block allocation plot from Coasters Block log lines
----------------------------------------------------------------

. Normalize the log.

. Build up a block allocation data file:
+
------------------------------------------
./block-level.pl < swift-run.norm > blocks.data
------------------------------------------
Note: This script not available.

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s blocks.{cfg,eps,data}
------------------------------------------
Note: This script not available.

Make a job run time distribution plot from Coasters Cpu log lines
-----------------------------------------------------------------

. Normalize the log.

. Build up a job runtime file:
+
------------------------------------------
./extract-times.pl < swift-run.norm > times.data
------------------------------------------

. Put the job runtimes into 1-second buckets:
+
------------------------------------------
./buckets.pl 1 times.data > buckets.data
------------------------------------------
Note: This script not available.

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s buckets.cfg buckets.eps buckets.data
------------------------------------------
Note: This script not available.

== Utilities

+iso-to-secs+::
Convert human-readable log dates to Unix time

+extract-start-time+::
Pull out the first Unix timestamp from the log file

+normalise-event-start-time+::
Convert Unix seconds to seconds from start time, given a start time file

+normalise-event-start-time-to-any+::
Convert Unix seconds to seconds from start time, given a start time number

+sec-to-hour.pl+::
Convert seconds to hours in the Unix time column.

+sec-to-min.pl+::
Convert seconds to minutes in the Unix time column.
