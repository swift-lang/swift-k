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
## RECIPE: NFS Server
##
## Set up a domain's NFS server and its shared directories.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

subnet = nil

# Install the NFS server package
package "nfs-kernel-server" do
  action :install
end


# Configuration file with fixed port
cookbook_file "/etc/default/nfs-kernel-server" do
  source "nfs-kernel-server"
  mode 0644
  owner "root"
  group "root"
end

# Set configuration options for NFSv4
cookbook_file "/etc/default/nfs-common" do
  source "nfs-common"
  mode 0644
  owner "root"
  group "root"
end

template "/etc/hosts.allow" do
  source "hosts.denyallow.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet,
    :type => :allow
  )
end

template "/etc/hosts.deny" do
  source "hosts.denyallow.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet,
    :type => :deny
  )
end

# Create directories

# Home directories
directory "/nfs/home" do
  owner "root"
  group "root"
  mode "0755"
  recursive true
  action :create
end

# Scratch directory
# This is a kludge: it assumes that ephemeral storage will be mounted
# on /mnt. If it is not, the recipe should still work since /mnt
# has to be empty, but keeping the scratch directory there is not ideal. 
# A more general-purpose solution would be preferable (ideally by
# specifying these shared directories in the topology)
directory "/mnt/scratch" do
  owner "root"
  group "root"
  mode 01777
  recursive true
  action :create
end

link "/nfs/scratch" do
  to "/mnt/scratch"
end

# Software directories
directory "/nfs/software" do
  owner "root"
  group "root"
  mode "0755"
  recursive true
  action :create
end

# /nfs/software/bin will be in every user's $PATH
# For an executable in /nfs/software to be in the user's PATH,
# the corresponding recipe should create a symbolic link from
# /nfs/software/bin to the executable
directory "/nfs/software/bin" do
  owner "root"
  group "root"
  mode "0755"
  recursive true
  action :create
end

# Add exports
template "/etc/exports" do
  source "exports.erb"
  mode 0644
  owner "root"
  group "root"
  variables(
    :subnet => subnet
  )
  notifies :restart, "service[nfs-kernel-server]"
  notifies :run, "execute[nfs services restart]"
end


# Restart NFS
service "nfs-kernel-server"

execute "nfs services restart" do
  user "root"
  group "root"
  action :nothing
  case node.platform
    when "debian"
      command "/etc/init.d/nfs-common restart"
    when "ubuntu"
      command "service statd --full-restart; service idmapd --full-restart"
  end
end
