#!/bin/bash

DIRECTORIES="cookbook designs documentation merged releasenotes siteguide staging swift-tutorial tutorial userguide2 utils"
#DIRECTORIES="userguide2"

# Usage: See usage() for usage

# crash: Report a problem and exit
crash()
{
    MSG=$1
    echo ${MSG}  >&2
    exit 1
}

# Output directory mode
CHMOD_DIRECTORY_MODE="775"
# Output file mode
CHMOD_FILE_MODE="664"
# Output group
GROUP="vdl2-svn"
# Make PDFs iff MAKE_PDF=1
MAKE_PDF=0

# See getopts loop below for options
usage()
{
  echo "Usage: ./build_docs.sh <opts> <installation directory>"
}

while getopts "dh" OPTION
do
  case ${OPTION} in
    d)
      MAKE_PDF=0
      shift
      ;;
    h)
      usage
      exit 0
      ;;
  esac
done

INSTALLATION_DIRECTORY=$1

if [[ $INSTALLATION_DIRECTORY == "" ]]
then
  echo "Not given: installation directory"
  usage
  exit 1
fi

if [[ "${INSTALLATION_DIRECTORY:0:1}" != "/" ]]; then
   INSTALLATION_DIRECTORY=`pwd`/$INSTALLATION_DIRECTORY
fi

# Create installation directory if needed
if [ ! -d "$INSTALLATION_DIRECTORY" ]; then
   mkdir $INSTALLATION_DIRECTORY || crash "Unable to create directory $INSTALLATION_DIRECTORY"
   chgrp $GROUP $INSTALLATION_DIRECTORY > /dev/null 2>&1
   chmod $CHMOD_DIRECTORY_MODE $INSTALLATION_DIRECTORY > /dev/null 2>&1
fi

mkdir -p tmp

rm -f tmp/asciidoc.js
rm -f tmp/asciidoc.css

cat `ls scripts/*.js` > tmp/asciidoc.js
cat stylesheets/*.css > tmp/asciidoc.css

unamestr=`\uname`
if [[ "$unamestr" == 'Linux' ]]; then
   pushd $(dirname $(readlink -f $0)) > /dev/null 2>&1
else
   pushd $(dirname $(greadlink -f $0)) > /dev/null 2>&1
fi

# Gather version information
pushd .. > /dev/null 2>&1
VERSION=`libexec/svn-revision |awk '{print $1}'`
popd > /dev/null 2>&1
echo Version is $VERSION
echo Installing docs into $INSTALLATION_DIRECTORY

# Copy graphics
cp -R icons $INSTALLATION_DIRECTORY/$VERSION

# Convert files
for directory in $DIRECTORIES
do
   pushd $directory > /dev/null 2>&1
   for libtemplate in `ls -1 *.def 2>/dev/null`; do
     $(pwd)/../tools/process-template $libtemplate
     if [ "$?" != "0" ]; then
     	echo "Error processing library template $libtemplate"
     	exit 1
     fi
   done
   FILES=`ls -1 *.txt 2>/dev/null`
   for file in $FILES
   do
      BN=`basename -s .txt $file`
      echo Converting "$directory/$file" to HTML
      asciidoc -a toc -a toclevels=2                            \
               -a max-width=750px                               \
               -a textwidth=80                                  \
               -a source-highlighter=$(pwd)/../tools/hlfilter   \
               -a stylesdir=$(pwd)/../tmp/                      \
               -a scriptsdir=$(pwd)/../tmp/                     \
               -a asciimath                                     \
               -a icons                                         \
               -a iconsdir=../icons                             \
               -f $(pwd)/../asciidoc.conf                       \
               $file
      if (( MAKE_PDF ))
      then
        echo Converting "$directory/$file" to PDF
        #a2x --format=pdf --no-xmllint $file
        asciidoc -a toc -a toclevels=2                            \
                 -a asciimath                                     \
                 -a icons                                         \
                 -a iconsdir=../icons                             \
                 -f $(pwd)/../latex.conf                          \
                 -b latex                                         \
                 $file
        for svg in `ls -1 *.svg 2>/dev/null`; do
        	SVGBN=`basename -s .svg $svg`
        	convert $svg $SVGBN.pdf
        done
        sed 's/\.svg\}/.pdf}/' <$BN.tex >$BN.tex.new
        rm $BN.tex
        mv $BN.tex.new $BN.tex
        pdflatex $BN.tex #2>&1 >$BN.texlog
      fi
   done

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION"
   fi

   if [ ! -d "$INSTALLATION_DIRECTORY/$VERSION/$directory" ]; then
      mkdir $INSTALLATION_DIRECTORY/$VERSION/$directory || crash "Unable to create directory $INSTALLATION_DIRECTORY/$VERSION/$directory"
   fi

   # Copy all files to destination (may include graphics, etc)
   for copyfile in `find -L . -type f 2>/dev/null |grep -v .svn`
   do
      DN=`dirname $copyfile`
      mkdir -p $INSTALLATION_DIRECTORY/$VERSION/$directory/$DN > /dev/null 2>&1
      cp $copyfile $INSTALLATION_DIRECTORY/$VERSION/$directory/$DN || crash "Unable to copy $copyfile to $INSTALLATION_DIRECTORY/$VERSION/$directory"
   done

   popd > /dev/null 2>&1
done
popd > /dev/null 2>&1

find $INSTALLATION_DIRECTORY/$VERSION -type f -exec chgrp $GROUP {} \; -exec chmod $CHMOD_FILE_MODE {} \; > /dev/null 2>&1
find $INSTALLATION_DIRECTORY/$VERSION -type d -exec chgrp $GROUP {} \; -exec chmod $CHMOD_DIRECTORY_MODE {} \; > /dev/null 2>&1

rm -rf tmp