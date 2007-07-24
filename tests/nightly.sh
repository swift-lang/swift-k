#!/bin/bash

OUTDIR=$PWD
LOGCOUNT=0
SEQ=1
DATE=`date +"%Y-%m-%d"`
TIME=`date +"%T %Z(%z)"`
RUNDIRBASE="run-$DATE"
RUNDIR=$OUTDIR/$RUNDIRBASE
mkdir -p $RUNDIR

if [ "$1" == "-fco" ]; then
	FCO=1
	shift
elif [ "$2" == "-fco" ]; then
	FCO=1
fi

if [ "$1" == "" ]; then
	OUTBASE=$RUNDIRBASE/tests.log
else
	OUTBASE=$RUNDIRBASE/$1
fi
OUT=$OUTDIR/$OUTBASE

head() {
	HTMLBASE=tests-$DATE.html
	HTML=$OUTDIR/$HTMLBASE
	rm -f $OUTDIR/current.html
	
	#This doesn't work well with servers that don't follow symlinks
	#ln -s $HTML $OUTDIR/current.html
	
	cat <<DOH >$OUTDIR/current.html
<html>
	<head>
		<title>Redirecting...</title>
		<meta http-equiv="cache-control" content="no-cache">
		<script language="JavaScript">
			function redirect() {
				window.location="$HTMLBASE";
			}
		</script>
	</head>
	<body onLoad="redirect()">
		You should be redirected to <a href="$HTMLBASE">$HTMLBASE</a>
	</body>
</html>
DOH
	cat <<DOH >$HTML
<html>
	<head>
		<title>Swift nightly integration tests and build ($DATE $TIME)</title>
		<style type="text/css">
			a:link {color:black}
			a:visited {color:black}
			td.success {background: #60ff00; text-align: center;}
			td.failure {background: #ff6000; text-align: center;}
			tr.testline {background: #e0e0e0}
			tr.part {background: #c0c0c0; text-align: center; font-size: large;}
		</style>
	</head>
	<body>
	<h1>Swift nightly integration tests and build</h1>
	<ul>
	  <li>Date: $DATE</li>
	  <li>Time: $TIME</li>
	  <li>Test host: $(hostname)</li>
	</ul>
	<ol>
		<li><a href="#tests">Test results</a>
		<li><a href="#packages">Compiled packages</a>
		<li><a href="#older">Older tests</a>
		<li><a href="addtests.html">How to add new tests</a>
	</ol>
DOH
	FIRSTTEST=1
}

html() {
	echo $@ >>$HTML
}

tail() {
	MONTHS=("" "Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec")
	html "</tr></table></tr></table>"
	
	if [ "$BINPACKAGE" != "" ]; then
		FBP=$OUTDIR/$BINPACKAGE
		SIZE=`ls -hs $FBP`
		SIZE=${SIZE/$FBP}
		cat <<DOH >>$HTML
	<h1>Binary packages</h1>
	<a name="#packages">
	<a href="$BINPACKAGE">$BINPACKAGE</a> ($SIZE)<br>
DOH
	fi
	
	LASTYR="00"
	LASTMO="00"
	html "<h1>Older tests</h1>"
	html '<a name="older">'
	html "<table><tr>"
	for OLDER in `ls $OUTDIR/tests-*.html|sort`; do
		O=`basename $OLDER`
		YR=${O:6:2}
		MO=${O:8:2}
		DY=${O:10:2}
		if echo "$DY$MO$YR"|egrep -v "[0-9]{6}"; then
			YR=${O#tests-}
			YR=${YR%.html}
			MO=0
			DY=$YR
		else
			YR="20$YR"
		fi
		if [ $LASTYR != $YR ]; then
			html "</tr></table>"
			html "<h2>$YR</h2>"
			LASTYR=$YR
		fi
		if [ $LASTMO != $MO ]; then
			html "</tr></table>"
			html "<h3>${MONTHS[$MO]}</h3>"
			html "<table border=\"0\"><tr>"
			LASTMO=$MO
		fi
		SUCCESS=`grep 'class="success"' $OLDER|wc -l`
		FAILURE=`grep 'class="failure"' $OLDER|wc -l`
		if [ "$SUCCESS$FAILURE" == "00" ]; then
			COLOR="#e0e0e0"
		else
			COLOR=`perl -e "printf \"#%02x%02x%02x\", $FAILURE/($SUCCESS+$FAILURE)*220+35, $SUCCESS/($SUCCESS+$FAILURE)*220+35, 40;"`
		fi
		html "<td bgcolor=\"$COLOR\"><a href=\"$O\">$DY</a></td>"
	done
	html "</tr></table><br><br>"
	cat <<DOH >>$HTML
	<a href="addtests.html">How to add new tests</a>
	</body>
</html>
DOH
}


outecho() {
	TYPE=$1
	shift
	echo "<$TYPE>$1|$2|$3|$4|$5|$6|$7|$8|$9|"
}

out() {
	echo $@
	TYPE=$1
	if [ "$TYPE" == "test" ]; then
	
		NAME=$2
		SEQ=$3
		CMD=$4
		RES=$5
		LOG=$6
		
		if [ "$FIRSTTEST" == "1" ]; then
			html "<h1>Test results</h1>"
			html "<a name=\"tests\">"
			html "<a href=\"$OUTBASE\">Output log from tests</a>"
			html "<table border=\"0\">"
			FIRSTTEST=0
		else
			if [ "$FLUSH" == "1" ]; then
				html "</tr></table></tr>"
			fi
		fi
		
		if [ "$TESTPART" != "" ]; then
			html "<tr class=\"part\"><th colspan=\"2\">$TESTPART</th></tr>"
			TESTPART=
		fi
		
		if [ "$FLUSH" == "1" ]; then
			html "<tr class=\"testline\"><th align=\"right\">$NAME: </th><td><table border=\"0\"><tr>"
		fi
		if [ ${#SEQ} -gt 2 ]; then
			WIDTH=""
		else
			WIDTH="width=\"20\""
		fi
		if [ "$RES" == "Passed" ]; then
			html "<td class=\"success\" $WIDTH title=\"$CMD\">"
			html "<a href=\"$LOG\">$SEQ</a>"
		else
			html "<td class=\"failure\" $WIDTH title=\"$CMD\">"
			html "<a href=\"$LOG\">$SEQ</a>"
		fi
		html "</td>"
		
	elif [ "$TYPE" == "package" ]; then
		BINPACKAGE=$2
	else
		html $@
	fi
	
}

aexec() {
	echo Executing "$@" >>$OUT
	rm -f $OUTDIR/x73010test.log
	LASTCMD="$@"
	"$@" >$OUTDIR/x73010test.log 2>&1
	EXITCODE=$?
	if [ "$EXITCODE" == "127" ]; then
		echo "Command not found: $@" >$OUTDIR/x73010test.log
	fi
	if [ -f $OUTDIR/x73010test.log ]; then
		cat $OUTDIR/x73010test.log >>$OUT
	fi
	
}

tlog() {
	TLOG="output_$LOGCOUNT.txt"
	rm -f $RUNDIR/$TLOG
	banner "$LASTCMD" $RUNDIR/$TLOG
	if [ -f $OUTDIR/x73010test.log ]; then
		cat $OUTDIR/x73010test.log >>$RUNDIR/$TLOG 2>>$OUT
	fi
	TLOG="$RUNDIRBASE/$TLOG"
	let "LOGCOUNT=$LOGCOUNT+1"
}

fexec() {
	FLUSH=1
	banner "$TEST (faked)"
	echo "Faking $TEST"
	EXITCODE=0
	LASTCMD=""
	vtest
}

banner() {
	if [ "$2" == "" ]; then
		BOUT=$OUT
	else
		BOUT=$2
	fi
	echo "">>$BOUT
	echo "*****************************************************************************************">>$BOUT
	echo "* $1" >>$BOUT
	echo "*****************************************************************************************">>$BOUT
}

pexec() {
	banner "$TEST (part $SEQ)"
	echo "Executing $TEST (part $SEQ)"
	aexec "$@"
	ptest
	let "SEQ=$SEQ+1"
	FLUSH=0
}

ssexec() {
	SEQSAVE=$SEQ
	SEQ=$1
	shift
	banner "$TEST (part $SEQ)"
	echo "Executing $TEST (part $SEQ)"
	aexec "$@"
	ptest
	SEQ=$SEQSAVE
	FLUSH=0
}

vexec() {
	if [ "$SEQ" == "1" ]; then
		banner "$TEST"
		echo "Executing $TEST"
	else
		banner "$TEST (part $SEQ)"
		echo "Executing $TEST (part $SEQ)"
	fi
	aexec "$@"
	vtest
	SEQ=1
	FLUSH=1
}

ptest() {
	if [ "$EXITCODE" == "0" ]; then
		RES="Passed"
	else
		RES="Failed"
	fi
	tlog
	out test "$TEST" $SEQ "$LASTCMD" $RES $TLOG
	if [ "$EXITONFAILURE" == "true" ]; then
		if [ "$EXITCODE" != "0" ]; then
			exit
		fi
	fi
}

vtest() {
	EC=$?
	if [ "$EXITCODE" == "0" ]; then
		RES="Passed"
	else
		RES="Failed"
	fi
	tlog
	out test "$TEST" $SEQ "$LASTCMD" $RES $TLOG
	if [ "$EXITCODE" != "0" ]; then
		if [ "$EXITONFAILURE" == "true" ]; then
			exit
		fi
	fi
}

date > $OUT
FLUSH=1

head
TESTPART="Part I: Build"
EXITONFAILURE=true
if [ "$FCO" != "1" ]; then
	TEST="Checkout CoG"
	pexec rm -rf cog
	vexec svn co https://cogkit.svn.sourceforge.net/svnroot/cogkit/trunk/current/src/cog

	TEST="Checkout Swift"
	pexec rm -rf trunk
	#vexec cvs -d :pserver:anonymous@cvs.cogkit.org:/cvs/cogkit co src/vdsk
	vexec svn co https://svn.ci.uchicago.edu/svn/vdl2/trunk
fi

TEST="Directory setup"
pexec mkdir cog/modules/vdsk
vexec cp -r trunk/* cog/modules/vdsk

TEST="Compile"
pexec cd cog/modules/vdsk
pexec rm -rf dist
vexec ant -quiet dist

TEST="Package"
pexec cd dist
VDSK=`ls -d vdsk*.*`
pexec cd $VDSK/lib
pexec rm -f castor*.jar *gt2ft*.jar ant.jar
pexec cd ../..
pexec rm -rf vdsk-$DATE
pexec mv $VDSK vdsk-$DATE
vexec tar -pczf $OUTDIR/vdsk-$DATE.tar.gz vdsk-$DATE
out package "vdsk-$DATE.tar.gz"

PATH=$PWD/vdsk-$DATE/bin:$PATH
cd ..
TESTDIR=$PWD/tests
echo "Path: $PATH" >>$OUT
cd $RUNDIR

EXITONFAILURE=false
TESTPART="Part II: Local Tests"

for TEST in `ls $TESTDIR/*.dtm $TESTDIR/*.swift`; do
	BN=`basename $TEST`
	echo $BN
	cp $TESTDIR/$BN .
	
	
	TESTNAME=${BN%.dtm}
	TESTNAME=${TESTNAME%.swift}
	TEST="<a href=\"$RUNDIRBASE/$BN\">$TESTNAME</a>"
	
	ssexec "Compile" vdlc $BN
	for ((i=0; $i<9; i=$i+1)); do
		pexec swift -sites.file ~/.vdl2/sites-local.xml $TESTNAME.kml
	done
	vexec swift -sites.file ~/.vdl2/sites-local.xml $TESTNAME.kml
done

TESTPART="Part III: Grid Tests"

for TEST in `ls $TESTDIR/*.dtm $TESTDIR/*.swift`; do
	BN=`basename $TEST`
	echo $BN
	cp $TESTDIR/$BN .
	
	TESTNAME=${BN%.dtm}
	TESTNAME=${TESTNAME%.swift}
	TEST="<a href=\"$RUNDIRBASE/$BN\">$TESTNAME</a>"
	
	ssexec "Compile" vdlc $BN
	for ((i=0; $i<9; i=$i+1)); do
		pexec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
	done
	vexec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
done

#Don't remove me:
tail
