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

"""
This is the core package of Globus Provision. The core is in charge of managing
instances (creating, starting, stopping, etc.). Although the core orchestrates 
the deployment of instances, it does not have any infrastructure-specific code. 
In other words, the core knows how to start an instance but, when it reaches a 
point where an infrastructure-specific action has to be taken (e.g., "start an 
EC2 instance"), it delegates that task to the appropriate deployer (in the
globus.provision.deploy package). 

The core provides an API that is used by the Command-Line Interface, 
and could potentially be used by other frontends.
"""