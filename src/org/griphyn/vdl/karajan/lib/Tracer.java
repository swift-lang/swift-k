//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 9, 2012
 */
package org.griphyn.vdl.karajan.lib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.griphyn.vdl.engine.Karajan;
import org.griphyn.vdl.karajan.FutureWrapper;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.VDL2Config;

public class Tracer {
    public static final Logger logger = Logger.getLogger("TRACE");
    private static boolean globalTracingEnabled;
    private static final Map<String, String> NAME_MAPPINGS;
    private ThreadLocal<String> thread = new ThreadLocal<String>();
    
    static {
        try {
            globalTracingEnabled = VDL2Config.getConfig().isTracingEnabled();
        }
        catch (IOException e) {
            globalTracingEnabled = false;
        }
        NAME_MAPPINGS = new HashMap<String, String>();
        NAME_MAPPINGS.put("assignment", "ASSIGN");
        NAME_MAPPINGS.put("iterate", "ITERATE");
        NAME_MAPPINGS.put("vdl:new", "DECLARE");
    }
    
    private final String source;
    private final boolean enabled;
    
    private Tracer(boolean enabled) {
        source = null;
        this.enabled = enabled;
    }
    
    private Tracer(FlowNode fe, String name) {
        source = buildSource(fe, name);
        if (source == null) {
            enabled = false;
        }
        else {
            enabled = true;
        }
    }
    
    private Tracer(String line, String name) {
        source = buildSource(line, name);
        enabled = true;
    }
    
    private Tracer(String name) {
        source = name;
        enabled = true;
    }
    
    private Tracer(FlowNode fe) {
        this(fe, null);
    }
    
    private String buildSource(FlowNode fe, String name) {
        String line = findLine(fe);
        if (line == null) {
            return null;
        }
        if (name == null) {
            name = getType(fe);
        }
        return buildSource(line, name);      
    }

    private String buildSource(String line, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(", line ");
        sb.append(line);
        return sb.toString();
    }

    private String getType(FlowNode fe) {
        String t = Karajan.demangle(fe.getTextualName());
        String nt = NAME_MAPPINGS.get(t);
        if (nt == null) {
            return t;
        }
        else {
            return nt;
        }
    }

    private String findLine(FlowElement fe) {
        String line;
        if (fe.hasProperty("_traceline")) {
            line = (String) fe.getProperty("_traceline");
        }
        else if (fe.hasProperty("_defline")) {
            line = (String) fe.getProperty("_defline");
        }
        else {
            line = null;
        }
        if (line == null || line.equals("-1") || line.equals("")) {
            return null;
        }
        else {
            return line;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void trace(VariableStack stack, Object msg) throws VariableNotFoundException {
        trace(ThreadingContext.get(stack).toString(), msg);
    }
    
    public void trace(String thread, Object msg) {
        String str = source + ", thread " + thread + ", " + msg;
        logger.info(str);
        System.out.println(str);
    }
    
    public void trace(String thread, String name, String line, Object msg) {
        if (line == null) {
            return;
        }
        String str = name + ", line " + line + ", thread " + thread + ", "+ msg;
        logger.info(str);
        System.out.println(str);
    }
    
    public void trace(String thread, String line, Object msg) {
        if (line == null) {
            return;
        }
        String str = source + ", line " + line + ", thread " + thread + ", " + msg;
        logger.info(str);
        System.out.println(str);
    }
    
    public void trace(String thread) {
        logger.info(source + ", thread " + thread);
        System.out.println(source + ", thread " + thread);
    }
    
    private static Tracer disabledTracer, enabledTracer;
    
    public static Tracer getTracer(FlowNode fe) {
        return getTracer(fe, null);
    }
    
    public static Tracer getTracer(FlowNode fe, String name) {
        if (globalTracingEnabled) {
            return new Tracer(fe, name);
        }
        else {
            return getGenericTracer(false);
        }
    }
    
    public static Tracer getTracer(String line, String name) {
        if (globalTracingEnabled) {
            return new Tracer(line, name);
        }
        else {
            return getGenericTracer(false);
        }
    }
    
    public static Tracer getTracer(String name) {
        if (globalTracingEnabled) {
            return new Tracer(name);
        }
        else {
            return getGenericTracer(false);
        }
    }
    
    public static Tracer getTracer() {
        return getGenericTracer(globalTracingEnabled);
    }

    private synchronized static Tracer getGenericTracer(boolean enabled) {
        if (enabled) {
            if (enabledTracer == null) {
                enabledTracer = new Tracer(true);
            }
            return enabledTracer;
        }
        else {
            if (disabledTracer == null) {
                disabledTracer = new Tracer(false);
            }
            return disabledTracer;
        }
    }
    
    public static String getVarName(DSHandle var) {
        if (var instanceof AbstractDataNode) {
            AbstractDataNode data = (AbstractDataNode) var;
            Path path = data.getPathFromRoot();
            String p;
            if (path.isEmpty()) {
                p = "";
            }
            else if (path.isArrayIndex(0)) {
                p = path.toString();
            }
            else {
                p = "." + path.toString();
            }
            return data.getDisplayableName() + p;
        }
        else {
            return String.valueOf(var);
        }
    }
    
    public static String getFutureName(Future future) {
        if (future instanceof FutureWrapper) {
            return getVarName(((FutureWrapper) future).getHandle());
        }
        else {
            return future.toString();
        }
    }
    
    public static Object unwrapHandle(Object o) {
        if (o instanceof DSHandle) {
            DSHandle h = (DSHandle) o;
            if (h.isClosed()) {
                if (h.getType().isPrimitive()) {
                    if (Types.STRING.equals(h.getType())) {
                        return "\"" + h.getValue() + '"';
                    }
                    else {
                        return h.getValue();
                    }
                }
                else if (h.getType().isComposite()){
                    return getVarName(h);
                }
                else {
                    Mapper m = h.getMapper();
                    return "<" + m.map(h.getPathFromRoot()) + ">";
                }
            }
            else {
                return "?" + getVarName(h);
            }
        }
        else {
            return o;
        }
    }

    public static Object fileName(AbstractDataNode n) {
        if (Types.STRING.equals(n.getType())) {
            return unwrapHandle(n);
        }
        else {
            Mapper m = n.getActualMapper();
            if (m == null) {
                return "?" + getVarName(n);
            }
            else {
                return "<" + m.map(n.getPathFromRoot()) + ">";
            }
        }
    }
}
