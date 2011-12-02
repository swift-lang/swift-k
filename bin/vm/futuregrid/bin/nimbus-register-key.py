#!/usr/bin/env python

import os
import sys
import base64
import boto
from boto.ec2.regioninfo import RegionInfo
import urllib2


def add_key(ec2conn, keyname, keytext):
    pairs = ec2conn.get_all_key_pairs([keyname,])
    if not pairs:
        print "Adding a new key pair named %s" % (keyname)
        ec2conn.import_key_pair(keyname, keytext)
    else:
        print "Keyname %s already exists" % (keyname)

try:
    access_id = os.environ['FUTUREGRID_IAAS_ACCESS_KEY']
    access_secret = os.environ['FUTUREGRID_IAAS_SECRET_KEY']

    iaas_url = os.environ['FUTUREGRID_IAAS_URL']
except Exception, ex:
    print "Make sure the envs CLOUDINITD_* are set %s" % (str(ex))
    sys.exit(1)

url_parts = urllib2.urlparse.urlparse(iaas_url)
region = RegionInfo(name="nimbus", endpoint=url_parts.hostname)
ec2conn = boto.connect_ec2(access_id, access_secret, region=region, port=url_parts.port)

keyname = sys.argv[1]
keylocation = sys.argv[2]
keytext = open(keylocation).read()
keytext = base64.b64encode(keytext)
add_key(ec2conn, keyname, keytext)

sys.exit(0)

