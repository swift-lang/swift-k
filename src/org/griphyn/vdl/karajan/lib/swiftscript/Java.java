package org.griphyn.vdl.karajan.lib.swiftscript;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class Java extends VDLFunction
{

    static
    {
        setArguments(Java.class, new Arg[] { Arg.VARGS });
    }

    protected Object function(VariableStack stack) throws ExecutionException
    {
        AbstractDataNode[] args = waitForAllVargs(stack);

        Method method = getMethod(args);
        Object[] p = convertInputs(method, args);
        Type type = returnType(method);
        Object value = invoke(method, p);
        DSHandle result = swiftResult(type, value);

        return result;
    }

    /**
       Given the user args, locate the Java Method.
    */

    Method getMethod(DSHandle[] args)
    {
        Method result;
        Class<?> clazz;

        String lib = "unset";
        String name = "unset";

        Class[] parameterTypes = new Class[args.length-2];

        if (args.length < 2)
            throw new RuntimeException
                ("@java() requires at least two arguments");

        try
        {
            lib = (String) args[0].getValue();
            name = (String) args[1].getValue();
            clazz = Class.forName(lib);

            for (int i = 2; i < args.length; i++)
            {
                Class p = null;
                Type  t = args[i].getType();

                if (t.equals(Types.FLOAT))        p = double.class;
                else if (t.equals(Types.INT))     p = int.class;
                else if (t.equals(Types.BOOLEAN)) p = boolean.class;
                else if (t.equals(Types.STRING))  p = String.class;
                else                              throw new RuntimeException("Cannot use @java with non-primitive types");

                parameterTypes[i-2] = p;
            }
            result = clazz.getMethod(name, parameterTypes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException
                ("@java(): Error attempting to use: " + args[0].getValue());
        }

        if (result == null)
            throw new RuntimeException
                ("No method: " + name + " in " + lib + "with parameter types" + Arrays.toString(parameterTypes));

        return result;
    }

    /**
       Convert the user args to a Java Object array.
    */
    Object[] convertInputs(Method method, DSHandle[] args)
    {
        Object[] result = new Object[args.length-2];
        Object a = null;
        try
        {
            for (int i = 2; i < args.length; i++)
            {
                Type   t = args[i].getType();
                Object v = args[i].getValue();
                if (t.equals(Types.FLOAT))
                    a = (Double) v;
                else if (t.equals(Types.INT))
                    a = (Integer) v;
                else if (t.equals(Types.BOOLEAN))
                    a = (Boolean) v;
                else if (t.equals(Types.STRING))
                    a = (String) v;
                result[i-2] = a;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException
                ("Error converting input arguments: \n" +
                 " to: " + method.getDeclaringClass() +
                 "." + method + " \n argument: " + a);
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

    DSHandle swiftResult(Type type, Object value)
    {
        DSHandle result = new RootDataNode(type);
        result.setValue(value);
        result.closeShallow();
        return result;
    }
}
