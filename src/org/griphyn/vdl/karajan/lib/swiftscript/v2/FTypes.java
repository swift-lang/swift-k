//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 6, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.KRunnable;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.SwiftBinaryOp;
import org.griphyn.vdl.karajan.lib.SwiftUnaryOp;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.mapping.nodes.RootClosedArrayDataNode;
import org.griphyn.vdl.mapping.nodes.RootClosedPrimitiveDataNode;
import org.griphyn.vdl.type.Field;

public class FTypes {
    
    protected static double widen(Object n) {
        return ((Number) n).doubleValue();
    }

    public static abstract class FloatPFloat extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v(widen(v1.getValue())));
        }
    
        protected abstract double v(double arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
    
    public static abstract class StringPInt extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Integer) v1.getValue()));
        }
    
        protected abstract String v(int arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class StringPFloat extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v(widen(v1.getValue())));
        }
    
        protected abstract String v(double arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class StringPBool extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Boolean) v1.getValue()));
        }
    
        protected abstract String v(boolean arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class StringPString extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((String) v1.getValue()));
        }
    
        protected abstract String v(String arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class IntPFloat extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v(widen(v1.getValue())));
        }
    
        protected abstract int v(double arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static abstract class FloatPInt extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Integer) v1.getValue()));
        }
    
        protected abstract double v(int arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
    
    public static abstract class FloatPFloatFloat extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v(widen(v1.getValue()), widen(v2.getValue())));
        }
    
        protected abstract double v(double arg1, double arg2);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }

    public static abstract class FloatPNumNum extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Number) v1.getValue(), (Number) v2.getValue()));
        }
    
        protected abstract double v(Number arg1, Number arg2);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }

    public static abstract class IntPInt extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Integer) v1.getValue()));
        }
    
        protected abstract int v(int arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static abstract class IntPIntInt extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v((Integer) v1.getValue(), (Integer) v2.getValue()));
        }
    
        protected abstract int v(int arg1, int arg2);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static abstract class AsyncReducer<R, S> extends SwiftFunction {
        private ArgRef<ArrayHandle> a;
        
        @Override
        public Object function(Stack stack) {
            // not used
            return null;
        }

        @Override
        public void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            try {
                ArrayHandle a = this.a.getValue(stack);
                final S acc = initial(stack, a);
                final R r = makeReturn(acc);
                final DSHandle rh = retNode(stack, r);
                
                thr.fork(new KRunnable() {
                    @Override
                    public void run(LWThread thr) {
                        try {
                            while (update(acc)) {
                                // just loop
                            }
                            finalizeReturn(r, acc);
                        }
                        catch (DependentException e) {
                            rh.setValue(e);
                        }
                    }
                });
            }
            catch (DependentException e) {
                ret(stack, NodeFactory.newRoot(getReturnType(), e));
            }
        }
        
        protected abstract R makeReturn(S acc);
        
        protected abstract S initial(Stack stack, ArrayHandle a);
        
        protected abstract boolean update(S acc);
        
        protected abstract void finalizeReturn(R r, S acc);
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("a"));
        }
        
        protected Field getReturnType() {
            return Field.GENERIC_ANY;
        }
    }
    
    public static abstract class Reducer<R, T, S, A> extends AbstractFunction {

        @Override
        public Object function(Stack stack) {
            // not used
            return null;
        }

        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            returnDynamic(scope);
            return super.compileBody(w, argScope, scope);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runBody(LWThread thr) {
            int i = thr.checkSliceAndPopState();
            Iterator<A> entries = (Iterator<A>) thr.popState();
            S current = (S) thr.popState();
            AbstractDataNode n = (AbstractDataNode) thr.popState();
            Stack stack = thr.getStack();
            try {
                switch (i) {
                    case 0:
                        entries = getIterator(stack);
                        current = initial(stack);
                        i++;
                    case 1:
                        if (n != null) {
                            n.waitFor(this);
                            current = reduceOne(current, (T) n.getValue());
                        }
                        while (entries.hasNext()) {
                            n = getItem(entries.next());
                            n.waitFor(this);
                            Object value = n.getValue();
                            n = null;
                            current = reduceOne(current, (T) value);
                        }
                        i++;
                    case 2:
                        this.ret(stack, makeReturn(getValue(current)));
                }
            }
            catch (DependentException e) {
                this.ret(stack, NodeFactory.newRoot(getReturnType(), e));
            }
            catch (Yield y) {
                y.getState().push(n);
                y.getState().push(current);
                y.getState().push(entries);
                y.getState().push(i);
                throw y;
            }
        }
        
        protected abstract AbstractDataNode getItem(A next);

        protected abstract Iterator<A> getIterator(Stack stack);

        protected Object makeReturn(R value) {
            return new RootClosedPrimitiveDataNode(getReturnType(), value);
        }

        protected abstract S initial(Stack stack);

        protected abstract S reduceOne(S current, T value);
        
        protected abstract R getValue(S current);
        
        protected Field getReturnType() {
            return Field.GENERIC_ANY;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void setArg(WrapperNode w, Param r, Object value) throws CompilationException {
            if (r.name.equals("array")) {
                // don't unwrap the array
                super.setArg(w, r, value);
            }
            else if (r.name.charAt(0) == '_') {
                super.setArg(w, r, value);
            }
            else {
                super.setArg(w, r, new ArgRefWrapper((ArgRef<AbstractDataNode>) value, this));
            }
        }
    }
    
    public static abstract class ArrayReducer<R, T, S> extends Reducer<R, T, S, List<?>> {
        private ArgRef<ArrayHandle> array;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("array"), returns(channel("...", 1)));
        }

        @Override
        protected AbstractDataNode getItem(List<?> next) {
            return (AbstractDataNode) next.get(1);
        }

        @Override
        protected Iterator<List<?>> getIterator(Stack stack) {
            return array.getValue(stack).entryList().iterator();
        }
    }
    
    public static abstract class VargsReducer<R, T, S> extends Reducer<R, T, S, AbstractDataNode> {
        private ChannelRef<AbstractDataNode> c_vargs;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }

        @Override
        protected AbstractDataNode getItem(AbstractDataNode next) {
            return next;
        }

        @Override
        protected Iterator<AbstractDataNode> getIterator(Stack stack) {
            return c_vargs.get(stack).iterator();
        }
    }

    
    public static abstract class ArrayReducerInt<T, S> extends ArrayReducer<Integer, T, S> {
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
    }
    
    public static abstract class ArrayReducerFloat<T, S> extends ArrayReducer<Double, T, S> {
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_FLOAT;
        }
    }
    
    public static abstract class ArrayReducerString<T, S> extends ArrayReducer<String, T, S> {
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class VargsReducerString<T, S> extends VargsReducer<String, T, S> {
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_STRING;
        }
    }
    
    public static abstract class BoolPFloat extends SwiftUnaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1) {
            return new RootClosedPrimitiveDataNode(getReturnType(), v(widen(v1.getValue())));
        }
    
        protected abstract boolean v(double arg);
    
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
    }
        
    private static class ArgRefWrapper<T> extends ArgRef<T> {
        private ArgRef<AbstractDataNode> wrapped;
        private Node owner;
        
        public ArgRefWrapper(ArgRef<AbstractDataNode> wrapped, Node owner) {
            this.wrapped = wrapped;
            this.owner = owner;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public T getValue(Stack stack) {
            AbstractDataNode dn = wrapped.getValue(stack);
            dn.waitFor(owner);
            return (T) dn.getValue();
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getValue() {
            AbstractDataNode dn = wrapped.getValue();
            dn.waitFor(owner);
            return (T) dn.getValue();
        }
        
        @Override
        public boolean isStatic() {
            return wrapped.isStatic();
        }
    }
    
    private static enum ReturnMode {
        PRIMITIVE, ARRAY, OBJECT, VOID;
    }
    
    public static abstract class SwiftFunction extends AbstractFunction {
        private ReturnMode returnMode;
        
        public SwiftFunction() {
            Field returnType = getReturnType();
            if (returnType == null) {
                returnMode = ReturnMode.VOID;
            }
            else if (returnType.getType().isPrimitive()) {
                returnMode = ReturnMode.PRIMITIVE;
            }
            else if (returnType.getType().isArray()) {
                returnMode = ReturnMode.ARRAY;
            }
            else {
                returnMode = ReturnMode.OBJECT;
            }
        }
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            returnDynamic(scope);
            return super.compileBody(w, argScope, scope);
        }
        
        @Override
        public void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            try {
                retNode(stack, function(stack));
            }
            catch (DependentException e) {
                if (returnMode != ReturnMode.VOID) {
                    ret(stack, NodeFactory.newRoot(getReturnType(), e));
                }
            }
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void setArg(WrapperNode w, Param r, Object value) throws CompilationException {
            if (r.name.charAt(0) == '_') {
                super.setArg(w, r, value);
            }
            else {
                super.setArg(w, r, new ArgRefWrapper((ArgRef<AbstractDataNode>) value, this));
            }
        }

        protected DSHandle retNode(Stack stack, Object value) {
            Field returnType = getReturnType();
            DSHandle h;
            switch (returnMode) {
                case PRIMITIVE:
                    h = new RootClosedPrimitiveDataNode(returnType, value);
                    break;
                case OBJECT:
                    h = (DSHandle) value;
                    break;
                case ARRAY:
                    h = new RootClosedArrayDataNode(returnType, (List<?>) value, null);
                    break;
                case VOID:
                    return null;
                default:
                    throw new ExecutionException(this, "Internal error");
            }
            super.ret(stack, h);
            return h;
        }
        
        protected abstract Field getReturnType();

        @Override
        public String getTextualName() {
            String name = getType();
            int ix = name.indexOf("$");
            if (ix == -1) {
                return name;
            }
            else {
                return name.substring(0, ix);
            }
        }    
    }
}
