<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>3. Mappers</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="language.php" title="2. The SwiftScript Language"><link rel="next" href="commands.php" title="4. Commands"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">3. Mappers</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="language.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="commands.php">Next</a></td></tr></table><hr></div><div class="section" title="3. Mappers"><div class="titlepage"><div><div><h2 class="title"><a name="mappers"></a>3. Mappers</h2></div></div></div><p>
Mappers provide a mechanism to specify the layout of mapped datasets on
disk. This is needed when Swift must access files to transfer them to
remote sites for execution or to pass to applications.</p><p>
Swift provides a number of mappers that are useful in common cases. This
section details those standard mappers. For more complex cases, it is
possible to write application-specific mappers in Java and
use them within a SwiftScript program. 
		</p><div class="section" title="3.1. The single file mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.single_file_mapper"></a>3.1. The single file mapper</h3></div></div></div><p>
The <code class="literal">single_file_mapper</code> maps a single physical file to a dataset.
			</p><p>
		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       f                                 myfile

       f[0]                              INVALID

       f.bar                             INVALID

		</pre><p>
</p><div class="table"><a name="id3427767"></a><p class="title"><b>Table 3. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">file</td><td align="left">The location of the physical file including path and file name.</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
			</p><pre class="programlisting">
	file f &lt;single_file_mapper;file="plot_outfile_param"&gt;;</pre><p>

There is a simplified syntax for this mapper:


			</p><pre class="programlisting">
	file f &lt;"plot_outfile_param"&gt;;</pre><p>
</p></div><div class="section" title="3.2. The simple mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.simple_mapper"></a>3.2. The simple mapper</h3></div></div></div><p>The <code class="literal">simple_mapper</code> maps a file or a list of files
into an array by prefix, suffix, and pattern.  If more than one file is
matched, each of the file names will be mapped as a subelement of the dataset.
</p><div class="table"><a name="id3427844"></a><p class="title"><b>Table 4. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">A directory that the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
<code class="literal">"*foo*"</code> would match all file names that
contain <code class="literal">foo</code>. When this mapper is used to specify output
filenames, <code class="literal">pattern</code> is ignored.</td></tr></tbody></table></div></div><br class="table-break"><p>Examples:</p><p>
		</p><pre class="programlisting">
	type file;
	file f &lt;simple_mapper;prefix="foo", suffix=".txt"&gt;;
			</pre><p>
The above maps all filenames that start with <code class="filename">foo</code> and
have an extension <code class="filename">.txt</code> into file f.

		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       f                                 foo.txt

		</pre><p>
