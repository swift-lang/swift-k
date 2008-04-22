BS=$0
LS=$1
EMD5=$2
LMD5=$3
ID=$4
H=$5
L=$6
error() {
	echo $1
	echo $1 >>$L
	rm -f $DJ
	exit 1
}
if [ "$L" == "" ]; then 
	L=~/coaster-boot-$ID.log 
fi
DJ=`mktemp bootstrap.XXXXXX`
echo "BS: $BS" >>$L
wget -c -q $BS/coaster-bootstrap.jar -O $DJ >>$L 2>&1
if [ "$?" != "0" ]; then
	error "Failed to download bootstrap jar from $BS"
fi
AMD5=`/usr/bin/md5sum $DJ`
echo "Expected checksum: $EMD5" >>$L
echo "Computed checksum: ${AMD5:0:32}" >>$L
if [ "${AMD5:0:32}" != "$EMD5" ]; then
	error "Bootstrap jar checksum failed: $EMD5 != ${AMD5:0:32}"
fi

if [ "$JAVA_HOME" != "" ]; then
	JAVA=$JAVA_HOME/bin/java
else
	JAVA=`which java`
fi
echo "JAVA=$JAVA" >>$L
if [ -x $JAVA ]; then 
	echo "$JAVA -Djava.home="$JAVA_HOME" -DX509_USER_PROXY="$X509_USER_PROXY" -DGLOBUS_HOSTNAME="$H" -jar $DJ $BS $LMD5 $LS $ID" >>$L
	$JAVA -Djava.home="$JAVA_HOME" -DX509_USER_PROXY="$X509_USER_PROXY" -DGLOBUS_HOSTNAME="$H" -jar $DJ $BS $LMD5 $LS $ID >>$L 2>&1
	EC=$?
	echo "Exit code: $EC" >>$L
	rm -f $DJ
	exit $EC
else
	error "Could not find a valid java executable"
fi
