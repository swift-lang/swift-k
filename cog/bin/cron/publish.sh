#! /bin/sh 


$TMPDIR=/tmp

#
# use ssh-keygen -rsa 
# use ssh-keygen -dsa
# without password to create login onto the cvs server without passwd
# you need to concatenate the .pub keys on the remote machine
# BE careful checkin and choeckout will now not anymore preceeded by
# the password query. Some people may not want to do this in order
# to be more carefule with commits.

#
# a simple check error function
#
USER=`whoami`
USERDIR=/${TMPDIR}/${USER}

LOGFILE=${USERDIR}/test.log
DIR=${USERDIR}/nightly-test


#HTMLOUTDIR=/mcs/www-unix.globus.org/cog
#WEBADDRESS=http://www-unix.globus.org/cog

#####################################################

CVSROOT=":pserver:/cvs/cogkit"

ANT_HOME=/home/dsl/javapkgs/jakarta-ant-1.5
JAVA_HOME=/home/dsl/javapkgs/linux/j2sdk1.4.1_01

PATH=${JAVA_HOME}/bin:${ANT_HOME}/bin:${PATH}
export ANT_HOME
export JAVA_HOME
export PATH


ant -version
java -version

check_error()
{
  # Function. Parameter 1 is the return code
  # Parameter 2 is the text displayed on failure.
  # Parameter 3 is the text displayed on success. 
  if [ "${1}" -ne "0" ]; then
    echo "[ERROR ${1}]: ${2}"
    # exit the script with the last reported error code.
    echo The log file is located in ${LOGFILE}
    echo Please invoke 
    echo "    " more ${LOGFILE} 
    echo to see more details

    tail -f $LOGFILE

    exit ${1}
  else
    echo "[OK]: ${3}"
  fi
}



mkdir -p ${USERDIR} 



#
# first remove the old log file
#


rm -f ${LOGFILE}

echo ----------------------------------------------------------------------
echo Testing the Java CoG Build process
echo ----------------------------------------------------------------------
SCRIPT=`pwd`"/publish"
echo SCRIPT=$SCRIPT

echo LOGFILE=$LOGFILE

date >> ${LOGFILE} 2>&1

rm -rf ${DIR} >> ${LOGFILE} 2>&1


mkdir ${DIR} >> ${LOGFILE} 2>&1
check_error $? "Could not create directory ${DIR}" "Created directory ${DIR}" 



cd ${DIR}  >> ${LOGFILE} 2>&1
check_error $? "cd to $DIR failed" "cd to $DIR"

cvs_checkout()
{
    cvs -d $CVSROOT co ${1} >> $LOGFILE 2>&1
    check_error $? "cvs checkout ${1} failed" "cvs checkout ${1}"
}

cvs_checkout_jgss()
{
    cvs -d $CVSROOT co -r jglobus-jgss ${1} >> $LOGFILE 2>&1
    check_error $? "cvs checkout ${1} failed" "cvs checkout ${1}"
}

#
# checkout 
#
cvs_checkout_jgss ogce
cvs_checkout_jgss jglobus
cvs_checkout manual
cvs_checkout bib
cvs_checkout gt3
cvs_checkout design

#
# create the code
#

cd $DIR/ogce 
ant dist >> $LOGFILE 2>&1
check_error $? "compiling ogce failed" "compiled ogce"

#
# create javadoc
#
cd $DIR/ogce
ant doc >> $LOGFILE 2>&1
check_error $? "javadoc creation failed" "javadoc created"


#
# create manual
#
cd $DIR/manual
make >> $LOGFILE 2>&1
check_error $? "manual creation failed" "manual created"


#
# create the code
#
mkdir -p $HTMLOUTDIR/current
check_error $? "can not crate wab directory for manual" "created web directory"

rcp -r $DIR/jglobus $HTMLOUTDIR/current
rcp -r $DIR/ogce $HTMLOUTDIR/current
check_error $? "copied source code failed" "copied source code"

rcp -r $DIR/build/cog-* $HTMLOUTDIR/current
check_error $? "copied javadoc failed" "copied javadoc"

rcp -r $DIR/manual/manual.pdf $HTMLOUTDIR/manual.pdf
check_error $? "copied manual failed" "copied manual.pdf"

rcp -r $DIR/gt3/gridant/docs/grant-gt3-manual.pdf $HTMLOUTDIR/grant-gt3-manual.pdf
check_error $? "copied grant-gt3-manual.pdf failed" "copied grant-gt3-manual.pdf"

rcp -r $DIR/design/grant/grant.pdf $HTMLOUTDIR/grant.pdf
check_error $? "copied grant.pdf failed" "copied grant.pdf"

chmod a+r $HTMLOUTDIR/manual.pdf
chmod a+r $HTMLOUTDIR/grant-gt3-manual.pdf
chmod -R a+r $HTMLOUTDIR/current
check_error $? "chmod faild" "change to world readability"

cd $DIR/ogce
mkdir $HTMLOUTDIR/tests

# Copy the host list

wget http://www-unix.mcs.anl.gov/~gose/env.txt
rm -f machines.txt
mv env.txt machines.txt
#cp /homes/gose/public_html/env.txt ./machines.txt

echo Starting production tests 
ant -f test.xml -Dhtml.reports $HTMLOUTDIR/tests tests >> $LOGFILE 2>&1
check_error $? "general tests failed" "tests"
ant -f test.xml -Dhtml.reports $HTMLOUTDIR/tests gram >> $LOGFILE 2>&1
check_error $? "gram test failed" "gram"
ant -f test.xml -Dhtml.reports $HTMLOUTDIR/tests ftp >> $LOGFILE 2>&1
check_error $? "ftp test failed" "ftp"

chmod -R a+r $HTMLOUTDIR/tests

#

#INSTALLING JAVA WEBSTART
echo Starting Java Webstart Deployment 
ant -f webstart.xml -Dwww.install $HTMLOUTDIR/demo/ogce  -Dwww.address $WEBADDRESS/demo/ogce >> $LOGFILE 2>&1

check_error $? "webstart deployment failed" "Webstart Deployment Successfull"