from globus.provision.core.config import SimpleTopologyConfig
import tempfile
import shutil

def load_config_file(f, dummy):
    configf = open(f)
    config_txt = configf.read()
    if dummy:
        config_txt = config_txt.replace("deploy: ec2", "deploy: dummy")
    configf.close()

    topology_file = f
    conf = SimpleTopologyConfig(topology_file)
    topology = conf.to_topology()
    topology_json = topology.to_json_string()   
    
    return config_txt, topology_json

def create_temp_config_file(f, tempf, dummy):
    configf = open(f)
    config_txt = configf.read()
    if dummy:
        config_txt = config_txt.replace("deploy: ec2", "deploy: dummy")
    configf.close()
    
    configf = open(tempf, "w")
    configf.write(config_txt)
    configf.close()

def create_instances_dir():
    return tempfile.mkdtemp(prefix="gptesttmp")

def remove_instances_dir(d):
    shutil.rmtree(d)