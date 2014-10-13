#!/bin/bash

usage()
{
    echo $0: usage:
    cat <<EOF
    -c|--count)        The number of fibonacci numbers to generate (default = 5)
    -s|--sleep)        The delay between generating each number (default = none)
    -p|--checkpoint)   A checkpoint file which is the output from a previous fibonacci run
    -e|--end)          Stop when fibonacci number is greater that the value specified here
    -f|--endfile)      output file
    -x|--failrate)     Failure rate as a probability (0.0-1.0), default 0.0
    -h|-?|--help|*)    Prints this help message

EOF
}

FAILRATE=0.0
ENDFILE=/dev/stdout
while [ $# -gt 0 ]; do
  case $1 in
    -c|--count)        COUNT=$2        ;;
    -s|--sleep)        SLEEP=$2        ;;
    -p|--checkpoint)   CHECKPOINT=$2   ;;
    -e|--end)          ENDCOND=$2      ;;
    -f|--endfile)      ENDFILE=$2      ;;
    -x|--failrate)     FAILRATE=$2     ;;
    -h|-?|--help|*) usage; exit  ;;
  esac
  shift 2
done

# If no count is defined set COUNT to 5
if [ -z $COUNT ] || [ "$COUNT" == "" ]
then
    COUNT=5
fi

export BC_LINE_LENGTH=0

fibonacci()
{
    COUNT=$1
    A=$2
    B=$3
    FAIL=$(echo "($RANDOM%100) < ($FAILRATE*100)" | bc )
    if [[ "$FAIL" == "1" ]]
    then
        echo "Failing" 1>&2
        exit 5;
    fi
    for i in $(seq 1 1 $COUNT)
    do
        C=$(echo "$A + $B" | bc)
        [ ! -z $SLEEP ] && sleep $SLEEP
        echo $C
        A=$B
        B=$C
        if [ ! -z "$ENDCOND" ]
        then
            if [[ $B -gt $ENDCOND ]]
            then
                echo "done" > $ENDFILE
            else
                echo "$B $ENDCOND not satisfied" > $ENDFILE
            fi
        fi
    done
}


# Checkpoint was not provided at all, Starting case
if [ "$CHECKPOINT" == "" ]
then
    echo "Starting from initial point since no checkpoint was provided" 1>&2
    echo "0"
    echo "1"
    fibonacci $(($COUNT-2)) 0 1

elif [ ! -s "$CHECKPOINT" ]
then
    echo "Starting from initial point since checkpoint was empty" 1>&2
    echo "0"
    echo "1"
    fibonacci $(($COUNT-2)) 0 1

# Checkpoint given
elif [ -f "$CHECKPOINT" ]
then
    echo "Checkpoint file present" 1>&2
    LAST=($(tail -n 2 $CHECKPOINT))
    fibonacci $COUNT ${LAST[0]} ${LAST[1]}
fi


