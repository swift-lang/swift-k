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
import json

"""
Commands that directly invoke the API. Most of these commands are a one-to-one
mapping to the API, although some some (like gp-instance-add-host) simply provide 
a more convenient interface on top of instance_update().
"""

import time
import sys

from colorama import Fore, Style

import globus.provision.common.defaults as defaults

from globus.provision.cli import Command
from globus.provision.core.api import API
from globus.provision.common.utils import parse_extra_files_files
from globus.provision.common.threads import SIGINTWatcher
from globus.provision.core.topology import Topology, Node, User
from globus.provision.core.config import SimpleTopologyConfig
from globus.provision.common.config import ConfigException


def gp_instance_create_func():
    return gp_instance_create(sys.argv).run()
        
class gp_instance_create(Command):
    """
    Creates a new Globus Provision instance.
    """
    
    name = "gp-instance-create"

    def __init__(self, argv):
        Command.__init__(self, argv, disable_sigintwatch=True)
        
        self.optparser.add_option("-c", "--conf", 
                                  action="store", type="string", dest="conf", 
                                  default = defaults.CONFIG_FILE,
                                  help = "Configuration file.")
        
        self.optparser.add_option("-t", "--topology", 
                                  action="store", type="string", dest="topology",
                                  help = "Topology file. Can be either a simple topology file (with extension .conf) "
                                         "or a topology JSON file (with extension .json).")        
                
    def run(self):    
        self.parse_options()

        if self.opt.conf is None:
            print "You must specify a configuration file using the -c/--conf option."
            return 1

        self._check_exists_file(self.opt.conf)

        if self.opt.topology is None:
            topology_file = self.opt.conf
        else:
            self._check_exists_file(self.opt.topology)
            topology_file = self.opt.topology
        
        if topology_file.endswith(".json"):
            jsonfile = open(topology_file)
            topology_json = jsonfile.read()
            jsonfile.close()
        elif topology_file.endswith(".conf"):
            try:
                conf = SimpleTopologyConfig(topology_file)
                topology = conf.to_topology()
                topology_json = topology.to_json_string()
            except ConfigException, cfge:
                self._print_error("Error in topology configuration file.", cfge)
                return 1         
        else:   
            self._print_error("Unrecognized topology file format.", "File must be either a JSON (.json) or configuration (.conf) file.")
            return 1         

        configf = open(self.opt.conf)
        config_txt = configf.read()
        configf.close()

        api = API(self.opt.dir)
        (status_code, message, inst_id) = api.instance_create(topology_json, config_txt)

        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not create instance.", message)
            return 1
        else:
            print "Created new instance:",
            print Fore.WHITE + Style.BRIGHT + inst_id
            self._set_last_gpi(inst_id)
            return 0
            
def gp_instance_describe_func():
    return gp_instance_describe(sys.argv).run()            
        
