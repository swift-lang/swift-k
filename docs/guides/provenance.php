<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>provenance working notes, benc</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.html" title="provenance working notes, benc"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="provenance working notes, benc"><div class="titlepage"><div><div><h2 class="title"><a name="id2980927"></a>provenance working notes, benc</h2></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="section"><a href="#id3048053">1. Goal of this present work</a></span></dt><dt><span class="section"><a href="#owndb">2. Running your own provenance database</a></span></dt><dd><dl><dt><span class="section"><a href="#id3097138">2.1. Check out the latest SVN code</a></span></dt><dt><span class="section"><a href="#id3097160">2.2. Configuring your SQL database</a></span></dt><dt><span class="section"><a href="#id3048241">2.3. Import your logs</a></span></dt><dt><span class="section"><a href="#id3048301">2.4. Querying the newly generated database</a></span></dt></dl></dd><dt><span class="section"><a href="#commands">3. swift-about-* commands</a></span></dt><dt><span class="section"><a href="#id3049168">4. What this work does not address</a></span></dt><dt><span class="section"><a href="#id3049417">5. Data model</a></span></dt><dd><dl><dt><span class="section"><a href="#id3049422">5.1. Introduction to the data model</a></span></dt><dt><span class="section"><a href="#id3049458">5.2. execute</a></span></dt><dt><span class="section"><a href="#id3049480">5.3. execute2</a></span></dt><dt><span class="section"><a href="#id3049500">5.4. dataset</a></span></dt><dt><span class="section"><a href="#id3049544">5.5. workflow</a></span></dt></dl></dd><dt><span class="section"><a href="#id3109688">6. Prototype Implementations</a></span></dt><dd><dl><dt><span class="section"><a href="#id3109729">6.1. Relational, using SQL</a></span></dt><dt><span class="section"><a href="#id3110140">6.2. XML</a></span></dt><dt><span class="section"><a href="#id3110631">6.3. RDF and SPARQL</a></span></dt><dt><span class="section"><a href="#id3110664">6.4. GraphGrep</a></span></dt><dt><span class="section"><a href="#id3110689">6.5. prolog</a></span></dt><dt><span class="section"><a href="#id3110823">6.6. amazon simpledb</a></span></dt><dt><span class="section"><a href="#id3110860">6.7. graphviz</a></span></dt></dl></dd><dt><span class="section"><a href="#id3110894">7. Comparison with related work that our group has done
before</a></span></dt><dd><dl><dt><span class="section"><a href="#id3110899">7.1. vs VDS1 VDC</a></span></dt><dt><span class="section"><a href="#id3110927">7.2. vs VDL provenance paper figure 1 schema</a></span></dt></dl></dd><dt><span class="section"><a href="#id3110969">8. Questions/Discussion points</a></span></dt><dd><dl><dt><span class="section"><a href="#id3110973">8.1. metadata</a></span></dt><dt><span class="section"><a href="#id3111060">8.2. The 'preceeds' relation</a></span></dt><dt><span class="section"><a href="#id3111244">8.3. Unique identification of provenance objects</a></span></dt><dt><span class="section"><a href="#id3111511">8.4. Type representation</a></span></dt><dt><span class="section"><a href="#id3111524">8.5. representation of workflows</a></span></dt><dt><span class="section"><a href="#id3111542">8.6. metadata extraction</a></span></dt><dt><span class="section"><a href="#id3111557">8.7. source code recreation</a></span></dt><dt><span class="section"><a href="#id3111574">8.8. Input parameters</a></span></dt></dl></dd><dt><span class="section"><a href="#opm">9. Open Provenance Model (OPM)</a></span></dt><dd><dl><dt><span class="section"><a href="#id3111607">9.1. OPM-defined terms and their relation to Swift</a></span></dt><dt><span class="section"><a href="#id3111687">9.2. OPM links</a></span></dt><dt><span class="section"><a href="#id3111700">9.3. Swift specific OPM considerations</a></span></dt></dl></dd><dt><span class="section"><a href="#id3111841">10. Processing i2u2 cosmic metadata</a></span></dt><dt><span class="section"><a href="#id3112106">11. processing fMRI metadata</a></span></dt><dt><span class="section"><a href="#id3112135">12. random unsorted notes</a></span></dt><dt><span class="section"><a href="#id3112189">13. Provenance Challenge 1 examples</a></span></dt><dd><dl><dt><span class="section"><a href="#id3112194">13.1. Basic SQL</a></span></dt><dt><span class="section"><a href="#id3112413">13.2. SQL with transitive closures</a></span></dt></dl></dd><dt><span class="section"><a href="#id3112460">14. Representation of dataset containment and procedure execution in r2681 and how it could change.</a></span></dt></dl></div><pre class="screen">$Id$</pre><div class="section" title="1. Goal of this present work"><div class="titlepage"><div><div><h2 class="title"><a name="id3048053"></a>1. Goal of this present work</h2></div></div></div><p>
The goal of the work described in this document is to investigate
<span class="emphasis"><em>retrospective provenance</em></span> and
<span class="emphasis"><em>metadata handling</em></span> in Swift, with an emphasis
on effective querying of the data, rather than on collection of the data.
</p><p>
The motivating examples are queries of the kinds discussed in section 4 of
<a class="ulink" href="http://www.ci.uchicago.edu/swift/papers/VirtualDataProvenance.pdf" target="_top">'Applying the Virtual Data Provenance Model'</a>;
the queries and metadata in the <a class="ulink" href="http://twiki.ipaw.info/bin/view/Challenge/FirstProvenanceChallenge" target="_top">First Provenance Challenge</a>; and the metadata database used by
i2u2 cosmic.
</p><p>
I am attempting to scope this so that it can be implemented in a few
months; more expensive features, though desirable, are relegated the the
'What this work does not address' section. Features which appear fairly
orthogonal to the main aims are also omitted.
</p><p>
This document is a combination of working notes and on-going status report
regarding my provenance work; as such its got quite a lot of opinion in it,
some of it not justified in the text.
</p></div><div class="section" title="2. Running your own provenance database"><div class="titlepage"><div><div><h2 class="title"><a name="owndb"></a>2. Running your own provenance database</h2></div></div></div><p>This section details running your own SQL-based provenance database on
servers of your own control.</p><div class="section" title="2.1. Check out the latest SVN code"><div class="titlepage"><div><div><h3 class="title"><a name="id3097138"></a>2.1. Check out the latest SVN code</h3></div></div></div><p>
Use the following command to check out the <code class="literal">provenancedb</code>
module:

</p><pre class="screen">
svn co https://svn.ci.uchicago.edu/svn/vdl2/provenancedb                      
</pre><p>
</p></div><div class="section" title="2.2. Configuring your SQL database"><div class="titlepage"><div><div><h3 class="title"><a name="id3097160"></a>2.2. Configuring your SQL database</h3></div></div></div><p>
Follow the instructions in one of the following sections, to configure your
database either for sqlite3 or for postgres.
</p><div class="section" title="2.2.1. Configuring your sqlite3 SQL database"><div class="titlepage"><div><div><h4 class="title"><a name="id3048884"></a>2.2.1. Configuring your sqlite3 SQL database</h4></div></div></div><p>
This section describes configuring the SQL scripts to use 
<a class="ulink" href="http://www.sqlite.org/" target="_top">sqlite</a>, which is
appropriate for a single-user installation.
</p><p>Install or find sqlite3. On
<code class="literal">communicado.ci.uchicago.edu</code>, it is installed and can be
accessed by adding the line <code class="literal">+sqlite3</code> to your ~/.soft file
and typing <code class="literal">resoft</code>. Alternatively, on OS X with MacPorts, this command works:
</p><pre class="screen">
$ <strong class="userinput"><code>sudo port install sqlite3</code></strong>
</pre><p>
Similar commands using <code class="literal">apt</code> or <code class="literal">yum</code> will
probably work under Linux.
</p><p>
In the next section, you will create a <code class="literal">provenance.config</code>
file. In that, you should configure the use of sqlite3 by specifying:
</p><pre class="screen">
export SQLCMD="sqlite3 provdb "
</pre><p>
(note the trailing space before the closing quote)
</p></div><div class="section" title="2.2.2. Configuring your own postgres 8.3 SQL database"><div class="titlepage"><div><div><h4 class="title"><a name="id3048131"></a>2.2.2. Configuring your own postgres 8.3 SQL database</h4></div></div></div><p>
This section describes configuring a postgres 8.3 database, which is
appropriate for a large installation (where large means lots of log
files or multiple users)
</p><p>
First install and start postgres as appropriate for your platform
(using <span class="command"><strong>apt-get</strong></span> or <span class="command"><strong>port</strong></span> for example).
</p><p>
As user <code class="literal">postgres</code>, create a database:
</p><pre class="screen">
$ <strong class="userinput"><code>/opt/local/lib/postgresql83/bin/createdb provtest1</code></strong>
</pre><p>
</p><p>
Check that you can connect and see the empty database:
</p><pre class="screen">
$ <strong class="userinput"><code>psql83 -d provtest1 -U postgres</code></strong>
Welcome to psql83 8.3.6, the PostgreSQL interactive terminal.

Type:  \copyright for distribution terms
       \h for help with SQL commands
       \? for help with psql commands
       \g or terminate with semicolon to execute query
       \q to quit

provtest1=# <strong class="userinput"><code>\dt</code></strong>
No relations found.
provtest1=# <strong class="userinput"><code>\q</code></strong>
</pre><p>
</p><p>
In the next section, when configuring <code class="literal">provenance.config</code>,
specify the use of postgres like this:
</p><pre class="screen">
export SQLCMD="psql83 -d provtest1 -U postgres "
</pre><p>
Note the trailing space before the final quote. Also, note that if you
fiddled the above test command line to make it work, you will have to make
similar fiddles in the <code class="literal">SQLCMD</code> configuration line.
</p></div></div><div class="section" title="2.3. Import your logs"><div class="titlepage"><div><div><h3 class="title"><a name="id3048241"></a>2.3. Import your logs</h3></div></div></div><p>
Now create a <code class="filename">etc/provenance.config</code> file to define local
configuration. An example that I use on my laptop is present in
<code class="filename">provenance.config.soju</code>.
The <code class="literal">SQLCMD</code> indicates which command to run for SQL
access. This is used by other scripts to access the database. The
<code class="literal">LOGREPO</code> and <code class="literal">IDIR</code> variables should
point to the directory under which you collect your Swift logs.
</p><p>
Now import your logs for the first time like this:
</p><pre class="screen">
$ <strong class="userinput"><code>./swift-prov-import-all-logs rebuild</code></strong>
</pre><p>
</p></div><div class="section" title="2.4. Querying the newly generated database"><div class="titlepage"><div><div><h3 class="title"><a name="id3048301"></a>2.4. Querying the newly generated database</h3></div></div></div><p>
You can use <span class="command"><strong>swift-about-*</strong></span> commands, described in
the <a class="link" href="#commands" title="3. swift-about-* commands">commands section</a>.
</p><p>
If you're using the SQLite database, you can get an interactive SQL
session to query your new provenance database like this:
</p><pre class="screen">
$ <strong class="userinput"><code>sqlite3 provdb</code></strong>
SQLite version 3.6.11
Enter ".help" for instructions
Enter SQL statements terminated with a ";"
sqlite&gt; 
</pre><p>

</p></div></div><div class="section" title="3. swift-about-* commands"><div class="titlepage"><div><div><h2 class="title"><a name="commands"></a>3. swift-about-* commands</h2></div></div></div><p>There are several swift-about- commands:
</p><p>swift-about-filename - returns the global dataset IDs for the specified
filename. Several runs may have output the same filename; the provenance
database cannot tell which run (if any) any file with that name that
exists now came from.
</p><p>Example: this looks for information about
<code class="filename">001-echo.out</code> which is the output of the first
test in the language-behaviour test suite:
</p><pre class="screen">
$ <strong class="userinput"><code>./swift-about-filename 001-echo.out</code></strong>
Dataset IDs for files that have name file://localhost/001-echo.out
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080114-1353-g1y3moc0:720000000001
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080107-1440-67vursv4:720000000001
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080107-2146-ja2r2z5f:720000000001
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080107-1608-itdd69l6:720000000001
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080303-1011-krz4g2y0:720000000001
 tag:benc@ci.uchicago.edu,2008:swift:dataset:20080303-1100-4in9a325:720000000001
