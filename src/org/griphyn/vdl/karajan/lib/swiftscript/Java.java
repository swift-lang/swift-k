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


package org.griphyn.vdl.karajan.lib.swiftscript;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Java extends SwiftFunction {
    private ArgRef<AbstractDataNode> lib;
    private ArgRef<AbstractDataNode> name;
    private ChannelRef<AbstractDataNode> c_vargs;
    private Method method;
    private static Map<MethodEntry, Method> cachedMethods;
    
    static {
        cachedMethods = new WeakHashMap<MethodEntry, Method>();
    }
    
    private static class MethodEntry {
        private final String className;
        private final String methodName;
        private final Class<?>[] parameterTypes;
        
        public MethodEntry(String className, String methodName, Class<?>[] parameterTypes) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MethodEntry) {
                MethodEntry me = (MethodEntry) o;
                if (!className.equals(me.className)) {
                    return false;
                }
                if (!methodName.equals(me.methodName)) {
                    return false;
                }
                if (parameterTypes.length != me.parameterTypes.length) {
                    return false;
                }
                
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (!parameterTypes[i].equals(me.parameterTypes[i])) {
                        return false;
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int h = 0;
            h += className.hashCode();
            h += methodName.hashCode();
            for (Class<?> c : parameterTypes) {
                h += c.hashCode();
            }
            return h;
        }
    }
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("lib", "name", "..."));
    }
    
    
    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        if (lib.isStatic() && name.isStatic()) {
            AbstractDataNode lib = this.lib.getValue();
            AbstractDataNode name = this.name.getValue();
            if (lib != null && name != null && lib.isClosed() && name.isClosed()) {
                method = getSingleMethod(w, (String) lib.getValue(), (String) name.getValue());
            }
        }
        return super.compileBody(w, argScope, scope);
    }



    private Method getSingleMethod(WrapperNode w, String clsName, String methodName) 
            throws CompilationException {
        
        try {
            Class<?> cls = Class.forName(clsName);
            Method[] methods = cls.getMethods();
            Method result = null;
            for (Method m : methods) {
                if (methodName.equals(m.getName())) {
                    if (result == null) {
                        result = m;
                    }
                    else {
                        return null;
                    }
                }
            }
            return result;
        }
        catch (ClassNotFoundException e) {
            throw new CompilationException(w, e.getMessage());
        }
    }

    private static final DSHandle FALSE = NodeFactory.newRoot(Types.BOOLEAN, false);

    @Override
    public Object function(Stack stack) {
    	AbstractDataNode hlib = this.lib.getValue(stack);
    	AbstractDataNode hname = this.name.getValue(stack);
        Channel<AbstractDataNode> args = this.c_vargs.get(stack);

        hlib.waitFor(this);
        hname.waitFor(this);
        waitForAll(this, args);

        Method method = this.method;
        if (method == null) {
            method = getMethod((String) hlib.getValue(), (String) hname.getValue(), args);
        }
        Object[] p = convertInputs(method, args);
        Type type = returnType(method);
        Object value = invoke(method, p);
        if (type != null) {
            return NodeFactory.newRoot(type, value);
        }
        else {
            return FALSE;
        }
    }

    /**
       Given the user args, locate the Java Method.
    */

    private Method getMethod(String lib, String name, Channel<AbstractDataNode> args) {
        Class<?> clazz;

        Class<?>[] parameterTypes = new Class[args.size()];

        try {
            clazz = Class.forName(lib);

            for (int i = 0; i < args.size(); i++) {
                Type t = args.get(i).getType();
                parameterTypes[i] = getJavaType(t);
            }
            Method m = getCachedMethod(lib, name, parameterTypes);
            if (m != null) {
                return m;
            }
            m = searchForMethod(clazz, name, parameterTypes);
            if (m == null) {
                throw new ExecutionException(this, "No method: " 
                    + name + " in " + lib + " with parameter types" 
                    + Arrays.toString(parameterTypes));
            }
            else {
                cacheMethod(lib, name, parameterTypes, m);
                return m;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExecutionException(this, "Error attempting to use: " + lib);
        }
    }

    private Method searchForMethod(Class<?> cls, String name, Class<?>[] parameterTypes) {
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (name.equals(m.getName())) {
                if (parametersMatch(m, parameterTypes)) {
                    return m;
                }
            }
        }
        return null;
    }

    private boolean parametersMatch(Method m, Class<?>[] parameterTypes) {
        Class<?>[] methodPTypes = m.getParameterTypes();
        if (methodPTypes.length > parameterTypes.length) {
            return false;
        }
        boolean varargs = false;
        int n = methodPTypes.length;
        if (n <= parameterTypes.length) {
            // could be varargs
            if (m.isVarArgs()) {
                if (n == parameterTypes.length && parameterTypes[n - 1].isArray()) {
                    // varargs passed as array
                }
                else {
                    // only match the first n - 1 parameter types and pass the rest as varargs
                    n--;
                    varargs = true;
                }
            }
            else if (n < parameterTypes.length) {
                return false;
            }
        }
        for (int i = 0; i < n; i++) {
            Class<?> pt = checkWrapPrimitive(parameterTypes[i], methodPTypes[i]);
            if (!methodPTypes[i].isAssignableFrom(pt)) {
                return false;
            }
        }
        if (varargs) {
            // match vararg types
            Class<?> vargsType = methodPTypes[methodPTypes.length - 1];
            if (!vargsType.isArray()) {
                throw new RuntimeException("Method with varargs does not have array last parameter?");
            }
            Class<?> memberType = vargsType.getComponentType();
            for (int i = n; i < parameterTypes.length; i++) {
                Class<?> pt = checkWrapPrimitive(parameterTypes[i], memberType);
                if (!memberType.isAssignableFrom(pt)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Class<?> checkWrapPrimitive(Class<?> pt, Class<?> expected) {
        if (pt.isPrimitive() || (pt.isArray() && pt.getComponentType().isPrimitive())) {
            // for Swift int, try both int.class and Integer.class
            if (!expected.equals(pt)) {
                return getWrappedType(pt);
            }
        }
        return pt;
    }


    private Class<?> getWrappedType(Class<?> cls) {
        if (cls == int.class) {
            return Integer.class;
        }
        else if (cls == boolean.class) {
            return Boolean.class;
        }
        else if (cls == double.class) {
            return Double.class;
        }
        else if (cls == int[].class) {
            return Integer[].class;
        }
        else if (cls == boolean[].class) {
            return Boolean[].class;
        }
        else if (cls == double[].class) {
            return Double[].class;
        }
        else {
            return cls;
        }
    }


    private static synchronized Method getCachedMethod(String cls, String name, Class<?>[] parameterTypes) {
        return cachedMethods.get(new MethodEntry(cls, name, parameterTypes));
    }
    
    private static synchronized void cacheMethod(String cls, String name, Class<?>[] parameterTypes, Method m) {
        cachedMethods.put(new MethodEntry(cls, name, parameterTypes), m);
    }


    private Class<?> getJavaType(Type t) {
        if (t.equals(Types.FLOAT)) {
            return double.class;
        }
        else if (t.equals(Types.INT)) {
            return int.class;
        }
        else if (t.equals(Types.BOOLEAN)) {
            return boolean.class;
        }
        else if (t.equals(Types.STRING)) {
            return String.class;
        }
        else if (t.isArray() && t.keyType().equals(Types.INT)) {
            return arrayType(getJavaType(t.itemType()));
        }
        else if (t.isArray()) {
            return java.util.Map.class;
        }
        else {
            throw new RuntimeException("Cannot use @java with type " + t);
        }
    }




    private Class<?> arrayType(Class<?> javaType) {
        if (javaType == double.class) {
            return double[].class;
        }
        if (javaType == int.class) {
            return int[].class;
        }
        if (javaType == boolean.class) {
            return boolean[].class;
        }
        if (javaType == String.class) {
            return String[].class;
        }
        throw new RuntimeException("Cannot use @java with non-primitive types");
    }


    /**
       Convert the user args to a Java Object array.
    */
    private Object[] convertInputs(Method method, Channel<AbstractDataNode> args) {
        Class<?>[] methodParamTypes = method.getParameterTypes();
        int n = method.getParameterTypes().length;
        Object[] result = new Object[n];
        boolean varargs = false;
        if (method.isVarArgs() && (args.size() > n || !args.get(n - 1).getType().isArray())) {
            // so:
            // assuming f(String, Object...):
            // f("a", 1) -> invoked as f("a", [1])
            // f("a", 1, 2) -> invoked as f("a", [1, 2])
            // f{"a", [1, 2]) -> invoked as f("a", [1, 2])
            varargs = true;
            n--;
        }
        for (int i = 0; i < n; i++) {
            result[i] = javaValue(args.get(i));
        }
        if (varargs) {
            Object[] vargs = new Object[args.size() - n];
            for (int i = 0; i < vargs.length; i++) {
                vargs[i] = javaValue(args.get(n + i));
            }
            result[n] = vargs;
        }
        return result;
    }

    private Object javaValue(AbstractDataNode dn) {
        Type t = dn.getType();
        if (t.isPrimitive()) {
            return dn.getValue();
        }
        if (t.isArray()) {
            if (t.keyType().equals(Types.INT) && t.itemType().isPrimitive()) { 
                return unwrapArray(dn);
            }
            else {
                return unwrapMap(dn);
            }
        }
        throw new ExecutionException(this, "Don't know how to convert " + dn + " to a java object");
    }


    private Object unwrapMap(AbstractDataNode dn) {
        waitForArray(this, dn);
        Map<Object, Object> m = new HashMap<Object, Object>();
        Map<Comparable<?>, DSHandle> sa = dn.getArrayValue();
        for (Map.Entry<Comparable<?>, DSHandle> e : sa.entrySet()) {
            m.put(e.getKey(), javaValue((AbstractDataNode) e.getValue()));
        }
        return m;
    }


    private Object unwrapArray(AbstractDataNode dn) {
        waitForArray(this, dn);
        Map<Comparable<?>, DSHandle> sa = dn.getArrayValue();
        Object[] a = new Object[sa.size()];
        int i = 0;
        for (DSHandle h : sa.values()) {
            a[i++] = javaValue((AbstractDataNode) h);
        }
        return a;
    }


    private Type returnType(Method method) {
        Type result = null;

        Class<?> rt = method.getReturnType();
        if (rt.equals(Double.TYPE) || rt.equals(Double.class))
            result = Types.FLOAT;
        else if (rt.equals(Integer.TYPE) || rt.equals(Integer.class))
            result = Types.INT;
        else if (rt.equals(Boolean.TYPE) || rt.equals(Boolean.class))
            result = Types.BOOLEAN;
        else if (rt.equals(String.class))
            result = Types.STRING;
        return result;
    }

    private Object invoke(Method method, Object[] p) {
        try {
            return method.invoke(null, p);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error attempting to invoke: " +
                 method);
        }
    }
}
