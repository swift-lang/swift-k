<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>10. Swift configuration properties</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="procedures.php" title="9. Built-in procedure reference"><link rel="next" href="profiles.php" title="11. Profiles"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">10. Swift configuration properties</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="procedures.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="profiles.php">Next</a></td></tr></table><hr></div><div class="section" title="10. Swift configuration properties"><div class="titlepage"><div><div><h2 class="title"><a name="engineconfiguration"></a>10. Swift configuration properties</h2></div></div></div><p>
		
			Various aspects of the behavior of the Swift Engine can be
			configured through properties. The Swift Engine recognizes a global,
			per installation properties file which can found in <code class="filename">etc/swift.properties</code> in the Swift installation directory and a user
			properties file which can be created by each user in <code class="filename">~/.swift/swift.properties</code>. The Swift Engine
			will first load the global properties file. It will then try to load
			the user properties file. If a user properties file is found,
			individual properties explicitly set in that file will override the
			respective properties in the global properties file. Furthermore,
			some of the properties can be overridden directly using command line
			arguments to the <a class="link" href="commands.php#swiftcommand" title="4.1. swift"><span class="command"><strong>swift</strong></span> command</a>.
		
		</p><p>
			
				Swift properties are specified in the following format:
				
</p><pre class="screen">
&lt;name&gt;=&lt;value&gt;
</pre><p>
			
				The value can contain variables which will be expanded when the
				properties file is read. Expansion is performed when the name of
				the variable is used inside the standard shell dereference
				construct: <code class="literal">${<code class="varname">name</code>}</code>. The following variables
				can be used in the Swift configuration file:
				
				</p><div class="variablelist" title="Swift Configuration Variables"><p class="title"><b>Swift Configuration Variables</b></p><dl><dt><span class="term">
							<code class="varname">swift.home</code>
						</span></dt><dd><p>
							
								Points to the Swift installation directory
								(<code class="filename"><code class="envar">$SWIFT_HOME</code></code>). In general, this should not be set
as Swift can find its own installation directory, and incorrectly setting it
may impair the correct functionality of Swift.
							
							</p></dd><dt><span class="term">
							<code class="varname">user.name</code>
						</span></dt><dd><p>
							
								The name of the current logged in user.
								
							</p></dd><dt><span class="term">
							<code class="varname">user.home</code>
						</span></dt><dd><p>
							
								The user's home directory.
								
							</p></dd></dl></div><p>
				
				The following is a list of valid Swift properties:
				
				</p><div class="variablelist" title="Swift Properties"><p class="title"><b>Swift Properties</b></p><dl><dt><a name="property.caching.algorithm"></a><span class="term">
							<span class="property">caching.algorithm</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">LRU</code>
							</p><p>
								Default value: <code class="literal">LRU</code>
							</p><p>
							
								Swift caches files that are staged in on remote
								resources, and files that are produced remotely
								by applications, such that they can be re-used
								if needed without being transfered again.
								However, the amount of remote file system space
								to be used for caching can be limited using the
								<a class="link" href="profiles.php#profile.swift.storagesize"><span class="property">swift:storagesize</span></a> profile
								entry in the sites.xml file. Example:
								
</p><pre class="screen">

&lt;pool handle="example" sysinfo="INTEL32::LINUX"&gt;
	&lt;gridftp url="gsiftp://example.org" storage="/scratch/swift" major="2" minor="4" patch="3"/&gt;
	&lt;jobmanager universe="vanilla" url="example.org/jobmanager-pbs" major="2" minor="4" patch="3"/&gt;
	&lt;workdirectory&gt;/scratch/swift&lt;/workdirectory&gt;
	&lt;profile namespace="SWIFT" key="storagesize"&gt;20000000&lt;/profile&gt;
&lt;/pool&gt;

</pre><p>
								
								
								The decision of which files to keep in the cache
								and which files to remove is made considering
								the value of the
								<span class="property">caching.algorithm</span> property. 							
								Currently, the only available value for this 							
								property is <code class="literal">LRU</code>, which would
								cause the least recently used files to be
								deleted first.
								
							</p></dd><dt><a name="property.clustering.enabled"></a><span class="term">
							<span class="property">clustering.enabled</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
								Enables <a class="link" href="clustering.php" title="17. Clustering">clustering</a>.
							</p></dd><dt><a name="property.clustering.min.time"></a><span class="term">
							<span class="property">clustering.min.time</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>
							</p><p>
								Default value: <code class="literal">60</code>
							</p><p>
							
								Indicates the threshold wall time for
								clustering, in seconds. Jobs that have a 
								wall time smaller than the value of this
								property will be considered for clustering.
								
							</p></dd><dt><a name="property.clustering.queue.delay"></a><span class="term">
							<span class="property">clustering.queue.delay</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>
							</p><p>
								Default value: <code class="literal">4</code>
							</p><p>
							
								This property indicates the interval, in
								seconds, at which the clustering queue is
								processed.
								
							</p></dd><dt><a name="property.execution.retries"></a><span class="term">execution.retries</span></dt><dd><p>
								Valid values: positive integers
							</p><p>
								Default value: 2
							</p><p>
								The number of time a job will be retried if it
								fails (giving a maximum of 1 +
								execution.retries attempts at execution)
							</p></dd><dt><a name="property.foreach.max.threads"></a><span class="term">foreach.max.threads</span></dt><dd><p>
								Valid values: positive integers
							</p><p>
								Default value: 1024
							</p><p>
