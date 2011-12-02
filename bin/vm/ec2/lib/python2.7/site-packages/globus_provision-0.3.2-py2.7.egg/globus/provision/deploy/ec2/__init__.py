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
The EC2 deployer

This deployer will create and manage hosts for a topology using Amazon EC2.
"""

from cPickle import load
from boto.exception import BotoClientError, EC2ResponseError
from globus.provision.common.utils import create_ec2_connection 
from globus.provision.common.ssh import SSH, SSHCommandFailureException
from globus.provision.common.threads import MultiThread, GPThread, SIGINTWatcher
import random
import time
import sys
import traceback
import os.path
from globus.provision.common import log
from globus.provision.core.deploy import BaseDeployer, VM, ConfigureThread, WaitThread,\
    DeploymentException
from globus.provision.core.topology import DeployData, EC2DeployData, Node

class EC2VM(VM):
    """
    Represents a VM running on EC2.
    
    See the documentation on globus.provision.core.deploy.VM for details
    on what the VM class is used for.
    """
        
    def __init__(self, ec2_instance):
        self.ec2_instance = ec2_instance
        
    def __str__(self):
        return self.ec2_instance.id

class Deployer(BaseDeployer):
    """
    The EC2 deployer.
    """
  
    def __init__(self, *args, **kwargs):
        BaseDeployer.__init__(self, *args, **kwargs)
        self.conn = None
        self.instances = None
        self.vols = []
        self.supports_create_tags = True
        self.has_gp_sg = False

    def set_instance(self, inst):
        self.instance = inst
        self.__connect()         
    
    def __connect(self):
        config = self.instance.config
        
        try:
            log.debug("Connecting to EC2...")
            ec2_server_hostname = config.get("ec2-server-hostname")
            ec2_server_port = config.get("ec2-server-port")
            ec2_server_path = config.get("ec2-server-path")
            
            if ec2_server_hostname != None:
                self.conn = create_ec2_connection(ec2_server_hostname,
                                                  ec2_server_path,
                                                  ec2_server_port) 
            else:
                self.conn = create_ec2_connection()
            
            if self.conn == None:
                raise DeploymentException, "AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables are not set."

            log.debug("Connected to EC2.")
        except BotoClientError, exc:
            raise DeploymentException, "Could not connect to EC2. %s" % exc.reason
        
    def __get_security_groups(self, topology, node):
        sgs = topology.get_deploy_data(node, "ec2", "security_groups")
        if sgs is None:
            sgs = []

        if len(sgs) == 0:
            if self.has_gp_sg:
                sgs = ["globus-provision"]
            else:
                gp_sg = self.conn.get_all_security_groups(filters={"group-name":"globus-provision"})
                if len(gp_sg) == 0:
                    gp_sg = self.conn.create_security_group('globus-provision', 'Security group for Globus Provision instances')
                
                    # Internal
                    gp_sg.authorize(src_group = gp_sg)

                    # SSH
                    gp_sg.authorize('tcp', 22, 22, '0.0.0.0/0')
                
                    # GridFTP
                    gp_sg.authorize('tcp', 2811, 2811, '0.0.0.0/0')
                    gp_sg.authorize('udp', 2811, 2811, '0.0.0.0/0')
                    gp_sg.authorize('tcp', 50000, 51000, '0.0.0.0/0')
                
                    # MyProxy
                    gp_sg.authorize('tcp', 7512, 7512, '0.0.0.0/0')

                    # Galaxy
                    gp_sg.authorize('tcp', 8080, 8080, '0.0.0.0/0')
        
                sgs = ["globus-provision"]
                self.has_gp_sg = True
        else:
            all_sgs = self.conn.get_all_security_groups()
            # TODO: Validate that the security groups are valid
        
        return sgs
    
    def allocate_vm(self, node):
        topology = self.instance.topology
        
        instance_type = topology.get_deploy_data(node, "ec2", "instance_type")
        ami = topology.get_deploy_data(node, "ec2", "ami")
        security_groups = self.__get_security_groups(topology, node)

        try:
            image = self.conn.get_image(ami)
        except EC2ResponseError, ec2err:
            if ec2err.error_code in ("InvalidAMIID.NotFound", "InvalidAMIID.Malformed"):
                raise DeploymentException, "AMI %s does not exist" % ami
            else:
                raise ec2err

        if image == None:
            # Workaround for this bug:
            # https://bugs.launchpad.net/eucalyptus/+bug/495670
            image = [i for i in self.conn.get_all_images() if i.id == ami]
            if len(image) == 0:
                raise DeploymentException, "AMI %s does not exist" % ami
            else:
                image = image[0]         
        
        user_data = """#cloud-config
