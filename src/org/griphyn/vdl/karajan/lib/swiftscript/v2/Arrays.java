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
 * Created on Mar 8, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import java.util.Iterator;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;
import org.griphyn.vdl.mapping.nodes.RootFutureArrayDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;

public class Arrays {
    private static class SliceState {
        public int start, end, crt;
        public ArrayHandle a;
        public RootFutureArrayDataNode r;
    }
    
    public static class Slice extends FTypes.AsyncReducer<RootFutureArrayDataNode, SliceState> {
        private ArgRef<Integer> start;
        private ArgRef<Integer> end;

        @Override
        protected RootFutureArrayDataNode makeReturn(SliceState acc) {
            return acc.r = new RootFutureArrayDataNode(Field.Factory.createField("?", acc.a.getType()), null);
        }

        @Override
        protected SliceState initial(Stack stack, ArrayHandle a) {
            int start = this.start.getValue(stack);
            int end = this.end.getValue(stack);
            if (start >= end) {
                throw new ExecutionException(this, "start (" + start+ ") >= end (" + end + ")");
            }
            SliceState acc = new SliceState();
            acc.start = start;
            acc.end = end;
                        
            acc.crt = acc.start;
            acc.a = a;
            return acc;
        }

        @Override
        protected boolean update(SliceState acc) {
            try {
                DSHandle o = acc.a.getField(acc.crt);
                acc.r.addField(acc.crt - acc.start, o);
                acc.crt++;
                return acc.crt < acc.end; 
            }
            catch (NoSuchFieldException e) {
                acc.r.setException(new ExecutionException(this, e));
                return false;
            }
        }

        @Override
        protected void finalizeReturn(RootFutureArrayDataNode r, SliceState acc) {
            if (acc.crt >= acc.end) {
                r.closeShallow();
            }
            else {
                // exception occurred
            }
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("a", "start", "end"));
        }
    }
    
    private static class SplitState {
        public int n, crt, chunkIndex;
        public RootFutureArrayDataNode r;
        public RootFutureArrayDataNode currentChunk;
        public Iterator<List<?>> a;
        public Type type;
    }
    
    public static class Split extends FTypes.AsyncReducer<RootFutureArrayDataNode, SplitState> {
        private ArgRef<Integer> n;

        @Override
        protected RootFutureArrayDataNode makeReturn(SplitState acc) {
            return acc.r;
        }

        @Override
        protected SplitState initial(Stack stack, ArrayHandle a) {
            int n = this.n.getValue(stack);
            if (n <= 0) {
                throw new ExecutionException("n must be greater than 0");
            }
            SplitState acc = new SplitState();
            acc.n = n;
            acc.crt = 0;
            // arrayType(): X -> X[int]
            acc.r = new RootFutureArrayDataNode(Field.Factory.createField("?", a.getType().arrayType()), null);
            acc.type = a.getType();
            acc.a = a.entryList().iterator();
            return acc;
        }

        @Override
        protected boolean update(SplitState acc) {
            if (acc.a.hasNext()) {
                List<?> pair = acc.a.next();
                Comparable<?> key = (Comparable<?>) pair.get(0);
                DSHandle value = (DSHandle) pair.get(1);
                if (acc.crt == 0) {
                    acc.currentChunk = new RootFutureArrayDataNode(Field.Factory.createField(0, acc.type), null);
                    acc.r.addField(acc.chunkIndex, acc.currentChunk);
                    acc.chunkIndex++;
                }
                acc.currentChunk.addField(key, value);
                acc.crt++;
                if (acc.crt == acc.n) {
                    acc.currentChunk.closeShallow();
                    acc.crt = 0;
                }
            }
            return acc.a.hasNext();
        }

        @Override
        protected void finalizeReturn(RootFutureArrayDataNode r, SplitState acc) {
            if (acc.crt != 0) {
                acc.currentChunk.closeShallow();
            }
            r.closeShallow();
        }
    }
    
    
}
