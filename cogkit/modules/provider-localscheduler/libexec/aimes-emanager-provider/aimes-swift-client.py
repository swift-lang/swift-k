#!/usr/bin/env python
##############################################
# This code is deprecated
##############################################

# this code talks to a swift-aimes rest service and runs a simple workload.  The
# first argument is the service endpoint.
import os
import sys
import json
import time
import pprint
import requests
import logging
import argparse
import datetime
from datetime import datetime

def create_session():
    r = requests.put("%s/swift/sessions/" % ep)
    ssid = r.json()['emgr_sid']
    return ssid

def filepath_cleanup(filepath):
    fpath = filepath.strip('\n')
    if fpath.startswith('file://localhost/'):
        l = len('file://localhost/')
        fpath = fpath[l:]
    return fpath

def compose_compute_unit(task_filename):

    task_desc = open(task_filename, 'r').readlines()
    index     = 0
    args      = []
    stageins  = []
    stageouts = []
    env_vars  = {}
    walltime  = 0
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

        elif (task_desc[index].startswith("attr.maxwalltime=")):
            l = len("attr.maxwalltime=")
            d = datetime.strptime(task_desc[index][l:].strip('\n'), "%H:%M:%S")
            walltime = d.hour*3600 + d.minute*60 + d.second

        else:
            logging.debug("ignoring option : {0}".format(task_desc[index].strip('\n')))

        index += 1

    # Trim out all info besides the stagein and stageout sources.
    _stageins  = [x["source"] for x in stageins]
    _stageouts = [x["source"] for x in stageouts if x["source"] != 'wrapper.error' ]

    logging.error("ARGS      : {0}".format(args))
    logging.error("EXEC      : {0}".format(executable))
    logging.error("STAGEINS  : {0}".format(_stageins))
    logging.error("STAGEOUTS : {0}".format(_stageouts))
    logging.error("WALLTIME  : {0}".format(walltime))

    jobdesc = {"executable" : str(executable),
			   "arguments"  : args,
               "cores"      : int(env_vars.get("cores", 1)),
               "duration"   : walltime,
               "input_staging" : _stageins,
               "output_staging" : _stageouts
               }

    logging.error("Jobdesc : {0}".format(jobdesc))
    return jobdesc



def mock_job_desc(jobdesc):
    jobdesc = {"executable" : "/bin/sleep",
               "arguments"  : ["%d" % 2],
               "cores"      : 1}
    return jobdesc


def submit_task(jobdesc, ssid, ep):
    #cud  = mock_job_desc(jobdesc)
    cud  = compose_compute_unit(jobdesc)
    data = {'td': json.dumps(cud)}
    logging.debug("Submit : {0}".format(data))
    r = requests.put("%s/swift/sessions/%s" % (ep, ssid), data)
    #print r.json()
    return r.json()['emgr_tid']

state_mapping = {'New'                      : 'Q',
                 'Unscheduled'              : 'Q',
                 'Scheduling'               : 'Q',
                 'AllocatingPending'        : 'Q',
				 'Allocating'               : 'Q',
                 'PendingAgentInputStaging' : 'Q',
                 'AgentStagingInputPending' : 'Q',
                 'PendingInputStaging'      : 'Q',
                 'AgentStagingInput'        : 'Q',
                 'StagingInput'             : 'R',
                 'PendingExecution'         : 'Q',
                 'ExecutingPending'         : 'Q',
                 'Executing'                : 'R',
                 'PendingAgentOutputStaging': 'R',
                 'AgentStagingOutputPending': 'R',
                 'AgentStagingOutput'       : 'R',
                 'PendingOutputStaging'     : 'R',
                 'StagingOutput'            : 'R',
                 'Done'                     : 'C',
				 'Failed'                   : 'F'}

def status_task(jobid, ssid, ep):
    r = requests.get("%s/swift/sessions/%s/%s" % (ep, ssid, jobid))
    logging.debug("Status : {0}".format(r.json()))
    state = r.json()['result']['state']
    if state in state_mapping :
        return "{0} {1}".format(jobid, state_mapping[state])
    else:
        return "{0} UNKNOWN STATE {1}".format(jobid, state)

def cancel_task(jobid, ssid, ep):
    print "cancel_tasks : {0}".format(args)


if __name__ == '__main__' :
    parser   = argparse.ArgumentParser()
    mu_group = parser.add_mutually_exclusive_group(required=True)
    mu_group.add_argument("-i", "--init_session", action='store_true' ,  help='Takes a config file. Submits the CMD_STRING in the configs for execution via Radical Pilots')
    mu_group.add_argument("-s", "--submit", default=None ,  help='Takes a config file. Submits the CMD_STRING in the configs for execution via Radical Pilots')
    mu_group.add_argument("-t", "--status", default=None ,  help='gets the status of the CMD_STRING in the configs for execution via Radical Pilots')
    mu_group.add_argument("-c", "--cancel", default=None ,  help='cancels the jobs with jobids')
    parser.add_argument("-v", "--verbose", help="set level of verbosity, DEBUG, INFO, WARN")
    parser.add_argument("-l", "--logfile", help="set path to logfile, defaults to /dev/null")
    parser.add_argument("-e", "--endpoint", required=True, help="Endpoint for the aimes service")
    parser.add_argument("-z", "--session_id", help="Session ID")

    parser.add_argument("-j", "--jobid", type=str, action='append')
    args   = parser.parse_args()
    # Setting up logging
    if args.logfile:
        if not os.path.exists(os.path.dirname(args.logfile)):
            os.makedirs(os.path.dirname(args.logfile))

    logging.basicConfig(filename=args.logfile, level=logging.DEBUG)
    if not args.endpoint :
        logging.error("Missing endpoint. Cannot proceed");

    ep = args.endpoint
    logging.debug("Endpoint : {0}".format(ep));

    if not args.init_session and not args.session_id:
        logging.error("Missing session id. Failing")
        exit(-4)
    ssid = args.session_id

    if args.init_session:
        ssid = create_session()
        print ssid

    elif args.submit :
        print submit_task(args.submit, ssid, ep)

    elif args.status :
        print status_task(args.status, ssid, ep)

    elif args.cancel :
        print cancel_task(args.cancel, ssid, ep)

    else:
        sys.stderr.write("ERROR: Undefined args, cannot be handled")
        sys.stderr.write("ERROR: Exiting...")
        exit(-1)

    exit(0)


# create a session, and begin submitting tasks.  Then let some time expire so
# that the tasks get executed by the watcher
print ' ---------- create session'
ssid = create_session()

print ' ---------- list sessions'
list_sessions()
tids = list()

print ' ---------- submit tasks'
tids.append(add_task(ssid))
tids.append(add_task(ssid))
tids.append(add_task(ssid))

print ' ---------- sleep'
time.sleep(6)

# Now we do the same again, and this batch should get executed in some seconds,
# too.
print ' ---------- submit tasks'
tids.append(add_task(ssid))
tids.append(add_task(ssid))
tids.append(add_task(ssid))

while True:

    # we wait for all tasks to finish
    all_finished = True
    for tid in tids:
        if not check_task(ssid, tids[-1]):
            all_finished = False
            break

    if all_finished:
        break
    else:
        print ' ---------- sleep 3'
        time.sleep (3)

print ' ---------- all tasks are final'
print ' ---------- list sessions, dump this session'
list_sessions()
dump_session(ssid)

# print ' ---------- delete this session, list sessions'
# delete_session(ssid)
# list_sessions()