class gp_instance_describe(Command):
    """
    Describes a Globus Provision instance, providing information on the state of the instance,
    and of the individual hosts (including their hostnames and IPs, if the instance is running). 
    Running in verbose mode (with the ``-v`` option)
    will print out the raw JSON representation of the instance's topology.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-describe --verbose gpi-12345678
        
    """
    
    name = "gp-instance-describe"
    
    def __init__(self, argv):
        Command.__init__(self, argv, disable_sigintwatch=True)
                
    def run(self):    
        self.parse_options()
        
        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
         
        inst_id = self.args[1]
        
        api = API(self.opt.dir)
        (status_code, message, topology_json) = api.instance(inst_id)
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not access instance.", message)
            return 1 
        else:
            self._set_last_gpi(inst_id)
            
            if self.opt.verbose or self.opt.debug:
                print topology_json
            else:
                topology = Topology.from_json_string(topology_json)
                reset = Fore.RESET + Style.RESET_ALL
                print Fore.WHITE + Style.BRIGHT + inst_id + reset + ": " + self._colorize_topology_state(topology.state)
                print
                for domain in topology.domains.values():
                    print "Domain " + Fore.CYAN + "'%s'" % domain.id
                    
                    # Find "column" widths and get values while we're at it
                    node_width = state_width = hostname_width = ip_width = 0
                    rows = []
                    for node in domain.get_nodes():
                        if len(node.id) > node_width:
                            node_width = len(node.id)                        
                        
                        if node.has_property("state"):
                            state = node.state
                        else:
                            state = Node.STATE_NEW                            
                        state_str = Node.state_str[state]

                        if len(state_str) > state_width:
                            state_width = len(state_str)

                        if node.has_property("hostname"):
                            hostname = node.hostname
                        else:
                            hostname = ""                            

                        if len(hostname) > hostname_width:
                            hostname_width = len(hostname)

                        if node.has_property("ip"):
                            ip = node.ip
                        else:
                            ip = "" 

                        if len(ip) > ip_width:
                            ip_width = len(ip)    
                            
                        rows.append((node.id, state, state_str, hostname, ip))                          
                                                
                    for (node_id, state, state_str, hostname, ip) in rows:
                        node_id_pad = self._pad(node_id, Fore.WHITE + Style.BRIGHT + node_id + Fore.RESET + Style.RESET_ALL, node_width + 2) 
                        state_pad = self._pad(state_str, self._colorize_node_state(state), state_width + 2) 
                        hostname_pad   = self._pad(hostname, "", hostname_width + 2)
                        ip_pad = self._pad(ip, "", ip_width)
                        print "    " + node_id_pad + state_pad + hostname_pad + ip_pad
                    print
            
            return 0
                    
def gp_instance_start_func():
    return gp_instance_start(sys.argv).run()  

class gp_instance_start(Command):
    """
    Starts a Globus Provision instance. If the instance was previous stopped, ``gp-instance-start``
    will resume it.
    
    See :ref:`sec_test_chef` for details on how to use the ``--extra-files`` option.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-start --extra-files foo.txt gpi-12345678
        
    """
        
    name = "gp-instance-start"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)

        self.optparser.add_option("-x", "--extra-files", 
                                  action="store", type="string", dest="extra_files", 
                                  help = "File with list of files to upload to each host before configuring the instance.")

        self.optparser.add_option("-r", "--run", 
                                  action="store", type="string", dest="run", 
                                  help = "File with list of commands to run on each host after configuring the instance.")
                        
    def run(self):        
        t_start = time.time()        
        self.parse_options()
        
        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
        
        inst_id = self.args[1]
            
        if self.opt.extra_files != None:
            self._check_exists_file(self.opt.extra_files)
            extra_files = parse_extra_files_files(self.opt.extra_files)
        else:
            extra_files = []

        if self.opt.run != None:
            self._check_exists_file(self.opt.run)
            run_cmds = [l.strip() for l in open(self.opt.run).readlines()]
        else:
            run_cmds = []

        print "Starting instance",
        print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
        api = API(self.opt.dir)
        status_code, message = api.instance_start(inst_id, extra_files, run_cmds)
        
        if status_code == API.STATUS_SUCCESS:
            print Fore.GREEN + Style.BRIGHT + "done!"

            self._set_last_gpi(inst_id)
            
            t_end = time.time()
            
            delta = t_end - t_start
            minutes = int(delta / 60)
            seconds = int(delta - (minutes * 60))
            print "Started instance in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
            return 0
        elif status_code == API.STATUS_FAIL:
            print
            self._print_error("Could not start instance.", message)
            return 1 

def gp_instance_update_func():
    return gp_instance_update(sys.argv).run()  

