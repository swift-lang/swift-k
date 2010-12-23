/*
 * Created on Dec 26, 2006
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.HashSet;
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
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.util.Profile;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.util.FQN;

public class TCProfile extends VDLFunction {
    public static final Logger logger = Logger.getLogger(TCProfile.class);
    
	public static final Arg OA_TR = new Arg.Optional("tr");
	public static final Arg PA_HOST = new Arg.Positional("host");
	
	static {
		setArguments(TCProfile.class, new Arg[] { PA_HOST, OA_TR });
	}

	private static Map<String, Arg> PROFILE_T;

	static {
		PROFILE_T = new HashMap<String, Arg>();
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
		String tr = null;
		if (OA_TR.isPresent(stack)) {
		    tr = TypeUtil.toString(OA_TR.getValue(stack));
		}
		BoundContact bc = (BoundContact) PA_HOST.getValue(stack);
		
		NamedArguments named = ArgUtil.getNamedReturn(stack);
		Map<String,Object> attrs = null;
		
		attrs = attributesFromHost(bc, attrs, named);

		TCEntry tce = null;
		if (tr != null) {
		    tce = getTCE(tc, new FQN(tr), bc);
		}
		
		Map<String,String> env = new HashMap<String,String>();
		if (tce != null) {
			addEnvironment(env, tce);
			addEnvironment(env, bc);
			attrs = attributesFromTC(tce, attrs, named);
		}
		named.add(GridExec.A_ENVIRONMENT, env);
		checkWalltime(tr, named);
		addAttributes(named, attrs);
		return null;
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
	
	private static final Set<String> warnedAboutWalltime = 
	    new HashSet<String>();
	
	private void warn(String tr, String message) {
        synchronized (warnedAboutWalltime) {
            if (warnedAboutWalltime.add(tr)) {
                System.out.println(message);
            }
        }
    }

	private void addEnvironment(Map<String,String> m, 
	                            TCEntry tce) {
		List<Profile> list = tce.getProfiles(Profile.ENV);
		if (list != null) {
			for (Profile p : list) {
				m.put(p.getProfileKey(), p.getProfileValue());
			}
		}
	}

	public static final String PROFILE_GLOBUS_PREFIX = 
	    (Profile.GLOBUS + "::").toLowerCase();

	private void addEnvironment(Map<String,String> m, 
	                            BoundContact bc) {
		Map<String,Object> props = bc.getProperties();
		for (Map.Entry<String,Object> e : props.entrySet()) {
			String name = e.getKey();
			FQN fqn = new FQN(name); 
			String value = (String) e.getValue();
			if (Profile.ENV.equalsIgnoreCase(fqn.getNamespace())) {
				m.put(fqn.getName(), value);
			}
		}
	}
	
	private void addAttributes(NamedArguments named, 
	                           Map<String,Object> attrs) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Attributes: " + attrs);
	    }
	    if (attrs == null || attrs.size() == 0) {
	        return;
	    }
	    named.add(GridExec.A_ATTRIBUTES, attrs);
	}

	private Map<String,Object> 
	attributesFromTC(TCEntry tce, 
	                 Map<String,Object> attrs, 
	                 NamedArguments named) {
	    List<Profile> list = tce.getProfiles(Profile.GLOBUS);
		if (list != null) {
			for (Profile p : list) {
				Arg a = PROFILE_T.get(p.getProfileKey());
				if (a == null) {
				    if (attrs == null) {
				        attrs = new HashMap<String,Object>();
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
	
	/**
	   Inserts namespace=globus attributes from BoundContact bc 
	   into given attrs
	 */
	private Map<String,Object> 
	attributesFromHost(BoundContact bc, 
	                   Map<String,Object> attrs, 
	                   NamedArguments named) {
		Map<String,Object> props = bc.getProperties();
		if (props != null) {
		    for (Map.Entry<String,Object> e : props.entrySet()) {
		        FQN fqn = new FQN(e.getKey());
		        if (Profile.GLOBUS.equalsIgnoreCase(fqn.getNamespace())) {
		            Arg a = PROFILE_T.get(fqn.getName());
		            if (a == null) {
		                if (attrs == null) {
		                    attrs = new HashMap<String,Object>();
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
