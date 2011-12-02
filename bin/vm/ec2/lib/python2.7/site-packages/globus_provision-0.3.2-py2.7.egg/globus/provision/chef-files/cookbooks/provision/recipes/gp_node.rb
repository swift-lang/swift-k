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
## RECIPE: Globus Provision common actions
##
## This recipe performs actions that are common to all Globus Provision nodes.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

# Copy the hosts file
cookbook_file "/etc/hosts" do
  source "hosts"
  mode 0644
  owner "root"
  group "root"
end

# Create a BASH profile file with Globus Provision variables
file "/etc/profile.d/globusprovision" do
  mode 0644
  owner "root"
  group "root"
  content "export MYPROXY_SERVER=#{gp_domain[:myproxy_server]}"
end

# Add passwordless access to members of the gp-admins group
execute "add_sudoers" do
  line = "%gp-admins ALL=NOPASSWD: ALL"
  only_if do
    File.read("/etc/sudoers").index(line).nil?
  end  
  user "root"
  group "root"
  command "echo \"#{line}\" >> /etc/sudoers"
  action :run
end

