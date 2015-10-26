#!/usr/bin/env python

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

import sys, glob
import argparse
import logging
import os

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/gen-py')

from radical_interface import RadicalPilotInterface
from radical_interface.ttypes import *

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

import timeit

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
	return config


'''
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
'''

def init (conf_file):
	logging.debug("conf_file: " + str(conf_file))
	configs    = read_configs(conf_file)
	ec2_driver = "blah"
	return configs,ec2_driver

if __name__ == '__main__' :
	parser   = argparse.ArgumentParser()
	mu_group = parser.add_mutually_exclusive_group(required=True)
	mu_group.add_argument("-s", "--submit", default=None ,  help='Takes a config file. Submits the CMD_STRING in the configs for execution via Radical Pilots')
	mu_group.add_argument("-t", "--status", default=None ,  help='gets the status of the CMD_STRING in the configs for execution via Radical Pilots')
	mu_group.add_argument("-c", "--cancel", default=None ,  help='cancels the jobs with jobids')
	mu_group.add_argument("-x", "--terminate", default="TERMINATE" ,  help='terminates the server')
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

	#try:
	transport = TSocket.TSocket('localhost', 9090)
	# Buffering is critical. Raw sockets are very slow
	transport = TTransport.TBufferedTransport(transport)
	# Wrap in a protocol
	protocol = TBinaryProtocol.TBinaryProtocol(transport)
	# Create a client to use the protocol encoder
	client = RadicalPilotInterface.Client(protocol)
	# Connect!
	transport.open()

	if args.submit :
		#print configs
		configs, driver = init(args.submit)
		print client.submit_task(args.submit)

	elif args.status :
		#print configs
		print client.status_task(args.status)

	elif args.cancel :
		#print configs
		print client.cancel_task(args.cancel)

	elif args.terminate :
		client.server_die("All work done")

	else:
		sys.stderr.write("ERROR: Undefined args, cannot be handled")
		sys.stderr.write("ERROR: Exiting...")
		exit(-1)

	transport.close()

	#except Thrift.TException, tx:
	#	print '%s' % (tx.message)

	exit(0)


