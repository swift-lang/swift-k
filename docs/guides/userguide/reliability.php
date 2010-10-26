<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>16. Reliability mechanisms</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="kickstart.php" title="15. Kickstart"><link rel="next" href="clustering.php" title="17. Clustering"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">16. Reliability mechanisms</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="kickstart.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="clustering.php">Next</a></td></tr></table><hr></div><div class="section" title="16. Reliability mechanisms"><div class="titlepage"><div><div><h2 class="title"><a name="reliability"></a>16. Reliability mechanisms</h2></div></div></div><p>
This section details reliabilty mechanisms in Swift: retries, restarts
and replication.
	</p><div class="section" title="16.1. Retries"><div class="titlepage"><div><div><h3 class="title"><a name="retries"></a>16.1. Retries</h3></div></div></div><p>
If an application procedure execution fails, Swift will attempt that
execution again repeatedly until it succeeds, up until the limit
defined in the <code class="literal">execution.retries</code> configuration
property.
		</p><p>
Site selection will occur for retried jobs in the same way that it happens
for new jobs. Retried jobs may run on the same site or may run on a
different site.
		</p><p>
If the retry limit <code class="literal">execution.retries</code> is reached for an
application procedure, then that application procedure will fail. This will
cause the entire run to fail - either immediately (if the
<code class="literal">lazy.errors</code> property is <code class="literal">false</code>) or
after all other possible work has been attempted (if the
<code class="literal">lazy.errors</code> property is <code class="literal">true</code>).
		</p></div><div class="section" title="16.2. Restarts"><div class="titlepage"><div><div><h3 class="title"><a name="restart"></a>16.2. Restarts</h3></div></div></div><p>
If a run fails, Swift can resume the program from the point of
failure. When a run fails, a restart log file will be left behind in
a file named using the unique job ID and a <code class="filename">.rlog</code> extension. This restart log
can then be passed to a subsequent Swift invocation using the <code class="literal">-resume</code>
parameter. Swift will resume execution, avoiding execution of invocations
that have previously completed successfully. The SwiftScript source file
and input data files should not be modified between runs.
		</p><p>
Every run creates a restart
log file with a named composed of the file name of the workflow
being executed, an invocation ID, a numeric ID, and the <code class="filename">.rlog</code> extension. For example, <code class="filename">example.swift</code>, when executed, could produce
the following restart log file: <code class="filename">example-ht0adgi315l61.0.rlog</code>. Normally, if
the run completes successfully, the restart log file is
deleted. If however the workflow fails, <span class="command"><strong>swift</strong></span>
can use the restart log file to continue
execution from a point before the
failure occurred. In order to restart from a restart log
file, the <code class="option">-resume <em class="parameter"><code><code class="filename">logfile</code></code></em></code> argument can be
used after the SwiftScript program file name. Example:

</p><pre class="screen">
<code class="prompt">$</code> <span class="command"><strong>swift</strong></span> <code class="option">-resume <code class="filename">example-ht0adgi315l61.0.rlog</code></code> <code class="option"><code class="filename">example.swift</code></code>.
</pre><p>

		</p></div><div class="section" title="16.3. Replication"><div class="titlepage"><div><div><h3 class="title"><a name="replication"></a>16.3. Replication</h3></div></div></div><p>
When an execution job has been waiting in a site queue for a certain
period of time, Swift can resubmit replicas of that job (up to the limit
defined in the <code class="literal">replication.limit</code> configuration property).
When any of those jobs moves from queued to active state, all of the
other replicas will be cancelled.
		</p><p>
This is intended to deal with situations where some sites have a substantially
longer (sometimes effectively infinite) queue time than other sites.
Selecting those slower sites can cause a very large delay in overall run time.
		</p><p>
Replication can be enabled by setting the
<code class="literal">replication.enabled</code> configuration property to
<code class="literal">true</code>. The maximum number of replicas that will be
submitted for a job is controlled by the <code class="literal">replication.limit</code>
configuration property.
		</p><p>
When replication is enabled, Swift will also enforce the
<code class="literal">maxwalltime</code> profile setting for jobs as documented in
the <a class="link" href="profiles.php" title="11. Profiles">profiles section</a>.
		</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="kickstart.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="clustering.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">15. Kickstart </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 17. Clustering</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
