<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Swift User Guide</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.html" title="Swift User Guide"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="Swift User Guide"><div class="titlepage"><div><div><h2 class="title"><a name="id2759603"></a>Swift User Guide</h2></div><div><h3 class="subtitle"><i>Source control $LastChangedRevision: 3215 $</i></h3></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="section"><a href="#overview">1. Overview</a></span></dt><dt><span class="section"><a href="#language">2. The SwiftScript Language</a></span></dt><dd><dl><dt><span class="section"><a href="#id2828596">2.1. Language basics</a></span></dt><dt><span class="section"><a href="#id2826752">2.2. Arrays and Parallel Execution</a></span></dt><dt><span class="section"><a href="#id2826945">2.3. Ordering of execution</a></span></dt><dt><span class="section"><a href="#id2827824">2.4. Compound procedures</a></span></dt><dt><span class="section"><a href="#id2827906">2.5. More about types</a></span></dt><dt><span class="section"><a href="#id2888220">2.6. Data model</a></span></dt><dt><span class="section"><a href="#id2888303">2.7. More technical details about SwiftScript</a></span></dt><dt><span class="section"><a href="#id2888863">2.8. Operators</a></span></dt><dt><span class="section"><a href="#globals">2.9. Global constants</a></span></dt><dt><span class="section"><a href="#imports">2.10. Imports</a></span></dt></dl></dd><dt><span class="section"><a href="#mappers">3. Mappers</a></span></dt><dd><dl><dt><span class="section"><a href="#mapper.single_file_mapper">3.1. The single file mapper</a></span></dt><dt><span class="section"><a href="#mapper.simple_mapper">3.2. The simple mapper</a></span></dt><dt><span class="section"><a href="#mapper.concurrent_mapper">3.3. concurrent mapper</a></span></dt><dt><span class="section"><a href="#mapper.filesys_mapper">3.4. file system mapper</a></span></dt><dt><span class="section"><a href="#mapper.fixed_array_mapper">3.5. fixed array mapper</a></span></dt><dt><span class="section"><a href="#mapper.array_mapper">3.6. array mapper</a></span></dt><dt><span class="section"><a href="#mapper.regexp_mapper">3.7. regular expression mapper</a></span></dt><dt><span class="section"><a href="#id2890074">3.8. csv mapper</a></span></dt><dt><span class="section"><a href="#mapper.ext_mapper">3.9. external mapper</a></span></dt></dl></dd><dt><span class="section"><a href="#commands">4. Commands</a></span></dt><dd><dl><dt><span class="section"><a href="#swiftcommand">4.1. swift</a></span></dt><dt><span class="section"><a href="#id2890846">4.2. swift-osg-ress-site-catalog</a></span></dt><dt><span class="section"><a href="#id2890962">4.3. swift-plot-log</a></span></dt></dl></dd><dt><span class="section"><a href="#appmodel">5. Executing <code class="literal">app</code> procedures</a></span></dt><dd><dl><dt><span class="section"><a href="#id2891034">5.1. Mapping of <code class="literal">app</code> semantics into unix
process execution semantics</a></span></dt><dt><span class="section"><a href="#id2891322">5.2. 
How Swift implements the site execution model
</a></span></dt></dl></dd><dt><span class="section"><a href="#techoverview">6. Technical overview of the Swift architecture</a></span></dt><dd><dl><dt><span class="section"><a href="#id2891557">6.1. karajan - the core execution engine</a></span></dt><dt><span class="section"><a href="#id2891563">6.2. Execution layer</a></span></dt><dt><span class="section"><a href="#id2891593">6.3. SwiftScript language compilation layer</a></span></dt><dt><span class="section"><a href="#id2891615">6.4. Swift/karajan library layer</a></span></dt></dl></dd><dt><span class="section"><a href="#extending">7. Ways in which Swift can be extended</a></span></dt><dt><span class="section"><a href="#functions">8. Function reference</a></span></dt><dd><dl><dt><span class="section"><a href="#function.arg">8.1. @arg</a></span></dt><dt><span class="section"><a href="#function.extractint">8.2. @extractint</a></span></dt><dt><span class="section"><a href="#function.filename">8.3. @filename</a></span></dt><dt><span class="section"><a href="#function.filenames">8.4. @filenames</a></span></dt><dt><span class="section"><a href="#function.regexp">8.5. @regexp</a></span></dt><dt><span class="section"><a href="#function.strcat">8.6. @strcat</a></span></dt><dt><span class="section"><a href="#function.strcut">8.7. @strcut</a></span></dt><dt><span class="section"><a href="#function.strsplit">8.8. @strsplit</a></span></dt><dt><span class="section"><a href="#function.toint">8.9. @toint</a></span></dt></dl></dd><dt><span class="section"><a href="#procedures">9. Built-in procedure reference</a></span></dt><dd><dl><dt><span class="section"><a href="#procedure.readdata">9.1. readData</a></span></dt><dt><span class="section"><a href="#procedure.readdata2">9.2. readdata2</a></span></dt><dt><span class="section"><a href="#procedure.trace">9.3. trace</a></span></dt><dt><span class="section"><a href="#procedure.writedata">9.4. writeData</a></span></dt></dl></dd><dt><span class="section"><a href="#engineconfiguration">10. Swift configuration properties</a></span></dt><dt><span class="section"><a href="#profiles">11. Profiles</a></span></dt><dd><dl><dt><span class="section"><a href="#profile.karajan">11.1. Karajan namespace</a></span></dt><dt><span class="section"><a href="#profile.swift">11.2. swift namespace</a></span></dt><dt><span class="section"><a href="#profile.globus">11.3. Globus namespace</a></span></dt><dt><span class="section"><a href="#profile.env">11.4. env namespace</a></span></dt></dl></dd><dt><span class="section"><a href="#sitecatalog">12. The Site Catalog - sites.xml</a></span></dt><dd><dl><dt><span class="section"><a href="#id2894804">12.1. Pool element</a></span></dt><dt><span class="section"><a href="#id2894864">12.2. File transfer method</a></span></dt><dt><span class="section"><a href="#id2894967">12.3. Execution method</a></span></dt><dt><span class="section"><a href="#id2895229">12.4. Work directory</a></span></dt><dt><span class="section"><a href="#id2895262">12.5. Profiles</a></span></dt></dl></dd><dt><span class="section"><a href="#transformationcatalog">13. The Transformation Catalog - tc.data</a></span></dt><dt><span class="section"><a href="#buildoptions">14. Build options</a></span></dt><dt><span class="section"><a href="#kickstart">15. Kickstart</a></span></dt><dt><span class="section"><a href="#reliability">16. Reliability mechanisms</a></span></dt><dd><dl><dt><span class="section"><a href="#retries">16.1. Retries</a></span></dt><dt><span class="section"><a href="#restart">16.2. Restarts</a></span></dt><dt><span class="section"><a href="#replication">16.3. Replication</a></span></dt></dl></dd><dt><span class="section"><a href="#clustering">17. Clustering</a></span></dt><dt><span class="section"><a href="#coasters">18. Coasters</a></span></dt><dt><span class="section"><a href="#localhowtos">19. How-To Tips for Specific User Communities</a></span></dt><dd><dl><dt><span class="section"><a href="#savinglogs">19.1. Saving Logs - for UChicago CI Users</a></span></dt><dt><span class="section"><a href="#id2896345">19.2. Specifying TeraGrid allocations</a></span></dt><dt><span class="section"><a href="#tips.mpi">19.3. Launching MPI jobs from Swift</a></span></dt><dt><span class="section"><a href="#tips.windows">19.4. Running on Windows</a></span></dt></dl></dd></dl></div><div class="section" title="1. Overview"><div class="titlepage"><div><div><h2 class="title"><a name="overview"></a>1. Overview</h2></div></div></div><p>
This manual provides reference material for Swift: the SwiftScript language
and the Swift runtime system. For introductory material, consult
the <a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/tutorial.php" target="_top">Swift
tutorial</a>.
	</p><p>
Swift is a data-oriented coarse grained scripting language that
supports dataset typing and mapping, dataset iteration,
conditional branching, and procedural composition.
	</p><p>
Swift programs (or <em class="firstterm">workflows</em>) are written in
a language called <em class="firstterm">SwiftScript</em>.
	</p><p>
SwiftScript programs are dataflow oriented - they are primarily
concerned with processing (possibly large) collections of data files,
by invoking programs to do that processing. Swift handles execution of
such programs on remote sites by choosing sites, handling the staging
of input and output files to and from the chosen sites and remote execution
of program code.
	</p></div><div class="section" title="2. The SwiftScript Language"><div class="titlepage"><div><div><h2 class="title"><a name="language"></a>2. The SwiftScript Language</h2></div></div></div><div class="section" title="2.1. Language basics"><div class="titlepage"><div><div><h3 class="title"><a name="id2828596"></a>2.1. Language basics</h3></div></div></div><p>
A Swift script describes data, application components, invocations
of applications components, and the inter-relations (data flow) 
between those invocations.
</p><p>
Data is represented in a script by strongly-typed single-assignment
variables. The syntax superficially resembles C and Java. For example,
<code class="literal">{</code> and <code class="literal">}</code> characters are used to
enclose blocks of statements.
</p><p>
Types in Swift can be <em class="firstterm">atomic</em> or
<em class="firstterm">composite</em>. An atomic type can be either a
<em class="firstterm">primitive type</em> or a <em class="firstterm">mapped type</em>.
Swift provides a fixed set of primitive types, such as
<em class="firstterm">integer</em> and <em class="firstterm">string</em>. A mapped
type indicates that the actual data does not reside in CPU addressable
memory (as it would in conventional programming languages), but in
POSIX-like files. Composite types are further subdivided into
<em class="firstterm">structures</em> and <em class="firstterm">arrays</em>.
Structures are similar in most respects to structure types in other languages.
Arrays use numeric indices, but are sparse. They can contain elements of
any type, including other array types, but all elements in an array must be
of the same type.  We often refer to instances of composites of mapped types
as <em class="firstterm">datasets</em>.
</p><img src="type-hierarchy.png"><p>
Mapped type and composite type variable declarations can be annotated with a
<em class="firstterm">mapping descriptor</em> indicating the file(s) that make up
that dataset.  For example, the following line declares a variable named
<code class="literal">photo</code> with type <code class="literal">image</code>. It additionally
declares that the data for this variable is stored in a single file named
<code class="filename">shane.jpeg</code>.
</p><pre class="programlisting">
  image photo &lt;"shane.jpeg"&gt;;
</pre><p>
Component programs of scripts are declared in an <em class="firstterm">app
declaration</em>, with the description of the command line syntax
for that program and a list of input and output data. An <code class="literal">app</code>
block describes a functional/dataflow style interface to imperative
components.
</p><p>
For example, the following example lists a procedure which makes use  of
the <a class="ulink" href="http://www.imagemagick.org/" target="_top"> ImageMagick</a>
<span class="command"><strong>convert</strong></span> command to rotate a supplied
image by a specified angle:
</p><pre class="programlisting">
  app (image output) rotate(image input) {
    convert "-rotate" angle @input @output;
  }
