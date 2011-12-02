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
## RECIPE: Domain users
##
## This recipe creates the users in a domain.
##
## This recipe will work both on a node that has the ``nfs_server`` and/or
## ``nis_server`` recipes on it (in which case global accounts will be
## created) and on a node that is not an NFS/NIS server (in which case,
## local accounts will be created).
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

# Necessary to create users
package "libshadow-ruby1.8" do
  action :install
end

# Create the "Globus Provision Admins" group.
# Users in this group have passwordless sudo access
# on all nodes.
group "gp-admins" do
  gid 3000
end


# The :users attribute is part of the generated topology.rb file,
# and contains information on a domain's users (username,
# password, etc.)
users = gp_domain[:users].to_hash

if gp_domain[:nfs_server]
    homedirs = "/nfs/home"
else
    homedirs = "/home"
end


# We start by creating the domain's users.
users.values.each do |u|
	# Create the user
	user u[:id] do
	  not_if "id #{u[:id]}"
	  comment u[:description]
	  gid 100
	  home "#{homedirs}/#{u[:id]}"
	  password u[:password_hash]
	  shell "/bin/bash"
	  supports :manage_home => true
	  notifies :run, "execute[rebuild_yp]"
	end

	auth_keys = "#{homedirs}/#{u[:id]}/.ssh/authorized_keys"
	key_file = "#{homedirs}/#{u[:id]}/.ssh/id_rsa"
	pkey_file = key_file+".pub"

  # Create passwordless SSH key
  execute "ssh-keygen" do
    not_if do File.exists?(key_file) end
    user u[:id]
    command "ssh-keygen -N \"\" -f #{key_file}"
    action :run
  end
		
  file auth_keys do
    owner u[:id]
    mode "0644"
    action :create
  end  
    
	# Create the authorized_keys file.
  execute "add_pkey" do
    only_if do
        pkey = File.read(pkey_file)
        File.read(auth_keys).index(pkey).nil?
    end  
    user "root"
    group "root"
    command "cat #{pkey_file} >> #{auth_keys}"
    action :run
  end  
  
  execute "add_topology_pkey" do
    only_if do
      u[:ssh_pkey] and File.read(auth_keys).index(u[:ssh_pkey]).nil?
    end  
    user "root"
    group "root"
    command "echo #{u[:ssh_pkey]} >> #{auth_keys}"
    action :run
  end    
  
  group "gp-admins" do
    only_if do u[:admin] end
    members [u[:id]]
    append true
    action :modify
  end

end

# If we specified that this domain's users will use certificates
# for authentication, then we need to copy the certificate and key
# into their .globus directory.
users.values.select{|u| u[:certificate] == "generated"}.each do |u|
	directory "#{homedirs}/#{u[:id]}/.globus" do
	  owner u[:id]
	  group "users"
	  mode "0755"
	  action :create
	end

	cookbook_file "#{homedirs}/#{u[:id]}/.globus/usercert.pem" do
	  source "#{u[:id]}_cert.pem"
	  mode 0644
	  owner u[:id]
	  group "users"
	end

	cookbook_file "#{homedirs}/#{u[:id]}/.globus/userkey.pem" do
	  source "#{u[:id]}_key.pem"
	  mode 0400
	  owner u[:id]
	  group "users"
	end
end

# We need to run this for changes to take effect in the NIS server.
execute "rebuild_yp" do
 only_if do gp_domain[:nis_server] end
 user "root"
 group "root"
 command "make -C /var/yp"
 action :nothing
end
