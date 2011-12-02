# -------------------------------------------------------------------------- #
# Copyright 2006-2009, 2011 University of Chicago                            #
# Copyright 2008-2009, Distributed Systems Architecture Group, Universidad   #
# Complutense de Madrid (dsa-research.org)                                   #
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
A self-documenting, self-validating configuration file parser
(derived from Python's ConfigParser)
"""

import ConfigParser
import textwrap
import os.path
        
OPTTYPE_INT = 0
OPTTYPE_FLOAT = 1
OPTTYPE_STRING = 2
OPTTYPE_BOOLEAN = 3
OPTTYPE_FILE = 4

class ConfigException(Exception):
    """A simple exception class used for configuration exceptions"""
    pass

class Section(object):
    """
    A section in the configuration file
    """
    
    def __init__(self, name, required, multiple = None, required_if=None, doc=None):
        self.name = name
        self.required = required
        self.required_if = required_if
        self.multiple = multiple
        self.doc = doc
        self.options = {}
        
    def get_doc(self):
        return textwrap.dedent(self.doc).strip()


class Option(object):
    """
    An option in a section
    """
    
    def __init__(self, name, getter, type, required, required_if=None, default=None, valid=None, doc=None):
        self.name = name
        self.getter = getter
        self.type = type
        self.required = required
        self.required_if = required_if
        self.default = default
        self.valid = valid
        self.doc = doc
        
    def get_doc(self):
        return textwrap.dedent(self.doc).strip()

class Config(object):
    """
    A configuration file
    """
    
    def __init__(self, config_file, sections):
        self.config = ConfigParser.ConfigParser()
        self.config.readfp(open(config_file, "r"))        

        self.sections = sections
        self._options = {}
        
        self.__load_all()
        
    def __load_all(self):
        multi_sections = [s for s in self.sections if s.multiple != None]
        required_sections = [s for s in self.sections if s.required]
        conditional_sections = [s for s in self.sections if not s.required and s.required_if != None]
        optional_sections = [s for s in self.sections if not s.required and s.required_if == None]
        
        sections = multi_sections + required_sections + conditional_sections + optional_sections
        
        for sec in sections:
            if sec.multiple != None:
                valid = self.config.get(sec.multiple[0],sec.multiple[1]).split()
                for v in valid:
                    secname = "%s-%s" % (sec.name, v)
                    if self.config.has_section(secname):
                        for opt in sec.options:
                            self.__load_option(sec, secname, opt, True, multiname = v)                        
            else:
                has_section = self.config.has_section(sec.name)
            
                # If the section is required, check if it exists
                if sec.required and not has_section:
                    raise ConfigException, "Required section [%s] not found" % sec.name
                
                # If the section is conditionally required, check that
                # it meets the conditions
                if sec.required_if != None:
                    for req in sec.required_if:
                        (condsec,condopt) = req[0]
                        condvalue = req[1]
                        
                        if self.config.has_option(condsec,condopt) and self.config.get(condsec,condopt) == condvalue:
                            if not has_section:
                                raise ConfigException, "Section '%s' is required when %s.%s==%s" % (sec.name, condsec, condopt, condvalue)
                        
                # Load options
                for opt in sec.options:
                    self.__load_option(sec, sec.name, opt, has_section)

    
    def __load_option(self, sec, secname, opt, has_section, multiname = None):
        # Load a single option
        optname = opt.name
        
        has_option = self.config.has_option(secname, optname)
        
        if not has_option:
            if has_section:
                if opt.required:
                    raise ConfigException, "Required option '%s.%s' not found" % (secname, optname)
                if opt.required_if != None:
                    for req in opt.required_if:
                        (condsec,condopt) = req[0]
                        condvalue = req[1]
                        
                        if self.config.has_option(condsec,condopt) and self.config.get(condsec,condopt) == condvalue:
                            raise ConfigException, "Option '%s.%s' is required when %s.%s==%s" % (secname, optname, condsec, condopt, condvalue)
            
            value = opt.default
            if has_section and opt.type == OPTTYPE_FILE and value != None:
                value = os.path.expanduser(value)
                if not os.path.exists(value):
                    raise ConfigException, "File '%s' does not exist (default for '%s.%s')" % (value, secname, optname)            
        else:
            if opt.type == OPTTYPE_INT:
                value = self.config.getint(secname, optname)
            elif opt.type == OPTTYPE_FLOAT:
                value = self.config.getfloat(secname, optname)
            elif opt.type == OPTTYPE_STRING:
                value = self.config.get(secname, optname)
            elif opt.type == OPTTYPE_BOOLEAN:
                value = self.config.getboolean(secname, optname)
            elif opt.type == OPTTYPE_FILE:
                value = os.path.expanduser(self.config.get(secname, optname))
                if not os.path.exists(value):
                    raise ConfigException, "File '%s' does not exist (specified for '%s.%s')" % (value, secname, optname)
                
            if opt.valid != None:
                if not value in opt.valid:
                    raise ConfigException, "Invalid value '%s' specified for '%s.%s'. Valid values are %s" % (value, secname, optname, opt.valid)
        
        if sec.multiple != None:
            self._options[(multiname,opt.getter)] = value
        else:
            self._options[opt.getter] = value
        
    def get(self, opt):
        return self._options[opt]
    
    def has(self, opt):
        return self._options.has_key(opt)    
        
    @classmethod
    def from_file(cls, configfile):
        file = open (configfile, "r")
        c = ConfigParser.ConfigParser()
        c.readfp(file)
        cfg = cls(c)
        return cfg

        
        