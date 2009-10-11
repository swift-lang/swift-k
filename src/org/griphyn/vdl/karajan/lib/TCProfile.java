/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.grid.GridExec;
import org.griphyn.cPlanner.classes.Profile;
import org.griphyn.common.catalog.TransformationCatalogEntry;
import org.griphyn.common.classes.Os;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.util.FQN;

public class TCProfile extends VDLFunction {
    public static final Logger logger = Logger.getLogger(TCProfile.class);
    
	public static final Arg PA_TR = new Arg.Positional("tr");
	public static final Arg PA_HOST = new Arg.Positional("host");
	public static final Arg OA_FQN = new Arg.Optional("fqn");

	static {
		setArguments(TCProfile.class, new Arg[] { PA_TR, PA_HOST, OA_FQN });
	}

	private static Map PROFILE_T;

	static {
		PROFILE_T = new HashMap();
		PROFILE_T.put("count", GridExec.A_COUNT);
		PROFILE_T.put("jobtype", GridExec.A_JOBTYPE);
		PROFILE_T.put("maxcputime", GridExec.A_MAXCPUTIME);
		PROFILE_T.put("maxmemory", GridExec.A_MAXMEMORY);
		PROFILE_T.put("maxtime", GridExec.A_MAXTIME);
		PROFILE_T.put("maxwalltime", GridExec.A_MAXWALLTIME);
		PROFILE_T.put("minmemory", GridExec.A_MINMEMORY);
		PROFILE_T.put("project", GridExec.A_PROJECT);
		PROFILE_T.put("queue", GridExec.A_QUEUE);
	}

	public Object function(VariableStack stack) throws ExecutionException {
		TCCache tc = getTC(stack);
		String tr = TypeUtil.toString(PA_TR.getValue(stack));
		BoundContact bc = (BoundContact) PA_HOST.getValue(stack);
		if (OA_FQN.isPresent(stack)) {
		    return getSingle(tc, tr, bc, new FQN(TypeUtil.toString(OA_FQN.getValue(stack))));
		}
		
		NamedArguments named = ArgUtil.getNamedReturn(stack);
		Map attrs = null;
		
		attrs = attributesFromHost(bc, attrs, named);

		TransformationCatalogEntry tce = getTCE(tc, new FQN(tr), bc);
		
		Map env = new HashMap();
		if (tce != null) {
			addEnvironment(env, tce);
			addEnvironment(env, bc);
			if (env.size() != 0) {
				named.add(GridExec.A_ENVIRONMENT, env);
			}

			attrs = attributesFromTC(tce, attrs, named);
		}
		checkWalltime(tr, named);
		addAttributes(named, attrs);
		return null;
	}
	
	public static final FQN SWIFT_WRAPPER_INTERPRETER = new FQN("swift:wrapperInterpreter");
	public static final FQN SWIFT_WRAPPER_INTERPRETER_OPTIONS = new FQN("swift:wrapperInterpreterOptions");
	public static final FQN SWIFT_WRAPPER_SCRIPT = new FQN("swift:wrapperScript");
	public static final FQN INTERNAL_OS = new FQN("INTERNAL:OS");
	