class gp_instance_update(Command):
    """
    Updates a Globus Provision instance's topology. Globus Provision will determine what changes
    and necessary (adding/removing hosts, etc.) and will return an error if an invalid update
    was specified.
    
    See :ref:`sec_test_chef` for details on how to use the ``--extra-files`` option.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-update --topology newtopology.json gpi-12345678
        
    """
        
    name = "gp-instance-update"

    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)

        self.optparser.add_option("-t", "--topology", 
                                  action="store", type="string", dest="topology",
                                  help = "Topology file (JSON format only)")

        self.optparser.add_option("-x", "--extra-files", 
                                  action="store", type="string", dest="extra_files", 
                                  help = "File with list of files to upload to each host before configuring the instance.")
        
        self.optparser.add_option("-r", "--run", 
                                  action="store", type="string", dest="run", 
                                  help = "File with list of commands to run on each host after configuring the instance.")        
                
    def run(self):                
        t_start = time.time()        
        self.parse_options()

        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
        
        inst_id = self.args[1]

        if self.opt.topology != None:
            self._check_exists_file(self.opt.topology)
    
            jsonfile = open(self.opt.topology)
            topology_json = jsonfile.read()
            jsonfile.close()
        else:
            topology_json = None
                

        if self.opt.extra_files != None:
            self._check_exists_file(self.opt.extra_files)
            extra_files = parse_extra_files_files(self.opt.extra_files)
        else:
            extra_files = []
            
        if self.opt.run != None:
            self._check_exists_file(self.opt.run)
            run_cmds = [l.strip() for l in open(self.opt.run).readlines()]
        else:
            run_cmds = []            

        print "Updating topology of",
        print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",

        api = API(self.opt.dir)
        status_code, message = api.instance_update(inst_id, topology_json, extra_files, run_cmds)
        
        if status_code == API.STATUS_SUCCESS:
            print Fore.GREEN + Style.BRIGHT + "done!"
            
            self._set_last_gpi(inst_id)
            
            t_end = time.time()
            
            delta = t_end - t_start
            minutes = int(delta / 60)
            seconds = int(delta - (minutes * 60))
            print "Updated topology in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
            return 0
        elif status_code == API.STATUS_FAIL:
            self._print_error("Could not update topology.", message)
            return 1


def gp_instance_stop_func():
    return gp_instance_stop(sys.argv).run()         
        
class gp_instance_stop(Command):
    """
    Stops a running Globus Provision instance. This will shut down all the hosts in the instance,
    but it will not free the corresponding resources. You can use :ref:`cli_gp-instance-start` to resume
    the instance at a later time. Use :ref:`cli_gp-instance-terminate` if you want to shut down the hosts
    *and* free all their resources (including all associated storage) 
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-stop --verbose gpi-12345678
        
    """
        
    name = "gp-instance-stop"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)
                
    def run(self):       
        self.parse_options()
        
        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
        
        inst_id = self.args[1]            

        print "Stopping instance",
        print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
        api = API(self.opt.dir)
        status_code, message = api.instance_stop(inst_id)
        
        if status_code == API.STATUS_SUCCESS:
            print Fore.GREEN + Style.BRIGHT + "done!"
            self._set_last_gpi(inst_id)
            return 0
        elif status_code == API.STATUS_FAIL:
            self._print_error("Could not stop instance.", message)
            print
            return 1 


def gp_instance_terminate_func():
    return gp_instance_terminate(sys.argv).run()     

class gp_instance_terminate(Command):
    """
    Terminates a Globus Provision instance. This not only shuts down all the hosts in the
    instance, but also frees up all associated resources, including storage. Use this command
    only if you want to irreversibly "kill" your instance. If you only want to stop it temporarily
    (shutting down the hosts, but allowing them to be resumed at a later time), use 
    :ref:`cli_gp-instance-stop` instead.
    
    This command can also be used on instances that are in the "Failed" state, to free their
    resources.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-terminate --verbose gpi-12345678
        
    """
    
    name = "gp-instance-terminate"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)
                
    def run(self):        
        self.parse_options()

        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
                
        inst_id = self.args[1]

        print "Terminating instance",
        print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
        api = API(self.opt.dir)
        status_code, message = api.instance_terminate(inst_id)
        
        if status_code == API.STATUS_SUCCESS:
            print Fore.GREEN + Style.BRIGHT + "done!"
            self._set_last_gpi(inst_id)
            return 0
        elif status_code == API.STATUS_FAIL:
            print
            self._print_error("Could not terminate instance.", message)
            return 1  


def gp_instance_list_func():
    return gp_instance_list(sys.argv).run()     

