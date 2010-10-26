<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Swift Quick Start Guide</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><meta name="description" content="This guide describes the steps needed to download, install, configure, and run the basic examples for Swift. If you are using a pre-installed version of Swift, you can skip directly to the configuration section."><link rel="home" href="index.html" title="Swift Quick Start Guide"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="Swift Quick Start Guide"><div class="titlepage"><div><div><h2 class="title"><a name="id2601088"></a>Swift Quick start Guide</h2></div><div><h3 class="subtitle"><i>Source Control $LastChangedRevision: 2810 $</i></h3></div><div><div class="abstract" title="Abstract"><p class="title"><b>Abstract</b></p><p><b> </b>
				
				This guide describes the steps needed to download, install,
				configure, and run the basic examples for Swift. If you are
				using a pre-installed version of Swift, you can skip 
				directly to the <a class="link" href="#configure" title="3. Configuring Swift">configuration
				section</a>.

				</p></div></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="sect1"><a href="#download">1. Downloading a Swift Distribution</a></span></dt><dd><dl><dt><span class="sect2"><a href="#dl-stable">1.1. Stable Releases</a></span></dt><dt><span class="sect2"><a href="#dl-nightly">1.2. Nightly Builds</a></span></dt><dt><span class="sect2"><a href="#dl-repository">1.3. Source Repository</a></span></dt></dl></dd><dt><span class="sect1"><a href="#install">2. Installing a Swift Binary Package</a></span></dt><dt><span class="sect1"><a href="#configure">3. Configuring Swift</a></span></dt><dd><dl><dt><span class="sect2"><a href="#security">3.1. Grid Security</a></span></dt><dt><span class="sect2"><a href="#id2668393">3.2. Swift Properties</a></span></dt></dl></dd><dt><span class="sect1"><a href="#examples">4. Running Swift Examples</a></span></dt></dl></div><div class="sect1" title="1. Downloading a Swift Distribution"><div class="titlepage"><div><div><h2 class="title"><a name="download"></a>1. Downloading a Swift Distribution</h2></div></div></div><p>
		
			There are three main ways of getting the Swift implementation: <a class="link" href="#dl-stable" title="1.1. Stable Releases">stable releases</a>, <a class="link" href="#dl-nightly" title="1.2. Nightly Builds">nightly builds</a>, and the <a class="link" href="#dl-repository" title="1.3. Source Repository">source code repository</a>. 

		</p><div class="sect2" title="1.1. Stable Releases"><div class="titlepage"><div><div><h3 class="title"><a name="dl-stable"></a>1.1. Stable Releases</h3></div></div></div><p>
			
				Stable releases can be obtained from the Swift download page:
				<a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/index.php#stable" target="_top">Swift
				Downloads Page</a>. Once you downloaded the package, please
				move to the <a class="link" href="#install" title="2. Installing a Swift Binary Package">install section</a>.

			</p></div><div class="sect2" title="1.2. Nightly Builds"><div class="titlepage"><div><div><h3 class="title"><a name="dl-nightly"></a>1.2. Nightly Builds</h3></div></div></div><p>
			
				Swift builds and tests are being run every day. The <a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/index.php#nightly" target="_top">Swift
				downloads page</a> contains links to the latest build and
				test page. The nightly builds reflect a development version of
				the Swift code and should not be used in production mode. After
				downloading a nightly build package, please continue to the
				<a class="link" href="#install" title="2. Installing a Swift Binary Package">install section</a>.
			
			</p></div><div class="sect2" title="1.3. Source Repository"><div class="titlepage"><div><div><h3 class="title"><a name="dl-repository"></a>1.3. Source Repository</h3></div></div></div><p>
			
				Details about accessing the Swift source repository together with
				build instructions are available on the <a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/index.php#nightly" target="_top">Swift
				downloads page</a>. Once built, the <code class="filename">dist/swift-svn</code> directory
				will contain a self-contained build which can be used in place or moved to a different location.
				You should then proceed to the <a class="link" href="#configure" title="3. Configuring Swift">configuration section</a>.
			
			</p></div></div><div class="sect1" title="2. Installing a Swift Binary Package"><div class="titlepage"><div><div><h2 class="title"><a name="install"></a>2. Installing a Swift Binary Package</h2></div></div></div><p>
		
			Simply unpack the downloaded package (<code class="filename">swift-&lt;version&gt;.tar.gz</code>) into a
			directory of your choice:
			
</p><pre class="screen">
<code class="prompt">&gt;</code> <span class="command"><strong>tar</strong></span> <code class="option">-xzvf</code> <code class="filename">swift-&lt;version&gt;.tar.gz</code>
</pre><p>
			
			This will create a <code class="filename">swift-&lt;version&gt;</code> directory
			containing the build.
		
		</p></div><div class="sect1" title="3. Configuring Swift"><div class="titlepage"><div><div><h2 class="title"><a name="configure"></a>3. Configuring Swift</h2></div></div></div><p>
		
			This section describes configuration steps that need to be taken in
			order to get Swift running. Since all command line tools provided
			with Swift can be found in the <code class="filename">bin/</code> directory of the Swift distribution, it may
			be a good idea to add this directory to your <code class="envar">PATH</code>
			environment variable:
			
