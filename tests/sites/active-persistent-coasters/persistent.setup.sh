#!/bin/bash

$(timeout 120 coaster-service -p 50562 -nosec) &
sleep 3
