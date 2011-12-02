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
## RECIPE: Galaxy (Globus fork) common actions
##
## This recipe performs common actions required when installing the Globus
## fork of Galaxy. If Galaxy is being installed on a domain with NFS/NIS,
## this recipe must be run on the NFS/NIS server, and the galaxy-globus
## recipe can be run on another node.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

go_endpoints = gp_domain[:go_endpoints].to_a

if go_endpoints.size > 0
	go_endpoint = go_endpoints[0]
	go_endpoint = "#{go_endpoint[:user]}##{go_endpoint[:name]}" 
else
	go_endpoint = ""
end

if gp_domain[:nfs_server]
    homedirs = "/nfs/home"
else
    homedirs = "/home"
end

group "galaxy" do
  gid 4000
end

user "galaxy" do
  comment "Galaxy User"
  uid 4000
  gid 4000
  home "#{homedirs}/galaxy"
  password "!"
  shell "/bin/bash"
  supports :manage_home => true
  notifies :run, "execute[ypinit]"
end

# We need to run this for changes to take effect in the NIS server.
execute "ypinit" do
 only_if do gp_domain[:nis_server] end
 user "root"
 group "root"
 command "make -C /var/yp"
 action :nothing
end

if ! File.exists?(node[:galaxy][:dir])

  directory "#{node[:galaxy][:dir]}" do
    owner "galaxy"
    group "galaxy"
    mode "0755"
    action :create
  end
  
  remote_file "#{node[:scratch_dir]}/galaxy-dist.tip.tar.bz2" do
    source "https://bitbucket.org/steder/galaxy-globus/get/tip.tar.bz2"
    owner "root"
    group "root"    
    mode "0644"
  end

  execute "tar" do
    user "galaxy"
    group "galaxy"
    command "tar xjf #{node[:scratch_dir]}/galaxy-dist.tip.tar.bz2 --strip-components=1 --directory #{node[:galaxy][:dir]}"
    action :run
  end  	

  cookbook_file "#{node[:galaxy][:dir]}/galaxy-setup.sh" do
    source "galaxy-setup.sh"
    owner "galaxy"
    group "galaxy"
    mode "0755"
  end
  
  template "#{node[:galaxy][:dir]}/universe_wsgi.ini" do
    source "galaxy-universe.erb"
    mode 0644
    owner "galaxy"
    group "galaxy"
    variables(
      :db_connect => "postgres:///galaxy?user=galaxy&password=galaxy&host=/var/run/postgresql",
      :go_endpoint => go_endpoint
    )
  end  

end
  
