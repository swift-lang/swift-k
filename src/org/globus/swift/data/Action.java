package org.globus.swift.data;

import java.io.IOException;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.swift.data.policy.Broadcast;
import org.globus.swift.data.policy.Policy;

public class Action extends FunctionsCollection {

    public static final Arg PA_FILE = new Arg.Positional("srcfile");
    public static final Arg PA_DIR  = new Arg.Positional("srcdir");

    static {
        setArguments("cdm_broadcast", new Arg[]{ PA_FILE, PA_DIR });
    }

    public void cdm_broadcast(VariableStack stack) throws ExecutionException {
        String srcfile = (String) PA_FILE.getValue(stack);
        String srcdir  = (String) PA_DIR.getValue(stack);
        
        Policy policy = Director.lookup(srcfile);
        
        if (!(policy instanceof Broadcast)) {
            throw new RuntimeException("Attempting to BROADCAST the wrong file");
        }
        
        if (srcdir == "") { 
            srcdir = ".";
        }
        
        Broadcast broadcast   = (Broadcast) policy;
        broadcast.action(srcfile, srcdir);
    }
}
