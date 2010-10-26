<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>13. The Transformation Catalog - tc.data</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="sitecatalog.php" title="12. The Site Catalog - sites.xml"><link rel="next" href="buildoptions.php" title="14. Build options"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">13. The Transformation Catalog - tc.data</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="sitecatalog.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="buildoptions.php">Next</a></td></tr></table><hr></div><div class="section" title="13. The Transformation Catalog - tc.data"><div class="titlepage"><div><div><h2 class="title"><a name="transformationcatalog"></a>13. The Transformation Catalog - tc.data</h2></div></div></div><p>
The transformation catalog lists where application executables are located
on remote sites.
		</p><p>
By default, the site catalog is stored in <code class="filename">etc/tc.data</code>.
This path can be overridden with the <code class="literal">tc.file</code> configuration property,
either in the Swift configuration file or on the command line.
		</p><p>
The format is one line per executable per site, with fields separated by
tabs. Spaces cannot be used to separate fields.
		</p><p>Some example entries:
</p><pre class="screen">
localhost  echo    /bin/echo       INSTALLED       INTEL32::LINUX  null
TGUC       touch   /usr/bin/touch  INSTALLED       INTEL32::LINUX  GLOBUS::maxwalltime="0:1"
</pre><p>
		</p><p>
The fields are: site, transformation name, executable path, installation
status, platform, and profile entrys.
		</p><p>
The site field should correspond to a site name listed in the sites
catalog.</p><p>
The transformation name should correspond to the transformation name
used in a SwiftScript <code class="literal">app</code> procedure.
		</p><p>
The executable path should specify where the particular executable is
located on that site.
		</p><p>
The installation status and platform fields are not used. Set them to
<code class="literal">INSTALLED</code> and <code class="literal">INTEL32::LINUX</code> respectively.
		</p><p>
The profiles field should be set to <code class="literal">null</code> if no profile entries are to be
specified, or should contain the profile entries separated by semicolons.
		</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="sitecatalog.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="buildoptions.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">12. The Site Catalog - sites.xml </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 14. Build options</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
