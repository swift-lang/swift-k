# -------------------------------------------------------------------------- #
# Copyright 2010-2011, University of Chicago                                      #
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

##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
##
## RECIPE: NIS client
##
## Set up node so it will have access to its domain's NIS server, allowing
## domain users to log into it.
##
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

# The nis_server attribute is part of the generated topology.rb file,
# and contains the IP of the domain's NFS/NIS server.
server = gp_domain[:nis_server_ip]


# Packages we need

package "nis"
package "portmap"


# Modify various configuration files to enable access to the NIS server.

execute "add_passwd_entry" do
  only_if do
    File.read("/etc/passwd").index("+::::::").nil?
  end  
  user "root"
  group "root"
  command "echo +:::::: >> /etc/passwd"
  action :run
  notifies :restart, "service[nis]"
end

execute "add_shadow_entry" do
  only_if do
    File.read("/etc/shadow").index("+::::::::").nil?
  end  
  user "root"
  group "root"
  command "echo +:::::::: >> /etc/shadow"
  action :run
  notifies :restart, "service[nis]"
end

execute "add_group_entry" do
  only_if do
    File.read("/etc/group").index("+:::").nil?
  end  
  user "root"
  group "root"
  command "echo +::: >> /etc/group"
  action :run
  notifies :restart, "service[nis]"
end

file "/etc/yp.conf" do
  owner "root"
  mode "0644"
  content "ypserver #{server}"
  notifies :restart, "service[nis]", :immediately
end  

# Restart NIS
service "nis"

execute "update-rc.d nis defaults" do
  user "root"
  group "root"
end  
