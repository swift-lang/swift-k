#! /bin/sh

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



DIR=`pwd`/nightly-test/cogkit
LOGFILE=`pwd`/test.log

#####################################################

CVS=:pserver:anonymous@cvs.cogkit.org/cvs/cogkit

check_error()
{
  # Function. Parameter 1 is the return code
  # Parameter 2 is the text displayed on failure.
  # Parameter 3 is the text displayed on success. 
  if [ "${1}" -ne "0" ]; then
    echo "[ERROR ${1}]: ${2}"
    # exit the script with the last reported error code.
#    echo =======================================
#    cat $LOGFILE
#    echo =======================================
    echo The log file is located in $LOGFILE
    echo Please invoke 
    echo "    " more $LOGFILE 
    echo to see more details
    exit ${1}
  else
    echo "[OK]: ${3}"
  fi
}

#
# first remove the old log file
#


rm -f $LOGFILE

echo ----------------------------------------------------------------------
echo Testing the Java CoG Kit Build process
echo ----------------------------------------------------------------------

echo LOGFILE=$LOGFILE

date >> $LOGFILE 2>&1

rm -rf $DIR >> $LOGFILE 2>&1
check_error $? "Could not remove directory $DIR" "Removed directory $DIR" 

mkdir -p $DIR >> $LOGFILE 2>&1
check_error $? "Could not create directory $DIR" "Created directory $DIR" 


cd $DIR  >> $LOGFILE 2>&1
check_error $? "cd to $DIR failed" "cd to $DIR"

#
# checkout Cog 4
#
cvs -d $CVS co src/cog >> $LOGFILE 2>&1
check_error $? "cvs checkout cog failed" "cvs checkout cog"

#
# checkout jglobus
#
cvs -d $CVS co -r jglobus-jgss src/jglobus >> $LOGFILE 2>&1
check_error $? "cvs checkout cog failed" "cvs checkout jglobus"

#
# checkout manual
#
cvs -d $CVS co doc/manual >> $LOGFILE 2>&1
check_error $? "cvs checkout cog failed" "cvs checkout manual"

cvs -d $CVS co papers/bib >> $LOGFILE 2>&1
check_error $? "cvs checkout cog failed" "cvs checkout manual"


#
# create the code
#

cd $DIR/src/cog 
ant dist >> $LOGFILE 2>&1
check_error $? "compiling cog failed" "compiled cog"

#
#
# create the code
#

cd $DIR/src/jglobus
ant dist >> $LOGFILE 2>&1
check_error $? "compiling jglobus failed" "compiled jglobus"

#
# create manual
#
cd $DIR/doc/manual
make >> $LOGFILE 2>&1
check_error $? "manual creation failed" "manual created"

#
date >> $LOGFILE 2>&1
#