</pre><p>
Six different datasets in the provenance database have had that filename
(because six language behaviour test runs have been uploaded to the
database).
</p><p>swift-about-dataset - returns information about a dataset, given
that dataset's uri. Returned information includes the IDs of a containing
dataset, datasets contained within this dataset, and IDs for executions
that used this dataset as input or output.
</p><p>Example:
</p><pre class="screen">
$ <strong class="userinput"><code>./swift-about-dataset tag:benc@ci.uchicago.edu,2008:swift:dataset:20080114-1353-g1y3moc0:720000000001</code></strong>
About dataset tag:benc@ci.uchicago.edu,2008:swift:dataset:20080114-1353-g1y3moc0:720000000001
That dataset has these filename(s):
 file://localhost/001-echo.out

That dataset is part of these datasets:

That dataset contains these datasets:

That dataset was input to the following executions (as the specified named parameter):

That dataset was output from the following executions (as the specified return parameter):
 tag:benc@ci.uchicago.edu,2008:swiftlogs:execute:001-echo-20080114-1353-n7puv429:0                                                | t     
</pre><p>
This shows that this dataset is not part of a more complicated dataset
structure, and was produced as an output parameter t from an execution.
</p><p>swift-about-execution - gives information about an execution, given
an execution ID
</p><pre class="screen">
$ <strong class="userinput"><code>./swift-about-execution tag:benc@ci.uchicago.edu,2008:swiftlogs:execute:001-echo-20080114-1353-n7puv429:0</code></strong>
About execution tag:benc@ci.uchicago.edu,2008:swiftlogs:execute:001-echo-20080114-1353-n7puv429:0
                                                                id                                                                |   starttime    |     duration      |                                                            finalstate                                                            |                                                               app                                                                |                                                             scratch                                                              
----------------------------------------------------------------------------------------------------------------------------------+----------------+-------------------+----------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------
 tag:benc@ci.uchicago.edu,2008:swiftlogs:execute:001-echo-20080114-1353-n7puv429:0                                                | 1200318839.393 | 0.743000030517578 | 0                                                                                                                                | END_SUCCESS                                                                                                                      | echo                                                                                                                            
