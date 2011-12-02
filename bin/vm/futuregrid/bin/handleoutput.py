#!/usr/bin/env python

import os
import sys
import simplejson as json

x = json.load(open(sys.argv[1], "r"))

levels = x['levels']
lvl1 = levels[0]
services = lvl1['services']

for svc in services:
    print svc['hostname']


