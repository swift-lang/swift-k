//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 17, 2014
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class SwiftFunctionsCollection extends FunctionsCollection {
    private static Map<String, Type> returnTypes;
    
    static {
        returnTypes = new HashMap<String, Type>();
    }
    
    protected static void setReturnType(String name, Type rt) {
        returnTypes.put(name, rt);
    }
    
    protected Type getReturnType() {
        Type t = returnTypes.get(getMethod().getName());
        if (t == null) {
            return Types.ANY;
        }
        else {
            return t;
        }
    }
    
    public Object function(VariableStack stack) throws ExecutionException {
        try {
            return getMethod().invoke(this, stack);
        }
        catch (InvocationTargetException e) {
            Throwable ex = e.getTargetException();
            if (ex instanceof ExecutionException) {
                throw (ExecutionException) ex;
            }
            else if (ex instanceof FutureNotYetAvailable) {
                throw (FutureNotYetAvailable) ex;
            }
            else if (ex instanceof DependentException) {
                RootDataNode rdn = new RootDataNode(getReturnType());
                rdn.setValue(new DataDependentException(rdn, (DependentException) ex));
                return rdn;
            }
            throw new ExecutionException(e.getTargetException());
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
}
