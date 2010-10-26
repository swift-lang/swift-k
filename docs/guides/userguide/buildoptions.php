<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>14. Build options</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="transformationcatalog.php" title="13. The Transformation Catalog - tc.data"><link rel="next" href="kickstart.php" title="15. Kickstart"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">14. Build options</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="transformationcatalog.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="kickstart.php">Next</a></td></tr></table><hr></div><div class="section" title="14. Build options"><div class="titlepage"><div><div><h2 class="title"><a name="buildoptions"></a>14. Build options</h2></div></div></div><p>
See <a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/" target="_top">the
Swift download page</a> for instructions on downloading and
building Swift from source. When building, various build options can
be supplied on the ant commandline. These are summarised here:
		</p><p>
<code class="literal">with-provider-condor</code> - build with CoG condor provider
		</p><p>
<code class="literal">with-provider-coaster</code> - build with CoG coaster provider (see
<a class="link" href="coasters.php" title="18. Coasters">the section on coasters</a>). Since 0.8,
coasters are always built, and this option has no effect.
		</p><p>
<code class="literal">with-provider-deef</code> - build with Falkon provider deef. In order for this
option to work, it is necessary to check out the provider-deef code in
the cog/modules directory alongside swift:

			</p><pre class="screen">
$ <strong class="userinput"><code>cd cog/modules</code></strong>
$ <strong class="userinput"><code>svn co https://svn.ci.uchicago.edu/svn/vdl2/provider-deef</code></strong>
$ <strong class="userinput"><code>cd ../swift</code></strong>
$ <strong class="userinput"><code>ant -Dwith-provider-deef=true redist</code></strong>
			</pre><p>

		</p><p>
<code class="literal">with-provider-wonky</code> - build with provider-wonky, an execution provider
that provides delays and unreliability for the purposes of testing Swift's
fault tolerance mechanisms. In order for this option to work, it is
necessary to check out the provider-wonky code in the <code class="filename">cog/modules</code>
directory alongside swift:

			</p><pre class="screen">
$ <strong class="userinput"><code>cd cog/modules</code></strong>
$ <strong class="userinput"><code>svn co https://svn.ci.uchicago.edu/svn/vdl2/provider-wonky</code></strong>
$ <strong class="userinput"><code>cd ../swift</code></strong>
$ <strong class="userinput"><code>ant -Dwith-provider-wonky=true redist</code></strong>
			</pre><p>
		</p><p>
<code class="literal">no-supporting</code> - produces a distribution without supporting commands such
as <span class="command"><strong>grid-proxy-init</strong></span>. This is intended for when the Swift distribution will be
used in an environment where those commands are already provided by other
packages, where the Swift package should be providing only Swift
commands, and where the presence of commands such as grid-proxy-init from
the Swift distribution in the path will mask the presence of those
commands from their true distribution package such as a Globus Toolkit
package.
</p><pre class="screen">
$ <strong class="userinput"><code>ant -Dno-supporting=true redist</code></strong>
</pre><p>
		</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="transformationcatalog.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="kickstart.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">13. The Transformation Catalog - tc.data </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 15. Kickstart</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
