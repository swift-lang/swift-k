<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>11. Profiles</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="engineconfiguration.php" title="10. Swift configuration properties"><link rel="next" href="sitecatalog.php" title="12. The Site Catalog - sites.xml"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
		<!-- entire page container -->
		<div id="container">
			<!-- header -->
			<div id="header">
				<?php require('../../inc/header.php') ?>
			</div>
			<!-- end header -->
			<!-- nav -->
			<div id="nav">
				<?php require('../../inc/nav.php') ?>
			</div>
			<!-- end nav -->
			<!-- content container -->
			<div id="content">
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">11. Profiles</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="engineconfiguration.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="sitecatalog.php">Next</a></td></tr></table><hr></div><div class="section" title="11. Profiles"><div class="titlepage"><div><div><h2 class="title"><a name="profiles"></a>11. Profiles</h2></div></div></div><p>
Profiles are configuration parameters than can be specified either for
sites or for transformation catalog entries. They influence the behaviour
of Swift towards that site (for example, by changing the load Swift will
place on that sites) or when running a particular procedure.
		</p><p>
Profile entries for a site are specified in the site catalog. Profile
entries for specific procedures are specified in the transformation
catalog.
		</p><div class="section" title="11.1. Karajan namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.karajan"></a>11.1. Karajan namespace</h3></div></div></div><p><a name="profile.karajan.maxsubmitrate"></a><code class="literal">maxSubmitRate</code> limits the maximum rate of job submission, in jobs per second.
For example:
</p><pre class="screen">
&lt;profile namespace="karajan" key="maxSubmitRate"&gt;0.2&lt;/profile&gt;
</pre><p>
will limit job submission to 0.2 jobs per second (or equivalently,
one job every five seconds).
			</p><p><a name="profile.karajan.jobThrottle"></a><code class="literal">jobThrottle</code>
allows the job throttle factor (see Swift property <a class="link" href="engineconfiguration.php#property.throttle.score.job.factor">throttle.score.job.factor</a>) to be set per site.
			</p><p><a name="profile.karajan.initialScore"></a><code class="literal">initialScore</code>
allows the initial score for rate limiting and site selection to be set to
a value other than 0.
			</p><p><a name="profile.karajan.delayBase"></a><code class="literal">delayBase</code> controls how much a site will be delayed when it performs poorly. With each reduction
in a sites score by 1, the delay between execution attempts will increase by
a factor of delayBase.</p><p><a name="profile.karajan.status.mode"></a><code class="literal">status.mode</code> allows the status.mode property to be set per-site instead of for an entire run.
See the Swift configuration properties section for more information.
(since Swift 0.8)</p></div><div class="section" title="11.2. swift namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.swift"></a>11.2. swift namespace</h3></div></div></div><p><a name="profile.swift.storagesize"></a><code class="literal">storagesize</code> limits the
amount of space that will be used on the remote site for temporary files.
When more than that amount of space is used, the remote temporary file
cache will be cleared using the algorithm specified in the
<a class="link" href="engineconfiguration.php#property.caching.algorithm"><code class="literal">caching.algorithm</code></a> property.
			</p><p><a name="swift.wrapperInterpreter"></a><code class="literal">wrapperInterpreter</code>
The wrapper interpreter indicates the command (executable) to be used to run the Swift wrapper
script. The default is "/bin/bash" on Unix sites and "cscript.exe" on Windows sites.
			</p><p><a name="swift.wrapperInterpreterOptions"></a><code class="literal">wrapperInterpreterOptions</code>
Allows specifying additional options to the executable used to run the Swift wrapper. The defaults
are no options on Unix sites and "//Nologo" on Windows sites.
			</p><p><a name="swift.wrapperScript"></a><code class="literal">wrapperScript</code>
Specifies the name of the wrapper script to be used on a site. The defaults are "_swiftwrap" on 
Unix sites and "_swiftwrap.vbs" on Windows sites. If you specify a custom wrapper script, it 
must be present in the "libexec" directory of the Swift installation.
			</p><p><a name="swift.cleanupCommand"></a><code class="literal">cleanupCommand</code>
