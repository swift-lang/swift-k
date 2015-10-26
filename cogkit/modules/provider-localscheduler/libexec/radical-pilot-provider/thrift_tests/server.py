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
import logging
import random
import os

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + '/gen-py')

from radical_interface import RadicalPilotInterface
import radical.pilot as rp

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

def extract_configs(task_file):
	configs = {}
	index               = 0
	task_desc           = open(task_file, 'r').readlines()
	# Set some default pilot confs
	pilot_confs         = {'mongodb'  : 'mongodb://127.0.0.1:50055',
						   'userpass' : 'userpass',
						   'cleanup'  : False}

	while index < len(task_desc):
		if (task_desc[index].startswith("attr.radical-pilot.")):
			l   = len("attr.radical-pilot.")
			[key,value] = task_desc[index][l:].strip('\n').split("=")
			pilot_confs[key]= value

		index += 1

	configs['pilot_confs'] = pilot_confs
	print "Extracted configs : ", configs
	return configs

def pilot_state_cb (pilot, state) :
	print "[Callback]: ComputePilot '%s' state: %s." % (pilot.uid, state)
	if not pilot:
		return

	if  state == rp.FAILED :
		sys.exit (1)

def unit_state_cb (unit, state) :
	if not unit:
		return

	print "[Callback]: ComputeUnit  '%s' state: %s." % (unit.uid, state)

def rp_radical_init (configs):
	print "[rp_radical_init]"
	try:
		session = rp.Session(database_url=configs['pilot_confs']['mongodb'])
		c = rp.Context(configs['pilot_confs']['userpass'])
		session.add_context(c)
		print "Initializing Pilot Manager ..."
		pmgr = rp.PilotManager(session=session)
		pmgr.register_callback(pilot_state_cb)

		# Combine the ComputePilot, the ComputeUnits and a scheduler via
		# a UnitManager object.
		print "Initializing Unit Manager ..."
		umgr = rp.UnitManager (session=session,
							   scheduler=rp.SCHED_DIRECT_SUBMISSION)

		# Register our callback with the UnitManager. This callback will get
		# called every time any of the units managed by the UnitManager
		# change their state.
		umgr.register_callback(unit_state_cb)

		pdesc = rp.ComputePilotDescription ()
		pdesc.resource = configs['pilot_confs']['resource']
		pdesc.runtime  = int(configs['pilot_confs']['runtime'])
		pdesc.cores    = int(configs['pilot_confs']['cores'])
		pdesc.cleanup  = True if configs['pilot_confs']['cleanup'] in ["true", "True"] else False

		# submit the pilot.
		print "Submitting Compute Pilot to Pilot Manager ..."
		pilot = pmgr.submit_pilots(pdesc)

		# Add the created ComputePilot to the UnitManager.
		print "Registering Compute Pilot with Unit Manager ..."
		umgr.add_pilots(pilot)
		#session = "session_name"
		#pmgr    = "pmgr_foo"
		#umgr    = "umpr_blah"
		return [session, pmgr, umgr]

	except Exception as e:
		print "An error occurred: %s" % ((str(e)))
		sys.exit (-1)

def filepath_cleanup(filepath):
	fpath = filepath.strip('\n')
	if fpath.startswith('file://localhost/'):
		l = len('file://localhost/')
		fpath = fpath[l:]
	return fpath

