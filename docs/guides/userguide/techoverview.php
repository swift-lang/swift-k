<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>6. Technical overview of the Swift architecture</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="appmodel.php" title="5. Executing app procedures"><link rel="next" href="extending.php" title="7. Ways in which Swift can be extended"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">6. Technical overview of the Swift architecture</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="appmodel.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="extending.php">Next</a></td></tr></table><hr></div><div class="section" title="6. Technical overview of the Swift architecture"><div class="titlepage"><div><div><h2 class="title"><a name="techoverview"></a>6. Technical overview of the Swift architecture</h2></div></div></div><p>
This section attempts to provide a technical overview of the Swift
architecture.
	</p><div class="section" title="6.1. karajan - the core execution engine"><div class="titlepage"><div><div><h3 class="title"><a name="id3429988"></a>6.1. karajan - the core execution engine</h3></div></div></div></div><div class="section" title="6.2. Execution layer"><div class="titlepage"><div><div><h3 class="title"><a name="id3429994"></a>6.2. Execution layer</h3></div></div></div><p>
The execution layer causes an application program (in the form of a unix
executable) to be executed either locally or remotely.
	</p><p>
The two main choices are local unix execution and execution through GRAM.
Other options are available, and user provided code can also be plugged in.
	</p><p>
The <a class="link" href="kickstart.php" title="15. Kickstart">kickstart</a> utility can
be used to capture environmental information at execution time
to aid in debugging and provenance capture. 
	</p></div><div class="section" title="6.3. SwiftScript language compilation layer"><div class="titlepage"><div><div><h3 class="title"><a name="id3430020"></a>6.3. SwiftScript language compilation layer</h3></div></div></div><p>
Step i: text to XML intermediate form parser/processor. parser written in
ANTLR - see resources/VDL.g. The XML Schema Definition (XSD) for the
intermediate language is in resources/XDTM.xsd.
	</p><p>
Step ii: XML intermediate form to Karajan workflow. Karajan.java - reads
the XML intermediate form. compiles to karajan workflow language - for
example, expressions are converted from SwiftScript syntax into Karajan
syntax, and function invocations become karajan function invocations
with various modifications to parameters to accomodate return parameters
and dataset handling.
	</p></div><div class="section" title="6.4. Swift/karajan library layer"><div class="titlepage"><div><div><h3 class="title"><a name="id3430037"></a>6.4. Swift/karajan library layer</h3></div></div></div><p>
Some Swift functionality is provided in the form of Karajan libraries
that are used at runtime by the Karajan workflows that the Swift
compiler generates.
	</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="appmodel.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="extending.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">5. Executing <code class="literal">app</code> procedures </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 7. Ways in which Swift can be extended</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