Limits the number of concurrent iterations that each foreach statement
can have at one time. This conserves memory for swift programs that 
have large numbers of iterations (which would otherwise all be executed
in parallel). (since Swift 0.9)
							</p></dd><dt><span class="term">
							<span class="property">ip.address</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;ipaddress&gt;</code></em>
							</p><p>
								Default value: N/A
							</p><p>
								The Globus GRAM service uses a callback
								mechanism to send notifications about the status
								of submitted jobs. The callback mechanism
								requires that the Swift client be reachable from
								the hosts the GRAM services are running on.
								Normally, Swift can detect the correct IP address
								of the client machine. However, in certain cases
								(such as the client machine having more than one
								network interface) the automatic detection
								mechanism is not reliable. In such cases, the IP
								address of the Swift client machine can be
								specified using this property. The value of this
								property must be a numeric address without quotes.
							</p><p>
								This option is deprecated and the hostname
								property should be used instead.
							</p></dd><dt><span class="term">
							<span class="property">kickstart.always.transfer</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
							
								This property controls when output from
								Kickstart is transfered back to the submit site,
								if Kickstart is enabled. When set to
								<code class="literal">false</code>, Kickstart output is
								only transfered for jobs that fail. If set to
								<code class="literal">true</code>, Kickstart output is
								transfered after every job is completed or
								failed.
								
							</p></dd><dt><span class="term">
							<span class="property">kickstart.enabled</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>, <code class="literal">maybe</code>
							</p><p>
								Default value: <code class="literal">maybe</code>
							</p><p>
								
								This option allows controlling of
								when Swift uses <a class="link" href="kickstart.php" title="15. Kickstart">Kickstart</a>. A value of
								<code class="literal">false</code> disables the use of
								Kickstart, while a value of
								<code class="literal">true</code> enables the use of
								Kickstart, in which case sites specified in the
								<code class="filename">sites.xml</code> file
								must have valid
								<em class="parameter"><code>gridlaunch</code></em> attributes.
								The <code class="literal">maybe</code> value will
								enable the use of Kickstart only
								on sites that have the
								<em class="parameter"><code>gridlaunch</code></em> attribute
								specified.

							</p></dd><dt><span class="term">
							<span class="property">lazy.errors</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
							
								Swift can report application errors in two
								modes, depending on the value of this property.
								If set to <code class="constant">false</code>, Swift will
								report the first error encountered and
								immediately stop execution. If set to
								<code class="constant">true</code>, Swift will attempt to
								run as much as possible from a SwiftScript program before
								stopping execution and reporting all errors
								encountered.
							</p><p>When developing SwiftScript programs, using the
								default value of <code class="constant">false</code> can
								make the program easier to debug. However
								in production runs, using <code class="constant">true</code>
								will allow more of a SwiftScript program to be run before
								Swift aborts execution.
							</p></dd><dt><span class="term">
							<span class="property">pgraph</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>, <em class="parameter"><code>&lt;file&gt;</code></em>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
							
								Swift can generate a 
<a class="ulink" href="http://www.graphviz.org/" target="_top">Graphviz</a> file representing 
								the structure of the SwiftScript program it has run. If this
								property is set to <code class="literal">true</code>,
								Swift will save the provenance graph in a file
								named by concatenating the program name and the
								instance ID (e.g. <code class="filename">helloworld-ht0adgi315l61.dot</code>). 
							</p><p>
								If set to <code class="literal">false</code>, no
								provenance  graph will be generated. If a file
								name is used, then  the provenance graph will be
								saved in the specified file.
							</p><p>
								The generated dot file can be rendered
								into a graphical form using
								<a class="ulink" href="http://www.graphviz.org/" target="_top">Graphviz</a>,
								for example with a command-line such as:
							</p><pre class="screen">
