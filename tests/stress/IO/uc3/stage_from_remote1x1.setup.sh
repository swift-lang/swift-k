#!/bin/bash

ARGS_FILE=${0%.setup.sh}.args
UC3_USERNAME=$UC3_USERNAME

case $STRESS in
    "S1")
        SIZE=10
        LOOPS=0
        ;;
    "S2")
        SIZE=15
        LOOPS=0
        ;;
    *)
        SIZE=10
        LOOPS=0
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

if [[ -z $UC3_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{env.USER}/$UC3_USERNAME/" > tmp && mv tmp sites.xml
fi
