<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>15. Kickstart</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="buildoptions.php" title="14. Build options"><link rel="next" href="reliability.php" title="16. Reliability mechanisms"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">15. Kickstart</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="buildoptions.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="reliability.php">Next</a></td></tr></table><hr></div><div class="section" title="15. Kickstart"><div class="titlepage"><div><div><h2 class="title"><a name="kickstart"></a>15. Kickstart</h2></div></div></div><p>

Kickstart is a tool that can be used to gather various information 
about the remote execution environment for each job that Swift tries
to run.
		</p><p>
For each job, Kickstart generates an XML <em class="firstterm">invocation
record</em>. By default this record is staged back to the submit
host if the job fails.
		</p><p>
Before it can be used it must be installed on the remote site and
the sites file must be configured to point to kickstart.
		</p><p>
Kickstart can be downloaded as part of the Pegasus 'worker package' available
from the worker packages section of <a class="ulink" href="http://pegasus.isi.edu/code.php" target="_top">the Pegasus download page</a>.
		</p><p>
Untar the relevant worker package somewhere where it is visible to all of the
worker nodes on the remote execution machine (such as in a shared application
filesystem).
		</p><p>Now configure the gridlaunch attribute of the sites catalog
to point to that path, by adding a <em class="parameter"><code>gridlaunch</code></em>
attribute to the <code class="function">pool</code> element in the site
catalog:

</p><pre class="screen">

&lt;pool handle="example" gridlaunch="/usr/local/bin/kickstart" sysinfo="INTEL32::LINUX"&gt;
[...]
&lt;/pool&gt;

</pre><p>

		</p><p>
There are various kickstat.* properties, which have sensible default
values. These are documented in <a class="link" href="engineconfiguration.php" title="10. Swift configuration properties">the
properties section</a>.
		</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="buildoptions.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="reliability.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">14. Build options </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 16. Reliability mechanisms</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
