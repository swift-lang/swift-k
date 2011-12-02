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
Topology

This module contains classes used to manage a topology. All of these classes
are PersistentObjects, so the properties are defined separately from
the classes themselves, which can make the code a bit hard to read. 
Before diving into this code, you may want to check out the topology 
documentation (in the Globus Provision Documentation), which is automatically
generated from the code in this module.  

"""

from globus.provision.common.persistence import PersistentObject, PropertyTypes,\
    Property
    
class Topology(PersistentObject):
    STATE_NEW = 1
    STATE_STARTING = 2
    STATE_CONFIGURING = 3
    STATE_RUNNING = 4
    STATE_STOPPING = 5
    STATE_STOPPED = 6
    STATE_RESUMING = 7
    STATE_TERMINATING = 8
    STATE_TERMINATED = 9
    STATE_FAILED = 10
    
    # String representation of states
    state_str = {STATE_NEW : "New",
                 STATE_STARTING : "Starting",
                 STATE_CONFIGURING : "Configuring",
                 STATE_RUNNING : "Running",
                 STATE_STOPPING : "Stopping",
                 STATE_STOPPED : "Stopped",
                 STATE_RESUMING : "Resuming",
                 STATE_TERMINATING : "Terminating",
                 STATE_TERMINATED : "Terminated",
                 STATE_FAILED : "Failed"}        
    
    def get_nodes(self):
        nodes = []
        for domain in self.domains.values():
            nodes += [n for n in domain.get_nodes()]
        return nodes       
    
    def get_users(self):
        users = []
        for domain in self.domains.values():
            users += domain.get_users()
        return users    
    
    def gen_hosts_file(self, filename):
        hosts = """127.0.0.1    localhost

# The following lines are desirable for IPv6 capable hosts
::1     localhost ip6-localhost ip6-loopback
fe00::0 ip6-localnet
ff00::0 ip6-mcastprefix
ff02::1 ip6-allnodes
ff02::2 ip6-allrouters
ff02::3 ip6-allhosts

