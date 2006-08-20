#!/bin/bash

# Use this script to create one Eclipse project file for the whole CoG Kit
# Must be run from the cog directory
# This is unsupported

	cat << EOF >.project
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>Java CoG Kit</name>
	<comment></comment>
	<projects>
EOF
	cat << EOF >.classpath
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="output" path=".build"/>
	<classpathentry kind="lib" path="etc"/>
EOF
exclude="template qos portlets CVS"

for jar in `ls lib/*.jar 2>/dev/null`; do
	echo "	<classpathentry exported=\"true\" kind=\"lib\" path=\"$jar\"/>">>.classpath
done

for module in modules/*; do
	name=`basename $module`
	if [ -n "`echo $exclude | grep $name`" ]; then
		echo "Skipping $name"
		continue
	fi
	echo "Processing $name"
	
	echo "	<classpathentry excluding=\"**/CVS/*\" kind=\"src\" path=\"$module/src\"/>">>.classpath
	if [ -d "$module/resources" ]; then
		echo "	<classpathentry excluding=\"**/CVS/*\" kind=\"src\" path=\"$module/resources\"/>">>.classpath
	fi
	
	for jar in `ls $module/lib/*.jar 2>/dev/null`; do
		echo "	<classpathentry exported=\"true\" kind=\"lib\" path=\"$jar\"/>">>.classpath
	done
done
echo "</classpath>">>.classpath

cat << EOF >>.project
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
