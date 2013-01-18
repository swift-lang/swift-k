#!/bin/bash

DEST=$1
URL=$2
DIR=src/cog-compile/modules/abstraction/dist/`ls src/cog-compile/modules/abstraction/dist/`
CRTDIR=`pwd`

rm -f $DEST/*.*
cp -R $DIR/lib* $DEST

cd $DEST
mkdir maven-files

mv lib lib-common
for libdir in lib*; do
	mkdir $libdir/jars
	mv $libdir/*.jar $libdir/jars
	rm -f $libdir/*
done

rename "lib-" "cog-" *

EURL=`echo $URL|sed -e 's/\//\\\\\//g'`
for libdir in *; do
	if [ "$libdir" != "maven-files" ]; then
		$CRTDIR/mavenize.pl $libdir
		mkdir maven-files/$libdir
		mv $libdir/project.xml maven-files/$libdir/
	fi
done

rename "cog-" "" maven-files/*

cd maven-files
for libdir in *; do
	sed -e "s/@REPOSITORY@/$EURL/" $CRTDIR/projectm.properties >$libdir/project.properties
	if [ "$libdir" == "common" ]; then
		sed -e "s/@ID@/common/" -e "s/@DEST@/lib/" $CRTDIR/mavenm.xml >$libdir/maven.xml
	else
		sed -e "s/@ID@/$libdir/" -e "s/@DEST@/lib-$libdir/" $CRTDIR/mavenm.xml >$libdir/maven.xml
	fi
done
cd ..

cp $CRTDIR/project.xml maven-files
cp $CRTDIR/maven.xml maven-files
sed -e "s/@REPOSITORY@/$EURL/" $CRTDIR/project.properties >maven-files/project.properties

tar -czf maven-files.tar.gz --exclude=*.jar --exclude=*.gz maven-files
