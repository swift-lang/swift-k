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
## RECIPE: Host certificate
##
## Adds a host certificate to the node.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Create grid-security directory
directory "/etc/grid-security" do
  owner "root"
  group "root"
  mode "0755"
  action :create
end


# Copy the certificate and key.

cookbook_file "/etc/grid-security/hostcert.pem" do
  source "#{node[:node_id]}_cert.pem"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/hostkey.pem" do
  source "#{node[:node_id]}_key.pem"
  mode 0400
  owner "root"
  group "root"
end