$ <strong class="userinput"><code>swift -pgraph graph1.dot q1.swift</code></strong>
$ <strong class="userinput"><code>dot -ograph.png -Tpng graph1.dot</code></strong>
							</pre></dd><dt><span class="term">
							<span class="property">pgraph.graph.options</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;string&gt;</code></em>
							</p><p>
								Default value: <code class="literal">splines="compound", rankdir="TB"</code>
							</p><p>
							
								This property specifies a <a class="ulink" href="http://www.graphviz.org" target="_top">Graphviz</a>
								specific set of parameters for the graph.
								
							</p></dd><dt><span class="term">
							<span class="property">pgraph.node.options</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;string&gt;</code></em>
							</p><p>
								Default value: <code class="literal">color="seagreen", style="filled"</code>
							</p><p>
							
								Used to specify a set of <a class="ulink" href="http://www.graphviz.org" target="_top">Graphviz</a>
								specific properties for the nodes in the graph.
								
							</p></dd><dt><span class="term">
							<span class="property">provenance.log</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
								This property controls whether the log file will contain provenance information enabling this will increase the size of log files, sometimes significantly.
							</p></dd><dt><span class="term">
							<span class="property">replication.enabled</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
Enables/disables replication. Replication is used to deal with jobs sitting
in batch queues for abnormally large amounts of time. If replication is enabled
and certain conditions are met, Swift creates and submits replicas of jobs, and
allows multiple instances of a job to compete.
							</p></dd><dt><span class="term">
							<span class="property">replication.limit</span>
						</span></dt><dd><p>
								Valid values: positive integers
							</p><p>
								Default value: 3
							</p><p>
The maximum number of replicas that Swift should attempt.
							</p></dd><dt><span class="term">
							<span class="property">sitedir.keep</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>true</code></em>, <em class="parameter"><code>false</code></em>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
Indicates whether the working directory on the remote site should be
left intact even when a run completes successfully. This can be
used to inspect the site working directory for debugging purposes.
							</p></dd><dt><span class="term">
							<span class="property">sites.file</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;file&gt;</code></em>
							</p><p>
								Default value: ${<code class="varname">swift.home</code>}<code class="literal">/etc/sites.xml</code>
							</p><p>
							
								Points to the location of the site
								catalog, which contains a list of all sites that
								Swift should use.

								
							</p></dd><dt><span class="term">
							<span class="property">status.mode</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>files</code></em>, <em class="parameter"><code>provider</code></em>
							</p><p>
								Default value: <code class="literal">files</code>
							</p><p>
Controls how Swift will communicate the result code of running user programs
from workers to the submit side. In <code class="literal">files</code> mode, a file
indicating success or failure will be created on the site shared filesystem.
In <code class="literal">provider</code> mode, the execution provider job status will
be used.
							</p><p>
<code class="literal">provider</code> mode requires the underlying job execution system
to correctly return exit codes. In at least the cases of GRAM2, and clusters
used with any provider, exit codes are not returned, and so
<code class="literal">files</code> mode must be used in those cases.  Otherwise,
<code class="literal">provider</code> mode can be used to reduce the amount of
filesystem access. (since Swift 0.8)
							</p></dd><dt><span class="term">
							<span class="property">tc.file</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;file&gt;</code></em>
							</p><p>
								Default value: ${<code class="varname">swift.home</code>}<code class="literal">/etc/tc.data</code>
							</p><p>
							
								Points to the location of the transformation
								catalog file which contains information about
								installed applications. Details about the format
								of the transformation catalog can be found
								<a class="ulink" href="http://vds.uchicago.edu/vds/doc/userguide/html/H_TransformationCatalog.html" target="_top">here</a>.
								
							</p></dd><dt><span class="term">
							<span class="property">tcp.port.range</span>
						</span></dt><dd><p>Valid values: <em class="parameter"><code>&lt;start&gt;</code></em>,<em class="parameter"><code>&lt;end&gt;</code></em> where start and end are integers</p><p>Default value: none</p><p>
A TCP port range can be specified to restrict the ports on which 
GRAM callback services are started. This is likely needed if your
 submit host is behind a firewall, in which case the firewall 
