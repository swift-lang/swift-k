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
Globus Provision is a tool for deploying fully-configured Globus systems on Amazon EC2

See http://globus.org/provision/ for more details
"""

VERSION="0.3"
RELEASE="0.3.2"
AMI={"us-east-1":
        {"32-bit": "ami-4f35f826",
         "64-bit": "ami-375d905e",
         "HVM": "ami-0b5d9062"}
     }
