<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>4. Commands</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="mappers.php" title="3. Mappers"><link rel="next" href="appmodel.php" title="5. Executing app procedures"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">4. Commands</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="mappers.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="appmodel.php">Next</a></td></tr></table><hr></div><div class="section" title="4. Commands"><div class="titlepage"><div><div><h2 class="title"><a name="commands"></a>4. Commands</h2></div></div></div><p>
The commands detailed in this section are available in the
<code class="filename">bin/</code> directory of a Swift installation and can
by run from the commandline if that directory is placed on the
PATH.
		</p><div class="section" title="4.1. swift"><div class="titlepage"><div><div><h3 class="title"><a name="swiftcommand"></a>4.1. swift</h3></div></div></div><p>
The <span class="command"><strong>swift</strong></span> command is the main command line tool
for executing SwiftScript programs.
	</p><div class="section" title="4.1.1. Command-line Syntax"><div class="titlepage"><div><div><h4 class="title"><a name="id3428976"></a>4.1.1. Command-line Syntax</h4></div></div></div><p>The <span class="command"><strong>swift</strong></span> command is invoked as follows:
<span class="command"><strong>swift [options] SwiftScript-program [SwiftScript-arguments]</strong></span>
with options taken from the following list, and SwiftScript-arguments
made available to the SwiftScript program through the
<a class="link" href="functions.php#function.arg" title="8.1. @arg">@arg</a> function.
</p><div class="variablelist" title="Swift command-line options"><p class="title"><b>Swift command-line options</b></p><dl><dt><span class="term">-help or -h</span></dt><dd><p>
      Display usage information </p></dd><dt><span class="term">-typecheck</span></dt><dd><p>
      Does a typecheck of a SwiftScript program, instead of executing it.</p></dd><dt><span class="term">-dryrun</span></dt><dd><p>
      Runs the SwiftScript program without submitting any jobs (can be used to get
      a graph)
    </p></dd><dt><span class="term">-monitor</span></dt><dd><p>
      Shows a graphical resource monitor 
    </p></dd><dt><span class="term">-resume <code class="literal">file</code></span></dt><dd><p>
      Resumes the execution using a log file 
    </p></dd><dt><span class="term">-config <code class="literal">file</code></span></dt><dd><p>
      Indicates the Swift configuration file to be used for this run. 
      Properties in this configuration file will override the default 
      properties. If individual command line arguments are used for 
      properties, they will override the contents of this file. 
    </p></dd><dt><span class="term">-verbose | -v</span></dt><dd><p>
      Increases the level of output that Swift produces on the console 
      to include more detail about the execution 
    </p></dd><dt><span class="term">-debug | -d</span></dt><dd><p>
      Increases the level of output that Swift produces on the console 
      to include lots of detail about the execution 
    </p></dd><dt><span class="term">-logfile <code class="literal">file</code></span></dt><dd><p>
      Specifies a file where log messages should go to. By default 
      Swift uses the name of the program being run and a numeric index
      (e.g. myworkflow.1.log) 
    </p></dd><dt><span class="term">-runid <code class="literal">identifier</code></span></dt><dd><p>
      Specifies the run identifier. This must be unique for every invocation
      and is used in several places to keep files from different executions
      cleanly separated. By default, a datestamp and random number are used
      to generate a run identifier. When using this parameter, care should be
      taken to ensure that the run ID remains unique with respect to all
      other run IDs that might be used, irrespective of (at least) expected
      execution sites, program or user.
    </p></dd><dt><span class="term">-tui</span></dt><dd>
      Displays an interactive text mode monitor during a run. (since Swift 0.9)
    </dd></dl></div><p>In addition, the following Swift properties can be set on the
command line:

