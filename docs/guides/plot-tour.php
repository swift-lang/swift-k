<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Swift log plotting and the some internal mechanics of Swift</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.html" title="Swift log plotting and the some internal mechanics of Swift"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="Swift log plotting and the some internal mechanics of Swift"><div class="titlepage"><div><div><h2 class="title"><a name="id2564156"></a>Swift log plotting and the some internal mechanics of Swift</h2></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="section"><a href="#overview">1. Overview</a></span></dt><dt><span class="section"><a href="#execute">2. 'execute' - SwiftScript app {} block invocations</a></span></dt><dt><span class="section"><a href="#execute2">3. execute2 - one attempt at running an execute</a></span></dt><dt><span class="section"><a href="#info">4. wrapper info logs</a></span></dt><dt><span class="section"><a href="#id2632110">5. Relation of logged entities to each other</a></span></dt></dl></div><div class="section" title="1. Overview"><div class="titlepage"><div><div><h2 class="title"><a name="overview"></a>1. Overview</h2></div></div></div><p>
This document attempts to explain some of the meaning of the Swift
log-processing plots, giving an explanation of how some of Swift's
execution mechanism works and of some of the terminology used.
</p></div><div class="section" title="2. 'execute' - SwiftScript app {} block invocations"><div class="titlepage"><div><div><h2 class="title"><a name="execute"></a>2. 'execute' - SwiftScript app {} block invocations</h2></div></div></div><p>
When a SwiftScript program invokes a application procedure (one with an
app {} block), an 'execute' appears in the log file in START state. When
all attempts at execution have finished (either successfully or unsuccessfully)
then the execute will go into END_SUCCESS or END_FAILURE state. A workflow
is successful if and only if all invocations end in END_SUCCESS.
</p><p>
The execute states represent progress
through the karajan procedure defined in
<code class="filename">libexec/execute-default.k</code>.
</p><p>State changes for execute logs are defined by karajan log calls throughout
this file.
</p><span class="inlinemediaobject"><img src="execute.png"></span><p>An execute consists of multiple attempts to perform
<a class="link" href="#execute2" title="3. execute2 - one attempt at running an execute">execute2</a>s, with retries and replication
as appropriate. Retries and replication are not exposed through the states
of 'execute's.
</p><p>
Executes are uniquely identified within a run by their karajan thread ID,
which is present in the log files as the thread= parameter on execute
log messsages.
</p><p>
Here is a simple SwiftScript program which runs a foreach loop (<code class="filename">few.swift</code>):
</p><pre class="programlisting">
p() { 
    app {
        sleep "10s";
    }
}

foreach i in [1:8] {
    p();
}
</pre><p>

</p><p>
Using the <span class="command"><strong>swift-plot-log</strong></span> from the log processing module,
this graph gets generated to summarise execute state transitions:
</p><p>
<span class="inlinemediaobject"><img src="plot-tour/pregenerated/execute.png"></span>
</p><p>
In this graph, the forloop calls p() eight times. Because there are no
dependencies between those eight invocations, they are all invoked at the same
time, around 1s into the run. This is show on the graph by the JOB_START line
going from zero up to eight at around x=1s. As time passes, the sleep jobs
complete, and as they do so the number of jobs in END_SUCCESS state increases.
When all eight jobs are in END_SUCCESS state, the run is over.
</p><p>Here is a program with some data dependencies between invocations (<code class="filename">dep.swift</code>):

</p><pre class="programlisting">
$ cat dep.swift 
type file;

p(file f) { 
    app {
        sleep "10s";
    }
}

(file o) q() {
    app {
        touch @o;
    }
}

file intermediate = q();
p(intermediate);
</pre><p>

