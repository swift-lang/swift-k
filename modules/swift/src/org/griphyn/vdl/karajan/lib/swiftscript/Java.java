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
import java.util.Arrays;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Java extends SwiftFunction {
    private ArgRef<AbstractDataNode> lib;
    private ArgRef<AbstractDataNode> name;
    private ChannelRef<AbstractDataNode> c_vargs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("lib", "name", "..."));
    }

    @Override
    public Object function(Stack stack) {
    	AbstractDataNode hlib = this.lib.getValue(stack);
    	AbstractDataNode hname = this.name.getValue(stack);
        Channel<AbstractDataNode> args = this.c_vargs.get(stack);

        hlib.waitFor(this);
        hname.waitFor(this);
        waitForAll(this, args);

        Method method = getMethod((String) hlib.getValue(), (String) hname.getValue(), args);
        Object[] p = convertInputs(method, args);
        Type type = returnType(method);
        Object value = invoke(method, p);
        return NodeFactory.newRoot(type, value);
    }

    /**
       Given the user args, locate the Java Method.
    */

    private Method getMethod(String lib, String name, Channel<AbstractDataNode> args) {
        Method result;
        Class<?> clazz;

        Class<?>[] parameterTypes = new Class[args.size()];

        try {
            clazz = Class.forName(lib);

            for (int i = 0; i < args.size(); i++) {
                Class<?> p = null;
                Type t = args.get(i).getType();

                if (t.equals(Types.FLOAT))        p = double.class;
                else if (t.equals(Types.INT))     p = int.class;
                else if (t.equals(Types.BOOLEAN)) p = boolean.class;
                else if (t.equals(Types.STRING))  p = String.class;
                else                              throw new RuntimeException("Cannot use @java with non-primitive types");

                parameterTypes[i] = p;
            }
            result = clazz.getMethod(name, parameterTypes);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExecutionException(this, "@java(): Error attempting to use: " + lib);
        }

        if (result == null)
            throw new ExecutionException(this, "No method: " 
            		+ name + " in " + lib + "with parameter types" 
            		+ Arrays.toString(parameterTypes));

        return result;
    }

    /**
       Convert the user args to a Java Object array.
    */
    private Object[] convertInputs(Method method, Channel<AbstractDataNode> args) {
        Object[] result = new Object[args.size()];
        Object a = null;
        for (int i = 0; i < args.size(); i++) {
            result[i] = args.get(i).getValue();
        }
        return result;
    }

    Type returnType(Method method) {
        Type result = null;

        Class<?> rt = method.getReturnType();
        if (rt.equals(Double.TYPE))
            result = Types.FLOAT;
        else if (rt.equals(Integer.TYPE))
            result = Types.INT;
        else if (rt.equals(Boolean.TYPE))
            result = Types.BOOLEAN;
        else if (rt.equals(String.class))
            result = Types.STRING;
        return result;
    }

    Object invoke(Method method, Object[] p)
    {
        Object result = null;
        try
        {
            result = method.invoke(null, p);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException
                ("Error attempting to invoke: " +
                 method.getDeclaringClass() + "." + method);
        }
        return result;
    }
}
