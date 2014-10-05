/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 9, 2012
 */
package org.griphyn.vdl.karajan.lib;

import java.util.HashMap;
import java.util.Map;

import k.rt.Future;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.SwiftConfig;

public class Tracer {
    public static final Logger logger = Logger.getLogger("TRACE");
    private static boolean globalTracingEnabled;
    private static final Map<String, String> NAME_MAPPINGS;
    private ThreadLocal<String> thread = new ThreadLocal<String>();
    
    static {
        globalTracingEnabled = SwiftConfig.getDefault().isTracingEnabled();
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
    
    private Tracer(Node fe, String name) {
        source = buildSource(fe, name);
        if (source == null) {
            enabled = false;
        }
        else {
            enabled = true;
        }
    }
    
    private Tracer(int line, String name) {
        source = buildSource(line, name);
        enabled = true;
    }
    
    private Tracer(String name) {
        source = name;
        enabled = true;
    }
    
    private Tracer(Node fe) {
        this(fe, null);
    }
    
    private String buildSource(Node fe, String name) {
        int line = findLine(fe);
        if (line == -1) {
            return null;
        }
        if (name == null) {
            name = getType(fe);
        }
        return buildSource(line, name);      
    }

    private String buildSource(int line, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(", line ");
        sb.append(line);
        return sb.toString();
    }

    private String getType(Node fe) {
        String t = fe.getTextualName();
        String nt = NAME_MAPPINGS.get(t);
        if (nt == null) {
            return t;
        }
        else {
            return nt;
        }
    }

    private int findLine(Node fe) {
        return fe.getLine();
    }

    public boolean isEnabled() {
        return enabled;
    }
        
    public void trace(LWThread thr, Object msg) throws VariableNotFoundException {
        trace(threadName(thr), msg);
    }
    
    public void trace(String thread, Object msg) {
        String str = source + ", thread " + threadName(thread) + ", " + msg;
        logger.info(str);
    }
    
    public void trace(String thread, String name, int line, Object msg) {
        if (line == -1) {
            return;
        }
        String str = name + ", line " + line + ", thread " + threadName(thread) + ", "+ msg;
        logger.info(str);
    }
    
    public void trace(LWThread thread, int line, Object msg) {
        if (line == -1) {
            return;
        }
        String str = source + ", line " + line + ", thread " + threadName(thread) + ", " + msg;
        logger.info(str);
    }
    
    public void trace(LWThread thread) {
        logger.info(source + ", thread " + threadName(thread));
    }
    
    private String threadName(String thread) {
        if (thread.isEmpty()) {
            return "main";
        }
        else {
            return thread;
        }
    }
    
    private String threadName(LWThread thr) throws VariableNotFoundException {
        return SwiftFunction.getThreadPrefix(thr);
    }
    
    private static Tracer disabledTracer, enabledTracer;
    
    public static Tracer getTracer(Node fe) {
        return getTracer(fe, null);
    }
    
    public static Tracer getTracer(Node fe, String name) {
        if (globalTracingEnabled) {
            return new Tracer(fe, name);
        }
        else {
            return getGenericTracer(false);
        }
    }
    
    public static Tracer getTracer(Integer line, String name) {
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
        // TODO
        /*if (future instanceof FutureWrapper) {
            return getVarName(((FutureWrapper) future).getHandle());
        }
        else {
            return future.toString();
        }*/
        return future.toString();
    }
    
    public static Object unwrapHandle(Object o) {
        if (o instanceof AbstractDataNode) {
            AbstractDataNode h = (AbstractDataNode) o;
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
                    return fileName(h);
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
        RootHandle root = n.getRoot();
        Mapper m = root.getActualMapper();
        if (m == null) {
            return "?" + getVarName(n);
        }
        else {
            try {
                return "<" + m.map(n.getPathFromRoot()) + ">";
            }
            catch (InvalidPathException e) {
                return "<ERROR>";
            }
        }
    }
}