</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">caching.algorithm</li><li class="listitem">clustering.enabled</li><li class="listitem">clustering.min.time</li><li class="listitem">clustering.queue.delay</li><li class="listitem">ip.address</li><li class="listitem">kickstart.always.transfer</li><li class="listitem">kickstart.enabled</li><li class="listitem">lazy.errors</li><li class="listitem">pgraph</li><li class="listitem">pgraph.graph.options</li><li class="listitem">pgraph.node.options</li><li class="listitem">sitedir.keep</li><li class="listitem">sites.file</li><li class="listitem">tc.file</li><li class="listitem">tcp.port.range</li></ul></div><p>
</p></div><div class="section" title="4.1.2. Return codes"><div class="titlepage"><div><div><h4 class="title"><a name="id3429212"></a>4.1.2. Return codes</h4></div></div></div><p>
The <span class="command"><strong>swift</strong></span> command may exit with the following return codes:
</p><div class="table"><a name="id3429225"></a><p class="title"><b>Table 12. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">value</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">0</td><td align="left">success</td></tr><tr><td align="left">1</td><td align="left">command line syntax error or missing project name</td></tr><tr><td align="left">2</td><td align="left">error during execution</td></tr><tr><td align="left">3</td><td align="left">error during compilation</td></tr><tr><td align="left">4</td><td align="left">input file does not exist</td></tr></tbody></table></div></div><p><br class="table-break">
	</p></div><div class="section" title="4.1.3. Environment variables"><div class="titlepage"><div><div><h4 class="title"><a name="id3429295"></a>4.1.3. Environment variables</h4></div></div></div><p>The <span class="command"><strong>swift</strong></span> is influenced by the
following environment variables:
		</p><p>
<code class="literal">GLOBUS_HOSTNAME</code>, <code class="literal">GLOBUS_TCP_PORT_RANGE</code> - set in the environment before running
Swift. These can be set to inform Swift of the
configuration of your local firewall. More information can be found in
<a class="ulink" href="http://dev.globus.org/wiki/FirewallHowTo" target="_top">the Globus firewall
How-to</a>.
		</p><p>
<code class="literal">COG_OPTS</code> - set in the environment before running Swift. Options set in this
variable will be passed as parameters to the Java Virtual Machine which
will run Swift. The parameters vary between virtual machine imlementations,
but can usually be used to alter settings such as maximum heap size.
Typing 'java -help' will sometimes give a list of commands. The Sun Java
1.4.2 command line options are <a class="ulink" href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/java.html" target="_top">documented here</a>.
		</p></div></div><div class="section" title="4.2. swift-osg-ress-site-catalog"><div class="titlepage"><div><div><h3 class="title"><a name="id3429352"></a>4.2. swift-osg-ress-site-catalog</h3></div></div></div><p>
The <span class="command"><strong>swift-osg-ress-site-catalog</strong></span> command generates a site
catalog based on <a class="ulink" href="http://www.opensciencegrid.org/" target="_top">OSG</a>'s
ReSS information system (since Swift 0.9)
			</p><p>
Usage: <span class="command"><strong>swift-osg-ress-site-catalog [options]</strong></span>
			</p><div class="variablelist"><dl><dt><span class="term">--help</span></dt><dd><p>Show help message</p></dd><dt><span class="term">--vo=[name]</span></dt><dd><p>Set what VO to query ReSS for</p></dd><dt><span class="term">--engage-verified</span></dt><dd><p>Only retrieve sites verified by the Engagement VO site
verification tests This can not be used together with <code class="literal">--vo</code>,
as the query will only work for sites advertising support for the
Engagement VO.</p><p>This option means information will be retrieved from the
Engagement collector instead of the top-level ReSS collector.</p></dd><dt><span class="term">--out=[filename]</span></dt><dd><p>Write to [filename] instead of stdout</p></dd><dt><span class="term">--condor-g</span></dt><dd><p>Generates sites files which will submit jobs using a local Condor-G
installation rather than through direct GRAM2 submission. (since Swift 0.10)</p></dd></dl></div></div><div class="section" title="4.3. swift-plot-log"><div class="titlepage"><div><div><h3 class="title"><a name="id3429461"></a>4.3. swift-plot-log</h3></div></div></div><p>
<span class="command"><strong>swift-plot-log</strong></span> generates summaries of Swift run log
files.
		</p><p>
Usage: <span class="command"><strong>swift-plot-log [logfile] [targets]</strong></span>
		</p><p>
When no targets are specified, <span class="command"><strong>swift-plog-log</strong></span> will
generate an HTML report for the run. When targets are specified, only
those named targets will be generated.
		</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="mappers.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="appmodel.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">3. Mappers </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 5. Executing <code class="literal">app</code> procedures</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
