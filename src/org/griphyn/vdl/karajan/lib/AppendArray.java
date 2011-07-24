//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2011
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Path;

public class AppendArray extends SetFieldValue {
    
    public static final Arg PA_ID = new Arg.Positional("id");

    static {
        setArguments(AppendArray.class, new Arg[] { PA_VAR, PA_VALUE });
    }
    
    @Override
    public Object function(VariableStack stack) throws ExecutionException {
        DSHandle var = (DSHandle) PA_VAR.getValue(stack);
        AbstractDataNode value = (AbstractDataNode) PA_VALUE.getValue(stack);
        // while there isn't a way to avoid conflicts between auto generated indices
        // and a user manually using the same index, adding a "#" may reduce
        // the incidence of problems
        Path path = Path.EMPTY_PATH.addFirst(getThreadPrefix(stack), true);
        try {
            deepCopy(var.getField(path), value, stack, 0);
        }
        catch (InvalidPathException e) {
            throw new ExecutionException(e);
        }
        return null;
    }
}
