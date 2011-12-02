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
## RECIPE: Globus Toolkit 5.1.1 repository
##
## Adds the APT sources list for the Globus Toolkit 5.1.1
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


case node.platform
  when "ubuntu"
    if node.platform_version == "11.04"
      distro_id = "natty"
    elsif node.platform_version == "10.10"
      distro_id = "maverick"
    elsif node.platform_version == "10.04"
      distro_id = "lenny"
    end

  when "debian"
    if node.platform_version.to_f >= 6.0
      distro_id = "squeeze"
    elsif node.platform_version.to_f >= 5.0
      distro_id = "lenny"
    end
    
end

remote_file "#{node[:scratch_dir]}/gt5_repository.deb" do
  action :create_if_missing 
  source "http://www.globus.org/ftppub/gt5/5.1/5.1.1/installers/repo/globus-repository-#{distro_id}_0.0.1_all.deb"
  owner "root"
  group "root"    
  mode "0644"
end

package "gt5_repository" do
  action :install
  source "#{node[:scratch_dir]}/gt5_repository.deb"
  provider Chef::Provider::Package::Dpkg
  notifies :run, "execute[apt-get update]", :immediately
end

execute "apt-get update" do
 user "root"
 group "root"
 command "apt-get update"
 action :nothing
end
