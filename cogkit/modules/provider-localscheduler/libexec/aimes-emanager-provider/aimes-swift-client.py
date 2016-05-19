#!/usr/bin/env python
##############################################
# This code is deprecated
##############################################

# this code talks to a swift-aimes rest service and runs a simple workload.  The
# first argument is the service endpoint.

import radical.utils as ru

import os
import sys
import json
import time
import glob
import pprint
import requests
import argparse


from flufl.lock import Lock as FLock
from datetime   import datetime

# FIXME: this is expensive to do on every invokation
ru_logger = ru.get_logger('aimes.swift', header=False)

RUNDIRS        = glob.glob("run[0-9][0-9][0-9]")
RUNDIR         = sorted(RUNDIRS)[-1]
LOG            = "%s/aimes-swift.log" 
endpoint       = 'http://localhost:8090'
id_file        = '%s/ssid' % RUNDIR
lock_file      = '%s/flock' % RUNDIR

# ------------------------------------------------------------------------------
#
def get_session():

    flock = FLock(lock_file)
    with flock:

        if not os.path.exists(id_file):

            r = requests.put("%s/emgr/sessions/" % endpoint)
            ssid = r.json()['emgr_sid']
            ru_logger.debug("Session : %s", r.json())

            with open(id_file, 'w') as f:
                f.write(ssid)

        else:
            with open(id_file, 'r') as f:
                ssid = f.read()

    ru_logger.debug("session: %s", ssid)
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
def compose_compute_unit():

    task_desc  = sys.stdin.readlines()
    swift_tid  = None
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

        if (line.startswith("directory=")):
            swift_tid = line.split('/')[-1]

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
            ru_logger.debug('si: %s', stagein_item)

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
            ru_logger.debug('so: %s', stagein_item)

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
            ru_logger.debug("ignoring option : {0}".format(line.strip()))

        index += 1

    # Trim out all info besides the stagein and stageout sources.
    _stageins  = [x["source"] for x in stageins]
    _stageouts = [x["source"] for x in stageouts if x["source"] != 'wrapper.error' ]

    # we interpret executable + args, and plug things out of the hand of
    # _swiftwrap.staging.  
    ru_logger.debug('sis: %s', _stageins)
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


    ru_logger.info("EXEC      : %s", executable)
    ru_logger.info("ARGS      : %s", args)
    ru_logger.info("ARGS_PRE  : %s", args_pre)
    ru_logger.info("ARGS_POST : %s", args_post)
    ru_logger.info("STAGEINS  : %s", _stageins)
    ru_logger.info("STAGEOUTS : %s", _stageouts)
    ru_logger.info("WALLTIME  : %s", walltime)
    ru_logger.info("CORES     : %s", cores)
    ru_logger.info("MPI       : %s", mpi)

    jobdesc = {
               "swift_tid"      : swift_tid, 
               "executable"     : executable,
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

    ru_logger.debug("Jobdesc : %s" % pprint.pformat(jobdesc))
    return jobdesc


# ------------------------------------------------------------------------------
#
def submit_task(ssid):

    cud  = compose_compute_unit()
    data = {'td': json.dumps(cud)}

    ru_logger.debug("Submit : {0}".format(data))
    
    r = requests.put("%s/emgr/sessions/%s" % (endpoint, ssid), data)
    emgr_tid  = r.json()['emgr_tid']
    swift_tid = cud['swift_tid']

    ru_logger.debug('%s -> %s', swift_tid, emgr_tid)

    print "jobid=%s" % emgr_tid


# ------------------------------------------------------------------------------
#
state_mapping = {'New'                      : 'Q',
                 'Unscheduled'              : 'Q',
                 'Scheduling'               : 'Q',
                 'AllocatingPending'        : 'Q',
                 'Allocating'               : 'Q',
                 'PendingAgentInputStaging' : 'Q',
                 'AgentStagingInputPending' : 'R',
                 'PendingInputStaging'      : 'R',
                 'AgentStagingInput'        : 'R',
                 'StagingInput'             : 'R',
                 'PendingExecution'         : 'R',
                 'ExecutingPending'         : 'R',
                 'Executing'                : 'R',
                 'PendingAgentOutputStaging': 'R',
                 'AgentStagingOutputPending': 'R',
                 'AgentStagingOutput'       : 'R',
                 'PendingOutputStaging'     : 'R',
                 'StagingOutput'            : 'R',
                 'Done'                     : 'C',
                 'Canceled'                 : 'F',
                 'Failed'                   : 'F'}

def status_task(jobids, ssid):

    r = requests.get("%s/emgr/sessions/%s/%s" % (endpoint, ssid, ':'.join(jobids)))
    ru_logger.debug("status : %s", r.json())

    task_infos = r.json()['result']

  # import pprint
  # ru_logger.debug('result: %s', pprint.pformat(task_infos))

    for task in task_infos:
        jobid       = task['uid']
        emgr_state  = task['state']
        swift_state = state_mapping.get(emgr_state)

        ru_logger.debug('state  %s: %s [%s]', jobid, swift_state, emgr_state)
        
        if swift_state:
            print "%s %s" % (jobid, swift_state)
        else:
            print "%s UNKNOWN STATE %s" % (jobid, emgr_state)


# ------------------------------------------------------------------------------
#
def cancel_task(jobid, ssid):
    r = requests.delete("%s/emgr/sessions/%s/%s" % (endpoint, ssid, jobid))
    ru_logger.debug("cancel : %s", r.json())


# ------------------------------------------------------------------------------
#
def destroy_session(ssid):
    r = requests.delete("%s/emgr/sessions/%s" % (endpoint, ssid))
    ru_logger.debug('close session %s', ssid)


# ------------------------------------------------------------------------------
#
if __name__ == '__main__' :

    try:
        ru_logger.debug('aimes-swift %s', sys.argv)

        ssid = get_session()

        if sys.argv[0].endswith('submit.py'):
            submit_task(ssid)

        elif sys.argv[0].endswith('status.py'):
            status_task(sys.argv[1:], ssid)

        elif sys.argv[0].endswith('cancel.py'):
            cancel_task(sys.argv[1:], ssid)

        else:
            ru_logger.error("undefined handler for %s", sys.argv)
            exit(-1)

    except Exception as e:
        ru_logger.exception('caught exception')
        raise


# ------------------------------------------------------------------------------

