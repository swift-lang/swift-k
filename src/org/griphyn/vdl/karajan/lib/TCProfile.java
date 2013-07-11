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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.functions.Map.Entry;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.util.Profile;
import org.griphyn.vdl.karajan.TCCache;
import org.griphyn.vdl.util.FQN;

public class TCProfile extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(TCProfile.class);
    
    private ArgRef<BoundContact> host;
    /**
       Allows for dynamic attributes from the SwiftScript 
       profile statements. 
       These override any other attributes. 
     */
    private ArgRef<Map<String, Object>> attributes;
    private ArgRef<String> tr;
    
    private VarRef<Object> r_count;
    private VarRef<Object> r_jobType;
    private VarRef<Object> r_attributes;
    private ChannelRef<Map.Entry<Object, Object>> cr_environment;
    
    private enum Attr {
        COUNT, JOB_TYPE;
    }
    
    private static final Map<String, Attr> ATTR_TYPES;
    
    static {
        ATTR_TYPES = new HashMap<String, Attr>();
        ATTR_TYPES.put("count", Attr.COUNT);
        ATTR_TYPES.put("jobType", Attr.JOB_TYPE);
    }

	@Override
    protected Signature getSignature() {
        return new Signature(
            params("host", optional("attributes", null), optional("tr", null)),
            returns("count", "jobType",
                "attributes", channel("environment", DYNAMIC))
        );
    }

    public Object function(Stack stack) {
		TCCache tc = getTC(stack);
		String tr = this.tr.getValue(stack);
		
		Map<String, Object> dynamicAttributes = readDynamicAttributes(stack);
		
		BoundContact bc = this.host.getValue(stack);
		
		Map<String,Object> attrs = null;	
		attrs = attributesFromHost(bc, attrs, stack);

		TCEntry tce = null;
		if (tr != null) {
		    tce = getTCE(tc, new FQN(tr), bc);
		}
		
		if (tce != null) {
			addEnvironment(stack, tce);
			addEnvironment(stack, bc);
			attrs = attributesFromTC(tce, attrs, stack);
		}
		attrs = addDynamicAttributes(attrs, dynamicAttributes);
		checkWalltime(attrs, tr, stack);
		addAttributes(attrs, stack);
		return null;
	}

	/**
	   Bring in the dynamic attributes from the Karajan stack 
	   @return Map, may be null
	 */
	private Map<String, Object> readDynamicAttributes(Stack stack) {
		return this.attributes.getValue(stack);
	}
	
	/**
       Store dynamic attributes into returned attributes, 
       overwriting if necessary
       @param result Attributes so far known, may be null
       @param dynamicAttributes Attributes to insert, may be null
       @result Combination, may be null
	 */
	private Map<String, Object>
	addDynamicAttributes(Map<String, Object> result,
	                     Map<String, Object> dynamicAttributes) {
		if (result == null && dynamicAttributes == null)
			return null;
		if (result == null)
			return dynamicAttributes;
		if (dynamicAttributes == null)
			return result;
		result.putAll(dynamicAttributes);
		return result;
	}
	
	private void checkWalltime(Map<String, Object> attrs, String tr, Stack stack) {
		if (attrs == null) {
			return;
		}
	    Object walltime = attrs.get("maxwalltime");
        if (walltime == null) {
            return;
        }
        try {
        	//validate walltime
            WallTime.timeToSeconds(walltime.toString());
        }
        catch (ExecutionException e) {
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

	private void addEnvironment(Stack stack, TCEntry tce) {
		List<Profile> list = tce.getProfiles(Profile.ENV);
		if (list != null) {
			for (Profile p : list) {
			    cr_environment.append(stack, new Entry(p.getProfileKey(), p.getProfileValue()));
			}
		}
	}

	public static final String PROFILE_GLOBUS_PREFIX = (Profile.GLOBUS + "::").toLowerCase();

	private void addEnvironment(Stack stack, BoundContact bc) {
		Map<String,Object> props = bc.getProperties();
		if (props != null) {
    		for (Map.Entry<String,Object> e : props.entrySet()) {
    			String name = e.getKey();
    			FQN fqn = new FQN(name); 
    			String value = (String) e.getValue();
    			if (Profile.ENV.equalsIgnoreCase(fqn.getNamespace())) {
    			    cr_environment.append(stack, new Entry(fqn.getName(), value));
    			}
    		}
		}
	}
	
	private void addAttributes(Map<String,Object> attrs, Stack stack) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("Attributes: " + attrs);
	    }
	    if (attrs == null) {
	        return;
	    }
	    Iterator<Map.Entry<String, Object>> i = attrs.entrySet().iterator();
	    while (i.hasNext()) {
	        Map.Entry<String, Object> e = i.next();
	        Attr a = ATTR_TYPES.get(e.getKey());
	        if (a != null) {
	            setAttr(a, stack, e.getValue());
	            i.remove();
	        }
	    }
	    if (attrs.size() == 0) {
	        return;
	    }
	    this.r_attributes.setValue(stack, attrs);
	}

	private Map<String,Object> attributesFromTC(TCEntry tce, Map<String,Object> attrs, Stack stack) {
	    List<Profile> list = tce.getProfiles(Profile.GLOBUS);
		if (list != null) {
			for (Profile p : list) {
				Attr a = ATTR_TYPES.get(p.getProfileKey());
				if (a == null) {
				    if (attrs == null) {
				        attrs = new HashMap<String,Object>();
				    }
				    attrs.put(p.getProfileKey(), p.getProfileValue());
				}
				else {
				    setAttr(a, stack, p.getProfileValue());
				}
			}
		}
		return attrs;
	}
	
	/**
	   Inserts namespace=globus attributes from BoundContact bc 
	   into given attrs
	 */
	private Map<String,Object> attributesFromHost(BoundContact bc, Map<String, Object> attrs, Stack stack) {
		Map<String,Object> props = bc.getProperties();
		if (props != null) {
		    for (Map.Entry<String,Object> e : props.entrySet()) {
		        FQN fqn = new FQN(e.getKey());
		        if (Profile.GLOBUS.equalsIgnoreCase(fqn.getNamespace())) {
		            Attr a = ATTR_TYPES.get(fqn.getName());
		            if (a == null) {
		                if (attrs == null) {
		                    attrs = new HashMap<String,Object>();
		                }
		                attrs.put(fqn.getName(), e.getValue());
		            }
		            else {
		                setAttr(a, stack, e.getValue());
		            }
		        }
		    }
		}
		return attrs;
	}

    private void setAttr(Attr a, Stack stack, Object value) {
        switch (a) {
            case COUNT:
                r_count.setValue(stack, value);
                break;
            case JOB_TYPE:
                r_jobType.setValue(stack, value);
                break;
        }
    }
}
