//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

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
