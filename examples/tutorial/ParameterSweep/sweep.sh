#! /bin/sh

# Create new runNNN directory

rundir=$( echo run??? | sed -e 's/^.*run//' | awk '{ printf("run%03d\n", $1+1)}' )
mkdir $rundir

cat >$rundir/cf <<END

wrapperlog.always.transfer=true
sitedir.keep=true
execution.retries=0
lazy.errors=false

END

cat >$rundir/local.xml <<END

<config>
  <pool handle="local">
    <execution provider="local" />
    <profile namespace="karajan" key="jobThrottle">.23</profile>
    <profile namespace="karajan" key="initialScore">10000</profile>
    <filesystem provider="local"/>
    <workdirectory>$PWD/swiftwork</workdirectory>
  </pool>
</config>

END

cat >$rundir/tc <<END

local gensweep $PWD/gensweep.sh
local simulate $PWD/simulate.sh

END

cp sweep.swift $rundir

cd $rundir
echo Running in directory $rundir
swift -config cf -tc.file tc -sites.file local.xml sweep.swift $* 2>&1 | tee swift.out