(1 row)
</pre><p>
This shows some basic information about the execution - the start time,
the duration, the name of the application, the final status.
</p></div><div class="section" title="4. What this work does not address"><div class="titlepage"><div><div><h2 class="title"><a name="id3049168"></a>4. What this work does not address</h2></div></div></div><p>This work explicitly excludes a number of uses which traditionally
have been associated with the VDS1 Virtual Data Catalog - either as real
or as imagined functionality.</p><p>
Much of this is open to debate; especially regarding which features are the
most important to implement after the first round of implementation has
occurred.
</p><div class="variablelist"><dl><dt><span class="term">Namespaces and versioning</span></dt><dd><p>
the need for these is somewhat orthogonal to the work here.
</p><p>Namespaces and versions provide a richer identifier but don't
fundamentally change the nature of the identifier.</p><p>so for now I mostly ignore as they are
(I think) fairly straightforward drudgework to implement, rather than
being fundamentally part of how queries are formed. Global namespaces
are used a little bit for identifying datasets between runs (see tag URI
section)
</p></dd><dt><span class="term">Prospective provenance</span></dt><dd><p>
SwiftScript source programs don't have as
close a similarity to their retrospective structure as in VDL1, so a bunch
of thought required here. Is this required? Is it different from the
SwiftScript program-library point?
</p></dd><dt><span class="term">A database of all logged information</span></dt><dd><p>though it would be interesting
to see what could be done there. straightforward to import eg.
.event and/or .transition files from log parsers into the DB.
</p></dd><dt><span class="term">Replica management</span></dt><dd><p>
No specific replica location or management support. However see sections
on general metadata handling (in as much as general metadata can support
replica location as a specific metadata usecase); and also the section on
global naming in the to-be-discussed section. This ties in with the
Logical File Names concept somehow.
</p></dd><dt><span class="term">A library for SwiftScript code</span></dt><dd><p>need better uses for this and
some indication that a more conventional version control system is
not more appropriate.
</p><p>
Also included in this exclusion is storage of type definitions.
Its straightforward to store type names; but the definitions are
per-execution. More usecases would be useful here to figure out what sort
of query people want to make.
</p></dd><dt><span class="term">Live status updates of in-progress workflows</span></dt><dd><p>
this may happen if data goes
into the DB during run rather than at end (which may or may not happen).
also need to deal with slightly different data - for example, execute2s
that ran but failed (which is not direct lineage provenance?)
</p><p>
so - one successful invocation has: one execute, one execute2 (the most recent),
and maybe one kickstart record. it doesn't track execute2s and kickstarts for
failed execution attempts (nor, perhaps, for failed workflows at all...)
</p></dd><dt><span class="term">Deleting or otherwise modifying provenance data</span></dt><dd><p>
Deleting or otherwise modifying provenance data. Though deleting/modifying
other metadata should be taken into account.
</p></dd><dt><span class="term">Security</span></dt><dd><p>There are several approaches
here. The most native approach is to use the security model of the
underlying database (which will vary depending on which database is used).
</p><p>This is a non-trivial area, especially to do with any richness.
Trust relationships between the various parties accessing the database
should be taken into account.
</p></dd><dt><span class="term">A new metadata or provenance query language</span></dt><dd><p>Designing a (useful - i.e. usable and performing) database
and query language is a non-trivial exercise (on the order of years).
</p><p>
For now, use existing query languages and their implementations. Boilerplate
queries can be developed around those languages.
</p><p>
One property of this is that there will not be a uniform query language for
all prototypes. This is contrast to the VDS1 VDC which had a language which
was then mapped to at least SQL and perhaps some XML query language too.
</p><p>
An intermediate / alternative to something language-like is a much more
tightly constrained set of library / template queries with a very constrained
set of parameters.
</p><p>
Related to this is the avoidance as much as possible of mixing models; so that
one query language is needed for part of a query, and another query language
is needed for another part of a query. An example of this in practice is the
storage of XML kickstart records as blobs inside a relational database in the
VDS1 VDC. SQL could be used to query the containing records, whilst an
XML query language had to be used to query inner records. No benefit could
be derived there from query language level joining and query optimisation;
instead the join had to be implemented poorly by hand.
</p></dd><dt><span class="term">An elegant collection mechanism for provenance or
metadata</span></dt><dd><p>
The prototypes here collect their information through log stripping. This
may or may not be the best way to collect the data. For example, hooks
inside the code might be a better way.
</p></dd></dl></div></div><div class="section" title="5. Data model"><div class="titlepage"><div><div><h2 class="title"><a name="id3049417"></a>5. Data model</h2></div></div></div><div class="section" title="5.1. Introduction to the data model"><div class="titlepage"><div><div><h3 class="title"><a name="id3049422"></a>5.1. Introduction to the data model</h3></div></div></div><p>
All of the prototypes use a basic data model that is strongly
related to the structure of data in the log files; much of the naming here
comes from names in the log files, which in turn often comes from source
code procedure names.
</p><p>
The data model consists of the following data objects:
</p><p>execute - an execute represents a procedure call in a
SwiftScript program.
</p><p>execute2 - an execute2 is an attempt to actually execute an
'execute' object.</p><p>dataset - a dataset is data used by a Swift program. this might be
a file, an array, a structure, or a simple value.</p><p>workflow - a workflow is an execution of an entire SwiftScript
program</p></div><div class="section" title="5.2. execute"><div class="titlepage"><div><div><h3 class="title"><a name="id3049458"></a>5.2. execute</h3></div></div></div><p>
<em class="firstterm">execute</em> - an 'execute' is an execution of a
procedure call in a SwiftScript program. Every procedure call in a
SwiftScript program corresponds to either
one execute (if the execution was attempted) or zero (if the workflow was
abandoned before an execution was attempted). An 'execute' may encompass
a number of attempts to run the appropriate procedure, possibly on differnt
sites. Those attempts are contained within an execute as execute2 entities.
Each execute is related to zero or more datasets - those passed as inputs
and those that are produced as outputs.
</p></div><div class="section" title="5.3. execute2"><div class="titlepage"><div><div><h3 class="title"><a name="id3049480"></a>5.3. execute2</h3></div></div></div><p>
<em class="firstterm">execute2</em> - an 'execute2' is an attempt to run a
program on some grid site. It consists of staging in input files, running the
program, and staging out the output files. Each execute2 belongs to exactly
one execute. If the database is storing only successful workflows and
successful executions, then each execute will be associated with
exactly one execute2. If storing data about unsuccessful workflows or
executions, then each execute may have zero or more execute2s.
</p></div><div class="section" title="5.4. dataset"><div class="titlepage"><div><div><h3 class="title"><a name="id3049500"></a>5.4. dataset</h3></div></div></div><p>
A dataset represents data within a
SwiftScript program. A dataset can be an array, a structure, a file or
a simple value. Depending on the nature of the dataset it may have some
of the following attributes: a value (for example, if the dataset
represents an integer); a filename (if the dataset represents a file);
child datasets (if the dataset represents a structure or an array); and
parent dataset (if the dataset is contained with a structure or an
array).
</p><p>
At present, each dataset corresponds to exactly one in-memory DSHandle
object in the Swift runtime environment; however this might not continue
to be the case - see the discussion section on cross-dataset run
identification.
</p><p>Datasets may be related to executes, either as datasets
taken as inputs by an execute, or as datasets produced by an execute.
A dataset may be produced as an output by at most one execute. If it is not
produced by any execute, it is an <em class="firstterm">input to the workflow</em>
and has been produced through some other mechanism. Multiple datasets may
have the same filename - for example, at present, each time the same file
is used as an input in different workflows, a different dataset appears in
the database. this might change. multiple workflows might (and commonly do)
output files with the same name. at present, these are different datasets,
but are likely to remain that way to some extent - if the contents of files
is different then the datasets should be regarded as distinct.
</p></div><div class="section" title="5.5. workflow"><div class="titlepage"><div><div><h3 class="title"><a name="id3049544"></a>5.5. workflow</h3></div></div></div><p>
<em class="firstterm">workflow</em> - a 'workflow' is an execution of an
entire SwiftScript program. Each execute belongs to exactly one workflow. At
present, each dataset also belongs to exactly one workflow (though the
discussion section talks about how that should not necessarily be the case).
</p></div><p>TODO: diagram of the dataset model (similar to the one in the
provenance paper but probably different). design so that in the XML
model, the element containment hierarchies can be easily marked in a
different colour</p></div><div class="section" title="6. Prototype Implementations"><div class="titlepage"><div><div><h2 class="title"><a name="id3109688"></a>6. Prototype Implementations</h2></div></div></div><p>
I have made a few prototype implementations to explore ways of storing
and querying provenance data.
</p><p>
The basic approach is: build on the log-processing code, which knows how
to pull out lots of information from the log files and store it in a
structured text format; extend Swift to log additional information as
needed;
write import code which knows
how to take the log-processing structured files and put them into whatever
database/format is needed by the particular prototype.
</p><p>
If it is desirable to support more than one of these storage/query mechanisms
(perhaps because
they have unordered values of usability vs query expessibility) then
perhaps should be core provenance output code which is somewhat
agnostic to storage system (equivalent to the post-log-processing
text files at the moment) and then some relatively straightforward set
of importers which are doing little more than syntax change
(cf. it was easy to adapt the SQL import code to make
prolog code instead)
</p><p>
The script <span class="command"><strong>import-all</strong></span> will import into the
basic SQL and eXist XML databases.
</p><div class="section" title="6.1. Relational, using SQL"><div class="titlepage"><div><div><h3 class="title"><a name="id3109729"></a>6.1. Relational, using SQL</h3></div></div></div><p>There are a couple of approaches based around relational databases
using SQL. The plain SQL approach allows many queries to be answered, but
does provide particularly easy querying for the transitive relations
(such as the 'preceeds' relation mentioned elsewhere); ameliorating this 
problem is point of the second model.
</p><div class="section" title="6.1.1. Plain SQL"><div class="titlepage"><div><div><h4 class="title"><a name="id3109742"></a>6.1.1. Plain SQL</h4></div></div></div><p>In this model, the provenance model is mapped to a relational
schema, stored in sqlite3 and queried with SQL.
</p><p>
This prototype uses sqlite3 on my laptop. The <span class="command"><strong>import-all</strong></span>
will initialise and import into this database (and also into the XML DB).
</p><p>
example query - counts how many of each procedure have been called.
</p><pre class="screen">
sqlite&gt; select procedure_name, count(procedure_name) from executes, invocation_procedure_names where executes.id = invocation_procedure_names.execute_id group by procedure_name;
align|4
average|1
convert|3
slicer|3
</pre><p>
</p><p>
needs an SQL database. sqlite is easy to get (from standard OS software
repos, and from globus toolkit) so this is not as horrible as it seems. 
setup requirements for sqlite are minimal.
</p><p>
metadata: one way is to handle them as SQL relations. this allows them
to be queried using SQL quite nicely, and to be indexed and joined on
quite easily.
</p><p>
prov query 1:Find the process that led to Atlas X Graphic / everything that caused Atlas X Graphic to be as it is. This should tell us the new brain images from which the averaged atlas was generated, the warping performed etc.
</p><div class="section" title="6.1.1.1. Description of tables"><div class="titlepage"><div><div><h5 class="title"><a name="id3109798"></a>6.1.1.1. Description of tables</h5></div></div></div><p>
Executions are stored in a table called 'executes'. Each execution has the fields: id - a globally unique ID for that execution; starttime - the start time
of the execution attempt, in seconds since the unix epoch (this is roughly
the time that swift decides to submit the task, *not* the time that a worker
node started executing the task); duration - in seconds (time from start time
to swift finally finishing the execution, not the actual on-worker execution
time); final state (is likely to always be END_SUCCESS as the present import
code ignores failed tasks, but in future may include details of failures;
app - the symbolic name of the application
</p><p>
Details of datasets are stored in three tables: dataset_filenames,
dataset_usage and dataset_containment.
</p><p>
dataset_filenames maps filenames (or more generally URIs) to unique dataset
identifiers.
</p><p>
dataset_usage maps from unique dataset identifiers to the execution
unique identifiers for executions that take those datasets as inputs
and outputs. execute_id and dataset_id identify the execution and the
procedure which are related. direction indicates whether this dataset
was used as an input or an output. param_name is the name of the parameter
in the SwiftScript source file.
</p><p>
dataset_containment indicates which datasets are contained within others,
for example within a structure or array. An array or structure is a dataset
with its own unique identifier; and each member of the array or structure
is again a dataset with its own unique identifier. The outer_dataset_id and
inner_dataset_id fields in each row indicate respectively the
containing and contained dataset.
</p></div></div><div class="section" title="6.1.2. SQL with Pre-generated Transitive Closures"><div class="titlepage"><div><div><h4 class="title"><a name="id3047915"></a>6.1.2. SQL with Pre-generated Transitive Closures</h4></div></div></div><p>SQL does not allow expression of transitive relations. This causes a
problem for some of the queries.</p><p>Work has previously been done (cite) to work on pre-generating
transitive closures over relations. This is similar in concept to the
pregenerated indices that SQL databases traditionally provide.
</p><p>In the pre-generated transitive closure model, a transitive closure
table is pregenerated (and can be incrementally maintained as data is added
to the database). Queries are then made against this table instead of
against the ground table.
</p><p>All of the data available in the earlier SQL model is available, in
addition to the additional closures generated here.</p><p>
Prototype code: There is a script called <code class="literal">prov-sql-generate-transitive-closures.sh</code> to generate the close of the preceeds
relation and places it in a table called <code class="literal">trans</code>:
</p><pre class="screen">
$ prov-sql-generate-transitive-closures.sh 
Previous: 0 Now: 869
Previous: 869 Now: 1077
Previous: 1077 Now: 1251
Previous: 1251 Now: 1430
Previous: 1430 Now: 1614
Previous: 1614 Now: 1848
Previous: 1848 Now: 2063
Previous: 2063 Now: 2235
Previous: 2235 Now: 2340
Previous: 2340 Now: 2385
Previous: 2385 Now: 2396
Previous: 2396 Now: 2398
Previous: 2398 Now: 2398
</pre><p>
</p><p>A note on timing - constructing the closure of 869 base relations,
leading to 2398 relations in the closure takes 48s with no indices; adding
an index on a column in the transitive relations table takes this time down
to 1.6s. This is interesting as an example of how some decent understanding
of the data structure to produce properly optimised queries and the like
is very helpful in scaling up, and an argument against implementing a poor
'inner system'.
</p><p>Now we can reformulate some of the queries from the SQL section
making use of this table.
</p><p>
There's some papers around about transitive closures in SQL:

<a class="ulink" href="http://coblitz.codeen.org:3125/citeseer.ist.psu.edu/cache/papers/cs/554/http:zSzzSzsdmc.krdl.org.sgzSzkleislizSzpsZzSzdlsw-ijit97-9.pdf/dong99maintaining.pdf" target="_top">'Maintaining transitive closure of graphs in SQL'</a>
and
<a class="ulink" href="http://willets.org/sqlgraphs.html" target="_top">http://willets.org/sqlgraphs.html</a>
</p><p>
how expensive is doing this? how cheaper queries? how more expensive is
adding data? and how scales (in both time and size (eg row count)) as we
put in more rows (eg. i2u2 scale?) exponential, perhaps? though
the theoretical limit is going to be influenced by our usage pattern
which I think for the most part will be lots of disjoint graphs
(I think). we get to index the transitive closure table, which we don't
get to do when making the closure at run time.
</p><p>We don't have the path(s) between nodes but we could store that in the
closure table too if we wanted (though multiple paths would then be more
expensive as there are now more unique rows to go in the closure table)</p><p>
This is a violation of normalisation which the traditional relational people
would say is bad, but OLAP people would say is ok.
</p><p>
how much easier does it make queries?
for queries to root, should be much easier (query over
transitive table almost as if over base table). but queries such as
'go back n-far then stop' and the like harder to query.
</p><p>
keyword: 'incremental evaluation system' (to maintain transitive closure)
</p><p>
The difference between plain SQL and SQL-with-transitive-closures
is that in SQL mode, construction occurs at query time and the query
needs to specify that construction. In the transitive-close mode,
construction occurs at data insertion time, with increased expense there
and in size of db, but cheaper queries (I think).
</p><p>
sample numbers: fmri example has 50 rows in base causal relation
table. 757 in table with transitive close.
</p><p>
If running entirely separate workflows, both those numbers will scale linearly
with the number of workflows; however, if there is some crossover between
subsequent workflows in terms of shared data files then the transitive
graph will grow super-linearly.
</p></div></div><div class="section" title="6.2. XML"><div class="titlepage"><div><div><h3 class="title"><a name="id3110140"></a>6.2. XML</h3></div></div></div><p>In this XML approach, provenance data and metadata is represented as 
a set of XML documents.</p><p>Each document is stored in some kind of document store.
Two different document stores are used: 
the posix filesystem and eXist. XPath and XQuery are investigated as
query languages.</p><p>semi-structuredness allows structured metadata without having to
necessarily declare its schema (which I think is one of the desired properties
that turns people off using plain SQL tables to reflect the metadata
schema). but won't get indexing without some configuration of structure so
whilst that will be nice for small DBs it may be necessary to scale up
(though that in itself isn't a problem - it allows gentle start without
schema declaration and to scale up, add schema declarations later on - fits
in with the scripting style). semi-structured form of XML lines up very
will with the desire to have semi-structured metadata. compare ease of
converting other things (eg fmri showheader output) to loose XML - field
relabelling without having to know what the fields actually are - to how
this needs to be done in SQL.
</p><p>
The hierarchical structure of XML perhaps better for dataset containment
because we can use // operator which is transitive down the tree for
dataset containment.
</p><p>
XML provides a more convenient export format than SQL or the other formats
in terms of an easily parseable file format. There are lots of
tools around for processing XML files in various different ways (for example,
treating as text-like documents; deserialising into Java in-memory objects
based on an XML Schema definition), and XML is one of the most familiar
structured data file formats.
</p><p>
Not sure what DAG representation would look like here? many (one per arc)
small documents? is that a problem for the DBs? many small elements, more
likely, rather than many small documents - roughly one document per workflow.
</p><div class="section" title="6.2.1. xml metadata"><div class="titlepage"><div><div><h4 class="title"><a name="id3110196"></a>6.2.1. xml metadata</h4></div></div></div><p>in the XML model, two different ways of putting in metadata: as descendents of the
appropriate objects (eg. dataset metadata under the relevant datasets). this
is most xml-like in the sense that its strongly hierarchical. as separate
elements at a higher level (eg. separate documents in xml db). the two ways
are compatible to the extent that some metadata can be stored one way, some
the other way, although the way of querying each will be different.
</p><p>
way i: at time of converting provenance data into XML, insert metadata at
appropriate slots (though if XML storage medium allows, it could be inserted
later on).
</p><p>
modified <span class="command"><strong>prov-to-xml.sh</strong></span> to put that info in for
the appropriate datasets (identified using the below descripted false-filename
method</p><p>
can now make queries such as 'tell me the datasets which have header metadata':
</p><pre class="screen">
cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '//dataset[headermeta]'
</pre><p>
</p><p>
way ii: need to figure out what the dataset IDs for the volumes are. At the
moment, the filename field for (some) mapped dataset parents still 
has a filename
even though that file never exists, like below. This depends on the mapper
being able to invent a filename for such. Mappers aren't guaranteed to be
able to do that - eg where filenames are not formed as a function of the
parameters and path, but rely on eg. whats in the directory at initialisation
(like the filesystem mapper).
</p><pre class="screen">
&lt;dataset identifier="10682109"&gt;
&lt;filename&gt;file://localhost/0001.in&lt;/filename&gt;
&lt;dataset identifier="12735302"&gt;
&lt;filename&gt;file://localhost/0001.h.in&lt;/filename&gt;
&lt;/dataset&gt;
&lt;dataset identifier="7080341"&gt;
&lt;filename&gt;file://localhost/0001.v.in&lt;/filename&gt;
&lt;/dataset&gt;
</pre><p>
so we can perhaps use that. The mapped filename here is providing a
dataset identification (by chance, not by design) so we can take advantage
of it:
</p><pre class="screen">
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance//dataset[filename="file://localhost/0001.in"]/@identifier'
10682109
</pre><p>
</p><p>I think metadata in XML is more flexible than metadata in relational,
in terms of not having to define schema and not having to stick to schema.
However, how will it stand up to the challenge of scalability? Need to get
a big DB. Its ok to say that indices need to be made - I don't dispute that.
What's nice is that you can operate at the low end without such. So need to
get this stuff being imported into eg eXist (maybe the prototype XML processing
should look like -&gt; XML doc(s) on disk -&gt; whatever xmldb in order to
facilitate prototyping and pluggability.)
</p></div><div class="section" title="6.2.2. XPath query language"><div class="titlepage"><div><div><h4 class="title"><a name="id3110288"></a>6.2.2. XPath query language</h4></div></div></div><p>
XPath queries can be run either against the posix file system store
or against the eXist database. When using eXist, the opportunity exists
for more optimised query processing (and indeed, the eXist query processing
model appears to evaluate queries in an initially surprising and unintuitive
way to get speed); compared to on the filesystem, 
where XML is stored in serialised form and must be parsed for each query.
</p><p>
xml generation:
</p><pre class="screen">
./prov-to-xml.sh &gt; /tmp/prov.xml
</pre><p>
and basic querying with xpathtool (http://www.semicomplete.com/projects/xpathtool/)
</p><pre class="screen">
cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/provenance/execute[thread="0-4-1"]' 
</pre><p>
</p><p>
q1:
</p><pre class="screen">
cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/provenance//dataset[filename="file://localhost/0001.jpeg"]'       
&lt;toplevel&gt;
  &lt;dataset identifier="14976260"&gt;
    &lt;filename&gt;file://localhost/0001.jpeg&lt;/filename&gt;
  &lt;/dataset&gt;
&lt;/toplevel&gt;
</pre><p>
or can get the identifier like this:
</p><pre class="screen">
 cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance//dataset[filename="file://localhost/0001.jpeg"]/@identifier' 
14976260
</pre><p>
can also request eg IDs for multiple, like this:
</p><pre class="screen">
cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance//dataset[filename="file://localhost/0001.jpeg"]/@identifier|/provenance//dataset[filename="file://localhost/0002.jpeg"]/@identifier' 
</pre><p>
</p><p>
can find the threads that use this dataset like this:
</p><pre class="screen">
 cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/provenance/tie[dataset=/provenance//dataset[filename="file://localhost/0001.jpeg"]/@identifier]' 
&lt;toplevel&gt;
  &lt;tie&gt;
    &lt;thread&gt;0-4-3&lt;/thread&gt;
    &lt;direction&gt;output&lt;/direction&gt;
    &lt;dataset&gt;14976260&lt;/dataset&gt;
    &lt;param&gt;j&lt;/param&gt;
    &lt;value&gt;org.griphyn.vdl.mapping.DataNode hashCode 14976260 with no value at dataset=final path=[1]&lt;/value&gt;
  &lt;/tie&gt;
</pre><p>
</p><p>
now we can iterate as in the SQL example:
</p><pre class="screen">
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[thread="0-4-3"][direction="input"]/dataset'
4845856
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[dataset="4845856"][direction="output"]/thread'
0-3-3
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[thread="0-3-3"][direction="input"]/dataset'
3354850
6033476
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[dataset="3354850"][direction="output"]/thread'
0-2
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[thread="0-2"][direction="input"]/dataset'
4436324
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[dataset="4436324"][direction="output"]/thread'
</pre><p>
</p><p>so now we've exhausted the tie relation - dataset 4436324 comes from
elsewhere...</p><p>
so we say this:
</p><pre class="screen">
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/dataset[@identifier="4436324"]//dataset/@identifier'
11153746
7202698
12705705
7202698
12705705
655223
2088036
13671126
2088036
13671126
5169861
14285084
12896050
14285084
12896050
6487148
5772360
4910675
5772360
4910675
</pre><p>
which gives us (non-unique) datasets contained within dataset 4436324. We can
uniquify outside of the language:
</p><pre class="screen">
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/dataset[@identifier="4436324"]//dataset/@identifier' | sort |uniq
11153746
12705705
12896050
13671126
14285084
2088036
4910675
5169861
5772360
6487148
655223
7202698
</pre><p>
and now need to find what produced all of those... iterate everything again.
probably we can do it integrated with the previous query so that we
don't have to iterate externally:
</p><pre class="screen">
$ cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/provenance/tie[dataset=/provenance/dataset[@identifier="4436324"]//dataset/@identifier]'
&lt;?xml version="1.0"?&gt;
&lt;toplevel&gt;
  &lt;tie&gt;
    &lt;thread&gt;0-1-3&lt;/thread&gt;
    &lt;direction&gt;output&lt;/direction&gt;
    &lt;dataset&gt;5169861&lt;/dataset&gt;
    &lt;param&gt;o&lt;/param&gt;
    &lt;value&gt;org.griphyn.vdl.mapping.DataNode hashCode 5169861 with no value at dataset=aligned path=[4]&lt;/value&gt;
  &lt;/tie&gt;
  &lt;tie&gt;
    &lt;thread&gt;0-1-4&lt;/thread&gt;
    &lt;direction&gt;output&lt;/direction&gt;
    &lt;dataset&gt;6487148&lt;/dataset&gt;
    &lt;param&gt;o&lt;/param&gt;
    &lt;value&gt;org.griphyn.vdl.mapping.DataNode hashCode 6487148 with no value at dataset=aligned path=[1]&lt;/value&gt;
  &lt;/tie&gt;
  &lt;tie&gt;
    &lt;thread&gt;0-1-2&lt;/thread&gt;
    &lt;direction&gt;output&lt;/direction&gt;
    &lt;dataset&gt;655223&lt;/dataset&gt;
    &lt;param&gt;o&lt;/param&gt;
    &lt;value&gt;org.griphyn.vdl.mapping.DataNode hashCode 655223 with no value at dataset=aligned path=[2]&lt;/value&gt;
  &lt;/tie&gt;
  &lt;tie&gt;
    &lt;thread&gt;0-1-1&lt;/thread&gt;
    &lt;direction&gt;output&lt;/direction&gt;
    &lt;dataset&gt;11153746&lt;/dataset&gt;
    &lt;param&gt;o&lt;/param&gt;
    &lt;value&gt;org.griphyn.vdl.mapping.DataNode hashCode 11153746 with no value at dataset=aligned path=[3]&lt;/value&gt;
  &lt;/tie&gt;
&lt;/toplevel&gt;
</pre><p>
which reveals only 4 ties to procedures from those datasets - the elements
of the aligned array. We can get just the thread IDs for that by adding
/thread onto the end:
</p><pre class="screen">
 cat /tmp/prov.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh '/provenance/tie[dataset=/provenance/dataset[@identifier="4436324"]//dataset/@identifier]/thread'
0-1-3
0-1-4
0-1-2
0-1-1
</pre><p>
so now we need to iterate over those four threads as before using same
process.
</p><p>so we will ask 'which datasets does this contain?' because at
present, a composite dataset will ultimately be produced by its component
datasets (though I think perhaps we'll end up with apps producing datasets
that are composites, eg when a file is output that then maps into some
structure - eg file contains  (1,2)   and this maps to struct { int x; int y;}.
TODO move this para into section on issues-for-future-discussion.
</p><p>so xpath here doesn't really seem too different in expressive ability
from the SQL approach - it still needs external implementation of
transitivity for some of the transitive relations (though not for
dataset containment). and that's a big complicating factor for ad-hoc queries...
</p><div class="section" title="6.2.2.1. notes on using eXist"><div class="titlepage"><div><div><h5 class="title"><a name="id3110509"></a>6.2.2.1. notes on using eXist</h5></div></div></div><p><a class="ulink" href="http://exist.sourceforge.net/client.html" target="_top">command line
client doc</a></p><p>
run in command line shell with local embedded DB (not running inside a
server, so analogous to using sqlite rather than postgres):
</p><pre class="screen">
~/work/eXist/bin/client.sh -s -ouri=xmldb:exist://
</pre><p>
</p><p>
import a file:
</p><pre class="screen">
~/work/eXist/bin/client.sh -m /db/prov -p `pwd`/tmp.xml  -ouri=xmldb:exist://
</pre><p>
note that the -p document path is relative to exist root directory, not to
the pwd, hence the explicit pwd.
</p><p>
xpath query from commandline:
</p><pre class="screen">
 echo '//tie' |  ~/work/eXist/bin/client.sh -ouri=xmldb:exist:// -x
</pre><p>
</p></div></div><div class="section" title="6.2.3. XSLT"><div class="titlepage"><div><div><h4 class="title"><a name="id3110560"></a>6.2.3. XSLT</h4></div></div></div><p>
very much like when we revieved xpath, xslt and xquery for MDS data - these
are the three I'll consider for the XML data model? does XSLT add anything?
not sure. for now I think not so ignore, or at least comment that it does
not add anything.
</p><p>
</p><pre class="screen">
./prov-to-xml.sh &gt; /tmp/prov.xml
xsltproc ./prov-xml-stylesheet.xslt /tmp/prov.xml
</pre><p>
with no rules will generate plain text output that is not much use.
</p><p>
Two potential uses: i) as a formatting language for stuff coming out of
some other (perhaps also XSLT, or perhaps other language) query process.
and ii) as that other language doing semantic rather than presentation
level querying (better names for those levels?)
</p></div><div class="section" title="6.2.4. XQuery query language"><div class="titlepage"><div><div><h4 class="title"><a name="id3110592"></a>6.2.4. XQuery query language</h4></div></div></div><p>
Build query results for this using probably the same database as the
above XPath section, but indicating where things could be better expressed
using XPath.
</p><p>
Using XQuery with eXists:
</p><pre class="screen">
$ cat xq.xq
//tie
$ ~/work/eXist/bin/client.sh -ouri=xmldb:exist:// -F `pwd`/xq.xq
</pre><p>
</p><p>
A more advanced query:
</p><pre class="screen">
for $t in //tie
  let $dataset := //dataset[@identifier=$t/dataset]
  let $exec := //execute[thread=$t/thread]
  where $t/direction="input"
  return &lt;r&gt;An invocation of {$exec/trname} took input {$dataset/filename}&lt;/r&gt;
</pre><p>
</p></div></div><div class="section" title="6.3. RDF and SPARQL"><div class="titlepage"><div><div><h3 class="title"><a name="id3110631"></a>6.3. RDF and SPARQL</h3></div></div></div><p>
This can probably also be extended to SPARQL-with-transitive-closures
using the same methods as 1; or see OWL note below.
</p><p>
Pegasus/WINGS queries could be interesting to look at here - they
are from the same tradition as Swift. However, the don't deal very
well with transitivity.
</p><p>OWL mentions transitivity as something that can be expressed in
an OWL ontology but are there any query languages around that can
make use of that kind of information?
</p><p>See prolog section on RDF querying with prolog.
</p><p>
There's an RDF-in-XML format for exposing information in serialised form.
Same discussion applies to this as to the discussion in XML above.
</p></div><div class="section" title="6.4. GraphGrep"><div class="titlepage"><div><div><h3 class="title"><a name="id3110664"></a>6.4. GraphGrep</h3></div></div></div><pre class="screen">
 - download link see email
<a class="ulink" href="http://www.cs.nyu.edu/shasha/papers/graphgrep/" target="_top">graphgrep</a>
graphgrep install notes: port install db3
some hack patches to get it to build with db3
</pre><p>
Got a version of graph grep with interesting graph language apparently in it.
Haven't tried it yet though.
</p></div><div class="section" title="6.5. prolog"><div class="titlepage"><div><div><h3 class="title"><a name="id3110689"></a>6.5. prolog</h3></div></div></div><p>Perhaps interesting querying ability here. Probably slow? but not
really sure - SWI Prolog talks about indexing its database (and allowing
the indexing to be customised) and about supporting very large databases.
So this sounds hopeful.
</p><p>
convert database into SWI prolog. make queries based on that.
</p><p>Can make library to handle things like transitive relations - should be
easy to express the transitivity in various different ways (dataset
containment, procedure-ordering, whatever) - far more clear there than
in any other query language.</p><p>
SWI Prolog has some RDF interfacing, so this is clearly a realm that is
being investigated by some other people. For example:
</p><div class="blockquote"><blockquote class="blockquote">It is assumed that Prolog is a suitable vehicle to reason with the data expressed in RDF models -- http://www.swi-prolog.org/packages/rdf2pl.html</blockquote></div><p>
</p><p><a class="ulink" href="http://www.xml.com/pub/a/2001/04/25/prologrdf/index.html" target="_top">
http://www.xml.com/pub/a/2001/04/25/prologrdf/index.html
</a>
</p><p>
prolog can be used over RDF or over any other tuples. stuff in SQL
tables should map neatly too. Stuff in XML hieararchy perhaps not so
easily but should still be doable.</p><p>indeed, SPARQL queries have a very prolog-like feel to them
superficially.
</p><p>prolog db is a program at the moment - want something that looks more
like a persistent modifiable database. not sure what the prolog approach
to doing that is.</p><p>
so maybe prolog makes an interesting place to do future research on
query language? not used by this immediate work but a direction to
do query expressibility research (building on top of whatever DB is used
for this round?)
</p><p>q1 incremental:

</p><pre class="screen">
?- dataset_filenames(Dataset,'file://localhost/0001.in').

Dataset = '10682109' ;
</pre><p>

Now with lib.pl:

</p><pre class="screen">
dataset_trans_preceeds(Product, Source) :-
   dataset_usage(Thread, 'O', Product, _, _),
   dataset_usage(Thread, 'I', Source, _, _).


dataset_trans_preceeds(Product, Source) :-
   dataset_usage(Thread, 'O', Product, _, _),
   dataset_usage(Thread, 'I', Inter, _, _),
   dataset_trans_preceeds(Inter, Source).
</pre><p>

then we can ask:

</p><pre class="screen">
?- dataset_trans_preceeds('14976260',S).

S = '4845856' ;

S = '3354850' ;

S = '6033476' ;

S = '4436324' ;

No
</pre><p>

which is all the dataset IDs up until the point that we get into
array construction. This is the same iterative problem we have
in the SQL section too - however, it should be solvable in the prolog case
within prolog in the same way that the recursion is. so now:

</p><pre class="screen">
base_dataset_trans_preceeds(Product, Source, Derivation) :-
   dataset_usage(Thread, 'O', Product, _, _),
   dataset_usage(Thread, 'I', Source, _, _),
   Derivation = f(one).

base_dataset_trans_preceeds(Product, Source, Derivation) :-
   dataset_containment(Product, Source),
   Derivation = f(two).

dataset_trans_preceeds(Product, Source, Derivation) :-
    base_dataset_trans_preceeds(Product, Source, DBase),
    Derivation = [DBase].

dataset_trans_preceeds(Product, Source, Derivation) :-
   base_dataset_trans_preceeds(Product, Inter, DA),
   dataset_trans_preceeds(Inter, Source, DB),
   Derivation = [DA|DB].
</pre><p>

</p><p>q4:

</p><pre class="screen">
invocation_procedure_names(Thread, 'align_warp'), dataset_usage(Thread, Direction, Dataset, 'model', '12'), execute(Thread, Time, Duration, Disposition, Executable),  format_time(atom(DayOfWeek), '%u', Time), DayOfWeek = '5'.
TODO format this multiline, perhaps remove unused bindings
</pre><p>
</p></div><div class="section" title="6.6. amazon simpledb"><div class="titlepage"><div><div><h3 class="title"><a name="id3110823"></a>6.6. amazon simpledb</h3></div></div></div><p>restricted beta access... dunno if i will get any access - i have
none so far, though I have applied.</p><p>
From reading a bit about it, my impressions are that this will prove to be
a key-&gt;value lookup mechanism with poor support for going the other way
(eg. value or value pattern or predicate-on-value  -&gt; key) or for doing
joins (so rather like a hash table - which then makes me say 'why not also
investigate last year's buzzword of DHTs?'. I think that these additional
lookup mechanisms are probably necessary for a lot of the
query patterns.
</p><p>
For some set of queries, though, key -&gt; value lookup is sufficient; and
likely the set of queries that is appropriate to this model varies depending
on how the key -&gt; value model is laid out (i.e. what gets to be a key
and what is its value? do we form a hierarchy from workflow downwards?)
</p></div><div class="section" title="6.7. graphviz"><div class="titlepage"><div><div><h3 class="title"><a name="id3110860"></a>6.7. graphviz</h3></div></div></div><p>
This is a very different approach that is on the boundaries of relevance.
</p><p>
goal: produce an annotated graph showing the procedures and the
datasets, with appropriate annotation of identifiers and
descriptive text (eg filenames, procedure names, executable names) that
for small (eg. fmri sized workflows) its easy to get a visual view of
whats going on.
</p><p>
don't target anything much bigger than the fmri example for this.
(though there is maybe some desire to produce larger visualisations for
this - perhaps as a separate piece of work. eg could combine foreach
into single node, datasets into single node)
</p><p>
perhaps make subgraphs by the various containment relationships:
datasets in same subgraph as their top level parent;
app procedure invocations in the same subgraph as their compound
procedure invocation.
</p></div></div><div class="section" title="7. Comparison with related work that our group has done before"><div class="titlepage"><div><div><h2 class="title"><a name="id3110894"></a>7. Comparison with related work that our group has done
before</h2></div></div></div><div class="section" title="7.1. vs VDS1 VDC"><div class="titlepage"><div><div><h3 class="title"><a name="id3110899"></a>7.1. vs VDS1 VDC</h3></div></div></div><p>gendax - VDS1 has a tool <span class="command"><strong>gendax</strong></span> which provides
various ways of accessing data from the command line. Eg. prov challenge
question 1 very easily answered by this.
</p><p>
two points I don't like that should discuss here: i) the metadata schema
(I claim there doesn't need to be a generic metadata schema at all - 
when applications decide they want to store certain metadata, they declare
it in the database); and ii) the mixed-model - this is discussed a bit in
the 'no general query language' section. consolidate/crosslink.
</p></div><div class="section" title="7.2. vs VDL provenance paper figure 1 schema"><div class="titlepage"><div><div><h3 class="title"><a name="id3110927"></a>7.2. vs VDL provenance paper figure 1 schema</h3></div></div></div><p>
The significant differences are:
(TODO perhaps produce a diagram for comparison. could use same diagram
differently annotated to indicate trees in the XML section and also
in the transitivity discussion section)
</p><p>
the 'annotation' model - screw that, go
native</p><p>the dataset containment model, which doesn't exist in the
virtual dataset model.
</p><p>
workflow object has a fromDV and toDV field. what are
those meant to mean? In present model, there isn't any base data for a workflow
at the moment - everything can be found in the descriptions of its
components (such as files used, start time, etc). (see notes on
compound procedure containment with model of a workflow as a compound
procedure)
</p><p>invocation to call to procedure chain. this chain looks different.
there are executes (which look like invocations/calls) and procedure names
(which do not exist as primary objects because I am not storing
program code). kickstart records and execute2 records would be more like
the annotations you'd get from the annotation part, with the call being
more directly associated with the execute object.
</p></div></div><div class="section" title="8. Questions/Discussion points"><div class="titlepage"><div><div><h2 class="title"><a name="id3110969"></a>8. Questions/Discussion points</h2></div></div></div><div class="section" title="8.1. metadata"><div class="titlepage"><div><div><h3 class="title"><a name="id3110973"></a>8.1. metadata</h3></div></div></div><p>
discourse analysis: Perhaps the word 'metadata' should be banned in
this document - it implies that there is
some special property that distinguishes it sufficiently from normal
data such that it must be treated differently from different data.
I don't believe this to be the case.
</p><p>
script <span class="command"><strong>prov-mfd-meta-to-xml</strong></span> that generates (fake)
metadata record in XML like this:
</p><pre class="screen">$ ./prov-mfd-meta-to-xml 123
&lt;headermeta&gt;
  &lt;dataset&gt;123&lt;/dataset&gt;
  &lt;bitspixel&gt;16&lt;/bitspixel&gt;
  &lt;xdim&gt;256&lt;/xdim&gt;
  &lt;ydim&gt;256&lt;/ydim&gt;
  &lt;zdim&gt;128&lt;/zdim&gt;
  &lt;xsize&gt;1.000000e+00&lt;/xsize&gt;
  &lt;ysize&gt;1.000000e+00&lt;/ysize&gt;
  &lt;zsize&gt;1.250000e+00&lt;/zsize&gt;
  &lt;globalmaximum&gt;4095&lt;/globalmaximum&gt;
  &lt;globalminimum&gt;0&lt;/globalminimum&gt;
&lt;/headermeta&gt;
</pre><p>
</p><div class="section" title="8.1.1. metadata random notes"><div class="titlepage"><div><div><h4 class="title"><a name="id3111013"></a>8.1.1. metadata random notes</h4></div></div></div><p>
metadata: there's a model of arbitrary metadata pairs being
associated with arbitrary objects.</p><p>there's another model (that I tend to
favour) in that the metadata schema is more defined than this - eg in i2u2
for any particular elab, the schema for metadata is fairly well defined.
</p><p>
eg in cosmic, there are strongly typed fields such as "blessed" or
"detector number" that
are hard-coded throughout the elab. whilst the VDS1 VDC can deal with
arbitrary typing, that's not the model that i2u2/cosmic is using. need to be
careful to avoid the inner-platform effect here especially - "we need a
system that can do arbitrarily typed metadata pairs" is not actually a
requirement in this case as the schema is known at application build time.
(note that this matters for SQL a lot, not so much for plain XML data model,
though if we want to specify things like 'is-transitive' properties then
in any model things like that need to be better defined)
</p><p>
fMRI provenance challenge metadata (extracted using scanheader) looks like
this:
</p><pre class="screen">
$ /Users/benc/work/fmri-tutorial/AIR5.2.5/bin/scanheader ./anatomy0001.hdr
bits/pixel=16
x_dim=256
y_dim=256
z_dim=128
x_size=1.000000e+00
y_size=1.000000e+00
z_size=1.250000e+00

global maximum=4095
global minimum=0
</pre><p>
</p></div></div><div class="section" title="8.2. The 'preceeds' relation"><div class="titlepage"><div><div><h3 class="title"><a name="id3111060"></a>8.2. The 'preceeds' relation</h3></div></div></div><div class="section" title="8.2.1. Provenance of hierarchical datasets"><div class="titlepage"><div><div><h4 class="title"><a name="id3111065"></a>8.2.1. Provenance of hierarchical datasets</h4></div></div></div><p>
One of the main provenance queries is whether some entity (a
data file or a procedure) was influenced by some other entity.
</p><p>
In VDS1 a workflow is represented by a bipartite DAG where one
vertex partition is files and the other is procedures.
</p><p>
The more complex data structures in Swift make the provenance graph
not so straightforward. Procedures input and output datasets that may
be composed of smaller datasets and may in turn be composed into larger
datasets.
</p><p>
For example, a dataset D coming out of a procedure P may form a part
of a larger dataset E. Dataset E may then be an input to procedure Q.
The ordering is then:
</p><pre class="screen">
 P --output--&gt; D --contained-by-&gt; E --input--&gt; Q
</pre><p>
</p><p>
Conversely, a dataset D coming out of a procedure P may contain a
smaller dataset E. Dataset E may then be used as an input to procedure
Q.
</p><pre class="screen">
 P --output--&gt; D --contains--&gt; E --input--&gt; Q
</pre><p>
</p><p>
So the contains relation and its reverse, the contained-by relation, do not
in the general case seem to give an appropriate preceeds relation.

</p><pre class="screen">
so: i) should Q1&lt;-&gt;Q be a bidirection dependency (in which case we
  no longer have a DAG, which causes trouble)

or

    ii) the dependency direction between Q1 and Q depends on how Q and Q1
were constructed. I think this is the better approach, because I think
there really is some natural dependency order.

If A writes to Q1 and Q1 is part of Q then A-&gt;Q1-&gt;Q
If A writes to Q and Q1 is part of Q then A-&gt;Q-&gt;Q1

So when we write to a dataset, we need to propagate out the dependency
from there (both upwards and downwards, I think).

eg. if Q1X is part of Q1 is part of Q
and A writes to Q1, then Q1X depends on Q1 and Q depends on Q1.


</pre><p>

</p><p>
Various ways of doing closure - we have various relations in the graph
such as dataset containment and procedure input/output. Need to figure out
how this relates to predecessor/successors in the provenance sense.

</p><pre class="screen">
A(
Also there are multiple levels of tracking (see the section on that):

If an app procedure produces eg
volume v, consisting of two files v.img and v.hdr (the fmri example)
then what is the dependency here? I guess v.img and v.hdr is the
output... (so in the present model there will never be
downward propagation as every produced dataset will be produced out of
base files. however its worth noting that this perhaps not always the
case...)

Alternatively we can model at the level of the app procedure, which in
the above case returns a volume v.

I guess this is similar to the case of the compound procedures vs
contained app procedures...

If we model at the level of files, then we don't really need to know
about higher datasets much?

Perhaps for now should model at level of procedure calls
)A

List A()A above as an issue and pick one choice - for now, lowest=file
production, so that all intermediate and output datasets will end up
with a strictly upward dependency

This rule does not deal with input-only datasets (that is, datasets
which we do not know where they came from). It would be fairly natural
with the above choice to again make dependencies from files upward.

So for now, dataset dependency rule is:

  * parent datasets depend on their children.

Perhaps?
</pre><p>
</p></div><div class="section" title="8.2.2. Transitivity of relations in query language"><div class="titlepage"><div><div><h4 class="title"><a name="id3111181"></a>8.2.2. Transitivity of relations in query language</h4></div></div></div><p>
One of my biggest concerns in query languages such as SQL and XPath
is lack of decent transitive query ability.
</p><p>
I think we need a main relation, the <em class="firstterm">preceeds</em>
relation. None of the relations defined in the source provenance data
provides this relation.
</p><p>The relation needs to be such that if any dataset or program Y that
contributed to the production of any other dataset or program Y, then
X preceeds Y.
</p><p>
We can construct pieces of this relation from the existing relations:
</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>There are fairly simple rules for procedure inputs and
outputs:
A dataset passed as an input to a procedure preceeds that procedure.
Similarly, a procedure that outputs a dataset preceeds that dataset.
</p></li><li class="listitem"><p>
Hierarchical datasets are straightforward to describe in the present
implementation. Composite data structures are always described in terms
of their members, so the members of a data structure always preceed
the structures that contain them. [not true, i think - we can pass a
struct into a procedure and have that procedure populate multiple
contained files... bleugh]
</p></li><li class="listitem"><p>
The relation is transitive, so the presence of some relations by the
above rules will imply the presence of other relations to ensure
transitivity.
</p></li></ul></div><p>
</p></div></div><div class="section" title="8.3. Unique identification of provenance objects"><div class="titlepage"><div><div><h3 class="title"><a name="id3111244"></a>8.3. Unique identification of provenance objects</h3></div></div></div><p>
A few issues - what are the objects that should be identified? (semantics);
and how should the objects be identified? (syntax).
</p><div class="section" title="8.3.1. provenence object identifier syntax"><div class="titlepage"><div><div><h4 class="title"><a name="id3111254"></a>8.3.1. provenence object identifier syntax</h4></div></div></div><p>
For syntax, I favour a URI-based approach and this is what I have
implemented in the prototypes. URIs rovide a ready made system for
identifying different kinds of objects in different ways within the
same syntax.
which should be useful for the querys that want to do that.
file, gsiftp URIs for filenames. probably should be normalising file
URIs to refer to a specific hostname? otherwise they're fairly
meaningless outside of one host...
also, these name files but files are mutable.
</p><p>
its also fairly straightforward to subsume other identifier schemes into
URIs (for example, that is already done for UUIDs, in RFC4122).
</p><p>
for other IDs, such as workflow IDs, a tag or uuid URI would be nice.
</p><p>
cite: <a class="ulink" href="http://www.rfc-editor.org/rfc/rfc4151.txt" target="_top">RFC4151</a>
</p><div class="blockquote"><blockquote class="blockquote">
The tag algorithm lets people mint -- create -- identifiers that no one else using the same algorithm could ever mint. It is simple enough to do in your head, and the resulting identifiers can be easy to read, write, and remember. The identifiers conform to the URI (URL) Syntax.
</blockquote></div><p>
</p><p>
cite:  <a class="ulink" href="http://www.rfc-editor.org/rfc/rfc4122.txt" target="_top">RFC4122</a>
</p><div class="blockquote"><blockquote class="blockquote">
This specification defines a Uniform Resource Name namespace for
UUIDs (Universally Unique IDentifier), also known as GUIDs (Globally
Unique IDentifier).  A UUID is 128 bits long, and requires no central
registration process.
</blockquote></div><p>
</p><div class="section" title="8.3.1.1. tag URIs"><div class="titlepage"><div><div><h5 class="title"><a name="id3111320"></a>8.3.1.1. tag URIs</h5></div></div></div><p>
tag URIs for identifiers of provenance objects:
</p><p>
all URIs allocated according to this section are labelled beginning with one
of:
</p><pre class="screen">
tag:benc@ci.uchicago.edu,2007:swift:
tag:benc@ci.uchicago.edu,2008:
</pre><p>
</p><p>
for datasets identified only within a run (that is, for example, anything
that doesn't have a filename):
tag:benc@ci.uchicago.edu,2007:swift:dataset:TIMESTAMP:SEQ
with TIMESTAMP being a timestamp of sometime near the start of the run,
intending to be a unique workflow id (probably better to use the
run-id)
and SEQ being a sequence number. However, shouldn't really be pulling any
information out of these time and seq fields.
</p><p>
for executes - this is based on the karajan thread ID and the log base
filename (which is assumed to be a globally unique identifying string):
tag:benc@ci.uchicago.edu,2007:swiftlogs:execute:WFID:THREAD with,
as for datasets, WFID is a workflow-id-like entity.
</p></div></div><div class="section" title="8.3.2. Dataset identifier semantics"><div class="titlepage"><div><div><h4 class="title"><a name="crossrun-id"></a>8.3.2. Dataset identifier semantics</h4></div></div></div><p>At present, dataset identifiers are formed uniquely for every
dataset object created in the swift runtime (unique across JVMs as well
as within a JVM).</p><p>This provides an overly sensitive(?) identity - datasets
which are the same will be given different dataset identifiers at
different times/places; although two different datasets will never be
given the same identifier.
</p><p>A different approach would be to say 'datasets are made of files,
so we want to identify files, and files already have identiers called
filenames'.</p><p>I think this approach is also insufficient.</p><p>The assertion 'datasets are made of files' is not correct. Datasets
come in several forms: typed files, typed simple values, and typed
collections of other datasets. Each of these needs a way to identify it.
</p><p>
Simple values are probably the easiest to identify. They can be identified
by their own value and embedded within a suitable URI scheme. For example,
a dataset representing the integer 7 could be identified as:
</p><pre class="screen">
tag:benc@ci.uchicago.edu,2008:swift:types:int:7
</pre><p>
This would have the property that all datasets representing the integer
7 would be identical (that is, have the same identifier).
</p><p>
Collections of datasets are more complicated. One interesting example of
something that feels to me quite similar is the treatment of directories
in hash-based file systems, such as git. In this model, a collection of
datasets would be represented by a hash of a canonical representation of
its contenst, for example, a dataset consisting of a three element array
of three files in this order: "x-foo:red", "x-foo:green" and "x-foo:blue"
might be represented as:
</p><pre class="screen">
tag:benc@ci.uchicago.edu,2008:collection:QHASH
</pre><p>
where:
</p><pre class="screen">
QHASH := sha1sum(QLONG)

QLONG := "[0] x-foo:red [1] x-foo:green [2] x-foo:blue"
</pre><p>
This allows a repeatable computation of dataset identifiers given 
only knowledge of the contents of the dataset. Specifically it does
not rely on a shared database to map content to identifier. However,
it can only be computed when the content of the dataset is fully known
(roughly equivalent to when the dataset is closed in the Swift
runtime)
</p><p>
For identifying a dataset that is a file, there are various properties.
Filename is one property. File content is another property. It seems
desirable to distinguish between datasets that have the same name yet
have different content, whilst identifying datasets that have the same
content. To this end, an identifier might be constructed from both the
filename and a hash of the content.
</p><p>
for prototype could deal only with files staged to local system,
so that we can easily compute a hash over the content.
</p><p>related to taking md5sums, kickstart provides the first few bytes
of certain files (the executable and specified input and output files);
whilst useful for basic sanity checks, there are very strong correlations
with magic numbers and common headers that make this a poor content
identifying function. perhaps it should be absorbed as dataset metadata if
its available?
</p><p>TODO the following para needs to rephrase as justification for
having identities for dataset collections  ::: at run-time when can we
pick up the
identities from other runs? pretty much we want identity to be expressed
in some way so that we can get cross-run linkup.
how do we label a dataset such that we can annotate it - eg in fmri
example, how do we identify the input datasets (as file pairs) rather than
the individual files?</p><p>
Its desirable to give the same dataset the same identifier in multiple
runs; and be able to figure out that dataset identifier outside of a run,
for example for the purposes of dealing with metadata that is annotating
a dataset.
</p></div><div class="section" title="8.3.3. File content tracking"><div class="titlepage"><div><div><h4 class="title"><a name="id3111488"></a>8.3.3. File content tracking</h4></div></div></div><p>
identify file contents with md5sum (or other hash) - this is somewhat
expensive, but without it we have (significantly) lessened belief in what the
contents of a file are - we would otherwise, I think, be using only names
and relying on the fact that those names are primary keys to file content
(which is not true in general).
so this should be perhaps optional. plus where to do it? various places...
in wrapper.sh?
</p><p>
References here for using content-hashes:
git, many of the DHTs (freenet, for example - amusing to cite the classic
freenet gpl.txt example)
</p></div></div><div class="section" title="8.4. Type representation"><div class="titlepage"><div><div><h3 class="title"><a name="id3111511"></a>8.4. Type representation</h3></div></div></div><p>
how to represent types in this? for now use names, but that doesn't
go cross-program because we can define a different type with the same
name in every different program. hashtree of type definitions?
</p></div><div class="section" title="8.5. representation of workflows"><div class="titlepage"><div><div><h3 class="title"><a name="id3111524"></a>8.5. representation of workflows</h3></div></div></div><p>
perhaps need a workflow object that acts something like a namespace
but implicitly definted rather than being user labelled (hence capturing
the actual runtime space rather than what the user claims). that's the
runID, I guess.
</p><p>
Also tracking of workflow source file. Above-mentioned reference to
tracking file contents applies to this file too.
</p></div><div class="section" title="8.6. metadata extraction"><div class="titlepage"><div><div><h3 class="title"><a name="id3111542"></a>8.6. metadata extraction</h3></div></div></div><p>
provenance challenge I question 5 reports about pulling fields out of the
headers of one of the input files. There's a program, scanheader, that
extracts this info. Related but not actually useful, I think, for this
question is that header fields could be mapped into SwiftScript if we
allowed value+file simultaneous data structures.
</p></div><div class="section" title="8.7. source code recreation"><div class="titlepage"><div><div><h3 class="title"><a name="id3111557"></a>8.7. source code recreation</h3></div></div></div><p>
should the output of the queries be sufficient to regenerate the
data? the most difficult thing here seems to be handling data
sets - we have the mapping tree for a dataset, but what is the right
way to specify that in swift syntax? maybe need mapper that takes a
literal datastructure and maps the filenames from it. though that
doesn't account for file contents (so this bit of this point is
the file contents issue, which should perhaps be its own chapter
in this file)
</p></div><div class="section" title="8.8. Input parameters"><div class="titlepage"><div><div><h3 class="title"><a name="id3111574"></a>8.8. Input parameters</h3></div></div></div><p>
Should also work on workflows which take an input parameter, so that we
end up with the same output file generated several times with different
output values - eg pass a string as a parameter and write that to
'output.txt' - every time we run it, the file will be different, and we'll
have multiple provenance reports indicating how it was made, with different
parameters. that's a simple demonstration of the content-tracking which
could be useful.
</p><p>
If we're tracking datasets for simple values, I think we get this
automatically. The input parameters are input datasets in the same way
that input files are input datasets; and so fit into the model in the
same way.
</p></div></div><div class="section" title="9. Open Provenance Model (OPM)"><div class="titlepage"><div><div><h2 class="title"><a name="opm"></a>9. Open Provenance Model (OPM)</h2></div></div></div><div class="section" title="9.1. OPM-defined terms and their relation to Swift"><div class="titlepage"><div><div><h3 class="title"><a name="id3111607"></a>9.1. OPM-defined terms and their relation to Swift</h3></div></div></div><p>
OPM defines a number of terms. This section describes how those terms
relate to Swift.
</p><p>
artifact: This OPM term maps well onto the internal Swift representation
of <code class="literal">DSHandle</code>s. Each DSHandle in a Swift run is an
OPM artifact, and each OPM artifact in a graph is a DSHandle.
</p><p>collection: OPM collections are a specific kind of artifact, containing
other artifacts. This corresponds with DSHandles for composite data types
(structs and arrays). OPM has collection accessors and collection
constructors which correspond to the <code class="literal">[]</code> and
<code class="literal">.</code> operators (for accessors) and various assignment
forms for constructors.
</p><p>
process: An OPM process corresponds to a number of Swift concepts (although
they are slowly converging in Swift to a single concept). Those concepts
are: procedure invocations, function calls, and operators.
</p><p>
agent: There are several entities which can act as an agent. At the
highest level where only Swift is involved, a run of the
<code class="literal">swift</code> commandline client is an agent which drives
everything. Some other components of Swift may be regarded as agents,
such as the client side wrapper script. For present OPM work, the
only agent will be the Swift command line client invocation.
</p><p>
account: For present OPM work, there will be one account per workflow run.
In future, different levels of granularity that could be expressed through
different accounts might include representing compound procedure calls as
processes vs representing atomic procedures calls explicitly.
</p><p>
OPM graph: there are two kinds of OPM graph that appear interesting and
straightforward to export: i) of entire provenance database (thus containing
multiple workflow runs); ii) of a single run
</p></div><div class="section" title="9.2. OPM links"><div class="titlepage"><div><div><h3 class="title"><a name="id3111687"></a>9.2. OPM links</h3></div></div></div><p><a class="ulink" href="http://twiki.ipaw.info/bin/view/Challenge/OPM" target="_top">Open Provenance Model at ipaw.info</a></p></div><div class="section" title="9.3. Swift specific OPM considerations"><div class="titlepage"><div><div><h3 class="title"><a name="id3111700"></a>9.3. Swift specific OPM considerations</h3></div></div></div><p>
non-strictness: Swift sometimes lazily constructs collections (leading to
the notion in Swift of an array being closed, which means that we know no
more contents will be created, somewhat like knowing we've reached the end
of a list). It may be that an array is never closed during a run, but that
we still have sufficient provenance information to answer useful queries
(for example, if we specify a list [1:100000] and only refer to the 5th
element in that array, we probably never generate most of the DSHandles...
so an explicit representation of that array in terms of datasets cannot be
expressed - though a higher level representation of it in terms of its
constructor parameters can be made) (?)
</p><p>
aliasing: (this is related to some similar ambiguity in other parts of
Swift, to do with dataset roots - not provenance related). It is possible to
construct arrays by explicitly listing their members:
</p><pre class="programlisting">
int i = 8;
int j = 100;
int a[] = [i,j];
int k = a[1];
// here, k = 8
</pre><p>
The dataset contained in <code class="literal">i</code> is an artifact (a literal, so
some input artifact that has no creating process). The array
<code class="literal">a</code> is an artifact created by the explicit array construction
syntax <code class="literal">[memberlist]</code> (which is an OPM process). If we
then model the array accessor syntax <code class="literal">a[1]</code> as an OPM
process, what artifact does it return? The same one or a different one?
In OPM, we want it to return a different artifact; but in Swift we want this
to be the same dataset... (perhaps explaining this with <code class="literal">int</code>
type variables is not the best way - using file-mapped data might be better)
TODO: what are the reasons we want files to have a single dataset
representation in Swift? dependency ordering - definitely. cache management?
Does this lead to a stronger notion of aliasing in Swift?
</p><p>
Provenance of array indices: It seems fairly natural to represent arrays as OPM
collections, with array element extraction being a process. However, in OPM,
the index of an array is indicated with a role (with suggestions that it might
be a simple number or an XPath expression). In Swift arrays, the index is
a number, but it has its own provenance, so by recording only an integer there,
we lose provenance information about where that integer came from - that
integer is a Swift dataset in its own right, which has its own provenance.
It would be nice to be able to represent that (even if its not standardised
in OPM). I think that needs re-ification of roles so that they can be
described; or it needs treatment of [] as being like any other binary
operator (which is what happens inside swift) - where the LHS and RHS are
artifacts, and the role is not used for identifying the member (which would
also be an argument for making array element extraction be treated more
like a plain binary operator inside the Swift compiler and runtime)
</p><p>
provenance of references vs provenance of the data in them: the array and
structure access operators can be used to acquire <code class="literal">DSHandle</code>s
which have no value yet, and which are then subsequently assigned. In this
usage, the provenance of the containing structure should perhaps be that it
is constructed from the assignments made to its members, rather than the
other way round. There is some subtlety here that I have not fully figured
out.
</p><p>
Piecewise construction of collections: arrays and structs can be
constructed piecewise using <code class="literal">. =</code> and <code class="literal">[] =</code>.
how is this to be represented in OPM? perhaps the closing operation maps
to the OPM process that creates the array, so that it ends up looking
like an explicit array construction, happening at the time of the close?
</p><p>
Provenance of mapper parameters: mapper parameters are artifacts. We can
represent references to those in a Swift-specific part of an artifacts
value, perhaps. Probably not something OPM-generalisable.
</p></div></div><div class="section" title="10. Processing i2u2 cosmic metadata"><div class="titlepage"><div><div><h2 class="title"><a name="id3111841"></a>10. Processing i2u2 cosmic metadata</h2></div></div></div><p>i2u2 cosmic metadata is extracted from a VDS1 VDC.</p><p>
TODO some notes here about how I dislike the inner-plaform effect in the
metadata part of the VDS1 VDC.
</p><p>
to launch postgres on soju.hawaga.org.uk:
</p><pre class="screen">
sudo -u postgres /opt/local/lib/postgresql82/bin/postgres -D  /opt/local/var/db/postgresql82/defaultdb
</pre><p>

