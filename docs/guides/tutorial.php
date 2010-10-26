<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>A Swift Tutorial</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><meta name="description" content="This is an introductory tutorial on the use of Swift and its programming language SwiftScript. $LastChangedRevision: 3336 $"><link rel="home" href="index.html" title="A Swift Tutorial"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="A Swift Tutorial"><div class="titlepage"><div><div><h2 class="title"><a name="id2493295"></a>A Swift Tutorial</h2></div><div><div class="abstract" title="Abstract"><p class="title"><b>Abstract</b></p><p>
This is an introductory tutorial on the use of Swift and its
programming language SwiftScript.
                
$LastChangedRevision: 3336 $
                </p></div></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="section"><a href="#id2562275">1. Introduction</a></span></dt><dt><span class="section"><a href="#id2562321">2. Hello World</a></span></dt><dt><span class="section"><a href="#id2561043">3. Language features</a></span></dt><dd><dl><dt><span class="section"><a href="#id2561048">3.1. Parameters</a></span></dt><dt><span class="section"><a href="#id2560439">3.2. Adding another application</a></span></dt><dt><span class="section"><a href="#id2560611">3.3. Anonymous files</a></span></dt><dt><span class="section"><a href="#id2560650">3.4. Datatypes</a></span></dt><dt><span class="section"><a href="#id2561305">3.5. Arrays</a></span></dt><dt><span class="section"><a href="#id2561345">3.6. Mappers</a></span></dt><dt><span class="section"><a href="#id2561521">3.7. foreach</a></span></dt><dt><span class="section"><a href="#id2561595">3.8. If</a></span></dt><dt><span class="section"><a href="#tutorial.iterate">3.9. Sequential iteration</a></span></dt></dl></dd><dt><span class="section"><a href="#id2621927">4. Runtime features</a></span></dt><dd><dl><dt><span class="section"><a href="#id2621932">4.1. Visualising the workflow as a graph</a></span></dt><dt><span class="section"><a href="#id2621962">4.2. Running on a remote site</a></span></dt><dt><span class="section"><a href="#id2622004">4.3. Writing a mapper</a></span></dt><dt><span class="section"><a href="#id2560090">4.4. Starting and restarting</a></span></dt></dl></dd><dt><span class="section"><a href="#id2560078">5. bits</a></span></dt><dd><dl><dt><span class="section"><a href="#tutorial.named-parameters">5.1. Named and optional parameters</a></span></dt></dl></dd></dl></div><div class="section" title="1. Introduction"><div class="titlepage"><div><div><h2 class="title"><a name="id2562275"></a>1. Introduction</h2></div></div></div><p>
This tutorial is intended to introduce new users to the basics of Swift.
It is structured as a series of small exercise/examples which you can
try for yourself as you read along. After the first 'hello world'
example, there are two tracks - the language track (which introduces
features of the SwiftScript language) and the runtime track (which
introduces features of the Swift runtime environment, such as
running jobs on different sites)
    </p><p>
For information on getting an installation of Swift running, consult the
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/quickstartguide.php" target="_top">Swift Quickstart Guide</a>,
and return to this document when you have
successfully run the test SwiftScript program mentioned there.
    </p><p>
There is also a
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php" target="_top">Swift User's Guide</a>
which contains more detailed reference
material on topics covered in this manual. 

All of the programs included in this tutorial can be found in your Swift distribution in the examples/swift directory.
    </p></div><div class="section" title="2. Hello World"><div class="titlepage"><div><div><h2 class="title"><a name="id2562321"></a>2. Hello World</h2></div></div></div><p>
The first example program,
<code class="filename">first.swift</code>,
outputs a hello world message into
a file called <code class="filename">hello.txt</code>.
    </p><pre class="programlisting">
type messagefile;

app (messagefile t) greeting () {
        echo "Hello, world!" stdout=@filename(t);
}

messagefile outfile &lt;"hello.txt"&gt;;

outfile = greeting();
</pre><p>We can run this program as follows:</p><pre class="screen">
$ <strong class="userinput"><code>cd examples/swift/</code></strong>
$ <strong class="userinput"><code>swift first.swift</code></strong>
Swift svn swift-r3334 (swift modified locally) cog-r2752