def rp_compose_compute_unit(task_filename):

	task_desc = open(task_filename, 'r').readlines()
	index     = 0
	args      = []
	stageins  = []
	stageouts = []
	env_vars  = {}
	while index < len(task_desc):
		# We don't process directory options.
		if (task_desc[index].startswith("directory=")):
			l = len("directory=")

		elif (task_desc[index].startswith("env.")):
			l   = len("env.")
			[key,value] = task_desc[index][l:].strip('\n').split("=")
			env_vars[key] = value

		elif (task_desc[index].startswith("executable=")):
			l = len("executable=")
			executable = task_desc[index][l:].strip('\n')

		elif (task_desc[index].startswith("arg=")):
			l = len("arg=")
			args.append(task_desc[index][l:].strip('\n'))

		elif (task_desc[index].startswith("stagein.source=")):
			stagein_item = {}
 			l = len("stagein.source=")
			stagein_item['source'] = filepath_cleanup(task_desc[index][l:])
			index += 1
			if (task_desc[index].startswith("stagein.destination=")):
				l = len("stagein.destination=")
				stagein_item['destination'] = filepath_cleanup(task_desc[index][l:])
				index += 1
				if (task_desc[index].startswith("stagein.mode=")):
					l = len("stagein.mode=")
					# Ignore mode for now
					#stagein_item['destination'] = task_desc[index][l:].strip('\n')
					#index += 1
				else:
					index -= 1
			else:
				printf("[ERROR] Stagein source must have a destination")
			stageins.append(stagein_item)

		elif (task_desc[index].startswith("stageout.source=")):
			stageout_item = {}
 			l = len("stageout.source=")
			stageout_item['source'] = filepath_cleanup(task_desc[index][l:])
			index += 1
			if (task_desc[index].startswith("stageout.destination=")):
				l = len("stageout.destination=")
				stageout_item['destination'] = filepath_cleanup(task_desc[index][l:])
				index += 1
				if (task_desc[index].startswith("stageout.mode=")):
					l = len("stageout.mode=")
					# Ignore mode for now
					#stageout_item['destination'] = task_desc[index][l:].strip('\n')
					#index += 1
				else:
					index -= 1
			else:
				printf("[ERROR] Stageout source must have a destination")
			stageouts.append(stageout_item)

		else:
			logging.debug("ignoring option : {0}".format(task_desc[index].strip('\n')))

		index += 1

	logging.debug("ARGS      : {0}".format(args))
	logging.debug("EXEC      : {0}".format(executable))
	logging.debug("STAGEINS  : {0}".format(stageins))
	logging.debug("STAGEOUTS : {0}".format(stageouts))

	cudesc                = rp.ComputeUnitDescription()
	cudesc.environment    = env_vars
	cudesc.executable     = executable
	cudesc.arguments      = args
	cudesc.cores          = 1
	cudesc.input_staging  = stageins
	cudesc.output_staging = stageouts
	return [cudesc]

def rp_submit_task(unit_manager, task_filename):
	cu_desc = rp_compose_compute_unit(task_filename)
	c_unit  = unit_manager.submit_units(cu_desc)
	return c_unit


class RadicalPilotHandler:
	def __init__(self):
		self.session = 'NULL'
		self.pmgr    = 'NULL'
		self.umgr    = 'NULL'
		self.log     = {}
		self.configs = {}
		#self.rp_lock = threading.Lock()
		self.task_lookup = {}
		self.session = 'NULL'
		logging.debug("Init done")

	def submit_task(self, task_filename):
		print "[SUBMIT_TASK] :", task_filename

		# If self.configs is empty, this is the first task, which requires
		# radical pilots to be setup
		if not self.configs :
			logging.debug("[SUBMIT_TASK] : Starting radical.pilots")
			self.configs = extract_configs(task_filename)
			logging.debug("Extracting configs done")
			[self.session, self.pmgr, self.umgr] = rp_radical_init(self.configs)
			print [self.session, self.pmgr, self.umgr]
			logging.debug("done with radical_init")

		cu_list = rp_submit_task(self.umgr, task_filename)
		print cu_list[0]
		hash_id = str(len(self.task_lookup))
		self.task_lookup[hash_id] = cu_list[0]

		return hash_id

	def cancel_task(self, task_name):
		logging.debug("Cancelling task :" + task_name)
		return "Cancelled task"

	def status_task(self, task_name):

		radical_states = { 'PendingExecution' : 'Q',
						   'Scheduling'       : 'Q',
						   'Executing'        : 'R',
						   'Done'             : 'C',
						   'Failed'           : 'F' }

		if task_name not in self.task_lookup:
			return str(task_name) + " F -1 Task id not in the Radical Pilot lookup registry"

		state = self.task_lookup[task_name].state
		if state not in radical_states :
			logging.debug( "[DEBUG] task_name:" + task_name + " state: " +  state)
			return str(task_name) + " Q"

		logging.debug("[DEBUG] task_name:{0} state:{1}".format(task_name, state))
		return str(task_name) + " " + radical_states[state]

	def server_die(self, die_string):
		logging.debug("Server terminating. Received message: " + die_string)
		exit(0)

	def getStruct(self, key):
		print 'getStruct(%d)' % (key)
		return self.log[key]

	def zip(self):
		print 'zip()'


# Start logging
if ( len(sys.argv) < 2 ):
	print "[ERROR] Missing log_file argument"

logging.basicConfig(filename=sys.argv[1], level=logging.DEBUG)
logging.debug('Starting the server...')

handler   = RadicalPilotHandler()
processor = RadicalPilotInterface.Processor(handler)
transport = TSocket.TServerSocket(port=9090)
tfactory  = TTransport.TBufferedTransportFactory()
pfactory  = TBinaryProtocol.TBinaryProtocolFactory()

server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

# You could do one of these for a multithreaded server
#server = TServer.TThreadedServer(processor, transport, tfactory, pfactory)
#server = TServer.TThreadPoolServer(processor, transport, tfactory, pfactory)

server.serve()
logging.debug('done.')

