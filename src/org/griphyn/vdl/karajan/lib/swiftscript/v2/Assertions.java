//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 25, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;

public class Assertions {    
    private static final DSHandle MSG_ASSERTION_FAILED = 
        NodeFactory.newRoot(Types.STRING, "assertion failed");
    private static final DSHandle FLOAT_ZERO = 
        NodeFactory.newRoot(Types.FLOAT, 0.0);
    
    protected static String toStr(Object o) {
        if (o instanceof String) {
            return "\"" + o + '"';
        }
        else {
            return String.valueOf(o);
        }
    }
    
    private abstract static class Assertion extends FTypes.SwiftFunction {
        protected ArgRef<String> msg;

        @Override
        protected Field getReturnType() {
            return null;
        }

        @Override
        public Object function(Stack stack) {
            String msg = checkAssertion(stack);
            if (msg != null) {
                throw new ExecutionException(this, msg);
            }
            return null;
        }
        
        protected abstract String checkAssertion(Stack stack);
    }

    public static class AssertLTE extends Assertion {
        private ArgRef<Comparable<Object>> v1;
        private ArgRef<Comparable<Object>> v2;
        
        @Override
        protected String checkAssertion(Stack stack) {
            Comparable<Object> v1 = this.v1.getValue(stack);
            Comparable<Object> v2 = this.v2.getValue(stack);
            if (v1.compareTo(v2) <= 0) {
                return null;
            }
            else {
                return this.msg.getValue(stack) + " (" + toStr(v1) + " <= " + toStr(v2) + ")";
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("v1", "v2", optional("msg", MSG_ASSERTION_FAILED)));
        }
    }

    public static class AssertLT extends Assertion {
        private ArgRef<Comparable<Object>> v1;
        private ArgRef<Comparable<Object>> v2;
        
        @Override
        protected String checkAssertion(Stack stack) {
            Comparable<Object> v1 = this.v1.getValue(stack);
            Comparable<Object> v2 = this.v2.getValue(stack);
            if (v1.compareTo(v2) < 0) {
                return null;
            }
            else {
                return this.msg.getValue(stack) + " (" + toStr(v1) + " < " + toStr(v2) + ")";
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("v1", "v2", optional("msg", MSG_ASSERTION_FAILED)));
        }
    }

    public static class AssertEqual extends Assertion {
        private ArgRef<Object> v1;
        private ArgRef<Object> v2;
        
        @Override
        protected String checkAssertion(Stack stack) {
            Object v1 = this.v1.getValue(stack);
            Object v2 = this.v2.getValue(stack);
            if (v1.equals(v2)) {
                return null;
            }
            else {
                return this.msg.getValue(stack) + " (" + toStr(v1) + " == " + toStr(v2) + ")";
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("v1", "v2", optional("msg", MSG_ASSERTION_FAILED)));
        }
    }
    
    public static class AssertEqualFloat extends Assertion {
        private ArgRef<Number> v1;
        private ArgRef<Number> v2;
        
        @Override
        protected String checkAssertion(Stack stack) {
            Number v1 = this.v1.getValue(stack);
            Number v2 = this.v2.getValue(stack);
            
            if (v1.doubleValue() == v2.doubleValue()) {
                return null;
            }
            else {
                return this.msg.getValue(stack) + " (" + v1.doubleValue() + 
                    " == " + v2.doubleValue() + ")";
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("v1", "v2", 
                optional("msg", MSG_ASSERTION_FAILED)));
        }
    }
    
    public static class AssertAlmostEqual extends Assertion {
        private ArgRef<Number> v1;
        private ArgRef<Number> v2;
        private ArgRef<Number> tolerance;
        
        @Override
        protected String checkAssertion(Stack stack) {
            Number v1 = this.v1.getValue(stack);
            Number v2 = this.v2.getValue(stack);
            Number tol = this.tolerance.getValue(stack);
            
            if (java.lang.Math.abs(v1.doubleValue() - v2.doubleValue()) <= tol.doubleValue()) {
                return null;
            }
            else {
                return this.msg.getValue(stack) + " (" + v1.doubleValue() + 
                    " == " + v2.doubleValue() + " +/- " + tol.doubleValue() + ")";
            }
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("v1", "v2", "tolerance", 
                optional("msg", MSG_ASSERTION_FAILED)));
        }
    }


    public static class Assert extends Assertion {
        private ArgRef<Boolean> condition;

        @Override
        protected String checkAssertion(Stack stack) {
            if (this.condition.getValue(stack)) {
                return null;
            }
            else {
                return this.msg.getValue(stack);
            }
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("condition", optional("msg", MSG_ASSERTION_FAILED)));
        }
    }

}
