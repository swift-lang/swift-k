/*
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Flatten extends VDLFunction {
    
    static {
        setArguments(Flatten.class, new Arg[] { Arg.VARGS });
    }

    @Override
    protected Object function(VariableStack stack) throws ExecutionException {
        VariableArguments v = Arg.VARGS.get(stack);
        if (v.isEmpty()) {
            return "";
        }
        else {
            StringBuilder sb = new StringBuilder();
            flatten(sb, v.getAll());
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
    }

    private void flatten(StringBuilder sb, List l) {
        for (Object o : l) {
            if (o instanceof List) {
                flatten(sb, (List) o);
            }
            else {
                sb.append(TypeUtil.toString(o));
                sb.append('|');
            }
        }
    }
}
