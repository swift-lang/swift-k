<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>5. Executing app procedures</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="commands.php" title="4. Commands"><link rel="next" href="techoverview.php" title="6. Technical overview of the Swift architecture"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">5. Executing <code class="literal">app</code> procedures</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="commands.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="techoverview.php">Next</a></td></tr></table><hr></div><div class="section" title="5. Executing app procedures"><div class="titlepage"><div><div><h2 class="title"><a name="appmodel"></a>5. Executing <code class="literal">app</code> procedures</h2></div></div></div><p>
This section describes how Swift executes <code class="literal">app</code> procedures,
and requirements on the behaviour of application programs used in
<code class="literal">app</code> procedures.
These requirements are primarily to ensure
that the Swift can run your application in different places and with the
various fault tolerance mechanisms in place.
	</p><div class="section" title="5.1. Mapping of app semantics into unix process execution semantics"><div class="titlepage"><div><div><h3 class="title"><a name="id3429529"></a>5.1. Mapping of <code class="literal">app</code> semantics into unix
process execution semantics</h3></div></div></div><p>This section describes how an <code class="literal">app</code> procedure
invocation is translated into a (remote) unix process execution. It does not
describe the mechanisms by which Swift performs that translation; that
is described in the next section.</p><p>In this section, this example SwiftScript program is used
for reference:</p><pre class="programlisting">
 type file;

 app (file o) count(file i) {
   wc @i stdout=@o;
 }

 file q &lt;"input.txt"&gt;;
 file r &lt;"output.txt"&gt;;
