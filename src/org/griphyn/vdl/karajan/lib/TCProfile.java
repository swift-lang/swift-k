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
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;
import org.griphyn.vdl.engine.Warnings;

public class TCProfile extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(TCProfile.class);
    
    private ArgRef<SwiftContact> host;
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
    private VarRef<Map<String, String>> r_environment;
    
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
                "attributes", "environment")
        );
    }

    public Object function(Stack stack) {
		String tr = this.tr.getValue(stack);
		SwiftContact bc = this.host.getValue(stack);
		
		Application app = bc.findApplication(tr);
		if (app == null) {
		    if ("*".equals(tr)) {
		        return null;
		    }
		    else {
		        throw new RuntimeException("Application '" + tr + "' not found on site '" + bc.getName() + "'");
		    }
		}
		
		addEnvironment(stack, app.getEnv());
		
		Map<String, Object> dynamicAttributes = this.attributes.getValue(stack);
		Map<String, Object> attrs = null;
		
		attrs = combineAttributes(stack, attrs, app.getProperties());
		attrs = combineAttributes(stack, attrs, dynamicAttributes);
		
		checkWalltime(stack, attrs, tr);
		setAttributes(stack, attrs);
		return null;
	}

	private void addEnvironment(Stack stack, Map<String, String> env) {
	    r_environment.setValue(stack, env);
    }

    /**
	   Bring in the dynamic attributes from the Karajan stack 
	   @return Map, may be null
	 */
	private Map<String, Object> readDynamicAttributes(Stack stack) {
		return this.attributes.getValue(stack);
	}

	/**
	 * Combine attributes creating result as necessary
	 */
	private Map<String, Object>	combineAttributes(Stack stack, Map<String, Object> result, Map<String, Object> src) {
	    if (src == null || src.isEmpty()) {
	        return result;
	    }
		for (Map.Entry<String, Object> e : src.entrySet()) {
		    Attr a = ATTR_TYPES.get(e.getKey());
            if (a != null) {
                setAttr(a, stack, e.getValue());
            }
            else {
                if (result == null) {
                    result = new HashMap<String, Object>();
                }
                result.put(e.getKey(), e.getValue());
            }
		}
		return result;
	}
	
	private void checkWalltime(Stack stack, Map<String, Object> attrs, String tr) {
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
            Warnings.warn(Warnings.Type.SITE, "Invalid walltime specification for \"" + tr
                    + "\" (" + walltime + ").");
        }
	}
		
	private void setAttributes(Stack stack, Map<String, Object> attrs) {
	    this.r_attributes.setValue(stack, attrs);
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