</pre><p>
A procedure is invoked using the familiar syntax:
</p><pre class="programlisting">
  rotated = rotate(photo, 180);
</pre><p>
While this looks like an assignment, the actual unix level execution
consists of invoking the command line specified in the <code class="literal">app</code>
declaration, with variables on the left of the assignment bound to the
output parameters, and variables to the right of the procedure
invocation passed as inputs.
</p><p>
The examples above have used the type <code class="literal">image</code> without any
definition of that type. We can declare it as a <em class="firstterm">marker type</em>
which has no structure exposed to SwiftScript:
</p><pre class="programlisting">
  type image;
</pre><p>
This does not indicate that the data is unstructured; but it indicates
that the structure of the data is not exposed to SwiftScript. Instead,
SwiftScript will treat variables of this type as individual opaque
files.
</p><p>
With mechanisms to declare types, map variables to data files, and
declare and invoke procedures, we can build a complete (albeit simple)
script:
</p><pre class="programlisting">
 type image;
 image photo &lt;"shane.jpeg"&gt;;
 image rotated &lt;"rotated.jpeg"&gt;;

 app (image output) rotate(image input, int angle) {
    convert "-rotate" angle @input @output;
 }

 rotated = rotate(photo, 180);
</pre><p>
This script can be invoked from the command line:
</p><pre class="screen">
  $ <strong class="userinput"><code>ls *.jpeg</code></strong>
  shane.jpeg
  $ <strong class="userinput"><code>swift example.swift</code></strong>
  ...
  $ <strong class="userinput"><code>ls *.jpeg</code></strong>
  shane.jpeg rotated.jpeg
</pre><p>
This executes a single <code class="literal">convert</code> command, hiding from the
user features such as remote multisite execution and fault tolerance that
will be discussed in a later section.
</p><div class="figure"><a name="id2826725"></a><p class="title"><b>Figure 1. shane.jpeg</b></p><div class="figure-contents"><img src="userguide-shane.jpeg" alt="shane.jpeg"></div></div><br class="figure-break"><div class="figure"><a name="id2826738"></a><p class="title"><b>Figure 2. rotated.jpeg</b></p><div class="figure-contents"><img src="userguide-rotated.jpeg" alt="rotated.jpeg"></div></div><br class="figure-break"></div><div class="section" title="2.2. Arrays and Parallel Execution"><div class="titlepage"><div><div><h3 class="title"><a name="id2826752"></a>2.2. Arrays and Parallel Execution</h3></div></div></div><p>
Arrays of values can be declared using the <code class="literal">[]</code> suffix. An
array be mapped to a collection of files, one element per file, by using
a different form of mapping expression.  For example, the
<a class="link" href="#mapper.filesys_mapper" title="3.4. file system mapper"><code class="literal">filesys_mapper</code></a>
maps all files matching a particular unix glob pattern into an array:
</p><pre class="programlisting">
  file frames[] &lt;filesys_mapper; pattern="*.jpeg"&gt;;
</pre><p>
The <em class="firstterm"><code class="literal">foreach</code></em> construct can be used
to apply the same block of code to each element of an array:
</p><pre class="programlisting">
   foreach f,ix in frames {
     output[ix] = rotate(frames, 180);
   }
</pre><p>
Sequential iteration can be expressed using the <code class="literal">iterate</code>
construct:
</p><pre class="programlisting">
   step[0] = initialCondition();
   iterate ix {
     step[ix] = simulate(step[ix-1]);
   }
</pre><p>
This fragment will initialise the 0-th element of the <code class="literal">step</code>
array to some initial condition, and then repeatedly run the 
<code class="literal">simulate</code> procedure, using each execution's outputs as
input to the next step.
</p></div><div class="section" title="2.3. Ordering of execution"><div class="titlepage"><div><div><h3 class="title"><a name="id2826945"></a>2.3. Ordering of execution</h3></div></div></div><p>
Non-array variables are <em class="firstterm">single-assignment</em>, which
means that they must be assigned to exactly one value during execution.
A procedure or expression will be executed when all of its input parameters
have been assigned values. As a result of such execution, more variables may
become assigned, possibly allowing further parts of the script to
execute.
</p><p>
In this way, scripts are implicitly parallel. Aside from serialisation
implied by these dataflow dependencies, execution of component programs
can proceed in parallel.
</p><p>
In this fragment, execution of procedures <code class="literal">p</code> and
<code class="literal">q</code> can happen in parallel:
</p><pre class="programlisting">
  y=p(x);
  z=q(x);
</pre><p>while in this fragment, execution is serialised by the variable
<code class="literal">y</code>, with procedure <code class="literal">p</code> executing
before <code class="literal">q</code>.</p><pre class="programlisting">
 y=p(x);
 z=q(y);
</pre><p>
Arrays in SwiftScript are more
<em class="firstterm">monotonic</em> - a generalisation of being
assignment. Knowledge about the
content of an array increases during execution, but cannot otherwise
change. Each element of the array is itself single assignment or monotonic
(depending on its type).
During a run all values for an array are eventually known, and that array
is regarded as <em class="firstterm">closed</em>.
</p><p>
Statements which deal with the array as a whole will often wait for the array
to be closed before executing (thus, a closed array is the equivalent
of a non-array type being assigned). However, a <code class="literal">foreach</code>
statement will apply its body to elements of an array as they become
known. It will not wait until the array is closed.
</p><p>
Consider this script:
</p><pre class="programlisting">
 file a[];
 file b[];
 foreach v,i in a {
   b[i] = p(v);
 }
 a[0] = r();
 a[1] = s();
</pre><p>
Initially, the <code class="literal">foreach</code> statement will have nothing to
execute, as the array <code class="literal">a</code> has not been assigned any values.
The procedures <code class="literal">r</code> and <code class="literal">s</code> will execute.
As soon as either of them is finished, the corresponding invocation of
procedure <code class="literal">p</code> will occur. After both <code class="literal">r</code>
and <code class="literal">s</code> have completed, the array <code class="literal">a</code> will
be closed since no other statements in the script make an assignment to
<code class="literal">a</code>.
</p></div><div class="section" title="2.4. Compound procedures"><div class="titlepage"><div><div><h3 class="title"><a name="id2827824"></a>2.4. Compound procedures</h3></div></div></div><p>
As with many other programming languages, procedures consisting of SwiftScript
code can be defined. These differ from the previously mentioned procedures
declared with the <code class="literal">app</code> keyword, as they invoke other
SwiftScript procedures rather than a component program.
</p><pre class="programlisting">
 (file output) process (file input) {
   file intermediate;
   intermediate = first(input);
   output = second(intermediate);
 }

 file x &lt;"x.txt"&gt;;
 file y &lt;"y.txt"&gt;;
 y = process(x);
</pre><p>
This will invoke two procedures, with an intermediate data file named
anonymously connecting the <code class="literal">first</code> and
<code class="literal">second</code> procedures.
</p><p>
Ordering of execution is generally determined by execution of
<code class="literal">app</code> procedures, not by any containing compound procedures.
In this code block:
</p><pre class="programlisting">
 (file a, file b) A() {
   a = A1();
   b = A2();
 }
 file x, y, s, t;
 (x,y) = A();
 s = S(x);
 t = S(y);
</pre><p>
then a valid execution order is: <code class="literal">A1 S(x) A2 S(y)</code>. The
compound procedure <code class="literal">A</code> does not have to have fully completed
for its return values to be used by subsequent statements.
</p></div><div class="section" title="2.5. More about types"><div class="titlepage"><div><div><h3 class="title"><a name="id2827906"></a>2.5. More about types</h3></div></div></div><p>
Each variable and procedure parameter in SwiftScript is strongly typed.
Types are used to structure data, to aid in debugging and checking program
correctness and to influence how Swift interacts with data.
</p><p>
The <code class="literal">image</code> type declared in previous examples is a
<em class="firstterm">marker type</em>. Marker types indicate that data for a
variable is stored in a single file with no further structure exposed at
the SwiftScript level.
</p><p>
Arrays have been mentioned above, in the arrays section. A code block
may be applied to each element of an array using <code class="literal">foreach</code>;
or individual elements may be references using <code class="literal">[]</code> notation.
</p><p>There are a number of primitive types:</p><div class="table"><a name="id2827955"></a><p class="title"><b>Table 1. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">type</th><th align="left">contains</th></tr></thead><tbody><tr><td align="left">int</td><td align="left">integers</td></tr><tr><td align="left">string</td><td align="left">strings of text</td></tr><tr><td align="left">float</td><td align="left">floating point numbers, that behave the same as Java <code class="literal">double</code>s</td></tr><tr><td align="left">boolean</td><td align="left">true/false</td></tr></tbody></table></div></div><br class="table-break"><p>
Complex types may be defined using the <code class="literal">type</code> keyword:
</p><pre class="programlisting">
  type headerfile;
  type voxelfile;
  type volume {
    headerfile h;
    voxelfile v;
  }
</pre><p>
Members of a complex type can be accessed using the <code class="literal">.</code>
operator:
</p><pre class="programlisting">
  volume brain;
  o = p(brain.h);
</pre><p>
Sometimes data may be stored in a form that does not fit with Swift's
file-and-site model; for example, data might be stored in an RDBMS on some
database server. In that case, a variable can be declared to have
<em class="firstterm"><code class="literal">external</code></em> type. This indicates that
Swift should use the variable to determine execution dependency, but should
not attempt other data management; for example, it will not perform any form
of data stage-in or stage-out it will not manage local data caches on sites;
and it will not enforce component program atomicity on data output. This can
add substantial responsibility to component programs, in exchange for allowing
arbitrary data storage and access methods to be plugged in to scripts.
</p><pre class="programlisting">
  type file;

  app (external o) populateDatabase() {
    populationProgram;
  }

  app (file o) analyseDatabase(external i) {
    analysisProgram @o;
  }

  external database;
  file result &lt;"results.txt"&gt;;

  database = populateDatabase();
  result = analyseDatabase(database);
</pre><p>
Some external database is represented by the <code class="literal">database</code>
variable. The <code class="literal">populateDatabase</code> procedure populates the
database with some data, and the <code class="literal">analyseDatabase</code> procedure
performs some subsequent analysis on that database. The declaration of
<code class="literal">database</code> contains no mapping; and the procedures which
use <code class="literal">database</code> do not reference them in any way; the
description of <code class="literal">database</code> is entirely outside of the script.
The single assignment and execution ordering rules will still apply though;
<code class="literal">populateDatabase</code> will always be run before
<code class="literal">analyseDatabase</code>.
</p></div><div class="section" title="2.6. Data model"><div class="titlepage"><div><div><h3 class="title"><a name="id2888220"></a>2.6. Data model</h3></div></div></div><p>Data processed by Swift is strongly typed. It may be take the form
of values in memory or as out-of-core files on disk. Language constructs
called mappers specify how each piece of data is stored.</p><div class="section" title="2.6.1. Mappers"><div class="titlepage"><div><div><h4 class="title"><a name="id2888232"></a>2.6.1. Mappers</h4></div></div></div><p>
When a DSHandle represents a data file (or container of datafiles), it is
associated with a mapper. The mapper is used to
identify which files belong to that DSHandle.
		</p><p>