</p><p>
	</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting(string m) {.
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile &lt;simple_mapper;prefix="foo",suffix=".txt"&gt;;

outfile = greeting("hi");
	</pre><p>

This will output the string 'hi' to the file <code class="filename">foo.txt</code>.
	</p><p>
The <code class="literal">simple_mapper</code> can be used to map arrays. It will map the array index
into the filename between the prefix and suffix.

</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile[] &lt;simple_mapper;prefix="baz",suffix=".txt"&gt;;

outfile[0] = greeting("hello");
outfile[1] = greeting("middle");
outfile[2] = greeting("goodbye");
</pre><p>

		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       outfile[0]                        baz0000.txt
       outfile[1]                        baz0001.txt
       outfile[2]                        baz0002.txt

		</pre><p>

	</p><p>
<code class="literal">simple_mapper</code> can be used to map structures. It will map the name of the
structure member into the filename, between the prefix and the
suffix.

	</p><pre class="programlisting">
type messagefile;

type mystruct {
  messagefile left;
  messagefile right;
};

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

mystruct out &lt;simple_mapper;prefix="qux",suffix=".txt"&gt;;

out.left = greeting("hello");
out.right = greeting("goodbye");
	</pre><p>

This will output the string "hello" into the file
<code class="filename">qux.left.txt</code> and the string "goodbye"
into the file <code class="filename">qux.right.txt</code>.

		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       out.left                          quxleft.txt
       out.right                         quxright.txt

		</pre><p>
	</p></div><div class="section" title="3.3. concurrent mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.concurrent_mapper"></a>3.3. concurrent mapper</h3></div></div></div><p>
<code class="literal">concurrent_mapper</code> is almost the same as the simple mapper,
except that it is used to map an output file, and the filename
generated will contain an extract sequence that is unique.
This mapper is the default mapper for variables when no mapper is
specified.
</p><div class="table"><a name="id3428074"></a><p class="title"><b>Table 5. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">A directory that the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
<code class="literal">"*foo*"</code> would match all file names that
contain <code class="literal">foo</code>. When this mapper is used to specify output
filenames, <code class="literal">pattern</code> is ignored.</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
		</p><pre class="programlisting">
	file f1;
	file f2 &lt;concurrent_mapper;prefix="foo", suffix=".txt"&gt;;
			</pre><p>
The above example would use concurrent mapper for <code class="literal">f1</code> and
<code class="literal">f2</code>, and 
generate <code class="literal">f2</code> filename with prefix <code class="filename">"foo"</code> and extension <code class="filename">".txt"</code>
	</p></div><div class="section" title="3.4. file system mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.filesys_mapper"></a>3.4. file system mapper</h3></div></div></div><p><code class="literal">filesys_mapper</code> is similar to the simple mapper,
but maps a file or 
a list of files to an array. Each of the filename is 
mapped as an element in the array. The order of files in the resulting
array is not defined.
	</p><p>TODO: note on difference between location as a relative vs absolute
path wrt staging to remote location - as mihael said:
It's because you specify that location in the mapper. Try location="."
instead of location="/sandbox/..."</p><div class="table"><a name="id3428222"></a><p class="title"><b>Table 6. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">The directory where the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
<code class="literal">"*foo*"</code> would match all file names that
contain <code class="literal">foo</code>.
</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
			</p><pre class="programlisting">
	file texts[] &lt;filesys_mapper;prefix="foo", suffix=".txt"&gt;;
			</pre><p>
The above example would map all filenames that start with <code class="filename">"foo"</code> 
and have an extension <code class="filename">".txt"</code> into the array <code class="literal">texts</code>.
For example, if the specified directory contains files: <code class="filename">foo1.txt</code>, <code class="filename">footest.txt</code>,
<code class="filename">foo__1.txt</code>, then the mapping might be:
		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       texts[0]                          footest.txt
       texts[1]                          foo1.txt
       texts[2]                          foo__1.txt

		</pre><p>
</p></div><div class="section" title="3.5. fixed array mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.fixed_array_mapper"></a>3.5. fixed array mapper</h3></div></div></div><p>The <code class="literal">fixed_array_mapper</code> maps from a string that
contains a list of filenames into a file array.</p><div class="table"><a name="id3428376"></a><p class="title"><b>Table 7. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">files</td><td align="left">A string that contains a list of filenames, separated by space, comma or colon</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
			</p><pre class="programlisting">
	file texts[] &lt;fixed_array_mapper;files="file1.txt, fileB.txt, file3.txt"&gt;;
			</pre><p>
would cause a mapping like this:
		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       texts[0]                          file1.txt
       texts[1]                          fileB.txt
       texts[2]                          file3.txt

		</pre><p>
</p></div><div class="section" title="3.6. array mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.array_mapper"></a>3.6. array mapper</h3></div></div></div><p>The <code class="literal">array_mapper</code> maps from an array of strings
into a file</p><div class="table"><a name="id3428456"></a><p class="title"><b>Table 8. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">files</td><td align="left">An array of strings containing one filename per element</td></tr></tbody></table></div></div><br class="table-break"><p> Example:
		</p><pre class="programlisting">
string s[] = [ "a.txt", "b.txt", "c.txt" ];

file f[] &lt;array_mapper;files=s&gt;;
		</pre><p>
This will establish the mapping:
		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       f[0]                              a.txt
       f[1]                              b.txt
       f[2]                              c.txt

		</pre><p>

	</p></div><div class="section" title="3.7. regular expression mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.regexp_mapper"></a>3.7. regular expression mapper</h3></div></div></div><p>The <code class="literal">regexp_mapper</code> transforms one file name to
another using regular expression matching.</p><div class="table"><a name="id3428535"></a><p class="title"><b>Table 9. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">source</td><td align="left">The source file name</td></tr><tr><td align="left">match</td><td align="left">Regular expression pattern to match, use
<code class="literal">()</code> to match whatever regular expression is inside the
parentheses, and indicate the start and end of a group; the contents of a
group can be retrieved with the <code class="literal">\\number</code> special sequence 
(two backslashes are needed because the backslash is an escape sequence introducer)
</td></tr><tr><td align="left">transform</td><td align="left">The pattern of the file name to
transform to, use <code class="literal">\number</code> to reference the
group matched.</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
	</p><pre class="programlisting">
  string s = "picture.gif";
  file f &lt;regexp_mapper;
    source=s,
    match="(.*)gif",
    transform="\\1jpg"&gt;; </pre><p>

This example transforms a string ending <code class="literal">gif</code> into one
ending <code class="literal">jpg</code> and maps that to a file.

		</p><pre class="screen">
    Swift variable -------------------&gt;  Filename

       f                                    picture.jpg
		</pre><p>

</p></div><div class="section" title="3.8. csv mapper"><div class="titlepage"><div><div><h3 class="title"><a name="id3428641"></a>3.8. csv mapper</h3></div></div></div><p>
The <code class="literal">csv_mapper</code> maps the content of a CSV (comma-separated
value) file into an array of structures. The dataset type needs to be
correctly defined to conform to the column names in the
file. For instance, if the file contains columns:
<code class="literal">name age GPA</code> then the type needs to have member elements
like this:
</p><pre class="programlisting">
  type student {
    file name;
    file age;
    file GPA;
  }
</pre><p>

If the file does not contain a header with column info, then the column
names are assumed as <code class="literal">column1</code>, <code class="literal">column2</code>,
etc.
</p><div class="table"><a name="id3428682"></a><p class="title"><b>Table 10. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">file</td><td align="left">The name of the CSV file to read mappings from.</td></tr><tr><td align="left">header</td><td align="left">Whether the file has a line describing header info; default is <code class="literal">true</code></td></tr><tr><td align="left">skip</td><td align="left">The number of lines to skip at the beginning (after header line); default is <code class="literal">0</code>.</td></tr><tr><td align="left">hdelim</td><td align="left">Header field delimiter; default is the value of the <code class="literal">delim</code> parameter</td></tr><tr><td align="left">delim</td><td align="left">Content field delimiters; defaults are space, tab and comma</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
			</p><pre class="programlisting">
	student stus[] &lt;csv_mapper;file="stu_list.txt"&gt;;
			</pre><p>
The above example would read a list of student info from file 
<code class="filename">"stu_list.txt"</code> and map them into a student array. By default, the file should contain a header line specifying the names of the columns.
If <code class="filename">stu_list.txt</code> contains the following:
</p><pre class="screen">
name,age,gpa
101-name.txt, 101-age.txt, 101-gpa.txt
name55.txt, age55.txt, age55.txt
q, r, s
</pre><p>
then some of the mappings produced by this example would be:
		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       stus[0].name                         101-name.txt
       stus[0].age                          101-age.txt
       stus[0].gpa                          101-gpa.txt
       stus[1].name                         name55.txt
       stus[1].age                          age55.txt
       stus[1].gpa                          gpa55.txt
       stus[2].name                         q
       stus[2].age                          r
       stus[2].gpa                          s

		</pre><p>
</p></div><div class="section" title="3.9. external mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.ext_mapper"></a>3.9. external mapper</h3></div></div></div><p>
The external mapper, <code class="literal">ext</code> maps based on the output of a
supplied Unix executable.
		</p><div class="table"><a name="id3428833"></a><p class="title"><b>Table 11. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">exec</td><td align="left">The name of the executable
(relative to the current directory, if an absolute path is not
specified)</td></tr><tr><td align="left">*</td><td align="left">Other parameters are passed to the
executable prefixed with a <code class="literal">-</code> symbol</td></tr></tbody></table></div></div><br class="table-break"><p>   
The output of the executable should consist of two columns of data, separated
by a space. The first column should be the path of the mapped variable,
in SwiftScript syntax (for example <code class="literal">[2]</code> means the 2nd element of an
array) or the symbol <code class="literal">$</code> to represent the root of the mapped variable.
	</p><p> Example:
With the following in <code class="filename">mapper.sh</code>,
			</p><pre class="screen">
#!/bin/bash
echo "[2] qux"
echo "[0] foo"
echo "[1] bar"
			</pre><p>

then a mapping statement:

			</p><pre class="programlisting">
	student stus[] &lt;ext;exec="mapper.sh"&gt;;
			</pre><p>

would map

		</p><pre class="screen">

    Swift variable -------------------&gt;  Filename

       stus[0]                              foo
       stus[1]                              bar
       stus[2]                              qux

		</pre><p>

		</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="language.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="commands.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">2. The SwiftScript Language </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 4. Commands</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
