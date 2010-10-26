<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>12. The Site Catalog - sites.xml</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="profiles.php" title="11. Profiles"><link rel="next" href="transformationcatalog.php" title="13. The Transformation Catalog - tc.data"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">12. The Site Catalog - sites.xml</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="profiles.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="transformationcatalog.php">Next</a></td></tr></table><hr></div><div class="section" title="12. The Site Catalog - sites.xml"><div class="titlepage"><div><div><h2 class="title"><a name="sitecatalog"></a>12. The Site Catalog - sites.xml</h2></div></div></div><p>
The site catalog lists details of each site that Swift can use. The default
file contains one entry for local execution, and a large number of
commented-out example entries for other sites.
		</p><p>
By default, the site catalog is stored in <code class="filename">etc/sites.xml</code>.
This path can be overridden with the <code class="literal">sites.file</code> configuration property,
either in the Swift configuration file or on the command line.
		</p><p>
The sites file is formatted as XML. It consists of <code class="literal">&lt;pool&gt;</code> elements,
one for each site that Swift will use.
		</p><div class="section" title="12.1. Pool element"><div class="titlepage"><div><div><h3 class="title"><a name="id3432919"></a>12.1. Pool element</h3></div></div></div><p>
Each <code class="literal">pool</code> element must have a <code class="literal">handle</code> attribute, giving a symbolic name
for the site. This can be any name, but must correspond to entries for
that site in the transformation catalog.
		</p><p>
Optionally, the <code class="literal">gridlaunch</code> attribute can be used to specify the path to
<a class="link" href="kickstart.php" title="15. Kickstart">kickstart</a> on the site.
		</p><p>
Each <code class="literal">pool</code> must specify a file transfer method, an execution method
and a remote working directory. Optionally, <a class="link" href="profiles.php" title="11. Profiles">profile settings</a> can be specified.
		</p></div><div class="section" title="12.2. File transfer method"><div class="titlepage"><div><div><h3 class="title"><a name="id3432976"></a>12.2. File transfer method</h3></div></div></div><p>
Transfer methods are specified with either 
the <code class="literal">&lt;gridftp&gt;</code> element or the 
<code class="literal">&lt;filesystem&gt;</code> element.
		</p><p>
To use gridftp or local filesystem copy, use the <code class="literal">&lt;gridftp&gt;</code>
element:
</p><pre class="screen">
&lt;gridftp  url="gsiftp://evitable.ci.uchicago.edu" /&gt;
</pre><p>
The <code class="literal">url</code> attribute may specify a GridFTP server, using the gsiftp URI scheme;
or it may specify that filesystem copying will be used (which assumes that
the site has access to the same filesystem as the submitting machine) using
the URI <code class="literal">local://localhost</code>.
		</p><p>
Filesystem access using scp (the SSH copy protocol) can be specified using the
<code class="literal">&lt;filesystem&gt;</code> element:
</p><pre class="screen">
&lt;filesystem url="www11.i2u2.org" provider="ssh"/&gt;
</pre><p>
For additional ssh configuration information, see the ssh execution
provider documentation below.
		</p><p>
Filesystem access using <a class="link" href="coasters.php" title="18. Coasters">CoG coasters</a> can be
also be specified using the <code class="literal">&lt;filesystem&gt;</code> element. More detail about
configuring that can be found in the <a class="link" href="coasters.php" title="18. Coasters">CoG
coasters</a> section.
		</p></div><div class="section" title="12.3. Execution method"><div class="titlepage"><div><div><h3 class="title"><a name="id3433068"></a>12.3. Execution method</h3></div></div></div><p>
Execution methods may be specified either with the <code class="literal">&lt;jobmanager&gt;</code>
or <code class="literal">&lt;execution&gt;</code> element.
		</p><p>
The <code class="literal">&lt;jobmanager&gt;</code> element can be used to specify
execution through GRAM2. For example,
</p><pre class="screen">
    &lt;jobmanager universe="vanilla" url="evitable.ci.uchicago.edu/jobmanager-fork" major="2" /&gt;
</pre><p>
The <code class="literal">universe</code> attribute should always be set to vanilla. The
<code class="literal">url</code> attribute
should specify the name of the GRAM2 gatekeeper host, and the name of the
jobmanager to use. The major attribute should always be set to 2.
		</p><p>
The <code class="literal">&lt;execution&gt;</code> element can be used to specify
execution through other execution providers:
		</p><p>
