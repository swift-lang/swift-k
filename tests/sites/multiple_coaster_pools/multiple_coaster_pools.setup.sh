#!/bin/bash

cp -v ${GROUP}/hostsnsleep.sh . || exit 1
sed -i s/\$\{env.USER\}/$USER/g swift.conf
exit 0
