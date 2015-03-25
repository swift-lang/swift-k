#!/bin/bash

cat<<'EOF' > simulate
#! /bin/bash

printparams()
{
  printf "\nSimulation parameters:\n\n"
  echo bias=$bias
  echo biasfile=$biasfile
  echo initseed=$initseed
  echo log=$log
  echo paramfile=$paramfile
  echo range=$range
  echo scale=$scale
  echo seedfile=$seedfile
  echo timesteps=$timesteps
  echo output width=$width
}

log() {
  printf "\nCalled as: $0: $cmdargs\n\n"
  printf "Start time: "; /bin/date
  printf "Running as user: "; /usr/bin/id
  printf "Running on node: "; /bin/hostname
  printf "Node IP address: "; /bin/hostname -I
  printparams
  printf "\nEnvironment:\n\n"
  printenv | sort
}

addsims() {
  while read f1 ; do
    read -u 3 f2 
    if [ _$f1 = _ ]; then f1=$lastf1; fi
    if [ _$f2 = _ ]; then f2=$lastf2; fi
    printf "%${width}d\n" $(($f1+$f2)) 
    lastf1=$f1
    lastf2=$f2
  done <$1 3<$2
}

# set defaults

bias=0
biasfile=none
initseed=none
log=yes
paramfile=none
range=100
scale=1
seedfile=none
timesteps=0
nvalues=1
width=8
cmdargs="$*"

usage()
{
  echo $( basename $0 ): usage:
  cat <<END
    -b|--bias       offset bias: add this integer to all results
    -B|--biasfile   file of integer biases to add to results
    -l|--log        generate a log in stderr if not null
    -n|--nvalues    print this many values per simulation            
    -r|--range      range (limit) of generated results
    -s|--seed       use this integer [0..32767] as a seed
    -S|--seedfile   use this file (containing integer seeds [0..32767]) one per line
    -t|--timesteps  number of simulated "timesteps" in seconds (determines runtime)
    -x|--scale      scale the results by this integer
    -h|-?|?|--help  print this help
END
}

# FIXME: NOT YET IMPLEMENTED:
#    -p|--paramfile  take these parameters (in form param=value) from this file 
#    -p|--paramfile) paramfile=$2 ;;

while [ $# -gt 0 ]; do
  case $1 in
    -b|--bias)      bias=$2      ;;
    -B|--biasfile)  biasfile=$2  ;;
    -l|--log)       log=$2       ;;
    -n|--nvalues)   nvalues=$2   ;;       
    -s|--seed)      initseed=$2  ;;
    -S|--seedfile)  seedfile=$2  ;;
    -t|--timesteps) timesteps=$2 ;;
    -r|--range)     range=$2     ;;   
    -w|--width)     width=$2     ;;
    -x|--scale)     scale=$2     ;;
    -h|-?|--help|*) usage; exit  ;;
  esac
  shift 2
done
    
# process initial seed

if [ $initseed != none ]; then
  RANDOM=$initseed
fi

# process file of seeds

if [ $seedfile != none ]; then
  seed=0
  while read $seedfile s; do
    seed=$(($seed+$s))
  done <$seedfile
  RANDOM=$seed
fi

# run for some number of "timesteps"

sleep $timesteps

# emit N (nvalues) "simulation results" scaled and biased by argument values

simout=$(mktemp simout.XXXX)
for ((i=0;i<nvalues;i++)); do
  # value=$(( (($RANDOM)*(2**16))+$RANDOM ))
  value=$(( (($RANDOM)*(2**48)) + (($RANDOM)*(2**32)) + (($RANDOM)*(2**16)) + $RANDOM ))
  printf "%${width}d\n" $(( ($value%range)*scale+bias))
done  >$simout

# process file of biases

if [ $biasfile != none ]; then
  addsims $simout $biasfile
else
  cat $simout
fi
rm $simout

# log environmental data

if [ $log != off ]; then
  log 1>&2
fi
EOF

cat <<'EOF' > stats
#! /bin/sh

log() {
  printf "\nCalled as: $0: $cmdargs\n\n"
  printf "Start time: "; /bin/date
  printf "Running as user: "; /usr/bin/id
  printf "Running on node: "; /bin/hostname
  printf "Node IP address: "; /bin/hostname -I
  printf "\nEnvironment:\n\n"
  printenv | sort
}

awk '

{ sum += $1}

END { printf("%d\n",sum/NR) }
' $*
log 1>&2
EOF

