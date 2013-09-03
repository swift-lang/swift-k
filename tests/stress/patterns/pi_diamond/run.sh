#!/bin/bash
clear;
#swift -tc.file tc.data -sites.file sites.xml motoko.swift

#echo "Running run_catsn.swift"
#swift -tc.file tc.data -sites.file sites.xml run_catsn.swift
export GLOBUS_HOSTNAME="swift.rcc.uchicago.edu"

if [ "$1" != "" ]
then
    SITE="$1"
else
    SITE="local"
fi

#Call as check_error $? <ERR_CODE> <Error message>
check_error()
{
    if [ $1 == $2 ]
    then
        echo $3
    else
        echo "Run completed with code:$1"
    fi
}
export COG_OPTS="-Dtcp.channel.log.io.performance=true"

####################BARE BONES VERSION############################
{
    LOOPS=100
    RECSIZE=100 
    echo "RUNTYPE :BARE BONES VERSION, SITE: $SITE, CHUNKS: $RECSIZE X $LOOPS"
    time swift -tc.file tc.data -config cf -sites.file $SITE.xml teragen.swift -loops=$LOOPS -recsize=$RECSIZE
} | tee -a LAB_RECORDS

exit 0
####################PROVIDER STAGING VERSION######################

TOTALSIZE=1000000000 # Totalsize/chunksize is the  
CHUNKSIZE=100000000  # 10^8 records in each chunk
NUMCHUNKS=$(($TOTALSIZE / $CHUNKSIZE))
LOOP=1
TIMEOUT=600
time {
    echo "RUNTYPE :PROVIDER STAGING VERSION, SITE: $SITE, CHUNKS: $NUMCHUNKS"
    echo "timeout $TIMEOUT swift -tc.file tc.data -config cf -sites.file $SITE.xml teragen.swift -loops=$NUMCHUNKS"
    timeout $TIMEOUT swift -tc.file tc.data -config cf -sites.file $SITE.xml teragen.swift -loops=$NUMCHUNKS
    check_error $? 124 "Run terminated by timeout of $((TIMEOUT/60)) minute(s)"
} | tee -a LAB_RECORDS
exit 0