class gp_instance_list(Command):
    """
    Lists all instances you've created, showing their identifier and their state.
    
    If you only want to list certain instances, you can specify a list of instance
    identifiers after all other parameters. For example::
    
        gp-instance-list --verbose gpi-11111111 gpi-22222222 gpi-33333333
        
    """
        
    name = "gp-instance-list"
    
    def __init__(self, argv):
        Command.__init__(self, argv, disable_sigintwatch=True)
                
    def run(self):    
        self.parse_options()
        
        if len(self.args) >= 2:
            inst_ids = self.args[1:]
        else:
            inst_ids = None
        
        api = API(self.opt.dir)
        (status_code, message, topologies_json) = api.instance_list(inst_ids)
        
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Unable to list instances.", message)
            return 1
        else:        
            insts = json.loads(topologies_json)
            
            for inst in insts:
                if self.opt.verbose or self.opt.debug:
                    print json.dumps(inst, indent=2)
                else:
                    topology = Topology.from_json_dict(inst)
                    
                    reset = Fore.RESET + Style.RESET_ALL
                    print Fore.WHITE + Style.BRIGHT + topology.id + reset + ": " + self._colorize_topology_state(topology.state)
            
            return 0

def gp_instance_add_user_func():
    return gp_instance_add_user(sys.argv).run()     

class gp_instance_add_user(Command):
    """
    Adds a user to a running instance.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-add-user --domain simple --login johnsmith gpi-12345678
        
    """
        
    name = "gp-instance-add-user"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)

        self.optparser.add_option("-m", "--domain", 
                                  action="store", type="string", dest="domain",
                                  help = "Domain that the user will be added to.")  

        self.optparser.add_option("-l", "--login", 
                                  action="store", type="string", dest="login",
                                  help = "User's UNIX login name")        

        self.optparser.add_option("-p", "--password-hash", 
                                  action="store", type="string", dest="passwd", 
                                  default = "!",
                                  help = "Password hash (default is disabled password)")
        
        self.optparser.add_option("-s", "--ssh-pubkey", 
                                  action="store", type="string", dest="ssh", 
                                  help = "Public SSH key to be added to the new user's authorized_keys file.")        

        self.optparser.add_option("-a", "--admin", 
                                  action="store_true", dest="admin", 
                                  help = "Gives the user sudo privileges.")
        
        self.optparser.add_option("-c", "--certificate", 
                                  action="store", type="string", dest="certificate", 
                                  default = "generated",                                  
                                  help = "Type of certificate. Can be \"generated\" or \"none\" (default is " 
                                         "to generate a certificate for the user)")

                
    def run(self):    
        t_start = time.time()        
        self.parse_options()
                
        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
          
        inst_id = self.args[1]

        api = API(self.opt.dir)
        (status_code, message, topology_json) = api.instance(inst_id)
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not access instance.", message)
            return 1 
        else:
            t = Topology.from_json_string(topology_json)
            
            if not t.domains.has_key(self.opt.domain):
                self._print_error("Could not add user", "Domain '%s' does not exist" % self.opt.domain)
                return 1
            
            domain = t.domains[self.opt.domain]
            
            user = User()
            user.set_property("id", self.opt.login)
            user.set_property("password_hash", self.opt.passwd)
            user.set_property("ssh_pkey", self.opt.ssh)
            if self.opt.admin != None:
                user.set_property("admin", self.opt.admin)
            else:
                user.set_property("admin", False)
            user.set_property("certificate", self.opt.certificate)

            domain.add_to_array("users", user)
            
            topology_json = t.to_json_string()

            print "Adding new user to",
            print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
            status_code, message = api.instance_update(inst_id, topology_json, [], [])
            
            if status_code == API.STATUS_SUCCESS:
                print Fore.GREEN + Style.BRIGHT + "done!"

                self._set_last_gpi(inst_id)
                
                t_end = time.time()
                
                delta = t_end - t_start
                minutes = int(delta / 60)
                seconds = int(delta - (minutes * 60))
                print "Added user in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
                return 0
            elif status_code == API.STATUS_FAIL:
                self._print_error("Could not update topology.", message)
                return 1
        
        
