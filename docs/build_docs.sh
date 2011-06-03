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
pushd $(dirname $(readlink -f $0)) > /dev/null 2>&1
pushd .. > /dev/null 2>&1
VERSION=`svn info |grep URL|awk -F / '{print $NF}'`
popd > /dev/null 2>&1
echo Installing docs into $INSTALLATION_DIRECTORY

# Convert files
DIRECTORIES=`ls -d */ 2>/dev/null`
for directory in $DIRECTORIES
do
   pushd $directory > /dev/null 2>&1
   FILES=`ls -1 *.txt 2>/dev/null`
   for file in $FILES
   do
      echo Converting $directory"$file" to HTML
      asciidoc -a toc -a max-width=750px $file
      echo Converting $directory"$file" to PDF
      a2x --format=pdf --no-xmllint $file 
   done

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION"
   fi

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION/$directory" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION/$directory"
   fi

   # Copy all files to destination (may include graphics, etc)
   for copyfile in `ls * 2>/dev/null`
   do
      cp $copyfile $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to copy $copyfile to $INSTALLATION_DIRECTORY/$VERSION/$directory"
   done

   popd > /dev/null 2>&1
done
popd > /dev/null 2>&1
