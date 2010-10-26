<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>19. How-To Tips for Specific User Communities</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="coasters.php" title="18. Coasters"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">19. How-To Tips for Specific User Communities</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="coasters.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> </td></tr></table><hr></div><div class="section" title="19. How-To Tips for Specific User Communities"><div class="titlepage"><div><div><h2 class="title"><a name="localhowtos"></a>19. How-To Tips for Specific User Communities</h2></div></div></div><div class="section" title="19.1. Saving Logs - for UChicago CI Users"><div class="titlepage"><div><div><h3 class="title"><a name="savinglogs"></a>19.1. Saving Logs - for UChicago CI Users</h3></div></div></div><p>
If you have a UChicago Computation Institute account, run this command in your 
submit directory after each run. It will copy all your logs and kickstart 
records into a directory at the CI for reporting, usage tracking, support and debugging.
			</p><p>
</p><pre class="screen">
rsync --ignore-existing *.log *.d login.ci.uchicago.edu:/disks/ci-gpfs/swift/swift-logs/ --verbose
</pre><p>
			</p></div><div class="section" title="19.2. Specifying TeraGrid allocations"><div class="titlepage"><div><div><h3 class="title"><a name="id3434292"></a>19.2. Specifying TeraGrid allocations</h3></div></div></div><p>TeraGrid users with no default project or with several project
allocations can specify a project allocation using a profile key in
the site catalog entry for a TeraGrid site:
</p><pre class="screen">
&lt;profile namespace="globus" key="project"&gt;TG-CCR080002N&lt;/profile&gt;
</pre><p>
</p><p>
More information on the TeraGrid allocations process can
be found <a class="ulink" href="http://www.teragrid.org/userinfo/access/allocations.php" target="_top">here</a>.
</p></div><div class="section" title="19.3. Launching MPI jobs from Swift"><div class="titlepage"><div><div><h3 class="title"><a name="tips.mpi"></a>19.3. Launching MPI jobs from Swift</h3></div></div></div><p>
Here is an example of running a simple MPI program.
</p><p>
In SwiftScript, we make an invocation that does not look any different
from any other invocation. In the below code, we do not have any input
files, and have two output files on stdout and stderr:
</p><pre class="programlisting">
type file;

(file o, file e) p() { 
    app {
        mpi stdout=@filename(o) stderr=@filename(e);
    }
}

file mpiout &lt;"mpi.out"&gt;;
file mpierr &lt;"mpi.err"&gt;;

(mpiout, mpierr) = p();
</pre><p>
</p><p>
Now we define how 'mpi' will run in tc.data:
</p><pre class="screen">
tguc    mpi             /home/benc/mpi/mpi.sh   INSTALLED       INTEL32::LINUX GLOBUS::host_xcount=3
</pre><p>
</p><p>
mpi.sh is a wrapper script that launches the MPI program. It must be installed
on the remote site:
</p><pre class="screen">
#!/bin/bash
mpirun -np 3 -machinefile $PBS_NODEFILE /home/benc/mpi/a.out 
</pre><p>
</p><p>
Because of the way that Swift runs its server side code, provider-specific
MPI modes (such as GRAM jobType=mpi) should not be used. Instead, the
mpirun command should be explicitly invoked.
</p></div><div class="section" title="19.4. Running on Windows"><div class="titlepage"><div><div><h3 class="title"><a name="tips.windows"></a>19.4. Running on Windows</h3></div></div></div><p>
			
				Since 10/11/09, the development version of Swift has the
ability to run on a Windows machine, as well as the ability to submit
jobs to a Windows site (provided that an appropriate provider is used). 
			
			</p><p>
			
In order to launch Swift on Windows, use the provided batch file
(swift.bat). In certain cases, when a large number of jar libraries are
present in the Swift lib directory and depending on the exact location
of the Swift installation, the classpath environment variable that the
Swift batch launcher tries to create may be larger than what Windows can
handle. In such a case, either install Swift in a directory closer to
the root of the disk (say, c:\swift) or remove non-essential jar files
from the Swift lib directory.

			</p><p>
			
Due to the large differences between Windows and Unix environments,
Swift must use environment specific tools to achieve some of its goals.
In particular, each Swift executable is launched using a wrapper script.
This script is a Bourne Shell script. On Windows machines, which have no
Bourne Shell interpreter installed by default, the Windows Scripting
Host is used instead, and the wrapper script is written in VBScript.
Similarly, when cleaning up after a run, the "/bin/rm" command available
in typical Unix environments must be replaced by the "del" shell command.
			
			</p><p>
			
It is important to note that in order to select the proper set of tools
to use, Swift must know when a site runs under Windows. To inform Swift
of this, specify the "sysinfo" attribute for the "pool" element in the
site catalog. For example:

</p><pre class="programlisting">
	&lt;pool handle="localhost" sysinfo="INTEL32::WINDOWS"&gt;
	...
	&lt;/pool&gt;
</pre><p>
			
			</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="coasters.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> </td></tr><tr><td width="40%" align="left" valign="top">18. Coasters </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> </td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
