#!/bin/bash

ARGS_FILE=${0%.setup.sh}.args
BEAGLE_USERNAME=$BEAGLE_USERNAME

case $STRESS in
    "S1")
        SIZE=20
        LOOPS=10
        ;;
    "S2")
        SIZE=30
        LOOPS=20
        ;;
    *)
        SIZE=20
        LOOPS=10
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
