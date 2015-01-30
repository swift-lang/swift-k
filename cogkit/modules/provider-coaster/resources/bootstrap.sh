BS=$1
LS=$2
EMD5=$3
ID=$5
H=$6
HO=$7
B="coaster-bootstrap"

error() {
	echo $1
	echo $1 >>$L
	rm -f $DJ
	exit 1
}
tf() {
        eval [ "X$1" == "X" ] || [ ! -x "$1" ]
}
detectPaths() {
        R=`eval which java 2>>$L`
        CMD="which java 1>/tmp/$ID"
        if tf $R; then
                /bin/bash -l -c "$CMD" >>$L
                R=`cat /tmp/$ID`
                if tf $R; then
                        R=`eval echo $JAVA_HOME`
                        if [ "X$R" != "X" ]; then
                                WR=plain
                                R=$R/bin/java
                        else
                                /bin/bash -l -c 'echo $JAVA_HOME 1>/tmp/$ID' 2>>$L
                                R=`cat /tmp/$ID`
                                if [ "X$R" == "X" ]; then
                                        error "Cannot find java"
                                fi
                                WR=wrapped
                                R=$R/bin/java
                        fi
                else
                        WR=wrapped
                fi
        else
                WR=plain
        fi
        echo "using $WR mode" >>$L
        rm -f /tmp/$ID
        JAVA=$R
}
wrapped() {
        IFS=" "
        /bin/bash -l -c "$* 1>/tmp/$ID" >>$L
        cat /tmp/$ID
}
plain() {
        eval "$@"
}

if [ "$HO" == "" ]; then
	HO=$HOME
fi

if [ "$L" == "" ]; then
	L=$HO/$B-$ID.log
fi

detectPaths
DJ=`mktemp /tmp/bootstrap.XXXXXX`
UNAME=`uname`
echo "BS: $BS" >>$L
WGET=`$WR which wget`
if [ "X$WGET" == "X" ]; then
	WGET=`$WR which curl`
	if [ "X$WGET" == "X" ]; then
	    error "No wget or curl available"
	elif [ "$UNAME" == "Darwin" ]; then
	    WGET="$WGET -o $DJ $BS/$B.jar >>$L 2>&1"
	else
	    WGET="$WGET -O $DJ $BS/$B.jar >>$L 2>&1"  
	fi
else
	WGET="$WGET -c -q $BS/$B.jar -O $DJ >>$L 2>&1"
fi
eval $WGET
if [ "$?" != "0" ]; then
	error "Failed to download bootstrap jar from $BS"
fi

MD5SUM=`$WR which gmd5sum 2>>$L`
if [ "X$MD5SUM" == "X" ]; then
    MD5SUM=`$WR which md5sum 2>>$L`
    if [ "X$MD5SUM" == "X" ]; then
        MD5SUM=`$WR which md5 2>>$L`
        if [ "X$MD5SUM" == "X" ]; then
            error "No md5/md5sum/gmd5sum found"
        else
            if [ "$UNAME" == "Darwin" ]; then
               MD5SUM="$MD5SUM -r"
            fi
        fi
    fi
fi
AMD5=`$MD5SUM $DJ`
AAMD5=`eval echo \$\{AMD5:0:32\}`
echo "Expected checksum: $EMD5" >>$L
echo "Computed checksum: $AAMD5" >>$L
if [ "$AAMD5" != "$EMD5" ]; then
	error "Bootstrap jar checksum failed: $EMD5 != $AAMD5"
fi
echo "JAVA=$JAVA" >>$L
if [ -x $JAVA ]; then
	CMD="$WR $JAVA -Djava=\"$JAVA\" -Xmx64M -DGLOBUS_TCP_PORT_RANGE=\"$GLOBUS_TCP_PORT_RANGE\" -DX509_USER_PROXY=\"$X509_USER_PROXY\" -DX509_CERT_DIR=\"$X509_CERT_DIR\" -DGLOBUS_HOSTNAME=\"$H\" -Duser.home=\"$HO\" -jar $DJ $BS $LS $ID"
	echo $CMD >>$L
	eval $CMD >>$L
	EC=$?
	echo "EC: $EC" >>$L
	rm -f $DJ
	exit $EC
else
	error "Could not find a valid java executable"
fi
