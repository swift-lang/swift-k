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
import java.util.List;

import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.swift.catalog.site.Application;
import org.globus.swift.catalog.site.SwiftContact;
import org.griphyn.vdl.karajan.Command;
import org.griphyn.vdl.karajan.FileNameResolver;
import org.griphyn.vdl.karajan.FileNameResolver.Transform;

public class ResolveFiles extends SwiftFunction {
	public static final Logger logger = Logger.getLogger(ResolveFiles.class);
	
	private ArgRef<Command[]> commands;
	private ArgRef<String> stagingMethod;
	private ArgRef<SwiftContact> host;

	@Override
    protected Signature getSignature() {
        return new Signature(params("commands", "host", optional("stagingMethod", "file")));
    }

	public Object function(Stack stack) {
	    Command[] cmds = this.commands.getValue(stack);
	    String stagingMethod = this.stagingMethod.getValue(stack);
	    SwiftContact host = this.host.getValue(stack);
	    
	    boolean direct = "direct".equals(stagingMethod);
	    
	    for (Command cmd : cmds) {
	        String executable = cmd.getExecutable();
	        Application app = host.findApplication(executable);
            if (app == null) {
                throw new RuntimeException("Application '" + executable + "' not found on site '" + host.getName() + "'");
            }
            cmd.setApplication(app);
	        
    	    List<Object> args = cmd.getArguments();
    	    List<Object> r = new ArrayList<Object>(args.size());
    	    for (int i = 0; i < args.size(); i++) {
    	        Object a = args.get(i);
    	        if (a instanceof FileNameResolver) {
    	            FileNameResolver ex = (FileNameResolver) a;
    	            if (direct) {
    	                ex.setTransform(Transform.ABSOLUTE);
    	            }
    	            ex.toString(r);
    	        }
    	        else {
    	            r.add(a);
    	        }
    	    }
    	    
    	    cmd.setArguments(r);
    	    
    	    cmd.setStdin(getPath(cmd.getStdin(), stagingMethod, "stdin", direct));
    	    cmd.setStdout(getPath(cmd.getStdout(), stagingMethod, "stdout", direct));
    	    cmd.setStderr(getPath(cmd.getStderr(), stagingMethod, "stderr", direct));
	    }
	    return null;
	}
	
	private Object getPath(Object o, String defaultScheme, String name, boolean direct) {
	    if (o == null) {
	        return null;
	    }
        if (o instanceof String) {
            return o;
        }
        else if (o instanceof FileNameResolver) {
            FileNameResolver e = (FileNameResolver) o;
            if (direct) {
                e.setTransform(Transform.ABSOLUTE);
            }
            if (defaultScheme != null) {
                e.setDefaultScheme(defaultScheme);
            }
            String[] fns = e.toStringArray();
            if (fns.length != 1) {
                throw new IllegalArgumentException("Multiple filenames passed to " + name);
            }
            return fns[0];
        }
        else {
            throw new IllegalArgumentException("Invalid value for " + name + ": '" + o + "'");
        }
    }
}

