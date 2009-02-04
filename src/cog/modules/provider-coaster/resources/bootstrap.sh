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
find() {
	R=`eval $1 2>/dev/null`
	if [ "X$R" == "X" ]; then
		R=`/bin/bash -l -c "$1"`
	fi
	echo $R
}
if [ "$L" == "" ]; then
	L=~/coaster-boot-$ID.log 
fi
DJ=`mktemp /tmp/bootstrap.XXXXXX`
echo "BS: $BS" >>$L
WGET=`find 'which wget'`
if [ "X$WGET" == "X" ]; then
	WGET=`find 'which curl'`
	if [ "X$WGET" == "X" ]; then
		error "No wget or curl available"
	fi
	WGET="$WGET -O $DJ $BS/coaster-bootstrap.jar >>$L 2>&1"
else
	WGET="$WGET -c -q $BS/coaster-bootstrap.jar -O $DJ >>$L 2>&1"
fi
eval $WGET
if [ "$?" != "0" ]; then
	error "Failed to download bootstrap jar from $BS"
fi
MD5SUM=`find 'which gmd5sum'`
if [ "X$MD5SUM" == "X" ]; then
	MD5SUM=`find 'which md5sum'`
	if [ "X$MD5SUM" == "X" ]; then
		error "No md5sum or gmd5sum found"
	fi
fi
AMD5=`$MD5SUM $DJ`
AAMD5=`eval echo \$\{AMD5:0:32\}`
echo "Expected checksum: $EMD5" >>$L
echo "Computed checksum: $AAMD5" >>$L
if [ "$AAMD5" != "$EMD5" ]; then
	error "Bootstrap jar checksum failed: $EMD5 != $AAMD5"
fi

JAVA_HOME=`find 'echo $JAVA_HOME'`

if [ "X$JAVA_HOME" == "X" ]; then
	JAVA=`find 'which java'`
	JAVA_HOME=$(dirname $JAVA)/..
else 
	JAVA=$JAVA_HOME/bin/java
fi
echo "JAVA=$JAVA" >>$L
if [ -x $JAVA ]; then 
	echo "$JAVA -Djava.home="$JAVA_HOME" -DX509_USER_PROXY="$X509_USER_PROXY" -DGLOBUS_HOSTNAME="$H" -jar $DJ $BS $LMD5 $LS $ID" >>$L
	$JAVA -Djava.home="$JAVA_HOME" -DGLOBUS_TCP_PORT_RANGE="$GLOBUS_TCP_PORT_RANGE" -DX509_USER_PROXY="$X509_USER_PROXY" -DX509_CERT_DIR="$X509_CERT_DIR" -DGLOBUS_HOSTNAME="$H" -jar $DJ $BS $LMD5 $LS $ID
	EC=$?
	echo "EC: $EC" >>$L
	rm -f $DJ
	exit $EC
else
	error "Could not find a valid java executable"
fi
