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
## RECIPE: Condor worker node
##
## Set up a Condor worker node.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]

# The "condor" recipe handles actions that are common to
# both head and worker nodes.
include_recipe "condor::condor"


# The lrm_head attribute is part of the generated topology.rb file,
# and contains the FQDN of the head node.
server = gp_domain[:lrm_head]


# Domain (used by Condor for authorization). 
# This should eventually be included in the topology.
domain = server[server.index(".")+1, server.length]


# Create the local configuration file.
template "/etc/condor/condor_config.local" do
  source "condor_config.erb"
  mode 0644
  owner "condor"
  group "condor"
  variables(
    :server => server,
    :domain => domain,    
    :daemons => "MASTER, STARTD"
  )
  notifies :restart, "service[condor]"
end

service "condor" 

