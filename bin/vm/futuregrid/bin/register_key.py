#!/usr/bin/env python

import os
import sys
import base64
import boto
from boto.ec2.regioninfo import RegionInfo


try:
    access_id = os.environ['FUTUREGRID_IAAS_ACCESS_KEY']
    access_secret = os.environ['FUTUREGRID_IAAS_SECRET_KEY']
except:
    print "Make sure the envs FUTUREGRID_IAAS_ACCESS_KEY and FUTUREGRID_IAAS_SECRET_KEY are set"
    sys.exit(1)

hostfile = sys.argv[1]
keyname = "swiftkey"
if len(sys.argv) > 2:
    keylocation = sys.argv[2]
else:
    keylocation = os.path.expanduser("~/.ssh/id_rsa.pub")

keytext = open(keylocation).read()

f = open(hostfile, "r")
for line in f:
    host = line.strip()
    print "creating key %s on %s" % (keyname, host)

    region = RegionInfo(name="nimbus", endpoint=host)
    ec2conn = boto.connect_ec2(access_id, access_secret, region=region, port=8444)

#    keytext = base64.b64encode(keytext)
    ec2conn.import_key_pair(keyname, keytext)

sys.exit(0)
