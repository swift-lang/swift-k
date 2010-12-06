package org.globus.swift.data;

import org.apache.log4j.Logger;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.FunctionsCollection;
import org.globus.swift.data.policy.Broadcast;
import org.globus.swift.data.policy.External;
import org.globus.swift.data.policy.Policy;

/**
 * Karajan-accessible CDM functions that change something.
 * */
public class Action extends FunctionsCollection {
    private static final Logger logger = 
        Logger.getLogger(Action.class);
    
    public static final Arg PA_SRCFILE  = 
        new Arg.Positional("srcfile");
    public static final Arg PA_SRCDIR   = 
        new Arg.Positional("srcdir");
    public static final Arg PA_DESTHOST = 
        new Arg.Positional("desthost");
    public static final Arg PA_DESTDIR  = 
        new Arg.Positional("destdir");

    static {
        setArguments("cdm_broadcast", new Arg[]{ PA_SRCFILE, 
                                                 PA_SRCDIR });
        setArguments("cdm_external", new Arg[]{ PA_SRCFILE, 
                                                PA_SRCDIR, 
                                                PA_DESTHOST, 
                                                PA_DESTDIR });
        setArguments("cdm_wait", new Arg[]{});
    }

    /**
       Register a file for broadcast by CDM.
       The actual broadcast is triggered by {@link cdm_wait}.
    */
    public void cdm_broadcast(VariableStack stack) 
    throws ExecutionException {
        String srcfile = (String) PA_SRCFILE.getValue(stack);
        String srcdir  = (String) PA_SRCDIR.getValue(stack);

        logger.debug("cdm_broadcast()");
        
        Policy policy = Director.lookup(srcfile);
        
        if (!(policy instanceof Broadcast)) {
            throw new RuntimeException
                ("Attempting to BROADCAST the wrong file: " +
                 srcdir + " " + srcfile + " -> " + policy);
        }
        
        if (srcdir == "") { 
            srcdir = ".";
        }

        Director.addBroadcast(srcdir, srcfile);
    }

    public void cdm_external(VariableStack stack) 
    throws ExecutionException
    {
        String srcfile  = (String) PA_SRCFILE.getValue(stack);
        String srcdir   = (String) PA_SRCDIR.getValue(stack);
        BoundContact bc = (BoundContact) PA_DESTHOST.getValue(stack);
        String destdir  = (String) PA_DESTDIR.getValue(stack);
        
        if (srcdir.length() == 0)
            srcdir = ".";
        String desthost = bc.getHost();
        String workdir = (String) bc.getProperty("workdir");
        
        External.doExternal(srcfile, srcdir, 
                            desthost, workdir+"/"+destdir);
    }
    
    /**
       Wait until CDM has ensured that all data has been propagated.
    */
    public void cdm_wait(VariableStack stack) 
    throws ExecutionException {
        logger.debug("cdm_wait()");
        Director.doBroadcast();
    }
}
