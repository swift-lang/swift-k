#!/bin/bash


# Set following config options:
cat <<EOF > coaster-service.conf
# Set ipaddr of the headnode
IPADDR=128.135.112.73
WORKER_USERNAME="yadunandb"
WORKER_HOSTS="communicado.ci.uchicago.edu bridled.ci.uchicago.edu"
WORKER_CONCURRENCY=2
WORKER_MODE=ssh
SSH_TUNNELING="no"
WORKER_LOGGING_LEVEL="DEBUG"
WORKER_LOG_DIR="/home/yadunandb/workers/"
WORKER_LOCATION="/home/yadunandb/workers/"
JOBSPERNODE=2
EOF


start-coaster-service

