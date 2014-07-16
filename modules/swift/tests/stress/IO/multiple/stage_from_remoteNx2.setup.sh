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
        SIZE=20
        LOOPS=50
        ;;
    "S2")
        SIZE=50
        LOOPS=50
        ;;
    *)
        SIZE=20
        LOOPS=50
        ;;
esac

echo "-loops=$LOOPS -size=$SIZE " > $ARGS_FILE

cat <<'EOF' > filemaker.sh 
#!/bin/bash

echo "From filemaker.sh $1 $2 on Host:$HOSTNAME"
MAXSIZE=$1
OUT=$2
dd if=/dev/zero of=$OUT bs=1024 count=0 seek=$((1024*MAXSIZE))

EOF