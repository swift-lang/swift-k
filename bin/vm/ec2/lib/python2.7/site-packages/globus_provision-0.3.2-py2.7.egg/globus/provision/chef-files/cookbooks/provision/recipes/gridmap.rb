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
## RECIPE: Gridmap file
##
## This recipe creates a gridmap file with the entries specified in the topoloy.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

# Create grid-security directory.
directory "/etc/grid-security" do
  owner "root"
  group "root"
  mode "0755"
  action :create
end

# If it does not exist, create an empty gridmap file.
file "/etc/grid-security/grid-mapfile" do
  owner "root"
  group "root"
  mode "0644"
  action :create
end

# Create gridmap
# Note: Will be regenerated from scratch on subsequent runs of Chef.
# TODO: Read in existing gridmap, and merge it with provided one (shouldn't be hard
# to do, but not necessary right now)
gridmap = gp_domain[:gridmap].to_a
template "/etc/grid-security/grid-mapfile" do
  source "gridmap.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :gridmap => gridmap
  )
end
