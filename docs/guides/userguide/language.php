<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>2. The SwiftScript Language</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.php" title="Swift User Guide"><link rel="up" href="index.php" title="Swift User Guide"><link rel="prev" href="overview.php" title="1. Overview"><link rel="next" href="mappers.php" title="3. Mappers"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="navheader"><table width="100%" summary="Navigation header"><tr><th colspan="3" align="center">2. The SwiftScript Language</th></tr><tr><td width="20%" align="left"><a accesskey="p" href="overview.php">Prev</a> </td><th width="60%" align="center"> </th><td width="20%" align="right"> <a accesskey="n" href="mappers.php">Next</a></td></tr></table><hr></div><div class="section" title="2. The SwiftScript Language"><div class="titlepage"><div><div><h2 class="title"><a name="language"></a>2. The SwiftScript Language</h2></div></div></div><div class="section" title="2.1. Language basics"><div class="titlepage"><div><div><h3 class="title"><a name="id3385649"></a>2.1. Language basics</h3></div></div></div><p>
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
</p><div class="figure"><a name="id3385849"></a><p class="title"><b>Figure 1. shane.jpeg</b></p><div class="figure-contents"><img src="userguide-shane.jpeg" alt="shane.jpeg"></div></div><br class="figure-break"><div class="figure"><a name="id3385862"></a><p class="title"><b>Figure 2. rotated.jpeg</b></p><div class="figure-contents"><img src="userguide-rotated.jpeg" alt="rotated.jpeg"></div></div><br class="figure-break"></div><div class="section" title="2.2. Arrays and Parallel Execution"><div class="titlepage"><div><div><h3 class="title"><a name="id3385877"></a>2.2. Arrays and Parallel Execution</h3></div></div></div><p>
Arrays of values can be declared using the <code class="literal">[]</code> suffix. An
array be mapped to a collection of files, one element per file, by using
a different form of mapping expression.  For example, the
<a class="link" href="mappers.php#mapper.filesys_mapper" title="3.4. file system mapper"><code class="literal">filesys_mapper</code></a>
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
</p></div><div class="section" title="2.3. Ordering of execution"><div class="titlepage"><div><div><h3 class="title"><a name="id3385959"></a>2.3. Ordering of execution</h3></div></div></div><p>
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
</p></div><div class="section" title="2.4. Compound procedures"><div class="titlepage"><div><div><h3 class="title"><a name="id3426671"></a>2.4. Compound procedures</h3></div></div></div><p>
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
</p></div><div class="section" title="2.5. More about types"><div class="titlepage"><div><div><h3 class="title"><a name="id3426744"></a>2.5. More about types</h3></div></div></div><p>
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
</p><p>There are a number of primitive types:</p><div class="table"><a name="id3426788"></a><p class="title"><b>Table 1. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">type</th><th align="left">contains</th></tr></thead><tbody><tr><td align="left">int</td><td align="left">integers</td></tr><tr><td align="left">string</td><td align="left">strings of text</td></tr><tr><td align="left">float</td><td align="left">floating point numbers, that behave the same as Java <code class="literal">double</code>s</td></tr><tr><td align="left">boolean</td><td align="left">true/false</td></tr></tbody></table></div></div><br class="table-break"><p>
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
</p></div><div class="section" title="2.6. Data model"><div class="titlepage"><div><div><h3 class="title"><a name="id3426961"></a>2.6. Data model</h3></div></div></div><p>Data processed by Swift is strongly typed. It may be take the form
of values in memory or as out-of-core files on disk. Language constructs
called mappers specify how each piece of data is stored.</p><div class="section" title="2.6.1. Mappers"><div class="titlepage"><div><div><h4 class="title"><a name="id3426970"></a>2.6.1. Mappers</h4></div></div></div><p>
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
These are documented in the <a class="link" href="mappers.php" title="3. Mappers">mappers section</a>
of this guide.
</p></div></div><div class="section" title="2.7. More technical details about SwiftScript"><div class="titlepage"><div><div><h3 class="title"><a name="id3427028"></a>2.7. More technical details about SwiftScript</h3></div></div></div><p>The syntax of SwiftScript has a superficial resemblance to C and
Java. For example, { and } characters are used to enclose blocks of
statements.
</p><p>
A SwiftScript program consists of a number of statements.
Statements may declare types, procedures and variables, assign values to
variables, and express operations over arrays.
			</p><div class="section" title="2.7.1. Variables"><div class="titlepage"><div><div><h4 class="title"><a name="id3427044"></a>2.7.1. Variables</h4></div></div></div><p>Variables in SwiftScript are declared to be of a specific type.
