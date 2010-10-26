<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>7. Ways in which Swift can be extended</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="techoverview.php" title="6. Technical overview of the Swift architecture"><link rel="next" href="functions.php" title="8. Function reference"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">7. Ways in which Swift can be extended</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="techoverview.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="functions.php">Next</a></td></tr></table><hr></div><div class="section" title="7. Ways in which Swift can be extended"><div class="titlepage"><div><div><h2 class="title"><a name="extending"></a>7. Ways in which Swift can be extended</h2></div></div></div><p>Swift is extensible in a number of ways. It is possible to add
mappers to accomodate different filesystem arrangements, site selectors
to change how Swift decides where to run each job, and job submission
interfaces to submit jobs through different mechanisms.
</p><p>A number of mappers are provided as part of the Swift release and
documented in the <a class="link" href="mappers.php" title="3. Mappers">mappers</a> section.
New mappers can be implemented
in Java by implementing the org.griphyn.vdl.mapping.Mapper interface. The
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/tutorial.php" target="_top">Swift
tutorial</a> contains a simple example of this.
</p><p>Swift provides a default site selector, the Adaptive Scheduler.
New site selectors can be plugged in by implementing the
org.globus.cog.karajan.scheduler.Scheduler interface and modifying
libexec/scheduler.xml and etc/karajan.properties to refer to the new
scheduler.
</p><p>Execution providers and filesystem providers, which allow to Swift
to execute jobs and to stage files in and out through mechanisms such
as GRAM and GridFTP can be implemented as Java CoG kit providers.
</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="techoverview.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="functions.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">6. Technical overview of the Swift architecture </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 8. Function reference</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
