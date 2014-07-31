#!/bin/bash

ARGS_FILE=${0%.setup.sh}.args

case $STRESS in
    *)
	DELAY=10
        FILES=10
        LOOPS=10
	TIMEOUT=600
        ;;
esac

dd if=/dev/zero of=dummy bs=1024 count=0 seek=$((1024*FILES))
echo "$TIMEOUT"                    > ${0%.setup.sh}.timeout
echo "-loops=$LOOPS -delay=$DELAY" > $ARGS_FILE

cat <<'EOF' > wrapper.sh
#!/bin/bash
ARG1=$1
ARG2=$2

echo "ARGS : $*"
DEFAULT_SLEEP=60
VARIANCE=20 # in percent

ls | grep "$1" &> /dev/null
if [ $? == 0 ]
then
    echo "Hey this is wrapper and the $1 exists as a file";
    ls -lah;
else
    echo "Doinks! the file we need isn't here";
    ls -lah
fi

if [ -z $ARG2 ]
then
    echo "Switching to default"
    ARG2=$DEFAULT_SLEEP
fi

CHANGE=$(($ARG2*$VARIANCE/100))
RAND=$(shuf -i 0-$((2*CHANGE)) -n 1)

echo "sleep $(($ARG2-$CHANGE+$RAND))"
sleep $(($ARG2-$CHANGE+$RAND))

cat $ARG1 > $ARG1.test

if [ $? == 0 ]
then
    echo "The cat worked! ";
else
    echo "The cat failed ";
fi

rm $ARG1 $ARG1.test
echo "Residual files cleaned up"
EOF
