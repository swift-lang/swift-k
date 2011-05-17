#!/bin/bash

# crash: Report a problem and exit
crash()
{
    MSG=$1
    echo ${MSG}  >&2
    exit 1
}

# Verify correct arguments
if [ -n "$1" ]; then
   INSTALLATION_DIRECTORY=$1
else
   crash "Error: Must specify a directory for installation"
fi

# Create installation directory if needed
if [ ! -d "$INSTALLATION_DIRECTORY" ]; then
   mkdir $INSTALLATION_DIRECTORY || crash "Unable to create directory $INSTALLATION_DIRECTORY"
fi

# Gather version information
pushd .. > /dev/null 2>&1
VERSION=`svn info |grep URL|awk -F / '{print $NF}'`
popd > /dev/null 2>&1
echo Installing docs into $INSTALLATION_DIRECTORY

# Convert files
DIRECTORIES=*/
for directory in $DIRECTORIES
do
   pushd $directory > /dev/null 2>&1
   FILES=*.txt
   for file in $FILES
   do
      echo Converting $directory"$file" to HTML
      asciidoc -a toc $file
      echo Converting $directory"$file" to PDF
      a2x --format=pdf --no-xmllint $file 
   done

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION"
   fi

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION/$directory" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION/$directory"
   fi
   cp *.html $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to copy html files to $INSTALLATION_DIRECTORY/$VERSION/$directory"
   cp *.pdf $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to copy pdf files to $INSTALLATION_DIRECTORY/$VERSION/$directory"
   popd > /dev/null 2>&1
done