A dataset's physical representation is declared by a mapping descriptor, 
which defines how each element in the dataset's logical schema is 
stored in, and fetched from, physical structures such as directories,
files, and remote servers.
</p><p>
Mappers are parameterized to take into account properties such as
varying dataset location.
In order
to access a dataset, we need to know three things: its type, 
its mapping, and the value(s) of any parameter(s) associated 
with the mapping descriptor. For example, if we want to describe a dataset,
of type imagefile, and whose physical
representation is a file called "file1.bin" located at "/home/yongzh/data/",
then the dataset might be declared as follows:
</p><pre class="programlisting">
imagefile f1&lt;single_file_mapper;file="/home/yongzh/data/file1.bin"&gt;
</pre><p>
The above example declares a dataset called f1, which uses a single 
file mapper to map a file from a specific location.
</p><p>
SwiftScript has a simplified syntax for this case, since single_file_mapper 
is frequently used:

</p><pre class="programlisting">
binaryfile f1&lt;"/home/yongzh/data/file1.bin"&gt;
</pre><p>
</p><p>
Swift comes with a number of mappers that handle common mapping patterns.
These are documented in the <a class="link" href="#mappers" title="3. Mappers">mappers section</a>
of this guide.
</p></div></div><div class="section" title="2.7. More technical details about SwiftScript"><div class="titlepage"><div><div><h3 class="title"><a name="id2888303"></a>2.7. More technical details about SwiftScript</h3></div></div></div><p>The syntax of SwiftScript has a superficial resemblance to C and
Java. For example, { and } characters are used to enclose blocks of
statements.
</p><p>
A SwiftScript program consists of a number of statements.
Statements may declare types, procedures and variables, assign values to
variables, and express operations over arrays.
			</p><div class="section" title="2.7.1. Variables"><div class="titlepage"><div><div><h4 class="title"><a name="id2888321"></a>2.7.1. Variables</h4></div></div></div><p>Variables in SwiftScript are declared to be of a specific type.
Assignments to those variables must be data of that type.
SwiftScript variables are single-assignment - a value may be assigned
to a variable at most once. This assignment can happen at declaration time
or later on in execution. When an attempt to read from a variable
that has not yet been assigned is made, the code performing the read
is suspended until that variable has been written to. This forms the
basis for Swift's ability to parallelise execution - all code will
execute in parallel unless there are variables shared between the code
that cause sequencing.</p><div class="section" title="2.7.1.1. Variable Declarations"><div class="titlepage"><div><div><h5 class="title"><a name="id2888339"></a>2.7.1.1. Variable Declarations</h5></div></div></div><p>
Variable declaration statements declare new variables. They can
optionally assign a value to them or map those variables to on-disk files.
			</p><p>
Declaration statements have the general form:
</p><pre class="programlisting">
  typename variablename (&lt;mapping&gt; | = initialValue ) ;
</pre><p>
The format of the mapping expression is defined in the Mappers section.
initialValue may be either an expression or a procedure call that
returns a single value.
</p><p>Variables can also be declared in a multivalued-procedure statement,
described in another section.</p></div><div class="section" title="2.7.1.2. Assignment Statements"><div class="titlepage"><div><div><h5 class="title"><a name="id2888372"></a>2.7.1.2. Assignment Statements</h5></div></div></div><p>
Assignment statements assign values to previously declared variables.
Assignments may only be made to variables that have not already been 
assigned. Assignment statements have the general form:

</p><pre class="programlisting">
  variable = value;
</pre><p>
where value can be either an expression or a procedure call that returns
a single value.
			</p><p>
Variables can also be assigned in a multivalued-procedure statement,
described in another section.
			</p></div></div><div class="section" title="2.7.2. Procedures"><div class="titlepage"><div><div><h4 class="title"><a name="id2888399"></a>2.7.2. Procedures</h4></div></div></div><p>There are two kinds of procedure: An atomic procedure, which
describes how an external program can be executed; and compound
procedures which consist of a sequence of SwiftScript statements.
</p><p>
A procedure declaration defines the name of a procedure and its
input and output parameters. SwiftScript procedures can take multiple
inputs and produce multiple outputs.  Inputs are specified to the right
of the function name, and outputs are specified to the left. For example:

</p><pre class="programlisting">
(type3 out1, type4 out2) myproc (type1 in1, type2 in2)
</pre><p>

The above example declares a procedure called <code class="literal">myproc</code>, which 
has two inputs <code class="literal">in1</code> (of type <code class="literal">type1</code>)
and <code class="literal">in2</code> (of type <code class="literal">type2</code>) 
and two outputs <code class="literal">out1</code> (of type <code class="literal">type3</code>)
and <code class="literal">out2</code> (of type <code class="literal">type4</code>).
			</p><p>
A procedure input parameter can be an <em class="firstterm">optional
parameter</em> in which case it must be declared with a default
value.  When calling a procedure, both positional parameter and named
parameter passings can be passed, provided that all optional
parameters are declared after the required parameters and any
optional parameter is bound using keyword parameter passing.
For example, if <code class="literal">myproc1</code> is defined as:

</p><pre class="programlisting">
(binaryfile bf) myproc1 (int i, string s="foo")
</pre><p>

Then that procedure can be called like this, omitting the optional
parameter <code class="literal">s</code>:

</p><pre class="programlisting">
binaryfile mybf = myproc1(1);
</pre><p>

or like this supplying a value for the optional parameter
<code class="literal">s</code>:

</p><pre class="programlisting">
binaryfile mybf = myproc1 (1, s="bar");
</pre><p>

			</p><div class="section" title="2.7.2.1. Atomic procedures"><div class="titlepage"><div><div><h5 class="title"><a name="procedures.atomic"></a>2.7.2.1. Atomic procedures</h5></div></div></div><p>
An atomic procedure specifies how to invoke an
external executable program, and how logical data
types are mapped to command line arguments.
			</p><p>
Atomic procedures are defined with the <code class="literal">app</code> keyword:
</p><pre class="programlisting">
app (binaryfile bf) myproc (int i, string s="foo") {
	myapp i s @filename(bf);
}			
</pre><p>

which specifies that <code class="literal">myproc</code> invokes an executable
called <code class="literal">myapp</code>,
passing the values of <code class="literal">i</code>, <code class="literal">s</code>
and the filename of <code class="literal">bf</code> as command line arguments.
			</p></div><div class="section" title="2.7.2.2. Compound procedures"><div class="titlepage"><div><div><h5 class="title"><a name="procedures.compound"></a>2.7.2.2. Compound procedures</h5></div></div></div><p>
A compound procedure contains a set of SwiftScript statements:

</p><pre class="programlisting">
(type2 b) foo_bar (type1 a) {
	type3 c;
	c = foo(a);    // c holds the result of foo
	b = bar(c);    // c is an input to bar
}
</pre><p>
		</p></div></div><div class="section" title="2.7.3. Control Constructs"><div class="titlepage"><div><div><h4 class="title"><a name="id2888618"></a>2.7.3. Control Constructs</h4></div></div></div><p>
SwiftScript provides <code class="literal">if</code>, <code class="literal">switch</code>,
<code class="literal">foreach</code>, and <code class="literal">iterate</code> constructs,
with syntax and semantics similar to comparable constructs in
other high-level languages.
			</p><div class="section" title="2.7.3.1. foreach"><div class="titlepage"><div><div><h5 class="title"><a name="id2888653"></a>2.7.3.1. foreach</h5></div></div></div><p>
The <code class="literal">foreach</code> construct is used to apply a block of statements to
each element in an array. For example:

</p><pre class="programlisting">
check_order (file a[]) {
	foreach f in a {
		compute(f);
	}
}
</pre><p>
</p><p>
<code class="literal">foreach</code> statements have the general form:

</p><pre class="programlisting">
foreach controlvariable (,index) in expression {
    statements
}
</pre><p>

The block of statements is evaluated once for each element in
<code class="literal">expression</code> which must be an array,
with <code class="literal">controlvariable</code> set to the corresponding element
and <code class="literal">index</code> (if specified) set to the
integer position in the array that is being iterated over.

			</p></div><div class="section" title="2.7.3.2. if"><div class="titlepage"><div><div><h5 class="title"><a name="id2888714"></a>2.7.3.2. if</h5></div></div></div><p>
The <code class="literal">if</code> statement allows one of two blocks of statements to be
executed, based on a boolean predicate. <code class="literal">if</code> statements generally
have the form:
</p><pre class="programlisting">
if(predicate) {
    statements
} else {
    statements
}
</pre><p>

where <code class="literal">predicate</code> is a boolean expression.
			</p></div><div class="section" title="2.7.3.3. switch"><div class="titlepage"><div><div><h5 class="title"><a name="id2888750"></a>2.7.3.3. switch</h5></div></div></div><p>
<code class="literal">switch</code> expressions allow one of a selection of blocks to be chosen based on
the value of a numerical control expression. <code class="literal">switch</code> statements take the
general form:
</p><pre class="programlisting">
switch(controlExpression) {
    case n1:
        statements2
    case n2:
        statements2
    [...]
    default:
        statements
}
</pre><p>
The control expression is evaluated, the resulting numerical value used to
select a corresponding <code class="literal">case</code>, and the statements belonging to that
<code class="literal">case</code> block
are evaluated. If no case corresponds, then the statements belonging to
the <code class="literal">default</code> block are evaluated.
			</p><p>Unlike C or Java switch statements, execution does not fall through to
subsequent <code class="literal">case</code> blocks, and no <code class="literal">break</code>
statement is necessary at the end of each block.
</p></div><div class="section" title="2.7.3.4. iterate"><div class="titlepage"><div><div><h5 class="title"><a name="construct.iterate"></a>2.7.3.4. iterate</h5></div></div></div><p>
<code class="literal">iterate</code> expressions allow a block of code to be evaluated repeatedly, with an
integer parameter sweeping upwards from 0 until a termination condition
holds.
				</p><p>
The general form is:
</p><pre class="programlisting">
iterate var {
	statements;
} until (terminationExpression);
</pre><p>
with the variable <code class="literal">var</code> starting at 0 and increasing
by one in each iteration. That
variable is in scope in the statements block and when evaluating the
termination expression.
				</p></div></div></div><div class="section" title="2.8. Operators"><div class="titlepage"><div><div><h3 class="title"><a name="id2888863"></a>2.8. Operators</h3></div></div></div><p>The following infix operators are available for use in
