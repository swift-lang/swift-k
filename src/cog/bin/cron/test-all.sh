#! /bin/sh 

echo this script has not yet been tested

exit

# Change these for your configuration

# Main build directory
BUILDDIR=$HOME/tmp/cog-nightly-test

# The log file
LOGFILE=$BUILDDIR/test.log

# The directory to put the cvs modules in
SOURCES=$BUILDDIR/cogkit

# The directory to generate the html output in
HTMLOUTDIR=$HOME/public_html

# A directory containing jdks. It can contain other things, which will be filtered out
JDKSDIR=/home/dsl/javapkgs/linux

# A list of full paths to JDKS. Leave blank to use the above
JDKS=

# The location of Ant
ANT_HOME=/home/dsl/javapkgs/jakarta-ant-1.5.1

# The location of the cvs repository
CVSROOT=":pserver:anonymous@cvs.cogkit.org:/cvs/cogkit"

# The cog.properties file to be used. If it is not found, a default will be used
COG_PROPERTIES=$HOME/.globus/cog-psaltery.properties

# A list of URLs containing lists of globus machines/services.
# You can use file://... for local files
# See the README for the format of the list.
HOSTLISTS="http://www-unix.mcs.anl.gov/~gose/env.txt"

# Local means that the test will be executed from this directory
# This directory has to be a valid Java CoG Kit module. Along with it, you
# need a jglobus directory.
# instead of a fresh checkout. "yes"|"no"
LOCAL="no"

# Time in seconds (CPU time) after which to kill a test if it has not completed
TIMEOUT=300 

# End of configuration part


TESTSOUTDIR=$HTMLOUTDIR/tests
PATH=$ANT_HOME/bin:$PATH
export PATH
export ANT_HOME
ANT_VERSION=`ant -version`
UNAME=`uname -srmp`

timeout_wrapper(){
    TMPSCRIPT=`mktemp /tmp/timeoutwrapperXXXXXX`
    COMMAND=$1
    export COMMAND
    export TIMEOUT
    cat <<LULU >$TMPSCRIPT
#!/bin/bash
ulimit -t $TIMEOUT
$COMMAND
LULU
    chmod +x $TMPSCRIPT
    sleep $TIMEOUT && killall -q `basename $TMPSCRIPT` >>$LOGFILE 2>&1 &
    $TMPSCRIPT >>$LOGFILE 2>&1
    rm -f $TMPSCRIPT
}

error()
{
  # Function. Parameter 1 is the return code
  # Parameter 2 is the text displayed on failure.
  # Parameter 3 is the text displayed on success. 
  if [ "$1" -ne "0" ]; then
    echo "[ERROR $1]: $2"
    echo The log file is located in $LOGFILE
    exit $1 
  else
    echo "[OK]: $3"
  fi
}

warning()
{
  if [ "$1" -ne "0" ]; then
    echo "[WARNING $1]: $2"
  else
    echo "[OK]: $3"
  fi
}

mkdir -p $BUILDDIR 
rm -f $LOGFILE

echo SCRIPT=$0
echo LOGFILE=$LOGFILE
date >> $LOGFILE 2>&1




cvs_checkout_jgss()
{
# ERROR IN SCRIPT: you do not need a cvs pass for this
    case $CVSROOT in
	:pserver*)
	    echo "/1 $CVSROOT A" >$HOME/.cvspass
	    ;;
    esac
    cvs -d $CVSROOT co -r jglobus-jgss $1 >>$LOGFILE 2>&1
    error $? "cvs checkout $1 failed" "cvs checkout $1"
}

cvs_checkout()
{
# ERROR IN SCRIPT: you do not need a cvs pass for this
    case $CVSROOT in
	:pserver*)
	    echo "/1 $CVSROOT A" >$HOME/.cvspass
	    ;;
    esac
    cvs -d $CVSROOT co $1 >>$LOGFILE 2>&1
    error $? "cvs checkout $1 failed" "cvs checkout $1"
}

if test $LOCAL = "yes"; then
    cd ..
else
    rm -rf $SOURCES >>$LOGFILE 2>&1

    mkdir $SOURCES >>$LOGFILE 2>&1
    error $? "Could not create directory $SOURCES" "Created directory $SOURCES" 

    cd $SOURCES >>$LOGFILE 2>&1
    error $? "cd to $SOURCES failed" "cd to $SOURCES"

    cvs_checkout src/cog
    cvs_checkout_jgss src/jglobus

    cd $SOURCES/ogce
fi

echo "Ant version is $ANT_VERSION" >> $LOGFILE