manage_etc_hosts: true
"""        
        
        log.info(" |- Launching a %s instance for %s." % (instance_type, node.id))
        reservation = image.run(min_count=1, 
                                max_count=1,
                                instance_type=instance_type,
                                security_groups= security_groups,
                                user_data = user_data,
                                key_name=self.instance.config.get("ec2-keypair"),
                                placement = None)
        instance = reservation.instances[0]
        
        return EC2VM(instance)

    def resume_vm(self, node):
        ec2_instance_id = node.deploy_data.ec2.instance_id

        log.info(" |- Resuming instance %s for %s." % (ec2_instance_id, node.id))        
        started = self.conn.start_instances([ec2_instance_id])            
        log.info(" |- Resumed instance %s." % ",".join([i.id for i in started]))
        
        return EC2VM(started[0])

    def post_allocate(self, node, vm):
        ec2_instance = vm.ec2_instance
        
        if ec2_instance.private_ip_address != None:
            # A correct EC2 system should return this
            node.ip = ec2_instance.private_ip_address
        else:
            # Unfortunately, some EC2-ish systems won't return the private IP address
            # We fall back on the private_dns_name, which should still work
            # (plus, some EC2-ish systems actually set this to the IP address)
            node.ip = ec2_instance.private_dns_name

        node.hostname = ec2_instance.public_dns_name

        # TODO: The following won't work on EC2-ish systems behind a firewall.
        node.public_ip = ".".join(ec2_instance.public_dns_name.split(".")[0].split("-")[1:])

        if not node.has_property("deploy_data"):
            node.deploy_data = DeployData()
            node.deploy_data.ec2 = EC2DeployData()            

        node.deploy_data.ec2.instance_id = ec2_instance.id

        try:
            if self.supports_create_tags:
                self.conn.create_tags([ec2_instance.id], {"Name": "%s_%s" % (self.instance.id, node.id)})
        except:
            # Some EC2-ish systems don't support the create_tags call.
            # If it fails, we just silently ignore it, as it is not essential,
            # but make sure not to call it again, as EC2-ish systems will
            # timeout instead of immediately returning an error
            self.supports_create_tags = False


    def get_node_vm(self, nodes):
        ec2_instance_ids = [n.deploy_data.ec2.instance_id for n in nodes]
        reservations = self.conn.get_all_instances(ec2_instance_ids)
        node_vm = {}
        for r in reservations:
            instance = r.instances[0]
            node = [n for n in nodes if n.deploy_data.ec2.instance_id==instance.id][0]
            node_vm[node] = EC2VM(instance)
        return node_vm

    def stop_vms(self, nodes):
        ec2_instance_ids = [n.deploy_data.ec2.instance_id for n in nodes]
        log.info("Stopping EC2 instances %s." % ", ".join(ec2_instance_ids))
        stopped = self.conn.stop_instances(ec2_instance_ids)
        log.info("Stopped EC2 instances %s." % ", ".join([i.id for i in stopped]))

    def terminate_vms(self, nodes):
        ec2_instance_ids = [n.deploy_data.ec2.instance_id for n in nodes]
        log.info("Terminating EC2 instances %s." % ", ".join(ec2_instance_ids))
        terminated = self.conn.terminate_instances(ec2_instance_ids)
        log.info("Terminated EC2 instances %s." % ", ".join([i.id for i in terminated]))
        
    def wait_state(self, obj, state, interval = 2.0):
        jitter = random.uniform(0.0, 0.5)
        while True:
            time.sleep(interval + jitter)
            try:
                newstate = obj.update()
            except EC2ResponseError, ec2err:
                if ec2err.error_code == "InvalidInstanceID.NotFound":
                    # If the instance was just created, this is a transient error. 
                    # We just have to wait until the instance appears.
                    pass
                else:
                    raise ec2err            
            
            if newstate == state:
                return True
        # TODO: Check errors            
        
    def get_wait_thread_class(self):
        return self.NodeWaitThread

    def get_configure_thread_class(self):
        return self.NodeConfigureThread
            
    class NodeWaitThread(WaitThread):
        def __init__(self, multi, name, node, vm, deployer, state, depends = None):
            WaitThread.__init__(self, multi, name, node, vm, deployer, state, depends)
            self.ec2_instance = vm.ec2_instance
                        
        def wait(self):
            if self.state in (Node.STATE_RUNNING_UNCONFIGURED, Node.STATE_RESUMED_UNCONFIGURED):
                self.deployer.wait_state(self.ec2_instance, "running")
                log.info("Instance %s is running. Hostname: %s" % (self.ec2_instance.id, self.ec2_instance.public_dns_name))
            elif self.state == Node.STATE_STOPPED:
                self.deployer.wait_state(self.ec2_instance, "stopped")
            elif self.state == Node.STATE_TERMINATED:
                self.deployer.wait_state(self.ec2_instance, "terminated")
            
            
    class NodeConfigureThread(ConfigureThread):
        def __init__(self, multi, name, node, vm, deployer, depends = None, basic = True, chef = True):
            ConfigureThread.__init__(self, multi, name, node, vm, deployer, depends, basic, chef)
            self.ec2_instance = self.vm.ec2_instance
            
        def connect(self):
            keyfile = os.path.expanduser(self.config.get("ec2-keyfile"))
            ssh = self.ssh_connect(self.config.get("ec2-username"), self.ec2_instance.public_dns_name, keyfile)
            return ssh
        
        def pre_configure(self, ssh):
            node = self.node
            instance = self.ec2_instance
            
            log.info("Setting up instance %s. Hostname: %s" % (instance.id, instance.public_dns_name), node)
           
            try:
                ssh.run("ls -l /chef")
            except SSHCommandFailureException:
                #The image is not properly setup, so do all pre-configuration for globus-provision
                log.info("Image is not configured with Chef, so installing...")

                ssh.run("sudo chown -R %s /chef" % self.config.get("ec2-username"))
                ssh.scp_dir("%s" % self.chef_dir, "/chef")



                ssh.run("addgroup admin", exception_on_error = False)
                ssh.run("echo \"%s `hostname`\" | sudo tee -a /etc/hosts" % instance.private_ip_address)

                ssh.run("sudo apt-get install lsb-release wget")
                ssh.run("echo \"deb http://apt.opscode.com/ `lsb_release -cs` main\" | sudo tee /etc/apt/sources.list.d/opscode.list")
                ssh.run("wget -qO - http://apt.opscode.com/packages@opscode.com.gpg.key | sudo apt-key add -")
                ssh.run("sudo apt-get update")
                ssh.run("echo 'chef chef/chef_server_url string http://127.0.0.1:4000' | sudo debconf-set-selections")
                ssh.run("sudo apt-get -q=2 install chef")
        
                ssh.run("echo -e \"cookbook_path \\\"/chef/cookbooks\\\"\\nrole_path \\\"/chef/roles\\\"\" > /tmp/chef.conf")        
                ssh.run("echo '{ \"run_list\": \"recipe[provision::ec2]\", \"scratch_dir\": \"%s\" }' > /tmp/chef.json" % self.scratch_dir)

                ssh.run("sudo chef-solo -c /tmp/chef.conf -j /tmp/chef.json")    
        
                ssh.run("sudo update-rc.d -f nis remove")
                ssh.run("sudo update-rc.d -f condor remove")
                ssh.run("sudo update-rc.d -f chef-client remove")
       

                log.debug("Removing private data...")
         
                ssh.run("sudo find /root/.*history /home/*/.*history -exec rm -f {} \;", exception_on_error = False)


            
                
        def post_configure(self, ssh):
            pass
            
