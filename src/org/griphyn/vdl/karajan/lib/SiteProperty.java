/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.swift.catalog.site.SwiftContact;
import org.globus.swift.catalog.types.Os;
import org.globus.swift.catalog.types.SysInfo;

public class SiteProperty extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(SiteProperty.class);
    
    private ArgRef<SwiftContact> host;
    private ArgRef<String> name;
    private ArgRef<Object> _default;
    private ChannelRef<Object> cr_vargs;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("host", "name", optional("default", null)), returns(channel("...", 1)));
    }

	public Object function(Stack stack) throws ExecutionException {
		SwiftContact bc = host.getValue(stack);
		return getSingle(bc, name.getValue(stack), _default.getValue(stack));
	}
	
	public static final Os DEFAULT_OS = Os.LINUX;
	
	public static final String SWIFT_WRAPPER_INTERPRETER = "wrapperInterpreter";
	public static final String SWIFT_WRAPPER_INTERPRETER_OPTIONS = "wrapperInterpreterOptions";
	public static final String SWIFT_WRAPPER_SCRIPT = "wrapperScript";
	public static final String SWIFT_CLEANUP_COMMAND = "cleanupCommand";
	public static final String SWIFT_CLEANUP_COMMAND_OPTIONS = "cleanupCommandOptions";
	public static final String SYSINFO_OS = "OS";
	
	private static final Map<Os, Map<String, Object>> DEFAULTS;
	private static final Set<String> DEFAULTS_NAMES; 
	
	private static void addDefault(Os os, String name, Object value) {
		DEFAULTS_NAMES.add(name);
		Map<String, Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			osm = new HashMap<String, Object>();
			DEFAULTS.put(os, osm);
		}
		osm.put(name, value);
	}
	
	private static boolean hasDefault(Os os, String name) {
	    Map<String, Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			return false;
		}
		else {
			return osm.containsKey(name);
		}
	}
	
	private static Object getDefault(Os os, String name) {
	    Map<String, Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			osm = DEFAULTS.get(null);
		}
		return osm.get(name);
	}
	
	static {
		DEFAULTS = new HashMap<Os, Map<String, Object>>();
		DEFAULTS_NAMES = new HashSet<String>();
		addDefault(Os.WINDOWS, SWIFT_WRAPPER_INTERPRETER, "cscript.exe");
		addDefault(Os.WINDOWS, SWIFT_WRAPPER_SCRIPT, "_swiftwrap.vbs");
		addDefault(Os.WINDOWS, SWIFT_WRAPPER_INTERPRETER_OPTIONS, new String[] {"//Nologo"});
		addDefault(Os.WINDOWS, SWIFT_CLEANUP_COMMAND, "cmd.exe");
		addDefault(Os.WINDOWS, SWIFT_CLEANUP_COMMAND_OPTIONS, new String[] {"/C", "del", "/Q"});
		addDefault(null, SWIFT_WRAPPER_INTERPRETER, "/bin/bash");
		addDefault(null, SWIFT_WRAPPER_SCRIPT, "_swiftwrap");
		addDefault(null, SWIFT_WRAPPER_INTERPRETER_OPTIONS, null);
		addDefault(null, SWIFT_CLEANUP_COMMAND, "/bin/rm");
		addDefault(null, SWIFT_CLEANUP_COMMAND_OPTIONS, new String[] {"-rf"});
	}
	
	private Object getSingle(SwiftContact bc, String name, Object defval) 
	    throws ExecutionException {
            String value = getProperty(bc, name);
            if (value == null) {
            	Os os = getOS(bc);
            	if (DEFAULTS_NAMES.contains(name)) {
            		return getDefault(os, name);
            	}
                else if (SYSINFO_OS.equals(name)) {
                	return os;
                }
                else if (defval != null) {
                    return defval;
                }
                else {
                	throw new ExecutionException(this, "Missing profile: " + name);
                }
            }
            else {
            	return value;
            }
	}

    private String getProperty(SwiftContact bc, String name) {
        Object o = bc.getProperty(name);
        if (o == null) {
            return null;
        }
        else {
            return o.toString();
        }
    }
    
    private Os getOS(SwiftContact bc) {
    	Object o = bc.getProperty("OS");
    	if (o == null) {
    		return DEFAULT_OS;
    	}
    	else {
    		return SysInfo.fromString(o.toString()).getOs();
    	}
    }    
}
