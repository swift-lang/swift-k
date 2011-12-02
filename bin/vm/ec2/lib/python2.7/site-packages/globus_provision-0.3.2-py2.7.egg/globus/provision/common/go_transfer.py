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
Helper classes for performing Globus Online operations.

"""

import re
import StringIO
import os.path
import paramiko

from pkg_resources import resource_filename

from globus.provision.common.ssh import SSH
from globus.provision.common import log
from globusonline.transfer.api_client import TransferAPIClient, ClientError


class GlobusOnlineException(Exception):
    pass
    
class EndpointExistsException(GlobusOnlineException):
    pass    

class GlobusOnlineHelper(object):
    
    def endpoint_create(self, ep, replace):
            
        if self.endpoint_exists(ep):
            if not replace:
                raise EndpointExistsException, "An endpoint called '%s' already exists. Please choose a different name." % ep.name
            else:
                self.endpoint_remove(ep)                

        gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject = self._get_hostnames_subjects(ep)
            
        self._endpoint_create(ep.name, gridftp_hostname, gridftp_subject, myproxy_hostname, ep.public)       

    def _get_hostnames_subjects(self, ep):
        ca_dn = self.inst.config.get("ca-dn")
        if ca_dn == None:
            ca_dn = "/O=Grid/OU=Globus Provision (generated)"
        else:
            ca_dn = [x.split("=") for x in ca_dn.split(",")]
            ca_dn = "".join(["/%s=%s" % (n.upper().strip(), v.strip()) for n,v in ca_dn])

        if ep.gridftp.startswith("node:"):
            gridftp_node = self.inst.topology.get_node_by_id(ep.gridftp[5:])
            gridftp_hostname = gridftp_node.hostname
            
            if ep.has_property("globus_connect_cert") and ep.globus_connect_cert:
                if ep.has_property("globus_connect_cert_dn"):
                    gridftp_subject = ep.globus_connect_cert_dn
                else:
                    gridftp_subject = None
            else:
                gridftp_subject = "%s/CN=host/%s" % (ca_dn, gridftp_hostname)
        else:
            # Note: If user specifies an arbitrary GridFTP hostname, it
            # will only work if it uses a valid host certificate that
            # is trusted by Globus Online.
            gridftp_hostname = ep.gridftp
            gridftp_subject = None
            
        if ep.myproxy.startswith("node:"):
            myproxy_node = self.inst.topology.get_node_by_id(ep.myproxy[5:])
            myproxy_hostname = myproxy_node.hostname
            myproxy_subject = "%s/CN=host/%s" % (ca_dn, gridftp_hostname)
        else:
            myproxy_hostname = ep.myproxy  
            myproxy_subject = None
            
        return gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject    
    
    @staticmethod
    def from_instance(inst):
        if inst.config.get("go-cert-file") == None and inst.config.get("go-ssh-key") != None:
            # Use SSH
            ssh_key = os.path.expanduser(inst.config.get("go-ssh-key"))
            return GlobusOnlineCLIHelper(inst, ssh_key)
        elif inst.config.get("go-cert-file") != None:
            go_cert_file = os.path.expanduser(inst.config.get("go-cert-file"))
            go_key_file = os.path.expanduser(inst.config.get("go-key-file"))
            go_server_ca = resource_filename("globus.provision", "chef-files/cookbooks/globus/files/default/gd-bundle_ca.cert")
            return GlobusOnlineAPIHelper(inst, go_cert_file, go_key_file, go_server_ca)
        else:
            raise GlobusOnlineException, "Must specify either an SSH key or a certificate to connect to GO"
        

class GlobusOnlineAPIHelper(GlobusOnlineHelper):
    def __init__(self, inst, go_cert_file, go_key_file, go_server_ca):
        self.inst = inst
        self.go_cert_file = go_cert_file
        self.go_key_file = go_key_file
        self.go_server_ca = go_server_ca
        
    def connect(self, username):
        self.api = TransferAPIClient(username, self.go_server_ca, self.go_cert_file, self.go_key_file)

    def endpoint_exists(self, ep):
        try:
            (code, msg, data) = self.api.endpoint(ep.name)

            if code == 200:
                return True
            elif code == 404:
                return False
            else:
                raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
        except ClientError as ce:
            if ce.status_code == 404:
                return False
            else:
                raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce

    def disconnect(self):
        self.api.close()

    def endpoint_remove(self, ep):
        try:
            (code, msg, data) = self.api.endpoint_delete(ep.name)
            if code >= 400:
                raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
        except ClientError as ce:
            raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce

    def endpoint_gc_create(self, ep, replace):
        if self.endpoint_exists(ep):
            if not replace:
                raise EndpointExistsException, "An endpoint called '%s' already exists. Please choose a different name." % ep.name
            else:
                self.endpoint_remove(ep)
                   
        try:
            (code, msg, data) = self.api.endpoint_create(ep.name, None, description="Globus Provision endpoint",
                                                         is_globus_connect=True)
            if code >= 400:
                raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
        except ClientError as ce:
            raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce                   

        gc_setupkey = data["globus_connect_setup_key"]

        return gc_setupkey

    def endpoint_gc_create_finalize(self, ep):
        gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject = self._get_hostnames_subjects(ep)
        
        # This is a kludge until http://jira.globus.org/browse/KOA-1624 gets fixed
        username = self.inst.config.get("ec2-username")
        keyfile = os.path.expanduser(self.inst.config.get("ec2-keyfile"))
        gc_setupkey = self.inst.topology.get_node_by_id(ep.gridftp[5:]).gc_setupkey
        ssh = SSH(username, gridftp_hostname, keyfile, default_outf = None, default_errf = None)
        ssh.open()      
        outf = StringIO.StringIO()
        errf = StringIO.StringIO()
        rc = ssh.run("grid-cert-info -subject -file /etc/grid-security/gc-cert-%s.pem" % (gc_setupkey), outf=outf, errf=errf, exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not create endpoint %s" % ep.name
        ssh.close()

        gridftp_subject = outf.getvalue().strip()     
              
        self.endpoint_remove(ep)
                      
        self._endpoint_create(ep.name, gridftp_hostname, gridftp_subject, myproxy_hostname, ep.public)
        

    def _endpoint_create(self, ep_name, gridftp_hostname, gridftp_subject, myproxy_hostname, public):
        try:
            (code, msg, data) = self.api.endpoint_create(ep_name, gridftp_hostname, description="Globus Provision endpoint",
                                                         scheme="gsiftp", port=2811, subject=gridftp_subject,
                                                         myproxy_server=myproxy_hostname)
            if code >= 400:
                raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
        except ClientError as ce:
            raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce
   

        if public:
            try:
                (code, msg, data) = self.api.endpoint(ep_name)
                if code >= 400:
                    raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
            except ClientError as ce:
                raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce    
                  
            data["public"] = True
            
            try:
                (code, msg, data) = self.api.endpoint_update(ep_name, data)
                if code >= 400:
                    raise GlobusOnlineException, "Unexpected error %i: %s" % (code, msg)            
            except ClientError as ce:
                raise GlobusOnlineException, "Unexpected GO API exception: %s" % ce          



class GlobusOnlineCLIHelper(GlobusOnlineHelper):
    def __init__(self, inst, ssh_key):
        self.inst = inst
        self.ssh_key = ssh_key
        
    def connect(self, username):
        self.ssh = SSH(username, "cli.globusonline.org", self.ssh_key, default_outf = None, default_errf = None)
        try:
            self.ssh.open(timeout=10)
        except paramiko.PasswordRequiredException, pre:
            msg = "The specified SSH key (%s) requires a password." % self.ssh_key
            msg += "Please specify a passwordless SSH key."
            raise GlobusOnlineException, msg
        except Exception, e:
            raise GlobusOnlineException, "Could not connect to GO CLI. Cause: %s" % e
        
    def disconnect(self):
        self.ssh.close()

    def endpoint_exists(self, ep):
        rc = self.ssh.run("endpoint-list %s" % (ep.name), exception_on_error=False)
        return (rc == 0)

    def endpoint_remove(self, ep):
        rc = self.ssh.run("endpoint-remove %s" % (ep.name), exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not remove endpoint %s" % ep.name        

    def endpoint_remove_server(self, ep, server):
        rc = self.ssh.run("endpoint-remove %s -p %s" % (ep.name, server), exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not remove server for endpoint %s" % ep.name

    def endpoint_gc_create(self, ep, replace):
        if self.endpoint_exists(ep):
            if not replace:
                raise EndpointExistsException, "An endpoint called '%s' already exists. Please choose a different name." % ep.name
            else:
                self.endpoint_remove(ep)
                   
        outf = StringIO.StringIO()
        errf = StringIO.StringIO()
        
        rc = self.ssh.run("endpoint-add %s --gc" % (ep.name), outf=outf, errf=errf, exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not create endpoint %s" % ep.name
        
        output = outf.getvalue()
        gc_setupkey = re.findall("[0-9abcdef-]+$", output)[0]
        
        return gc_setupkey

    def endpoint_gc_create_finalize(self, ep):
        gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject = self._get_hostnames_subjects(ep)
        
        outf = StringIO.StringIO()
        errf = StringIO.StringIO()
        rc = self.ssh.run("endpoint-list -f subjects  %s" % (ep.name), outf=outf, errf=errf, exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not create endpoint %s" % ep.name

        gridftp_subject = outf.getvalue().strip()      
        ep.set_property("globus_connect_cert_dn", gridftp_subject)
        
        self.endpoint_remove(ep)
           
        self._endpoint_create(ep.name, gridftp_hostname, gridftp_subject, myproxy_hostname, ep.public)

    def endpoint_stop(self, ep):
        gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject = self._get_hostnames_subjects(ep)
        
        self.endpoint_remove_server(ep, gridftp_hostname)        
        
        # Create a disconnected endpoint
        self._endpoint_create(ep.name, "relay-disconnected.globusonline.org", gridftp_subject, "myproxy.globusonline.org", ep.public)

    def endpoint_resume(self, ep):
        gridftp_hostname, gridftp_subject, myproxy_hostname, myproxy_subject = self._get_hostnames_subjects(ep)
        
        # Remove disconnected server
        self.endpoint_remove_server(ep, "relay-disconnected.globusonline.org") 
        
        self._endpoint_create(ep.name, gridftp_hostname, gridftp_subject, myproxy_hostname, ep.public)

    def _endpoint_create(self, ep_name, gridftp_hostname, gridftp_subject, myproxy_hostname, public):   
        rc = self.ssh.run("endpoint-add %s -p %s -s \"%s\"" % (ep_name, gridftp_hostname, gridftp_subject), exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not create endpoint %s" % ep_name

        rc = self.ssh.run("endpoint-modify --myproxy-server=%s %s" % (myproxy_hostname, ep_name), exception_on_error=False)
        if rc != 0:
            raise GlobusOnlineException, "Could not set MyProxy server for endpoint %s" % ep_name

        if public:
            rc = self.ssh.run("endpoint-modify --public %s" % (ep_name), exception_on_error=False)
            if rc != 0:
                raise GlobusOnlineException, "Could not make endpoint %s public" % ep_name






        
        
