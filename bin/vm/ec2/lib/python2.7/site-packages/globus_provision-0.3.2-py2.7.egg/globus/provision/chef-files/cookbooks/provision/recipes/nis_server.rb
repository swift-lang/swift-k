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
## RECIPE: NIS Server
##
## Set up an domain's NIS server.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

subnet = nil

# Packages we need

package "nis"
package "portmap"


# Only allow access to the nodes in that domain's subnet

template "/etc/hosts.allow" do
  source "hosts.denyallow.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet,
    :type => :allow
  )
end

template "/etc/hosts.deny" do
  source "hosts.denyallow.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet,
    :type => :deny
  )
end

cookbook_file "/etc/default/nis" do
  source "nis"
  mode 0644
  owner "root"
  group "root"
  notifies :restart, "service[nis]"
  notifies :run, "execute[ypinit]"
end

file "/etc/yp.conf" do
  owner "root"
  mode "0644"
  content "domain grid.example.org server #{gp_node[:hostname]}"
  notifies :restart, "service[nis]"
  notifies :run, "execute[ypinit]"
end  

template "/etc/ypserv.securenets" do
  source "ypserv.securenets.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet
  )
  notifies :run, "execute[ypinit]"
end


# Restart services so the changes take effect.

execute "ypinit" do
 user "root"
 group "root"
 command "echo | /usr/lib/yp/ypinit -m"
 action :nothing
end

service "nis"

execute "update-rc.d nis defaults" do
  user "root"
  group "root"
end  
