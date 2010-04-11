package org.globus.swift.data;

import java.io.IOException;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.swift.data.policy.Broadcast;
import org.globus.swift.data.policy.Policy;

/**
 * Karajan-accessible CDM functions that change something.
 * */
public class Action extends FunctionsCollection {

    public static final Arg PA_FILE = new Arg.Positional("srcfile");
    public static final Arg PA_DIR  = new Arg.Positional("srcdir");

    static {
        setArguments("cdm_broadcast", new Arg[]{ PA_FILE, PA_DIR });
        setArguments("cdm_wait", new Arg[]{});
    }

    /**
       Register a file for broadcast by CDM.
       The actual broadcast is triggered by {@link cdm_wait}.
    */
    public void cdm_broadcast(VariableStack stack) throws ExecutionException {
        String srcfile = (String) PA_FILE.getValue(stack);
        String srcdir  = (String) PA_DIR.getValue(stack);

        System.out.println("cdm_broadcast()");
        
        Policy policy = Director.lookup(srcfile);
        
        if (!(policy instanceof Broadcast)) {
            throw new RuntimeException
                ("Attempting to BROADCAST the wrong file: directory: `" +
                 srcdir + "' `" + srcfile + "' -> " + policy);
        }
        
        if (srcdir == "") { 
            srcdir = ".";
        }

        Director.addBroadcast(srcdir, srcfile);
    }

    /**
       Wait until CDM has ensured that all data has been propagated.
    */
    public void cdm_wait(VariableStack stack) throws ExecutionException {
        System.out.println("cdm_wait()");
        Director.doBroadcast();
    }
}
