#!/bin/sh

# Called by org.globus.swift.data.policy.Broadcast
# usage: cdm_broadcast.sh <MODE> <LOG> <DIR> <FILE> <DEST>
# copies DIR/FILE to DEST via MODE with logging to LOG

SWIFT_HOME=$( dirname $( dirname $0 ) )

# Broadcast the given files to the given location
# Input: bgp_broadcast_perform <location> <file>*
bgp_broadcast()
{
  LOCATION=$1
  shift
  WORK=( ${*} )

  IP=$( listip ${LOCATION} )

  if [[ ${#WORK[@]} > 3 ]]
    then
    SCRIPT=$( mktemp )
    {
      echo "#!/bin/sh"
      while [[ ${*} != "" ]]
       do
       FILE=$1
       DEST=$2
       shift 2
       echo "/bin.rd/f2cn ${FILE} ${DEST}"
      done
    } > ${SCRIPT}
    scp ${SCRIPT} ${IP}:${SCRIPT}
    ssh ${IP} ${SCRIPT}
  else
    while [[ ${*} != "" ]]
     do
     FILE=$1
     DEST=$2
     shift 2
     ssh_until_success 120 ${IP} /bin.rd/f2cn ${FILE} ${DEST}
    done
  fi
}

# Repeat command N times until success
ssh_until_success()
{
  N=$1
  shift
  for (( i=0 ; i < N ; i++ ))
   do
   ssh -o PasswordAuthentication=no ${*}
   if [[ $? == 0 ]];
     then
     break
   fi
   sleep 1
  done
  return 0
}

local_broadcast()
{
  ALLOCATION=$1 # Ignored (LOCAL_FILE)
  FILE=$2
  DEST=$3
  cp -v ${FILE} ${DEST}/${FILE}
}

MODE=$1
LOG=$2
shift 2

[[ ${LOG} != /dev/null ]] && set -x
{
  declare -p PWD LOG

  if [[ ${MODE} == "f2cn" ]]
    then
    BROADCAST="bgp_broadcast"
  elif [[ ${MODE} == "file" ]]
    then
    BROADCAST="local_broadcast"
  else
    echo "Unknown broadcast mode!"
    exit 1
  fi

  while [[ ${*} != "" ]]
  do
    L=$1 # -l
    shift
    ARGS=$1 # Location
    shift
    while true
    do
      if [[ $1 == "-l" || $1 == "" ]]
      then
        break
      fi
      ARGS="${ARGS} $1"
      shift
    done
    ${BROADCAST} ${ARGS}
  done
} >> ${LOG} 2>&1

exit 0
