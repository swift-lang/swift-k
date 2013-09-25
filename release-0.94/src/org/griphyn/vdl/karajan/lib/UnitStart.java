//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 13, 2012
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.griphyn.vdl.engine.Karajan;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.mapping.DSHandle;

public class UnitStart extends FlowNode {
	public static final Logger uslogger = Logger.getLogger(UnitStart.class);
    // keep compatibility with log()
    public static final Logger logger = Logger.getLogger("swift");
    
    public static final Arg.Positional TYPE = new Arg.Positional("type");
    public static final Arg.Optional NAME = new Arg.Optional("name", null);
    public static final Arg.Optional LINE = new Arg.Optional("line", null);
    public static final Arg.Optional OUTPUTS = new Arg.Optional("outputs", null);
    
    private Tracer tracer;
    private List<String> inputArgs, outputArgs;
    
    @Override
    protected void initializeStatic() {
        super.initializeStatic();
        String type = (String) TYPE.getStatic(this);
        if (type.equals("PROCEDURE")) {
            tracer = Tracer.getTracer((FlowNode) getParent(), "APPCALL");
        }
        else if (type.equals("COMPOUND")) {
            tracer = Tracer.getTracer((FlowNode) getParent(), "CALL");
        }
        if (tracer != null && tracer.isEnabled()) {
            populateArgNames();
        }
    }

    private void populateArgNames() {
        String outs = (String) getStaticArguments().get("outputs");
        if (outs != null && outs.length() > 0) {
            outputArgs = Arrays.asList(outs.split(","));
        }
        else {
            outputArgs = Collections.emptyList();
        }
        String args = (String) getParent().getStaticArguments().get("arguments");
        if (args != null && args.length() > 0) {
            inputArgs = new ArrayList<String>(Arrays.asList(args.split(",")));
            inputArgs.removeAll(outputArgs);
        }
        else {
            inputArgs = Collections.emptyList();
        }
        
    }

    @Override
    public void execute(VariableStack stack) throws ExecutionException {
        executeSimple(stack);
        complete(stack);
    }
    
    @Override
    public boolean isSimple() {
        return super.isSimple();
    }
    
    @Override
    public void executeSimple(VariableStack stack) throws ExecutionException {
        String type = (String) TYPE.getStatic(this);
        ThreadingContext thread = ThreadingContext.get(stack);
        String name = (String) NAME.getStatic(this);
        String line = (String) LINE.getStatic(this);
        
        if (tracer != null && tracer.isEnabled()) {
            tracer.trace(thread.toString(), Karajan.demangle(name) + "(" + formatArguments(stack) + ")");
        }
        
        log(true, type, thread, name, line);
        
        String outputs = (String) OUTPUTS.getStatic(this);
        if (outputs != null) {
            trackOutputs(stack, outputs, "SCOPE".equals(type));
        }
    }
    
    private String formatArguments(VariableStack stack) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String name : inputArgs) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(Karajan.demangle(name));
            sb.append(" = ");
            sb.append(Tracer.unwrapHandle(stack.parentFrame().getVar(name)));
        }
        return sb.toString();
    }

    private static final List<DSHandle> EMPTY_OUTPUTS = Collections.emptyList();

    private void trackOutputs(VariableStack stack, String outputs, boolean deep) {
    	if (outputs.length() != 0) {
            String[] names = outputs.split(",");
            List<DSHandle> l = new LinkedList<DSHandle>();
            for (String name : names) {
            	if (deep) {
            	    try {
                        l.add((DSHandle) stack.getVar(name.toLowerCase()));
                    }
                    catch (VariableNotFoundException e) {
                        logger.info("Could not find variable " + name, e);
                    }
            	}
            	else {
            		l.add((DSHandle) stack.parentFrame().getVar(name));
            	}
            }
            WaitingThreadsMonitor.addOutput(stack, l);
    	}
    }

    protected static void log(boolean start, String type, ThreadingContext thread, String name, String line) {
        if (type.equals("COMPOUND")) {
            logger.info((start ? "START" : "END") + type + " thread=" + thread + " name=" + name);
        }
        else if (type.equals("PROCEDURE")) {
            if (start) {
                logger.debug("PROCEDURE line=" + line + " thread=" + thread + " name=" + name);
            }
            else {
                logger.debug("PROCEDURE_END line=" + line + " thread=" + thread + " name=" + name);
            }
        }
        else if (type.equals("FOREACH_IT")) {
            logger.debug("FOREACH_IT_" + (start ? "START" : "END") + " line=" + line + " thread=" + thread);
            if (start) {
                logger.debug("SCOPE thread=" + thread);
            }
        }
        else if (type.equals("INTERNALPROC")) {
            logger.debug("INTERNALPROC_" + (start ? "START" : "END") + " thread=" + thread + " name=" + name);
        }
        else if (type.equals("CONDITION_BLOCK")) {
            if (start) {
                logger.debug("SCOPE thread=" + thread);
            }
        }
    }
}