def gp_instance_add_host_func():
    return gp_instance_add_host(sys.argv).run()     
        
class gp_instance_add_host(Command):
    """
    Adds a new host to a running instance.
    
    The instance identifier must be specified after all other parameters. For example::
    
        gp-instance-add-host --domain simple --id simple-gridftp --run-list role[domain-gridftp-default] gpi-12345678
        
    """
        
    name = "gp-instance-add-host"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)

        self.optparser.add_option("-m", "--domain", 
                                  action="store", type="string", dest="domain",
                                  help = "Domain that the user will be added to.")  

        self.optparser.add_option("-n", "--id", 
                                  action="store", type="string", dest="id",
                                  help = "Unique identifier for the new host.")        

        self.optparser.add_option("-p", "--depends", 
                                  action="store", type="string", dest="depends", 
                                  default = None,
                                  help = "ID of the node (if any) that the new host depends on.")
        
        self.optparser.add_option("-r", "--run-list", 
                                  action="store", type="string", dest="runlist", 
                                  help = "List of Chef recipes or roles to apply to this host.")     
                
    def run(self):    
        t_start = time.time()        
        self.parse_options()
                
        if len(self.args) != 2:
            print "You must specify an instance id."
            print "For example: %s [options] gpi-37a8bf17" % self.name
            return 1
        
        inst_id = self.args[1]

        api = API(self.opt.dir)
        (status_code, message, topology_json) = api.instance(inst_id)
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not access instance.", message)
            return 1
        else:
            t = Topology.from_json_string(topology_json)
            
            if not t.domains.has_key(self.opt.domain):
                self._print_error("Could not add host", "Domain '%s' does not exist" % self.opt.domain)
                return 1
            
            domain = t.domains[self.opt.domain]
            
            node = Node()
            node.set_property("id", self.opt.id)
            node.set_property("run_list", self.opt.runlist.split(","))
            if self.opt.depends != None:
                node.set_property("depends", "node:%s" % self.opt.depends)

            domain.add_to_array("nodes", (node))
            
            topology_json = t.to_json_string()

            print "Adding new host to",
            print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
            status_code, message = api.instance_update(inst_id, topology_json, [], [])
            
            if status_code == API.STATUS_SUCCESS:
                print Fore.GREEN + Style.BRIGHT + "done!"
                
                self._set_last_gpi(inst_id)
                
                t_end = time.time()
                
                delta = t_end - t_start
                minutes = int(delta / 60)
                seconds = int(delta - (minutes * 60))
                print "Added host in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
                return 0
            elif status_code == API.STATUS_FAIL:
                self._print_error("Could not update topology.", message)
                return 1
        
        
def gp_instance_remove_users_func():
    return gp_instance_remove_users(sys.argv).run()     

class gp_instance_remove_users(Command):
    """
    Removes users from a running instance.
    
    The logins of the users to be removed must be specified after the instance identifier which,
    in turn, must be specified after all other parameters. For example::
    
        gp-instance-remove-users --domain simple gpi-12345678 johnsmith sarahjane
        
    """
        
    name = "gp-instance-remove-users"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)
                
        self.optparser.add_option("-m", "--domain", 
                                  action="store", type="string", dest="domain",
                                  help = "Removes users from this domain")  
                
    def run(self):    
        t_start = time.time()        
        self.parse_options()
                
        if len(self.args) <= 2:
            print "You must specify an instance id and at least one host."
            print "For example: %s [options] gpi-37a8bf17 simple-wn3" % self.name
            return 1
        
        inst_id = self.args[1]
        users = self.args[2:]
        
        api = API(self.opt.dir)
        (status_code, message, topology_json) = api.instance(inst_id)
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not access instance.", message)
            return 1
        else:
            t = Topology.from_json_string(topology_json)
            
            if not t.domains.has_key(self.opt.domain):
                self._print_error("Could not remove users", "Domain '%s' does not exist" % self.opt.domain)
                return 1
                            
            removed = []
            domain_users = t.domains[self.opt.domain].users
            
            for user in users:
                if user in domain_users:
                    domain_users.pop(user)
                    removed.append(user)
                        
            remaining = set(removed) ^ set(users)
            for r in remaining:
                print Fore.YELLOW + Style.BRIGHT + "Warning" + Fore.RESET + Style.RESET_ALL + ":",
                print "User %s does not exist." % r

            topology_json = t.to_json_string()

            if len(removed) > 0:
                print "Removing users %s from" % list(removed),
                print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
                status_code, message = api.instance_update(inst_id, topology_json, [], [])
    
                if status_code == API.STATUS_SUCCESS:
                    print Fore.GREEN + Style.BRIGHT + "done!"
                    
                    self._set_last_gpi(inst_id)
                    
                    t_end = time.time()
                    
                    delta = t_end - t_start
                    minutes = int(delta / 60)
                    seconds = int(delta - (minutes * 60))
                    print "Removed users in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
                    return 0
                elif status_code == API.STATUS_FAIL:
                    self._print_error("Could not update topology.", message)
                    return 1

              