SwiftScript expressions.
</p><div class="table"><a name="id2888872"></a><p class="title"><b>Table 2. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">operator</th><th align="left">purpose</th></tr></thead><tbody><tr><td align="left">+</td><td align="left">numeric addition; string concatenation</td></tr><tr><td align="left">-</td><td align="left">numeric subtraction</td></tr><tr><td align="left">*</td><td align="left">numeric multiplication</td></tr><tr><td align="left">/</td><td align="left">floating point division</td></tr><tr><td align="left">%/</td><td align="left">integer division</td></tr><tr><td align="left">%%</td><td align="left">integer remainder of division</td></tr><tr><td align="left">== !=</td><td align="left">comparison and not-equal-to</td></tr><tr><td align="left"> &lt; &gt; &lt;= &gt;=</td><td align="left">numerical ordering</td></tr><tr><td align="left">&amp;&amp; ||</td><td align="left">boolean and, or</td></tr><tr><td align="left">!</td><td align="left">boolean not</td></tr></tbody></table></div></div><br class="table-break"></div><div class="section" title="2.9. Global constants"><div class="titlepage"><div><div><h3 class="title"><a name="globals"></a>2.9. Global constants</h3></div></div></div><p>
At the top level of a SwiftScript program, the <code class="literal">global</code>
modified may be added to a declaration so that it is visible throughout
the program, rather than only at the top level of the program. This allows
global constants (of any type) to be defined. (since Swift 0.10)
		</p></div><div class="section" title="2.10. Imports"><div class="titlepage"><div><div><h3 class="title"><a name="imports"></a>2.10. Imports</h3></div></div></div><p>
The <code class="literal">import</code> directive can be used to import definitions from
another SwiftScript file. (since Swift 0.10)
		</p><p>
For example, a SwiftScript program might contain this:
			</p><pre class="programlisting">
import defs;
file f;
			</pre><p>
which would import the content of <code class="filename">defs.swift</code> in the
current directory:
			</p><pre class="programlisting">
type file;
			</pre><p>
		</p><p>
Imported files are read from the current working directory.
		</p><p>
There is no requirement that a module is imported only once. If a module
is imported multiple times, for example in different files, then Swift will
only process the imports once.
		</p><p>
Imports may contain anything that is valid in a SwiftScript program,
including code that causes remote execution.
		</p></div></div><div class="section" title="3. Mappers"><div class="titlepage"><div><div><h2 class="title"><a name="mappers"></a>3. Mappers</h2></div></div></div><p>
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
</p><div class="table"><a name="id2889114"></a><p class="title"><b>Table 3. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">file</td><td align="left">The location of the physical file including path and file name.</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
			</p><pre class="programlisting">
	file f &lt;single_file_mapper;file="plot_outfile_param"&gt;;</pre><p>

There is a simplified syntax for this mapper:


			</p><pre class="programlisting">
	file f &lt;"plot_outfile_param"&gt;;</pre><p>
</p></div><div class="section" title="3.2. The simple mapper"><div class="titlepage"><div><div><h3 class="title"><a name="mapper.simple_mapper"></a>3.2. The simple mapper</h3></div></div></div><p>The <code class="literal">simple_mapper</code> maps a file or a list of files
into an array by prefix, suffix, and pattern.  If more than one file is
matched, each of the file names will be mapped as a subelement of the dataset.
</p><div class="table"><a name="id2889197"></a><p class="title"><b>Table 4. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">A directory that the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
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
</p><div class="table"><a name="id2889462"></a><p class="title"><b>Table 5. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">A directory that the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
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
instead of location="/sandbox/..."</p><div class="table"><a name="id2889620"></a><p class="title"><b>Table 6. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">location</td><td align="left">The directory where the files are located.</td></tr><tr><td align="left">prefix</td><td align="left">The prefix of the files</td></tr><tr><td align="left">suffix</td><td align="left">The suffix of the files, for instance: <code class="literal">".txt"</code></td></tr><tr><td align="left">pattern</td><td align="left">A UNIX glob style pattern, for instance:
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
contains a list of filenames into a file array.</p><div class="table"><a name="id2889784"></a><p class="title"><b>Table 7. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">files</td><td align="left">A string that contains a list of filenames, separated by space, comma or colon</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
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
into a file</p><div class="table"><a name="id2889872"></a><p class="title"><b>Table 8. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">files</td><td align="left">An array of strings containing one filename per element</td></tr></tbody></table></div></div><br class="table-break"><p> Example:
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
another using regular expression matching.</p><div class="table"><a name="id2889959"></a><p class="title"><b>Table 9. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">source</td><td align="left">The source file name</td></tr><tr><td align="left">match</td><td align="left">Regular expression pattern to match, use
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

</p></div><div class="section" title="3.8. csv mapper"><div class="titlepage"><div><div><h3 class="title"><a name="id2890074"></a>3.8. csv mapper</h3></div></div></div><p>
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
</p><div class="table"><a name="id2890118"></a><p class="title"><b>Table 10. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">file</td><td align="left">The name of the CSV file to read mappings from.</td></tr><tr><td align="left">header</td><td align="left">Whether the file has a line describing header info; default is <code class="literal">true</code></td></tr><tr><td align="left">skip</td><td align="left">The number of lines to skip at the beginning (after header line); default is <code class="literal">0</code>.</td></tr><tr><td align="left">hdelim</td><td align="left">Header field delimiter; default is the value of the <code class="literal">delim</code> parameter</td></tr><tr><td align="left">delim</td><td align="left">Content field delimiters; defaults are space, tab and comma</td></tr></tbody></table></div></div><br class="table-break"><p>Example:
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
		</p><div class="table"><a name="id2890287"></a><p class="title"><b>Table 11. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">parameter</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">exec</td><td align="left">The name of the executable
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

		</p></div></div><div class="section" title="4. Commands"><div class="titlepage"><div><div><h2 class="title"><a name="commands"></a>4. Commands</h2></div></div></div><p>
The commands detailed in this section are available in the
<code class="filename">bin/</code> directory of a Swift installation and can
by run from the commandline if that directory is placed on the
PATH.
		</p><div class="section" title="4.1. swift"><div class="titlepage"><div><div><h3 class="title"><a name="swiftcommand"></a>4.1. swift</h3></div></div></div><p>
The <span class="command"><strong>swift</strong></span> command is the main command line tool
for executing SwiftScript programs.
	</p><div class="section" title="4.1.1. Command-line Syntax"><div class="titlepage"><div><div><h4 class="title"><a name="id2890443"></a>4.1.1. Command-line Syntax</h4></div></div></div><p>The <span class="command"><strong>swift</strong></span> command is invoked as follows:
