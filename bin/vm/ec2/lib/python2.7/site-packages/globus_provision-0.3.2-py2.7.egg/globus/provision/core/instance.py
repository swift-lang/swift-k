# -------------------------------------------------------------------------- #
# Copyright 2010-2011, University of Chicago                                 #
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License"); you may    #
# not use this file except in compliance with the License. You may obtain    #
# a copy of the License at                                                   #
#                                                                            #
# http://www.apache.org/licenses/LICENSE-2.0                                 #
#                                                                            #
# Unless required by applicable law or agreed to in writing, software        #
# distributed under the License is distributed on an "AS IS" BASIS,          #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   #
# See the License for the specific language governing permissions and        #
# limitations under the License.                                             #
# -------------------------------------------------------------------------- #

"""
Instance Management

This module is the single point of access to information about instances.
See the documentation in InstanceStore and Instance for more details.
 
"""

import os.path
import random
from globus.provision.core.config import GPConfig
from globus.provision.core.topology import Topology
from globus.provision.common.certs import CertificateGenerator
from globus.provision.common.persistence import ObjectValidationException

class InstanceException(Exception):
    """A simple exception class used for instance exceptions"""
    pass

class InstanceStore(object):
    """
    The instance database.
    
    Stores information on all the instances created by the user.
    
    Currently, it uses a simple filesystem-based model. There is a
    designated "instances directory" (default: ~/.globusprovision/instances/).
    When an instance is created, a directory with the instance's id is
    created in the instances directory. For example::
    
         ~/.globusprovision/instances/gpi-12345678/
         
    All files related to an instance, including the topology file are
    stored in that directory. Generated files (such as certificates, etc.)
    are also stored there.
    
    The rest of the code only accesses the instances through this class.
    So, it should be possible to eventually replace this with a more elaborate
    solution (e.g., storing the instance data in a database, etc.)    
    """
    
    def __init__(self, instances_dir):
        self.instances_dir = instances_dir
        
    def create_new_instance(self, topology_json, config_txt):
        created = False
        while not created:
            inst_id = "gpi-" + hex(random.randint(1,2**31-1))[2:].rjust(8,"0")
            inst_dir = "%s/%s" % (self.instances_dir, inst_id)
            if not os.path.exists(inst_dir):
                os.makedirs(inst_dir)
                created = True
                
        configf = open("%s/provision.conf" % inst_dir, "w")
        configf.write(config_txt)
        configf.close()

        # We don't do anything with it. Just use it to raise an exception
        # if there is anything wrong with the configuration file
        GPConfig("%s/provision.conf" % inst_dir)

        topology = Topology.from_json_string(topology_json)
        topology.set_property("id", inst_id)
        topology.set_property("state", Topology.STATE_NEW)
        topology.save("%s/topology.json" % inst_dir)
                                        
        inst = Instance(inst_id, inst_dir)
        
        return inst
    
    def get_instance(self, inst_id):
        inst_dir = "%s/%s" % (self.instances_dir, inst_id)

        if not os.path.exists(inst_dir):
            raise InstanceException("Instance %s does not exist" % inst_id)
        return Instance(inst_id, inst_dir)

    def get_instances(self, inst_ids = None):
        valid_instances = []
        invalid_instances = []
        
        for inst_id in self.__get_instance_ids():
            if inst_ids == None or (inst_ids != None and inst_id in inst_ids):
                try:
                    inst = Instance(inst_id, "%s/%s" % (self.instances_dir, inst_id))
                    valid_instances.append(inst)
                except Exception, e:
                    invalid_instances.append((inst_id,str(e)))
        
        return (valid_instances, invalid_instances)
    
    def __get_instance_ids(self):
        inst_ids = [i for i in os.listdir(self.instances_dir)]
        return inst_ids

