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

import java.io.IOException;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.BoundContact;
import org.griphyn.vdl.util.VDL2Config;

public class ConfigProperty extends AbstractSingleValuedFunction {
    private ArgRef<String> name;
    private ArgRef<Boolean> instance;
    private ArgRef<BoundContact> host;
    
    private VDL2Config instanceConfig;
    private VarRef<Context> context;
    
    
    @Override
    protected Param[] getParams() {
        return params("name", optional("instance", Boolean.TRUE), optional("host", null));
    }

    public static final String INSTANCE_CONFIG = "SWIFT_CONFIG";

    public static final Logger logger = Logger.getLogger(ConfigProperty.class);
    
    
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        context = scope.getVarRef("#context");
        return super.compileBody(w, argScope, scope);
    }

    @Override
    public Object function(Stack stack) {
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
                return prop;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find property " + name + " in BoundContact");
            }
        }
        return getProperty(name, instance, getInstanceConfig(stack));
    }

    private synchronized VDL2Config getInstanceConfig(Stack stack) {
        if (instanceConfig == null) {
            Context ctx = this.context.getValue(stack);
            instanceConfig = (VDL2Config) ctx.getAttribute("SWIFT:CONFIG");
        }
        return instanceConfig;
    }

    public static String getProperty(String name, VDL2Config instanceConfig) {
        return getProperty(name, true, instanceConfig);
    }

    public static String getProperty(String name, boolean instance, VDL2Config instanceConfig) {
        try {
            VDL2Config conf;
            String prop;
            if (!instance) {
                conf = VDL2Config.getConfig();
                prop = conf.getProperty(name);
            }
            else {
                conf = instanceConfig;
                prop = conf.getProperty(name);
            }
            if (prop == null) {
                throw new ExecutionException("Swift config property \"" + name + "\" not found in "
                        + conf);
            }
            else {
                return prop;
            }
        }
        catch (IOException e) {
            throw new ExecutionException("Failed to load Swift configuration", e);
        }
    }
}