</p><pre class="screen">
<code class="prompt">&gt;</code> <span class="command"><strong>export</strong></span> <code class="envar">PATH</code>=<code class="filename">/path/to/swift/bin</code>:<code class="envar">$PATH</code>
</pre><p>
			
		</p><div class="sect2" title="3.1. Grid Security"><div class="titlepage"><div><div><h3 class="title"><a name="security"></a>3.1. Grid Security</h3></div></div></div><p>For local execution of jobs, no grid security configuration
				is necessary.
			</p><p>However, when submitting jobs to a remote machine using Globus
				Toolkit services, Swift makes use of the
				<a class="ulink" href="http://www.globus.org/toolkit/docs/4.0/security/key-index.html" target="_top">
				Grid Security Infrastructure (GSI)</a> for authentication
				and authorization. The requirements for this are detailed in
				the following sections. Note that GSI is not required to be
				configured for local execution (which will usually be the
				case when first starting with Swift).
			</p><div class="sect3" title="3.1.1. User Certificate"><div class="titlepage"><div><div><h4 class="title"><a name="certs"></a>3.1.1. User Certificate</h4></div></div></div><p>
			
				GSI requires a certificate/private key
				pair for authentication to 
				<a class="ulink" href="http://www.globus.org/toolkit" target="_top">Globus Toolkit</a>
				services. The certificate and private key should
				be placed into the <code class="filename">~/.globus/usercert.pem</code> and <code class="filename">~/.globus/userkey.pem</code> files,
				respectively.
			
			</p></div><div class="sect3" title="3.1.2. Certificate Authorities Root Certificates"><div class="titlepage"><div><div><h4 class="title"><a name="cas"></a>3.1.2. Certificate Authorities Root Certificates</h4></div></div></div><p>
			
				The Swift client libraries are generally required to authenticate
				the services to which they connect. This process requires the
				presence on the Swift submit site of the root certificates used
				to sign the host certificates of services used. These root
				certificates need to be installed in either (or both) the
				<code class="filename">~/.globus/certificates</code>
				and <code class="filename">/etc/grid-security/certificates</code>
				directories. A package with the root certificates of the
				certificate authorities used in the <a class="ulink" href="http://www.teragrid.org" target="_top">TeraGrid</a> can be found
				<a class="ulink" href="http://security.teragrid.org/TG-CAs.html" target="_top">here</a>.
			
			</p></div></div><div class="sect2" title="3.2. Swift Properties"><div class="titlepage"><div><div><h3 class="title"><a name="id2668393"></a>3.2. Swift Properties</h3></div></div></div><p>
			
				A Swift properties file (named <code class="filename">swift.properties</code>) can be used to
				customize certain configuration aspects of Swift. A shared
				version of this file, <code class="filename">etc/swift.properties</code>
				in the installation directory
				can be used to provide installation-wide defaults. A per-user
				properties file, <code class="filename">~/.swift/swift.properties</code> can be used for
				user specific settings. Swift first loads the shared
				configuration file and, if present, the user configuration file.
				Any properties not explicitly set in the user configuration file
				will be inherited from the shared configuration file. Properties
				are specified in the following format:

</p><pre class="screen">
<span class="property">name</span>=<em class="parameter"><code>value</code></em>
</pre><p>

				For details about the various properties Swift accepts, please
				take a look at the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#properties" target="_top">Swift
				Properties Section</a> in the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php" target="_top">Swift
				User Guide</a>.

			</p></div></div><div class="sect1" title="4. Running Swift Examples"><div class="titlepage"><div><div><h2 class="title"><a name="examples"></a>4. Running Swift Examples</h2></div></div></div><p>
		
			The Swift examples can be found in the <code class="filename">examples</code> directory in the Swift distribution.
			The examples are written in the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide/language.php" target="_top">SwiftScript
			language</a>, and have <code class="filename">.swift</code> as
			a file extension. 

		</p><p>
		
			The Grid Security Infrastructure, which Swift uses, works with
			limited time certificates called proxies. These proxies can be
			generated from your user certificate and private key using one of
			<span class="command"><strong>grid-proxy-init</strong></span> or
			<span class="command"><strong>cog-proxy-init</strong></span> (the latter being a Java Swing
			interface to the former).
		
		</p><p>
		
			Execution of a Swift workflow is done using the
			<span class="command"><strong>swift</strong></span> command, which takes the Swift
			workflow file name as an argument:
			
</p><pre class="screen">
<code class="prompt">&gt;</code> <span class="command"><strong>cd examples/swift</strong></span>
<code class="prompt">&gt;</code> <span class="command"><strong>swift</strong></span> <code class="option"><code class="filename">first.swift</code></code>
</pre><p>

			The <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#swiftcommand" target="_top">Swift
			Command Options Section</a> in the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php" target="_top">Swift 			
			User Guide</a> contains details about the various options of the
			<span class="command"><strong>swift</strong></span>.
		
		</p></div></div>
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

<!--
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
-->
