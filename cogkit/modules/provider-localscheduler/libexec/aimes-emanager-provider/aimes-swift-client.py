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

from datetime import datetime

# ------------------------------------------------------------------------------
#
def create_session():
    r = requests.put("%s/emgr/sessions/" % ep)
    ssid = r.json()['emgr_sid']
    return ssid


# ------------------------------------------------------------------------------
#
def delete_session():
    r = requests.put("%s/emgr/sessions/" % ep)
    ssid = r.json()['emgr_sid']
    return ssid


# ------------------------------------------------------------------------------
#
def filepath_cleanup(filepath):
    fpath = filepath.strip()
    if fpath.startswith('file://localhost/'):
        l = len('file://localhost/')
        fpath = fpath[l:]
    return fpath


# ------------------------------------------------------------------------------
#
def compose_compute_unit(task_filename):

    os.system('cp %s /tmp/' % task_filename)
    task_desc  = open(task_filename, 'r').readlines()
    index      = 0
    cmd        = list()
    args       = list()
    args_pre   = list()
    args_post  = list()
    stageins   = list()
    stageouts  = list()
    env_vars   = dict()
    walltime   = 0
    cores      = 1
    stdin      = None
    stdout     = None
    stderr     = None
    mpi        = False

    while index < len(task_desc):

        line = task_desc[index].strip()

        # We don't process directory options.
        if (line.startswith("directory=")):
            l = len("directory=")

        elif (line.startswith("env.")):
            l   = len("env.")
            [key,value] = line[l:].strip().split("=")
            env_vars[key] = value

        elif (line.startswith("executable=")):
            l = len("executable=")
            executable = line[l:].strip()

        elif (line.startswith("arg=")):
            l = len("arg=")
            args.append(line[l:].strip())

        elif (line.startswith("stagein.source=")):
            stagein_item = {}
            l = len("stagein.source=")
            stagein_item['source'] = filepath_cleanup(line[l:])

            index += 1
            line   = task_desc[index]

            if (line.startswith("stagein.destination=")):
                l = len("stagein.destination=")
                stagein_item['destination'] = filepath_cleanup(line[l:])

                index += 1
                line   = task_desc[index]

                if (line.startswith("stagein.mode=")):
                    l = len("stagein.mode=")
                    # Ignore mode for now
                    # stagein_item['destination'] = line[l:].strip()
                    # index += 1
                else:
                    index -= 1
            else:
                printf("[ERROR] Stagein source must have a destination")

            stageins.append(stagein_item)
            logging.debug('si: %s', stagein_item)

        elif (line.startswith("stageout.source=")):
            stageout_item = {}
            l = len("stageout.source=")
            stageout_item['source'] = filepath_cleanup(line[l:])

            index += 1
            line   = task_desc[index]

            if (line.startswith("stageout.destination=")):
                l = len("stageout.destination=")
                stageout_item['destination'] = filepath_cleanup(line[l:])

                index += 1
                line   = task_desc[index]

                if (line.startswith("stageout.mode=")):
                    l = len("stageout.mode=")
                    # Ignore mode for now
                    #stageout_item['destination'] = line[l:].strip()
                    #index += 1
                else:
                    index -= 1
            else:
                printf("[ERROR] Stageout source must have a destination")
            stageouts.append(stageout_item)
            logging.debug('so: %s', stagein_item)

        elif (line.startswith("attr.maxwalltime=")):
            l = len("attr.maxwalltime=")
            d = datetime.strptime(line[l:].strip(), "%H:%M:%S")
            walltime = d.hour*3600 + d.minute*60 + d.second

        elif (line.startswith("attr.hostcount=")):
            cores = int(line.split('=',1)[1])

        elif (line.startswith("attr.jobtype=")):
            jobtype = line.split('=',1)[1]
            if jobtype.lower() == 'mpi':
                mpi = True

        else:
            logging.debug("ignoring option : {0}".format(line.strip()))

        index += 1

    # Trim out all info besides the stagein and stageout sources.
    _stageins  = [x["source"] for x in stageins]
    _stageouts = [x["source"] for x in stageouts if x["source"] != 'wrapper.error' ]

    # we interpret executable + args, and plug things out of the hand of
    # _swiftwrap.staging.  
    logging.debug('sis: %s', _stageins)
    if args[0] == '_swiftwrap.staging':

        libexec = None
        for si in _stageins:
            if '_swiftwrap.staging' in si:
                libexec = os.path.dirname(si)
                break
        assert(libexec), si
        _stageins.append('%s/_swiftwrap_pre'  % libexec)
        _stageins.append('%s/_swiftwrap_post' % libexec)


        # remove wrapper from args
        new_args = list()
        args     = [arg.strip() for arg in args[1:]]
        args.append('--end')
        for i in range(len(args)):
            if args[i] == '--end':
                break
            elif args[i] == '-e':  # executable
                if not args[i+1].startswith('-'):
                    executable = args[i+1]
                    i+=1
            elif args[i] == '-a':  # arguments
                while not args[i+1].startswith('-'):
                    new_args.append(args[i+1])
                    i+=1
            elif args[i] == '-out':  # stdout
                if not args[i+1].startswith('-'):
                    stdout = args[i+1]
                    i+=1
            elif args[i] == '-err':  # stderr
                if not args[i+1].startswith('-'):
                    stderr = args[i+1]
                    i+=1
            elif args[i] == '-i':  # stdin
                if not args[i+1].startswith('-'):
                    stdin = args[i+1]
                    i+=1
            elif args[i] == '-d':  # dirs to create
                args_pre.append(args[i])
                while not args[i+1].startswith('-'):
                    args_pre.append(args[i+1])
                    i+=1
            elif args[i] == '-if':  # input files
                args_pre.append(args[i])
                while not args[i+1].startswith('-'):
                    args_pre.append(args[i+1])
                    i+=1
            elif args[i] == '-of':  # output files
                args_post.append(args[i])
                while not args[i+1].startswith('-'):
                    args_post.append(args[i+1])
                    i+=1
            elif args[i] == '-cf':  # collect (ignore)
                while not args[i+1].startswith('-'):
                    i+=1
            elif args[i] == '-cdmfile':  # cdmfile (ignore)
                while not args[i+1].startswith('-'):
                    i+=1
            elif args[i] == '-status':  # statusmode (ignored)
                while not args[i+1].startswith('-'):
                    i+=1
        args = new_args


    logging.info("EXEC      : %s", executable)
    logging.info("ARGS      : %s", args)
    logging.info("ARGS_PRE  : %s", args_pre)
    logging.info("ARGS_POST : %s", args_post)
    logging.info("STAGEINS  : %s", _stageins)
    logging.info("STAGEOUTS : %s", _stageouts)
    logging.info("WALLTIME  : %s", walltime)
    logging.info("CORES     : %s", cores)
    logging.info("MPI       : %s", mpi)

    jobdesc = {"executable"     : executable,
               "arguments"      : args,
               "cores"          : cores,
               "mpi"            : mpi,
               "stdout"         : stdout,
               "stderr"         : stderr,
               "duration"       : walltime,
               "input_staging"  : _stageins,
               "output_staging" : _stageouts
               }

    if args_pre:
        jobdesc['pre_exec']  = ['bash _swiftwrap_pre "%s"'  % '" "'.join(args_pre)]
    if args_post:
        jobdesc['post_exec'] = ['bash _swiftwrap_post "%s"' % '" "'.join(args_post)]

    logging.debug("Jobdesc : %s" % pprint.pformat(jobdesc))
    return jobdesc


