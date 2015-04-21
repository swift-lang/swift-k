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
 * Created on Mar 7, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;

public class Conversion {
    public static class ToInt extends FTypes.IntPFloat {
        @Override
        protected int v(double arg) {
            return (int) arg;
        }
    }
    
    public static class ToFloat extends FTypes.FloatPInt {
        @Override
        protected double v(int arg) {
            return arg;
        }
    }
    
    public static final DSHandle INT_TEN = NodeFactory.newRoot(Types.INT, 10); 
    
    public static class ParseInt extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        private ArgRef<Integer> base;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s", optional("base", INT_TEN)));
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            int base = this.base.getValue(stack);
            return Integer.parseInt(s, base);
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static class ParseFloat extends FTypes.SwiftFunction {
        private ArgRef<String> s;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("s"));
        }

        @Override
        public Object function(Stack stack) {
            String s = this.s.getValue(stack);
            return Double.parseDouble(s);
        }

        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
    
    public static class IntToString extends FTypes.StringPInt {
        @Override
        protected String v(int arg) {
            return String.valueOf(arg);
        }
    }
    
    public static class FloatToString extends FTypes.StringPFloat {
        @Override
        protected String v(double arg) {
            return String.valueOf(arg);
        }
    }
    
    public static class BoolToString extends FTypes.StringPBool {
        @Override
        protected String v(boolean arg) {
            return String.valueOf(arg);
        }
    }
}
