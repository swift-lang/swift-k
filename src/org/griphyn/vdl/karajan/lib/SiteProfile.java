/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.swift.catalog.types.Os;
import org.griphyn.vdl.util.FQN;

public class SiteProfile extends VDLFunction {
    public static final Logger logger = Logger.getLogger(SiteProfile.class);
    
	public static final Arg PA_HOST = new Arg.Positional("host");
	public static final Arg PA_FQN = new Arg.Positional("fqn");
	public static final Arg OA_DEFAULT = new Arg.Optional("default", null);

	static {
		setArguments(SiteProfile.class, new Arg[] { PA_HOST, PA_FQN, OA_DEFAULT });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		BoundContact bc = (BoundContact) PA_HOST.getValue(stack);
		return getSingle(stack, bc, new FQN(TypeUtil.toString(PA_FQN.getValue(stack))), OA_DEFAULT.getValue(stack));
	}
	
	public static final FQN SWIFT_WRAPPER_INTERPRETER = new FQN("swift:wrapperInterpreter");
	public static final FQN SWIFT_WRAPPER_INTERPRETER_OPTIONS = new FQN("swift:wrapperInterpreterOptions");
	public static final FQN SWIFT_WRAPPER_SCRIPT = new FQN("swift:wrapperScript");
	public static final FQN SWIFT_CLEANUP_COMMAND = new FQN("swift:cleanupCommand");
	public static final FQN SWIFT_CLEANUP_COMMAND_OPTIONS = new FQN("swift:cleanupCommandOptions");
	public static final FQN SYSINFO_OS = new FQN("SYSINFO:OS");
	
	private static final Map<Os, Map<FQN,Object>> DEFAULTS;
	private static final Set<FQN> DEFAULTS_NAMES; 
	
	private static void addDefault(Os os, FQN fqn, Object value) {
		DEFAULTS_NAMES.add(fqn);
		Map<FQN,Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			osm = new HashMap<FQN,Object>();
			DEFAULTS.put(os, osm);
		}
		osm.put(fqn, value);
	}
	
	@SuppressWarnings("unused")
	private static boolean hasDefault(Os os, FQN fqn) {
	    Map<FQN,Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			return false;
		}
		else {
			return osm.containsKey(fqn);
		}
	}
	
	private static Object getDefault(Os os, FQN fqn) {
	    Map<FQN,Object> osm = DEFAULTS.get(os);
		if (osm == null) {
			osm = DEFAULTS.get(null);
		}
		return osm.get(fqn);
	}
	
	static {
		DEFAULTS = new HashMap<Os,Map<FQN,Object>>();
		DEFAULTS_NAMES = new HashSet<FQN>();
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
	
	private Object getSingle(VariableStack stack, BoundContact bc, FQN fqn, Object defval) 
	    throws ExecutionException {
            String value = getProfile(bc, fqn);
            if (value == null) {
            	Os os = getOS(bc);
            	if (DEFAULTS_NAMES.contains(fqn)) {
            		return getDefault(os, fqn);
            	}
                else if (SYSINFO_OS.equals(fqn)) {
                	return os;
                }
                else if (defval != null) {
                    return defval;
                }
                else {
                	throw new ExecutionException(stack, "Missing profile: " + fqn);
                }
            }
            else {
            	return value;
            }
	}

    private String getProfile(BoundContact bc, FQN fqn) {
        Object o = bc.getProperty(fqn.toString());
        if (o == null) {
            return null;
        }
        else {
            return o.toString();
        }
    }
    
    private Os getOS(BoundContact bc) {
    	Object o = bc.getProperty("sysinfo");
    	if (o == null) {
    		return Os.LINUX;
    	}
    	else {
    		String[] p = o.toString().split("::");
    		if (p.length < 2) {
    			throw new IllegalArgumentException("Invalid sysinfo for " + bc + ": " + o);
    		}
    		return Os.fromString(p[1]);
    	}
    }
}
