<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>18. Coasters</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="clustering.php" title="17. Clustering"><link rel="next" href="localhowtos.php" title="19. How-To Tips for Specific User Communities"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">18. Coasters</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="clustering.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="localhowtos.php">Next</a></td></tr></table><hr></div><div class="section" title="18. Coasters"><div class="titlepage"><div><div><h2 class="title"><a name="coasters"></a>18. Coasters</h2></div></div></div><p>Coasters were introduced in Swift v0.6 as an experimental feature.
</p><p>
In many applications, Swift performance can be greatly enhanced by the
use of CoG coasters. CoG coasters provide a low-overhead job submission
and file transfer mechanism suited for the execution of short jobs
(on the order of a few seconds) and the transfer of small files (on the
order of a few kilobytes) for which other grid protocols such as GRAM
and GridFTP are poorly suited.
</p><p>
The coaster mechanism submits a head job using some other execution
mechanism such as GRAM, and for each worker node that will be used in
a remote cluster, it submits a worker job, again using some other
execution mechanism such as GRAM. Details on the design of the coaster
mechanism can be found
<a class="ulink" href="http://wiki.cogkit.org/wiki/Coasters" target="_top">
here.</a>
</p><p>
The head job manages file transfers and the dispatch of execution jobs
to workers. Much of the overhead associated with other grid protocols
(such as authentication and authorization, and allocation of worker nodes
by the site's local resource manager) is reduced, because that overhead
is associated with the allocation of a coaster head or coaster worker,
rather than with every Swift-level procedure invocation; potentially hundreds
or thousands of Swift-level procedure invocations can be run through a single
worker.
</p><p>
Coasters can be configured for use in two situations: job execution and
file transfer.
</p><p>
To use for job execution, specify a sites.xml execution element like this:
</p><pre class="screen">
&lt;execution provider="coaster" jobmanager="gt2:gt2:pbs" url="grid.myhost.org"&gt;
</pre><p>
The jobmanager string contains more detail than with other providers. It
contains either two or three colon separated fields:
1:the provider to be use to execute the coaster head job - this provider
will submit from the Swift client side environment. Commonly this will be
one of the GRAM providers; 2: the provider
to be used to execute coaster worker jobs. This provider will be used
to submit from the coaster head job environment, so a local scheduler
provider can sometimes be used instead of GRAM. 3: optionally, the
jobmanager to be used when submitting worker job using the provider
specified in field 2.
</p><p>
To use for file transfer, specify a sites.xml filesystem element like this:
</p><pre class="screen">
&lt;filesystem provider="coaster" url="gt2://grid.myhost.org" /&gt;
</pre><p>
The url parameter should be a pseudo-URI formed with the URI scheme being
the name of the provider to use to submit the coaster head job, and the
hostname portion being the hostname to be used to execute the coaster
head job. Note that this provider and hostname will be used for execution
of a coaster head job, not for file transfer; so for example, a GRAM
endpoint should be specified here rather than a GridFTP endpoint.
</p><p>
Coasters are affected by the following profile settings, which are
documented in the <a class="link" href="profiles.php#profile.globus" title="11.3. Globus namespace">Globus namespace profile
section</a>:
</p><div class="table"><a name="id3434143"></a><p class="title"><b>Table 13. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">profile key</th><th align="left">brief description</th></tr></thead><tbody><tr><td align="left">slots</td><td align="left">How many maximum LRM jobs/worker blocks are allowed</td></tr><tr><td align="left">workersPerNode</td><td align="left">How many coaster workers to run per execution node</td></tr><tr><td align="left">nodeGranularity</td><td align="left">Each worker block uses a number of nodes that is a multiple of this number</td></tr><tr><td align="left">lowOverallocation</td><td align="left">How many times larger than the job walltime should a block's walltime be if all jobs are 1s long</td></tr><tr><td align="left">highOverallocation</td><td align="left">How many times larger than the job walltime should a block's walltime be if all jobs are infinitely long</td></tr><tr><td align="left">overallocationDecayFactor</td><td align="left">How quickly should the overallocation curve tend towards the highOverallocation as job walltimes get larger</td></tr><tr><td align="left">spread</td><td align="left">By how much should worker blocks vary in worker size</td></tr><tr><td align="left">workersPerNode</td><td align="left">How many coaster workers to run per execution node</td></tr><tr><td align="left">reserve</td><td align="left">How many seconds to reserve in a block's walltime for starting/shutdown operations</td></tr><tr><td align="left">maxnodes</td><td align="left">The maximum number of nodes allowed in a block</td></tr><tr><td align="left">maxtime</td><td align="left">The maximum number of walltime allowed for a block</td></tr><tr><td align="left">remoteMonitorEnabled</td><td align="left">If true, show a graphical display of the status of the coaster service</td></tr></tbody></table></div></div><br class="table-break"></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="clustering.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="localhowtos.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">17. Clustering </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 19. How-To Tips for Specific User Communities</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