and then to import i2u2 vdc data as VDC1 vdc:

</p><pre class="screen">
$ /opt/local/lib/postgresql82/bin/createdb -U postgres i2u2vdc1
CREATE DATABASE
$ psql82 -U postgres -d i2u2vdc1 &lt; work/i2u2.vdc 
gives lots of errors like this:
ERROR:  role "portal2006_1022" does not exist
because indeed that role doesn't exist
but I think that doesn't matter for these purposes - everything will end
up being owned by the postgres user which suffices for what I want to do.
</pre><p>

</p><p>
annotation tables are:
</p><pre class="screen">
 public | anno_bool       | table | postgres   29214 rows
  this is boolean values

 public | anno_call       | table | postgres   0 rows
- this is a subject table. also has did

 public | anno_date       | table | postgres   52644 rows
   this is date values

 public | anno_definition | table | postgres   1849 rows
    this is XML-embedded derivations (values / objects)

 public | anno_dv         | table | postgres   0 rows
- this is a subject table. also has did

 public | anno_float      | table | postgres   27966 rows
    this is float values

 public | anno_int        | table | postgres   58879 rows
    this is int values

 public | anno_lfn        | table | postgres   411490 rows
    this is the subject record for LFN subjects - subjects have an
    mkey (predicate) column
  
 public | anno_lfn_b      | table | postgres
