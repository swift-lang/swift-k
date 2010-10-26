<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>9. Built-in procedure reference</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="functions.php" title="8. Function reference"><link rel="next" href="engineconfiguration.php" title="10. Swift configuration properties"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">9. Built-in procedure reference</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="functions.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="engineconfiguration.php">Next</a></td></tr></table><hr></div><div class="section" title="9. Built-in procedure reference"><div class="titlepage"><div><div><h2 class="title"><a name="procedures"></a>9. Built-in procedure reference</h2></div></div></div><p>
This section details built-in procedures that are available for use in
the SwiftScript language.
		</p><div class="section" title="9.1. readData"><div class="titlepage"><div><div><h3 class="title"><a name="procedure.readdata"></a>9.1. readData</h3></div></div></div><p>
<code class="literal">readData</code> will read data from a specified file.
			</p><p>
The format of the input file is controlled by the type of the return
value.
			</p><p>
For scalar return types, such as int, the specified file should contain
a single value of that type.
			</p><p>
For arrays of scalars, the specified file should contain one value
per line.
			</p><p>
For structs of scalars, the file should contain two rows.
The first row should be structure member names separated by whitespace.
The second row should be the corresponding values for each structure
member, separated by whitespace, in the same order as the header row.
			</p><p>
For arrays of structs, the file should contain a heading row listing
structure member names separated by whitespace. There should be one row
for each element of the array, with structure member elements listed in
the same order as the header row and separated by whitespace. (since Swift 0.4)
			</p></div><div class="section" title="9.2. readdata2"><div class="titlepage"><div><div><h3 class="title"><a name="procedure.readdata2"></a>9.2. readdata2</h3></div></div></div><p>
<code class="literal">readdata2</code> will read data from a specified file, like <code class="literal">readdata</code>, but using
a different file format more closely related to that used by the
ext mapper.
			</p><p>
Input files should list, one per line, a path into a Swift structure, and
the value for that position in the structure:
				</p><pre class="screen">
rows[0].columns[0] = 0                                                          
rows[0].columns[1] = 2                                                          
rows[0].columns[2] = 4                                                          
rows[1].columns[0] = 1                                                          
rows[1].columns[1] = 3                                                          
rows[1].columns[2] = 5 
				</pre><p>
which can be read into a structure defined like this:
				</p><pre class="programlisting">
type vector {                                                                   
        int columns[];                                                          
}                                                                               
                                                                                
type matrix {                                                                   
        vector rows[];                                                          
}                                                                               
                                                                                
matrix m;                                                                       
                                                                                
m = readData2("readData2.in");    
				</pre><p>
			</p><p>
(since Swift 0.7)
			</p></div><div class="section" title="9.3. trace"><div class="titlepage"><div><div><h3 class="title"><a name="procedure.trace"></a>9.3. trace</h3></div></div></div><p>
<code class="literal">trace</code> will log its parameters. By default these will appear on both stdout
and in the run log file. Some formatting occurs to produce the log message.
The particular output format should not be relied upon. (since Swift 0.4)
			</p></div><div class="section" title="9.4. writeData"><div class="titlepage"><div><div><h3 class="title"><a name="procedure.writedata"></a>9.4. writeData</h3></div></div></div><p>
<code class="literal">writeData</code> will write out data structures in the format
described for <code class="literal">readData</code>
			</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="functions.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="engineconfiguration.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">8. Function reference </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 10. Swift configuration properties</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
