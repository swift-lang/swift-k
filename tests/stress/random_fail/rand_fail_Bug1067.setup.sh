#!/bin/bash

# Setup script will just output the following file

cat<<'EOF' > randfail.sh
#!/bin/bash

FAIL_PROBABILITY=$1
DELAY=$2

ITEM=$(($RANDOM%100))
sleep $2

if (( "$ITEM" <= "$FAIL_PROBABILITY" ))
then
    echo "Failing $ITEM < $FAIL_PROBABILITY" >&2
    exit -1
fi
echo "Not failing $ITEM > $FAIL_PROBABILITY"
exit 0
EOF