this appears to be keyed by did field - ties dids to what looks like
LFNs

 public | anno_lfn_i      | table | postgres
 public | anno_lfn_o      | table | postgres
likewise these two

 public | anno_targ       | table | postgres
is this a subject table? it has an mkey value that always appears to be
'description' and then has a name column which lists invocation parameter
names and ties them to dids.

 public | anno_text       | table | postgres   242824 rows
text values (objects)

 public | anno_tr         | table | postgres
</pre><p>
</p><p>
most of the interesting data starts in anno_lfn because data is mostly
annotating LFNs:
</p><pre class="screen">
i2u2vdc1=# select * from anno_lfn limit 1;
 id |        name         |   mkey   
----+---------------------+----------
  2 | 180.2004.0819.0.raw | origname
</pre><p>
There are 63 different mkeys (predicates in RDF-speak):
</p><pre class="screen">
i2u2vdc1=# select distinct mkey from anno_lfn;
             mkey
------------------------------
 alpha
 alpha_error
 author
 avgaltitude
 avglatitude
 avglongitude
 background_constant
 background_constant_error
 bins
 blessed
 caption
 chan1
 chan2
 chan3
 chan4
 channel
 city
 coincidence
 comments
 cpldfrequency
 creationdate
 date
 description
 detectorcoincidence
 detectorid
 dvname
 enddate
 energycheck
 eventcoincidence
 eventnum
 expire
 filename
 gate
 gatewidth
 group
 height
 julianstartdate
 lifetime(microseconds)
 lifetime_error(microseconds)
 name
 nondatalines
 numBins
 origname
 plotURL
 project
 provenance
 radius
 rawanalyze
 rawdate
 school
 source
 stacked
 startdate
 state
 study
 teacher
 thumbnail
 time
 title
 totalevents
 transformation
 type
 year
