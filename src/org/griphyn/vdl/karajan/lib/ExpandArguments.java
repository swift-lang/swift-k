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


package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.FileNameExpander;

public class ExpandArguments extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(ExpandArguments.class);
	
	private ArgRef<Collection<Object>> args;
	private ArgRef<String> stagingMethod;

	@Override
    protected Signature getSignature() {
        return new Signature(params("args", "stagingMethod"));
    }

	public Object function(Stack stack) {
	    Collection<Object> args = this.args.getValue(stack);
	    String stagingMethod = this.stagingMethod.getValue(stack);
	    
	    boolean direct = "direct".equals(stagingMethod);
	    
	    List<Object> ret = new ArrayList<Object>();
	    for (Object a : args) {
	        if (a instanceof FileNameExpander) {
	            ((FileNameExpander) a).toString(ret, direct);
	        }
	        else {
	            ret.add(a);
	        }
	    }
	    return ret;
	}
}

