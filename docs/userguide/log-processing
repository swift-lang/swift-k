
Log Processing
--------------

To properly generate log plots, you must enable VDL/Karajan logging.
TODO:How?


You should check the scripts that you intend to use to determine
what log lines they require and ensure that you are generating
those lines via log4j.properties

Make sure log4.properties contains:
--------------------------------------
log4j.logger.swift=DEBUG
log4j.logger.org.globus.cog.abstraction.coaster.service.job.manager.Cpu=DEBUG
log4j.logger.org.globus.cog.abstraction.coaster.service.job.manager.Block=DEBUG
--------------------------------------
TODO: Does it work for coasters-based runs only?

Normalize event times in the log to the run start time
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Generate the log, assuming the log is titled +swift-run.log+

------------------------------------------
./normalize-log.pl file.contains.start.time swift-run.log > swift-run.norm
------------------------------------------
TODO:In what format does the start time be in 'file.contains.start.time'


Make a basic load plot from Coasters Cpu log lines
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

. Normalize the log.
. Build up a load data file:
+
------------------------------------------
./cpu-job-load.pl < swift-run.norm > load.data
------------------------------------------
. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s load.cfg load.eps load.data
------------------------------------------
Note: Th load.cfg is available from swift/libexec/log-processing/


Make a basic job completion plot from Coasters Cpu log lines
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

. Normalize the log.

. Build up a completed data file:
+
------------------------------------------
./cpu-job-completed.pl < swift-run.norm > completed.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s completed.cfg completed.eps completed.data
------------------------------------------

Make a basic Block allocation plot from Coasters Block log lines
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

. Normalize the log.

. Build up a block allocation data file:
+
------------------------------------------
./block-level.pl < swift-run.norm > blocks.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s blocks.{cfg,eps,data}
------------------------------------------

Make a job runtime distribution plot from Coasters Cpu log lines
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

. Normalize the log.

. Build up a job runtime file:
+
------------------------------------------
./extract-times.pl < swift-run.norm > times.data
------------------------------------------

. Put the job runtimes into 1-second buckets:
+
------------------------------------------
./ buckets.pl 1 times.data > buckets.data
------------------------------------------

. Plot with the JFreeChart-based plotter in usertools/plotter:
+
------------------------------------------
swift_plotter.zsh -s buckets.cfg buckets.eps buckets.data
------------------------------------------
