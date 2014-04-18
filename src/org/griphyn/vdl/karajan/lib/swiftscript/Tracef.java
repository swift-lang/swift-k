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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.karajan.lib.VDLFunction;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DependentException;

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
        try {
            AbstractDataNode[] args = waitForAllVargs(stack);
    
            String msg = Sprintf.format(args);
            logger.info(msg);
            System.out.print(msg);
        }
        catch (DependentException e) {
            logger.info("tracef(): <exception>");
        }
        return null;
    }
}
