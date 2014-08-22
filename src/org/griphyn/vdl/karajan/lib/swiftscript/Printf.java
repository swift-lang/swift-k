package org.griphyn.vdl.karajan.lib.swiftscript;

import k.rt.Channel;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

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
public class Printf extends SwiftFunction {
    private static final Logger logger = Logger.getLogger(Printf.class);
    
    private ArgRef<AbstractDataNode> spec;
    private ChannelRef<AbstractDataNode> c_vargs;

    @Override
    protected Signature getSignature() {
        return new Signature(params("spec", "..."));
    }

    
    @Override
    public Object function(Stack stack) {
        AbstractDataNode hspec = this.spec.getValue(stack);
        String msg;
        try {
            hspec.waitFor(this);
            Channel<AbstractDataNode> args = c_vargs.get(stack);
            waitForAll(this, args);
            String spec = (String) hspec.getValue();
         
            msg = Sprintf.format(spec, args);
        }
        catch (DependentException e) {
            msg = "<exception>";
        }
        logger.info(msg);
        System.out.print(msg);
        return null;
    }
    
    public static void waitForAll(Node who, Channel<AbstractDataNode> vargs) {
        for (AbstractDataNode n : vargs) {
            try {
                n.waitFor(who);
            }
            catch (DependentException e) {
                // ignore here, will print special message in trace
            }
        }
    }

}
