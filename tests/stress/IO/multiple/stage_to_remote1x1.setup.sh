#!/bin/bash

if [[ -z $MIDWAY_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{mid.USER}/$MIDWAY_USERNAME/" > tmp && mv tmp\
 sites.xml
fi
if [[ -z $UC3_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{uc3.USER}/$UC3_USERNAME/" > tmp && mv tmp si\
tes.xml
fi
if [[ -z $BEAGLE_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{beagle.USER}/$BEAGLE_USERNAME/" > tmp && mv \
tmp sites.xml
fi


ARGS_FILE=${0%.setup.sh}.args

case $STRESS in
    "S1")
	FILES=50
        LOOPS=0
        ;;
    "S2")
	FILES=100
        LOOPS=0
        ;;
    *)
        FILES=50
        LOOPS=0
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
