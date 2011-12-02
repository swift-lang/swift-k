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
## RECIPE: NFS client
##
## Set up node so it will have access to its domain's NFS server.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

# The nfs_server attribute is part of the generated topology.rb file,
# and contains the IP of the domain's NFS server.
server = gp_domain[:nfs_server_ip]


# Packages we need

package "nfs-common"
package "autofs"

# Set configuration options for NFSv4
cookbook_file "/etc/default/nfs-common" do
  source "nfs-common"
  mode 0644
  owner "root"
  group "root"
  notifies :run, "execute[nfs services restart]", :immediately
end


# Set up the home directories so they will be automounted.

if ! File.exists?("/nfs")
	# Create the directory where the NFS directories will be mounted
	directory "/nfs" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	  recursive true
	end
	
	# Create the directory where home directories will be mounted
	directory "/nfs/home" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	  recursive true
	end
	
	# Create the directory where scratch directory will be mounted
	directory "/nfs/scratch" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	  recursive true
	end
	
	# Create the directory where software directory will be mounted
	directory "/nfs/software" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	  recursive true
	end		
end


cookbook_file "/etc/auto.master" do
  source "auto.master"
  mode 0644
  owner "root"
  group "root"
end

template "/etc/auto.nfs" do
  source "auto.nfs.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :server => server
  )
  notifies :restart, "service[autofs]", :immediately  
end

execute "nfs services restart" do
  user "root"
  group "root"
  action :nothing
  case node.platform
    when "debian"
      command "/etc/init.d/nfs-common restart"
    when "ubuntu"
      command "service idmapd --full-restart"
  end
end

service "autofs"

# Add /nfs/software/bin to everyone's environment (we do this in /etc/enviroment
# instead of /etc/profile.d/ (which is BASH-specific) because daemons started by
# init scripts don't necessarily load BASH environment information.
# Note that if this file is modified and /nfs/software/bin is removed from the path,
# subsequent runs of Chef will replace it will a file with just the PATH variable
file "/etc/environment" do
  only_if do
    File.read("/etc/environment").index(/PATH=.*\/nfs\/software\/bin.*/).nil?
  end
  owner "root"
  mode "0644"
  content "PATH=\"/nfs/software/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games\"\n"
end  
