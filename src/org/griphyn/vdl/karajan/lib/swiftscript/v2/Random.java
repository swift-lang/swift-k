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
 * Created on Mar 6, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.type.Field;

public class Random {
    private abstract static class AbstractRandom extends FTypes.SwiftFunction {
        protected ArgRef<Integer> seed;
        protected ArgRef<Integer> sequenceNum;
        private MessageDigest md;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("seed", "sequenceNum", "min", "max"));
        }
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            try {
                md = MessageDigest.getInstance("SHA-1");
            }
            catch (NoSuchAlgorithmException e) {
                throw new CompilationException(w, "Cannot get SHA-1 instance");
            }
            return super.compileBody(w, argScope, scope);
        }
        
        @Override
        public Object function(Stack stack) {
            int seed = this.seed.getValue(stack);
            int seq = this.sequenceNum.getValue(stack);
            return rnd(stack, seed, seq);
        }

        protected abstract Object rnd(Stack stack, int seed, int seq);

        protected long hash(int seed, int seq) {
            synchronized (md) {
                md.reset();
                update(md, seed);
                update(md, seq);
                return toLong(md.digest(), 0);
            }
        }
        
        protected byte[] hashFull(int seed, int seq) {
            synchronized (md) {
                md.reset();
                update(md, seed);
                update(md, seq);
                return md.digest();
            }
        }

        protected long toLong(byte[] buf, int start) {
            long n = 0;
            for (int i = 0; i < 8; i++) {
                n <<= 8;
                n = n + (buf[i + start] & 0x000000ff);
            }
            return n;
        }
        
        protected double toDouble(byte[] buf, int start) {
            long n = 0;
            for (int i = 0; i < 7; i++) {
                n <<= 8;
                n = n + (buf[i + start] & 0x000000ff);
            }
            return ((double) (n & 0xfffffffffffffL)) / 0xfffffffffffffL;
        }
        
        private void update(MessageDigest md, int n) {
            for (int i = 0; i < 4; i++) {
                md.update((byte) (n % 0x000000ff));
                n >>= 8;
            }
        }
    }
    
    public static class RandomInt extends AbstractRandom {
        private ArgRef<Integer> min;
        private ArgRef<Integer> max;
                
        
        @Override
        protected Object rnd(Stack stack, int seed, int seq) {
            /*
             * Use rejection sampling to get integer in specified range.
             * Reduce the rejection probability by using minimum numbers
             * of bits necessary to cover the range
             */
            int min = this.min.getValue(stack);
            int max = this.max.getValue(stack);
            int step = 0;
            long hash = hash(seed, seq);
            int range = max - min;
            long mask = getMask(range);
            while (true) {
                if ((hash & mask) < range) {
                    return (int) (hash & mask) + min; 
                }
                step++;
                hash = hash ^ hash(seed, seq + step);
            }
        }
        
        private long getMask(int range) {
            long l = 1;
            while (l < range) {
                l <<= 1;
                l = l + 1;
            }
            return l;
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static class RandomFloat extends AbstractRandom {
        private ArgRef<Double> min;
        private ArgRef<Double> max;
                
        @Override
        protected Object rnd(Stack stack, int seed, int seq) {
            /*
             * This is easier because doubles can be scaled easily
             */
            double min = this.min.getValue(stack);
            double max = this.max.getValue(stack);
            return min + (max - min) * toDouble(hashFull(seed, seq), 0);
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
    
    public static class GaussianRandom extends AbstractRandom {
        private static final double TWO_PI = java.lang.Math.PI * 2;
                
        @Override
        protected Object rnd(Stack stack, int seed, int seq) {
            /*
             * See Box-Muller transform
             */
            int mseq = seq - (seq % 2); 
            int step = 0;
            byte[] brnd = hashFull(seed, mseq);
            double u1 = 0, u2;
            while (true) {
                u1 = toDouble(brnd, 0);
                u2 = toDouble(brnd, 8);
                if (u1 > 0) {
                    break;
                }
                xor(brnd, hashFull(seed, mseq + (step++)));
            }
            
            double d;
            if (seq % 2 == 0) {
                d = java.lang.Math.cos(TWO_PI * u2);
            }
            else {
                d = java.lang.Math.sin(TWO_PI * u2);
            }
            d *= java.lang.Math.sqrt(-2.0 * java.lang.Math.log(u1));
            return d;
        }

        private void xor(byte[] d, byte[] s) {
            for (int i = 0; i < d.length; i++) {
                d[i] = (byte) ((d[i] ^ s[i]) & 0x000000ff);
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("seed", "sequenceNum"));
        }
        
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
}