Assignments to those variables must be data of that type.
SwiftScript variables are single-assignment - a value may be assigned
to a variable at most once. This assignment can happen at declaration time
or later on in execution. When an attempt to read from a variable
that has not yet been assigned is made, the code performing the read
is suspended until that variable has been written to. This forms the
basis for Swift's ability to parallelise execution - all code will
execute in parallel unless there are variables shared between the code
that cause sequencing.</p><div class="section" title="2.7.1.1. Variable Declarations"><div class="titlepage"><div><div><h5 class="title"><a name="id3427057"></a>2.7.1.1. Variable Declarations</h5></div></div></div><p>
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
described in another section.</p></div><div class="section" title="2.7.1.2. Assignment Statements"><div class="titlepage"><div><div><h5 class="title"><a name="id3427084"></a>2.7.1.2. Assignment Statements</h5></div></div></div><p>
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
			</p></div></div><div class="section" title="2.7.2. Procedures"><div class="titlepage"><div><div><h4 class="title"><a name="id3427108"></a>2.7.2. Procedures</h4></div></div></div><p>There are two kinds of procedure: An atomic procedure, which
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
		</p></div></div><div class="section" title="2.7.3. Control Constructs"><div class="titlepage"><div><div><h4 class="title"><a name="id3427309"></a>2.7.3. Control Constructs</h4></div></div></div><p>
SwiftScript provides <code class="literal">if</code>, <code class="literal">switch</code>,
<code class="literal">foreach</code>, and <code class="literal">iterate</code> constructs,
with syntax and semantics similar to comparable constructs in
other high-level languages.
			</p><div class="section" title="2.7.3.1. foreach"><div class="titlepage"><div><div><h5 class="title"><a name="id3427342"></a>2.7.3.1. foreach</h5></div></div></div><p>
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

			</p></div><div class="section" title="2.7.3.2. if"><div class="titlepage"><div><div><h5 class="title"><a name="id3427399"></a>2.7.3.2. if</h5></div></div></div><p>
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
			</p></div><div class="section" title="2.7.3.3. switch"><div class="titlepage"><div><div><h5 class="title"><a name="id3427433"></a>2.7.3.3. switch</h5></div></div></div><p>
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
				</p></div></div></div><div class="section" title="2.8. Operators"><div class="titlepage"><div><div><h3 class="title"><a name="id3427536"></a>2.8. Operators</h3></div></div></div><p>The following infix operators are available for use in
SwiftScript expressions.
</p><div class="table"><a name="id3427545"></a><p class="title"><b>Table 2. </b></p><div class="table-contents"><table border="1"><colgroup><col><col></colgroup><thead><tr><th align="left">operator</th><th align="left">purpose</th></tr></thead><tbody><tr><td align="left">+</td><td align="left">numeric addition; string concatenation</td></tr><tr><td align="left">-</td><td align="left">numeric subtraction</td></tr><tr><td align="left">*</td><td align="left">numeric multiplication</td></tr><tr><td align="left">/</td><td align="left">floating point division</td></tr><tr><td align="left">%/</td><td align="left">integer division</td></tr><tr><td align="left">%%</td><td align="left">integer remainder of division</td></tr><tr><td align="left">== !=</td><td align="left">comparison and not-equal-to</td></tr><tr><td align="left"> &lt; &gt; &lt;= &gt;=</td><td align="left">numerical ordering</td></tr><tr><td align="left">&amp;&amp; ||</td><td align="left">boolean and, or</td></tr><tr><td align="left">!</td><td align="left">boolean not</td></tr></tbody></table></div></div><br class="table-break"></div><div class="section" title="2.9. Global constants"><div class="titlepage"><div><div><h3 class="title"><a name="globals"></a>2.9. Global constants</h3></div></div></div><p>
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
		</p></div></div>
			</div>
			<!-- end content container-->
			<!-- footer -->
			<div id="footer"><?php require('../../inc/footer.php') ?></div> 
			<!-- end footer -->

		</div>
		<!-- end entire page container -->

		
		<div class="navfooter"><hr><table width="100%" summary="Navigation footer"><tr><td width="40%" align="left"><a accesskey="p" href="overview.php">Prev</a> </td><td width="20%" align="center"> </td><td width="40%" align="right"> <a accesskey="n" href="mappers.php">Next</a></td></tr><tr><td width="40%" align="left" valign="top">1. Overview </td><td width="20%" align="center"><a accesskey="h" href="index.php">Home</a></td><td width="40%" align="right" valign="top"> 3. Mappers</td></tr></table></div><script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {var pageTracker = _gat._getTracker("UA-106257-5");
pageTracker._trackPageview();
} catch(err) {}</script></body></html>