"""
        
        nodes = self.get_nodes()
        for n in nodes:
            hosts += " ".join((n.ip, n.hostname, n.hostname.split(".")[0], "\n"))
        
        hostsfile = open(filename, "w")
        hostsfile.write(hosts)
        hostsfile.close()         
        
    def gen_chef_ruby_file(self, filename):
        
        def gen_topology_line(server_name, domain_id, recipes):
            server = domain.find_with_recipes(recipes)
            if len(server) > 0:
                server_node = server[0]
                if len(server) > 1:
                    # TODO: Print a warning saying more than one NFS server has been found
                    pass
                hostname_line = "default[:topology][:domains][\"%s\"][:%s] = \"%s\"\n" % (domain_id, server_name, server_node.hostname)
                ip_line       = "default[:topology][:domains][\"%s\"][:%s_ip] = \"%s\"\n" % (domain_id, server_name, server_node.ip)
                
                return hostname_line + ip_line
            else:
                return ""                              
        
        topology = "default[:topology] = %s\n" % self.to_ruby_hash_string()

        for domain in self.domains.values():
            topology += gen_topology_line("nfs_server", domain.id, ["recipe[provision::nfs_server]", "role[domain-nfsnis]"])
            topology += gen_topology_line("nis_server", domain.id, ["recipe[provision::nis_server]", "role[domain-nfsnis]"])
            topology += gen_topology_line("myproxy_server", domain.id, ["recipe[globus::myproxy]"])
            topology += gen_topology_line("lrm_head", domain.id, ["recipe[condor::condor_head]", "role[domain-condor]"])
        
        topologyfile = open(filename, "w")
        topologyfile.write(topology)
        topologyfile.close()        
    
    def get_depends(self, node):
        if not hasattr(node, "depends"):
            return None
        else:
            return self.get_node_by_id(node.depends[5:])
        
    def get_launch_order(self, nodes):
        order = []
        parents = [n for n in nodes if self.get_depends(n) == None or self.get_depends(n) not in nodes]
        while len(parents) > 0:
            order.append(parents)
            parents = [n for n in nodes if self.get_depends(n) in parents]   
        return order        
    
    def get_node_by_id(self, node_id):
        nodes = self.get_nodes()
        node = [n for n in nodes if n.id == node_id]
        if len(node) == 1:
            return node[0]
        else:
            return None    
        
    def get_deploy_data(self, node, deployer, p_name):
        if node.has_property("deploy_data") and node.deploy_data.has_property(deployer):
            deploy_data = node.deploy_data.get_property(deployer)
            if deploy_data.has_property(p_name):
                return deploy_data.get_property(p_name)
        
        # If node doesn't have requested deploy data, return default (if any)
        if self.has_property("default_deploy_data") and self.default_deploy_data.has_property(deployer):
            deploy_data = self.default_deploy_data.get_property(deployer)
            if deploy_data.has_property(p_name):
                return deploy_data.get_property(p_name)
            
        return None
    
    def get_go_endpoints(self):    
        eps = []
        for domain_name, domain in self.domains.items():
            if domain.has_property("go_endpoints"):
                eps += domain.go_endpoints
        return eps   
    
    def add_domain(self, domain):
        self.add_to_array("domains", domain)
        
    
    
class Domain(PersistentObject):

    def get_nodes(self):
        return self.nodes.values()
    
    def get_users(self):
        return self.users.values()     
    
    def find_with_recipes(self, recipes):
        nodes = []
        for node in self.nodes.values():
            for r in recipes:
                if r in node.run_list:
                    nodes.append(node)
                    continue
        return nodes

    def add_user(self, user):
        self.add_to_array("users", user)
        
    def add_node(self, node):
        self.add_to_array("nodes", node)        

class DeployData(PersistentObject):
    pass

class EC2DeployData(PersistentObject):
    pass

class Node(PersistentObject):
    STATE_NEW = 0
    STATE_STARTING = 1
    STATE_RUNNING_UNCONFIGURED = 2
    STATE_CONFIGURING = 3
    STATE_RUNNING = 4
    STATE_RECONFIGURING = 11
    STATE_STOPPING = 5
    STATE_STOPPING_CONFIGURING = 12
    STATE_STOPPING_CONFIGURED = 13
    STATE_STOPPED = 6
    STATE_RESUMING = 7
    STATE_RESUMED_UNCONFIGURED = 14
    STATE_RESUMED_RECONFIGURING = 15
    STATE_TERMINATING = 8
    STATE_TERMINATED = 9
    STATE_FAILED = 10
    
    # String representation of states
    state_str = {STATE_NEW : "New",
                 STATE_STARTING : "Starting",
                 STATE_RUNNING_UNCONFIGURED : "Running (unconfigured)",
                 STATE_CONFIGURING : "Configuring",
                 STATE_RUNNING : "Running",
                 STATE_RECONFIGURING : "Running (reconfiguring)",
                 STATE_STOPPING : "Stopping",
                 STATE_STOPPING_CONFIGURING : "Stopping (configuring)",
                 STATE_STOPPING_CONFIGURED : "Stopping (configured)",
                 STATE_STOPPED : "Stopped",
                 STATE_RESUMING : "Resuming",
                 STATE_RESUMED_UNCONFIGURED : "Resumed (unconfigured)",
                 STATE_RESUMED_RECONFIGURING : "Resumed (reconfiguring)",
                 STATE_TERMINATING : "Terminating",
                 STATE_TERMINATED : "Terminated",
                 STATE_FAILED : "Failed"}   


class User(PersistentObject):
    pass

class GridMapEntry(PersistentObject):
    pass

class GOEndpoint(PersistentObject):
    pass

Topology.properties = { 
                       "id":
                       Property(name="id",
                                proptype = PropertyTypes.STRING,
                                required = False,
                                description = """
                                Once an instance with this topology has been created,
                                this property will contain the instance ID 
                                (e.g., ``gpi-12345678``) assigned by Globus Provision.
                                """),       
                       
                       "state":
                       Property(name="state",
                                proptype = PropertyTypes.INTEGER,
                                required = False,
                                description = """
                                Once an instance has been created with this topology,
                                this property indicates the state the instance is in.
                                Possible values are:
                                
                                %s
                                
                                """ % 
"\n                                ".join(["* %i: %s" % (i, Topology.state_str[i]) for i in sorted(Topology.state_str.keys())])),     
                                                                        
                       "domains": 
                       Property(name = "domains",
                                proptype = PropertyTypes.ARRAY,
                                items = Domain,
                                items_unique = True,
                                editable = True,
                                required = True,
                                description = """
                                The domains in this topology.
                                """),

                       "default_deploy_data":
                       Property(name = "default_deploy_data",
                                proptype = DeployData,
                                required = False,
                                editable = True,                                
                                description = """
                                The default deployment-specific data for this instance.
                                Individual nodes can override the default values in their
                                ``deploy_data`` property.
                                """)          
                       }

DeployData.properties = { "ec2":
                            Property(name = "ec2",
                                     proptype = EC2DeployData,
                                     required = False,
                                     editable = True,
                                     description = """
                                     Used to specify EC2-specific deployment data.
                                     """)          
                       }

EC2DeployData.properties = { 
                            "instance_type":
                                Property(name = "instance_type",
                                         proptype = PropertyTypes.STRING,
                                         required = False,
                                         editable = True,
                                         description = """
                                         An EC2 instance type (e.g., ``t1.micro``, ``m1.small``, etc.)
                                         """),
                            
                            "instance_id":
                                Property(name = "instance_id",
                                         proptype = PropertyTypes.STRING,
                                         required = False,
                                         description = """
                                         Once a host has been deployed on EC2,
                                         this property will contain its EC2 instance identifier.                                        
                                         """),               
                            "ami":
                                Property(name = "ami",
                                         proptype = PropertyTypes.STRING,
                                         required = False,
                                         editable = True,
                                         description = """
                                         The Amazon Machine Image (AMI) to use when creating
                                         new hosts on Amazon EC2.
                                         """),
                            
                            "security_groups":
                                Property(name = "security_groups",
                                         proptype = PropertyTypes.ARRAY,
                                         items = PropertyTypes.STRING,
                                         items_unique = True,                                         
                                         required = False,
                                         editable = True,
                                         description = """
                                         A list of `Security Groups <http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/index.html?using-network-security.html>`_
                                         to apply to hosts on EC2. If no security groups are specified,
                                         Globus Provision will create one called ``globus-provision``
                                         that opens the TCP/UDP ports for SSH, GridFTP, and MyProxy. 
                                         """)                                
                       }


Domain.properties = {
                     "id":
                     Property(name="id",
                              proptype = PropertyTypes.STRING,
                              required = True,
                              description = """
                              A unique name for the domain.
                              """),               
                              
                     "nodes":
                     Property(name="nodes",
                              proptype = PropertyTypes.ARRAY,
                              items = Node,
                              items_unique = True,
                              required = True,
                              editable = True,
                              description = """
                              The list of hosts (or *nodes*) in this domain.
                              """),
                              
                     "go_endpoints":                    
                     Property(name="go_endpoints",
                              proptype = PropertyTypes.ARRAY,
                              items = GOEndpoint,
                              required = False,
                              editable = True,
                              description = """
                              The list of Globus Online endpoints defined for this domain.
                              """),
                                    
                     "users": 
                     Property(name="users",
                              proptype = PropertyTypes.ARRAY,
                              items = User,
                              items_unique = True,
                              required = True,
                              editable = True,
                              description = """
                              The list of users in this domain.
                              """),
                            
                     "gridmap":                                           
                     Property(name="gridmap",
                              proptype = PropertyTypes.ARRAY,
                              items = GridMapEntry,
                              required = False,
                              editable = True,
                              description = """
                              The list of gridmap entries for this domain. This
                              is the gridmap that Globus services running on this
                              domain will use to determine if a given user is
                              authorized to access the service.
                              """),
                     }

Node.properties = {
                   "id":
                   Property(name="id",
                            proptype = PropertyTypes.STRING,
                            required = True,
                            description = """
                            A unique identifier for this host. The value of this
                            property is only used for identification purposes
                            (e.g., when printing the status of an instance with
                            ``gp-instance-describe``), and will not affect other
                            properties, like its hostname, etc. (except when using
                            the ``dummy`` deployer).
                            """),
                   "state":
                   Property(name="state",
                            proptype = PropertyTypes.INTEGER,
                            required = False,
                            editable = False,
                            description = """
                            Once an instance with this topology has been created,
                            this property will indicate the state of this particular
                            host.
                            
                            Possible values are:
                                
                                %s
                                
                                """ % 
"\n                                ".join(["* %i: %s" % (i, Node.state_str[i]) for i in sorted(Node.state_str.keys())])),     
                                                                        
                   "run_list":
                   Property(name="run_list",
                            proptype = PropertyTypes.ARRAY,
                            items = PropertyTypes.STRING,
                            required = True,
                            editable = True,
                            description = """
                            The list of Chef recipes to run on this node.
                            See :ref:`sec_runlist` for more details.
                            """),
                            
                   "depends":
                   Property(name="depends",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            editable = True,
                            description = """
                            Sometimes, a host cannot be configured until another host
                            in the topology is configured. For example, NFS clients cannot
                            start until the NFS server is starting. This property is
                            used to specify such dependencies. The value of this property
                            must be of the form node:*node_id*, where *node_id* is
                            the identifier of another node in the domain.
                            
                            For example, if this node depends on ``simple-nfs`` the value
                            of this property would be ``node:simple-nfs``.
                            """),
                            
                   "hostname":
                   Property(name="hostname",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            description = """
                            The fully-qualified hostname assigned by the deployer.
                            """),
                            
                   "ip":
                   Property(name="ip",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            description = """
                            The IP address assigned by the deployer.
                            """),
                            
                   "public_ip":
                   Property(name="public_ip",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            description = """
                            If the IP address assigned in ``ip`` is a private, non-routable,
                            IP address, but the host is also assigned a public IP, it will
                            be assigned to this property by the deployer.
                            """),                            
                            
                   "deploy_data":
                   Property(name = "deploy_data",
                            proptype = DeployData,
                            required = False,
                            description = """
                            Host-specific deployment data. The values specified here
                            will override any values specified in the topology's
                            ``default_deploy__data`` property.
                            """),   
                            
                   "gc_setupkey":
                   Property(name = "gc_setupkey",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            description = """
                            Globus Connect setup key used to obtain a certificate
                            for use by services in this node. 
                            """)                 
                   
                   }          


User.properties = {
                   "id":
                   Property(name="id",                          
                            proptype = PropertyTypes.STRING,
                            required = True,
                            description = """
                            The user's login name.
                            """),
                            
                   "description":
                   Property(name="description",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            editable = True,
                            description = """
                            A description of the user.
                            """),
                            
                   "password_hash":
                   Property(name="password_hash",
                            proptype = PropertyTypes.STRING,
                            required = True,
                            editable = True,
                            description = """
                            The password hash for the user, exactly as it will appear
                            in the shadow file (``/etc/shadow``). To generate a password,
                            you can use the ``mkpasswd`` command. We recommend you generate
                            SHA-512 password hashes. For example::
                            
                                $ mkpasswd -m sha-512 mypassword
                                $6$XrtqyXi4LO$8M/sk6t8zE5Ac.acLPBt577f1eGv.YnUVZPhGmBlQF/YrYnkWQPq7EMfryWEdHm664B.RaY3O8oZtbiQjXfu10
                            
                            The string starting with ``$6$`` is the password hash.
                            
                            You can disable password access for this user by setting this 
                            property to ``!``. 
                            """),
                            
                   "ssh_pkey":
                   Property(name="ssh_pkey",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            editable = True,
                            description = """
                            A public SSH key. If a value is specified for this property,
                            this public key will be added to the user's ``authorized_keys``
                            file.
                            """),
                            
                   "admin":
                   Property(name="admin",
                            proptype = PropertyTypes.BOOLEAN,
                            required = False,
                            editable = True,
                            description = """
                            If ``true``, this user will be granted passwordless sudo
                            access on all hosts in this domain.
                            """),
                            
                   "certificate":
                   Property(name = "certificate",
                            proptype = PropertyTypes.STRING,
                            required = False,
                            description = """
                            This property can take on the following values:
                            
                            * ``"generated"``: A user certificate must be generated for this user.
                            * ``"none"``: Do not generate a certificate for this user.
                            """),           
                   }            

GridMapEntry.properties = {                   
                           "dn": 
                           Property(name="dn",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    description = """
                                    The distinguished name in the gridmap entry
                                    (e.g., ``"/O=Grid/OU=My Grid/CN=J.Random User"``)
                                    """),
                                   
                           "login":
                           Property(name="login",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    editable = True,
                                    description = """
                                    The login the distinguished name will map to.
                                    Must be a valid login in the domain.
                                    """),
                           }       

GOEndpoint.properties = {           
                         
                           "user":
                           Property(name="user",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    description = """
                                    The Globus Online user account in which to create
                                    this endpoint.
                                    """),
                           "name":
                           Property(name="name",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    description = """
                                    The endpoint name.
                                    """),

                           "public":
                           Property(name="public",
                                    proptype = PropertyTypes.BOOLEAN,
                                    required = True,
                                    description = """
                                    Whether the endpoint should be public or not.
                                    """),                         
                                   
                           "gridftp":
                           Property(name="gridftp",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    editable = True,
                                    description = """
                                    The GridFTP server for this endpoint. You can specify
                                    either a fully qualified hostname, or refer to an existing
                                    node in this domain by writing node:*node_id*, 
                                    where *node_id* is the identifier of another node in the domain.
                                    """),
                                   
                           "myproxy":
                           Property(name="myproxy",
                                    proptype = PropertyTypes.STRING,
                                    required = True,
                                    editable = True,
                                    description = """
                                    The MyProxy server that will be used for authenticating
                                    users that want to use this endpoint. You can specify
                                    either a fully qualified hostname, or refer to an existing
                                    node in this domain by writing node:*node_id*, 
                                    where *node_id* is the identifier of another node in the domain.
                                    
                                    Take into account that, to set up this endpoint for
                                    "Globus Online Authentication" (as described in :ref:`sec_go_auth`)
                                    you will need to do the following:
                                    
                                    * Set this property to ``myproxy.globusonline.org``
                                    * For each GO user you want to authorize in this endpoint,
                                      add an entry with the following distinguished name::
                                       
                                           "/C=US/O=Globus Consortium/OU=Globus Connect User/CN=username"
                                      
                                      Where ``username`` is the username of the GO account you
                                      want to authorized. Usually, you will want that DN to map
                                      to the same username in the domain, although this is not
                                      required.

                                    """),
                         
                           "globus_connect_cert":
                           Property(name="globus_connect_cert",
                                    proptype = PropertyTypes.BOOLEAN,
                                    required = False,
                                    description = """
                                    If true, this endpoint will use a Globus Connect certificate.
                                    If not, it will use the host certificate generated by Globus Provision.
                                    Take into account that, for the GridFTP server to be trusted by
                                    Globus Online, it must use a certificate trusted by Globus Online.
                                    Unless you used a CA trusted by Globus Online to generate the certificates
                                    for the topology, you must use a Globus Connect certificate.
                                    """),
                         
                           "globus_connect_cert_dn":
                           Property(name="globus_connect_cert",
                                    proptype = PropertyTypes.STRING,
                                    required = False,
                                    description = """
                                    The DN of the Globus Connect certificate for this endpoint.
                                    """)                                                               
                           
                        }                                                                                                                 