(63 rows)
</pre><p>
so work on a metadata importer for i2u2 cosmic that will initially deal
with only the lfn records.
</p><p>
There are 19040 annotated LFNs, with 411490 annotations in total, so about
21 annotations per LFN.
</p><p>The typing of the i2u2 data doesn't support metadata objects
that aren't swift workflow entities - for example high schools as
objects in their own right - the same text string is stored as a value
over and over in many anno_text rows. A more generalised 
Subject-Predicate-Object model in RDF would have perhaps a URI for
the high school, with metadata on files tying files to a high school and
metadata on the high school object. In SQL, that same could be modelled
in a relational schema.
</p><p>
Conversion of i2u2 VDS1 VDC LFN/text annotations into an XML document
using quick hack script took 32mins on soju, my laptop. resulting XML
is 8mb. needed some manual massage to remove malformed embedded xml and
things like that.
</p><pre class="screen">
./i2u2-to-xml.sh &gt;lfn-text-anno.xml
</pre><p>

so we end up with a lot of records that look like this:

</p><pre class="screen">
&lt;lfn name="43.2007.0619.0.raw"&gt;
&lt;origname&gt;rgnew.txt&lt;/origname&gt;
&lt;group&gt;riogrande&lt;/group&gt;
&lt;teacher&gt;Luis Torres Rosa&lt;/teacher&gt;
&lt;school&gt;Escuelo Superior Pedro Falu&lt;/school&gt;
&lt;city&gt;Rio Grande&lt;/city&gt;
&lt;state&gt;PR&lt;/state&gt;
&lt;year&gt;AY2007&lt;/year&gt;
&lt;project&gt;cosmic&lt;/project&gt;
&lt;comments&gt;&lt;/comments&gt;
&lt;detectorid&gt;43&lt;/detectorid&gt;
&lt;type&gt;raw&lt;/type&gt;
&lt;avglatitude&gt;18.22.8264&lt;/avglatitude&gt;
&lt;avglongitude&gt;-65.50.1975&lt;/avglongitude&gt;
&lt;avgaltitude&gt;-30&lt;/avgaltitude&gt;
&lt;/lfn&gt;
</pre><p>

