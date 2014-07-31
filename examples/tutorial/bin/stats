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
