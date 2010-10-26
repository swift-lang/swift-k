<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>5. The swift command</title><meta name="generator" content="DocBook XSL Stylesheets V1.71.1"><link rel="start" href="index.html" title="Swift User Guide"><link rel="up" href="index.html" title="Swift User Guide"><link rel="prev" href="engineconfiguration.html" title="4. Swift configuration properties"><link rel="next" href="kickstart.html" title="6. Kickstart"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="section" lang="en"><div class="titlepage"><div><div><h2 class="title"><a name="swiftcommand"></a>5. The swift command</h2></div></div></div><p>
The <span><strong class="command">swift</strong></span> command is the main command line tool
for executing SwiftScript programs.
	</p><div class="section" lang="en"><div class="titlepage"><div><div><h3 class="title"><a name="id2875220"></a>5.1. Command-line Syntax</h3></div></div></div><p>The <span><strong class="command">swift</strong></span> command is invoked as follows:
<span><strong class="command">swift [options] SwiftScript-program [SwiftScript-arguments]</strong></span>
with options taken from the following list, and SwiftScript-arguments
made available to the SwiftScript program through the
<a href="functions.html#function.arg" title="11.1. @arg">@arg</a> function.
</p><div class="variablelist"><p class="title"><b>Swift command-line options</b></p><dl><dt><span class="term">-help or -h</span></dt><dd><p>
      Display usage information </p></dd><dt><span class="term">-typecheck</span></dt><dd><p>
      Does a typecheck instead of executing the workflow </p></dd><dt><span class="term">-dryrun</span></dt><dd><p>
      Runs the workflow without submitting any jobs (can be used to get
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
      Swift uses the name of the workflow being run and a numeric index
      (e.g. myworkflow.1.log) 
    </p></dd><dt><span class="term">-runid <code class="literal">identifier</code></span></dt><dd><p>
Specifies the run identifier. This must be unique for every invocation of a workflow and is used in several places to keep files from different executions cleanly separated. By default, a datestamp and random number are used to generate a run identifier. When using this parameter, care should be taken to ensure
that the run ID remains unique with respect to all other run IDs that might
be used, irrespective of (at least) expected run location, workflow or user.
    </p></dd><dt><span class="term">-tcp.port.range <code class="literal">start,end</code></span></dt><dd><p>
      A TCP port range can be specified to restrict the ports on which 
      GRAM callback services are started. This is likely needed if your
      submit host is behind a firewall, in which case the firewall 
      should be configured to allow incoming connections on ports in 
      the range. 
    </p></dd></dl></div><p>In addition, the following Swift properties can be set on the
command line:

</p><div class="itemizedlist"><ul type="disc"><li>caching.algorithm</li><li>clustering.enabled</li><li>clustering.enabled</li><li>clustering.min.time</li><li>clustering.queue.delay</li><li>ip.address</li><li>kickstart.always.transfer</li><li>kickstart.enabled</li><li>lazy.errors</li><li>pgraph</li><li>pgraph.graph.options</li><li>pgraph.node.options</li><li>sitedir.keep</li><li>sites.file</li><li>tc.file</li></ul></div><p>
</p></div><div class="section" lang="en"><div class="titlepage"><div><div><h3 class="title"><a name="id2875483"></a>5.2. Return codes</h3></div></div></div><p>
The swift command may exit with the following return codes:
</p><div class="itemizedlist"><ul type="disc"><li>0 - success</li><li>1 - command line syntax error or missing project name</li><li>2 - error during workflow execution</li><li>3 - error during compilation of SwiftScript program</li><li>4 - input file does not exist</li></ul></div><p>
	</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		</body></html>
