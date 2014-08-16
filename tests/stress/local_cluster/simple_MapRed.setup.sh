#!/bin/bash

HOST=$(hostname -f)

if   [[ "$HOST" == *midway* ]]; then
    echo "On Midway"
    echo "midway bash /bin/bash null null null" > tc.data
elif [[ "$HOST" == *beagle* ]]; then
    echo "On Beagle"
    echo "beagle bash /bin/bash null null null" > tc.data
elif [[ "$HOST" == *mcs* ]]; then
    echo "On MCS"
    echo "mcs bash /bin/bash null null null" > tc.data
elif [[ "$HOST" == *uc3* ]]; then
    echo "On UC3"
    echo "uc3 bash /bin/bash null null null" > tc.data
else
    echo "On unidentified machine, using defaults"
    echo "local bash /bin/bash null null null" > tc.data
fi

if [[ -z $MIDWAY_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/{mid.USER}/$MIDWAY_USERNAME/" > tmp && mv tmp\
 sites.xml
fi
if [[ -z $UC3_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/{uc3.USER}/$UC3_USERNAME/" > tmp && mv tmp si\
tes.xml
fi
if [[ -z $BEAGLE_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/{beagle.USER}/$BEAGLE_USERNAME/" > tmp && mv \
tmp sites.xml
fi
if [[ -z $MCS_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/{mcs.USER}/$MCS_USERNAME/" > tmp && mv \
tmp sites.xml
fi

cat<<'EOF' > teragen_wrap.sh
#!/bin/bash

# By default with ARG1:100 and SLICESIZE=10000, this script will generate
# 10^6 records.
ARG1=1
[ ! -z $1 ] && ARG1=$1

FILE="input_$RANDOM.txt"
LOWERLIMIT=0
UPPERLIMIT=1000000 # 10^9
SLICESIZE=10000     # 10^4 records padded to 100B would result in 1MB file
#SLICESIZE=1000     # 10^3  If padded to 100B would result

shuf -i $LOWERLIMIT-$UPPERLIMIT -n $(($SLICESIZE*$ARG1)) | awk '{printf "%-99s\n", $0}'
exit 0
EOF

cat <<'EOF' > combiner.sh
#!/bin/bash

FILES=$*
SUM=0
COUNT=0

for file in $*
do
    RES=($(awk '{ sum += $1 } END { print sum,NR }' $file))
    echo "${RES[0]} ${RES[1]}"
    SUM=$(($SUM+${RES[0]}))
    COUNT=$(($COUNT+${RES[1]}))
done
echo "SUM  : $SUM"
echo "COUNT: $COUNT"
exit 0
EOF
