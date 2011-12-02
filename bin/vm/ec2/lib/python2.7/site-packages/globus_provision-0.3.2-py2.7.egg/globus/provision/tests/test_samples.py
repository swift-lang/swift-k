from globus.provision.core.api import API
from globus.provision.tests.common import load_config_file, create_instances_dir,\
    remove_instances_dir, create_temp_config_file
from globus.provision.core.topology import Topology
import json
from globus.provision.cli.api import gp_instance_create, gp_instance_describe,\
    gp_instance_start, gp_instance_stop, gp_instance_list, gp_instance_update,\
    gp_instance_terminate
import os

# TODO: Assumes we're running from the source root directory. Make this location independent.
SAMPLE_DIR = "./samples/"
SAMPLES = ("simple-dummy.conf", "simple-ec2.conf", "single-gridftp-ec2.conf")


def test_samples_api():
    for s in SAMPLES:
        yield check_sample_api, SAMPLE_DIR + s

def test_samples_cli():
    for s in SAMPLES:
        yield check_sample_cli, SAMPLE_DIR + s


def check_sample_api(s):
    instances_dir = create_instances_dir()
    
    config_txt, topology_json = load_config_file(s, dummy = True)
    
    api = API(instances_dir)

    (status_code, message, inst_id) = api.instance_create(topology_json, config_txt)
    assert status_code == API.STATUS_SUCCESS
    check_instance_state(api, inst_id, Topology.STATE_NEW)
    
    (status_code, message, topologies_json) = api.instance_list(None)
    assert status_code == API.STATUS_SUCCESS
    insts = json.loads(topologies_json)
    assert len(insts) == 1
    assert insts[0]["id"] == inst_id
            
    (status_code, message, topologies_json) = api.instance_list([inst_id])
    assert status_code == API.STATUS_SUCCESS, message
    insts = json.loads(topologies_json)
    assert len(insts) == 1
    assert insts[0]["id"] == inst_id
    
    (status_code, message) = api.instance_start(inst_id, [], [])
    assert status_code == API.STATUS_SUCCESS, message
    check_instance_state(api, inst_id, Topology.STATE_RUNNING)

    (status_code, message) = api.instance_stop(inst_id)
    assert status_code == API.STATUS_SUCCESS, message
    check_instance_state(api, inst_id, Topology.STATE_STOPPED)

    (status_code, message) = api.instance_start(inst_id, [], [])
    assert status_code == API.STATUS_SUCCESS, message
    check_instance_state(api, inst_id, Topology.STATE_RUNNING)
    
    (status_code, message) = api.instance_update(inst_id, None, [], [])
    assert status_code == API.STATUS_SUCCESS, message
    check_instance_state(api, inst_id, Topology.STATE_RUNNING)    
    
    (status_code, message) = api.instance_terminate(inst_id)
    assert status_code == API.STATUS_SUCCESS, message
    check_instance_state(api, inst_id, Topology.STATE_TERMINATED)
    
    remove_instances_dir(instances_dir)

def check_sample_cli(s):
    instances_dir = create_instances_dir()
    temp_config_file = instances_dir + "/test.conf"
    create_temp_config_file(s, temp_config_file, dummy = True)

    common_args = ["GPCOMMAND", "-i", instances_dir]

    args = ["-c", temp_config_file]
    rc = gp_instance_create(common_args + args).run()
    assert rc == 0

    insts = [n for n in os.listdir(instances_dir) if n.startswith("gpi-")]
    assert len(insts) == 1
    inst_id = insts[0]
    print inst_id
    args = [inst_id]
    print common_args + args
    rc = gp_instance_describe(common_args + args).run()
    assert rc == 0    

    args = [inst_id, "-v"]
    rc = gp_instance_describe(common_args + args).run()
    assert rc == 0    

    args = [inst_id]
    rc = gp_instance_start(common_args + args, disable_sigintwatch = True).run()
    assert rc == 0    
    
    args = []
    rc = gp_instance_list(common_args + args).run()
    assert rc == 0       

    args = ["-v"]
    rc = gp_instance_list(common_args + args).run()
    assert rc == 0       

    args = [inst_id]
    rc = gp_instance_list(common_args + args).run()
    assert rc == 0       

    args = ["-v", inst_id]
    rc = gp_instance_list(common_args + args).run()
    assert rc == 0       
    return
    args = [inst_id]
    rc = gp_instance_stop(common_args + args, disable_sigintwatch = True).run()
    assert rc == 0       

    args = [inst_id]
    rc = gp_instance_start(common_args + args, disable_sigintwatch = True).run()
    assert rc == 0       

    args = [inst_id]
    rc = gp_instance_update(common_args + args, disable_sigintwatch = True).run()
    assert rc == 0     
    
    args = [inst_id]
    rc = gp_instance_terminate(common_args + args, disable_sigintwatch = True).run()
    assert rc == 0         
    
    remove_instances_dir(instances_dir)


def check_instance_state(api, inst_id, state):
    (status_code, message, topology_json) = api.instance(inst_id)
    assert status_code == API.STATUS_SUCCESS, message
    topology = Topology.from_json_string(topology_json)
    assert topology.state == state
    