Indicates the command to be run at the end of a Swift run to clean up the run directories on a 
remote site. Defaults are "/bin/rm" on Unix sites and "cmd.exe" on Windows sites
			</p><p><a name="swift.cleanupCommandOptions"></a><code class="literal">cleanupCommandOptions</code>
Specifies the options to be passed to the cleanup command above. The options are passed in the
argument list to the cleanup command. After the options, the last argument is the directory
to be deleted. The default on Unix sites is "-rf". The default on Windows sites is ["/C", "del", "/Q"].
			</p></div><div class="section" title="11.3. Globus namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.globus"></a>11.3. Globus namespace</h3></div></div></div><p><a name="profile.globus.maxwalltime"></a><code class="literal">maxwalltime</code> specifies a walltime limit for each job, in minutes.
			</p><p>
The following formats are recognized:
				</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">Minutes</li><li class="listitem">Hours:Minutes</li><li class="listitem">Hours:Minutes:Seconds</li></ul></div><p>
			</p><p>Example:</p><pre class="screen">
localhost	echo	/bin/echo	INSTALLED	INTEL32::LINUX	GLOBUS::maxwalltime="00:20:00"
</pre><p>When replication is enabled (see <a class="link" href="reliability.php#replication" title="16.3. Replication">replication</a>), then walltime will also be enforced at the Swift client side: when
a job has been active for more than twice the maxwalltime, Swift will kill the
job and regard it as failed.
			</p><p>
When clustering is used, <code class="literal">maxwalltime</code> will be used to
select which jobs will be clustered together. More information on this is
available in the <a class="link" href="clustering.php" title="17. Clustering">clustering section</a>.
			</p><p>
When coasters as used, <code class="literal">maxwalltime</code> influences the default
coaster worker maxwalltime, and which jobs will be sent to which workers.
More information on this is available in the <a class="link" href="coasters.php" title="18. Coasters">coasters
section</a>.
			</p><p><a name="profile.globus.queue"></a><code class="literal">queue</code> 
is used by the PBS, GRAM2 and GRAM4 providers. This profile
entry specifies which queue jobs will be submitted to. The valid queue names
are site-specific.
			</p><p><a name="profile.globus.host_types"></a><code class="literal">host_types</code>
specifies the types of host that are permissible for a job to run on.
The valid values are site-specific. This profile entry is used by the
GRAM2 and GRAM4 providers.
			</p><p><a name="profile.globus.condor_requirements"></a><code class="literal">condor_requirements</code> allows a requirements string to be specified
when Condor is used as an LRM behind GRAM2. Example: <code class="literal">&lt;profile namespace="globus" key="condor_requirements"&gt;Arch == "X86_64" || Arch="INTEL"&lt;/profile&gt;</code>
			</p><p><a name="profile.slots"></a><code class="literal">slots</code>
When using <a class="link" href="coasters.php" title="18. Coasters">coasters</a>, this parameter
specifies the maximum number of jobs/blocks that the coaster scheduler will have running at any given time.
The default is 20.
			</p><p><a name="profile.workersPerNode"></a><code class="literal">workersPerNode</code>
This parameter determines how many coaster workers are 
started one each compute node. The default value is 1.
			</p><p><a name="profile.nodeGranularity"></a><code class="literal">nodeGranularity</code>
When allocating a coaster worker block, this parameter
restricts the number of nodes in a block to a multiple of this value. The total number of workers will
then be a multiple of workersPerNode * nodeGranularity. The default value is 1.
			</p><p><a name="profile.allocationStepSize"></a><code class="literal">allocationStepSize</code>
Each time the coaster block scheduler computes a schedule, it will attempt to allocate a
number of slots from the number of available slots (limited using the above slots profile). This
parameter specifies the maximum fraction of slots that are allocated in one schedule. Default is
0.1.
			</p><p><a name="profile.lowOverallocation"></a><code class="literal">lowOverallocation</code>
