# -------------------------------------------------------------------------- #
# Copyright 2010-2011, University of Chicago                                 #
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
## RECIPE: GC certificate
##
## 
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

require "openssl"

include_recipe "globus::go_cert"

gp_domain = node[:topology][:domains][node[:domain_id]]
gp_node   = gp_domain[:nodes][node[:node_id]]

cookbook_file "/etc/grid-security/anon.cert" do
  source "anon.cert"
  mode 0644
  owner "root"
  group "root"
end

cookbook_file "/etc/grid-security/anon.key" do
  source "anon.key"
  mode 0400
  owner "root"
  group "root"
end

ruby_block "get_gc_certificate" do
  cert_file = "/etc/grid-security/gc-cert-#{gp_node[:gc_setupkey]}.pem"
  key_file = "/etc/grid-security/gc-key-#{gp_node[:gc_setupkey]}.pem"
  only_if do ! File.exists?(cert_file) end
  block do
    begin
      ENV["X509_USER_CERT"]="/etc/grid-security/anon.cert"
      ENV["X509_USER_KEY"]="/etc/grid-security/anon.key"
      cert_blob = `gsissh -F /dev/null -o "GSSApiTrustDns no" -o "ServerAliveInterval 15" -o "ServerAliveCountMax 8" relay.globusonline.org -p 2223 register #{gp_node[:gc_setupkey]}`

      cert = OpenSSL::X509::Certificate.new(cert_blob)
      cert_f = File.new(cert_file, 'w')
      cert_f.write(cert.to_pem)
      cert_f.chmod(0644)
    
      key = OpenSSL::PKey::RSA.new(cert_blob)
      key_f = File.new(key_file, 'w')
      key_f.write(key.to_pem)
      key_f.chmod(0400)
    rescue
      # TODO: Find a way to communicate to GP that this has happened,
      #       so it can be reported to the user.
      p "Unable to obtain GC certificate"
    end
  end
end
