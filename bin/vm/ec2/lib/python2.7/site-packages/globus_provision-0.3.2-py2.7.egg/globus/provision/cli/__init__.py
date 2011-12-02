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
from globus.provision.core.topology import Topology, Node
import tempfile
from globus.provision.common.threads import SIGINTWatcher

"""
The CLI: A console frontend to Globus Provision that allows a user to request instances, 
start them, etc.
"""

import subprocess
from optparse import OptionParser, OptionGroup
import os
import os.path
import getpass
import colorama
from colorama import Fore, Style
from globus.provision.common import defaults
from globus.provision.common import log
from globus.provision import RELEASE

class Command(object):
    """Base class for all Globus Provisioning commands"""
    
    def __init__(self, argv, root = False, disable_sigintwatch = False):
        
        if root:
            if getpass.getuser() != "root":
                print "Must run as root"
                exit(1)
                
        self.argv = argv
        self.optparser = OptionParser(version = RELEASE)
        self.opt = None
        self.args = None
        
        common_opts = OptionGroup(self.optparser, "Common options", "These options are common to all Globus Provision commands")
        self.optparser.add_option_group(common_opts)
        
        common_opts.add_option("-v", "--verbose", 
                                  action="store_true", dest="verbose", 
                                  help = "Produce verbose output.")

        common_opts.add_option("-d", "--debug", 
                                  action="store_true", dest="debug", 
                                  help = "Write debugging information. Implies -v.")     

        common_opts.add_option("-i", "--instances-dir", 
                                  action="store", type="string", dest="dir", 
                                  default = defaults.INSTANCE_LOCATION,
                                  help = "Use this directory to store information about the instances "
                                         "(instead of the default ~/.globusprovision/instances/)")
        
        colorama.init(autoreset = True)
        
        if not disable_sigintwatch:
            SIGINTWatcher(self.cleanup_after_kill)
                

    def parse_options(self):
        opt, args = self.optparser.parse_args(self.argv)

        self.opt = opt
        self.args = args
        
        if self.opt.debug:
            loglevel = 2
        elif self.opt.verbose:
            loglevel = 1
        else:
            loglevel = 0
            
        log.init_logging(loglevel)
        
    def _run(self, cmd, exit_on_error=True, silent=True):
        if silent:
            devnull = open("/dev/null")
        cmd_list = cmd.split()
        if silent:
            retcode = subprocess.call(cmd_list, stdout=devnull, stderr=devnull)
        else:
            retcode = subprocess.call(cmd_list)
        if silent:
            devnull.close()
        if retcode != 0 and exit_on_error:
            print "Error when running %s" % cmd
            exit(1)        
        return retcode
    
    def _check_exists_file(self, filename):
        if not os.path.exists(filename):
            print "File %s does not exist" % filename
            exit(1)
            
    def _print_error(self, what, reason):
        print colorama.Fore.RED + colorama.Style.BRIGHT + " \033[1;31mERROR\033[0m",
        print ": %s" % what
        print colorama.Fore.WHITE + colorama.Style.BRIGHT + "\033[1;37mReason\033[0m",
        print ": %s" % reason
        
    def _colorize_topology_state(self, state):
        state_str = Topology.state_str[state]
        reset = Fore.RESET + Style.RESET_ALL
        if state == Topology.STATE_NEW:
            return Fore.BLUE + Style.BRIGHT + state_str + reset
        elif state == Topology.STATE_RUNNING:
            return Fore.GREEN + Style.BRIGHT + state_str + reset
        elif state in (Topology.STATE_STARTING, Topology.STATE_CONFIGURING, Topology.STATE_STOPPING, Topology.STATE_RESUMING, Topology.STATE_TERMINATING):
            return Fore.YELLOW + Style.BRIGHT + state_str + reset
        elif state in (Topology.STATE_TERMINATED, Topology.STATE_FAILED):
            return Fore.RED + Style.BRIGHT + state_str + reset
        elif state == Topology.STATE_STOPPED:
            return Fore.MAGENTA + Style.BRIGHT + state_str + reset
        else:
            return state_str

    def _colorize_node_state(self, state):
        state_str = Node.state_str[state]
        reset = Fore.RESET + Style.RESET_ALL
        if state == Node.STATE_NEW:
            return Fore.BLUE + Style.BRIGHT + state_str + reset
        elif state == Node.STATE_RUNNING:
            return Fore.GREEN + Style.BRIGHT + state_str + reset
        elif state in (Node.STATE_STARTING, Node.STATE_RUNNING_UNCONFIGURED, Node.STATE_CONFIGURING, Node.STATE_RECONFIGURING, Node.STATE_STOPPING, Node.STATE_STOPPING_CONFIGURING, Node.STATE_STOPPING_CONFIGURED, Node.STATE_RESUMING, Node.STATE_RESUMED_UNCONFIGURED, Node.STATE_RESUMED_RECONFIGURING, Node.STATE_TERMINATING):
            return Fore.YELLOW + Style.BRIGHT + state_str + reset
        elif state in (Node.STATE_TERMINATED, Node.STATE_FAILED):
            return Fore.RED + Style.BRIGHT + state_str + reset
        elif state == Node.STATE_STOPPED:
            return Fore.MAGENTA + Style.BRIGHT + state_str + reset
        else:
            return state_str
        
    def _pad(self, str, colorstr, width):
        if colorstr == "":
            return str.ljust(width)
        else:
            return colorstr + " " * (width - len(str))        
        
    def _set_last_gpi(self, gpi):
        try:
            uid = os.getuid()
            ppid = os.getppid()
    
            gpi_dir = "%s/.globusprovision-%i" % (tempfile.gettempdir(), uid)
            gpi_file = "%s/%i" % (gpi_dir, ppid) 
            
            if not os.path.exists(gpi_dir):
                os.mkdir(gpi_dir, 0700)
                
            f = open(gpi_file, "w")
            f.write(gpi)
            f.close()
        except Exception, e:
            # Saving the last GPI is just for the benefit of auto-completion.
            # If it doesn't work, worse that will happen is that the user
            # won't be able to autocomplete the last GPI
            pass
        
    def cleanup_after_kill(self):
        print "Globus Provision has been unexpectedly killed and may have left resources"
        print "in an unconfigured state. Use gp-instance-terminate to release resources."
        