<span class="command"><strong>swift [options] SwiftScript-program [SwiftScript-arguments]</strong></span>
with options taken from the following list, and SwiftScript-arguments
made available to the SwiftScript program through the
<a class="link" href="#function.arg" title="8.1. @arg">@arg</a> function.
</p><div class="variablelist" title="Swift command-line options"><p class="title"><b>Swift command-line options</b></p><dl><dt><span class="term">-help or -h</span></dt><dd><p>
      Display usage information </p></dd><dt><span class="term">-typecheck</span></dt><dd><p>
      Does a typecheck of a SwiftScript program, instead of executing it.</p></dd><dt><span class="term">-dryrun</span></dt><dd><p>
      Runs the SwiftScript program without submitting any jobs (can be used to get
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
      Swift uses the name of the program being run and a numeric index
      (e.g. myworkflow.1.log) 
    </p></dd><dt><span class="term">-runid <code class="literal">identifier</code></span></dt><dd><p>
      Specifies the run identifier. This must be unique for every invocation
      and is used in several places to keep files from different executions
      cleanly separated. By default, a datestamp and random number are used
      to generate a run identifier. When using this parameter, care should be
      taken to ensure that the run ID remains unique with respect to all
      other run IDs that might be used, irrespective of (at least) expected
      execution sites, program or user.
    </p></dd><dt><span class="term">-tui</span></dt><dd>
      Displays an interactive text mode monitor during a run. (since Swift 0.9)
    </dd></dl></div><p>In addition, the following Swift properties can be set on the
command line:

</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">caching.algorithm</li><li class="listitem">clustering.enabled</li><li class="listitem">clustering.min.time</li><li class="listitem">clustering.queue.delay</li><li class="listitem">ip.address</li><li class="listitem">kickstart.always.transfer</li><li class="listitem">kickstart.enabled</li><li class="listitem">lazy.errors</li><li class="listitem">pgraph</li><li class="listitem">pgraph.graph.options</li><li class="listitem">pgraph.node.options</li><li class="listitem">sitedir.keep</li><li class="listitem">sites.file</li><li class="listitem">tc.file</li><li class="listitem">tcp.port.range</li></ul></div><p>
</p></div><div class="section" title="4.1.2. Return codes"><div class="titlepage"><div><div><h4 class="title"><a name="id2890697"></a>4.1.2. Return codes</h4></div></div></div><p>
The <span class="command"><strong>swift</strong></span> command may exit with the following return codes:
</p><div class="table"><a name="id2890711"></a><p class="title"><b>Table 12. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">value</th><th align="left">meaning</th></tr></thead><tbody><tr><td align="left">0</td><td align="left">success</td></tr><tr><td align="left">1</td><td align="left">command line syntax error or missing project name</td></tr><tr><td align="left">2</td><td align="left">error during execution</td></tr><tr><td align="left">3</td><td align="left">error during compilation</td></tr><tr><td align="left">4</td><td align="left">input file does not exist</td></tr></tbody></table></div></div><p><br class="table-break">
	</p></div><div class="section" title="4.1.3. Environment variables"><div class="titlepage"><div><div><h4 class="title"><a name="id2890783"></a>4.1.3. Environment variables</h4></div></div></div><p>The <span class="command"><strong>swift</strong></span> is influenced by the
following environment variables:
		</p><p>
<code class="literal">GLOBUS_HOSTNAME</code>, <code class="literal">GLOBUS_TCP_PORT_RANGE</code> - set in the environment before running
Swift. These can be set to inform Swift of the
configuration of your local firewall. More information can be found in
<a class="ulink" href="http://dev.globus.org/wiki/FirewallHowTo" target="_top">the Globus firewall
How-to</a>.
		</p><p>
<code class="literal">COG_OPTS</code> - set in the environment before running Swift. Options set in this
variable will be passed as parameters to the Java Virtual Machine which
will run Swift. The parameters vary between virtual machine imlementations,
but can usually be used to alter settings such as maximum heap size.
Typing 'java -help' will sometimes give a list of commands. The Sun Java
1.4.2 command line options are <a class="ulink" href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/java.html" target="_top">documented here</a>.
		</p></div></div><div class="section" title="4.2. swift-osg-ress-site-catalog"><div class="titlepage"><div><div><h3 class="title"><a name="id2890846"></a>4.2. swift-osg-ress-site-catalog</h3></div></div></div><p>
The <span class="command"><strong>swift-osg-ress-site-catalog</strong></span> command generates a site
catalog based on <a class="ulink" href="http://www.opensciencegrid.org/" target="_top">OSG</a>'s
ReSS information system (since Swift 0.9)
			</p><p>
Usage: <span class="command"><strong>swift-osg-ress-site-catalog [options]</strong></span>
			</p><div class="variablelist"><dl><dt><span class="term">--help</span></dt><dd><p>Show help message</p></dd><dt><span class="term">--vo=[name]</span></dt><dd><p>Set what VO to query ReSS for</p></dd><dt><span class="term">--engage-verified</span></dt><dd><p>Only retrieve sites verified by the Engagement VO site
verification tests This can not be used together with <code class="literal">--vo</code>,
as the query will only work for sites advertising support for the
Engagement VO.</p><p>This option means information will be retrieved from the
Engagement collector instead of the top-level ReSS collector.</p></dd><dt><span class="term">--out=[filename]</span></dt><dd><p>Write to [filename] instead of stdout</p></dd><dt><span class="term">--condor-g</span></dt><dd><p>Generates sites files which will submit jobs using a local Condor-G
installation rather than through direct GRAM2 submission. (since Swift 0.10)</p></dd></dl></div></div><div class="section" title="4.3. swift-plot-log"><div class="titlepage"><div><div><h3 class="title"><a name="id2890962"></a>4.3. swift-plot-log</h3></div></div></div><p>
<span class="command"><strong>swift-plot-log</strong></span> generates summaries of Swift run log
files.
		</p><p>
Usage: <span class="command"><strong>swift-plot-log [logfile] [targets]</strong></span>
		</p><p>
When no targets are specified, <span class="command"><strong>swift-plog-log</strong></span> will
generate an HTML report for the run. When targets are specified, only
those named targets will be generated.
		</p></div></div><div class="section" title="5. Executing app procedures"><div class="titlepage"><div><div><h2 class="title"><a name="appmodel"></a>5. Executing <code class="literal">app</code> procedures</h2></div></div></div><p>
This section describes how Swift executes <code class="literal">app</code> procedures,
and requirements on the behaviour of application programs used in
<code class="literal">app</code> procedures.
These requirements are primarily to ensure
that the Swift can run your application in different places and with the
various fault tolerance mechanisms in place.
	</p><div class="section" title="5.1. Mapping of app semantics into unix process execution semantics"><div class="titlepage"><div><div><h3 class="title"><a name="id2891034"></a>5.1. Mapping of <code class="literal">app</code> semantics into unix
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
</p></li></ul></div></div><div class="section" title="5.2.  How Swift implements the site execution model"><div class="titlepage"><div><div><h3 class="title"><a name="id2891322"></a>5.2. 
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
</p></div><img src="swift-site-model.png"></div><div class="section" title="6. Technical overview of the Swift architecture"><div class="titlepage"><div><div><h2 class="title"><a name="techoverview"></a>6. Technical overview of the Swift architecture</h2></div></div></div><p>
This section attempts to provide a technical overview of the Swift
architecture.
	</p><div class="section" title="6.1. karajan - the core execution engine"><div class="titlepage"><div><div><h3 class="title"><a name="id2891557"></a>6.1. karajan - the core execution engine</h3></div></div></div></div><div class="section" title="6.2. Execution layer"><div class="titlepage"><div><div><h3 class="title"><a name="id2891563"></a>6.2. Execution layer</h3></div></div></div><p>
The execution layer causes an application program (in the form of a unix
executable) to be executed either locally or remotely.
	</p><p>
The two main choices are local unix execution and execution through GRAM.
Other options are available, and user provided code can also be plugged in.
	</p><p>
The <a class="link" href="#kickstart" title="15. Kickstart">kickstart</a> utility can
be used to capture environmental information at execution time
to aid in debugging and provenance capture. 
	</p></div><div class="section" title="6.3. SwiftScript language compilation layer"><div class="titlepage"><div><div><h3 class="title"><a name="id2891593"></a>6.3. SwiftScript language compilation layer</h3></div></div></div><p>
Step i: text to XML intermediate form parser/processor. parser written in
ANTLR - see resources/VDL.g. The XML Schema Definition (XSD) for the
intermediate language is in resources/XDTM.xsd.
	</p><p>
Step ii: XML intermediate form to Karajan workflow. Karajan.java - reads
the XML intermediate form. compiles to karajan workflow language - for
example, expressions are converted from SwiftScript syntax into Karajan
syntax, and function invocations become karajan function invocations
with various modifications to parameters to accomodate return parameters
and dataset handling.
	</p></div><div class="section" title="6.4. Swift/karajan library layer"><div class="titlepage"><div><div><h3 class="title"><a name="id2891615"></a>6.4. Swift/karajan library layer</h3></div></div></div><p>
Some Swift functionality is provided in the form of Karajan libraries
that are used at runtime by the Karajan workflows that the Swift
compiler generates.
	</p></div></div><div class="section" title="7. Ways in which Swift can be extended"><div class="titlepage"><div><div><h2 class="title"><a name="extending"></a>7. Ways in which Swift can be extended</h2></div></div></div><p>Swift is extensible in a number of ways. It is possible to add
mappers to accomodate different filesystem arrangements, site selectors
to change how Swift decides where to run each job, and job submission
interfaces to submit jobs through different mechanisms.
</p><p>A number of mappers are provided as part of the Swift release and
documented in the <a class="link" href="#mappers" title="3. Mappers">mappers</a> section.
New mappers can be implemented
in Java by implementing the org.griphyn.vdl.mapping.Mapper interface. The
<a class="ulink" href="http://www.ci.uchicago.edu/swift/guides/tutorial.php" target="_top">Swift
tutorial</a> contains a simple example of this.
</p><p>Swift provides a default site selector, the Adaptive Scheduler.
New site selectors can be plugged in by implementing the
org.globus.cog.karajan.scheduler.Scheduler interface and modifying
libexec/scheduler.xml and etc/karajan.properties to refer to the new
scheduler.
</p><p>Execution providers and filesystem providers, which allow to Swift
to execute jobs and to stage files in and out through mechanisms such
as GRAM and GridFTP can be implemented as Java CoG kit providers.
</p></div><div class="section" title="8. Function reference"><div class="titlepage"><div><div><h2 class="title"><a name="functions"></a>8. Function reference</h2></div></div></div><p>
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
<a class="link" href="#function.filename" title="8.3. @filename">@filename</a>)
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
			</p></div></div><div class="section" title="9. Built-in procedure reference"><div class="titlepage"><div><div><h2 class="title"><a name="procedures"></a>9. Built-in procedure reference</h2></div></div></div><p>
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
			</p></div></div><div class="section" title="10. Swift configuration properties"><div class="titlepage"><div><div><h2 class="title"><a name="engineconfiguration"></a>10. Swift configuration properties</h2></div></div></div><p>
		
			Various aspects of the behavior of the Swift Engine can be
			configured through properties. The Swift Engine recognizes a global,
			per installation properties file which can found in <code class="filename">etc/swift.properties</code> in the Swift installation directory and a user
			properties file which can be created by each user in <code class="filename">~/.swift/swift.properties</code>. The Swift Engine
			will first load the global properties file. It will then try to load
			the user properties file. If a user properties file is found,
			individual properties explicitly set in that file will override the
			respective properties in the global properties file. Furthermore,
			some of the properties can be overridden directly using command line
			arguments to the <a class="link" href="#swiftcommand" title="4.1. swift"><span class="command"><strong>swift</strong></span> command</a>.
		
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
								<a class="link" href="#profile.swift.storagesize"><span class="property">swift:storagesize</span></a> profile
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
								Enables <a class="link" href="#clustering" title="17. Clustering">clustering</a>.
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
								when Swift uses <a class="link" href="#kickstart" title="15. Kickstart">Kickstart</a>. A value of
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
			
			</p></div><div class="section" title="11. Profiles"><div class="titlepage"><div><div><h2 class="title"><a name="profiles"></a>11. Profiles</h2></div></div></div><p>
Profiles are configuration parameters than can be specified either for
sites or for transformation catalog entries. They influence the behaviour
of Swift towards that site (for example, by changing the load Swift will
place on that sites) or when running a particular procedure.
		</p><p>
Profile entries for a site are specified in the site catalog. Profile
entries for specific procedures are specified in the transformation
catalog.
		</p><div class="section" title="11.1. Karajan namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.karajan"></a>11.1. Karajan namespace</h3></div></div></div><p><a name="profile.karajan.maxsubmitrate"></a><code class="literal">maxSubmitRate</code> limits the maximum rate of job submission, in jobs per second.
For example:
</p><pre class="screen">
&lt;profile namespace="karajan" key="maxSubmitRate"&gt;0.2&lt;/profile&gt;
</pre><p>
will limit job submission to 0.2 jobs per second (or equivalently,
one job every five seconds).
			</p><p><a name="profile.karajan.jobThrottle"></a><code class="literal">jobThrottle</code>
allows the job throttle factor (see Swift property <a class="link" href="#property.throttle.score.job.factor">throttle.score.job.factor</a>) to be set per site.
			</p><p><a name="profile.karajan.initialScore"></a><code class="literal">initialScore</code>
allows the initial score for rate limiting and site selection to be set to
a value other than 0.
			</p><p><a name="profile.karajan.delayBase"></a><code class="literal">delayBase</code> controls how much a site will be delayed when it performs poorly. With each reduction
in a sites score by 1, the delay between execution attempts will increase by
a factor of delayBase.</p><p><a name="profile.karajan.status.mode"></a><code class="literal">status.mode</code> allows the status.mode property to be set per-site instead of for an entire run.
See the Swift configuration properties section for more information.
(since Swift 0.8)</p></div><div class="section" title="11.2. swift namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.swift"></a>11.2. swift namespace</h3></div></div></div><p><a name="profile.swift.storagesize"></a><code class="literal">storagesize</code> limits the
amount of space that will be used on the remote site for temporary files.
When more than that amount of space is used, the remote temporary file
cache will be cleared using the algorithm specified in the
<a class="link" href="#property.caching.algorithm"><code class="literal">caching.algorithm</code></a> property.
			</p><p><a name="swift.wrapperInterpreter"></a><code class="literal">wrapperInterpreter</code>
The wrapper interpreter indicates the command (executable) to be used to run the Swift wrapper
script. The default is "/bin/bash" on Unix sites and "cscript.exe" on Windows sites.
			</p><p><a name="swift.wrapperInterpreterOptions"></a><code class="literal">wrapperInterpreterOptions</code>
Allows specifying additional options to the executable used to run the Swift wrapper. The defaults
are no options on Unix sites and "//Nologo" on Windows sites.
			</p><p><a name="swift.wrapperScript"></a><code class="literal">wrapperScript</code>
Specifies the name of the wrapper script to be used on a site. The defaults are "_swiftwrap" on 
Unix sites and "_swiftwrap.vbs" on Windows sites. If you specify a custom wrapper script, it 
must be present in the "libexec" directory of the Swift installation.
			</p><p><a name="swift.cleanupCommand"></a><code class="literal">cleanupCommand</code>
