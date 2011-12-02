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
## RECIPE: Galaxy (Globus fork) server
##
## This installs the Globus fork of Galaxy. 
## If Galaxy is being installed on a domain with NFS/NIS, you must have applied
## the galaxy-globus-common recipe to the NFS/NIS server. If you are running
## a standalone Galaxy server, you will need to apply galaxy-globus-common and
## galaxy-globus on the same node.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

include_recipe "postgresql::server"

database_exists = "psql galaxy postgres -c ''"

# Create postgresql user

execute "createuser" do
  user "postgres"
  command "createuser -D -S -R galaxy"
  action :run
  not_if database_exists
end  

execute "createdb" do
  user "postgres"
  command "createdb galaxy"
  action :run
  not_if database_exists
end  

execute "alter_user" do
  user "postgres"
  command "psql -c \"alter user galaxy with encrypted password 'galaxy';\""
  action :run
  not_if database_exists
end  

execute "grant_all" do
  user "postgres"
  command "psql -c \"grant all privileges on database galaxy to galaxy;\""
  action :run
  not_if database_exists
end

case node.platform
  when "ubuntu"
    if node.platform_version.to_f >= 11.04
		package "python2.6"
		execute "update-alternatives" do
		  python_version = `python -c "import sys; print sys.version_info[0] * 10 + sys.version_info[1]"`.to_f
		  only_if do python_version > 26 end
		  user "root"
		  command "update-alternatives --install /usr/bin/python python /usr/bin/python2.6 10"
		  action :run
		end
    end    
end

execute "galaxy-setup.sh" do
  user "galaxy"
  group "galaxy"
  cwd node[:galaxy][:dir]
  command "./galaxy-setup.sh"
  action :run
end  
 
# Add init script
cookbook_file "/etc/init.d/galaxy" do
  source "galaxy.init"
  owner "root"
  group "root"
  mode "0755"
  notifies :run, "execute[update-rc.d]"
end
  
execute "update-rc.d" do
  user "root"
  group "root"
  command "update-rc.d galaxy defaults"
  action :nothing
end  

execute "galaxy_restart" do
 user "root"
 group "root"
 command "/etc/init.d/galaxy restart"
 action :run
 environment ({'PATH' => "/nfs/software/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"}) 
end
