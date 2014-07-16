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
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Collection;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.AbstractFutureArrayDataNode;

public class Mark extends SwiftFunction {
    private ArgRef<Channel<AbstractDataNode>> restarts;
    private ArgRef<Boolean> err;
    private ArgRef<Boolean> mapping;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("restarts", "err", optional("mapping", Boolean.FALSE)));
    }

    @Override
    public Object function(Stack stack) {
        try {
            if (err.getValue(stack)) {
                boolean mapping = this.mapping.getValue(stack);
                
                Channel<AbstractDataNode> files = this.restarts.getValue(stack);
                for (AbstractDataNode dn : files) {
                    if (dn.getType().isArray() && !dn.isClosed()) {
                        // a dynamic array
                        AbstractFutureArrayDataNode fdn = (AbstractFutureArrayDataNode) dn;
                        if (mapping) {
                            fdn.setException(new MappingDependentException(dn, null));
                        }
                        else {
                            fdn.setException(new DataDependentException(dn, null));
                        }
                    }
                    else {
                        Collection<DSHandle> leaves = dn.getLeaves();
                        for (DSHandle leaf : leaves) {
                            synchronized (leaf) {
                                if (mapping) {
                                    leaf.setValue(new MappingDependentException(leaf, null));
                                }
                                else {
                                    leaf.setValue(new DataDependentException(leaf, null));
                                }
                                leaf.closeShallow();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException(this, e);
        }
        return null;
    }
}
