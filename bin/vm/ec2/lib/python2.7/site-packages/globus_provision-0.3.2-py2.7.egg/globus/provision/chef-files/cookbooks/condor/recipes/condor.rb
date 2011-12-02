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
## RECIPE: Condor common actions
##
## This recipe is a dependency of ``condor_head`` and ``condor_worker``, which will set
## up a Condor head node or worker node. This recipe handles all the actions
## that are common to both.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

case node.platform
when "ubuntu"

  cookbook_file "/etc/init/condor-dir.conf" do
    source "condor-dir.conf"
    mode 0644
    owner "root"
    group "root"
  end
  
end

apt_repository "condor" do
  uri "http://www.cs.wisc.edu/condor/debian/stable/"
  distribution "lenny"
  components ["contrib"]
  action :add
end

package "condor" do
  action :install
  options "--force-yes"  
end

execute "update-rc.d condor defaults" do
  user "root"
  group "root"
end  
