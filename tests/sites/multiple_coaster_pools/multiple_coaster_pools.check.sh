#!/bin/bash

count_procs()
{
   file=$1
   if [ ! -f "$file" ]; then
      echo "File $file does not exist!"
      exit 1
   fi
   count=$( grep "/bin/bash hostsnsleep.sh" $file |grep -v "/usr/bin/time" |wc -l )
   echo $count
}

get_maximum_jobs_single()
{
   for file in output/output.single*.log
   do
      echo $( count_procs $file )
   done | sort -n |tail -1
}

get_maximum_jobs_multiple()
{
   for file in output/output.multiple*.log
   do
      echo $( count_procs $file )
   done |sort -n | tail -1
}

if [ $( get_maximum_jobs_single ) -ne 1 ]; then
   echo Incorrect number of jobs detected on a pool with jobspernode=1
   exit 1
fi

if [ $( get_maximum_jobs_multiple ) -le 1 ]; then
   echo Incorrect number of jobs detected on a pool with jobspernode=16
   exit 1
fi

exit 0
