#!/bin/bash

if [ -n "$1" ]; then
   INSTALLATION_DIRECTORY=$1
else
   echo Error: Must specify a directory for installation
   exit 1
fi

if [ ! -d "$INSTALLATION_DIRECTORY" ]; then
   mkdir $INSTALLATION_DIRECTORY || exit 1
fi

echo Installing docs into $INSTALLATION_DIRECTORY

cd docs || exit 1
DIRECTORIES=*/
for directory in $DIRECTORIES
do
   cd $directory || exit 1
   FILES=*.txt
   for file in $FILES
   do
      echo Converting $directory"$file" to HTML
      asciidoc -a toc $file
      echo Converting $directory"$file" to PDF
      a2x --format=pdf --no-xmllint $file 
   done
   cp *.html $INSTALLATION_DIRECTORY || exit 1
   cp *.pdf $INSTALLATION_DIRECTORY || exit 1
   cd .. || exit 1
done
cd ..
