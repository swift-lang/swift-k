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
## RECIPE: Globus Toolkit 5.1.1 MyProxy
##
## Installs a MyProxy server, and sets is up as a xinetd service.
##
## For authentication, the MyProxy server will use the local UNIX accounts
## through PAM. If the node this recipe is applied to is part of a NIS
## domain, then the global NIS accounts will be used.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

include_recipe "globus::repository"

package "xinetd"
package "globus-simple-ca"
package "myproxy-server"

directory "/var/lib/myproxy" do
  owner "root"
  group "root"
  mode "0700"
  action :create
end


cookbook_file "/etc/myproxy-server.config" do
  source "myproxy-server.config"
  mode 0644
  owner "root"
  group "root"
end

template "/var/lib/myproxy/myproxy-certificate-mapapp" do
  source "myproxy-dnmap.erb"
  mode 0744
  owner "root"
  group "root"
end

cookbook_file "/etc/xinetd.d/myproxy" do
  source "xinetd.myproxy"
  mode 0644
  owner "root"
  group "root"
  notifies :restart, "service[xinetd]"
end

execute "add_services_entry" do
  line = "myproxy-server  7512/tcp                        # Myproxy server"
  only_if do
    File.read("/etc/services").index(line).nil?
  end  
  user "root"
  group "root"
  command "echo \"#{line}\" >> /etc/services"
  action :run
  notifies :restart, "service[xinetd]"
end

service "xinetd"