	private Object getSingle(TCCache tc, String tr, BoundContact bc, FQN fqn) {
            TransformationCatalogEntry tce = getTCE(tc, new FQN(tr), bc);
            String value = getProfile(tce, fqn);
            if (value == null) {
                value = getProfile(bc, fqn);
            }
            if (value == null) {
                if (SWIFT_WRAPPER_INTERPRETER.equals(fqn)) {
                    if (tce.getSysInfo().getOs().equals(Os.WINDOWS)) {
                        return "cscript.exe";
                    }
                    else {
                        return "/bin/bash";
                    }
                }
                else if (SWIFT_WRAPPER_SCRIPT.equals(fqn)) {
                    if (tce.getSysInfo().getOs().equals(Os.WINDOWS)) {
                        return "_swiftwrap.vbs";
                    }
                    else {
                        return "_swiftwrap";
                    }
                }
                else if (SWIFT_WRAPPER_INTERPRETER_OPTIONS.equals(fqn)) {
                	if (tce.getSysInfo().getOs().equals(Os.WINDOWS)) {
                		return new String[] {"//Nologo"};
                	}
                	else {
                		return null;
                	}
                }
                else if (INTERNAL_OS.equals(fqn)) {
                	Os os = tce.getSysInfo().getOs();
                	if (os == null) {
                		return Os.LINUX;
                	}
                	else {
                		return os;
                	}
                }
            }
            return value;
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

    private String getProfile(TransformationCatalogEntry tce, FQN fqn) {
        List profiles = tce.getProfiles();
        if (profiles == null) {
        	return null;
        }
        Iterator i = profiles.iterator();
        while (i.hasNext()) {
            Profile p = (Profile) i.next();
            if (eq(p.getProfileNamespace(), fqn.getNamespace()) && eq(p.getProfileKey(), fqn.getName())) {
                return p.getProfileValue();
            }
        }
        return null;
    }
	
	private boolean eq(Object o1, Object o2) {
	    if (o1 == null) {
	        return o2 == null;
	    }
	    else {
	        return o1.equals(o2);
	    }
	}

    private void checkWalltime(String tr, NamedArguments attrs) {
	    Object walltime = null;
	    if (attrs != null) {
	        if (attrs.hasArgument("maxwalltime")) {
	            walltime = attrs.getArgument("maxwalltime");
	        }
	    }
        if (walltime == null) {
            return;
        }
        try {
        	//validate walltime
            WallTime.timeToSeconds(walltime.toString());
        }
        catch (IllegalArgumentException e) {
            warn(tr, "Warning: invalid walltime specification for \"" + tr
                    + "\" (" + walltime + ").");
        }
	}
	
	private static final Set warnedAboutWalltime = new HashSet();
	
	private void warn(String tr, String message) {
        synchronized (warnedAboutWalltime) {
            if (warnedAboutWalltime.add(tr)) {
                System.out.println(message);
            }
        }
    }

	private void addEnvironment(Map m, TransformationCatalogEntry tce) {
		List l = tce.getProfiles(Profile.ENV);
		if (l != null) {
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Profile p = (Profile) i.next();
				m.put(p.getProfileKey(), p.getProfileValue());
			}
		}
	}

	public static final String PROFILE_GLOBUS_PREFIX = (Profile.GLOBUS + "::").toLowerCase();

	private void addEnvironment(Map m, BoundContact bc) {
		Map props = bc.getProperties();
		Iterator i = props.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			String name = (String) e.getKey();
			FQN fqn = new FQN(name); 
			String value = (String) e.getValue();
			if (Profile.ENV.equalsIgnoreCase(fqn.getNamespace())) {
				m.put(fqn.getName(), value);
			}
		}
	}
	
	private void addAttributes(NamedArguments named, Map attrs) {
	    if (logger.isInfoEnabled()) {
	        logger.info("Attributes: " + attrs);
	    }
	    if (attrs == null || attrs.size() == 0) {
	        return;
	    }
	    named.add(GridExec.A_ATTRIBUTES, attrs);
	}

	private Map attributesFromTC(TransformationCatalogEntry tce, Map attrs, NamedArguments named) {
		List l = tce.getProfiles(Profile.GLOBUS);
		if (l != null) {
			Iterator i = l.iterator();
			while (i.hasNext()) {
				Profile p = (Profile) i.next();
				Arg a = (Arg) PROFILE_T.get(p.getProfileKey());
				if (a == null) {
				    if (attrs == null) {
				        attrs = new HashMap();
				    }
				    attrs.put(p.getProfileKey(), p.getProfileValue());
				}
				else {
				    named.add(a, p.getProfileValue());
				}
			}
		}
		return attrs;
	}
	
	private Map attributesFromHost(BoundContact bc, Map attrs, NamedArguments named) {
		Map props = bc.getProperties();
		if (props != null) {
		    Iterator i = props.entrySet().iterator();
		    while (i.hasNext()) {
		        Map.Entry e = (Map.Entry) i.next();
		        FQN fqn = new FQN((String) e.getKey());
		        if (Profile.GLOBUS.equalsIgnoreCase(fqn.getNamespace())) {
		            Arg a = (Arg) PROFILE_T.get(fqn.getName());
		            if (a == null) {
		                if (attrs == null) {
		                    attrs = new HashMap();
		                }
		                attrs.put(fqn.getName(), e.getValue());
		            }
		            else {
		                named.add(a, e.getValue());
		            }
		        }
		    }
		}
		return attrs;
	}
}
