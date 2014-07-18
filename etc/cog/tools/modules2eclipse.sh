#!/bin/bash

# Use this script to create Eclipse project files for the CoG Kit
# Each module will become an Eclipse project which must then be imported
# 	in Eclipse
# Must be run from the cog directory
# This is unsupported

exclude="template qos portlets CVS"
for module in modules/*; do
	name=`basename $module`
	if [ -n "`echo $exclude | grep $name`" ]; then
		echo "Skipping $name"
		continue
	fi
	echo "Processing $name"
	cat << EOF >$module/.project
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>$name</name>
	<comment></comment>
	<projects>
EOF
	cat << EOF >$module/.classpath
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="output" path=".build"/>
	<classpathentry kind="lib" path="etc"/>
EOF
	if [ -d "$module/resources" ]; then
		echo "	<classpathentry excluding=\"**/CVS/*\" kind=\"src\" path=\"resources\"/>">>$module/.classpath
	fi
	libline=`grep "lib.deps" $module/project.properties`
	libs=`echo $libline | awk -F: 'BEGIN {FS="="} {print $2}'`
	IFS=', '
	if [ "$libs" != "-" ]; then
		echo $libs
		for lib in $libs; do
			main="`ls $PWD/lib/$lib 2>/dev/null`"
			mod="`ls $module/lib/$lib 2>/dev/null`"
			if [ -n "$main" ]; then
				jarc=$main
			else
				if [ -n "$mod" ]; then
					jarc="lib/`basename $mod`"
				else
					continue
				fi
			fi
			IFS=$'\n'
			for jar in $jarc; do
				if [ -n "`echo $jar | grep jar`" ]; then
					finaljar=$jar
				fi
			done
			IFS=', '
			echo "Adding jar $finaljar"
			echo "	<classpathentry exported=\"true\" kind=\"lib\" path=\"$finaljar\"/>">>$module/.classpath
		done
	fi
	IFS=$'\n'
	for line in `cat $module/dependencies.xml`; do
		if [ -n "`echo $line | grep property`" ]; then
			dep=`echo $line | awk -F: 'BEGIN {FS="\""} {print $4}'`
			echo "Adding dependency: $dep"
			echo "		<project>$dep</project>">>$module/.project
			echo "	<classpathentry exported=\"true\" excluding=\"**/CVS/*\" kind=\"src\" path=\"/$dep\"/>">>$module/.classpath
		fi
	done
	cat << EOF >>$module/.project
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
	</natures>
</projectDescription>
EOF
	echo "</classpath>">>$module/.classpath
done