</pre><p>
The executable for wc will be looked up in tc.data.
</p><p>
This unix executable will then be executed in some <em class="firstterm">application
procedure workspace</em>. This means:
</p><p>
Each application procedure workspace will have an application workspace 
directory.  (TODO: can collapse terms //application procedure workspace// 
and //application workspace directory// ?
</p><p>
This application workspace directory will not be shared with any other 
<em class="firstterm">application procedure execution attempt</em>; all
application procedure 
execution attempts will run with distinct application procedure 
workspaces. (for the avoidance of doubt:
 If a <em class="firstterm">SwiftScript procedure invocation</em> is subject
to multiple application procedure execution attempts (due to Swift-level
restarts, retries or replication) then each of those application procedure
execution attempts will be made in a different application procedure workspace.
)</p><p>
The application workspace directory will be a directory on a POSIX 
filesystem accessible throughout the application execution by the 
application executable.
</p><p>
Before the <em class="firstterm">application executable</em> is executed:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
The application workspace directory will exist.
</p></li><li class="listitem"><p>
The <em class="firstterm">input files</em> will exist inside the application workspace 
directory (but not necessarily as direct children; there may be 
subdirectories within the application workspace directory).
</p></li><li class="listitem"><p>
The input files will be those files <em class="firstterm">mapped</em>
to <em class="firstterm">input parameters</em> of the application procedure
invocation. (In the example, this means that the file
<code class="filename">input.txt</code> will exist in the application workspace
directory)
</p></li><li class="listitem"><p>
For each input file dataset, it will be the case that
<code class="literal">@filename</code> or 
<code class="literal">@filenames</code> invoked with that dataset as a parameter
will return the path 
relative to the application workspace directory for the file(s) that are 
associated with that dataset. (In the example, that means that <code class="literal">@i</code> will 
evaluate to the path <code class="filename">input.txt</code>)
</p></li><li class="listitem"><p>
For each <em class="firstterm">file-bound</em> parameter of the Swift procedure invocation, the 
associated files (determined by data type?) will always exist.
</p></li><li class="listitem"><p>
The input files must be treated as read only files. This may or may not 
be enforced by unix file system permissions. They may or may not be copies
of the source file (conversely, they may be links to the actual source file).
</p></li></ul></div><p>
During/after the <em class="firstterm">application executable execution</em>,
the following must be true:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
If the application executable execution was successful (in the opinion 
of the application executable), then the application executable should 
exit with <em class="firstterm">unix return code</em> <code class="literal">0</code>;
if the application executable execution 
was unsuccessful (in the opinion of the application executable), then the 
application executable should exit with unix return code not equal to 
<code class="literal">0</code>.
</p></li><li class="listitem"><p>
Each file mapped from an output parameter of the SwiftScript procedure 
call must exist. Files will be mapped in the same way as for input files.
</p><p>
(? Is it defined that output subdirectories will be precreated before 
execution or should app executables expect to make them? That's probably 
determined by the present behaviour of wrapper.sh)
</p></li><li class="listitem"><p>
Output produced by running the application executable on some inputs should
be the same no matter how many times, when or where that application
executable is run. 'The same' can vary depending on application (for example,
in an application it might be acceptable for a PNG-&gt;JPEG conversion to
produce different, similar looking, output jpegs depending on the
environment)
</p></li></ul></div><p>
Things to not assume:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
anything about the path of the application workspace directory
</p></li><li class="listitem"><p>
that either the application workspace directory will be deleted or will 
continue to exist or will remain unmodified after execution has finished
</p></li><li class="listitem"><p>
that files can be passed(?def) between application procedure invocations 
through any mechanism except through files known to Swift through the 
mapping mechanism (there is some exception here for <code class="literal">external</code>
datasets - there are a separate set of assertions that hold for 
<code class="literal">external</code> datasets)
</p></li><li class="listitem"><p>
that application executables will run on any particular site of those
available, or than any combination of applications will run on the same or
different sites.
</p></li></ul></div></div><div class="section" title="5.2.  How Swift implements the site execution model"><div class="titlepage"><div><div><h3 class="title"><a name="id3429779"></a>5.2. 
How Swift implements the site execution model
</h3></div></div></div><p>
This section describes the implementation of the semantics described
in the previous section.
</p><p>
Swift executes application procedures on one or more <em class="firstterm">sites</em>.
</p><p>
Each site consists of:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
worker nodes. There is some <em class="firstterm">execution mechanism</em>
through which the Swift client side executable can execute its
<em class="firstterm">wrapper script</em> on those 
worker nodes. This is commonly GRAM or Falkon or coasters.
</p></li><li class="listitem"><p>
a site-shared file system. This site shared filesystem is accessible 
through some <em class="firstterm">file transfer mechanism</em> from the
Swift client side 
executable. This is commonly GridFTP or coasters. This site shared 
filesystem is also accessible through the posix file system on all worker 
nodes, mounted at the same location as seen through the file transfer 
mechanism. Swift is configured with the location of some <em class="firstterm">site working 
directory</em> on that site-shared file system.
</p></li></ul></div><p>
There is no assumption that the site shared file system for one site is 
accessible from another site.
</p><p>
For each workflow run, on each site that is used by that run, a <em class="firstterm">run 
directory</em> is created in the site working directory, by the Swift client 
side.
</p><p>
In that run directory are placed several subdirectories:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>
<code class="filename">shared/</code> - site shared files cache
</p></li><li class="listitem"><p>
<code class="filename">kickstart/</code> - when kickstart is used, kickstart record files 
for each job that has generated a kickstart record.
</p></li><li class="listitem"><p>
<code class="filename">info/</code> - wrapper script log files
</p></li><li class="listitem"><p>
<code class="filename">status/</code> - job status files
</p></li><li class="listitem"><p>
<code class="filename">jobs/</code> - application workspace directories (optionally placed here - 
see below)
</p></li></ul></div><p>
Application execution looks like this:
</p><p>
For each application procedure call:
</p><p>
The Swift client side selects a site; copies the input files for that 
procedure call to the site shared file cache if they are not already in 
the cache, using the file transfer mechanism; and then invokes the wrapper 
script on that site using the execution mechanism.
</p><p>
The wrapper script creates the application workspace directory; places the 
input files for that job into the application workspace directory using 
either <code class="literal">cp</code> or <code class="literal">ln -s</code> (depending on a configuration option); executes the 
application unix executable; copies output files from the application 
workspace directory to the site shared directory using <code class="literal">cp</code>; creates a 
status file under the <code class="filename">status/</code> directory; and exits, returning control to
the Swift client side. Logs created during the execution of the wrapper 
script are stored under the <code class="filename">info/</code> directory.
</p><p>
The Swift client side then checks for the presence of and deletes a status 
file indicating success; and copies files from the site shared directory to 
the appropriate client side location.
</p><p>
The job directory is created (in the default mode) under the <code class="filename">jobs/</code> 
directory. However, it can be created under an arbitrary other path, which 
allows it to be created on a different file system (such as a worker node 
local file system in the case that the worker node has a local file 
system).
</p></div><img src="swift-site-model.png"></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="commands.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="techoverview.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">4. Commands </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 6. Technical overview of the Swift architecture</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
