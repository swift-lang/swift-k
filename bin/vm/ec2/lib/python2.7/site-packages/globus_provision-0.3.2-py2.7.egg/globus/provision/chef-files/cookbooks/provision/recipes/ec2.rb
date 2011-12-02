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
## RECIPE: EC2 AMI software pre-install
##
## This recipe preinstalls a subset of the software used on a Globus Provision
## instance to speed up subsequent deployments.
##
##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package "libshadow-ruby1.8"
package "nis"
package "portmap"
package "nfs-common"
package "autofs"
package "xinetd"
package "libssl0.9.8"

include_recipe "globus::client-tools"
package "globus-simple-ca"
package "myproxy-server"
package "globus-gridftp-server-progs"
package "libglobus-xio-gsi-driver-dev"

include_recipe "condor::condor"
include_recipe "java"

