<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>Historical: SwiftScript Language Reference Manual</title><meta name="generator" content="DocBook XSL Stylesheets V1.75.2"><link rel="home" href="index.html" title="Historical: SwiftScript Language Reference Manual"><link href="http://www.ci.uchicago.edu/swift/css/style1col.css" rel="stylesheet" type="text/css"><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/dhtml.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shCoreu.js"></script><script type="text/javascript" src="http://www.ci.uchicago.edu/swift/shBrushVDL2.js"></script></head><body onLoad="initjs();sh();" class="section-3">
		
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
		
		<div class="article" title="Historical: SwiftScript Language Reference Manual"><div class="titlepage"><div><div><h2 class="title"><a name="id2817288"></a>Historical: SwiftScript Language Reference Manual</h2></div><div><h3 class="subtitle"><i>work-in-progress, $LastChangedRevision: 2611 $</i></h3></div><div><div class="author"><h3 class="author"><span class="firstname">Yong</span> <span class="surname">Zhao</span></h3></div></div></div><hr></div><div class="toc"><p><b>Table of Contents</b></p><dl><dt><span class="sect1"><a href="#id2884502">1. Introduction</a></span></dt><dt><span class="sect1"><a href="#id2884589">2. Namespaces</a></span></dt><dt><span class="sect1"><a href="#id2884642">3. Lexical structure</a></span></dt><dd><dl><dt><span class="sect2"><a href="#id2885329">3.1. Comments</a></span></dt><dt><span class="sect2"><a href="#id2885360">3.2. Identifiers</a></span></dt><dt><span class="sect2"><a href="#id2885623">3.3. Keywords</a></span></dt><dt><span class="sect2"><a href="#id2945919">3.4. Literals</a></span></dt><dt><span class="sect2"><a href="#id2946505">3.5. Operators and Separators</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2946662">4. Type Definitions</a></span></dt><dd><dl><dt><span class="sect2"><a href="#id2946679">4.1. Primitive types</a></span></dt><dt><span class="sect2"><a href="#id2946700">4.2. Composite types</a></span></dt><dt><span class="sect2"><a href="#id2946713">4.3. Arrays</a></span></dt><dt><span class="sect2"><a href="#id2946737">4.4. Structs</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2946749">5. Datasets</a></span></dt><dt><span class="sect1"><a href="#id2946838">6. Mapping</a></span></dt><dt><span class="sect1"><a href="#id2946900">7. Variables</a></span></dt><dd><dl><dt><span class="sect2"><a href="#id2946919">7.1. Global variables</a></span></dt><dt><span class="sect2"><a href="#id2946935">7.2. Local variables</a></span></dt><dt><span class="sect2"><a href="#id2946980">7.3. Dataset-bound variable</a></span></dt><dt><span class="sect2"><a href="#id2947002">7.4. Scopes</a></span></dt><dt><span class="sect2"><a href="#id2947019">7.5. Variable references</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2947074">8. Procedure Definitions</a></span></dt><dd><dl><dt><span class="sect2"><a href="#id2947425">8.1. Atomic procedure body</a></span></dt><dt><span class="sect2"><a href="#id2948141">8.2. Compound procedure body</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2948263">9. Expressions</a></span></dt><dd><dl><dt><span class="sect2"><a href="#id2948273">9.1. Primary Expressions</a></span></dt><dt><span class="sect2"><a href="#id2949056">9.2. Operators</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2949145">10. Statements</a></span></dt><dd><dl><dt><span class="sect2"><a href="#sect10_1">10.1. Namespace Statement</a></span></dt><dt><span class="sect2"><a href="#id2949342">10.2. Include Statements</a></span></dt><dt><span class="sect2"><a href="#id2949407">10.3. Type Definitions</a></span></dt><dt><span class="sect2"><a href="#id2949934">10.4. Declaration Statements</a></span></dt><dt><span class="sect2"><a href="#id2950555">10.5. Expression Statements</a></span></dt><dt><span class="sect2"><a href="#id2950577">10.6. Selection Statements</a></span></dt><dt><span class="sect2"><a href="#id2950988">10.7. Loop statements</a></span></dt><dt><span class="sect2"><a href="#id2951523">10.8. The break statement</a></span></dt><dt><span class="sect2"><a href="#id2951548">10.9. The continue statement</a></span></dt></dl></dd><dt><span class="sect1"><a href="#id2951573">11. Examples</a></span></dt><dt><span class="sect1"><a href="#id2951585">12. Extensions to consider</a></span></dt></dl></div><div class="sect1" title="1. Introduction"><div class="titlepage"><div><div><h2 class="title"><a name="id2884502"></a>1. Introduction</h2></div></div></div><p>SwiftScript is a language for workflow specification in Data Grid environments, in which:</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>Data lives in files, in a variety of different file system organizations and file formats;</p></li><li class="listitem"><p>We want to be able to define and compose typed procedures that operate on such data; and</p></li><li class="listitem"><p>We want to be able to execute these procedures on distributed resources. </p></li></ul></div><p>SwiftScript addresses the challenges associated with such environments by defining:</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">a language for describing operations on typed <span class="emphasis"><em>data items</em></span>; and </li><li class="listitem">mechanisms for binding data items defined in this language to <span class="emphasis"><em>datasets</em></span> stored on persistent storage.</li></ul></div><p>The binding between data item and dataset is based on the XDTM (XML dataset typing and mapping) model [ref], which separates the declaration of the logical structure of datasets from their physical representation. The logical structure is specified via a subset of XML Schema, where a physical representation is defined by a mapping descriptor<span class="emphasis"><em>,</em></span> which describes how each element in the dataset’s SwiftScript representation can be mapped to a corresponding physical structure such as a directory, file, or database table.</p><p>This manual documents the XDTM-based SwiftScript, which uses a C-like syntax to represent XML Schema types and procedures. This C-like syntax is easier to read and write than XML, but can easily be mapped to XML. </p></div><div class="sect1" title="2. Namespaces"><div class="titlepage"><div><div><h2 class="title"><a name="id2884589"></a>2. Namespaces</h2></div></div></div><p>Since Swift is to be used in Grid environments, the type definitions and procedure definitions can be shared across multiple virtual organizations, groups, and project development stages. Thus namespaces issue is important to address.</p><p>In general, every type definition and procedure definition has an associated namespace. When they are referenced from within another namespace, they must be referenced with their namespace specified explicitly, so as to avoid any confliction with types and procedures defined in the origin namespace.</p><p>If the namespace for a definition is not specified, it uses </p><p>‘http://www.griphyn.org/vds/2006/08/nonamespace’ </p><p>as the default namespace. </p><p>A namespace prefix can be defined to represent an XML-style namespace (in the form of a URI or URN), We follow the XML <span class="emphasis"><em>prefix:localname</em></span> convention and use ‘:’ as the separator between the namespace and the local name of a definition. Examples of namespace declaration can be found in Section 
