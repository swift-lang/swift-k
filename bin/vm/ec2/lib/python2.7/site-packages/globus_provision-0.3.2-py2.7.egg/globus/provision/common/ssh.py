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
Remote execution and file transfer via SSH

This module provides an abstraction over the paramiko package.
"""

import sys
import paramiko
import time
import select
import os.path
import traceback
from paramiko.ssh_exception import SSHException

# Try to use our patched version of paraproxy only if
# it is available. If it isn't, ProxyCommand support
# will simply be unavailable
try:
    import dg_paraproxy
except:
    pass

from Crypto.Random import atfork

from globus.provision.common import log
from os import walk

class SSHCommandFailureException(Exception):
    def __init__(self, ssh, command):
        self.ssh = ssh
        self.command = command
        
        
class SSH(object):
    def __init__(self, username, hostname, key_path, default_outf = sys.stdout, default_errf = sys.stderr, port=22):
        self.username = username
        self.hostname = hostname
        self.key_path = key_path
        self.default_outf = default_outf
        self.default_errf = default_errf
        self.port = port
        
    def open(self, timeout = 120):
        key = paramiko.RSAKey.from_private_key_file(self.key_path)
        connected = False
        t_start = time.time()
        while not connected:
            try:
                atfork()
                self.client = paramiko.SSHClient()
                self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
                self.client.connect(self.hostname, self.port, self.username, timeout=5, pkey=key)
                connected = True
            except Exception, e:
                t_now = time.time()
                if t_now - t_start > timeout:
                    raise e
                else:
                    time.sleep(2)

        try:
            self.sftp = self.client.get_transport().open_sftp_client()
        except SSHException, sshe:
            # Some SSH servers, like the GO CLI, are not amenable to SFTP
            log.debug("Unable to create an SFTP client on this connection.")
            self.sftp = None  
        
    def close(self):
        self.client.close()
        
    def run(self, command, outf=None, errf=None, exception_on_error = True, expectnooutput=False):
        channel = self.client.get_transport().open_session()
        
        log.debug("%s - Running %s" % (self.hostname,command))
        
        if outf == None:
            outf = self.default_outf
    
        if errf == None:
            errf = self.default_errf
            
        try:
            channel.exec_command(command)
            if expectnooutput:
                log.debug("Ignoring output from command (not expecting any)")
            else:
                all_out_nbytes = 0
                all_err_nbytes = 0   
                rem_out = ""
                rem_err = ""
                while True:
                    rl, wl, xl = select.select([channel],[],[], 0.1)
                    if len(rl) > 0:
                        out_nbytes, rem_out = self.__recv(outf, channel.recv_ready, channel.recv, "SSH_OUT", rem_out)
                        err_nbytes, rem_err = self.__recv(errf, channel.recv_stderr_ready, channel.recv_stderr, "SSH_ERR", rem_err)

                        if out_nbytes + err_nbytes == 0:
                            break

                        all_out_nbytes += out_nbytes
                        all_err_nbytes += err_nbytes

                if all_out_nbytes == 0:
                    log.debug("Command did not write to standard output.")

                if all_err_nbytes == 0:
                    log.debug("Command did not write to standard error.")
            
            log.debug("%s - Waiting for exit status: %s" % (self.hostname,command))
            rc = channel.recv_exit_status()
            log.debug("%s - Ran %s (exit status: %i)" % (self.hostname,command,rc))
            channel.close()
        except Exception, e:
            raise # Replace by something more meaningful
         
        if exception_on_error and rc != 0:
            raise SSHCommandFailureException(self, command)
        else:
            return rc
    

        
        
    def scp(self, fromf, tof):
        # Create directory if it does not exist
        try:
            self.sftp.stat(os.path.dirname(tof))
        except IOError, e:
            pdirs = get_parent_directories(tof)
            for d in pdirs:
                try:
                    self.sftp.stat(d)
                except IOError, e:
                    self.sftp.mkdir(d)        
        try:
            self.sftp.put(fromf, tof)
        except Exception, e:
            traceback.print_exc()
            try:
                self.close()
            except:
                pass
        log.debug("scp %s -> %s:%s" % (fromf, self.hostname, tof))
        
    def scp_dir(self, fromdir, todir):
        for root, dirs, files in walk(fromdir):
            todir_full = todir + "/" + root[len(fromdir):]
            try:
                self.sftp.stat(todir_full)
            except IOError, e:
                self.sftp.mkdir(todir_full)
            for f in files:
                fromfile = root + "/" + f
                tofile = todir_full + "/" + f
                self.sftp.put(fromfile, tofile)
                log.debug("scp %s -> %s:%s" % (fromfile, self.hostname, tofile))
                
    def __recv(self, f, ready_func, recv_func, log_label, rem):
        nbytes = 0
        while ready_func():
            data = recv_func(4096)
            if len(data) > 0:
                nbytes += len(data)
                
                if f is not None: 
                    f.write(data)

                lines = data.split('\n')

                if len(lines) == 1:
                    rem += lines[0]
                else:
                    log.debug(log_label + ": %s" % (rem + lines[0]))
                    for line in lines[1:-1]:
                        log.debug(log_label + ": %s" % line)
                    rem = lines[-1]
                
        if f is not None: f.flush()
        
        return nbytes, rem
    

def get_parent_directories(filepath):
    dir = os.path.dirname(filepath)
    dirs = [dir]
    while dir != "/":
        dir = os.path.dirname(dir)
        dirs.append(dir)
    dirs.reverse()
    return dirs
    