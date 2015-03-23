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


package org.griphyn.vdl.karajan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import k.rt.Context;
import k.rt.ExecutionException;
import k.thr.LWThread;
import k.thr.Scheduler;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.grid.SchedulerNode;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.griphyn.vdl.karajan.lib.FileCopier;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class HangChecker extends TimerTask {
    public static final Logger logger = Logger.getLogger(HangChecker.class);
    
    public static final int CHECK_INTERVAL = 1000;
    public static final int MAX_CYCLES = 10;
    private Timer timer;
    private Context context;
    private long lastSequenceNumber, lastHandledSequenceNumber;
    
    public HangChecker(Context context) throws ExecutionException {
    	this.context = context;
    }

    public void start() {
        timer = new Timer("Hang checker");
        timer.scheduleAtFixedRate(this, CHECK_INTERVAL, CHECK_INTERVAL);
    }
    
    public void stop() {
        try {
            timer.cancel();
        }
        catch (Exception e) {
            logger.info("Failed to stop hang checker", e);
        }
    }
    
    public void startShutdownCheck() {
        // if still here after 10 seconds, dump the threads
        Timer t = new Timer("Shutdown checker");
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(os);
                ps.println("Swift did not shut down 10 seconds after the run completed\n");
                dumpJVMThreads(ps);
                logger.warn(os.toString());
            }
        }, 10000);
    }

    public void run() {
        try {
            long crtSequenceNumber = Scheduler.getScheduler().getSequenceId();
            if (crtSequenceNumber != lastSequenceNumber) {
                lastSequenceNumber = crtSequenceNumber;
                return;
            }
            if (lastHandledSequenceNumber == crtSequenceNumber) {
                // already printed info, so don't spam stdout
                return;
            }
            WeightedHostScoreScheduler s = (WeightedHostScoreScheduler) context.getAttribute(SchedulerNode.CONTEXT_ATTR_NAME);
            if (s != null) {
                int running = s.getRunning();
                running += FileCopier.getRunning();
                boolean allOverloaded = s.allOverloaded();
                if (running == 0 && !Scheduler.getScheduler().isAnythingRunning() && !allOverloaded) {
                    boolean found = false;
                    lastHandledSequenceNumber = crtSequenceNumber;
                    logger.warn("No events in " + (CHECK_INTERVAL / 1000) + "s. Details dumped to log.");
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(os);
                    dumpThreads(ps);
                    try {
                        Graph g = buildGraph();
                        //g.write(ps);
                        if (!found) {
                            found = findCycles(ps, g);
                        }
                        if (!found) {
                            found = findThreadsToBlame(ps, g);
                        }
                    }
                    catch (Exception e) {
                        logger.warn("Failed to build dependency graph", e);
                    }
                    if (!found) {
                        found = findJVMDeadlocks(ps);
                    }
                    if (!found) {
                        dumpJVMThreads(ps);
                    }
                    ps.close();
                    logger.info(os.toString());
                    if (found) {
                        boolean debugging;
                        try {
                            debugging = java.lang.management.ManagementFactory.getRuntimeMXBean().
                                getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
                        }
                        catch (Exception e) {
                            System.err.println("Error figuring out if debugging is enabled: " + e.getMessage());
                            debugging = false;
                        }
                        if (debugging) {
                            System.err.println("Things are being debugged. Skipping exit.");
                        }
                        else {
                            System.err.println("Irrecoverable error found. Exiting.");
                            System.exit(99);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn("Exception caught during hang check", e);
        }
    }
    
    private void dumpJVMThreads(PrintStream pw) {
        ThreadMXBean b = ManagementFactory.getThreadMXBean();
        if (b != null) {
            long[] ids = b.getAllThreadIds();
            if (ids != null && ids.length != 0) {
                ThreadInfo[] tis = b.getThreadInfo(ids, true, true); 
                pw.println("\nWaiting JVM threads:");
                for (ThreadInfo ti : tis) {
                    Thread.State state = ti.getThreadState();
                    if (state != Thread.State.RUNNABLE && state != Thread.State.TERMINATED) {
                        printThreadInfo(pw, ti);
                    }
                }
            }
        }
    }

    private boolean findJVMDeadlocks(PrintStream pw) {;
        ThreadMXBean b = ManagementFactory.getThreadMXBean();
        if (b != null) {
            long[] ids = b.findDeadlockedThreads();
            if (ids != null && ids.length != 0) {
                ThreadInfo[] tis = b.getThreadInfo(ids, true, true); 
                pw.println("\nDeadlocked Java threads found:");
                for (ThreadInfo ti : tis) {
                    printThreadInfo(pw, ti);
                }
                return true;
            }
        }
        return false;
    }

    private void printThreadInfo(PrintStream pw, ThreadInfo ti) {
        pw.println("\tThread \"" + ti.getThreadName() + "\" (" + hex(ti.getThreadId()) + ") " + ti.getThreadState());
        LockInfo l = ti.getLockInfo();
        if (l != null) {
            pw.println("\t\twaiting for " + format(l) + 
                    (ti.getLockOwnerName() == null ? "" : " held by " + ti.getLockOwnerName() + " (" + hex(ti.getLockOwnerId()) + ")"));
        }
        Map<StackTraceElement, MonitorInfo> mlocs = new HashMap<StackTraceElement, MonitorInfo>();
        MonitorInfo[] mis = ti.getLockedMonitors();
        if (mis.length > 0) {
            pw.println("\tMonitors held:");
            for (MonitorInfo mi : mis) {
                mlocs.put(mi.getLockedStackFrame(), mi);
                pw.println("\t\t" + format(mi));
            }
        }
        LockInfo[] lis = ti.getLockedSynchronizers();
        if (lis.length > 0) {
            pw.println("\tSynchronizers held:");
            for (LockInfo li : lis) {
                pw.println("\t\t" + format(li));
            }
        }
        pw.println("\tStack trace:");
        StackTraceElement[] stes = ti.getStackTrace();
        for (StackTraceElement ste : stes) {
            pw.print("\t\t" + ste.getClassName() + "." + ste.getMethodName() + formatLineNumber(":", ste.getLineNumber()));
            if (mlocs.containsKey(ste)) {
                pw.print(" -> locked " + format(mlocs.get(ste)));
            }
            pw.println();
        }
        pw.println();
    }

    private String formatLineNumber(String prefix, int n) {
        if (n < 0) {
            return "";
        }
        else {
            return prefix + String.valueOf(n);
        }
    }

    private String format(LockInfo l) {
        if (l != null) {
            return l.getClassName() + " (" + hex(l.getIdentityHashCode()) + ")";
        }
        else {
            return "<unknown>";
        }
    }

    private String hex(long x) {
        return String.format("0x%08x", x);
    }

    public void dumpThreads() {
        dumpThreads(System.out);
    }

    public static void dumpThreads(PrintStream pw) {
        pw.println("\nWaiting threads:");
        Map<LWThread, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
        Map<LWThread, List<DSHandle>> ot = WaitingThreadsMonitor.getOutputs();
        for (Map.Entry<LWThread, DSHandle> e : c.entrySet()) {
            dumpThread(pw, e.getKey(), e.getValue(), ot);
            pw.println();
        }
        pw.println("----");
    }
    
    public static void dumpThreadsShort(PrintStream pw) {
        pw.println("\nWaiting threads:");
        Map<LWThread, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (Map.Entry<LWThread, DSHandle> e : c.entrySet()) {
            inc(counts, varWithLine(e.getValue()));
        }
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            pw.println("\t" + e.getValue() + " threads waiting on " + e.getKey());
            pw.println();
        }
        pw.println("----");
    }

    private static void inc(Map<String, Integer> counts, String key) {
        Integer i = counts.get(key);
        if (i == null) {
            counts.put(key, 1);
        }
        else {
            counts.put(key, i + 1);
        }
    }

    public static void dumpThread(PrintStream pw, LWThread thr, DSHandle handle, Map<LWThread, List<DSHandle>> ot) {
        List<DSHandle> outputs = null;
        if (ot != null) {
            outputs = ot.get(thr);
        }
        try {
            pw.println("Thread: " + SwiftFunction.getThreadPrefix(thr) 
                + (handle == null ? "" : ", waiting on " + varWithLine(handle))
                + (outputs == null ? "" : ", producing: " + varsWithLine(outputs)));

            for (String t : getSwiftTrace(thr)) {
                pw.println("\t" + t);
            }
        }
        catch (VariableNotFoundException e1) {
            pw.println("unknown thread");
        }
    }
    
    private static String varsWithLine(List<DSHandle> vars) {
        StringBuilder sb = new StringBuilder();
        for (DSHandle h : vars) {
            sb.append("\n\t\t");
            sb.append(varWithLine(h));
        }
        return sb.toString();
    }

    public static String varWithLine(DSHandle value) {
        Integer line = null;
        String dbgname = null;
        line = value.getRoot().getLine();
        dbgname = value.getName();
        Path path = value.getPathFromRoot();
        return dbgname + 
            (value == value.getRoot() ? "" : (path.isArrayIndex(0) ? path : "." + path)) + 
            (line == null ? "" : " (declared on line " + line + ")");
    }
    
    public static String varWithLineShort(DSHandle value) {
        Integer line = null;
        String dbgname = null;
        line = value.getRoot().getLine();
        dbgname = value.getName();
        Path path = value.getPathFromRoot();
        return dbgname + 
            (value == value.getRoot() ? "" : (path.isArrayIndex(0) ? path : "." + path)) + 
            (line == null ? "" : " (" + line + ")");
    }
    
    public static String getLastCall(LWThread thr) {
        List<Object> trace = thr.getTrace();
        if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    int line = n.getLine();
                    return(n.getTextualName() + ", " + 
                            fileName(n) + ", line " + line);
                }
            }
        }
        return "?";
    }
    
    public static List<String> getSwiftTrace(LWThread thr) {
        List<String> ret = new ArrayList<String>();
        List<Object> trace = thr.getTrace();
        if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    int line = n.getLine();
                    ret.add(n.getTextualName() + ", " + 
                            fileName(n) + ", line " + line);
                
                }
            }
        }
        return ret;
    }
    
    public static List<Object> getSwiftTraceElements(LWThread thr) {
        List<Object> ret = new ArrayList<Object>();
        List<Object> trace = thr.getTrace();
        if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    ret.add(n.getLine());
                }
            }
        }
        return ret;
    }

    private static String fileName(Node n) {
        return new File(n.getFileName()).getName().replace(".kml", ".swift");
    }

    
    private boolean findThreadsToBlame(PrintStream ps, Graph g) {
        Map<LWThread, DSHandle> wt = WaitingThreadsMonitor.getAllThreads();
        Set<LWThread> sl = g.nodeSet();
        Set<LWThread> loners = new HashSet<LWThread>(wt.keySet());
        for (LWThread s : sl) {
            for (Graph.Edge e : g.getEdgesFrom(s)) {
                loners.remove(e.to);
            }
        }
        dumpThreadsToBlame(ps, loners, wt);
        if (loners.size() < 10) {
            dumpThreadsToBlame(System.out, loners, wt);
        }
        return !loners.isEmpty();
    }

    private void dumpThreadsToBlame(PrintStream ps, Set<LWThread> loners, Map<LWThread, DSHandle> wt) {
        if (!loners.isEmpty()) {
            ps.println();
            ps.println("The following threads are independently hung:");
            for (LWThread s : loners) {
                dumpThread(ps, s, wt.get(s), null);
                ps.println();
            }
            ps.println("----");
        }
    }

    private Graph buildGraph() throws VariableNotFoundException {
        Map<LWThread, List<DSHandle>> ot = WaitingThreadsMonitor.getOutputs();
        Map<LWThread, DSHandle> wt = WaitingThreadsMonitor.getAllThreads();
        Map<DSHandle, List<LWThread>> rwt = new HashMap<DSHandle, List<LWThread>>();
        
        for (Map.Entry<LWThread, DSHandle> e : wt.entrySet()) {
            List<LWThread> l = rwt.get(e.getValue());
            if (l == null) {
                l = new LinkedList<LWThread>();
                rwt.put(e.getValue(), l);
            }
            l.add(e.getKey());
        }
        
        Graph g = new Graph();

        // if n1 -> n2, then n1 produces an output that is used by n2
        for (Map.Entry<LWThread, List<DSHandle>> e : ot.entrySet()) {
            for (DSHandle h : e.getValue()) {
                List<LWThread> sl = rwt.get(h);
                if (sl != null) {
                    for (LWThread s : sl) {
                        g.addEdge(e.getKey(), s, h);
                    }
                }
            }
            
            
            LWThread tc = e.getKey();
            for (LWThread stk : ot.keySet()) {
                if (isStrictlyChildOf(tc, stk)) {
                    g.addEdge(e.getKey(), stk, null);
                }
            }
        }
        
        return g;
    }

    private boolean isStrictlyChildOf(LWThread child, LWThread parent) {
        if (child == parent) {
        	return false;
        }
        child = child.getParent();
        while (child != null) {
        	if (child == parent) {
        		return true;
        	}
        	child = child.getParent();
        }
        return false;
    }

    private static boolean findCycles(PrintStream ps, Graph g) {                
        System.out.print("Finding dependency loops...");
        System.out.flush();
        
        Set<LWThread> seen = new HashSet<LWThread>();
        LinkedList<Object> cycle = new LinkedList<Object>();
        List<LinkedList<Object>> cycles = new ArrayList<LinkedList<Object>>();
        for (LWThread t : g.nodeSet()) {
            seen.clear();
            cycle.clear();
            findLoop(t, g, seen, cycle, cycles);
        }
        
        if (cycles.size() == 1) {
            ps.println("Dependency loop found:");
        }
        else if (cycles.size() > 1) {
        	ps.println(cycles.size() + " dependency loops found:");
        }
        else {
            ps.println("No dependency loops found.");
        }
        int index = 0;
        for (LinkedList<Object> c : cycles) {
            index++;
            if (cycles.size() > 1) {
                ps.println("* " + index);
            }
            
            // rotate the cycle so that "the above must complete" is not the first thing.
            while (c.getLast() == null) {
                Object o1 = c.removeFirst(); Object o2 = c.removeFirst();
                c.add(o1); c.add(o2);
            }
            Object prev = c.getLast();
            for (Object o : c) {
                if (o instanceof LWThread) {
                    if (prev != null) {
                        ps.println("\t" + varWithLine((DSHandle) prev) + " is needed by: ");
                    }
                    else {
                        ps.println("\tthe above must complete before the block below can complete:");
                    }
                    for (String t : getSwiftTrace((LWThread) o)) {
                        ps.println("\t\t" + t);
                    }
                }
                else {
                    prev = o;
                    if (o != null) {
                        ps.println("\twhich produces " + varWithLine((DSHandle) o));
                    }
                    ps.println();
                }
            }
        }
        
        // TODO: fail the loops
        if (cycles.size() > 0) {
        	ps.println("----");
        }
        if (cycles.isEmpty()) {
            System.out.print(" none found");
        }
        else {
            System.out.print(" " + cycles.size() + " loops found");
        }
        return !cycles.isEmpty();
    }
        
    private static boolean isDuplicate(List<LinkedList<Object>> cycles, LinkedList<Object> cycle) {
        for (LinkedList<Object> c : cycles) {
            if (isSameCycle(c, cycle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSameCycle(LinkedList<Object> a, LinkedList<Object> b) {
        if (a.size() != b.size()) {
        	return false;
        }
        Iterator<Object> i = a.iterator();
        Object o = i.next();
        Iterator<Object> j = b.iterator();
        while (j.hasNext()) {
        	if (sameTraces(o, j.next())) {
        		while (i.hasNext()) {
        		    if (!j.hasNext()) {
        		        j = b.iterator();
        		    }
        		    if (!sameTraces(i.next(), j.next())) {
        		    	return false;
        		    }
        		}
        		return true;
        	}
        }
        return false;
    }

    private static boolean sameTraces(Object a, Object b) {
        if (a instanceof DSHandle) {
            return a == b;
        }
        if (b instanceof DSHandle) {
            return false;
        }
        if (a == null || b == null) {
            return a == b;
        }
        LWThread sa = (LWThread) a;
        LWThread sb = (LWThread) b;
        
        List<Object> ta = getSwiftTraceElements(sa);
        List<Object> tb = getSwiftTraceElements(sb);
        
        if (ta.size() != tb.size()) {
            return false;
        }
        for (int i = 0; i < ta.size(); i++) {
            if (ta.get(i) != tb.get(i)) {
                return false;
            }
        }
        
        return true;
    }

    private static void findLoop(LWThread t, Graph g, Set<LWThread> seen, LinkedList<Object> cycle, List<LinkedList<Object>> cycles) {
        if (cycles.size() > MAX_CYCLES) {
            return;
        }
        if (t == null) {
            return;
        }
        if (seen.contains(t)) {
            // remove things up to t in the cycle since they are just lead-ins
            LinkedList<Object> lc = new LinkedList<Object>(cycle);
            while (lc.getFirst() != t) {
                lc.removeFirst();
            }
            if (!isDuplicate(cycles, lc)) {
                cycles.add(new LinkedList<Object>(lc));
                System.out.print(".");
            }
            return;
        }
        cycle.add(t);
        seen.add(t);
        
        for (Graph.Edge e : g.getEdgesFrom(t)) {
            cycle.add(e.contents);
            findLoop(e.to, g, seen, cycle, cycles);
            cycle.removeLast();
        }

        cycle.removeLast();
        seen.remove(t);
    }
    
    public static class Graph {
        public static class Edge {
            public final LWThread to;
            public final DSHandle contents;
            
            public Edge(LWThread to, DSHandle contents) {
                this.to = to;
                this.contents = contents;
            }
        }
        
        private Map<LWThread, List<Edge>> outEdges = new HashMap<LWThread, List<Edge>>();
        
        public void write(PrintStream ps) {
            Set<String> boxes = new HashSet<String>();
            ps.println("Thread Graph:");
            ps.println("digraph Dependencies {");
            for (Map.Entry<LWThread, List<Edge>> e : outEdges.entrySet()) {
                for (Edge d : e.getValue()) {
                    String var = varWithLineShort(d.contents);
                    ps.print("\t\"");
                    ps.print(SwiftFunction.getThreadPrefix(e.getKey()));
                    ps.print("\" -> \"");
                    ps.print(var);
                    ps.println("\"");
                    ps.print("\t\"");
                    ps.print(var);
                    ps.print("\" -> \"");
                    ps.print(SwiftFunction.getThreadPrefix(d.to));
                    ps.println("\"");
                    if (!boxes.contains(var)) {
                        ps.print("\"");
                        ps.print(var);
                        ps.println("\" [shape=box]");
                        boxes.add(var);
                    }
                }
            }
            ps.println("}");
        }

        public void addEdge(LWThread from, LWThread to, DSHandle contents) {
            List<Edge> l = outEdges.get(from);
            if (l == null) {
                l = new ArrayList<Edge>();
                outEdges.put(from, l);
            }
            l.add(new Edge(to, contents));
        }

        public void dump(PrintStream ps) {
            for (Map.Entry<LWThread, List<Edge>> e : outEdges.entrySet()) {
                for (Edge edge : e.getValue()) {
                    String tcf = getThreadingContext(e.getKey());
                    String tct = getThreadingContext(edge.to);
                    ps.println(tcf + " -> " + tct);
                }
            }
        }

        private String getThreadingContext(LWThread s) {
            try {
                return String.valueOf(s);
            }
            catch (VariableNotFoundException e) {
                return "?";
            }
        }

        public List<Edge> getEdgesFrom(LWThread t) {
            List<Edge> l = outEdges.get(t);
            if (l == null) {
                return Collections.emptyList();
            }
            else {
                return l;
            }
        }

        public Set<LWThread> nodeSet() {
            return outEdges.keySet();
        }
    }
}
