#!/usr/bin/env python

import os
import sys
from random import randrange
import logging
import pprint
import argparse
import datetime
import time
import json
#from __future__ import print_function

from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver
from libcloud.compute.base import NodeSize, NodeImage
from libcloud.compute.types import NodeState
import libcloud.compute.types


SWIFT_NETWORK="swift-network"
SWIFT_FIREWALL="swift-firewall"

NODESTATES = { NodeState.RUNNING    : "RUNNING",
               NodeState.REBOOTING  : "REBOOTING",
               NodeState.TERMINATED : "TERMINATED",
               NodeState.STOPPED    : "STOPPED",
               NodeState.PENDING    : "PENDING",
               NodeState.UNKNOWN    : "UNKNOWN" }

WORKER_USERDATA='''#!/bin/bash
export JAVA=/usr/local/bin/jdk1.7.0_51/bin
export SWIFT=/usr/local/bin/swift-trunk/bin
export PATH=$JAVA:$SWIFT:$PATH
export WORKER_LOGGING_LEVEL=TRACE
'''

def gce_create_network(driver, configs):
    #current    = driver.ex_list_security_groups()
    networks   = driver.ex_list_networks()
    swift_net  = [ net for net in networks if net.name == SWIFT_NETWORK ]

    # Create SWIFT_NETWORK if not present
    if not swift_net:
        swift_net = driver.ex_create_network(SWIFT_NETWORK, "10.240.0.0/16")

    # Create a new firewall if one isn't present
    rules      = [ {"IPProtocol": "tcp",
                    "ports": ["30000-60000"]},
                   {"IPProtocol": "tcp",
                    "ports": ["20-85"]},
                   {"IPProtocol": "udp",
                    "ports": ["30000-60000"]} ]

    firewalls  = [ fw for fw in driver.ex_list_firewalls() if fw.network.name == SWIFT_NETWORK ]
    if not firewalls:
        driver.ex_create_firewall(SWIFT_FIREWALL, rules, network=SWIFT_NETWORK, source_ranges=['0.0.0.0/0'])
    return

# Check if the source is a gs://*image.tar.gz
#
def gce_check_image(driver, configs):
    source = configs['gceworkerimage']
    target = ""
    if source.startswith('gs://') and source.endswith('.image.tar.gz'):
        img_id = source.rstrip('.image.tar.gz')[-5:]
        target = "swift-worker-" + img_id
    else:
        target = source

    images = driver.list_images()
    matches= [ img for img in images if img.name == target ]

    # Copy image if there were no matches
    if not matches :
        #print "Copying image from source to target"
        driver.ex_copy_image(target, source,
                             description="Swift worker image from"+source+" Timestamp: " + datetime.datetime.fromtimestamp(time.time()).strftime('%H_%M_%S'))

    configs['gceimageid'] = target
    return

def check_keypair(driver, configs):
    if "gcekeypairname" in configs and "gcekeypairfile" in configs:
        all_pairs = driver.list_key_pairs()
        for pair in all_pairs:
            if pair.name == configs['gcekeypairname']:
                return 0

        key_pair = driver.create_key_pair(name=configs['gcekeypairname'])
        f = open(configs['gcekeypairfile'], 'w')
        f.write(str(key_pair.private_key))
        #f.close()
        os.chmod(configs['gcekeypairfile'], 0600)
    else:
        sys.stderr.write("gcekeypairname and/or gcekeypairfile missing\n")
        sys.stderr.write("Cannot proceed without gcekeypairname and gcekeypairfile\n")
        exit(-1)

def node_status(driver, node_uuids):
    nodes = driver.list_nodes()
    for node in nodes:
        if node.uuid in node_uuids :
            if node.state == NodeState.RUNNING:
                print node.uuid, "R"
            elif node.state == NodeState.PENDING:
                print node.uuid, "Q"
            elif node.state == NodeState.TERMINATED:
                print node.uuid, "C"
            elif node.state == NodeState.STOPPED:
                print node.uuid, "C"
            elif node.state == NodeState.UNKNOWN:
                print node.uuid, "Q" # This state could be wrong
            else:
                sys.stderr.write("Node state unknown/invalid " + str(NODESTATE[node.state]))
                return -1
    return 0

