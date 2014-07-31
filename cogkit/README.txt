CONTENTS  
=========

A. Creating new modules
B. Log4j issues
C. Java Webstart
D. Using PMD
 

A. Extending the CoG Kit through Contributed Modules
====================================================

A Sample module build file can be found in modules/template

There are a few requirements that have to be imposed in order
to keep consistency:

	The basic directory structure that *must* exists for each 
	module is:
		etc/MANIFEST.MF.head
		etc/MANIFEST.MF.tail
		lib/
		src/

Build files 

        There are 4 files that are required by the build system:

	1. build.xml - should not be modified at all unless absolutely
		necessary. If there is a feature that you would like
		added to the build system, please tell Mike
		(hategan@mcs.anl.gov)

	2. dependencies.xml - project dependencies are stored
		here. Please modify it to suit your needs. An example
		is given in the modules/template directory

	3. launchers.xml - launchers that you want created in the
		build process. Use the example in modules/template to
		see how to use it

	4. project.properties - The module properties. The module name
		*must* be the same as the directory name of the
		module.  The last line in this file contains the
		library dependencies for this module. If you don't add
		the jar files that your project requires there, it
		will not build. The format is a comma separated list
		of files. I suggest using <jar-name>.* (so that
		licenses and other things belonging to a jar will also
		be copied). Please read below about the libraries.
		
Build targets

		There are a few commonly used build targets:
		
		1. dist - builds a module, its dependencies and
		creates a distribution directory that contains
		everything required to use the module.
		
		2. dist.joint - does pretty much what dist does, but
		all compiled classes from all modules are put into one
		big jar file.
		
		3. jar - creates a jar file for the current module in
		the dist/lib directory. It requires the dist/lib
		directory to be present, all dependencies and
		libraries to be compiled and the jars copied to the
		dist/lib directory. You should normally not need to
		use this target. Use "dist" instead.
		
		4. clean - removes the compiled classes (the "build"
                   directory)
		
		5. distclean - removed both the compiled classes and
		the dist directory. It does recursively clean the
		dependencies.
		
		6. deploy.webstart - creates a webstart package for
		the module.  You may need to edit
		cog/webstart.properties.

Libraries

	Libraries can be found in two places:

		1. cog/lib
		2. cog/modules/yourmodule/lib

	The build system will automatically choose the library from
	either of the two directories. If a library exists in both
	directories, priority will be given to the library in the
	cog/lib directory. This may cause your module not to build.
	Please talk to Gregor or Mike in this case.  Also please note
	that the libraries in your module may at any time move to the
	cog/lib directory.
	
Sandboxed libraries

	In order to facilitate the peaceful coexistence of different
	GT client libraries within the same JVM, a classloader sandboxing 
	environment was created. It requires certain jar files to exist
	in directories that are separate from the main jar directory.
	These jars must NOT be added to the classpath if sandboxing is
	enabled.
	
	To disable the sanboxing system (this can only be done at build time)
	uncomment the 'merge.extra.libs=true' line in build.properties
	

Source

	The sources for your module. Not much to say here :)


B. Log4j issues 
=============== 

Due to potential conflicts with log4j initialization files, if you use
log4j in your module please note the following:

1. There is a global log4j initialization file in
cog/etc/log4j.properties.root. It defines the root category and a few
appenders. It will be automatically included in builds.

2. Each module can additionally define specific log4j logging clauses.
This can be done in modules/<module-name>/etc/log4j.properties.module.
Please do not re-define the root category there.


C. Java Webstart
================

Creating webstart deployments is easy. You would need to define the
webstart launchers in the launchers.xml file of your module. Please
take a look at the launchers.xml in the template module.

In order to pass arguments to the application or applet, you can use
the "application-params" (for applications) or "applet-params" (for
applets) parameters. You would need to literally provide all the
arguments as XML tags, while also escaping XML characters.

Examples:	 	 	

	1. You need to pass "one" and "two" as arguments to a webstart
	application. The correct format is:

		<property name="application-params" value="&lt;argument&gt;one&lt;/argument&gt; &lt;argument&gt;two&lt;/argument&gt;"/>
	
	2. You need to pass "key1=value1" to an applet:
		<property name="applet-params" value="&lt;param name=&quot;key1&quot; value=&quot;value1&quot;/&gt;"/>

In order to deploy the webstart applications, you need to first modify
the cog/webstart.properties file to match your particular
configuration.  After that, you can issue the "ant deploy.webstart"
command, either in the main cog directory or inside a module. An HTML
index page will be automatically that will point to the generated jnlp
files.

You can also put some metadata in your module that can be used to
generate nice pages with the webstart applications. The structure is
as follows:

<module>/meta/
<module>/meta/description.txt
<module>/meta/icon.png
<module>/meta/screenshot.(png|jpg)
<module>/meta/<launcher-name>/
<module>/meta/<launcher-name>/description.txt
<module>/meta/<launcher-name>/icon.png
<module>/meta/<launcher-name>/screenshot.(png|jpg)

D. Using PMD
============

We recommend that that developers and contributors use PMD
(http://pmd.sourceforge.net) to check their code.  Many of the
complaints that PMD generates should be taken seriously. Still, there
are instances when PMD rules do not apply for a good reason and create
false positives.

To use pmd, you need to download it and add all its jar files to the
../tools/pmd-2.3/lib directory.  Afterwards, just run 'ant pmd' in the
module you want to check. It will generate both an on-screen report
and an HTML report (pmd-report.html). For our core developers we have
a makefile in the ./cog directory that will install pmd appropriately by
calling "make install-qc".



