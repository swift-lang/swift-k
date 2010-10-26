<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>17. Clustering</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="reliability.php" title="16. Reliability mechanisms"><link rel="next" href="coasters.php" title="18. Coasters"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">17. Clustering</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="reliability.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="coasters.php">Next</a></td></tr></table><hr></div><div class="section" title="17. Clustering"><div class="titlepage"><div><div><h2 class="title"><a name="clustering"></a>17. Clustering</h2></div></div></div><p>
Swift can group a number of short job submissions into a single larger
job submission to minimize overhead involved in launching jobs (for example,
caused by security negotiation and queuing delay). In general,
<a class="link" href="coasters.php" title="18. Coasters">CoG coasters</a> should be used in preference
to the clustering mechanism documented in this section.
		</p><p>
By default, clustering is disabled. It can be activated by setting the
<a class="link" href="engineconfiguration.php#property.clustering.enabled">clustering.enabled</a>
property to true.
		</p><p>
A job is eligible for clustering if
the <a class="link" href="profiles.php#profile.globus.maxwalltime"><span class="property">GLOBUS::maxwalltime</span></a> profile is specified in the <code class="filename">tc.data</code> entry for that job, and its value is
less than the value of the
<a class="link" href="engineconfiguration.php#property.clustering.min.time"><span class="property">clustering.min.time</span></a>
property.
		</p><p>
Two or more jobs are considered compatible if they share the same site
and do not have conflicting profiles (e.g. different values for the same
environment variable). 
		</p><p>
When a submitted job is eligible for clustering, 
it will be put in a clustering queue rather than being submitted to
a remote site. The clustering queue is processed at intervals 
specified by the
<a class="link" href="engineconfiguration.php#property.clustering.queue.delay"><span class="property">clustering.queue.delay</span></a>
property. The processing of the clustering queue consists of selecting
compatible jobs and grouping them into clusters whose maximum wall time does
not exceed twice the value of the <span class="property">clustering.min.time</span>
property.
		</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="reliability.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="coasters.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">16. Reliability mechanisms </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 18. Coasters</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
