<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>8. Function reference</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="extending.php" title="7. Ways in which Swift can be extended"><link rel="next" href="procedures.php" title="9. Built-in procedure reference"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">8. Function reference</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="extending.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="procedures.php">Next</a></td></tr></table><hr></div><div class="section" title="8. Function reference"><div class="titlepage"><div><div><h2 class="title"><a name="functions"></a>8. Function reference</h2></div></div></div><p>
This section details functions that are available for use in the SwiftScript
language.
		</p><div class="section" title="8.1. @arg"><div class="titlepage"><div><div><h3 class="title"><a name="function.arg"></a>8.1. @arg</h3></div></div></div><p>
Takes a command line parameter name as a string parameter and an optional 
default value and returns the value of that string parameter from the 
command line. If no default value is specified and the command line parameter
is missing, an error is generated. If a default value is specified and the
command line parameter is missing, <code class="literal">@arg</code> will return the default value.
			</p><p>
Command line parameters recognized by <code class="literal">@arg</code> begin with exactly one hyphen
and need to be positioned after the script name.
			</p><p>For example:</p><pre class="programlisting">
trace(@arg("myparam"));
trace(@arg("optionalparam", "defaultvalue"));
			</pre><pre class="screen">
$ <strong class="userinput"><code>swift arg.swift -myparam=hello</code></strong>
Swift v0.3-dev r1674 (modified locally)

RunID: 20080220-1548-ylc4pmda
SwiftScript trace: defaultvalue
SwiftScript trace: hello
			</pre></div><div class="section" title="8.2. @extractint"><div class="titlepage"><div><div><h3 class="title"><a name="function.extractint"></a>8.2. @extractint</h3></div></div></div><p>
<code class="literal">@extractint(file)</code> will read the specified file, parse an integer from the
file contents and return that integer.
			</p></div><div class="section" title="8.3. @filename"><div class="titlepage"><div><div><h3 class="title"><a name="function.filename"></a>8.3. @filename</h3></div></div></div><p>
<code class="literal">@filename(v)</code> will return a string containing the filename(s) for the file(s)
mapped to the variable <code class="literal">v</code>. When more than one filename is returned, the
filenames will be space separated inside a single string return value.
			</p></div><div class="section" title="8.4. @filenames"><div class="titlepage"><div><div><h3 class="title"><a name="function.filenames"></a>8.4. @filenames</h3></div></div></div><p>
<code class="literal">@filenames(v)</code> will return multiple values (!) containing the filename(s) for
the file(s) mapped to the variable <code class="literal">v</code>. (compare to
<a class="link" href="functions.php#function.filename" title="8.3. @filename">@filename</a>)
			</p></div><div class="section" title="8.5. @regexp"><div class="titlepage"><div><div><h3 class="title"><a name="function.regexp"></a>8.5. @regexp</h3></div></div></div><p>
<code class="literal">@regexp(input,pattern,replacement)</code> will apply regular expression
substitution using the <a class="ulink" href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html" target="_top">Java java.util.regexp API</a>. For example:
</p><pre class="programlisting">
string v =  @regexp("abcdefghi", "c(def)g","monkey");
</pre><p>
will assign the value <code class="literal">"abmonkeyhi"</code> to the variable <code class="literal">v</code>.
			</p></div><div class="section" title="8.6. @strcat"><div class="titlepage"><div><div><h3 class="title"><a name="function.strcat"></a>8.6. @strcat</h3></div></div></div><p>
<code class="literal">@strcat(a,b,c,d,...)</code> will return a string containing all of the strings
passed as parameters joined into a single string. There may be any number
of parameters.
			</p><p>
The <code class="literal">+</code> operator concatenates two strings: <code class="literal">@strcat(a,b)</code> is the same as <code class="literal">a + b</code>
			</p></div><div class="section" title="8.7. @strcut"><div class="titlepage"><div><div><h3 class="title"><a name="function.strcut"></a>8.7. @strcut</h3></div></div></div><p>
<code class="literal">@strcut(input,pattern)</code> will match the regular expression in the pattern
parameter against the supplied input string and return the section that
matches the first matching parenthesised group.
			</p><p>
For example:
			</p><pre class="programlisting">
string t = "my name is John and i like puppies.";
string name = @strcut(t, "my name is ([^ ]*) ");
string out = @strcat("Your name is ",name);
trace(out);
			</pre><p>
will output the message: <code class="literal">Your name is John</code>.
			</p></div><div class="section" title="8.8. @strsplit"><div class="titlepage"><div><div><h3 class="title"><a name="function.strsplit"></a>8.8. @strsplit</h3></div></div></div><p>
<code class="literal">@strsplit(input,pattern)</code> will split the input string based on separators
that match the given pattern and return a string array. (since Swift 0.9)
			</p><p>
Example:
			</p><pre class="programlisting">
string t = "my name is John and i like puppies.";
string words[] = @strsplit(t, "\\s");
foreach word in words {
	trace(word);
}
			</pre><p>
will output one word of the sentence on each line (though
not necessarily in order, due to the fact that foreach 
iterations execute in parallel).
			</p></div><div class="section" title="8.9. @toint"><div class="titlepage"><div><div><h3 class="title"><a name="function.toint"></a>8.9. @toint</h3></div></div></div><p>
<code class="literal">@toint(input)</code> will parse its input string into an integer. This can be
used with <code class="literal">@arg</code> to pass input parameters to a SwiftScript program as
integers.
			</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="extending.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="procedures.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">7. Ways in which Swift can be extended </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 9. Built-in procedure reference</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