The translation here is not cosmic-aware - the XML tag is the mkey name from
vdc and the content is the value. So we get all the different metadata
(informal) schemas that appear to have been used, translated.

</p><p>
Output the entire provenance database:
</p><pre class="screen">
$ time cat lfn-text-anno.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/cosmic'  | wc -c
 10178037

real    0m2.624s
user    0m2.612s
sys     0m0.348s
</pre><p>
</p><p>
Select all LFN objects (which on this dataset means everything one layer
down):
</p><pre class="screen">
$ time cat lfn-text-anno.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/cosmic/lfn'  | wc -c
 9618818

real    0m2.692s
user    0m2.703s
sys     0m0.337s
</pre><p>
</p><p>
Try to select an LFN that doesn't exist, by specifying a filename that is not
there:
</p><pre class="screen">
$ time cat lfn-text-anno.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/cosmic/lfn[@name="NOSUCHNAME"]'
&lt;?xml version="1.0"?&gt;
&lt;toplevel/&gt;

real    0m0.867s
user    0m0.740s
sys     0m0.143s
</pre><p>
</p><p>
Similar query for a filename that does exist:
</p><pre class="screen">
$ time cat lfn-text-anno.xml | ~/work/xpathtool-20071102/xpathtool/xpathtool.sh --oxml '/cosmic/lfn[@name="1.2005.0801.0"]'
&lt;?xml version="1.0"?&gt;
&lt;toplevel&gt;
  &lt;lfn name="1.2005.0801.0"&gt;
    &lt;origname&gt;C:\Documents and Settings\zsaleh\My Documents\Tera stuff\Qnet\Qnet Data\All_data_Aug_01_2005_TERA_9_Vth_1000.TXT&lt;/origname&gt;
    &lt;group&gt;TERA&lt;/group&gt;
    &lt;teacher&gt;Marcus Hohlmann&lt;/teacher&gt;
    &lt;school&gt;Florida Institute of Technology&lt;/school&gt;
    &lt;city&gt;Melbourne&lt;/city&gt;
    &lt;state&gt;FL&lt;/state&gt;
    &lt;year&gt;AY2004&lt;/year&gt;
    &lt;project&gt;cosmic&lt;/project&gt;
    &lt;comments/&gt;
    &lt;source&gt;1.2005.0801.0&lt;/source&gt;
    &lt;detectorid&gt;1&lt;/detectorid&gt;
    &lt;type&gt;split&lt;/type&gt;
  &lt;/lfn&gt;
&lt;/toplevel&gt;

