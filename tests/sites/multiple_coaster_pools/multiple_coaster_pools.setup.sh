#!/bin/bash

cp -v ${GROUP}/hostsnsleep.sh . || exit 1
sed -i s/\{env.USER\}/$USER/g sites.xml
exit 0