should be configured to allow incoming connections on ports in 
the range. 
							</p></dd><dt><span class="term">
							<span class="property">throttle.file.operations</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>, <em class="parameter"><code>off</code></em>
							</p><p>
								Default value: <code class="literal">8</code>
							</p><p>
							
								Limits the total number of concurrent file
								operations that can happen at any given time.
								File operations (like transfers) require an
								exclusive connection to a site. These
								connections can be expensive to establish. A
								large number of concurrent file operations may
								cause Swift to attempt to establish many  such
								expensive connections to various sites. Limiting
								the number of concurrent file operations causes
								Swift to use a small number of cached
								connections and achieve better overall
								performance. 
								
							</p></dd><dt><span class="term">
							<span class="property">throttle.host.submit</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>, <em class="parameter"><code>off</code></em>
							</p><p>
								Default value: <code class="literal">2</code>
							</p><p>
							
								Limits the number of concurrent submissions for
								any of the sites Swift will try to send jobs to.
								In other words it guarantees that no more than
								the  value of this throttle jobs sent to any
								site will be concurrently in a state of being
								submitted.
								
							</p></dd><dt><a name="property.throttle.score.job.factor"></a><span class="term">
							<span class="property">throttle.score.job.factor</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>, <em class="parameter"><code>off</code></em>
							</p><p>
								Default value: <code class="literal">4</code>
							</p><p>
								The Swift scheduler has the ability to limit
								the number of concurrent jobs allowed on a
								site based on the performance history of that
								site. Each site is assigned a score (initially
								1), which can increase or decrease based on
								whether the site yields successful or faulty
								job runs. The score for a site can take values
								in the (0.1, 100) interval. The number of
								allowed jobs is calculated using the
								following formula:
							</p><p>
								2 + score*throttle.score.job.factor 
							</p><p>
								This means a site will always be allowed
								at least two concurrent jobs and at most
								2 + 100*throttle.score.job.factor. With a
								default of 4 this means at least 2 jobs and
								at most 402. 
							</p><p>
								This parameter can also be set per site
								using the jobThrottle profile key in a site
								catalog entry.
							</p></dd><dt><span class="term">
							<span class="property">throttle.submit</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>, <em class="parameter"><code>off</code></em>
							</p><p>
								Default value: <code class="literal">4</code>
							</p><p>
							
								Limits the number of concurrent submissions for
								a run. This throttle only limits
								the number of concurrent tasks (jobs) that are
								being sent to sites, not the total number of
								concurrent jobs that can be run. The submission
								stage in GRAM is one of the most CPU expensive
								stages (due mostly to the mutual authentication
								and delegation). Having too many  concurrent
								submissions can overload either or both the
								submit host CPU and the remote host/head node
								causing degraded performance.
								
							</p></dd><dt><span class="term">
							<span class="property">throttle.transfers</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>&lt;int&gt;</code></em>, <em class="parameter"><code>off</code></em>
							</p><p>
								Default value: <code class="literal">4</code>	
							</p><p>
							
								Limits the total number of concurrent file
								transfers that can happen at any given time.
								File transfers consume bandwidth. Too many
								concurrent transfers can cause the network to be
								overloaded preventing various other signaling
								traffic from flowing properly.
								
							</p></dd><dt><span class="term">
							<span class="property">ticker.disable</span>
						</span></dt><dd><p>
								Valid values: <em class="parameter"><code>true</code></em>, <em class="parameter"><code>false</code></em>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
When set to true, suppresses the output progress ticker that Swift sends
to the console every few seconds during a run (since Swift 0.9)
							</p></dd><dt><span class="term">
							<span class="property">wrapper.invocation.mode</span>
						</span></dt><dd><p>
Valid values: <em class="parameter"><code>absolute</code></em>, <em class="parameter"><code>relative</code></em>
							</p><p>
Default value: <code class="literal">absolute</code>
							</p><p>
Determines if Swift remote wrappers will be executed by specifying an
absolute path, or a path relative to the job initial working directory.
In most cases, execution will be successful with either option. However,
some execution sites ignore the specified initial working directory, and
so <code class="literal">absolute</code> must be used. Conversely on some sites,
job directories appear in a different place on the worker node file system
than on the filesystem access node, with the execution system handling
translation of the job initial working directory. In such cases,
<code class="literal">relative</code> mode must be used. (since Swift 0.9)
							</p></dd><dt><span class="term">
							<span class="property">wrapper.parameter.mode</span>
						</span></dt><dd><p>
Controls how Swift will supply parameters to the remote wrapper script.
<code class="literal">args</code> mode will pass parameters on the command line. Some
execution systems do not pass commandline parameters sufficiently cleanly
for Swift to operate correctly.
<code class="literal">files</code> mode will pass parameters through an additional
input file (since Swift 0.95). This provides a cleaner communication channel
for parameters, at the expense of transferring an additional file for each
job invocation.
							</p></dd><dt><span class="term">
							<span class="property">wrapperlog.always.transfer</span>
						</span></dt><dd><p>
								Valid values: <code class="literal">true</code>, <code class="literal">false</code>
							</p><p>
								Default value: <code class="literal">false</code>
							</p><p>
							
								This property controls when output from
								the Swift remote wrapper is transfered
								back to the submit site. When set to
								<code class="literal">false</code>, wrapper logs are 
								only transfered for jobs that fail. If set to
								<code class="literal">true</code>, wrapper logs are
								transfered after every job is completed or
								failed.
								
							</p></dd></dl></div><p>
				
				Example:
				
</p><pre class="screen">
sites.file=${vds.home}/etc/sites.xml
tc.file=${vds.home}/etc/tc.data
ip.address=192.168.0.1
</pre><p>
			
			</p></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="procedures.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="profiles.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">9. Built-in procedure reference </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 11. Profiles</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