# ------------------------------------------------------------------------------
#
def mock_job_desc(jobdesc):
    jobdesc = {"executable" : "/bin/sleep",
               "arguments"  : ["2"],
               "cores"      : 1}
    return jobdesc


# ------------------------------------------------------------------------------
#
def submit_task(jobdesc, ssid, ep):

   #cud  = mock_job_desc(jobdesc)
    cud  = compose_compute_unit(jobdesc)
    data = {'td': json.dumps(cud)}

    logging.debug("Submit : {0}".format(data))
    
    r = requests.put("%s/emgr/sessions/%s" % (ep, ssid), data)
    return r.json()['emgr_tid']


# ------------------------------------------------------------------------------
#
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

    r = requests.get("%s/emgr/sessions/%s/%s" % (ep, ssid, jobid))
    logging.debug("Status : {0}".format(r.json()))
    state = r.json()['result']['state']
    
    if state in state_mapping :
        return "{0} {1}".format(jobid, state_mapping[state])
    else:
        return "{0} UNKNOWN STATE {1}".format(jobid, state)


# ------------------------------------------------------------------------------
#
def cancel_task(jobid, ssid, ep):
    r = requests.delete("%s/emgr/sessions/%s/%s" % (ep, ssid, jobid))
    print "cancel_tasks : %s : %s" % (jobid, r.json())


# ------------------------------------------------------------------------------
#
def destroy_session(ssid, ep):
    r = requests.delete("%s/emgr/sessions/%s" % (ep, ssid))
    logging.debug("Close : {0}".format(r.json()))


# ------------------------------------------------------------------------------
#
if __name__ == '__main__' :

    parser   = argparse.ArgumentParser()
    mu_group = parser.add_mutually_exclusive_group(required=True)
    mu_group.add_argument("-i", "--init",      action='store_true')
    mu_group.add_argument("-s", "--submit",    default=None,      )
    mu_group.add_argument("-t", "--status",    default=None,      )
    mu_group.add_argument("-c", "--cancel",    default=None,      )
    mu_group.add_argument("-d", "--destroy",   default=None,      )
    parser.add_argument(  "-v", "--verbose",                      )
    parser.add_argument(  "-l", "--logfile",                      )
    parser.add_argument(  "-z", "--session_id",                   )
    parser.add_argument(  "-e", "--endpoint",  required=True,     )

    args = parser.parse_args()

    # Setting up logging
    if args.logfile:
        logdir = os.path.dirname(args.logfile)
        if not os.path.exists(logdir):
            os.makedirs(logdir)
        logging.basicConfig(filename=args.logfile, level=logging.DEBUG)

    if not args.endpoint:
        logging.error("Missing endpoint. Cannot proceed");
        sys.exit(-1)

    ep = args.endpoint
    logging.debug("Endpoint : {0}".format(ep));

    if args.init:
        ssid = create_session()
        print ssid
        sys.exit(0)

    if not args.session_id:
        logging.error("Missing session id. Failing")
        sys.exit(-4)
    ssid = args.session_id

    if args.submit:
        print submit_task(args.submit, ssid, ep)
        sys.exit(0)

    if args.status:
        print status_task(args.status, ssid, ep)
        sys.exit(0)

    if args.cancel:
        print cancel_task(args.cancel, ssid, ep)
        sys.exit(0)

    if args.destroy:
        print destroy_task(args.destroy, ssid, ep)
        sys.exit(0)

    logging.error("Undefined args, cannot be handled")
    exit(-1)


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

print ' ---------- delete this session, list sessions'
destroy_session(ssid)
list_sessions()

