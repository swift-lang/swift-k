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
## RECIPE: SimpleCA
##
## This recipe installs the CA certificate and key so the node can use SimpleCA
## commands to sign certificate requests.
##
## Note that, instead of using grid-create-ca, we set up all the necessary files 
## manually. This is necessary since the CA certificate already exists when the
## recipes are run (it is either created by Globus Provision or provided 
## explicitly by the user), and we need to install that specific CA.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if ! File.exists?(node[:globus][:simpleCA] )

  require "openssl"
  
  r = cookbook_file "#{node[:scratch_dir]}/gp-ca-cert.pem" do
    source "ca_cert.pem"
    mode 0644
    owner "root"
    group "root"
    action :nothing
  end
  
  r.run_action(:create)
  
  node.default[:ca_cert] = OpenSSL::X509::Certificate.new(File.read("#{node[:scratch_dir]}/gp-ca-cert.pem"))
  node.default[:ca_cert_hash] = "%08x" % node.default[:ca_cert].subject.hash  
  
	# Create the basic directory structure

	directory node[:globus][:simpleCA] do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	  recursive true
	end

	directory "#{node[:globus][:simpleCA]}/certs" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	end

	directory "#{node[:globus][:simpleCA]}/crl" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	end

	directory "#{node[:globus][:simpleCA]}/newcerts" do
	  owner "root"
	  group "root"
	  mode "0755"
	  action :create
	end

	directory "#{node[:globus][:simpleCA]}/private" do
	  owner "root"
	  group "root"
	  mode "0700"
	  action :create
	end


	# Copy the CA certificate and key.
	cookbook_file "#{node[:globus][:simpleCA]}/cacert.pem" do
	  source "ca_cert.pem"
	  mode 0644
	  owner "root"
	  group "root"
	end

	cookbook_file "#{node[:globus][:simpleCA]}/private/cakey.pem" do
	  source "ca_key.pem"
	  mode 0400
	  owner "root"
	  group "root"
	end

  # Various configuration files needed in the CA directory

	template "#{node[:globus][:simpleCA]}/grid-ca-ssl.conf" do
    source "globus-ssl.conf.erb"
    mode 0644
    owner "root"
    group "root"
    variables(
      :certificate => node.default[:ca_cert],
      :type => :ca
    )  
  end

	file "#{node[:globus][:simpleCA]}/index.txt" do
	  owner "root"
	  group "root"
	  mode "0644"
	  action :create
	end

	file "#{node[:globus][:simpleCA]}/serial" do
	  owner "root"
	  group "root"
	  mode "0644"
	  action :create
	  content "01\n"
	end

end