def node_start(driver, configs, WORKER_STRING):

    userdata   = WORKER_USERDATA + WORKER_STRING.lstrip('"').rstrip('"')
    nodename   = "swift-worker-" + datetime.datetime.fromtimestamp(time.time()).strftime('%H-%M-%S') + "-" + str(randrange(10000))
    start_up   = "/tmp/" + nodename
    #print "Userdata : ", userdata
    f = open(start_up, 'w')
    f.write(userdata)
    f.close()
    #size       = NodeSize(id=configs['gceworkertype'], name="swift_worker",
    #                      ram=None, disk=None, bandwidth=None, price=None, driver=driver)
    #image      = NodeImage(id=configs['gceworkerimage'], name=None, driver=driver)

    #print "Starting image : ", configs['gceimageid'], " with nodename : " ,nodename
    '''
    node       = driver.deploy_node(nodename,                      # name
                                    configs['gceworkertype'],      # size str or GCENodeSize
                                    configs['gceimageid'],         # image str or GCENodeImage
                                    start_up,                      # This must be a filename
                                    location=configs['gcezone'],   # GCEZone for execution
                                    ex_network="default")
    '''
    node       = driver.create_node(nodename,
                                    configs['gceworkertype'],
                                    configs['gceimageid'],
                                    location=configs['gcezone'],
                                    ex_network=SWIFT_NETWORK, #ex_network="default",
                                    external_ip='ephemeral',
                                    ex_metadata={'startup-script' : userdata })

    print 'jobid={0}'.format(node.uuid)

# node_names is a list
def node_terminate(driver, node_uuids):
    logging.debug("node_terminate : " + str(node_uuids));
    nodes          = driver.list_nodes()
    deleted_flag   = False

    for node in nodes:
        logging.debug("Nodes: " + str(node.uuid));
        if node.uuid in node_uuids and node.state == NodeState.RUNNING :
            logging.debug("Killing node :" + str(datetime.datetime.now()) )
            code = driver.destroy_node(node, destroy_boot_disk=True)
            logging.debug("Return code  :" + str(code) + " at " + str(datetime.datetime.now()) )
            deleted_flag = True

    logging.debug("node_terminate : done!")
    return deleted_flag

def _read_conf(config_file):
    cfile = open(config_file, 'r').read()
    config = {}
    for line in cfile.split('\n'):
        # Checking if empty line or comment
        if line.startswith('#') or not line :
            continue
        temp = line.split('=')
        config[temp[0]] = temp[1].strip('\r')
    return config

def init_checks(driver, configs):
    gce_create_network(driver, configs)
    gce_check_image(driver, configs)

def init(conf_file):

    configs    = _read_conf(conf_file)
    driver     = get_driver(Provider.GCE)
    gce_driver = driver(configs['gceemailaccount'],
                        configs['gcekeypairfile'],
                        project=configs['gceprojectid'],
                        datacenter=configs['gcezone'])
    return configs,gce_driver

# Main driver section
#configs, driver = init()
#args = sys.argv[1:]
#print "All args : ",str(args)

if __name__ == '__main__' :
    parser   = argparse.ArgumentParser()
    mu_group = parser.add_mutually_exclusive_group(required=True)
    mu_group.add_argument("-s", "--submit", default=None ,  help='Takes a config file. Submits the CMD_STRING in the configs for execution on a cloud resource')
    mu_group.add_argument("-t", "--status", default=None ,  help='gets the status of the CMD_STRING in the configs for execution on a cloud resource')
    mu_group.add_argument("-c", "--cancel", default=None ,  help='cancels the jobs with jobids')
    parser.add_argument("-v", "--verbose", help="set level of verbosity, DEBUG, INFO, WARN")
    parser.add_argument("-l", "--logfile", help="set path to logfile, defaults to /dev/null")

    parser.add_argument("-j", "--jobid", type=str, action='append')
    args   = parser.parse_args()

    # Setting up logging
    if args.logfile:
        if not os.path.exists(os.path.dirname(args.logfile)):
            os.makedirs(os.path.dirname(args.logfile))
        logging.basicConfig(filename=args.logfile, level=logging.DEBUG)
        logging.debug("Logging started")
    else:
        logging.basicConfig(filename='/dev/null', level=logging.DEBUG)

    config_file  = ( args.status or args.submit or args.cancel )
    configs, driver = init(config_file)

    if args.submit :

        # Init checks confirm keypairs and security groups to allow for access to ports
        init_checks(driver, configs)
        node_start(driver, configs, configs['CMD_STRING'])

    elif args.status :

        node_status(driver, args.jobid )

    elif args.cancel :

        node_terminate(driver, args.jobid)

    else:

        sys.stderr.write("ERROR: Undefined args, cannot be handled")
        sys.stderr.write("ERROR: Exiting...")
        exit(-1)

    logging.debug("Logging end")
    exit(0)
