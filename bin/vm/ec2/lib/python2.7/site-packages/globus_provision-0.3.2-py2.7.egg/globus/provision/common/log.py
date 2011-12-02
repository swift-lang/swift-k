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
Convenience functions around the logging module
"""

import logging
import os.path

def init_logging(level):
    if level == 2:
        level = logging.DEBUG
    elif level == 1:
        level = logging.INFO
    else:
        level = logging.WARNING
    logging.getLogger('boto').setLevel(logging.CRITICAL)
    logging.getLogger('paramiko').setLevel(logging.CRITICAL)
    
    l = logging.getLogger("globusprovision")
    l.setLevel(logging.DEBUG)
    
    fh = logging.StreamHandler()
    fh.setLevel(level)
    formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')
    fh.setFormatter(formatter)
    l.addHandler(fh)        

def set_logging_instance(instance):
    l = logging.getLogger("globusprovision")
    fh = logging.FileHandler(os.path.expanduser('%s/deploy.log' % instance.instance_dir))
    fh.setLevel(logging.DEBUG)
    formatter = logging.Formatter('%(asctime)s - %(message)s')
    fh.setFormatter(formatter)
    l.addHandler(fh)    

def log(msg, func, node):
    if node != None:
        msg = "%s - %s" % (node.id, msg)
    func(msg)

def debug(msg, node = None):
    log(msg, logging.getLogger('globusprovision').debug, node)

def warning(msg, node = None):
    log(msg, logging.getLogger('globusprovision').warning, node)
    
def info(msg, node = None):
    log(msg, logging.getLogger('globusprovision').info, node)
