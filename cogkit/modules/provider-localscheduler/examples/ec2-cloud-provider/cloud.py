#!/usr/bin/env python

import os
import errno
import sys
import random
import logging
import pprint
import argparse
import datetime
import time
#from __future__ import print_function

import imp

try:
    imp.find_module('libcloud')
except ImportError:
    sys.stderr.write("Python: Apache libcloud module not available, cannot proceed\n")
    exit(-1)

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

WORKER_USERDATA='''#!/bin/bash
export JAVA=/usr/local/bin/jdk1.7.0_51/bin
export SWIFT=/usr/local/bin/swift-trunk/bin
export PATH=$JAVA:$SWIFT:$PATH
export WORKER_LOGGING_LEVEL=TRACE
'''

NEW_LINE='''
'''

def aws_create_security_group(driver, configs):
    """ Creates security group if not present.
    Currently opens all tcp/udp ports in range 0, 65000 for all sources.
    args   : driver instance, configs dictionary
    returns: Nothing
    """
    group_name = configs["ec2securitygroup"]
    current    = driver.ex_list_security_groups()
    if group_name not in current:
        logging.debug("Security group absent, creating group" + str(configs["ec2securitygroup"]));
        res = driver.ex_create_security_group(name=group_name,description="Open all ports")
        if not driver.ex_authorize_security_group(group_name, 0, 65000, '0.0.0.0/0'):
            sys.stderr.write("Authorizing ports for security group failed \n")
        if not driver.ex_authorize_security_group(group_name, 0, 65000, '0.0.0.0/0', protocol='udp'):
            sys.stderr.write("Authorizing ports for security group failed \n")

def check_keypair(driver, configs):
    """ Checks if valid keypairs exist, if not creates them
    args   : driver instance, configs dictionary
    returns: Nothing
    """
    if "ec2keypairname" in configs and "ec2keypairfile" in configs:
        all_pairs = driver.list_key_pairs()
        for pair in all_pairs:
            if pair.name == configs['ec2keypairname']:
                return 0

        key_pair = driver.create_key_pair(name=configs['ec2keypairname'])
        f = open(configs['ec2keypairfile'], 'w')
        f.write(str(key_pair.private_key))
        f.close()
        os.chmod(configs['ec2keypairfile'], 0600)

    else:
        sys.stderr.write("ec2keypairname and/or ec2keypairfile missing\n")
        sys.stderr.write("Cannot proceed without ec2keypairname and ec2keypairfile\n")
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

    if 'ec2credentialsfile' in config :
        config['ec2credentialsfile'] =  os.path.expanduser(config['ec2credentialsfile'])
        config['ec2credentialsfile'] =  os.path.expandvars(config['ec2credentialsfile'])

        cred_lines    =  open(config['ec2credentialsfile']).readlines()
        cred_details  =  cred_lines[1].split(',')
        credentials   = { 'AWS_Username'   : cred_details[0],
                          'AWSAccessKeyId' : cred_details[1],
                          'AWSSecretKey'   : cred_details[2] }
        config.update(credentials)
    else:
        print "ec2credentialsfile , Missing"
        print "ERROR: Cannot proceed without access to ec2credentialsfile"
        exit(-1)

    return config

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

    cloudinit = ""
    if "ec2cloudinit" in configs:
        logging.info("ec2cloudinit from script : " + configs['ec2cloudinit'])
        cloudinit = open(configs['ec2cloudinit'],'r').read()

    userdata   = WORKER_USERDATA + cloudinit + NEW_LINE + WORKER_STRING.lstrip('"').rstrip('"')
    image      = NodeImage(id=configs['ec2workerimage'], name=None, driver=driver)
    sizes      = driver.list_sizes()
    size       = [ s for s in sizes if s.id == configs['ec2workertype'] ]
    if not size:
        logging.info("ec2workerimage not legal/valid : %s", configs['ec2workertype'])
        sys.stderr.write("ec2workerimage not legal/valid \n")
        exit(-1);
    node       = driver.create_node(name="swift_worker",
                                    image=image,
                                    size=size[0],
                                    ex_keyname=configs['ec2keypairname'],
                                    ex_securitygroup=configs['ec2securitygroup'],
                                    ex_userdata=userdata )
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
    logging.debug("conf_file: " + str(conf_file))
    configs    = read_configs(conf_file)

    # Setting defaults for optional configs
    if 'ec2securitygroup' not in configs :
        logging.info("ec2SecurityGroup not set: Defaulting to swift-security-group")
        configs['ec2securitygroup'] = "swift-security-group"

    if "ec2keypairname" not in configs:
        logging.info("ec2KeypairName not set: Defaulting to swift-keypaid")
        configs['ec2keypairname'] = "swift-keypair"

    # If $HOME/.ssh is not accessible check_keypair will throw errors
    if "ec2keypairfile" not in configs:
        keyfile = os.path.expandvars("$HOME/.ssh/" + configs['ec2keypairname'] + ".pem")
        logging.info("ec2keypairfile not set: Defaulting to " + keyfile)
        configs['ec2keypairfile'] = keyfile

    driver     = get_driver(Provider.EC2_US_WEST_OREGON) # was EC2
    ec2_driver = driver(configs['AWSAccessKeyId'], configs['AWSSecretKey'])
    return configs,ec2_driver

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

    exit(0)
