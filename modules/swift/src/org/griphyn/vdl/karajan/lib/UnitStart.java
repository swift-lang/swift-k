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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;
import org.griphyn.vdl.mapping.DSHandle;

public class UnitStart extends InternalFunction {
    public static final Logger uslogger = Logger.getLogger(UnitStart.class);
    // keep compatibility with log()
    public static final Logger logger = Logger.getLogger("swift");

    private ArgRef<String> type;
    private ArgRef<String> name;
    private ArgRef<Integer> line;
    private ArgRef<String> arguments;
    private ArgRef<String> outputs;

    @Override
    protected Signature getSignature() {
        return new Signature(params("type", optional("name", null),
            optional("line", -1),
            optional("outputs", null), optional("arguments", null)));
    }

    private static class NamedRef {
        public final String name;
        public final VarRef<DSHandle> ref;

        public NamedRef(String name, VarRef<DSHandle> ref) {
            this.name = name;
            this.ref = ref;
        }
    }

    private Tracer tracer;
    private List<NamedRef> inputArgs, outputArgs;

    @Override
    public Node compile(WrapperNode w, Scope scope) throws CompilationException {
        Node fn = super.compile(w, scope);
        String type = this.type.getValue();
        if (type.equals("PROCEDURE")) {
            tracer = Tracer.getTracer(line.getValue(), "APPCALL");
        }
        else if (type.equals("COMPOUND")) {
            tracer = Tracer.getTracer(line.getValue(), "CALL");
        }
        if (tracer != null && tracer.isEnabled()) {
            populateArgNames(scope);
        }
        return fn;
    }

    private void populateArgNames(Scope scope) {
        String outs = this.outputs.getValue();
        Set<String> outNames = new HashSet<String>();
        if (outs != null && outs.length() > 0) {
            outputArgs = new ArrayList<NamedRef>();
            for (String name : outs.split(",")) {
                VarRef<DSHandle> ref = scope.getVarRef(name);
                outputArgs.add(new NamedRef(name, ref));
                outNames.add(name);
            }
        }
        else {
            outputArgs = null;
        }
        String args = this.arguments.getValue();
        if (args != null && args.length() > 0) {
            inputArgs = new ArrayList<NamedRef>();
            for (String name : args.split(",")) {
                if (outNames.contains(name)) {
                    continue;
                }
                VarRef<DSHandle> ref = scope.getVarRef(name);
                inputArgs.add(new NamedRef(name, ref));
            }
        }
        else {
            inputArgs = null;
        }

    }

    @Override
    protected void runBody(LWThread thr) {
        String type = this.type.getValue();
        String name = this.name.getValue();
        Integer line = this.line.getValue();

        if (tracer != null && tracer.isEnabled()) {
            tracer.trace(thr, name + "("
                    + formatArguments(thr.getStack()) + ")");
        }

        log(true, type, thr, name, line);

        if (outputArgs != null) {
            trackOutputs(thr);
        }
    }

    private String formatArguments(Stack stack) {
        if (inputArgs != null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (NamedRef nr : inputArgs) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append(", ");
                }
                sb.append(nr.name);
                sb.append(" = ");
                sb.append(Tracer.unwrapHandle(nr.ref.getValue(stack)));
            }
            return sb.toString();
        }
        else {
            return "";
        }
    }

    private static final List<DSHandle> EMPTY_OUTPUTS = Collections.emptyList();

    private void trackOutputs(LWThread thr) {
        Stack stack = thr.getStack();
        if (!outputArgs.isEmpty()) {
            List<DSHandle> l = new LinkedList<DSHandle>();
            for (NamedRef nr : outputArgs) {
                l.add(nr.ref.getValue(stack));
            }
            WaitingThreadsMonitor.addOutput(thr, l);
        }
    }

    protected static void log(boolean start, String type, LWThread thread,
            String name, int line) {
        if (logger.isInfoEnabled()) {
            String threadName = SwiftFunction.getThreadPrefix(thread);
            if (type.equals("COMPOUND")) {
                logger.info((start ? "START" : "END") + type + " thread="
                        + threadName + " name=" + name);
            }
            else {
                if (logger.isDebugEnabled()) {
                    if (type.equals("PROCEDURE")) {
                        if (start) {
                            logger.debug("PROCEDURE line=" + line + " thread="
                                    + threadName + " name=" + name);
                        }
                        else {
                            logger.debug("PROCEDURE_END line=" + line
                                    + " thread="
                                    + threadName + " name=" + name);
                        }
                    }
                    else if (type.equals("FOREACH_IT")) {
                        logger.debug("FOREACH_IT_" + (start ? "START" : "END")
                                + " line=" + line + " thread="
                                + threadName);
                        if (start) {
                            logger.debug("SCOPE thread=" + threadName);
                        }
                    }
                    else if (type.equals("INTERNALPROC")) {
                        logger.debug("INTERNALPROC_"
                                + (start ? "START" : "END")
                                + " thread=" + threadName + " name="
                                + name);
                    }
                    else if (type.equals("CONDITION_BLOCK")) {
                        if (start) {
                            logger.debug("SCOPE thread=" + threadName);
                        }
                    }
                }
            }
        }
    }
}