# Copy the host list
rm -f machines.txt
for HOSTLIST in $HOSTLISTS; do
    OUTPUTFILE=`mktemp /tmp/hostlistXXXXXX`    
    wget -O $OUTPUTFILE $HOSTLIST >>$LOGFILE 2>&1
    if test $? -ne 0; then
	echo "Could not download hostlist: $HOSTLIST" >> $LOGFILE
    else
	cat $OUTPUTFILE >> machines.txt
    fi 
    rm -f $OUTPUTFILE >>$LOGFILE 2>&1
done

mkdir -p $TESTSOUTDIR >>$LOGFILE 2>&1
DATE=`date`
export UNAME
export DATE
cat <<LULU >$TESTSOUTDIR/index.html
<html>
<head>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title>Java CoG Kit nightly test</title>
</head>
<body>
<h3> Java CoG Kit nightly test</h3>
<p>
Host machine: $HOSTNAME <br>
OS: $UNAME <br>
Date: $DATE <br><br>
The tests were performed on the following JDKs:<br>
LULU

if test -z $JDKS; then
    for JDKDIR in $JDKSDIR/*; do
	if test -f $JDKDIR/bin/javac; then
	    #test to see if it's not already there
	    NEW=1
	    for JDK in $JDKS; do
		if test $JDK/bin/javac -ef $JDKDIR/bin/javac; then
		    NEW=0
		fi
	    done
	    if test $NEW -eq 1; then
		JDKS="$JDKS $JDKDIR"
		echo "Adding $JDKDIR" >> $LOGFILE
	    else
		echo "$JDKDIR already added. Maybe it's a symlink" >>$LOGFILE
	    fi
	fi
    done
fi

INITIAL_PATH=$PATH

for JAVA_HOME in $JDKS; do
    echo "Running tests on $JAVA_HOME" >>$LOGFILE
    echo $JAVA_HOME
    PATH="$JAVA_HOME/bin:$INITIAL_PATH"
    export PATH
    export JAVA_HOME
    export JDK

    JDK=`basename $JAVA_HOME`
    JAVA_VERSION=`java -version 2>&1`
    
    TESTDIR=$JDK
    echo "<a href=\"$JDK/index.html\">$JDK</a><br>">>$TESTSOUTDIR/index.html
    mkdir -p $TESTSOUTDIR/$TESTDIR >>$LOGFILE 2>&1
    export JAVA_VERSION
    cat <<LULU > $TESTSOUTDIR/$TESTDIR/index.html
	<html>
	<head>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<title>Test results on $JAVA_VERSION</title>
	</head>
	<body>
LULU
    #clean old builds
    ant distclean >>$LOGFILE 2>&1
    #build
    ant dist >>$LOGFILE 2>&1
    if test $? -ne 0; then
	echo "[ERROR $?]: compiling ogce failed" >>$LOGFILE
	cat <<LULU >>$TESTSOUTDIR/$TESTDIR/index.html
	    <h1>Java CoG Kit build failed on the target JDK</h1>
	    </body>
	    </html>
LULU
	continue
    else
	echo "Build successfull" >>$LOGFILE
	cat <<LULU >>$TESTSOUTDIR/$TESTDIR/index.html
	<h3>Test results for $JAVA_VERSION</h3><br>
	    <a href="general-results.html">General tests</a><br>
	    <a href="gram-results.html">Gram tests</a><br>
	    <a href="ftp-results.html">GridFTP tests</a><br>
	</body>
	</html>
LULU
    fi
    
    mkdir $TESTSOUTDIR


    echo Starting production tests
    timeout_wrapper "ant -f test.xml -Dhtml.outputdir $TESTSOUTDIR/$JDK -Dorg.globus.config.file $COG_PROPERTIES general >> $LOGFILE 2>&1"
    warning $? "general tests failed" "tests"
    timeout_wrapper "ant -f test.xml -Dhtml.outputdir $TESTSOUTDIR/$JDK -Dorg.globus.config.file $COG_PROPERTIES gram >> $LOGFILE 2>&1"
    warning $? "gram test failed" "gram"
    timeout_wrapper "ant -f test.xml -Dhtml.outputdir $TESTSOUTDIR/$JDK -Dorg.globus.config.file $COG_PROPERTIES ftp >> $LOGFILE 2>&1"
    warning $? "ftp test failed" "ftp"
done

cat <<LULU >>$TESTSOUTDIR/index.html
</body>
</html>
LULU

chmod -R a+r $HTMLOUTDIR/tests
