#!/bin/bash

COASTER_PORT=53001
export COG_OPTS=-Duser.home=/scratch/midway/yadunand/swiftwork
echo "Starting active coasters"
echo "Using swift-version : $(swift -version)"

coaster-service -p $COASTER_PORT -nosec | tee coaster_logs.txt
