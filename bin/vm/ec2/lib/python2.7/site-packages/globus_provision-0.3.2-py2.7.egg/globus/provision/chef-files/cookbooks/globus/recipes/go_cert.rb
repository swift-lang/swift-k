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
## RECIPE: GO CA certificates
##
## This recipe installs the certificates of CAs that sign the various GO
## certificates (user certificates, the Transfer API server, etc.)
##
## This recipe must be applied to any node that will be interacting with 
## a GO service.
## 
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Create the grid-security directory

directory "/etc/grid-security" do
  owner "root"
  group "root"
  mode "0755"
  action :create
end

directory "/etc/grid-security/certificates" do
  owner "root"
  group "root"
  mode "0755"
  action :create
end

cookbook_file "/etc/grid-security/certificates/d1b603c3.0" do
  source "d1b603c3.0"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/certificates/d1b603c3.signing_policy" do
  source "d1b603c3.signing_policy"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/certificates/4396eb4d.0" do
  source "4396eb4d.0"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/certificates/4396eb4d.signing_policy" do
  source "4396eb4d.signing_policy"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/certificates/gd-bundle_ca.cert" do
  source "gd-bundle_ca.cert"
  mode 0644
  owner "root"
  group "root"
end
