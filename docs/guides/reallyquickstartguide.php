<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Swift Really Quick Start Guide</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><meta name="description" content="This guide is a compressed version of the Swift Quick Start Guide."><link rel="home" href="index.html" title="Swift Really Quick Start Guide"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
		<!-- entire page container -->
		<div id="container">
			<!-- header -->
			<div id="header">
				<?php require('/disks/space0/projects/swift/inc/header.php') ?>
				<?php #require('/ci/www/projects/swift/inc/header.php') ?>
			</div>
			<!-- end header -->
			<!-- nav -->
			<div id="nav">
				<?php require('/disks/space0/projects/swift/inc/nav.php') ?>
				<?php #require('/ci/www/projects/swift/inc/nav.php') ?>
			</div>
			<!-- end nav -->
			<!-- content container -->
			<div id="content">
		
		<div class="article" title="Swift Really Quick Start Guide"><div class="titlepage"><div><div><h2 class="title"><a name="id2725197"></a>Swift Really Quick Start Guide</h2></div><div><h3 class="subtitle"><i>Source control $LastChangedRevision: 2646 $</i></h3></div><div><div class="abstract" title="Abstract"><p class="title"><b>Abstract</b></p><p><b> </b>
				
					This guide is a compressed version of the <a class="ulink" href="quickstartguide.php" target="_top">Swift Quick Start
					Guide</a>.
				
				</p></div></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="sect1"><a href="#reallyquickstart">1. Swift Really Quick Start Guide</a></span></dt></dl></div><div class="sect1" title="1. Swift Really Quick Start Guide"><div class="titlepage"><div><div><h2 class="title"><a name="reallyquickstart"></a>1. Swift Really Quick Start Guide</h2></div></div></div><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
				
					<a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/index.php" target="_top">Download</a>
					Swift.
				
				</p></li><li class="listitem"><p>
				
					Unpack it and add the <code class="filename">swift-xyz/bin</code> directory to your
					<code class="envar">PATH</code>.
				
				</p></li><li class="listitem"><p>
				
					Make sure you have your user certificate, a valid GSI proxy
					certificate, and the proper CA root certificates in either
					<code class="filename">~/.globus/certificates</code> or
					<code class="filename">/etc/grid-security/certificates</code>.
				
				</p></li><li class="listitem"><p>
				
					Edit <code class="filename">swift-xyz/etc/swift.properties</code>. You
					should add your numeric IP address there
					(<span class="property">ip.address</span>=<code class="literal">x.y.z.w</code>).
				
				</p></li><li class="listitem"><p>
				
					Use the example site catalog and transformation catalog (they 
					are configured for local submission):
					
</p><pre class="screen">
<span class="command"><strong>cd</strong></span> swift-xyz/etc
<span class="command"><strong>cp</strong></span> sites.xml.example sites.xml
<span class="command"><strong>cp</strong></span> tc.data.example tc.data
</pre><p>
				
				</p></li><li class="listitem"><p>
				
					Use <span class="command"><strong>swift file.dtm</strong></span> to compile and execute
					<code class="filename">file.dtm</code>.
				
				</p></li><li class="listitem"><p>
				
					Use <span class="command"><strong>swift -resume file-&lt;runid&gt;.?.rlog
					file.dtm</strong></span> to resume a failed run.
				
				</p></li></ul></div></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer">
				<?php require('/disks/space0/projects/swift/inc/footer.php') ?>
				<?php #require('/ci/www/projects/swift/inc/footer.php') ?>
			</div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