<a class="link" href="#sect10_1" title="10.1. Namespace Statement">???</a>.</p></div><div class="sect1" title="3. Lexical structure"><div class="titlepage"><div><div><h2 class="title"><a name="id2884642"></a>3. Lexical structure</h2></div></div></div><p>Lexical tokens follow the conventions of the C programming language. Specifically, there are five different tokens: identifiers, keywords, literals, operators, and other separators. White space (spaces, tabs, newlines) and comments are used to separate tokens and are ignored.</p><div class="sect2" title="3.1. Comments"><div class="titlepage"><div><div><h3 class="title"><a name="id2885329"></a>3.1. Comments</h3></div></div></div><p>The characters # or // starts a comment, which terminates when a newline is encountered. The C style /* and */ pair are used for multi-line comments.  For example:</p><pre class="programlisting">// this is a single-line comment</pre><pre class="programlisting"># this is another single-line comment </pre><pre class="programlisting">
/* multi-line comment line 1
   multi-line comment line 2 */
       </pre></div><div class="sect2" title="3.2. Identifiers"><div class="titlepage"><div><div><h3 class="title"><a name="id2885360"></a>3.2. Identifiers</h3></div></div></div><p>An identifier starts with an alphabetic character (‘a’-‘z’, ‘A’-‘Z’, ‘_’), after which there can be arbitrary number of letters or digits. Identifiers are case sensitive, meaning upper case letters are different from lower case ones. An identifier is used to represent the name of a variable, a procedure, a procedure argument, etc, which we’ll talk in detail in later sections.</p><div class="informaltable"><table border="1"><colgroup><col width="48.15pt"><col width="17.4pt"><col width="139.35pt"></colgroup><tbody><tr><td>
              <span class="emphasis"><em>
                <span class="underline">identifier</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>(</em></span>
              <span class="emphasis"><em>letter</em></span>
              <span class="emphasis"><em>|‘_’) (</em></span>
              <span class="emphasis"><em>letter</em></span>
              <span class="emphasis"><em> | </em></span>
              <span class="emphasis"><em>digit</em></span>
              <span class="emphasis"><em> | ‘_’)*</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">letter</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>lowercase</em></span>
              <span class="emphasis"><em> | </em></span>
              <span class="emphasis"><em>uppercase</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">lowercase</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>‘a</em></span>
              <span class="emphasis"><em>’</em></span>
              <span class="emphasis"><em> .. ‘z</em></span>
              <span class="emphasis"><em>’</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">uppercase</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>‘A</em></span>
              <span class="emphasis"><em>’</em></span>
              <span class="emphasis"><em> .. ‘Z</em></span>
              <span class="emphasis"><em>’</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">digit</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>‘0</em></span>
              <span class="emphasis"><em>’</em></span>
              <span class="emphasis"><em> .. ‘9</em></span>
              <span class="emphasis"><em>’</em></span>
            </td></tr></tbody></table></div></div><div class="sect2" title="3.3. Keywords"><div class="titlepage"><div><div><h3 class="title"><a name="id2885623"></a>3.3. Keywords</h3></div></div></div><p>Keywords are identifiers that are reserved for system use, and may not be used otherwise. We have reserved the following identifiers for type declarations and control statements:</p><p>
      <span class="bold"><strong>int</strong></span>
      <span class="bold"><strong>float</strong></span>
      <span class="bold"><strong>string</strong></span>
      <span class="bold"><strong></strong></span>
    </p><p>
      <span class="bold"><strong>date</strong></span>
      <span class="bold"><strong>boolean</strong></span>
      <span class="bold"><strong>uri</strong></span>
    </p><p>
      <span class="bold"><strong>any</strong></span>
    </p><p>
      <span class="bold"><strong>true</strong></span>
      <span class="bold"><strong>false</strong></span>
      <span class="bold"><strong>null</strong></span>
    </p><p>
      <span class="bold"><strong>namespace</strong></span>
      <span class="bold"><strong></strong></span>
      <span class="bold"><strong>include</strong></span>
      <span class="bold"><strong>type</strong></span>
    </p><p>
      <span class="bold"><strong>if</strong></span>
      <span class="bold"><strong>else</strong></span>
    </p><p>
      <span class="bold"><strong>switch</strong></span>
      <span class="bold"><strong>case</strong></span>
      <span class="bold"><strong>default</strong></span>
    </p><p>
      <span class="bold"><strong>while</strong></span>
    </p><p>
      <span class="bold"><strong>foreach</strong></span>
      <span class="bold"><strong>in</strong></span>
      <span class="bold"><strong>step</strong></span>
    </p><p>
      <span class="bold"><strong>repeat</strong></span>
      <span class="bold"><strong>until</strong></span>
    </p></div><div class="sect2" title="3.4. Literals"><div class="titlepage"><div><div><h3 class="title"><a name="id2945919"></a>3.4. Literals</h3></div></div></div><p>Swift <span class="emphasis"><em>literals</em></span> are constant values that are represented as strings in the program. The types and formats of literals are drawn from the set of atomic values defined by XML Schema. The type of a literal value is implicit from its context – from the type of the variable that its being assigned to or the type of the procedure parameter that it is being passed to, or the type of value that is expected in a specific position of a statement such as an <span class="emphasis"><em>if</em></span>, <span class="emphasis"><em>while</em></span>, or <span class="emphasis"><em>switch</em></span>.</p><p>Some literal types can be identified without being enclosed in quotes; string literals and similar types based on strings must be enclosed in quotes.</p><div class="sect3" title="3.4.1. Integer literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2945955"></a>3.4.1. Integer literals</h4></div></div></div><p>An integer literal is a sequence of digits. (We may need to support octal and hexal integer literals too.)</p><div class="informaltable"><table border="1"><colgroup><col width="64.95pt"><col width="17.4pt"><col width="113.55pt"></colgroup><tbody><tr><td>
              <span class="emphasis"><em>
                <span class="underline">integer literal</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>nonzerodigit</em></span>
              <span class="emphasis"><em>  </em></span>
              <span class="emphasis"><em>digit</em></span>
              <span class="emphasis"><em>* | ‘0’</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">nonzerodigit</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>‘1’ .. ‘9’</em></span>
            </td></tr></tbody></table></div></div><div class="sect3" title="3.4.2. Float literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946074"></a>3.4.2. Float literals</h4></div></div></div><p>A float literal has an integer part, a decimal point, a faction part, an <span class="bold"><strong>e</strong></span><span class="bold"><strong>, </strong></span>and an optionally signed integer exponent. The integer part and the faction part both consist of a sequence of digits, where either (but not both) may be missing. The <span class="bold"><strong>e</strong></span> together with the exponent may be missing too.</p><p>Every float literal is considered to be double-precision.</p><div class="informaltable"><table border="1"><colgroup><col width="62.55pt"><col width="17.4pt"><col width="134.55pt"></colgroup><tbody><tr><td>
              <span class="emphasis"><em>
                <span class="underline">float literal</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>pointfloat</em></span>
              <span class="emphasis"><em> | </em></span>
              <span class="emphasis"><em>exponentfloat</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">pointfloat</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>[</em></span>
              <span class="emphasis"><em>intpart</em></span>
              <span class="emphasis"><em>] </em></span>
              <span class="emphasis"><em>fraction</em></span>
              <span class="emphasis"><em> | </em></span>
              <span class="emphasis"><em>intpart</em></span>
              <span class="emphasis"><em> "."</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">exponentfloat</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>(</em></span>
              <span class="emphasis"><em>intpart</em></span>
              <span class="emphasis"><em> | </em></span>
              <span class="emphasis"><em>pointfloat</em></span>
              <span class="emphasis"><em>) </em></span>
              <span class="emphasis"><em>exponent</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">intpart</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>digit</em></span>
              <span class="emphasis"><em>+</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">fraction</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>"." </em></span>
              <span class="emphasis"><em>digit</em></span>
              <span class="emphasis"><em>+</em></span>
            </td></tr><tr><td>
              <span class="emphasis"><em>
                <span class="underline">exponent</span>
              </em></span>
            </td><td>
              <span class="emphasis"><em>::=</em></span>
            </td><td>
              <span class="emphasis"><em>("e" | "E") ["+" | "-"] </em></span>
              <span class="emphasis"><em>digit</em></span>
              <span class="emphasis"><em>+</em></span>
            </td></tr></tbody></table></div><p>Examples of float literals are:</p><pre class="programlisting">3.  .14  3.14  3.14e-6  2e100</pre></div><div class="sect3" title="3.4.3. Boolean literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946386"></a>3.4.3. Boolean literals</h4></div></div></div><p>There are two boolean literals:<span class="bold"><strong> </strong></span><span class="bold"><strong>true</strong></span> and <span class="bold"><strong>false</strong></span>.</p></div><div class="sect3" title="3.4.4. Date literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946413"></a>3.4.4. Date literals</h4></div></div></div><p>A date literal is represented in quoted string conforming to ISO-8601 standard, for example:</p><pre class="programlisting">"2005-09-25T11:30:00Z"</pre></div><div class="sect3" title="3.4.5. String literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946431"></a>3.4.5. String literals</h4></div></div></div><p>A string literal is a sequence of characters surrounded by two double quotes. The special string literal <span class="bold"><strong>null</strong></span> is used to represent an uninitialized string. </p></div><div class="sect3" title="3.4.6. XML literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946449"></a>3.4.6. XML literals</h4></div></div></div><p>XML literals refers to verbatim XML documents. We use @ followed by a string representation of the XML document to denote such literals. For instance:</p><pre class="programlisting">@“<span style="color: red">&lt;volume&gt;<span style="color: red">&lt;image&gt;b1.img&lt;/image&gt;</span><span style="color: red">&lt;header&gt;b1.hdr&lt;/header&gt;</span>&lt;/volume&gt;</span>”</pre></div><div class="sect3" title="3.4.7. URI literals"><div class="titlepage"><div><div><h4 class="title"><a name="id2946475"></a>3.4.7. URI literals</h4></div></div></div><p>An URI literal is a string that conforms to the URI specification – IETF RFC 2396. (<a class="ulink" href="http://www.ietf.org/rfc/rfc2396.txt" target="_top">http://www.ietf.org/rfc/rfc2396.txt</a>).</p><p>Example: </p><pre class="programlisting">"http://www.griphyn.org/"</pre></div></div><div class="sect2" title="3.5. Operators and Separators"><div class="titlepage"><div><div><h3 class="title"><a name="id2946505"></a>3.5. Operators and Separators</h3></div></div></div><p>Operators are used in expressions for operations that involve one or more operands. Separators are for grouping and separation. The operators and separators are as follows:</p><p></p><div class="informaltable"><table border="1"><colgroup><col width="106.2pt"><col width="109.8pt"><col width="171pt"></colgroup><tbody><tr><td>Operators</td><td>() [ ] .</td><td>Procedure call, member reference</td></tr><tr><td> </td><td>=</td><td>Assignment operator</td></tr><tr><td> </td><td>+ - * / %</td><td>Arithmetic operators</td></tr><tr><td> </td><td>&gt; &lt; ==  !=  &gt;=  &lt;=</td><td>Relational operators</td></tr><tr><td> </td><td>&amp;&amp;  ||  !</td><td>Boolean operators</td></tr><tr><td>Separators</td><td>{ }  </td><td>Block separator</td></tr><tr><td> </td><td>&lt; &gt;</td><td>Mapper declaration</td></tr><tr><td> </td><td>,  :  ;</td><td>Others</td></tr></tbody></table></div></div></div><div class="sect1" title="4. Type Definitions"><div class="titlepage"><div><div><h2 class="title"><a name="id2946662"></a>4. Type Definitions</h2></div></div></div><p>All data objects processed by Swift are typed. We distinguish between <span class="emphasis"><em>primitive types</em></span> and <span class="emphasis"><em>composite types.</em></span></p><div class="sect2" title="4.1. Primitive types"><div class="titlepage"><div><div><h3 class="title"><a name="id2946679"></a>4.1. Primitive types</h3></div></div></div><p>A primitive type is one of <span class="bold"><strong>int, float,</strong></span><span class="bold"><strong> boolean, date, string, uri</strong></span>.</p></div><div class="sect2" title="4.2. Composite types"><div class="titlepage"><div><div><h3 class="title"><a name="id2946700"></a>4.2. Composite types</h3></div></div></div><p>A composite type is a type composed of primitive types. We support two kinds of type constructions: Arrays and Structs. We will talk more about these in the declaration section.</p></div><div class="sect2" title="4.3. Arrays"><div class="titlepage"><div><div><h3 class="title"><a name="id2946713"></a>4.3. Arrays</h3></div></div></div><p>An array is a data structure that contains zero or more elements that are all of the same type; this type is called the <span class="emphasis"><em>element type</em></span> of the array.</p><p>Arrays are indexed by integer values, and they are 0-indexed following the C convention.</p><p>Currently only one-dimensional arrays are supported.</p></div><div class="sect2" title="4.4. Structs"><div class="titlepage"><div><div><h3 class="title"><a name="id2946737"></a>4.4. Structs</h3></div></div></div><p>A struct is a data structure that can contain members of different types, where those types can be either primitive or composite types.</p></div></div><div class="sect1" title="5. Datasets"><div class="titlepage"><div><div><h2 class="title"><a name="id2946749"></a>5. Datasets</h2></div></div></div><p>Swift provides a logical programming model for data Grids. A SwiftScript program consists of procedure calls that operate on data items. Swift provides the level of abstraction such that operations can be specified on a data item without regard to its physical location or representation. Within the Swift logical space, a data item is called a <span class="emphasis"><em>data object</em></span>, and its physical counterpart is called a <span class="emphasis"><em>dataset</em></span>.</p><p>A <span class="emphasis"><em>d</em></span><span class="emphasis"><em>ataset</em></span> is a data item that has persistent physical storage. Datasets have both logical representations and physical representations. A dataset’s logical structure is declared using a SwiftScript type definition, where its physical representation describes how the dataset is physically stored and cataloged on persistent storage.</p><p>A SwiftScript program specifies the operations on a dataset’s logical structure. The physical dataset is accessed via a mapper, which translates between the physical, persistent structure of the dataset and its logical representation.</p><p>A physical dataset is referenced via a <span class="emphasis"><em>dataset handle</em></span>, which contains name, type, and mapping information. The name of the dataset handle uniquely identifies the dataset; the type information specifies the logical type the dataset conforms to; and the mapping information comprises the name of a <span class="emphasis"><em>mapping descriptor</em></span> and the necessary parameters to the mapper.  A dataset handle builds the connection between a data object and its corresponding physical dataset.</p><p>The declaration of a dataset handle is defined in Section <a class="link" href="#sect10_4_2" title="10.4.2. Dataset Declaration">???</a></p></div><div class="sect1" title="6. Mapping"><div class="titlepage"><div><div><h2 class="title"><a name="id2946838"></a>6. Mapping</h2></div></div></div><p>The process of mapping, as defined by XDTM, converts between a dataset’s physical representation (typically in persistent storage) and a logical XML view of that data. SwiftScript programs operate on this logical view, and mapping functions implement the actions used to convert back and forth between the logical view and the physical representation.</p><p>Associated with each logical type is a mapping descriptor, which describes the implementation of the mapping functions and necessary mapping parameters to the implementation. </p><p>A mapping descriptor contains the following fields:</p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem">name - name of the descriptor</li><li class="listitem">description- a brief description of the mapper</li><li class="listitem">type- name of the abstract type of the dataset to map</li><li class="listitem">implementation_class- java class that implements the mapping API</li><li class="listitem">parameters- parameters for the implementation class</li></ul></div><p>The implementation of the mapper must conform to the mapper API, which is a standard interface defined between mappers and data sources.  </p></div><div class="sect1" title="7. Variables"><div class="titlepage"><div><div><h2 class="title"><a name="id2946900"></a>7. Variables</h2></div></div></div><p>A variable represents a storage location. Each variable has a name and an associated type that determines what values can be stored in the variable. The value of a variable is the value currently stored in the storage location allocated to the variable. The value of a variable can be initialized or changed through assignment.</p><p>A variable consists of a name and a value. A value is either a literal, or a reference to a data item. </p><div class="sect2" title="7.1. Global variables"><div class="titlepage"><div><div><h3 class="title"><a name="id2946919"></a>7.1. Global variables</h3></div></div></div><p>Global variables are the variables declared in the main body of a SwiftScript program. A global variable extends to any procedures and blocks defined in the program and can be referenced anywhere within the program. The syntax for declaring a global variable is not different from the others, it is just that its scope applies to the whole program.</p></div><div class="sect2" title="7.2. Local variables"><div class="titlepage"><div><div><h3 class="title"><a name="id2946935"></a>7.2. Local variables</h3></div></div></div><p>A local variable occurs in a block. A block is a section of code, which consists of one or more statements that can be grouped together. Examples of a block include an <span class="emphasis"><em>if</em></span> statement, a <span class="emphasis"><em>switch</em></span>, or a <span class="emphasis"><em>while</em></span> statement, etc. Blocks can be nested with one block inside another.</p><p>A local variable can be declared, for instance, within the body of a compound procedure, or in a <span class="emphasis"><em>while</em></span> or <span class="emphasis"><em>switch</em></span> statement. </p><p>A local variable may also be declared within a <span class="emphasis"><em>foreach</em></span> statement as an iteration variable.</p></div><div class="sect2" title="7.3. Dataset-bound variable"><div class="titlepage"><div><div><h3 class="title"><a name="id2946980"></a>7.3. Dataset-bound variable</h3></div></div></div><p>When a variable is associated with a dataset, i.e. it holds the dataset handle of that dataset; it is also called a dataset-bound variable. A dataset-bound variable usually has an associated mapping specification, for details, please look at Section <a class="link" href="#sect10_4_2" title="10.4.2. Dataset Declaration">???</a></p></div><div class="sect2" title="7.4. Scopes"><div class="titlepage"><div><div><h3 class="title"><a name="id2947002"></a>7.4. Scopes</h3></div></div></div><p>A scope defines the visibility of a variable. A global variable extends to any procedures and blocks defined in the program. For a local variable, if it is defined in a block, its scope is limited to that block. If it is defined at the beginning of a procedure, its scope extends to any blocks contained within the procedure, unless a contained block defines a variable with the same name. </p></div><div class="sect2" title="7.5. Variable references"><div class="titlepage"><div><div><h3 class="title"><a name="id2947019"></a>7.5. Variable references</h3></div></div></div><p>A variable reference is an expression that refers to a variable or its sub-elements. A simple example of a variable reference is an identifier. For an <span class="emphasis"><em>array</em></span> variable, subscript can be used to reference an array element. For instance if <span class="emphasis"><em>a</em></span> is an int array, then <span class="emphasis"><em>a[2]</em></span> is a variable reference that refers to element 3 in the array. For a <span class="emphasis"><em>struct</em></span> variable, member names can be used to refer to member variables in the struct. For instance, if <span class="emphasis"><em>addr</em></span> is a struct, with string members: <span class="emphasis"><em>street</em></span>, <span class="emphasis"><em>city</em></span>, and <span class="emphasis"><em>state</em></span>, then <span class="emphasis"><em>addr.city</em></span> refers to its <span class="emphasis"><em>city</em></span> member variable.</p></div></div><div class="sect1" title="8. Procedure Definitions"><div class="titlepage"><div><div><h2 class="title"><a name="id2947074"></a>8. Procedure Definitions</h2></div></div></div><p>Datasets are operated on by <span class="emphasis"><em>procedures</em></span>, which take one or more typed data items as input, perform computations on those data item(s), and produce zero or more data items as output.</p><p>A SwiftScript procedure can be either an <span class="emphasis"><em>atomic procedure</em></span> or a <span class="emphasis"><em>compound procedure</em></span>. An <span class="emphasis"><em>atomic </em></span>procedure definition specifies an interface to an executable program or service. A <span class="emphasis"><em>compound </em></span><span class="emphasis"><em>procedure</em></span> composes calls to atomic procedures, other compound procedures, and/or control statements: it can be viewed as a named workflow template defining a graph of multiple nodes.</p><p>A procedure definition has the form</p><div class="informaltable"><table border="1"><colgroup><col width="105.75pt"><col width="17.4pt"><col width="172.35pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-definition</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>procedure-declarator  procedure-body</em></span>
        </td></tr></tbody></table></div><p>A procedure declarator declares the output formal parameters, the name, and the input parameters of the procedure being defined. This construct is used for all procedures, regardless of the form of their body declarations.</p><div class="informaltable"><table border="1"><colgroup><col width="105.75pt"><col width="18pt"><col width="290.8pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-declarator</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘(’  <span class="emphasis"><em>output-parameter-list  </em></span>‘)’<span class="emphasis"><em>  procedure-name  </em></span>‘(’  <span class="emphasis"><em>input-parameter-list </em></span>’)’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">parameter-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>p</em></span><span class="emphasis"><em>arameter</em></span><span class="emphasis"><em> ( </em></span>‘,’<span class="emphasis"><em> parameter) *</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">parameter</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>type identifier</em></span>
        </td></tr></tbody></table></div><p>Both <span class="emphasis"><em>output-parameter-list</em></span> and <span class="emphasis"><em>input-parameter-list</em></span> can be optional. When there is zero or one output parameter, the parentheses for <span class="emphasis"><em>output-parameter-list</em></span> can be omitted.</p><p>The procedure-body is different for atomic procedure and compound procedure:</p><div class="informaltable"><table border="1"><colgroup><col width="105.75pt"><col width="17.4pt"><col width="231.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-body</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>atomic-</em></span><span class="emphasis"><em>procedure-</em></span><span class="emphasis"><em>body</em></span> | <span class="emphasis"><em>compound-procedure-body</em></span></td></tr></tbody></table></div><div class="sect2" title="8.1. Atomic procedure body"><div class="titlepage"><div><div><h3 class="title"><a name="id2947425"></a>8.1. Atomic procedure body</h3></div></div></div><p>An atomic procedure defines an interface to an external executable program or Web Service, and specifies how data items passed as input and output parameters are mapped to and from application program or service arguments and results. While the header of an atomic procedure specifies the name of the procedure, and the inputs and outputs to the procedure, the body of such an atomic procedure specifies how to set up its execution environment and how to assemble the call to the procedure. Thus, it is in the body of an atomic procedure that <span class="emphasis"><em>mapping operations</em></span> may appear to access components of any physical dataset that is dataset-bound to data items passed as procedure parameters.</p><div class="informaltable"><table border="1"><colgroup><col width="114.75pt"><col width="17.4pt"><col width="181.95pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">atomic-procedure-body</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>procedure-type</em></span> ‘{’<span class="emphasis"><em> invocation-config </em></span>’}’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-type</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>“</em></span><span class="bold"><strong>app</strong></span>”  |  “<span class="bold"><strong>service</strong></span>”</td></tr></tbody></table></div><p>The body can specify the invocation of either an application or a Web Service, where <span class="emphasis"><em>procedure-type</em></span> specifies the type of the procedure.</p><div class="sect3" title="8.1.1. Application procedure body"><div class="titlepage"><div><div><h4 class="title"><a name="id2947566"></a>8.1.1. Application procedure body</h4></div></div></div><p>An application procedure defines the interface to an application program that should be invoked, typically by a POSIX exec() primitive.</p><p>A program procedure body maps the SwiftScript arguments to the information needed to ultimately invoke an application through the POSIX interface, which involves setting arguments and environment variables, and passing back a return code (via an exit value).</p><p>Provisions for handling file descriptors (stdin, stdout, stderr) are provided in the body.</p><p>(TODO: environment variable and other configuration handling, probably using Profile)</p><p>In addition, we define the mapping from logical types to physical representations, via mapping functions.</p><div class="informaltable"><table border="1"><colgroup><col width="114.75pt"><col width="17.4pt"><col width="201.15pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">invocation-config</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>application-name application-argument*  </em></span>‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">application-argument</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>mapping-expression | stdio-argument</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-expression</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>mapping-function-call | </em></span>
          <span class="emphasis"><em>expression</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-function-call</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘@’<span class="emphasis"><em>function-name </em></span>‘(’<span class="emphasis"><em> expression </em></span>‘)’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">stdio-argument</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>“<span class="bold"><strong>stdin</strong></span>”  ‘ =’<span class="emphasis"><em>  mapping-expression </em></span>  |</p>
          <p>“<span class="bold"><strong>stdout</strong></span>” ‘=’<span class="emphasis"><em>  mapping-expression </em></span>  |</p>
          <p>“<span class="bold"><strong>stderr</strong></span>”  ‘=’<span class="emphasis"><em>  mapping-expression </em></span></p>
        </td></tr></tbody></table></div><p>Since <span class="emphasis"><em>@filename</em></span><span class="emphasis"><em>(f)</em></span> is commonly used for getting the name of a file <span class="emphasis"><em>f</em></span>, we introduce a shortcut for this specification, where <span class="emphasis"><em>filename</em></span> along with the parentheses can be omitted. In this case, it can be specified as either <span class="emphasis"><em>@f</em></span> or <span class="emphasis"><em>@(f)</em></span>. </p></div><div class="sect3" title="8.1.2. Service procedure body"><div class="titlepage"><div><div><h4 class="title"><a name="id2947852"></a>8.1.2. Service procedure body</h4></div></div></div><p>A Web Service body specifies the URL of the WSDL description, the port type and operation to invoke, and soap message mappings.</p><div class="informaltable"><table border="1"><colgroup><col width="114.75pt"><col width="17.4pt"><col width="266.55pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">invocation-config</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>wsdlURI  port-type  operation  soap-message-mapping*</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">wsdlURI</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>“<span class="bold"><strong>wsdlURI</strong></span>” ‘<span class="bold"><strong>=</strong></span>’<span class="emphasis"><em> string-literal </em></span>‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">port-type</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>“<span class="bold"><strong>portType</strong></span>” ‘=’<span class="emphasis"><em> string-literal </em></span>‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">operation</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>“<span class="bold"><strong>operation</strong></span>” ‘=’<span class="emphasis"><em> string-literal </em></span>‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">soap-message-mapping</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>( “<span class="bold"><strong>request</strong></span>” | “<span class="bold"><strong>response</strong></span>” )<span class="emphasis"><em> </em></span><span class="emphasis"><em>message-element-name </em></span>‘=’ </p>
          <p>( ‘{’ <span class="emphasis"><em>message-part-mapping*</em></span> ‘}’ ) | <span class="emphasis"><em>mapping-expression</em></span><span class="emphasis"><em> </em></span>‘;’</p>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">message-part-mapping</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>message-part-name</em></span> ‘=’ <span class="emphasis"><em>mapping-expression </em></span> ‘;’</td></tr></tbody></table></div><p>(TODO: WSRF service specification)<span class="emphasis"><em></em></span></p></div></div><div class="sect2" title="8.2. Compound procedure body"><div class="titlepage"><div><div><h3 class="title"><a name="id2948141"></a>8.2. Compound procedure body</h3></div></div></div><p>A compound procedure body is a block of one or more SwiftScript statements, which are executed in an order determined by their data dependencies.  </p><p>The body is comprised of <span class="emphasis"><em>procedure-statement-sequence</em></span>, which is just a sequence of statements:</p><div class="informaltable"><table border="1"><colgroup><col width="141.75pt"><col width="17.4pt"><col width="167.55pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">compound-procedure-body</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘{’<span class="emphasis"><em> </em></span><span class="emphasis"><em>procedure-</em></span><span class="emphasis"><em>statement-sequence </em></span>‘}’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-statement-sequence</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>statement*</em></span>
        </td></tr></tbody></table></div></div></div><div class="sect1" title="9. Expressions"><div class="titlepage"><div><div><h2 class="title"><a name="id2948263"></a>9. Expressions</h2></div></div></div><p>An expression consists of operands and operators that follow a certain sequence.</p><div class="sect2" title="9.1. Primary Expressions"><div class="titlepage"><div><div><h3 class="title"><a name="id2948273"></a>9.1. Primary Expressions</h3></div></div></div><p>There are several kinds of primary expressions:</p><p><span class="bold"><strong>Literals</strong></span></p><p>A literal is a value that has an associated type. We have already discussed literals in Section .</p><p>  <span class="bold"><strong>Variables</strong></span></p><p>A variable also needs to have an associated type. Variables have been described in section .</p><p><span class="bold"><strong>Member a</strong></span><span class="bold"><strong>ccesses</strong></span></p><p>A member access expression is an expression that accesses a member of a struct variable. It is a variable expression followed by a dot, and then followed by the name of a struct member. It has the type of the named member of the struct.</p><div class="informaltable"><table border="1"><colgroup><col width="88.95pt"><col width="17.4pt"><col width="96.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">member-expression</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>primary</em></span><span class="emphasis"><em> </em></span>‘.’<span class="emphasis"><em> </em></span><span class="emphasis"><em><span class="underline">identifier</span></em></span></td></tr></tbody></table></div><p>Example:</p><pre class="programlisting">addr.city</pre><p><span class="bold"><strong>Element a</strong></span><span class="bold"><strong>ccesses</strong></span>
  <span class="bold"><strong> []</strong></span>
  </p><p>An element access expression is an expression that accesses an element of an array. It is a primary expression followed by square brackets, containing a subscript expression. It has the type of the element type. It is also called subscription.</p><div class="informaltable"><table border="1"><colgroup><col width="87.75pt"><col width="17.4pt"><col width="116.55pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">element-expression</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em><span class="underline">primary</span></em></span><span class="emphasis"><em> </em></span>‘[’<span class="emphasis"><em> </em></span><span class="emphasis"><em><span class="underline">expression</span></em></span><span class="emphasis"><em> </em></span>‘]’</td></tr></tbody></table></div><p>Example:</p><pre class="programlisting">itemNumbers[5]</pre><p><span class="bold"><strong>Procedure calls</strong></span>
  <span class="bold"><strong> ()</strong></span>
  </p><p>A procedure call expression is an invocation of a procedure. It is in the form of parenthesized list of comma separated expressions, for actual output parameters; followed by a primary expression, for function name; and then followed by parenthesized list of comma separated expressions, for actual input parameters. Output parameters should have associated types explicitly defined. The acutal paramters can be optional. When there is only one output parameter specified, the parentheses can be optional.</p><div class="informaltable"><table border="1"><colgroup><col width="100.95pt"><col width="17.4pt"><col width="252.15pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">procedure-call</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘(’<span class="emphasis"><em> output-param-list</em></span><span class="emphasis"><em> </em></span>‘)’  <span class="emphasis"><em>primary </em></span>‘(’<span class="emphasis"><em> </em></span><span class="emphasis"><em>input-param-list</em></span><span class="emphasis"><em> </em></span>‘)’<span class="emphasis"><em> </em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">output-param-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>typed-parameter*</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">typed-parameter</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>(type</em></span>
          <span class="emphasis"><em>)?</em></span>
          <span class="emphasis"><em>  identifier</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">input</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">-</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">param</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">-</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>positional-parameters </em></span><span class="emphasis"><em> ( </em></span>‘,’<span class="emphasis"><em> keyword-parameters</em></span><span class="emphasis"><em> )?</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">positional</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">-</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">parameters</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em><span class="underline">expression</span></em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em>(</em></span><span class="emphasis"><em> </em></span>‘,’ <span class="emphasis"><em>expression</em></span><span class="emphasis"><em> )*</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">keyword</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">-</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">parameters</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em><span class="underline">keyword</span></em></span><span class="emphasis"><em><span class="underline">-</span></em></span><span class="emphasis"><em><span class="underline">item</span></em></span><span class="emphasis"><em> ( </em></span>‘,’ <span class="emphasis"><em>keyword-item</em></span><span class="emphasis"><em> )*</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">keyword</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">-</span>
          </em></span>
          <span class="emphasis"><em>
            <span class="underline">item</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em><span class="underline">identifier</span></em></span><span class="emphasis"><em> </em></span>‘=’<span class="emphasis"><em> </em></span><span class="emphasis"><em><span class="underline">expression</span></em></span></td></tr></tbody></table></div><p>Example:</p><pre class="programlisting">File out = myproc1 ( 100, optional_arg = “v1” );</pre><p><span class="bold"><strong>Parenthesized Expressions</strong></span></p><p>A parenthesized expression is a primary expression enclosed in parentheses. The presence of parentheses does not affect its type, or value. Parentheses are used solely for grouping, to achieve a specific order of evaluation.</p><div class="informaltable"><table border="1"><colgroup><col width="118.5pt"><col width="17.65pt"><col width="79.4pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">parenthesized_expression</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘(’<span class="emphasis"><em> expression</em></span>‘)’</td></tr></tbody></table></div><p>Example:</p><pre class="programlisting">int i = (a + b) * 5;</pre></div><div class="sect2" title="9.2. Operators"><div class="titlepage"><div><div><h3 class="title"><a name="id2949056"></a>9.2. Operators</h3></div></div></div><p>Operators in an expression indicate what kind of operations to apply to the operands. Currently we support an assignment operator, arithmetic operators and relational operators. </p><div class="sect3" title="9.2.1. Assignment Operators"><div class="titlepage"><div><div><h4 class="title"><a name="id2949068"></a>9.2.1. Assignment Operators</h4></div></div></div><p>The assignment operator = assigns the value of the right operand to the left operand. The left operand must be a variable reference.</p></div><div class="sect3" title="9.2.2. Arithmetic Operators"><div class="titlepage"><div><div><h4 class="title"><a name="id2949080"></a>9.2.2. Arithmetic Operators</h4></div></div></div><p>Currently we support arithmetic operators + - * / %</p></div><div class="sect3" title="9.2.3. Relational Operators"><div class="titlepage"><div><div><h4 class="title"><a name="id2949091"></a>9.2.3. Relational Operators</h4></div></div></div><p>The relational operators ==, !=, &lt;, &gt;, &lt;= and &gt;= are comparison operators, and the result of the comparisons evaluates to either <span class="bold"><strong>true</strong></span> or <span class="bold"><strong>false</strong></span>. For instance, <span class="emphasis"><em>x==y</em></span> evaluates to true is x is equal to y, and false otherwise.</p></div><div class="sect3" title="9.2.4. Boolean Expressions"><div class="titlepage"><div><div><h4 class="title"><a name="id2949123"></a>9.2.4. Boolean Expressions</h4></div></div></div><p>A boolean expression is an expression that evaluates to either true or false. There are three boolean operators: &amp;&amp;  || ! for AND, OR, and NOT operations respectively.</p><p>The controlling conditional expression of an if-statement, while-statement, or repeat-statement is a boolean expression.</p></div></div></div><div class="sect1" title="10. Statements"><div class="titlepage"><div><div><h2 class="title"><a name="id2949145"></a>10. Statements</h2></div></div></div><div class="sect2" title="10.1. Namespace Statement"><div class="titlepage"><div><div><h3 class="title"><a name="sect10_1"></a>10.1. Namespace Statement</h3></div></div></div><p>The namespace statement MUST appear at the very beginning of a SwiftScript program, and the namespace must be unique. It serves similar purpose as a Java package definition, so that the type definitions and procedure definitions defined in this namespace would not collide with others defined outside. The syntax for namespace definition is as follows:</p><p>“<span class="bold"><strong>namespace</strong></span>”  (<span class="emphasis"><em>prefix</em></span>)?<span class="emphasis"><em></em></span><span class="emphasis"><em> ‘“’  </em></span><span class="emphasis"><em>uri</em></span><span class="emphasis"><em>  ‘”’  </em></span>(<span class="emphasis"><em>‘;’</em></span>)?</p><p><span class="emphasis"><em>prefix</em></span> is the abbreviation of the namespace denoted by <span class="emphasis"><em>uri</em></span>. If prefix is ommitted, then the namespace is regarded as the default namespace. If a default namespace is not defined in the program, it assumes the value</p><p>“http://www.griphyn.org/vds/2006/08/nonamespace”</p><p>Some examples:</p><pre class="programlisting">
  <p>namespace“http://www.griphyn.org/”</p>
  <p>namespacefmri“http://www.fmridc.org/”</p>
  </pre><p>For the definitions that follow the namespace statement, they all belong to the default namespace unless otherwise specified.</p></div><div class="sect2" title="10.2. Include Statements"><div class="titlepage"><div><div><h3 class="title"><a name="id2949342"></a>10.2. Include Statements</h3></div></div></div><p>An include statement is used to include type definitions defined in an external XML Schema document, or to include another program defined in SwiftScript, so that the type definitions and procedure definitions can be used directly within the current SwiftScript program.</p><p>An include statement is of the form:</p><p>“<span class="bold"><strong>include</strong></span>”<span class="bold"><strong> </strong></span>‘<span class="bold"><strong>“</strong></span>’<span class="bold"><strong> </strong></span><span class="emphasis"><em>include-file-name</em></span><span class="emphasis"><em> ‘</em></span><span class="bold"><strong><span class="emphasis"><em>”</em></span></strong></span><span class="emphasis"><em>’</em></span></p><p>Since the definitions in the included file may have a different namespace from the one in the current program, it is necessary to explicitly specify the namespace for those definitions when they are used in the current program.</p></div><div class="sect2" title="10.3. Type Definitions"><div class="titlepage"><div><div><h3 class="title"><a name="id2949407"></a>10.3. Type Definitions</h3></div></div></div><p>A type definition is usually used at the beginning of a program, to define the structure of a new type, which can later be used to declare a variable. Type definitions have the form:</p><div class="informaltable"><table border="1"><colgroup><col width="83.25pt"><col width="17.75pt"><col width="202.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">type-definition</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>“<span class="bold"><strong>type</strong></span>”<span class="emphasis"><em> type-name type-specifier</em></span><span class="emphasis"><em> </em></span>‘;’ </td></tr></tbody></table></div><p>Where the <span class="emphasis"><em>type-name</em></span> is a unique identifier and the <span class="emphasis"><em>type-specifier</em></span> is either an already defined type, such as primitive types, or a struct declaration.</p><div class="sect3" title="10.3.1. Type Specifiers"><div class="titlepage"><div><div><h4 class="title"><a name="id2949507"></a>10.3.1. Type Specifiers</h4></div></div></div><p>The type specifiers are</p><p>“<span class="bold"><strong>int</strong></span>”</p><p><span class="bold"><strong></strong></span>“<span class="bold"><strong>float</strong></span>”</p><p><span class="bold"><strong></strong></span>“<span class="bold"><strong>string</strong></span>”</p><p><span class="bold"><strong></strong></span>“<span class="bold"><strong>boolean</strong></span>”</p><p><span class="bold"><strong></strong></span>“<span class="bold"><strong>date</strong></span>”</p><p><span class="bold"><strong></strong></span>“<span class="bold"><strong>uri</strong></span>”</p><p>
  <span class="emphasis"><em></em></span>
  <span class="emphasis"><em>struct-declaration</em></span>
  </p></div><div class="sect3" title="10.3.2. Struct Declarations"><div class="titlepage"><div><div><h4 class="title"><a name="id2949592"></a>10.3.2. Struct Declarations</h4></div></div></div><p>A struct declaration is of the form:</p><div class="informaltable"><table border="1"><colgroup><col width="93.9pt"><col width="19.7pt"><col width="148.85pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">struct-declaration</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘{’  <span class="emphasis"><em>type-decl</em></span><span class="emphasis"><em>aration-list</em></span><span class="emphasis"><em> </em></span> ‘}’</td></tr></tbody></table></div><p> The <span class="emphasis"><em>type-declaration-list</em></span> is a sequence of type declarations for the members of the struct.</p><div class="informaltable"><table border="1"><colgroup><col width="96.75pt"><col width="19.5pt"><col width="91.6pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">type-declaration-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>type-declaration*</em></span>
        </td></tr></tbody></table></div><p>A type declaration is of the form:</p><div class="informaltable"><table border="1"><colgroup><col width="96.75pt"><col width="19.7pt"><col width="163.15pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">type-declaration</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>type-specifier declarator-list </em></span>‘;’</td></tr></tbody></table></div><p>The <span class="emphasis"><em>declarator-list</em></span> is a comma-separated sequence of declarators. Each declarator can be an identifier, or an array declarator, which is an identifer followed by [ ], with an optional array size designated by an integer literal.  </p><div class="informaltable"><table border="1"><colgroup><col width="96.75pt"><col width="21.4pt"><col width="191.85pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">declarator-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>
            <span class="emphasis"><em>identifier</em></span>
            <span class="emphasis"><em> </em></span>
            <span class="emphasis"><em> |</em></span>
          </p>
          <p><span class="emphasis"><em>identifier  </em></span>‘[’ <span class="emphasis"><em> integer_literal?  </em></span>‘]’</p>
        </td></tr></tbody></table></div><p>For example, an order with an order number, a description, and a sequence of item numbers can be specified as follows:</p><pre class="programlisting">
   <span class="bold"><strong>type</strong></span> order {
       int orderNumber;
       string description;
       int itemNumbers[];
   }
  </pre></div></div><div class="sect2" title="10.4. Declaration Statements"><div class="titlepage"><div><div><h3 class="title"><a name="id2949934"></a>10.4. Declaration Statements</h3></div></div></div><p>A declaration statement declares a variable, or a physical dataset in the form of a <span class="emphasis"><em>dataset handle</em></span>.</p><div class="sect3" title="10.4.1. Local Variable Declaration"><div class="titlepage"><div><div><h4 class="title"><a name="id2949948"></a>10.4.1. Local Variable Declaration </h4></div></div></div><p>A local variable declaration declares one or more local variables. </p><div class="informaltable"><table border="1"><colgroup><col width="131.05pt"><col width="17.4pt"><col width="264.35pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">local-variable-declaration</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>type   local-variable-declarator-list</em></span><span class="emphasis"><em> </em></span>‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">local-variable-declarator-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>local-variable-declarator </em></span><span class="emphasis"><em>(</em></span><span class="emphasis"><em> </em></span>‘,’   <span class="emphasis"><em>local-variable-declarator</em></span><span class="emphasis"><em> )*</em></span> </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">local-variable-declarator</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>identifier  </em></span><span class="emphasis"><em>(</em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em> </em></span>‘=’<span class="emphasis"><em>   local-variable-initializer</em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em> )</em></span><span class="emphasis"><em>?</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">local-variable-initializer</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>expression</em></span>
          <span class="emphasis"><em> | </em></span>
          <span class="emphasis"><em>array-initializer</em></span>
          <span class="emphasis"><em> | range-initializer</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">array-initializer</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘[’<span class="emphasis"><em> expression </em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em>(</em></span><span class="emphasis"><em> </em></span>‘,’<span class="emphasis"><em> expression</em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em>)* </em></span>‘]’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">range-initializer</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘[’ <span class="emphasis"><em>expression </em></span>‘:’<span class="emphasis"><em> expression ( </em></span>‘:’<span class="emphasis"><em> expression ) ?</em></span> ‘]’</td></tr></tbody></table></div><p>Some examples:</p><pre class="programlisting">
  int x, y=2;
  String s = “hello”;
  floatf[] = [1.0, 2.0, 3.0];
  intp[] = [1 : 9 : 2];  // numbers 1 3 5 7 9
  </pre><p>Note a range initializes an array with a series of values with a fixed step, with a default step 1.</p></div><div class="sect3" title="10.4.2. Dataset Declaration"><div class="titlepage"><div><div><h4 class="title"><a name="sect10_4_2"></a>10.4.2. Dataset Declaration </h4></div></div></div><p>A physical dataset is referenced by a <span class="emphasis"><em>dataset handle</em></span>, which contains name, type and mapping information of the dataset.</p><div class="informaltable"><table border="1"><colgroup><col width="108.15pt"><col width="17.4pt"><col width="235.95pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">dataset-declaration</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>type  dataset-name </em></span>‘&lt;’<span class="emphasis"><em> mapping-description </em></span>‘&gt;’ ‘;’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">dataset-name</span>
          </em></span>
          <span class="underline"> </span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>identifier</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-descrition</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>mapping-descriptor</em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em>( </em></span><span class="emphasis"><em> </em></span>‘;’<span class="emphasis"><em> </em></span><span class="emphasis"><em> </em></span><span class="emphasis"><em>mapping-parameter-list</em></span><span class="emphasis"><em> )?</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-descriptor</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>identifier</em></span>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-parameter-list</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>mapping-parameter</em></span>  <span class="emphasis"><em>(</em></span><span class="emphasis"><em> </em></span>‘,’ <span class="emphasis"><em>mapping-parameter</em></span><span class="emphasis"><em> )</em></span><span class="emphasis"><em>*</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">mapping-parameter</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>identifier </em></span>‘=’<span class="emphasis"><em> </em></span><span class="emphasis"><em> mapping-</em></span><span class="emphasis"><em>expression</em></span></td></tr></tbody></table></div><p>A sample dataset declaration is shown as follows:</p><pre class="programlisting">Imageimg1&lt;image_mapper; location=“/home/archive/images/image1.jpg”&gt;;</pre><pre class="programlisting">Imageimg2&lt;simple_mapper; prefix=@img1, suffix=”.2”&gt;; </pre><p>As a dataset handle is no more than a variable holding a dataset, we can also call it a <span class="emphasis"><em>dataset-bound variable</em></span>.</p></div></div><div class="sect2" title="10.5. Expression Statements"><div class="titlepage"><div><div><h3 class="title"><a name="id2950555"></a>10.5. Expression Statements</h3></div></div></div><p>Most statements are expression statements, they take the form:</p><p><span class="emphasis"><em>expression </em></span>‘;’</p><p>Usually expression statements are assignments, or procedure calls.</p></div><div class="sect2" title="10.6. Selection Statements"><div class="titlepage"><div><div><h3 class="title"><a name="id2950577"></a>10.6. Selection Statements</h3></div></div></div><p>A selection statement selects one of a number of possible statements for execution, based on the value of a boolean expression.</p><div class="informaltable"><table border="1"><colgroup><col width="107.7pt"><col width="21.1pt"><col width="169.05pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">selection-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>if-statement</em></span>
          <span class="emphasis"><em> | </em></span>
          <span class="emphasis"><em>switch-statement</em></span>
        </td></tr></tbody></table></div><div class="sect3" title="10.6.1. The if statement"><div class="titlepage"><div><div><h4 class="title"><a name="id2950661"></a>10.6.1. The if statement</h4></div></div></div><p>The if statement selects a statement for execution based on the value of a boolean expression. </p><div class="informaltable"><table border="1"><colgroup><col width="78.75pt"><col width="18.65pt"><col width="259.25pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">if-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>“<span class="bold"><strong>if</strong></span>”   ‘(’  <span class="emphasis"><em>boolean-expression   </em></span>‘)’ ‘{’ <span class="emphasis"><em>statement*</em></span> ‘}’<span class="emphasis"><em> </em></span></p>
          <p><span class="emphasis"><em>( </em></span><span class="emphasis"><em> </em></span>“<span class="bold"><strong>else</strong></span>”  ‘{’ <span class="emphasis"><em>statement*</em></span> ‘}’ <span class="emphasis"><em>)?</em></span></p>
        </td></tr></tbody></table></div></div><div class="sect3" title="10.6.2. The switch statement"><div class="titlepage"><div><div><h4 class="title"><a name="id2950775"></a>10.6.2. The switch statement</h4></div></div></div><p>The switch statement selects one of many statement lists for execution based on the value of  the switch expression.</p><div class="informaltable"><table border="1"><colgroup><col width="77.7pt"><col width="17.4pt"><col width="234.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">switch-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>“<span class="bold"><strong>switch</strong></span>”   ‘(’   <span class="emphasis"><em>expression   </em></span>’)’   <span class="emphasis"><em>switch-block</em></span></td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">switch-block</span>
          </em></span>
          <span class="underline"> </span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>‘{’   <span class="emphasis"><em>switch-section*</em></span><span class="emphasis"><em>   </em></span>‘}’</td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">switch-section</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>switch-label   statement</em></span><span class="emphasis"><em>*</em></span> </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">switch-label</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em> </em></span>“<span class="bold"><strong>case</strong></span>”   <span class="emphasis"><em>constant-expression   </em></span>‘:’ |  <span class="emphasis"><em>(</em></span> “<span class="bold"><strong>default</strong></span>”<span class="bold"><strong> </strong></span>  ‘:’ <span class="emphasis"><em>)</em></span></td></tr></tbody></table></div></div></div><div class="sect2" title="10.7. Loop statements"><div class="titlepage"><div><div><h3 class="title"><a name="id2950988"></a>10.7. Loop statements</h3></div></div></div><p>A loop statement repeatedly executes some statements in the loop body. It can be of one of the following statements:</p><div class="informaltable"><table border="1"><colgroup><col width="78.75pt"><col width="17.75pt"><col width="256.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">loop-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td><span class="emphasis"><em>foreach-statement</em></span> | <span class="emphasis"><em>while-statement</em></span><span class="emphasis"><em> | </em></span><span class="emphasis"><em>repeat-statement</em></span></td></tr></tbody></table></div><div class="sect3" title="10.7.1. The foreach statement"><div class="titlepage"><div><div><h4 class="title"><a name="id2951072"></a>10.7.1. The foreach statement</h4></div></div></div><p>The foreach statement iterates over the elements of a collection, and executes the embedded statement for each of the elements.</p><div class="informaltable"><table border="1"><colgroup><col width="87.75pt"><col width="17.75pt"><col width="256.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">foreach-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>“<span class="bold"><strong>foreach</strong></span>”  <span class="emphasis"><em>type</em></span><span class="emphasis"><em>?</em></span><span class="emphasis"><em>  identifier</em></span><span class="emphasis"><em> ( ‘,’ index-identifier )?  </em></span><span class="emphasis"><em> </em></span></p>
          <p><span class="emphasis"><em> </em></span>“<span class="bold"><strong>in</strong></span>”  <span class="emphasis"><em>expression  </em></span>“<span class="bold"><strong>step</strong></span>” <span class="emphasis"><em>int-</em></span><span class="emphasis"><em>literal</em></span><span class="emphasis"><em> </em></span> ‘{’ <span class="emphasis"><em>statement*</em></span> ‘}’</p>
        </td></tr></tbody></table></div><p>The <span class="emphasis"><em>type</em></span> and <span class="emphasis"><em>identifier</em></span> of a foreach statement declare the iteration variable of the statement. if the <span class="emphasis"><em>identifier</em></span> is defined before the <span class="emphasis"><em>foreach</em></span> statement, then <span class="emphasis"><em>type</em></span> is optional. The type of the <span class="emphasis"><em>expression</em></span> in the foreach statement must be a collection type. The <span class="emphasis"><em>step</em></span> controls how far off the iteration jumps forward to another element, and the <span class="emphasis"><em>index</em></span> variable is an integer variable to track the current position of the iteration.</p></div><div class="sect3" title="10.7.2. The while statement"><div class="titlepage"><div><div><h4 class="title"><a name="id2951242"></a>10.7.2. The while statement</h4></div></div></div><p>The while statement executes an embedded statement zero or more times conditionally based on a boolean expression. </p><div class="informaltable"><table border="1"><colgroup><col width="101.25pt"><col width="17.75pt"><col width="256.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">while-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>“<span class="bold"><strong>while</strong></span>”<span class="bold"><strong> </strong></span>  ‘(’   <span class="emphasis"><em>boolean-expression   </em></span>‘)’  </p>
          <p>‘{’ <span class="emphasis"><em>embedded-statement</em></span> ‘}’</p>
        </td></tr><tr><td>
          <span class="emphasis"><em>
            <span class="underline">embedded-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <span class="emphasis"><em>statement*</em></span>
        </td></tr></tbody></table></div><p>A while statement is executed as follows: </p><div class="itemizedlist"><ul class="itemizedlist" type="disc"><li class="listitem"><p>First the <span class="emphasis"><em>boolean-expression</em></span> is evaluated. </p></li><li class="listitem"><p>If it is evaluated to true, control is transferred to the embedded statement. When control reaches the end point of the embedded statement, control goes back to the beginning of the while statement. </p></li><li class="listitem"><p>If the boolean expression yields false, control is transferred to the end point of the while statement. </p></li></ul></div></div><div class="sect3" title="10.7.3. The repeat statement"><div class="titlepage"><div><div><h4 class="title"><a name="id2951409"></a>10.7.3. The repeat statement</h4></div></div></div><p>The repeat statement executes an embedded statement zero or more times conditionally based on a boolean expression. </p><div class="informaltable"><table border="1"><colgroup><col width="101.25pt"><col width="17.75pt"><col width="256.75pt"></colgroup><tbody><tr><td>
          <span class="emphasis"><em>
            <span class="underline">repeat-statement</span>
          </em></span>
        </td><td>
          <span class="emphasis"><em>::=</em></span>
        </td><td>
          <p>“<span class="bold"><strong>repeat</strong></span>”‘{’ <span class="emphasis"><em>embedded-statement</em></span> ‘}’</p>
          <p><span class="emphasis"><em> </em></span>“<span class="bold"><strong>until</strong></span>”   ‘(’   <span class="emphasis"><em>boolean-expression   </em></span>‘)’   ‘;’</p>
        </td></tr></tbody></table></div><p>The repeat statement is slightly different from the while statement in that control goes to the embedded statement first, and the boolean expression is evaluated, if true, then control goes to the end point of the repeat statement, otherwise, control goes back to the embedded statement.</p></div></div><div class="sect2" title="10.8. The break statement"><div class="titlepage"><div><div><h3 class="title"><a name="id2951523"></a>10.8. The break statement</h3></div></div></div><p>The statement</p><p>“<span class="bold"><strong>break</strong></span>” ‘;’</p><p>causes termination of the smallest enclosing loop, or switch statement; control passes to the statement following the terminated statement.</p></div><div class="sect2" title="10.9. The continue statement"><div class="titlepage"><div><div><h3 class="title"><a name="id2951548"></a>10.9. The continue statement</h3></div></div></div><p>The statement</p><p>“<span class="bold"><strong>continue</strong></span>” ‘;’</p><p>causes control to pass to the loop continuation portion of the smallest enclosing loop statement; that is to the end of the loop. </p></div></div><div class="sect1" title="11. Examples"><div class="titlepage"><div><div><h2 class="title"><a name="id2951573"></a>11. Examples</h2></div></div></div><p>For detailed examples, please refer to the User Guide document in the Swift public release.</p></div><div class="sect1" title="12. Extensions to consider"><div class="titlepage"><div><div><h2 class="title"><a name="id2951585"></a>12. Extensions to consider</h2></div></div></div><p>- More atomic types, such as those defined in XML Schema</p><p>- Type inference: if the type of a formal parameter to a procedure can be inferred from its definition, then the type does not need to appear in the procedure signature. </p><p>For example, if you write</p><p></p><p>(c) myfunction (a,b)</p><p>{</p><p>     tmp=combineImages(a,b)</p><p>     c=invertImage(tmp)</p><p>}</p><p>as long as you have function prototypes for combineImages and invertImage,</p><p>you can infer from the program the types for the variables a,b and c and</p><p>hence the prototype for myfunction... and so on for the entire program.</p><p>- Literal XML snippets instead of quoted XML to avoid quoting problem.</p><p>- Blocks within a procedure, with new scopes for declaring variables</p><p>- Ability to invoke an XPath to extract a value from a document</p></div></div>
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