class Instance(object):
    """
    A Globus Provision Instance
    
    This class represents a single instance. Right now, an instance is 
    the combination of a configuration file and a topology (both of which
    are provided when the instance is created).
    
    The configuration file contains all the information about
    the instance that will (arguably) not change during its lifetime.
    The topology contains the specification of the hosts, users, etc.
    that are going to be deployed, and that could change during the
    instance's lifetime.
    
    For example, the configuration file specifies what keypair to use
    when accessing EC2. Although this could conceivably change, it is
    not as likely as a change in the topology (e.g., adding a new host,
    changing the run list of a host, etc.)
    
    """
    
    # Relative to instance directory
    CERTS_DIR = "/certs"

    def __init__(self, inst_id, instance_dir):
        self.instance_dir = instance_dir
        self.id = inst_id
        self.config = GPConfig("%s/provision.conf" % instance_dir)
        self.topology = self.__load_topology()
        
    def __load_topology(self):
        topology_file = "%s/topology.json" % self.instance_dir
        f = open (topology_file, "r")
        json_string = f.read()
        topology = Topology.from_json_string(json_string)
        topology._json_file = topology_file
        f.close()   
        return topology     

    def update_topology(self, topology_json):
        try:
            topology_file = "%s/topology.json" % self.instance_dir        
            new_topology = Topology.from_json_string(topology_json)
            new_topology._json_file = topology_file
        except ObjectValidationException, ove:
            message = "Error in topology file: %s" % ove 
            return (False, message, None)

        try:
            topology_changes = self.topology.validate_update(new_topology)
        except ObjectValidationException, ove:
            message = "Could not update topology: %s" % ove 
            return (False, message, None)

        self.topology = new_topology
        self.topology.save()
        
        return (True, "Success", topology_changes)

    def gen_certificates(self, force_hosts = False, force_users = False, force_ca = False):
        certs_dir = self.instance_dir + self.CERTS_DIR
        if not os.path.exists(certs_dir):
            os.makedirs(certs_dir)  

        dn = self.config.get("ca-dn")
        if dn == None:
            dn = "O=Grid, OU=Globus Provision (generated)"

        certg = CertificateGenerator(dn)

        cert_files = []
        ca_cert_file = self.config.get("ca-cert")
        ca_cert_key = self.config.get("ca-key")
        
        if ca_cert_file != None:
            ca_cert, ca_key = certg.load_certificate(ca_cert_file, ca_cert_key)
        else:
            ca_cert, ca_key = certg.gen_selfsigned_ca_cert("Globus Provision CA")
        
        certg.set_ca(ca_cert, ca_key)

        h = "%x" % ca_cert.subject_name_hash()

        hash_file = open(certs_dir + "/ca_cert.hash", "w")
        hash_file.write(h)
        hash_file.close()   

        ca_cert_file = "%s/ca_cert.pem" % certs_dir
        ca_key_file = certs_dir + "/ca_key.pem"
        cert_files.append(ca_cert_file)
        cert_files.append(ca_key_file)
        certg.save_certificate(cert = ca_cert,
                              key = ca_key,
                              cert_file = ca_cert_file,
                              key_file = ca_key_file, 
                              force = force_ca)

        users = [u for u in self.topology.get_users() if u.certificate=="generated"]
        for user in users:        
            cert, key = certg.gen_user_cert(cn = user.id) 
            
            cert_file = "%s/%s_cert.pem" % (certs_dir, user.id)
            key_file = "%s/%s_key.pem" % (certs_dir, user.id)
            cert_files.append(cert_file)
            cert_files.append(key_file)    
            certg.save_certificate(cert = cert,
                                    key = key,
                                    cert_file = cert_file,
                                    key_file = key_file, 
                                    force = force_users)
        
        nodes = self.topology.get_nodes()
        for n in nodes:
            cert, key = certg.gen_host_cert(hostname = n.hostname) 
            
            filename = n.id
            
            cert_file = "%s/%s_cert.pem" % (certs_dir, filename)
            key_file = "%s/%s_key.pem" % (certs_dir, filename)
            cert_files.append(cert_file)
            cert_files.append(key_file)          
            certg.save_certificate(cert = cert,
                                   key = key,
                                   cert_file = cert_file,
                                   key_file = key_file, 
                                   force = force_hosts)        

        return cert_files  


                 
        


        