</p><p>
Here is a plot of the execute states for this program:
</p><p><span class="inlinemediaobject"><img src="plot-tour/pregenerated/execute-dep.png"></span>
</p><p>
In this run, one invocation starts (q()) fairly quickly, 
but the other invocation (of p()) does not - instead, it does not start until
approximately the time that the q() invocation has reached END_SUCCESS. 
</p><p>
Finally in this section on 'execute', here is a demonstration of how the above
two patterns fit together in one program (<code class="filename">few2.swift</code>:
</p><pre class="programlisting">
type file;

(file o) p(file i) { 
    app {
        sleepcopy @i @o;
    }
}

file input &lt;"input"&gt;;
file output[];

foreach i in [1:8] {
    file intermediate;
    intermediate = p(input);
    output[i] = p(intermediate);
}
</pre><p>
</p><p>
In total the program has 16 invocations of p(), dependent on each other in
pairs. The dependencies can be plotted like this:

</p><pre class="screen">
$ <strong class="userinput"><code>swift -pgraph few2.dot few2.swift</code></strong>
$ dot -Tpng -o few2.png few2.dot 
</pre><p>

yielding this graph:
</p><p><span class="inlinemediaobject"><img src="plot-tour/pregenerated/few2.png"></span> </p><p>
When this program is run, the first row of 8 invocations can all start at the
beginning of the program, because they have no dependencies (aside from on
the input file). This can be seen around t=4 when the start line jumps up to 8.
The other 8 invocations can only begin when the invocations they are dependent
on have finished. This can be seen in the graph - every time one of the first
invocations reaches END_SUCCESS, a new invocation enters START.
</p><p><span class="inlinemediaobject"><img src="plot-tour/pregenerated/execute-many-dep.png"></span> </p></div><div class="section" title="3. execute2 - one attempt at running an execute"><div class="titlepage"><div><div><h2 class="title"><a name="execute2"></a>3. execute2 - one attempt at running an execute</h2></div></div></div><p>
An execute2 is one attempt to execute an app procedure. execute2s are invoked
by <a class="link" href="#execute" title="2. 'execute' - SwiftScript app {} block invocations">execute</a>, once for each retry or replication
attempt.
</p><p>The states of an execute2 represent progress through the execute2 karajan
procedure defined in <code class="filename">libexec/vdl-int.k</code>
</p><span class="inlinemediaobject"><img src="execute2.png"></span><p>
Before an execute2 makes its first state log entry, it chooses a site to run on.
Then at the start of file stage-in, the execute2 goes into THREAD_ASSOCIATION
state. Once stagein is completed, the JOB_START state is entered, indicating
that execution of the job executable will now be attempted. Following that,
STAGING_OUT indicates that the output files are being staged out. If everything
is completed successfully, the job will enter JOB_END state.
</p><p>There are two exceptions to the above sequence: JOB_CANCELLED indicates that
the replication mechanism has cancelled this job because a different execute2
began actual execution on a site for the same execute. APPLICATION_EXCEPTION
indicates that there was an error somewhere in the attempt to stage in,
actually execute or stage out. If a job goes into APPLICATION_EXCEPTION state
then it will generally be retried (up to a certain number of times defined
by the "execution.retries" parameter) by the containing <a class="link" href="#execute" title="2. 'execute' - SwiftScript app {} block invocations">execute</a>.
</p><p>
In this example, we use a large input file to slow down file staging so that
it is visible on an execute2 graph (<code class="filename">big-files.swift</code>):
</p><pre class="programlisting">
type file;  
  
(file o) p(file i) {   
    app {  
        sleepcopy @i @o;  
    }  
}  
  
file input &lt;"biginput"&gt;;  
file output[];  
  
foreach i in [1:8] {  
    output[i] = p(input);  
}  
</pre><p>
</p><p>
<span class="inlinemediaobject"><img src="plot-tour/pregenerated/execute2.png"></span></p><p>
There is an initial large input file that must be staged in. This causes the first
jobs to be in stagein state for a period of time (the space between the
ASSOCIATED and JOB_START lines at the lower left corner of the graph). All
invocations share a single input file, so it is only staged in once and
shared between all subsequent invocations - once the file has staged in at the
start, there is no space later on between the ASSOCIATED and JOB_START lines
because of this.
</p><p>
Conversely, each invocation generates a large output file without there being
any sharing. Each of those output files must be staged back to the submit
side, which in this application takes some time. This can be seen by the large
amount of space between the STAGING_OUT and JOB_END lines.
</p><p>
The remaining large space on the graph is between the JOB_START and STAGING_OUT
lines. This represents the time taken to queue and execute the application
executable (and surrounding Swift worker-side wrapper, which can sometimes
have non-negligable execution times - this can be seen in the
<a class="link" href="#info" title="4. wrapper info logs">info section</a>).
</p></div><div class="section" title="4. wrapper info logs"><div class="titlepage"><div><div><h2 class="title"><a name="info"></a>4. wrapper info logs</h2></div></div></div><p>
When a job runs, it is wrapped by a Swift shell script on the remote site that
prepares the job environment, creating a temporary directory and moving
input and output files around. Each wrapper invocation corresponds to a single
application execution. For each invocation of the wrapper, a log file is created.
Sometimes that log file is moved back to the submission side (when there is
an error during execution, or when the setting 
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#engineconfiguration" target="_top">wrapper.always.transfer</a>=true
is set) and placed in a <code class="filename">*.d/</code> directory corresponding in
name to the main log file.
</p><span class="inlinemediaobject"><img src="info.png"></span><p>The states of the info logs represent progress through the wrapper
script, <code class="filename">libexec/wrapper.sh</code>.
</p><p>
For the same run of <code class="filename">big-files.swift</code> as shown in the
<a class="link" href="#execute2" title="3. execute2 - one attempt at running an execute">execute2 section</a>, here is a plot of states
in wrapper info log files:
</p><p>
<span class="inlinemediaobject"><img src="plot-tour/pregenerated/info.png"></span></p><p>
The trace lines on this graph fit entirely within the space between JOB_START 
and STAGING_OUT on the corresponding execute2 graph, because the Swift worker node
wrapper script does not run until the submit side of Swift has submitted a
job for execution and that job has begun running.
</p><p>
Many of the lines on this plot are very close together, because many of the
operations take minimal time. The main space between lines is between
EXECUTE and EXECUTE_DONE, where the actual application executable is executing;
and between COPYING_OUTPUTS and RM_JOBDIR, where the large output files are
copied from a job specific working directory to the site-specific shared
directory. It is quite hard to distinguish on the graph where overlapping
lines are plotted together.
</p><p>
Note also that minimal time is spent copying input files into the job-specific
directory in the wrapper script; that is because in this run, the wrapper
script is using the default behaviour of making symbolic links in the job-specific
directory; symbolic links are usually cheap to create compared to copying file
content. However, if the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#envvars" target="_top">SWIFT_JOBDIR_PATH</a> parameter is set, then Swift will
copy the input file to the specified job directory instead of linking. This
will generally result in much more time being spent preparing the job directory
in the Swift wrapper, but in certain circumstances this time is overwhelmingly
offset by increased performance of the actual application executable (so on
this chart, this would be seen as an increased job directory preparation time,
but a reduced-by-more application executable time).
</p></div><div class="section" title="5. Relation of logged entities to each other"><div class="titlepage"><div><div><h2 class="title"><a name="id2632110"></a>5. Relation of logged entities to each other</h2></div></div></div><p>Here is a simple diagram of how some of the above log channels along
with other pieces fit together:</p><span class="inlinemediaobject"><img src="logrelations.png"></span></div></div>
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