real    0m0.875s
user    0m0.745s
sys     0m0.154s
</pre><p>
</p></div><div class="section" title="11. processing fMRI metadata"><div class="titlepage"><div><div><h2 class="title"><a name="id3112106"></a>11. processing fMRI metadata</h2></div></div></div><p>
for fmri, we can extract embedded image metadata using the scanheader
utility.
</p><p>
associate that with the 'volume' dataset, not with the actual image data
files. for now that means we need the datasets to have been labelled with
their ID already, which is at the moment after execution has completed.
that's fine for now with the retrospective provenance restriction of this
immediate work. see the 
<a class="link" href="#crossrun-id" title="8.3.2. Dataset identifier semantics">'cross-run dataset ID' section</a>, for which this
also applies - we are generating dataset IDs outside of a particular run.
</p></div><div class="section" title="12. random unsorted notes"><div class="titlepage"><div><div><h2 class="title"><a name="id3112135"></a>12. random unsorted notes</h2></div></div></div><p>
to put provdb in postgres instead of sqlite3:

start as per i2u2 instructions, then <span class="command"><strong>/opt/local/lib/postgresql82/bin/createdb -U postgres provdb</strong></span>

then:
<span class="command"><strong>
 psql82 -U postgres -d provdb &lt; prov-init.sql 
</strong></span> to initialise the db.
</p><p>
on terminable, made new database that is not the default system db install,
by using existing postgres but running under my user id:
</p><pre class="screen">
  131  mkdir pgplay
  133  chmod 0700 pgplay/
  135  initdb -D ~/pgplay/
  138  postmaster -D ~/pgplay/ -p 5435
$ createdb -p 5435 provdb
CREATE DATABASE
</pre><p>
now can access like this:
</p><pre class="screen">
$ psql -p 5435 -d provdb
provdb=# \dt
No relations found.
</pre><p>
</p><p>osg/gratia - how does this data tie in?
</p><p>cedps logging - potential for info there but there doesn't seem
anything particularly substantial at the moment
</p></div><div class="section" title="13. Provenance Challenge 1 examples"><div class="titlepage"><div><div><h2 class="title"><a name="id3112189"></a>13. Provenance Challenge 1 examples</h2></div></div></div><div class="section" title="13.1. Basic SQL"><div class="titlepage"><div><div><h3 class="title"><a name="id3112194"></a>13.1. Basic SQL</h3></div></div></div><div class="section" title="13.1.1. provch q1"><div class="titlepage"><div><div><h4 class="title"><a name="id3112199"></a>13.1.1. provch q1</h4></div></div></div><pre class="screen">
get the dataset id for the relevant final dataset:
sqlite&gt; select * from dataset_filenames where filename like '%0001.jpeg';
14976260|file://localhost/0001.jpeg

get containment info for that file:
sqlite&gt; select * from dataset_containment where inner_dataset_id = 14976260;
7316236|14976260
sqlite&gt; select * from dataset_containment where inner_dataset_id = 7316236;
[no answer]

now need to find what contributed to those...

&gt; select * from dataset_usage where dataset_id=14976260;
0-4-3|O|14976260

&gt; select * from dataset_usage where execute_id='0-4-3' and direction='I';
0-4-3|I|4845856
qlite&gt; select * from dataset_usage where dataset_id=4845856 and direction='O';
0-3-3|O|4845856

sqlite&gt; select * from dataset_usage where execute_id='0-3-3' and direction='I';
0-3-3|I|3354850
0-3-3|I|6033476

qlite&gt; select * from dataset_usage where (dataset_id=3354850 or dataset_id=6033476) and direction='O';
0-2|O|3354850

sqlite&gt; select * from dataset_usage where execute_id='0-2' and direction='I';0-2|I|4436324

sqlite&gt; select * from dataset_usage where dataset_id=4436324 and direction='O';
[no answer]

so here we have run out of places to keep going. however, I think this 4436324
is not an input - its related to another dataset. so we need another rule for
inference here...



</pre></div><div class="section" title="13.1.2. prov ch q4"><div class="titlepage"><div><div><h4 class="title"><a name="id3112253"></a>13.1.2. prov ch q4</h4></div></div></div><p>prov ch q4 incremental solutions:</p><p>first cut:
this will select align_warp procedures and their start times. does not
select based on parameters, and does not select based on day of week.
(the former we don't have the information for; the latter maybe don't have
the information in sqlite3 to do - or maybe need SQL date ops and SQL dates
rather than unix timestamps)
</p><pre class="screen">
sqlite&gt; select id, starttime from invocation_procedure_names, executes where executes.id = invocation_procedure_names.execute_id and procedure_name='align_warp';
</pre><p>
</p><p>Next, this will display the day of week for an invocation:</p><p>
</p><pre class="screen">
select id, strftime('%w',starttime, 'unixepoch') from executes,invocation_procedure_names where procedure_name='align_warp' and executes.id=invocation_Procedure_names.execute_id;
0-0-3|5
0-0-4|5
0-0-1|5
0-0-2|5
</pre><p>
</p><p>And this will match day of week (sample data is on day 5, which is a
Friday, not the day requested in the question):
</p><pre class="screen">
sqlite&gt; select id from executes,invocation_procedure_names where procedure_name='align_warp' and executes.id=invocation_Procedure_names.execute_id and strftime('%w',starttime, 'unixepoch') = '5';
0-0-3
0-0-4
0-0-1
0-0-2
</pre><p>
</p><p>
Now we bring in input data binding: we query which datasets were passed in
as the model parameter for each of the above found invocations:
</p><pre class="screen">
sqlite&gt; select executes.id, dataset_usage.dataset_id from executes,invocation_procedure_names, dataset_usage where procedure_name='align_warp' and executes.id=invocation_Procedure_names.execute_id and strftime('%w',starttime, 'unixepoch') = '5' and dataset_usage.execute_id = executes.id and direction='I' and param_name='model';
0-0-3|11032210
0-0-4|13014156
0-0-1|14537849
0-0-2|16166946
</pre><p>
though at the moment this doesn't give us the value of the parameter.
</p><p>so now pull in the parameter value:

</p><pre class="screen">
sqlite&gt; select executes.id, dataset_usage.dataset_id, dataset_usage.value from executes,invocation_procedure_names, dataset_usage where procedure_name='align_warp' and executes.id=invocation_Procedure_names.execute_id and strftime('%w',starttime, 'unixepoch') = '5' and dataset_usage.execute_id = executes.id and direction='I' and param_name='model';
0-0-3|11032210|12
0-0-4|13014156|12
0-0-1|14537849|12
0-0-2|16166946|12
</pre><p>
</p><p>
Now we can select on the parameter value and get our final answer:
</p><pre class="screen">
sqlite&gt; select executes.id from executes,invocation_procedure_names, dataset_usage where procedure_name='align_warp' and executes.id=invocation_Procedure_names.execute_id and strftime('%w',starttime, 'unixepoch') = '5' and dataset_usage.execute_id = executes.id and direction='I' and param_name='model' and dataset_usage.value=12;
0-0-3
0-0-4
0-0-1
0-0-2
</pre><p>
Note that in SQL in general,
we *don't* get typing of the parameter value here so can't do anything
more than string comparison. For example, we couldn't check for the
parameter being greater than 12 or similar. In sqlite, it happens that
its typing is dynamic enough to allow the use of relational operators like
&gt; on fields no matter what their declared type, because declared type is
ignored. This would stop working if stuff was run on eg postgres or mysql,
I think.
</p></div><div class="section" title="13.1.3. prov ch metadata"><div class="titlepage"><div><div><h4 class="title"><a name="id3112376"></a>13.1.3. prov ch metadata</h4></div></div></div><p>metadata: in the prov challenge, we annotate (some) files with
their header info. in the provenance paper, we want annotations on more
than just files.
</p><p>for prov ch metadata, define a scanheader table with the result of
scanheader on each input dataset, but do it *after* we've done the
run (because we're then aware of dataset IDs)</p><p>There's a representation question here - the metadata is about a volume
dataset which is a pair of files, not about a header or image file separately.
how to represent this? we need to know the dataset ID for the volume. at
the moment, we can know that after a run. but this ties into the
identification of datasets outside of an individual run point - move this
paragraph into that questions/discussions section.
</p><p>
should probably for each storage method show the inner-platform style of
doing metadata too; associated queries to allow comparison with the
different styles; speeds of metadata query for large metadata collections
(eg. dump i2u2 cosmic metadata for real cosmic VDC)
</p></div></div><div class="section" title="13.2. SQL with transitive closures"><div class="titlepage"><div><div><h3 class="title"><a name="id3112413"></a>13.2. SQL with transitive closures</h3></div></div></div><div class="section" title="13.2.1. prov ch question 1:"><div class="titlepage"><div><div><h4 class="title"><a name="id3112418"></a>13.2.1. prov ch question 1:</h4></div></div></div><pre class="screen">
$ sqlite3 provdb
SQLite version 3.3.17
Enter ".help" for instructions
sqlite&gt; select * from dataset_filenames where filename like '%0001.jpeg';
14976260|file://localhost/0001.jpeg
-- can query keeping relations
sqlite&gt; select * from trans where after=14976260;
0-4-3|14976260
4845856|14976260
0-3-3|14976260
3354850|14976260
6033476|14976260
4825541|14976260
7061626|14976260
0-2|14976260
4436324|14976260
11153746|14976260
655223|14976260
5169861|14976260
6487148|14976260
5772360|14976260
4910675|14976260
7202698|14976260
12705705|14976260
2088036|14976260
13671126|14976260
14285084|14976260
12896050|14976260
0-1-3|14976260
0-1-4|14976260
0-1-2|14976260
0-1-1|14976260
2673619|14976260
9339756|14976260
10682109|14976260
8426950|14976260
16032673|14976260
2274050|14976260
1461238|14976260
13975694|14976260
9282438|14976260
12766963|14976260
8344105|14976260
9190543|14976260
14055055|14976260
2942918|14976260
12735302|14976260
7080341|14976260
0-0-3|14976260
0-0-4|14976260
0-0-2|14976260
0-0-1|14976260
2307300|14976260
11032210|14976260
16166946|14976260
14537849|14976260
13014156|14976260
6435309|14976260
6646123|14976260
-- or can query without relations:
sqlite&gt; select before from trans where after=14976260;
0-4-3
4845856
0-3-3
3354850
6033476
4825541
7061626
0-2
4436324
11153746
655223
5169861
6487148
5772360
4910675
7202698
12705705
2088036
13671126
14285084
12896050
0-1-3
0-1-4
0-1-2
0-1-1
2673619
9339756
10682109
8426950
16032673
2274050
1461238
13975694
9282438
12766963
8344105
9190543
14055055
2942918
12735302
7080341
0-0-3
0-0-4
0-0-2
0-0-1
2307300
11032210
16166946
14537849
13014156
6435309
6646123


</pre></div></div></div><div class="section" title="14. Representation of dataset containment and procedure execution in r2681 and how it could change."><div class="titlepage"><div><div><h2 class="title"><a name="id3112460"></a>14. Representation of dataset containment and procedure execution in r2681 and how it could change.</h2></div></div></div><p>
Representation of processes that transform one dataset into another dataset
at present only occurs for <code class="literal">app</code> procedures, in logging of
<code class="literal">vdl:execute</code> invocations, in lines like this:
</p><pre class="screen">
2009-03-12 12:20:29,772+0100 INFO  vdl:parameterlog PARAM thread=0-10-1 direction=input variable=s provenanceid=tag:benc@ci.uchicago.edu,2008:swift:dataset:20090312-1220-md2mfc24:720000000033
</pre><p>
and dataset containment is represented at closing of the containing DSHandle by this:
</p><pre class="screen">
2009-03-12 12:20:30,205+0100 INFO  AbstractDataNode CONTAINMENT parent=tag:benc@ci.uchicago.edu,2008:swift:dataset:20090312-1220-md2mfc24:720000000020 child=tag:benc@ci.uchicago.edu,2008:swift:dataset:20090312-1220-md2mfc24:720000000086
2009-03-12 12:20:30,205+0100 INFO  AbstractDataNode ROOTPATH dataset=tag:benc@ci.uchicago.edu,2008:swift:dataset:20090312-1220-md2mfc24:720000000086 path=[2]
</pre><p>
</p><p>
This representation does not represent the relationship between datasets when
they are related by @functions or operators. Nor does it represent causal
relationships between collections and their members - instead it represents
containment.
</p><p>
Adding representation of operators (including array construction) and of
@function invocations would give substantially more information about
the provenance of many more datasets.
</p></div></div>
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