RunID: 20100526-1925-8zjupq1b
Progress:
Final status:  Finished successfully:1
$ <strong class="userinput"><code>cat hello.txt</code></strong>
Hello, world!
</pre><p>The basic structure of this program is a
<em class="firstterm">type definition</em>,
an <em class="firstterm">application procedure definition</em>,
a <em class="firstterm">variable definition</em> and
then a <em class="firstterm">call</em> to the procedure:</p><pre class="programlisting">
type messagefile;
</pre><p>
First we define a new type, called messagefile.
In this example, we will use this messagefile
type as the type for our output message.
</p><div class="sidebar"><p class="title"><b></b></p><p>All data in SwiftScript must be typed,
whether it is stored in memory or on disk. This example defines a
very simple type. Later on we will see more complex type examples.
</p></div><pre class="programlisting">
app (messagefile t) greeting() { 
    echo "Hello, world!" stdout=@filename(t);
}
</pre><p>
Next we define a procedure called greeting. This procedure will write out
the "hello world" message to a file.
</p><p>
To achieve this, it executes the unix utility 'echo' with a parameter
"Hello, world!" and directs the standard output into the output file.
</p><p>
The actual file to use is specified by the
<em class="firstterm">return parameter</em>, t.
</p><pre class="programlisting">
messagefile outfile &lt;"hello.txt"&gt;;
</pre><p>
Here we define a variable called outfile. The type of this variable is
messagefile, and we specify that the contents of this variable will
be stored on disk in a file called hello.txt
</p><pre class="programlisting">
outfile = greeting();
</pre><p>
Now we call the greeting procedure, with its output going to the
outfile variable and therefore to hello.txt on disk.
</p><p>Over the following exercises, we'll extend this simple
hello world program to demonstrate various features of Swift.</p></div><div class="section" title="3. Language features"><div class="titlepage"><div><div><h2 class="title"><a name="id2561043"></a>3. Language features</h2></div></div></div><div class="section" title="3.1. Parameters"><div class="titlepage"><div><div><h3 class="title"><a name="id2561048"></a>3.1. Parameters</h3></div></div></div><p>
Procedures can have parameters. Input parameters specify inputs to the
procedure and output parameters specify outputs. Our helloworld greeting
procedure already uses an output parameter, t, which indicates where the
greeting output will go. In this section, we will add an input parameter
to the greeting function.</p><p>The code changes from <code class="filename">first.swift</code>
are highlighted below.</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting (string s) {
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile &lt;"hello2.txt"&gt;;

outfile = greeting("hello world");
</pre><p>We have modified the signature of the greeting procedure to indicate
that it takes a single parameter, s, of type 'string'.</p><p>We have modified the invocation of the 'echo' utility so that it
takes the value of s as a parameter, instead of the string literal
"Hello, world!".</p><p>We have modified the output file definition to point to a different
file on disk.</p><p>We have modified the invocation of greeting so that a greeting
string is supplied.</p><p>The code for this section can be found in 
<code class="filename">parameter.swift</code>. It can be
invoked using the swift command, with output appearing in 
<code class="filename">hello2.txt</code>:</p><pre class="screen">
$ <strong class="userinput"><code>swift parameter.swift</code></strong>
</pre><p>Now that we can choose our greeting text, we can call the same
procedure with different parameters to generate several output files with
different greetings. The code is in manyparam.swift and can be run as before
using the swift command.
</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting (string s) {
    app {
        echo s stdout=@filename(t);
    }
}

messagefile english &lt;"english.txt"&gt;;
messagefile french &lt;"francais.txt"&gt;;
english = greeting("hello");
french = greeting("bonjour");

messagefile japanese &lt;"nihongo.txt"&gt;;
japanese = greeting("konnichiwa");
</pre><p>Note that we can intermingle definitions of variables with invocations
of procedures.</p><p>When this program has been run, there should be three new files in the
working directory (english.txt, francais.txt and nihongo.txt) each containing
a greeting in a different language.</p><p>In addition to specifying parameters positionally, parameters can
be named, and if desired a default value can be specified - see 
<a class="link" href="#tutorial.named-parameters" title="5.1. Named and optional parameters">Named and optional
parameters</a>.</p></div><div class="section" title="3.2. Adding another application"><div class="titlepage"><div><div><h3 class="title"><a name="id2560439"></a>3.2. Adding another application</h3></div></div></div><p>
Now we'll define a new application procedure. The procedure we define
will capitalise all the words in the input file.
</p><p>To do this, we'll use the unix 'tr' (translate) utility.

Here is an example of using <span class="command"><strong>tr</strong></span> on the unix
command line, not using Swift:</p><pre class="screen">
$ <strong class="userinput"><code>echo hello | tr '[a-z]' '[A-Z]'</code></strong>
HELLO
</pre><p>
There are several steps:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>transformation catalog</p></li><li class="listitem"><p>application block</p></li></ul></div><p>
</p><p>First we need to modify the
<em class="firstterm">transformation catalog</em> to define
a logical transformation for the tc utility.  The transformation
catalog can be found in <code class="filename">etc/tc.data</code>.
There are already several entries specifying where programs can
be found. Add a new line to the file, specifying where 
<span class="command"><strong>tr</strong></span> can be found
(usually in <code class="filename">/usr/bin/tr</code>
but it may differ on your system), like this:
</p><pre class="programlisting">
localhost       tr      /usr/bin/tr     INSTALLED       INTEL32::LINUX  null
</pre><p>For now, ignore all of the fields except the second and the third.
The second field 'tr' specifies a logical application name and the
third specifies the location of the application executable.
</p><p>Now that we have defined where to find <span class="command"><strong>tr</strong></span>, we can
use it in SwiftScript.
</p><p>
We can define a new procedure, <span class="command"><strong>capitalise</strong></span> which calls
tr.
</p><pre class="programlisting">
(messagefile o) capitalise(messagefile i) {   
    app {
        tr "[a-z]" "[A-Z]" stdin=@filename(i) stdout=@filename(o);
    }
}
</pre><p>
</p><p>
We can call capitalise like this:
</p><pre class="programlisting">
messagefile final &lt;"capitals.txt"&gt;;
final = capitalise(hellofile);
</pre><p>
</p><p>
So a full program based on the first exercise might look like this:

</p><pre class="programlisting">
type messagefile {} 

(messagefile t) greeting (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

(messagefile o) capitalise(messagefile i) {   
    app {
        tr "[a-z]" "[A-Z]" stdin=@filename(i) stdout=@filename(o);
    }
}

messagefile hellofile &lt;"hello.txt"&gt;;
messagefile final &lt;"capitals.txt"&gt;;

hellofile = greeting("hello from Swift");
final = capitalise(hellofile);
</pre><p>
</p><p>We can use the swift command to run this:

</p><pre class="screen">
$ <strong class="userinput"><code>swift second_procedure.swift</code></strong>
[...]
$ <strong class="userinput"><code>cat capitals.txt</code></strong>
HELLO FROM SWIFT
</pre><p>
</p></div><div class="section" title="3.3. Anonymous files"><div class="titlepage"><div><div><h3 class="title"><a name="id2560611"></a>3.3. Anonymous files</h3></div></div></div><p>In the previous section, the file 
<code class="filename">greeting.txt</code> is used only to
store an intermediate result. We don't really care about which name is used
for the file, and we can let Swift choose the name.</p><p>To do that, omit the mapping entirely when declaring outfile:
</p><pre class="programlisting">
messagefile outfile;
</pre><p>
</p><p>
Swift will choose a filename, which in the present version will be
in a subdirectory called <code class="filename">_concurrent</code>.
</p></div><div class="section" title="3.4. Datatypes"><div class="titlepage"><div><div><h3 class="title"><a name="id2560650"></a>3.4. Datatypes</h3></div></div></div><p>
All data in variables and files has a data type.  So
far, we've seen two types:

</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">string - this is a built-in type for storing strings of text in
memory, much like in other programming languages</li><li class="listitem">messagefile - this is a user-defined type used to mark
files as containing messages</li></ul></div><p>
</p><p>
SwiftScript has the additional built-in types:
<em class="firstterm">boolean</em>, <em class="firstterm">integer</em> and
<em class="firstterm">float</em> that function much like their counterparts
in other programming languages.
</p><p>It is also possible to create user defined types with more
structure, for example:
</p><pre class="programlisting">
type details {
    string name;
    int pies;
}
</pre><p>
Each element of the structured type can be accessed using a . like this:
</p><pre class="programlisting">
person.name = "john";
</pre><p>
</p><p>
The following complete program, types.swift, outputs a greeting using a user-defined
structure type to hold parameters for the message:

</p><pre class="programlisting">
type messagefile {} 

type details {
    string name;
    int pies;
}

(messagefile t) greeting (details d) {   
    app {
        echo "Hello. Your name is" d.name "and you have eaten" d.pies "pies." stdout=@filename(t);
    }
}

details person;

person.name = "John";
person.pies = 3;

messagefile outfile &lt;"q15.txt&gt;";

outfile = greeting(person);
</pre><p>
</p><p>
Structured types can be comprised of marker types for files. See the later
section on mappers for more information about this.
</p></div><div class="section" title="3.5. Arrays"><div class="titlepage"><div><div><h3 class="title"><a name="id2561305"></a>3.5. Arrays</h3></div></div></div><p>We can define arrays using the [] suffix in a variable declaration:
</p><pre class="programlisting">
messagefile m[];
</pre><p>
This program, q5.swift, will declare an array of message files.
</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting (string s[]) {
    app {
        echo s[0] s[1] s[2] stdout=@filename(t);
    }
}

messagefile outfile &lt;"q5out.txt"&gt;;

string words[] = ["how","are","you"];

outfile = greeting(words);

</pre><p>Observe that the type of the parameter to greeting is now an
array of strings, 'string s[]', instead of a single string, 'string s',
that elements of the array can be referenced numerically, for example
s[0], and that the array is initialised using an array literal,
["how","are","you"].</p></div><div class="section" title="3.6. Mappers"><div class="titlepage"><div><div><h3 class="title"><a name="id2561345"></a>3.6. Mappers</h3></div></div></div><p>A significant difference between SwiftScript and other languages is
that data can be referred to on disk through variables in a very
similar fashion to data in memory.  For example, in the above
examples we have seen a variable definition like this:</p><pre class="programlisting">
messagefile outfile &lt;"q13greeting.txt"&gt;;
</pre><p>This means that 'outfile' is a dataset variable, which is
mapped to a file on disk called 'g13greeting.txt'. This variable
can be assigned to using = in a similar fashion to an in-memory
variable.  We can say that 'outfile' is mapped onto the disk file
'q13greeting.txt' by a <em class="firstterm">mapper</em>.
</p><p>There are various ways of mapping in SwiftScript. Two forms have already
been seen in this tutorial. Later exercises will introduce more forms.
</p><p>The two forms of mapping seen so far are:</p><div class="itemizedlist"><p>
simple named mapping - the name of the file that a variable is
mapped to is explictly listed. Like this:
</p><pre class="programlisting">
messagefile outfile &lt;"greeting.txt"&gt;;
</pre><p>

This is useful when you want to explicitly name input and output
files for your program. For example, 'outfile' in exercise HELLOWORLD.

</p><p>
anonymous mapping - no name is specified in the source code.
A name is automatically generated for the file. This is useful
for intermediate files that are only referenced through SwiftScript,
such as 'outfile' in exercise ANONYMOUSFILE. A variable declaration
is mapped anonymously by ommitting any mapper definition, like this:

</p><pre class="programlisting">
messagefile outfile;
</pre><p>

</p><ul class="itemizedlist" type="disc"></ul></div><p>Later exercises will introduce other ways of mapping from
disk files to SwiftScript variables.</p><p>TODO: introduce @v syntax.</p><div class="section" title="3.6.1. The regexp mapper"><div class="titlepage"><div><div><h4 class="title"><a name="id2561425"></a>3.6.1. The regexp mapper</h4></div></div></div><p>In this exercise, we introduce the <em class="firstterm">regexp mapper</em>.
This mapper transforms a string expression using a regular expression,
and uses the result of that transformation as the filename to map.</p><p>
<code class="filename">regexp.swift</code> demonstrates the use of this by placing output into a file that
is based on the name of the input file: our input file is mapped
to the inputfile variable using the simple named mapper, and then
we use the regular expression mapper to map the output file. Then we
use the countwords() procedure to count the works in the input file and 
store the result in the output file. In order for the countwords() procedure 
to work correctly, add the wc utility (usually found in /usr/bin/wc) to tc.data.
</p><p>
The important bit of <code class="filename">regexp.swift</code> is:
</p><pre class="programlisting">
messagefile inputfile &lt;"q16.txt"&gt;;

countfile c &lt;regexp_mapper;
             source=@inputfile,
             match="(.*)txt",
             transform="\\1count"
            &gt;;
</pre><p>
</p></div><div class="section" title="3.6.2. fixed_array_mapper"><div class="titlepage"><div><div><h4 class="title"><a name="id2561480"></a>3.6.2. fixed_array_mapper</h4></div></div></div><p>
The <em class="firstterm">fixed array mapper</em> maps a list of files into
an array - each element of the array is mapped into one file in the
specified directory. See <code class="filename">fixedarray.swift</code>.
</p><pre class="programlisting">
string inputNames = "one.txt two.txt three.txt";
string outputNames = "one.count two.count three.count";

messagefile inputfiles[] &lt;fixed_array_mapper; files=inputNames&gt;;
countfile outputfiles[] &lt;fixed_array_mapper; files=outputNames&gt;;

outputfiles[0] = countwords(inputfiles[0]);
outputfiles[1] = countwords(inputfiles[1]);
outputfiles[2] = countwords(inputfiles[2]);
</pre></div></div><div class="section" title="3.7. foreach"><div class="titlepage"><div><div><h3 class="title"><a name="id2561521"></a>3.7. foreach</h3></div></div></div><p>SwiftScript provides a control structure, foreach, to operate
on each element of an array.</p><p>In this example, we will run the previous word counting example
over each file in an array without having to explicitly list the
array elements. The source code for this example is in 
<code class="filename">foreach.swift</code>. The three input files
(<code class="filename">one.txt</code>, <code class="filename">two.txt</code> and
<code class="filename">three.txt</code>) are supplied. After
you have run the workflow, you should see that there are three output
files (<code class="filename">one.count</code>, <code class="filename">two.count</code>
and <code class="filename">three.count</code>) each containing the word
count for the corresponding input file. We combine the use of the
fixed_array_mapper and the regexp_mapper.</p><pre class="programlisting">
string inputNames = "one.txt two.txt three.txt";

messagefile inputfiles[] &lt;fixed_array_mapper; files=inputNames&gt;;


foreach f in inputfiles {
  countfile c &lt;regexp_mapper;
               source=@f,
               match="(.*)txt",
               transform="\\1count"&gt;;
  c = countwords(f);
}
</pre></div><div class="section" title="3.8. If"><div class="titlepage"><div><div><h3 class="title"><a name="id2561595"></a>3.8. If</h3></div></div></div><p>
Decisions can be made using 'if', like this:
</p><pre class="programlisting">
if(morning) {
  outfile = greeting("good morning");
} else {
  outfile = greeting("good afternoon");
}
</pre><p>
 <code class="filename">if.swift</code> contains a simple example of 
this. Compile and run <code class="filename">if.swift</code> and see that it
outputs 'good morning'. Changing the 'morning'
variable from true to false will cause the program to output 'good
afternoon'.
</p></div><div class="section" title="3.9. Sequential iteration"><div class="titlepage"><div><div><h3 class="title"><a name="tutorial.iterate"></a>3.9. Sequential iteration</h3></div></div></div><p>A development version of Swift after 0.2 (revision 1230) introduces
a sequential iteration construct.</p><p>
The following example demonstrates a simple application: each step of the
iteration is a string representation of the byte count of the previous
step's output, with iteration terminating when the byte count reaches zero.
</p><p>
Here's the program:
</p><pre class="programlisting">

type counterfile;

(counterfile t) echo(string m) { 
  app {
    echo m stdout=@filename(t);
  }
}

(counterfile t) countstep(counterfile i) {
  app {
    wcl @filename(i) @filename(t);
  }
}

counterfile a[]  &lt;simple_mapper;prefix="foldout"&gt;;

a[0] = echo("793578934574893");

iterate v {
  a[v+1] = countstep(a[v]);
 trace("extract int value ",@extractint(a[v+1]));
} until (@extractint(a[v+1]) &lt;= 1);

</pre><p>
<span class="command"><strong>echo</strong></span> is the standard unix echo.</p><p> <span class="command"><strong>wcl</strong></span>
is our application code - it counts the number of bytes in the one file
and writes that count out to another, like this:
</p><pre class="screen">
$ <strong class="userinput"><code>cat ../wcl</code></strong>
#!/bin/bash
echo -n $(wc -c &lt; $1) &gt; $2

$ <strong class="userinput"><code>echo -n hello &gt; a</code></strong>
$ <strong class="userinput"><code>wcl a b</code></strong>
$ <strong class="userinput"><code>cat b</code></strong>
5
</pre><p>Install the above wcl script somewhere and add a transformation catalog
entry for it. Then run the example program like this:
</p><pre class="screen">
$ <strong class="userinput"><code>swift iterate.swift</code></strong>
Swift svn swift-r3334 cog-r2752

RunID: 20100526-2259-gtlz8zf4
Progress:
SwiftScript trace: extract int value , 16.0
SwiftScript trace: extract int value , 2.0
SwiftScript trace: extract int value , 1.0
Final status:  Finished successfully:4

$ <strong class="userinput"><code>ls foldout*</code></strong>
foldout0000  foldout0001  foldout0002  foldout0003
</pre></div></div><div class="section" title="4. Runtime features"><div class="titlepage"><div><div><h2 class="title"><a name="id2621927"></a>4. Runtime features</h2></div></div></div><div class="section" title="4.1. Visualising the workflow as a graph"><div class="titlepage"><div><div><h3 class="title"><a name="id2621932"></a>4.1. Visualising the workflow as a graph</h3></div></div></div><p>
When running a workflow, its possible to generate a provenance graph at the
same time:
</p><pre class="screen">
$ <strong class="userinput"><code>swift -pgraph graph.dot first.swift</code></strong>
$ <strong class="userinput"><code>dot -ograph.png -Tpng graph.dot</code></strong>
</pre><p>
graph.png can then be viewed using your favourite image viewer.
</p></div><div class="section" title="4.2. Running on a remote site"><div class="titlepage"><div><div><h3 class="title"><a name="id2621962"></a>4.2. Running on a remote site</h3></div></div></div><p>As configured by default, all jobs are run locally. In the previous
examples, we've invoked 'echo' and 'tr' executables from our
SwiftScript program. These have been run on the local system
(the same computer on which you ran 'swift'). We can also make our
computations run on a remote resource.</p><p>WARNING: This example is necessarily more vague than previous examples,
because its requires access to remote resources. You should ensure that you
can submit a job using the globus-job-run (or globusrun-ws?) command(s).
</p><p>We do not need to modify any SwiftScript code to run on another resource.
Instead, we must modify another catalog, the 'site catalog'. This catalog
provides details of the location that applications will be run, with the
default settings referring to the local machine. We will modify it to
refer to a remote resource - the UC Teraport cluster. If you are not a
UC Teraport user, you should use details of a different resource that
you do have access to.
</p><p>The site catalog is located in etc/sites.xml and is a relatively
straightforward XML format file. We must modify each of the following
three settings: gridftp (which indicates how and where data can be
transferred to the remote resource), jobmanager (which indicates how
applications can be run on the remote resource) and workdirectory
(which indicates where working storage can be found on the
remote resource).</p></div><div class="section" title="4.3. Writing a mapper"><div class="titlepage"><div><div><h3 class="title"><a name="id2622004"></a>4.3. Writing a mapper</h3></div></div></div><p>
This section will introduce writing a custom mapper so that Swift is able
to access data files laid out in application-specific ways.
</p><p>
An application-specific mapper must take the form of a Java class
that implements the 
<a class="ulink" href="http://www.ci.uchicago.edu/swift/javadoc/vdsk/org/griphyn/vdl/mapping/Mapper.html" target="_top">Mapper</a> interface.
</p><p>
Usually you don't need to implement this interface directly, because
Swift provides a number of more concrete classes with some functionality
already implemented.
</p><p>The hierarchy of helper classes is:</p><p>
<a class="ulink" href="http://www.ci.uchicago.edu/swift/javadoc/vdsk/org/griphyn/vdl/mapping/Mapper.html" target="_top">Mapper</a>
- This is the abstract interface for mappers in Swift. You
must implement methods to provide access to mapper properties, to map
from a SwiftScript dataset path (such as foo[1].bar) to a file name,
to check whether a file exists. None of the default Swift mappers
implement this interface directly - instead they use one of the
following helper classes.</p><p>
<a class="ulink" href="http://www.ci.uchicago.edu/swift/javadoc/vdsk/org/griphyn/vdl/mapping/AbstractMapper.html" target="_top">AbstractMapper</a>
- This provides helper methods to manage mapper 
properties and to handle existance checking. Examples of mappers which
use this class are:
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.array_mapper" target="_top">array_mapper</a>,
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.csv_mapper" target="_top">csv_mapper</a>,
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.fixed_array_mapper" target="_top">fixed_array_mapper</a>,
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.regexp_mapper" target="_top">regexp_mapper</a> and
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.single_file_mapper" target="_top">single file mapper</a>
.</p><p>
<a class="ulink" href="http://www.ci.uchicago.edu/swift/javadoc/vdsk/org/griphyn/vdl/mapping/file/AbstractFileMapper.html" target="_top">AbstractFileMapper</a>
 - This provides a helper class for mappers
which select files based on selecting files from a directory listing.
It is necessary to write some helper methods that are different from
the above mapper methods. Examples of mappers which use this class
are:
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.simple_mapper" target="_top">simple_mapper</a>,
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/userguide.php#mapper.filesys_mapper" target="_top">filesys_mapper</a> and the (undocumented)
StructuredRegularExpressionMapper.
</p><p>
In general, to write a mapper, choose either the AbstractMapper or the
AbstractFileMapper and extend those. If your mapper will generally
select the files it returns based on a directory listing and will
convert paths to filenames using some regular conversion (for example, in
the way that simple_mapper maps files in a directory that match a
particular pattern), then you should probably use the AbstractFileMapper.
If your mapper will produce a list of files in some other way (for example,
in the way that csv_mapper maps based on filenames given in a CSV
file rather than looking at which files are in a directory), then you
should probably use the AbstractMapper.
</p><div class="section" title="4.3.1. Writing a very basic mapper"><div class="titlepage"><div><div><h4 class="title"><a name="id2622145"></a>4.3.1. Writing a very basic mapper</h4></div></div></div><p>In this section, we will write a very basic (almost useless)
mapper that will map a SwiftScript dataset into a hardcoded file
called <code class="filename">myfile.txt</code>, like this:

</p><pre class="screen">

    Swift variable                            Filename

      var   &lt;-----------------------------&gt;    myfile.txt

</pre><p>
We should be able to use the mapper we write in a SwiftScript program
like this:
</p><pre class="programlisting">
type file;

file f &lt;my_first_mapper;&gt;;
</pre><p>
First we must choose a base class - AbstractMapper or AbstractFileMapper.
We aren't going to use a directory listing to decide on our mapping
- we are getting the mapping from some other source (in fact, it
will be hard coded). So we will use AbstractMapper.
</p><p>
So now onto the source code. We must define a subclass of
<a class="ulink" href="" target="_top">AbstractMapper</a> and implement several
mapper methods: isStatic, existing, and map. These methods are documented
in the javadoc for the
<a class="ulink" href="" target="_top">Mapper</a>
interface.
</p><p>
Here is the code implementing this mapper. Put this in your source 
<code class="filename">vdsk</code> directory, make a directory
<code class="filename">src/tutorial/</code> and put this
file in <code class="filename">src/tutorial/MyFirstMapper.java</code>
</p><pre class="programlisting">
package tutorial;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class MyFirstMapper extends AbstractMapper {

  AbsFile myfile = new AbsFile("myfile.txt");

  public boolean isStatic() {
    return false;
  }

  public Collection existing() {
    if (myfile.exists())
      return Arrays.asList(new Path[] {Path.EMPTY_PATH});
    else
      return Collections.EMPTY_LIST;
  }

  public PhysicalFormat map(Path p) {
    if(p.equals(Path.EMPTY_PATH))
      return myfile;
    else
      return null;
  }
}

</pre><p>Now we need to inform the Swift engine about the existence of this
mapper. We do that by editing the MapperFactory class definition, in
<code class="filename">src/org/griphyn/vdl/mapping/MapperFactory.java</code> and
adding a registerMapper call alongside the existing registerMapper calls,
like this:
</p><pre class="programlisting">
registerMapper("my_first_mapper", tutorial.MyFirstMapper.class);
</pre><p>The first parameter is the name of the mapper that will be used
in SwiftScript program. The second parameter is the new Mapper class
that we just wrote.
</p><p>
Now rebuild Swift using the 'ant redist' target.
</p><p>
This new Swift build will be aware of your new mapper. We can test it out
with a hello world program:
</p><pre class="programlisting">
type messagefile;

(messagefile t) greeting() {
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile &lt;my_first_mapper;&gt;;

outfile = greeting();
</pre><p>Run this program, and hopefully you will find the "hello" string has
been output into the hard coded output file <code class="filename">myfile.txt</code>:
</p><pre class="screen">
$ <strong class="userinput"><code>cat myfile.txt</code></strong>
hello
</pre><p>So that's a first very simple mapper implemented. Compare the
source code to the single_file_mapper in
<a class="ulink" href="http://www.ci.uchicago.edu/trac/swift/browser/trunk/src/org/griphyn/vdl/mapping/file/SingleFileMapper.java" target="_top">SingleFileMapper.java</a>. There is
not much more code to the single_file_mapper - mostly code to deal
with the file parameter.
</p></div></div><div class="section" title="4.4. Starting and restarting"><div class="titlepage"><div><div><h3 class="title"><a name="id2560090"></a>4.4. Starting and restarting</h3></div></div></div><p>
Now we're going to try out the restart capabilities of Swift. We will make
a workflow that will deliberately fail, and then we will fix the problem
so that Swift can continue with the workflow.
</p><p>
First we have the program in working form, restart.swift.
</p><pre class="programlisting">
type file;

(file f) touch() {
  app {
    touch @f;
  }
}

(file f) processL(file inp) {
  app {
    echo "processL" stdout=@f;
  }
}

(file f) processR(file inp) {
  app {
    broken "process" stdout=@f;
  }
}

(file f) join(file left, file right) {
  app { 
    echo "join" @left @right stdout=@f;
  } 
}

file f = touch();

file g = processL(f);
file h = processR(f);

file i = join(g,h);
</pre><p>
We must define some transformation catalog entries:
</p><pre class="programlisting">
localhost	touch	/usr/bin/touch	INSTALLED	INTEL32::LINUX	null
localhost	broken	/bin/true	INSTALLED	INTEL32::LINUX	null
</pre><p>
Now we can run the program:
</p><pre class="programlisting">
$ swift restart.swift  
Swift 0.9 swift-r2860 cog-r2388

RunID: 20100526-1119-3kgzzi15
Progress:
Final status:  Finished successfully:4
</pre><p>
Four jobs run - touch, echo, broken and a final echo. (note that broken
isn't actually broken yet).
</p><p>
Now we will break the 'broken' job and see what happens. Replace the
definition in tc.data for 'broken' with this:
</p><pre class="programlisting">
localhost    broken     /bin/false   INSTALLED       INTEL32::LINUX  null
</pre><p>Now when we run the workflow, the broken task fails:</p><pre class="programlisting">
$ swift restart.swift 

Swift 0.9 swift-r2860 cog-r2388

RunID: 20100526-1121-tssdcljg
Progress:
Progress:  Stage in:1  Finished successfully:2
Execution failed:
	Exception in broken:
Arguments: [process]
Host: localhost
Directory: restart-20100526-1121-tssdcljg/jobs/1/broken-1i6ufisj
stderr.txt: 
stdout.txt: 

</pre><p>From the output we can see that touch and the first echo completed,
but then broken failed and so swift did not attempt to execute the
final echo.</p><p>There will be a restart log with the same name as the RunID:
</p><pre class="programlisting">
$ ls *20100526-1121-tssdcljg*rlog
restart-20100526-1121-tssdcljg.0.rlog
</pre><p>This restart log contains enough information for swift to know
which parts of the workflow were executed successfully.</p><p>We can try to rerun it immediately, like this:</p><pre class="programlisting">
$ swift -resume restart-20100526-1121-tssdcljg.0.rlog restart.swift 

Swift 0.9 swift-r2860 cog-r2388

RunID: 20100526-1125-7yx0zi6d
Progress:
Execution failed:
	Exception in broken:
Arguments: [process]
Host: localhost
Directory: restart-20100526-1125-7yx0zi6d/jobs/m/broken-msn1gisj
stderr.txt: 
stdout.txt: 

----

Caused by:
	Exit code 1

</pre><p>
Swift tried to resume the workflow by executing 'broken' again. It did not
try to run the touch or first echo jobs, because the restart log says that
they do not need to be executed again.
</p><p>Broken failed again, leaving the original restart log in place.</p><p>Now we will fix the problem with 'broken' by restoring the original
tc.data line that works.</p><p>Remove the existing 'broken' line and replace it with the successful
tc.data entry above:
</p><pre class="programlisting">
localhost       broken          /bin/true   INSTALLED       INTEL32::LINUX  null
</pre><p>
Now run again:
</p><pre class="programlisting">
$ swift -resume restart-20100526-1121-tssdcljg.0.rlog restart.swift

wift 0.9 swift-r2860 cog-r2388

RunID: 20100526-1128-a2gfuxhg
Progress:
Final status:  Initializing:2  Finished successfully:2
</pre><p>Swift tries to run 'broken' again. This time it works, and so
Swift continues on to execute the final piece of the workflow as if
nothing had ever gone wrong.
</p></div></div><div class="section" title="5. bits"><div class="titlepage"><div><div><h2 class="title"><a name="id2560078"></a>5. bits</h2></div></div></div><div class="section" title="5.1. Named and optional parameters"><div class="titlepage"><div><div><h3 class="title"><a name="tutorial.named-parameters"></a>5.1. Named and optional parameters</h3></div></div></div><p>In addition to specifying parameters positionally, parameters can
be named, and if desired a default value can be specified:

</p><pre class="programlisting">
(messagefile t) greeting (string s="hello") {
    app {
        echo s stdout=@filename(t);
    }
}
</pre><p>

When we invoke the procedure, we can specify values for the parameters
by name. The following code can be found in q21.swift.

</p><pre class="programlisting">
french = greeting(s="bonjour");
</pre><p>

or we can let the default value apply:

</p><pre class="programlisting">
english = greeting();
</pre><p>

</p></div></div></div>
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