To use GRAM4, specify the <code class="literal">gt4</code> provider. For example:
</p><pre class="screen">
&lt;execution provider="gt4" jobmanager="PBS" url="tg-grid.uc.teragrid.org" /&gt;
</pre><p>
The <code class="literal">url</code> attribute should specify the GRAM4 submission site.
The <code class="literal">jobmanager</code>
attribute should specify which GRAM4 jobmanager will be used.
		</p><p>
For local execution, the <code class="literal">local</code> provider should be used,
like this:
</p><pre class="screen">
&lt;execution provider="local" url="none" /&gt;
</pre><p>
		</p><p>
For PBS execution, the <code class="literal">pbs</code> provider should be used:
</p><pre class="screen">
&lt;execution provider="pbs" url="none" /&gt;
</pre><p>
The <code class="literal"><a class="link" href="profiles.php#profile.globus.queue">GLOBUS::queue</a></code> profile key
can be used to specify which PBS queue jobs will be submitted to.
		</p><p>
For execution through a local Condor installation, the <code class="literal">condor</code>
provider should be used. This provider can run jobs either in the default
vanilla universe, or can use Condor-G to run jobs on remote sites.
		</p><p>
When running locally, only the <code class="literal">&lt;execution&gt;</code> element
needs to be specified:
</p><pre class="screen">
&lt;execution provider="condor" url="none" /&gt;
</pre><p>
		</p><p>
When running with Condor-G, it is necessary to specify the Condor grid
universe and the contact string for the remote site. For example:
</p><pre class="screen">
 &lt;execution provider="condor" /&gt;
 &lt;profile namespace="globus" key="jobType"&gt;grid&lt;/profile&gt;
 &lt;profile namespace="globus" key="gridResource"&gt;gt2 belhaven-1.renci.org/jobmanager-fork&lt;/profile&gt;
</pre><p>
		</p><p>
For execution through SSH, the <code class="literal">ssh</code> provider should be used:
</p><pre class="screen">
&lt;execution url="www11.i2u2.org" provider="ssh"/&gt;
</pre><p>
with configuration made in <code class="filename">~/.ssh/auth.defaults</code> with
the string 'www11.i2u2.org' changed to the appropriate host name:
</p><pre class="screen">
www11.i2u2.org.type=key
www11.i2u2.org.username=hategan
www11.i2u2.org.key=/home/mike/.ssh/i2u2portal
www11.i2u2.org.passphrase=XXXX
</pre><p>
		</p><p>
For execution using the 
<a class="link" href="coasters.php" title="18. Coasters">CoG Coaster mechanism</a>, the <code class="literal">coaster</code> provider
should be used:
</p><pre class="screen">
&lt;execution provider="coaster" url="tg-grid.uc.teragrid.org"
    jobmanager="gt2:gt2:pbs" /&gt;
</pre><p>
More details about configuration of coasters can be found in the
<a class="link" href="coasters.php" title="18. Coasters">section on coasters</a>.
		</p></div><div class="section" title="12.4. Work directory"><div class="titlepage"><div><div><h3 class="title"><a name="id3433304"></a>12.4. Work directory</h3></div></div></div><p>
The <code class="literal">workdirectory</code> element specifies where on the site files can be
stored.
</p><pre class="screen">
&lt;workdirectory&gt;/home/benc&lt;/workdirectory&gt;
</pre><p>
This file must be accessible through the transfer mechanism specified
in the <code class="literal">&lt;gridftp&gt;</code> element and also mounted on all worker nodes that
will be used for execution. A shared cluster scratch filesystem is
appropriate for this. 
		</p></div><div class="section" title="12.5. Profiles"><div class="titlepage"><div><div><h3 class="title"><a name="id3433334"></a>12.5. Profiles</h3></div></div></div><p>
<a class="link" href="profiles.php" title="11. Profiles">Profile keys</a> can be specified using
the &lt;profile&gt; element. For example:
</p><pre class="screen">
&lt;profile namespace="globus" key="queue"&gt;fast&lt;/profile&gt;
</pre><p>
		</p></div><p>
The site catalog format is an evolution of the VDS site catalog format which
is documented
<a class="ulink" href="http://vds.uchicago.edu/vds/doc/userguide/html/H_SiteCatalog.html" target="_top">here</a>.
		</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="profiles.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="transformationcatalog.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">11. Profiles </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 13. The Transformation Catalog - tc.data</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
