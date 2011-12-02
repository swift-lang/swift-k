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
Miscellaneous utility functions.
"""

import glob
import os

from os import environ

from boto.ec2.connection import EC2Connection,RegionInfo
from boto import connect_ec2
       

def create_ec2_connection(hostname = None, path = None, port = None):
    if hostname == None:
        # We're using EC2.
        # Check for AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY,
        # and use EC2Connection. boto will fill in all the values
        if not (environ.has_key("AWS_ACCESS_KEY_ID") and environ.has_key("AWS_SECRET_ACCESS_KEY")):
            return None
        else:
            return EC2Connection()
    else:
        # We're using an EC2-ish cloud.
        # Check for EC2_ACCESS_KEY and EC2_SECRET_KEY (these are used by Eucalyptus;
        # we will probably have to tweak this further to support other systems)
        if not (environ.has_key("EC2_ACCESS_KEY") and environ.has_key("EC2_SECRET_KEY")):
            return None
        else:
            print "Setting region"
            region = RegionInfo(name="eucalyptus", endpoint=hostname)
            return connect_ec2(aws_access_key_id=environ["EC2_ACCESS_KEY"],
                        aws_secret_access_key=environ["EC2_SECRET_KEY"],
                        is_secure=False,
                        region=region,
                        port=port,
                        path=path)            


def parse_extra_files_files(f):
    l = []
    extra_f = open(f)
    for line in extra_f:
        srcglob, dst = line.split()
        srcs = glob.glob(os.path.expanduser(srcglob))
        srcs = [s for s in srcs if os.path.isfile(s)]
        dst_isdir = (os.path.basename(dst) == "")
        for src in srcs:
            full_dst = dst
            if dst_isdir:
                full_dst += os.path.basename(src)
            l.append( (src, full_dst) )
    return l

def rest_table(col_names, rows):
    def gen_line(lens, char):
        return "+" + char + (char + "+" + char).join([char * l for l in lens]) + char + "+\n"
    
    num_cols = len(col_names)
    len_cols = [0] * num_cols
    height_row = [0] * len(rows)
    
    table = ""
    
    for i, name in enumerate(col_names):
        len_cols[i] = max(len(name), len_cols[i])
        
    for i, row in enumerate(rows):
        for j in range(num_cols):
            lines = row[j].split("\n")
            row_len = max([len(l) for l in lines])
            len_cols[j] = max(row_len, len_cols[j])
            height_row[i] = max(len(lines), height_row[i])
            
    table += gen_line(len_cols, "-")

    table += "|"
    for i, name in enumerate(col_names): 
        table += " "
        table += col_names[i].ljust(len_cols[i])
        table += " |"
    table += "\n"

    table += gen_line(len_cols, "=")
    
    for i, row in enumerate(rows):
        for j in range(height_row[i]):
            table += "|"
            for k, col in enumerate(row): 
                lines = col.split("\n")
                
                if len(lines) < j + 1:
                    col_txt = " " * len_cols[k]
                else:
                    col_txt = lines[j].ljust(len_cols[k])
                
                table += " "
                table += col_txt
                table += " |"
            table += "\n"            

        table += gen_line(len_cols, "-")
    
    return table

# From http://stackoverflow.com/questions/36932/whats-the-best-way-to-implement-an-enum-in-python
def enum(*sequential, **named):
    enums = dict(zip(sequential, range(len(sequential))), **named)
    return type('Enum', (), enums)


