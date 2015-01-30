#!/bin/bash

ARGS_FILE=${0%.setup.sh}.args
BEAGLE_USERNAME=$BEAGLE_USERNAME

case $STRESS in
    "S1")
        FILES=10
        LOOPS=10
        ;;
    "S2")
        FILES=10
        LOOPS=50
        ;;
    *)
        FILES=10
        LOOPS=10
        ;;
esac


dd if=/dev/zero of=dummy bs=1024 count=0 seek=$((1024*FILES))
echo "-loops=$LOOPS" > $ARGS_FILE

cat <<'EOF' > wrapper.sh
#!/bin/bash
ARG1=$1
ls | grep "$1" &> /dev/null
if [ $? == 0 ]
then
    echo "Hey this is wrapper and the $1 exists as a file";
    ls -lah;
else
    echo "Doinks! the file we need isn't here";
    ls -lah
fi
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

