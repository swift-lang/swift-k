#!/bin/bash

jobspernode=$(( $RANDOM % 7 + 1 ))
LOOPS=$(( $(($RANDOM%4 + 1)) * 2 ))

HOST=$(hostname -f)

ARGS_FILE=${0%.setup.sh}.args

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
elif [[ "$HOST" == blogin*lcrc* ]]; then
    echo "On Blues at LCRC "
    echo "blues bash /bin/bash null null null" > tc.data
elif [[ "$HOST" == flogin*lcrc* ]]; then
    echo "On Fusion at LCRC "
    echo "fusion bash /bin/bash null null null" > tc.data
else
    echo "On unidentified machine, using defaults"
    echo "local bash /bin/bash null null null" > tc.data
fi

echo "-loops=$(( $jobspernode * $LOOPS ))" > $ARGS_FILE
cat sites.xml  | sed "s/JOBSPERNODE/$jobspernode/" > tmp && mv tmp sites.xml

cp sites.xml sites.xml.before
if [[ -z $MIDWAY_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/MIDWAY_USERNAME/$MIDWAY_USERNAME/" > tmp && mv tmp\
 sites.xml
fi
if [[ -z $UC3_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/UC3_USERNAME/$UC3_USERNAME/" > tmp && mv tmp si\
tes.xml
fi
if [[ -z $BEAGLE_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/BEAGLE_USERNAME/$BEAGLE_USERNAME/" > tmp && mv \
tmp sites.xml
fi
if [[ -z $MCS_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/MCS_USERNAME/$MCS_USERNAME/" > tmp && mv \
tmp sites.xml
fi
if [[ -z $BLUES_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/BLUES_USERNAME/$BLUES_USERNAME/" > tmp && mv \
tmp sites.xml
fi
if [[ -z $FUSION_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat sites.xml  | sed "s/FUSION_USERNAME/$FUSION_USERNAME/" > tmp && mv \
tmp sites.xml
fi
cp sites.xml sites.xml.after

cat<<'EOF' > count_jobs.sh
#!/bin/bash

SELF="count_jobs.sh"
SLEEPTIME=60


BEFORE=$(($RANDOM%$SLEEPTIME))
AFTER=$(($SLEEPTIME-$BEFORE))

sleep $BEFORE
echo  "NODE   $(hostname -f)"
ACTIVE=`ps -u $USER | grep $SELF | wc -l`
ps -u $USER
echo "ps -u $USER | grep  | wc -l"
echo  "ACTIVE $ACTIVE"
echo  "SPLIT $BEFORE:$AFTER"
sleep $AFTER
EOF