Overallocation is a function of the walltime of a job which determines how long (time-wise) a
worker job will be. For example, if a number of 10s jobs are submitted to the coaster service, 
and the overallocation for 10s jobs is 10, the coaster scheduler will attempt to start worker
jobs that have a walltime of 100s. The overallocation is controlled by manipulating the end-points
of an overallocation function. The low endpoint, specified by this parameter, is the overallocation
for a 1s job. The high endpoint is the overallocation for a (theoretical) job of infinite length.
The overallocation for job sizes in the [1s, +inf) interval is determined using an exponential decay function:

overallocation(walltime) = walltime * (lowOverallocation - highOverallocation) * exp(-walltime * overallocationDecayFactor) + highOverallocation

The default value of lowOverallocation is 10.
			</p><p><a name="profile.highOverallocation"></a><code class="literal">highOverallocation</code>
The high overallocation endpoint (as described above). Default: 1
			</p><p><a name="profile.overallocationDecayFactor"></a><code class="literal">overallocationDecayFactor</code>
The decay factor for the overallocation curve. Default 0.001 (1e-3).
			</p><p><a name="profile.spread"></a><code class="literal">spread</code>
When a large number of jobs is submitted to the a coaster service, the work is divided into blocks. This
parameter allows a rough control of the relative sizes of those blocks. A value of 0 indicates that all work
should be divided equally between the blocks (and blocks will therefore have equal sizes). A value of 1 
indicates the largest possible spread. The existence of the spread parameter is based on the assumption
that smaller overall jobs will generally spend less time in the queue than larger jobs. By submitting
blocks of different sizes, submitted jobs may be finished quicker by smaller blocks. Default: 0.9.
			</p><p><a name="profile.reserve"></a><code class="literal">reserve</code>
Reserve time is a time in the allocation of a worker that sits at the end of the worker time and 
is useable only for critical operations. For example, a job will not be submitted to a worker if 
it overlaps its reserve time, but a job that (due to inaccurate walltime specification) runs into
the reserve time will not be killed (note that once the worker exceeds its walltime, the queuing 
system will kill the job anyway). Default 10 (s).
			</p><p><a name="profile.maxnodes"></a><code class="literal">maxnodes</code>
Determines the maximum number of nodes that can be allocated in one coaster block. Default: unlimited.
			</p><p><a name="profile.maxtime"></a><code class="literal">maxtime</code>
Indicates the maximum walltime that a coaster block can have. Default: unlimited.
			</p><p><a name="profile.remoteMonitorEnabled"></a><code class="literal">remoteMonitorEnabled</code>
If set to "true", the client side will get a Swing window showing, graphically, the state of the
coaster scheduler (blocks, jobs, etc.). Default: false
			</p></div><div class="section" title="11.4. env namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.env"></a>11.4. env namespace</h3></div></div></div><p>
Profile keys set in the env namespace will be set in the unix environment of the
executed job. Some environment variables influence the worker-side
behaviour of Swift:
			</p><p>
<code class="literal">PATHPREFIX</code> - set in env namespace profiles. This path is prefixed onto the start
of the <code class="literal">PATH</code> when jobs are
executed. It can be more useful than setting the <code class="literal">PATH</code> environment variable directly,
because setting <code class="literal">PATH</code> will cause the execution site's default path to be lost.
			</p><p>
<code class="literal">SWIFT_JOBDIR_PATH</code> - set in env namespace profiles. If set, then Swift will
use the path specified here as a worker-node local temporary directory to
copy input files to before running a job. If unset, Swift will keep input
files on the site-shared filesystem. In some cases, copying to a worker-node
local directory can be much faster than having applications access the
site-shared filesystem directly.
			</p><p>
<code class="literal">SWIFT_EXTRA_INFO</code> - set in env namespace profiles. If set,
then Swift will execute the command specified in
<code class="literal">SWIFT_EXTRA_INFO</code> on execution sites immediately before
each application execution, and will record the stdout of that command in the
wrapper info log file for that job. This is intended to allow software
version and other arbitrary information about the remote site to be gathered
and returned to the submit side. (since Swift 0.9)
			</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="engineconfiguration.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="sitecatalog.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">10. Swift configuration properties </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 12. The Site Catalog - sites.xml</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
