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
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class Stageout extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(Stageout.class);

    private ArgRef<AbstractDataNode> var;
    private ChannelRef<Object> cr_stageout;
    private ChannelRef<Object> cr_restartout;
    private VarRef<Boolean> r_deperror;
    private VarRef<Boolean> r_mdeperror;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var"), returns("deperror", "mdeperror", 
            channel("stageout", DYNAMIC), channel("restartout", DYNAMIC)));
    }
    
    private List<?> list(Path p, DSHandle var) {
        ArrayList<Object> l = new ArrayList<Object>(2);
        l.add(p);
        l.add(var);
        return l;
    }

    @Override
    public Object function(Stack stack) {
        AbstractDataNode var = this.var.getValue(stack);
        boolean deperr = false;
        boolean mdeperr = false;
        // currently only static arrays are supported as app returns
        // however, previous to this, there was no code to check
        // if these arrays had their sizes closed, which could lead to 
        // race conditions (e.g. if this array's mapper had some parameter
        // dependencies that weren't closed at the time the app was started).
        if (var.getType().isArray()) {
            var.waitFor(this);
        }
        try {
            if (!var.isPrimitive()) {
                retPaths(cr_stageout.get(stack), var);
            }
            if (var.isRestartable()) {
                retPaths(cr_restartout.get(stack), var);
            }
        }
        catch (MappingDependentException e) {
            logger.debug(e);
            deperr = true;
            mdeperr = true;
        }
        if (deperr) {
            this.r_deperror.setValue(stack, true);
        }
        if (mdeperr) {
            this.r_mdeperror.setValue(stack, true);
        }
        return null;
    }

    private void retPaths(k.rt.Channel<Object> channel, DSHandle var) throws ExecutionException {
        try {
            Collection<Path> fp = var.getFringePaths();
            for (Path p : fp) {
                channel.add(list(p, var));
            }
        }
        catch (Exception e) {
            throw new ExecutionException(this, e);
        }
    }
}