def gp_instance_remove_hosts_func():
    return gp_instance_remove_hosts(sys.argv).run()             
        
class gp_instance_remove_hosts(Command):
    """
    Removes hosts from a running instance.
    
    The host identifiers must be specified after the instance identifier which,
    in turn, must be specified after all other parameters. For example::
    
        gp-instance-remove-hosts --domain simple gpi-12345678 simple-gridftp simple-condor
        
    """
        
    name = "gp-instance-remove-hosts"
    
    def __init__(self, argv, disable_sigintwatch=False):
        Command.__init__(self, argv, disable_sigintwatch=disable_sigintwatch)

        self.optparser.add_option("-m", "--domain", 
                                  action="store", type="string", dest="domain",
                                  help = "Remove hosts from this domain")  
                
    def run(self):    
        t_start = time.time()        
        self.parse_options()
                
        if len(self.args) <= 2:
            print "You must specify an instance id and at least one host."
            print "For example: %s [options] gpi-37a8bf17 simple-wn3" % self.name
            return 1
        
        inst_id = self.args[1]
        hosts = self.args[2:]
        
        api = API(self.opt.dir)
        (status_code, message, topology_json) = api.instance(inst_id)
        
        if status_code != API.STATUS_SUCCESS:
            self._print_error("Could not access instance.", message)
            return 1 
        else:
            t = Topology.from_json_string(topology_json)
            
            if not t.domains.has_key(self.opt.domain):
                self._print_error("Could not remove hosts", "Domain '%s' does not exist" % self.opt.domain)
                return 1
                            
            removed = []
            nodes = t.domains[self.opt.domain].nodes
            
            for host in hosts:
                if host in nodes:
                    nodes.pop(host)
                    removed.append(host)
                        
            remaining = set(removed) ^ set(hosts)
            for r in remaining:
                print Fore.YELLOW + Style.BRIGHT + "Warning" + Fore.RESET + Style.RESET_ALL + ":",
                print "Host %s does not exist." % r

            topology_json = t.to_json_string()

            if len(removed) > 0:
                print "Removing hosts %s from" % list(removed),
                print Fore.WHITE + Style.BRIGHT + inst_id + Fore.RESET + Style.RESET_ALL + "...",
                status_code, message = api.instance_update(inst_id, topology_json, [], [])
    
                if status_code == API.STATUS_SUCCESS:
                    print Fore.GREEN + Style.BRIGHT + "done!"
                    
                    self._set_last_gpi(inst_id)
                    
                    t_end = time.time()
                    
                    delta = t_end - t_start
                    minutes = int(delta / 60)
                    seconds = int(delta - (minutes * 60))
                    print "Removed hosts in " + Fore.WHITE + Style.BRIGHT + "%i minutes and %s seconds" % (minutes, seconds)
                elif status_code == API.STATUS_FAIL:
                    self._print_error("Could not update topology.", message)
                    return 1
                