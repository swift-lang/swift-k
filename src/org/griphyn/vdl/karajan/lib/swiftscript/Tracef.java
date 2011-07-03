package org.griphyn.vdl.karajan.lib.swiftscript;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;

/**
    Formatted trace output. <br>
    Example: tracef("\t%s\n", "hello"); <br>
    Differences from trace(): 
    1) respects \t, \n and \\;
    2) allows for typechecked format specifiers 
       (cf. {@link Sprintf}); 
    3) allows for consumption of variables without display (%k); 
    4) does not impose any formatting (commas, etc.).  <br><br>
 */
public class Tracef extends VDLFunction {

    private static final Logger logger = 
        Logger.getLogger(Tracef.class);
    
    static {
        setArguments(Tracef.class, new Arg[] { Arg.VARGS });
    }
    
    @Override
    protected Object function(VariableStack stack) 
    throws ExecutionException {
        AbstractDataNode[] args = waitForAllVargs(stack);

        String msg = Sprintf.format(args);
        logger.info(msg);
        System.out.print(msg);
        return null;
    }
}
