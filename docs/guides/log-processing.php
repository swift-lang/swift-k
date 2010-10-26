<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Swift log processing tools</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.html" title="Swift log processing tools"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="Swift log processing tools"><div class="titlepage"><div><div><h2 class="title"><a name="id2976759"></a>Swift log processing tools</h2></div><div><h3 class="subtitle"><i>Source control $LastChangedRevision$</i></h3></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="section"><a href="#id3045733">1. Overview</a></span></dt><dt><span class="section"><a href="#id3045744">2. Prerequisites</a></span></dt><dt><span class="section"><a href="#id3045754">3. Web page about a run</a></span></dt><dt><span class="section"><a href="#id3045770">4. CEDPS logs</a></span></dt><dt><span class="section"><a href="#id3045786">5. Event/transition channels</a></span></dt><dt><span class="section"><a href="#id3043697">6. Internal file formats</a></span></dt><dt><span class="section"><a href="#id3043735">7. hacky scripts</a></span></dt></dl></div><div class="section" title="1. Overview"><div class="titlepage"><div><div><h2 class="title"><a name="id3045733"></a>1. Overview</h2></div></div></div><p>
There is a package of Swift log processing utilties. 
		</p></div><div class="section" title="2. Prerequisites"><div class="titlepage"><div><div><h2 class="title"><a name="id3045744"></a>2. Prerequisites</h2></div></div></div><p>
gnuplot 4.0, gnu m4, gnu textutils, perl
</p></div><div class="section" title="3. Web page about a run"><div class="titlepage"><div><div><h2 class="title"><a name="id3045754"></a>3. Web page about a run</h2></div></div></div><p>
			</p><pre class="screen">
swift-plot-log /path/to/readData-20080304-0903-xgqf5nhe.log 
			</pre><p>
This will create a web page, report-readData-20080304-0903-xgqf5nhe
If the above command is used before a run is completed, the web page will
report information about the workflow progress so far.
		</p></div><div class="section" title="4. CEDPS logs"><div class="titlepage"><div><div><h2 class="title"><a name="id3045770"></a>4. CEDPS logs</h2></div></div></div><p>
The log processing tools can output transition streams in
CEDPS logging format:
			</p><pre class="screen">
swift-plot-log /path/to/readData-20080304-0903-xgqf5nhe.log execute.cedps
			</pre><p>
		</p></div><div class="section" title="5. Event/transition channels"><div class="titlepage"><div><div><h2 class="title"><a name="id3045786"></a>5. Event/transition channels</h2></div></div></div><p>
Various event channels are extracted from the log files and made available
as <code class="filename">.event</code> and <code class="filename">.transition</code> files.
These roughly correspond to processes within the Swift runtime environment.
		</p><p>These streams are then used to provide the data for the various
output formats, such as graphs, web pages and CEDPS log format.</p><p>The available streams are:

</p><div class="table"><a name="id3045814"></a><p class="title"><b>Table 1. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th>Stream name</th><th>Description</th></tr></thead><tbody><tr><td>execute</td><td>Swift procedure invocations</td></tr><tr><td>execute2</td><td>individual execution attempts</td></tr><tr><td>kickstart</td><td>kickstart records (not available as transitions)</td></tr><tr><td>karatasks</td><td> karajan level tasks, available as transitions (there are also four substreams karatasks.FILE_OPERATION,  karatasks.FILE_TRANSFER and karatasks.JOB_SUBMISSION available as events but not transitions)</td></tr><tr><td>workflow</td><td>a single event representing the entire workflow</td></tr><tr><td>dostagein</td><td>stage-in operations for execute2s</td></tr><tr><td>dostageout</td><td>stage-out operations for execute2s</td></tr></tbody></table></div></div><p><br class="table-break">

</p><p>
Streams are generated from their source log files either as .transitions
or .event files, for example by <code class="literal">swift-plot-log whatever.log foo.event</code>.
</p><p>
Various plots are available based on different streams:

</p><div class="table"><a name="id3044467"></a><p class="title"><b>Table 2. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th>Makefile target</th><th>Description</th></tr></thead><tbody><tr><td>foo.png</td><td>Plots the foo event stream</td></tr><tr><td>foo-total.png</td><td>Plots how many foo events are in progress at any time</td></tr><tr><td>foo.sorted-start.png</td><td>Plot like foo.png but ordered by start time</td></tr></tbody></table></div></div><p><br class="table-break">

</p><p>
Text-based statistics are also available with <code class="literal">make foo.stats</code>.
</p><p>
Event streams are nested something like this:

</p><pre class="screen">
workflow
  execute
    execute2
      dostagein
        karatasks (fileops and filetrans)
      clustering (optional)
        karatasks (execution)
          cluster-log (optional)
            wrapper log (optional)
              kickstart log
      dostageout
        karatasks (fileops and filetrans)
</pre><p>

</p></div><div class="section" title="6. Internal file formats"><div class="titlepage"><div><div><h2 class="title"><a name="id3043697"></a>6. Internal file formats</h2></div></div></div><p>The log processing code generates a number of internal files that
follow a standard format. These are used for communication between the
modules that parse various log files to extract relevant information; and
the modules that generate plots and other summary information.</p><pre class="screen">
need an event file format of one event per line, with that line
containing start time and duration and other useful data.

col1 = start, col2 = duration, col3 onwards = event specific data - for
some utilities for now should be column based, but later will maybe
move to attribute based.

between col 1 and col 2 exactly one space
between col 2 and col 3 exactly one space

start time is in seconds since unix epoch. start time should *not* be
normalised to start of workflow

event files should not (for now) be assumed to be in order

different event streams can be stored in different files. each event
stream should use the extension  .event
</pre><pre class="screen">
.coloured-event files
=====================
third column is a colour index
first two columns as per .event (thus a coloured-event is a specific
form of .event)
</pre></div><div class="section" title="7. hacky scripts"><div class="titlepage"><div><div><h2 class="title"><a name="id3043735"></a>7. hacky scripts</h2></div></div></div><p>There are a couple of hacky scripts that aren't made into proper
commandline tools. These are in the libexec/log-processing/ directory:

</p><pre class="screen">
  ./execute2-status-from-log [logfile]
     lists every (execute2) job and its final status

  ./execute2-summary-from-log [logfile]
     lists the counts of each final job status in log
</pre><p>
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
