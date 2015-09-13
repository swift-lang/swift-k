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

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.Pair;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.nodes.RootFutureStructDataNode;
import org.griphyn.vdl.type.Field;

public abstract class AbstractCreateKVStruct<T> extends SetFieldValue {
	public static final Logger logger = Logger.getLogger(AbstractCreateKVStruct.class);
	
	private ArgRef<Field> field;
	private ArgRef<DSHandle> var;
	private ChannelRef<Pair<T, DSHandle>> c_vargs;

    @Override
    protected Signature getSignature() {
        return new Signature(params(optional("field", null), optional("var", null), "..."));
    }

    @Override
    public void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState(1);
        Stack stack = thr.getStack();
        DSHandle var = (DSHandle) thr.popState();
        @SuppressWarnings("unchecked")
        Channel<Pair<T, DSHandle>> vargs = (Channel<Pair<T, DSHandle>>) thr.popState(); 
        try {
            switch(i) {
                case 0:
                    Field field = this.field.getValue(stack);
                    var = this.var.getValue(stack);
                    if (var == null) {
                        var = newVar(field, getDMChecker(stack));
                    }
                    vargs = c_vargs.get(stack);
                    i++;
                default:
                    for (; i <= vargs.size(); i++) {
                        Pair<T, DSHandle> p = vargs.get(i - 1);
                        assign(var, p.s, p.t, stack);
                    }
                    var.closeShallow();
            }
        }
        catch (Yield y) {
            y.getState().push(vargs);
            y.getState().push(var);
            y.getState().push(i, 1);
            throw y;
        }
    }

    protected DSHandle newVar(Field field, DuplicateMappingChecker dmChecker) {
        return new RootFutureStructDataNode(field, dmChecker);
    }

    protected void assign(DSHandle var, T key, DSHandle t, Stack stack) {
        try {
            DSHandle dst = getField(var, key);
            deepCopy(dst, t, stack);
        }
        catch (NoSuchFieldException e) {
            throw new ExecutionException("Internal error: trying to set a field " +
                    "that does not exist for the target type (" + key + "). The " +
                            "compiler should have caught this.", e);
        }
        catch (InvalidPathException e) {
            throw new ExecutionException("Internal error: problem encountered in a deep copy", e);
        }    
    }

    protected abstract DSHandle getField(DSHandle var, T key) throws NoSuchFieldException;
}