Indicates the command to be run at the end of a Swift run to clean up the run directories on a 
remote site. Defaults are "/bin/rm" on Unix sites and "cmd.exe" on Windows sites
			</p><p><a name="swift.cleanupCommandOptions"></a><code class="literal">cleanupCommandOptions</code>
Specifies the options to be passed to the cleanup command above. The options are passed in the
argument list to the cleanup command. After the options, the last argument is the directory
to be deleted. The default on Unix sites is "-rf". The default on Windows sites is ["/C", "del", "/Q"].
			</p></div><div class="section" title="11.3. Globus namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.globus"></a>11.3. Globus namespace</h3></div></div></div><p><a name="profile.globus.maxwalltime"></a><code class="literal">maxwalltime</code> specifies a walltime limit for each job, in minutes.
			</p><p>
The following formats are recognized:
				</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">Minutes</li><li class="listitem">Hours:Minutes</li><li class="listitem">Hours:Minutes:Seconds</li></ul></div><p>
			</p><p>Example:</p><pre class="screen">
localhost	echo	/bin/echo	INSTALLED	INTEL32::LINUX	GLOBUS::maxwalltime="00:20:00"
</pre><p>When replication is enabled (see <a class="link" href="#replication" title="16.3. Replication">replication</a>), then walltime will also be enforced at the Swift client side: when
a job has been active for more than twice the maxwalltime, Swift will kill the
job and regard it as failed.
			</p><p>
When clustering is used, <code class="literal">maxwalltime</code> will be used to
select which jobs will be clustered together. More information on this is
available in the <a class="link" href="#clustering" title="17. Clustering">clustering section</a>.
			</p><p>
When coasters as used, <code class="literal">maxwalltime</code> influences the default
coaster worker maxwalltime, and which jobs will be sent to which workers.
More information on this is available in the <a class="link" href="#coasters" title="18. Coasters">coasters
section</a>.
			</p><p><a name="profile.globus.queue"></a><code class="literal">queue</code> 
is used by the PBS, GRAM2 and GRAM4 providers. This profile
entry specifies which queue jobs will be submitted to. The valid queue names
are site-specific.
			</p><p><a name="profile.globus.host_types"></a><code class="literal">host_types</code>
specifies the types of host that are permissible for a job to run on.
The valid values are site-specific. This profile entry is used by the
GRAM2 and GRAM4 providers.
			</p><p><a name="profile.globus.condor_requirements"></a><code class="literal">condor_requirements</code> allows a requirements string to be specified
when Condor is used as an LRM behind GRAM2. Example: <code class="literal">&lt;profile namespace="globus" key="condor_requirements"&gt;Arch == "X86_64" || Arch="INTEL"&lt;/profile&gt;</code>
			</p><p><a name="profile.slots"></a><code class="literal">slots</code>
When using <a class="link" href="#coasters" title="18. Coasters">coasters</a>, this parameter
specifies the maximum number of jobs/blocks that the coaster scheduler will have running at any given time.
The default is 20.
			</p><p><a name="profile.workersPerNode"></a><code class="literal">workersPerNode</code>
This parameter determines how many coaster workers are 
started one each compute node. The default value is 1.
			</p><p><a name="profile.nodeGranularity"></a><code class="literal">nodeGranularity</code>
When allocating a coaster worker block, this parameter
restricts the number of nodes in a block to a multiple of this value. The total number of workers will
then be a multiple of workersPerNode * nodeGranularity. The default value is 1.
			</p><p><a name="profile.allocationStepSize"></a><code class="literal">allocationStepSize</code>
Each time the coaster block scheduler computes a schedule, it will attempt to allocate a
number of slots from the number of available slots (limited using the above slots profile). This
parameter specifies the maximum fraction of slots that are allocated in one schedule. Default is
0.1.
			</p><p><a name="profile.lowOverallocation"></a><code class="literal">lowOverallocation</code>
Overallocation is a function of the walltime of a job which determines how long (time-wise) a
worker job will be. For example, if a number of 10s jobs are submitted to the coaster service, 
and the overallocation for 10s jobs is 10, the coaster scheduler will attempt to start worker
jobs that have a walltime of 100s. The overallocation is controlled by manipulating the end-points
of an overallocation function. The low endpoint, specified by this parameter, is the overallocation
for a 1s job. The high endpoint is the overallocation for a (theoretical) job of infinite length.
The overallocation for job sizes in the [1s, +inf) interval is determined using an exponential decay function:

overallocation(walltime) = walltime * (lowOverallocation - highOverallocation) * exp(-walltime * overallocationDecayFactor) + highOverallocation

The default value of lowOverallocation is 10.
			</p><p><a name="profile.highOverallocation"></a><code class="literal">highOverallocation</code>
The high overallocation endpoint (as described above). Default: 1
			</p><p><a name="profile.overallocationDecayFactor"></a><code class="literal">overallocationDecayFactor</code>
The decay factor for the overallocation curve. Default 0.001 (1e-3).
			</p><p><a name="profile.spread"></a><code class="literal">spread</code>
When a large number of jobs is submitted to the a coaster service, the work is divided into blocks. This
parameter allows a rough control of the relative sizes of those blocks. A value of 0 indicates that all work
should be divided equally between the blocks (and blocks will therefore have equal sizes). A value of 1 
indicates the largest possible spread. The existence of the spread parameter is based on the assumption
that smaller overall jobs will generally spend less time in the queue than larger jobs. By submitting
blocks of different sizes, submitted jobs may be finished quicker by smaller blocks. Default: 0.9.
			</p><p><a name="profile.reserve"></a><code class="literal">reserve</code>
Reserve time is a time in the allocation of a worker that sits at the end of the worker time and 
is useable only for critical operations. For example, a job will not be submitted to a worker if 
it overlaps its reserve time, but a job that (due to inaccurate walltime specification) runs into
the reserve time will not be killed (note that once the worker exceeds its walltime, the queuing 
system will kill the job anyway). Default 10 (s).
			</p><p><a name="profile.maxnodes"></a><code class="literal">maxnodes</code>
Determines the maximum number of nodes that can be allocated in one coaster block. Default: unlimited.
			</p><p><a name="profile.maxtime"></a><code class="literal">maxtime</code>
Indicates the maximum walltime that a coaster block can have. Default: unlimited.
			</p><p><a name="profile.remoteMonitorEnabled"></a><code class="literal">remoteMonitorEnabled</code>
If set to "true", the client side will get a Swing window showing, graphically, the state of the
coaster scheduler (blocks, jobs, etc.). Default: false
			</p></div><div class="section" title="11.4. env namespace"><div class="titlepage"><div><div><h3 class="title"><a name="profile.env"></a>11.4. env namespace</h3></div></div></div><p>
Profile keys set in the env namespace will be set in the unix environment of the
executed job. Some environment variables influence the worker-side
behaviour of Swift:
			</p><p>
<code class="literal">PATHPREFIX</code> - set in env namespace profiles. This path is prefixed onto the start
of the <code class="literal">PATH</code> when jobs are
executed. It can be more useful than setting the <code class="literal">PATH</code> environment variable directly,
because setting <code class="literal">PATH</code> will cause the execution site's default path to be lost.
			</p><p>
<code class="literal">SWIFT_JOBDIR_PATH</code> - set in env namespace profiles. If set, then Swift will
use the path specified here as a worker-node local temporary directory to
copy input files to before running a job. If unset, Swift will keep input
files on the site-shared filesystem. In some cases, copying to a worker-node
local directory can be much faster than having applications access the
site-shared filesystem directly.
			</p><p>
<code class="literal">SWIFT_EXTRA_INFO</code> - set in env namespace profiles. If set,
then Swift will execute the command specified in
<code class="literal">SWIFT_EXTRA_INFO</code> on execution sites immediately before
each application execution, and will record the stdout of that command in the
wrapper info log file for that job. This is intended to allow software
version and other arbitrary information about the remote site to be gathered
and returned to the submit side. (since Swift 0.9)
			</p></div></div><div class="section" title="12. The Site Catalog - sites.xml"><div class="titlepage"><div><div><h2 class="title"><a name="sitecatalog"></a>12. The Site Catalog - sites.xml</h2></div></div></div><p>
The site catalog lists details of each site that Swift can use. The default
file contains one entry for local execution, and a large number of
commented-out example entries for other sites.
		</p><p>
By default, the site catalog is stored in <code class="filename">etc/sites.xml</code>.
This path can be overridden with the <code class="literal">sites.file</code> configuration property,
either in the Swift configuration file or on the command line.
		</p><p>
The sites file is formatted as XML. It consists of <code class="literal">&lt;pool&gt;</code> elements,
one for each site that Swift will use.
		</p><div class="section" title="12.1. Pool element"><div class="titlepage"><div><div><h3 class="title"><a name="id2894804"></a>12.1. Pool element</h3></div></div></div><p>
Each <code class="literal">pool</code> element must have a <code class="literal">handle</code> attribute, giving a symbolic name
for the site. This can be any name, but must correspond to entries for
that site in the transformation catalog.
		</p><p>
Optionally, the <code class="literal">gridlaunch</code> attribute can be used to specify the path to
<a class="link" href="#kickstart" title="15. Kickstart">kickstart</a> on the site.
		</p><p>
Each <code class="literal">pool</code> must specify a file transfer method, an execution method
and a remote working directory. Optionally, <a class="link" href="#profiles" title="11. Profiles">profile settings</a> can be specified.
		</p></div><div class="section" title="12.2. File transfer method"><div class="titlepage"><div><div><h3 class="title"><a name="id2894864"></a>12.2. File transfer method</h3></div></div></div><p>
Transfer methods are specified with either 
the <code class="literal">&lt;gridftp&gt;</code> element or the 
<code class="literal">&lt;filesystem&gt;</code> element.
		</p><p>
To use gridftp or local filesystem copy, use the <code class="literal">&lt;gridftp&gt;</code>
element:
</p><pre class="screen">
&lt;gridftp  url="gsiftp://evitable.ci.uchicago.edu" /&gt;
</pre><p>
The <code class="literal">url</code> attribute may specify a GridFTP server, using the gsiftp URI scheme;
or it may specify that filesystem copying will be used (which assumes that
the site has access to the same filesystem as the submitting machine) using
the URI <code class="literal">local://localhost</code>.
		</p><p>
Filesystem access using scp (the SSH copy protocol) can be specified using the
<code class="literal">&lt;filesystem&gt;</code> element:
</p><pre class="screen">
&lt;filesystem url="www11.i2u2.org" provider="ssh"/&gt;
</pre><p>
For additional ssh configuration information, see the ssh execution
provider documentation below.
		</p><p>
Filesystem access using <a class="link" href="#coasters" title="18. Coasters">CoG coasters</a> can be
also be specified using the <code class="literal">&lt;filesystem&gt;</code> element. More detail about
configuring that can be found in the <a class="link" href="#coasters" title="18. Coasters">CoG
coasters</a> section.
		</p></div><div class="section" title="12.3. Execution method"><div class="titlepage"><div><div><h3 class="title"><a name="id2894967"></a>12.3. Execution method</h3></div></div></div><p>
