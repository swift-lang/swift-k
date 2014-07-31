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
 * Created on Dec 6, 2006
 */
package org.griphyn.vdl.karajan.functions;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.vdl.util.SwiftConfig;

public class ConfigProperty extends InternalFunction {
    private ArgRef<String> name;
    private ArgRef<Boolean> instance;
    private ArgRef<BoundContact> host;
    private ChannelRef<Object> cr_vargs;
    
    private SwiftConfig instanceConfig;
    private VarRef<Context> context;
    
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("name", optional("instance", Boolean.TRUE), optional("host", null)), 
        		returns(channel("...")));
    }

    public static final String INSTANCE_CONFIG = "SWIFT_CONFIG";

    public static final Logger logger = Logger.getLogger(ConfigProperty.class);

    
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        context = scope.getVarRef("#context");
        Context ctx = context.getValue();
        Var.Channel r = scope.parent.lookupChannel("...");
        if (this.name.isStatic()) {
            checkProperty(this.name.getValue());
        }
        if (this.name.isStatic() && this.instance.isStatic() && this.host.isStatic() 
        		&& ctx != null && this.host.getValue() == null) {
        	Object value = getProperty(this.name.getValue(), this.instance.getValue(), getInstanceConfig(ctx));
        	if (r.append(value)) {
        		return null;
        	}
        }
        
        r.appendDynamic();
        
        return super.compileBody(w, argScope, scope);
    }

    @Override
    protected void runBody(LWThread thr) {
    	Stack stack = thr.getStack();
        String name = this.name.getValue(stack);
        boolean instance = this.instance.getValue(stack);
        BoundContact host = this.host.getValue(stack);
        if (logger.isDebugEnabled()) {
            logger.debug("Getting property " + name + " with host " + host);
        }
        if (host != null) {
            // see if the host has this property defined, and if so
            // get its value
            String prop = (String) host.getProperty(name);
            if (prop != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found property " + name + " in BoundContact");
                }
                cr_vargs.append(stack, prop);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find property " + name + " in BoundContact");
            }
        }
        cr_vargs.append(stack, getProperty(name, instance, getInstanceConfig(stack)));
    }

    private synchronized SwiftConfig getInstanceConfig(Stack stack) {
        if (instanceConfig == null) {
            Context ctx = this.context.getValue(stack);
            instanceConfig = (SwiftConfig) ctx.getAttribute("SWIFT:CONFIG");
        }
        return instanceConfig;
    }
    
    private synchronized SwiftConfig getInstanceConfig(Context ctx) {
    	return (SwiftConfig) ctx.getAttribute("SWIFT:CONFIG");
    }

    public static Object getProperty(String name, SwiftConfig instanceConfig) {
        return getProperty(name, true, instanceConfig);
    }

    public static Object getProperty(String name, boolean instance, SwiftConfig instanceConfig) {
        SwiftConfig conf;
        Object prop;
        if (!instance) {
            conf = SwiftConfig.getDefault();
            prop = conf.getProperty(name);
        }
        else {
            conf = instanceConfig;
            prop = conf.getProperty(name);
        }
        if (prop == null) {
            throw new ExecutionException("Swift config property \"" + name + "\" not found");
        }
        else {
            return prop;
        }
    }
    
    private void checkProperty(String name) throws CompilationException {
        if (!SwiftConfig.SCHEMA.propertyExists(name)) {
            throw new CompilationException(this, "Unknown configuration property: " + name);
        }
    }
}
