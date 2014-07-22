#!/usr/bin/env python

import os
import sys
import random
import logging
import pprint
import argparse
import datetime
import time
#from __future__ import print_function

from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver
from libcloud.compute.base import NodeSize, NodeImage
from libcloud.compute.types import NodeState
import libcloud.compute.types

NODESTATES = { NodeState.RUNNING    : "RUNNING",
               NodeState.REBOOTING  : "REBOOTING",
               NodeState.TERMINATED : "TERMINATED",
               NodeState.STOPPED    : "STOPPED",
               NodeState.PENDING    : "PENDING",
               NodeState.UNKNOWN    : "UNKNOWN" }

timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d_%H:%M:%S');
logging.basicConfig(filename='cloud_ec2'+timestamp+'.log', level=logging.INFO)

WORKER_USERDATA='''#!/bin/bash
export JAVA=/usr/local/bin/jdk1.7.0_51/bin
export SWIFT=/usr/local/bin/swift-trunk/bin
export PATH=$JAVA:$SWIFT:$PATH
export WORKER_LOGGING_LEVEL=TRACE
mkdir -p /home/yadu/.globus/coasters
'''

def aws_create_security_group(driver, configs):
    group_name = configs["SECURITY_GROUP"]
    current    = driver.ex_list_security_groups()
    if group_name in current:
        logging.info("Security group: %s is already present", group_name)
    else:
        logging.info("Creating new security group: %s", group_name)
        res = driver.ex_create_security_group(name=group_name,description="Open all ports")
        if not driver.ex_authorize_security_group(group_name, 0, 65000, '0.0.0.0/0'):
            logging.info("Authorizing ports for security group failed")
        if not driver.ex_authorize_security_group(group_name, 0, 65000, '0.0.0.0/0', protocol='udp'):
            logging.info("Authorizing ports for security group failed")
        logging.debug("Security group: %s", str(res))

def check_keypair(driver, configs):
    if "AWS_KEYPAIR_NAME" in configs and "AWS_KEYPAIR_FILE" in configs:
        logging.debug("AWS_KEYPAIR_NAME : %s", configs['AWS_KEYPAIR_NAME'])
        logging.debug("AWS_KEYPAIR_FILE : %s", configs['AWS_KEYPAIR_FILE'])
        all_pairs = driver.list_key_pairs()
        for pair in all_pairs:
            if pair.name == configs['AWS_KEYPAIR_NAME']:
                logging.info("KEYPAIR exists, registered")
                return 0

        logging.info("KEYPAIR does not exist. Creating keypair")
        key_pair = driver.create_key_pair(name=configs['AWS_KEYPAIR_NAME'])
        f = open(configs['AWS_KEYPAIR_FILE'], 'w')
        f.write(str(key_pair.private_key))
        f.close()
        os.chmod(configs['AWS_KEYPAIR_FILE'], 0600)
        logging.info("KEYPAIR created")
    else:
        logging.error("AWS_KEYPAIR_NAME and/or AWS_KEYPAIR_FILE missing")
        logging.error("Cannot proceed without AWS_KEYPAIR_NAME and AWS_KEYPAIR_FILE")
        exit(-1)

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

def pretty_configs(configs):
    printer = pprint.PrettyPrinter(indent=4)
    printer.pprint(configs)

def read_configs(config_file):
    config = _read_conf(config_file)

    if 'AWS_CREDENTIALS_FILE' in config :
        config['AWS_CREDENTIALS_FILE'] =  os.path.expanduser(config['AWS_CREDENTIALS_FILE'])
        config['AWS_CREDENTIALS_FILE'] =  os.path.expandvars(config['AWS_CREDENTIALS_FILE'])

        cred_lines    =  open(config['AWS_CREDENTIALS_FILE']).readlines()
        cred_details  =  cred_lines[1].split(',')
        credentials   = { 'AWS_Username'   : cred_details[0],
                          'AWSAccessKeyId' : cred_details[1],
                          'AWSSecretKey'   : cred_details[2] }
        config.update(credentials)
    else:
        print "AWS_CREDENTIALS_FILE , Missing"
        print "ERROR: Cannot proceed without access to AWS_CREDENTIALS_FILE"
        exit(-1)

    if 'AWS_KEYPAIR_FILE' in config:
        config['AWS_KEYPAIR_FILE'] = os.path.expanduser(config['AWS_KEYPAIR_FILE'])
        config['AWS_KEYPAIR_FILE'] = os.path.expandvars(config['AWS_KEYPAIR_FILE'])
    return config

def node_status(driver, node_uuids):
    logging.info("Checking status of : %s", str(node_uuids))
    nodes = driver.list_nodes()
    for node in nodes:
        logging.info("INFO: Node status : %s",str(node))
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
                logging.warn("Node state unknown/invalid %s", NODESTATE[node.state])
                return -1
    return 0

def node_start(driver, configs, WORKER_STRING):
    # Setup userdata
    userdata   = WORKER_USERDATA + WORKER_STRING.lstrip('"').rstrip('"')
    logging.info("Worker userdata : %s", userdata)

    size       = NodeSize(id=configs['WORKER_MACHINE_TYPE'], name="swift_worker",
                          ram=None, disk=None, bandwidth=None, price=None, driver=driver)
    image      = NodeImage(id=configs['WORKER_IMAGE'], name=None, driver=driver)
    node       = driver.create_node(name="swift_worker",
                                    image=image,
                                    size=size,
                                    ex_keyname=configs['AWS_KEYPAIR_NAME'],
                                    ex_securitygroup=configs['SECURITY_GROUP'],
                                    ex_userdata=userdata )
    logging.info("Worker node started : %s", str(node))
    print 'jobid={0}'.format(node.uuid)

# node_names is a list
def node_terminate(driver, node_uuids):
    nodes          = driver.list_nodes()
    deleted_flag   = False
    for node in nodes:
        if node.uuid in node_uuids and node.state == NodeState.RUNNING :
            logging.info("Terminating node : %s", str(node))
            code = driver.destroy_node(node)
            deleted_flag = True
    return deleted_flag


def init_checks(driver, configs):
    aws_create_security_group(driver, configs)
    check_keypair(driver, configs)

def init(conf_file):
    configs    = read_configs(conf_file)
    #pretty_configs(configs)
    driver     = get_driver(Provider.EC2_US_WEST_OREGON) # was EC2
    ec2_driver = driver(configs['AWSAccessKeyId'], configs['AWSSecretKey'])
    return configs,ec2_driver

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

    parser.add_argument("-j", "--jobid", type=str, action='append')
    args   = parser.parse_args()

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

    exit(0)