Execution methods may be specified either with the <code class="literal">&lt;jobmanager&gt;</code>
or <code class="literal">&lt;execution&gt;</code> element.
		</p><p>
The <code class="literal">&lt;jobmanager&gt;</code> element can be used to specify
execution through GRAM2. For example,
</p><pre class="screen">
    &lt;jobmanager universe="vanilla" url="evitable.ci.uchicago.edu/jobmanager-fork" major="2" /&gt;
</pre><p>
The <code class="literal">universe</code> attribute should always be set to vanilla. The
<code class="literal">url</code> attribute
should specify the name of the GRAM2 gatekeeper host, and the name of the
jobmanager to use. The major attribute should always be set to 2.
		</p><p>
The <code class="literal">&lt;execution&gt;</code> element can be used to specify
execution through other execution providers:
		</p><p>
To use GRAM4, specify the <code class="literal">gt4</code> provider. For example:
</p><pre class="screen">
&lt;execution provider="gt4" jobmanager="PBS" url="tg-grid.uc.teragrid.org" /&gt;
</pre><p>
The <code class="literal">url</code> attribute should specify the GRAM4 submission site.
The <code class="literal">jobmanager</code>
attribute should specify which GRAM4 jobmanager will be used.
		</p><p>
For local execution, the <code class="literal">local</code> provider should be used,
like this:
</p><pre class="screen">
&lt;execution provider="local" url="none" /&gt;
</pre><p>
		</p><p>
For PBS execution, the <code class="literal">pbs</code> provider should be used:
</p><pre class="screen">
&lt;execution provider="pbs" url="none" /&gt;
</pre><p>
The <code class="literal"><a class="link" href="#profile.globus.queue">GLOBUS::queue</a></code> profile key
can be used to specify which PBS queue jobs will be submitted to.
		</p><p>
For execution through a local Condor installation, the <code class="literal">condor</code>
provider should be used. This provider can run jobs either in the default
vanilla universe, or can use Condor-G to run jobs on remote sites.
		</p><p>
When running locally, only the <code class="literal">&lt;execution&gt;</code> element
needs to be specified:
</p><pre class="screen">
&lt;execution provider="condor" url="none" /&gt;
</pre><p>
		</p><p>
When running with Condor-G, it is necessary to specify the Condor grid
universe and the contact string for the remote site. For example:
</p><pre class="screen">
 &lt;execution provider="condor" /&gt;
 &lt;profile namespace="globus" key="jobType"&gt;grid&lt;/profile&gt;
 &lt;profile namespace="globus" key="gridResource"&gt;gt2 belhaven-1.renci.org/jobmanager-fork&lt;/profile&gt;
</pre><p>
		</p><p>
For execution through SSH, the <code class="literal">ssh</code> provider should be used:
</p><pre class="screen">
&lt;execution url="www11.i2u2.org" provider="ssh"/&gt;
</pre><p>
with configuration made in <code class="filename">~/.ssh/auth.defaults</code> with
the string 'www11.i2u2.org' changed to the appropriate host name:
</p><pre class="screen">
www11.i2u2.org.type=key
www11.i2u2.org.username=hategan
www11.i2u2.org.key=/home/mike/.ssh/i2u2portal
www11.i2u2.org.passphrase=XXXX
</pre><p>
		</p><p>
For execution using the 
<a class="link" href="#coasters" title="18. Coasters">CoG Coaster mechanism</a>, the <code class="literal">coaster</code> provider
should be used:
</p><pre class="screen">
&lt;execution provider="coaster" url="tg-grid.uc.teragrid.org"
    jobmanager="gt2:gt2:pbs" /&gt;
</pre><p>
More details about configuration of coasters can be found in the
<a class="link" href="#coasters" title="18. Coasters">section on coasters</a>.
		</p></div><div class="section" title="12.4. Work directory"><div class="titlepage"><div><div><h3 class="title"><a name="id2895229"></a>12.4. Work directory</h3></div></div></div><p>
The <code class="literal">workdirectory</code> element specifies where on the site files can be
stored.
</p><pre class="screen">
&lt;workdirectory&gt;/home/benc&lt;/workdirectory&gt;
</pre><p>
This file must be accessible through the transfer mechanism specified
in the <code class="literal">&lt;gridftp&gt;</code> element and also mounted on all worker nodes that
will be used for execution. A shared cluster scratch filesystem is
appropriate for this. 
		</p></div><div class="section" title="12.5. Profiles"><div class="titlepage"><div><div><h3 class="title"><a name="id2895262"></a>12.5. Profiles</h3></div></div></div><p>
<a class="link" href="#profiles" title="11. Profiles">Profile keys</a> can be specified using
the &lt;profile&gt; element. For example:
</p><pre class="screen">
&lt;profile namespace="globus" key="queue"&gt;fast&lt;/profile&gt;
</pre><p>
		</p></div><p>
The site catalog format is an evolution of the VDS site catalog format which
is documented
<a class="ulink" href="http://vds.uchicago.edu/vds/doc/userguide/html/H_SiteCatalog.html" target="_top">here</a>.
		</p></div><div class="section" title="13. The Transformation Catalog - tc.data"><div class="titlepage"><div><div><h2 class="title"><a name="transformationcatalog"></a>13. The Transformation Catalog - tc.data</h2></div></div></div><p>
The transformation catalog lists where application executables are located
on remote sites.
		</p><p>
By default, the site catalog is stored in <code class="filename">etc/tc.data</code>.
This path can be overridden with the <code class="literal">tc.file</code> configuration property,
either in the Swift configuration file or on the command line.
		</p><p>
The format is one line per executable per site, with fields separated by
tabs. Spaces cannot be used to separate fields.
		</p><p>Some example entries:
</p><pre class="screen">
localhost  echo    /bin/echo       INSTALLED       INTEL32::LINUX  null
TGUC       touch   /usr/bin/touch  INSTALLED       INTEL32::LINUX  GLOBUS::maxwalltime="0:1"
</pre><p>
		</p><p>
The fields are: site, transformation name, executable path, installation
status, platform, and profile entrys.
		</p><p>
The site field should correspond to a site name listed in the sites
catalog.</p><p>
The transformation name should correspond to the transformation name
used in a SwiftScript <code class="literal">app</code> procedure.
		</p><p>
The executable path should specify where the particular executable is
located on that site.
		</p><p>
The installation status and platform fields are not used. Set them to
<code class="literal">INSTALLED</code> and <code class="literal">INTEL32::LINUX</code> respectively.
		</p><p>
The profiles field should be set to <code class="literal">null</code> if no profile entries are to be
specified, or should contain the profile entries separated by semicolons.
		</p></div><div class="section" title="14. Build options"><div class="titlepage"><div><div><h2 class="title"><a name="buildoptions"></a>14. Build options</h2></div></div></div><p>
See <a class="ulink" href="http://www.ci.uchicago.edu/swift/downloads/" target="_top">the
Swift download page</a> for instructions on downloading and
building Swift from source. When building, various build options can
be supplied on the ant commandline. These are summarised here:
		</p><p>
<code class="literal">with-provider-condor</code> - build with CoG condor provider
		</p><p>
<code class="literal">with-provider-coaster</code> - build with CoG coaster provider (see
<a class="link" href="#coasters" title="18. Coasters">the section on coasters</a>). Since 0.8,
coasters are always built, and this option has no effect.
		</p><p>
<code class="literal">with-provider-deef</code> - build with Falkon provider deef. In order for this
option to work, it is necessary to check out the provider-deef code in
the cog/modules directory alongside swift:

			</p><pre class="screen">
$ <strong class="userinput"><code>cd cog/modules</code></strong>
$ <strong class="userinput"><code>svn co https://svn.ci.uchicago.edu/svn/vdl2/provider-deef</code></strong>
$ <strong class="userinput"><code>cd ../swift</code></strong>
$ <strong class="userinput"><code>ant -Dwith-provider-deef=true redist</code></strong>
			</pre><p>

		</p><p>
<code class="literal">with-provider-wonky</code> - build with provider-wonky, an execution provider
that provides delays and unreliability for the purposes of testing Swift's
fault tolerance mechanisms. In order for this option to work, it is
necessary to check out the provider-wonky code in the <code class="filename">cog/modules</code>
directory alongside swift:

			</p><pre class="screen">
$ <strong class="userinput"><code>cd cog/modules</code></strong>
$ <strong class="userinput"><code>svn co https://svn.ci.uchicago.edu/svn/vdl2/provider-wonky</code></strong>
$ <strong class="userinput"><code>cd ../swift</code></strong>
$ <strong class="userinput"><code>ant -Dwith-provider-wonky=true redist</code></strong>
			</pre><p>
		</p><p>
<code class="literal">no-supporting</code> - produces a distribution without supporting commands such
as <span class="command"><strong>grid-proxy-init</strong></span>. This is intended for when the Swift distribution will be
used in an environment where those commands are already provided by other
packages, where the Swift package should be providing only Swift
commands, and where the presence of commands such as grid-proxy-init from
the Swift distribution in the path will mask the presence of those
commands from their true distribution package such as a Globus Toolkit
package.
</p><pre class="screen">
$ <strong class="userinput"><code>ant -Dno-supporting=true redist</code></strong>
</pre><p>
		</p></div><div class="section" title="15. Kickstart"><div class="titlepage"><div><div><h2 class="title"><a name="kickstart"></a>15. Kickstart</h2></div></div></div><p>

Kickstart is a tool that can be used to gather various information 
about the remote execution environment for each job that Swift tries
to run.
		</p><p>
For each job, Kickstart generates an XML <em class="firstterm">invocation
record</em>. By default this record is staged back to the submit
host if the job fails.
		</p><p>
Before it can be used it must be installed on the remote site and
the sites file must be configured to point to kickstart.
		</p><p>
Kickstart can be downloaded as part of the Pegasus 'worker package' available
from the worker packages section of <a class="ulink" href="http://pegasus.isi.edu/code.php" target="_top">the Pegasus download page</a>.
		</p><p>
Untar the relevant worker package somewhere where it is visible to all of the
worker nodes on the remote execution machine (such as in a shared application
filesystem).
		</p><p>Now configure the gridlaunch attribute of the sites catalog
to point to that path, by adding a <em class="parameter"><code>gridlaunch</code></em>
attribute to the <code class="function">pool</code> element in the site
catalog:

</p><pre class="screen">

&lt;pool handle="example" gridlaunch="/usr/local/bin/kickstart" sysinfo="INTEL32::LINUX"&gt;
[...]
&lt;/pool&gt;

</pre><p>

		</p><p>
