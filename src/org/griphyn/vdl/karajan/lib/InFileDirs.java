/*
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.griphyn.vdl.mapping.AbsFile;

public class InFileDirs extends AbstractSequentialWithArguments {
    public static final Arg STAGEINS = new Arg.Positional("stageins");
    
    static {
        setArguments(InFileDirs.class, new Arg[] { STAGEINS });
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEINS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        for (Object f : files) { 
        	String path = (String) f;
            String dir = new AbsFile(path).getDir();
            // there could be a clash here since
            // "/a/b/c.txt" would be remotely the same
            // as "a/b/c.txt". Perhaps absolute paths
            // should have a unique prefix.
            if (dir.startsWith("/") && dir.length() != 1) {
            	ret.append(dir.substring(1));
            }
            else if (dir.length() != 0) {
                ret.append(dir);
            }
        }
        super.post(stack);
    }
}