There are various kickstat.* properties, which have sensible default
values. These are documented in <a class="link" href="#engineconfiguration" title="10. Swift configuration properties">the
properties section</a>.
		</p></div><div class="section" title="16. Reliability mechanisms"><div class="titlepage"><div><div><h2 class="title"><a name="reliability"></a>16. Reliability mechanisms</h2></div></div></div><p>
This section details reliabilty mechanisms in Swift: retries, restarts
and replication.
	</p><div class="section" title="16.1. Retries"><div class="titlepage"><div><div><h3 class="title"><a name="retries"></a>16.1. Retries</h3></div></div></div><p>
If an application procedure execution fails, Swift will attempt that
execution again repeatedly until it succeeds, up until the limit
defined in the <code class="literal">execution.retries</code> configuration
property.
		</p><p>
Site selection will occur for retried jobs in the same way that it happens
for new jobs. Retried jobs may run on the same site or may run on a
different site.
		</p><p>
If the retry limit <code class="literal">execution.retries</code> is reached for an
application procedure, then that application procedure will fail. This will
cause the entire run to fail - either immediately (if the
<code class="literal">lazy.errors</code> property is <code class="literal">false</code>) or
after all other possible work has been attempted (if the
<code class="literal">lazy.errors</code> property is <code class="literal">true</code>).
		</p></div><div class="section" title="16.2. Restarts"><div class="titlepage"><div><div><h3 class="title"><a name="restart"></a>16.2. Restarts</h3></div></div></div><p>
If a run fails, Swift can resume the program from the point of
failure. When a run fails, a restart log file will be left behind in
a file named using the unique job ID and a <code class="filename">.rlog</code> extension. This restart log
can then be passed to a subsequent Swift invocation using the <code class="literal">-resume</code>
parameter. Swift will resume execution, avoiding execution of invocations
that have previously completed successfully. The SwiftScript source file
and input data files should not be modified between runs.
		</p><p>
Every run creates a restart
log file with a named composed of the file name of the workflow
being executed, an invocation ID, a numeric ID, and the <code class="filename">.rlog</code> extension. For example, <code class="filename">example.swift</code>, when executed, could produce
the following restart log file: <code class="filename">example-ht0adgi315l61.0.rlog</code>. Normally, if
the run completes successfully, the restart log file is
deleted. If however the workflow fails, <span class="command"><strong>swift</strong></span>
can use the restart log file to continue
execution from a point before the
failure occurred. In order to restart from a restart log
file, the <code class="option">-resume <em class="parameter"><code><code class="filename">logfile</code></code></em></code> argument can be
used after the SwiftScript program file name. Example:

</p><pre class="screen">
<code class="prompt">$</code> <span class="command"><strong>swift</strong></span> <code class="option">-resume <code class="filename">example-ht0adgi315l61.0.rlog</code></code> <code class="option"><code class="filename">example.swift</code></code>.
</pre><p>

		</p></div><div class="section" title="16.3. Replication"><div class="titlepage"><div><div><h3 class="title"><a name="replication"></a>16.3. Replication</h3></div></div></div><p>
When an execution job has been waiting in a site queue for a certain
period of time, Swift can resubmit replicas of that job (up to the limit
defined in the <code class="literal">replication.limit</code> configuration property).
When any of those jobs moves from queued to active state, all of the
other replicas will be cancelled.
		</p><p>
This is intended to deal with situations where some sites have a substantially
longer (sometimes effectively infinite) queue time than other sites.
Selecting those slower sites can cause a very large delay in overall run time.
		</p><p>
Replication can be enabled by setting the
<code class="literal">replication.enabled</code> configuration property to
<code class="literal">true</code>. The maximum number of replicas that will be
submitted for a job is controlled by the <code class="literal">replication.limit</code>
configuration property.
		</p><p>
When replication is enabled, Swift will also enforce the
<code class="literal">maxwalltime</code> profile setting for jobs as documented in
the <a class="link" href="#profiles" title="11. Profiles">profiles section</a>.
		</p></div></div><div class="section" title="17. Clustering"><div class="titlepage"><div><div><h2 class="title"><a name="clustering"></a>17. Clustering</h2></div></div></div><p>
Swift can group a number of short job submissions into a single larger
job submission to minimize overhead involved in launching jobs (for example,
caused by security negotiation and queuing delay). In general,
<a class="link" href="#coasters" title="18. Coasters">CoG coasters</a> should be used in preference
to the clustering mechanism documented in this section.
		</p><p>
By default, clustering is disabled. It can be activated by setting the
<a class="link" href="#property.clustering.enabled">clustering.enabled</a>
property to true.
		</p><p>
A job is eligible for clustering if
the <a class="link" href="#profile.globus.maxwalltime"><span class="property">GLOBUS::maxwalltime</span></a> profile is specified in the <code class="filename">tc.data</code> entry for that job, and its value is
less than the value of the
<a class="link" href="#property.clustering.min.time"><span class="property">clustering.min.time</span></a>
property.
		</p><p>
Two or more jobs are considered compatible if they share the same site
and do not have conflicting profiles (e.g. different values for the same
environment variable). 
		</p><p>
When a submitted job is eligible for clustering, 
it will be put in a clustering queue rather than being submitted to
a remote site. The clustering queue is processed at intervals 
specified by the
<a class="link" href="#property.clustering.queue.delay"><span class="property">clustering.queue.delay</span></a>
property. The processing of the clustering queue consists of selecting
compatible jobs and grouping them into clusters whose maximum wall time does
not exceed twice the value of the <span class="property">clustering.min.time</span>
property.
		</p></div><div class="section" title="18. Coasters"><div class="titlepage"><div><div><h2 class="title"><a name="coasters"></a>18. Coasters</h2></div></div></div><p>Coasters were introduced in Swift v0.6 as an experimental feature.
</p><p>
In many applications, Swift performance can be greatly enhanced by the
use of CoG coasters. CoG coasters provide a low-overhead job submission
and file transfer mechanism suited for the execution of short jobs
(on the order of a few seconds) and the transfer of small files (on the
order of a few kilobytes) for which other grid protocols such as GRAM
and GridFTP are poorly suited.
</p><p>
The coaster mechanism submits a head job using some other execution
mechanism such as GRAM, and for each worker node that will be used in
a remote cluster, it submits a worker job, again using some other
execution mechanism such as GRAM. Details on the design of the coaster
mechanism can be found
<a class="ulink" href="http://wiki.cogkit.org/wiki/Coasters" target="_top">
here.</a>
</p><p>
The head job manages file transfers and the dispatch of execution jobs
to workers. Much of the overhead associated with other grid protocols
(such as authentication and authorization, and allocation of worker nodes
by the site's local resource manager) is reduced, because that overhead
is associated with the allocation of a coaster head or coaster worker,
rather than with every Swift-level procedure invocation; potentially hundreds
or thousands of Swift-level procedure invocations can be run through a single
worker.
</p><p>
Coasters can be configured for use in two situations: job execution and
file transfer.
</p><p>
To use for job execution, specify a sites.xml execution element like this:
</p><pre class="screen">
&lt;execution provider="coaster" jobmanager="gt2:gt2:pbs" url="grid.myhost.org"&gt;
</pre><p>
The jobmanager string contains more detail than with other providers. It
contains either two or three colon separated fields:
1:the provider to be use to execute the coaster head job - this provider
will submit from the Swift client side environment. Commonly this will be
one of the GRAM providers; 2: the provider
to be used to execute coaster worker jobs. This provider will be used
to submit from the coaster head job environment, so a local scheduler
provider can sometimes be used instead of GRAM. 3: optionally, the
jobmanager to be used when submitting worker job using the provider
specified in field 2.
</p><p>
To use for file transfer, specify a sites.xml filesystem element like this:
</p><pre class="screen">
&lt;filesystem provider="coaster" url="gt2://grid.myhost.org" /&gt;
</pre><p>
The url parameter should be a pseudo-URI formed with the URI scheme being
the name of the provider to use to submit the coaster head job, and the
hostname portion being the hostname to be used to execute the coaster
head job. Note that this provider and hostname will be used for execution
of a coaster head job, not for file transfer; so for example, a GRAM
endpoint should be specified here rather than a GridFTP endpoint.
</p><p>
Coasters are affected by the following profile settings, which are
documented in the <a class="link" href="#profile.globus" title="11.3. Globus namespace">Globus namespace profile
section</a>:
</p><div class="table"><a name="id2896183"></a><p class="title"><b>Table 13. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">profile key</th><th align="left">brief description</th></tr></thead><tbody><tr><td align="left">slots</td><td align="left">How many maximum LRM jobs/worker blocks are allowed</td></tr><tr><td align="left">workersPerNode</td><td align="left">How many coaster workers to run per execution node</td></tr><tr><td align="left">nodeGranularity</td><td align="left">Each worker block uses a number of nodes that is a multiple of this number</td></tr><tr><td align="left">lowOverallocation</td><td align="left">How many times larger than the job walltime should a block's walltime be if all jobs are 1s long</td></tr><tr><td align="left">highOverallocation</td><td align="left">How many times larger than the job walltime should a block's walltime be if all jobs are infinitely long</td></tr><tr><td align="left">overallocationDecayFactor</td><td align="left">How quickly should the overallocation curve tend towards the highOverallocation as job walltimes get larger</td></tr><tr><td align="left">spread</td><td align="left">By how much should worker blocks vary in worker size</td></tr><tr><td align="left">workersPerNode</td><td align="left">How many coaster workers to run per execution node</td></tr><tr><td align="left">reserve</td><td align="left">How many seconds to reserve in a block's walltime for starting/shutdown operations</td></tr><tr><td align="left">maxnodes</td><td align="left">The maximum number of nodes allowed in a block</td></tr><tr><td align="left">maxtime</td><td align="left">The maximum number of walltime allowed for a block</td></tr><tr><td align="left">remoteMonitorEnabled</td><td align="left">If true, show a graphical display of the status of the coaster service</td></tr></tbody></table></div></div><br class="table-break"></div><div class="section" title="19. How-To Tips for Specific User Communities"><div class="titlepage"><div><div><h2 class="title"><a name="localhowtos"></a>19. How-To Tips for Specific User Communities</h2></div></div></div><div class="section" title="19.1. Saving Logs - for UChicago CI Users"><div class="titlepage"><div><div><h3 class="title"><a name="savinglogs"></a>19.1. Saving Logs - for UChicago CI Users</h3></div></div></div><p>
If you have a UChicago Computation Institute account, run this command in your 
submit directory after each run. It will copy all your logs and kickstart 
records into a directory at the CI for reporting, usage tracking, support and debugging.
			</p><p>
</p><pre class="screen">
rsync --ignore-existing *.log *.d login.ci.uchicago.edu:/disks/ci-gpfs/swift/swift-logs/ --verbose
</pre><p>
			</p></div><div class="section" title="19.2. Specifying TeraGrid allocations"><div class="titlepage"><div><div><h3 class="title"><a name="id2896345"></a>19.2. Specifying TeraGrid allocations</h3></div></div></div><p>TeraGrid users with no default project or with several project
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
