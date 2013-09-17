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

import org.apache.log4j.Logger;
import org.globus.cog.karajan.scheduler.WeightedHostScoreScheduler;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.nodes.grid.SchedulerNode;
import org.griphyn.vdl.mapping.DSHandle;

public class HangChecker extends TimerTask {
    public static final Logger logger = Logger.getLogger(HangChecker.class);
    
    public static final int CHECK_INTERVAL = 10000;
    public static final int MAX_CYCLES = 10;
    private Timer timer;
    private long lastEventCount;
    private VariableStack stack;
    
    public HangChecker(VariableStack stack) throws ExecutionException {
        this.stack = stack;
    }

    public void start() {
        timer = new Timer("Hang checker");
        timer.scheduleAtFixedRate(this, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    public void run() {
        try {
            WeightedHostScoreScheduler s = (WeightedHostScoreScheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
            if (s != null) {
                int running = s.getRunning();
                boolean allOverloaded = s.allOverloaded();
                if (running == 0 && EventBus.eventCount == lastEventCount && 
                        !EventBus.getBus().isAnythingRunning() && !allOverloaded) {
                    boolean found = false;
                    logger.warn("No events in " + (CHECK_INTERVAL / 1000) + "s.");
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(os);
                    Monitor.dumpVariables(ps);
                    Monitor.dumpThreads(ps);
                    try {
                        Graph g = buildGraph();
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
                    logger.warn(os.toString());
                    ps.close();
                    if (found) {
                        System.err.println("Irrecoverable error found. Exiting.");
                        System.exit(99);
                    }
                }
            }
            lastEventCount = EventBus.eventCount;
        }
        catch (Exception e) {
            logger.warn("Exception caught during hang check", e);
        }
    }
    
    private boolean findJVMDeadlocks(PrintStream pw) {;
        try {
            ThreadMXBean b = ManagementFactory.getThreadMXBean();
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
        catch (Exception e) {
            logger.warn("Exception caught trying to find JVM deadlocks", e);
        }
        return false;
    }

    private void printThreadInfo(PrintStream pw, ThreadInfo ti) {
        pw.println("\tThread \"" + ti.getThreadName() + "\" (" + hex(ti.getThreadId()) + ")");
        LockInfo l = ti.getLockInfo();
        pw.println("\t\twaiting for " + format(l) + " held by " + ti.getLockOwnerName() + " (" + hex(ti.getLockOwnerId()) + ")");
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
            pw.print("\t\t" + ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber());
            if (mlocs.containsKey(ste)) {
                pw.print(" -> locked " + format(mlocs.get(ste)));
            }
            pw.println();
        }
        pw.println();
    }

    private String format(LockInfo l) {
        return l.getClassName() + " (" + hex(l.getIdentityHashCode()) + ")";
    }

    private String hex(long x) {
        return String.format("0x%08x", x);
    }

    private boolean findThreadsToBlame(PrintStream ps, Graph g) {
        Map<VariableStack, DSHandle> wt = WaitingThreadsMonitor.getAllThreads();
        Set<VariableStack> sl = g.nodeSet();
        Set<VariableStack> loners = new HashSet<VariableStack>(wt.keySet());
        for (VariableStack s : sl) {
            for (Graph.Edge e : g.getEdgesFrom(s)) {
                loners.remove(e.to);
            }
        }
        if (!loners.isEmpty()) {
            ps.println();
            ps.println("The following threads are independently hung:");
            for (VariableStack s : loners) {
                Monitor.dumpThread(ps, s, wt.get(s));
                ps.println();
            }
            ps.println("----");
        }
        return !loners.isEmpty();
    }

    private Graph buildGraph() throws VariableNotFoundException {
        Map<VariableStack, List<DSHandle>> ot = WaitingThreadsMonitor.getOutputs();
        Map<VariableStack, DSHandle> wt = WaitingThreadsMonitor.getAllThreads();
        Map<DSHandle, List<VariableStack>> rwt = new HashMap<DSHandle, List<VariableStack>>();
        
        for (Map.Entry<VariableStack, DSHandle> e : wt.entrySet()) {
            List<VariableStack> l = rwt.get(e.getValue());
            if (l == null) {
                l = new LinkedList<VariableStack>();
                rwt.put(e.getValue(), l);
            }
            l.add(e.getKey());
        }
        
        Graph g = new Graph();

        // if n1 -> n2, then n1 produces an output that is used by n2
        for (Map.Entry<VariableStack, List<DSHandle>> e : ot.entrySet()) {
            for (DSHandle h : e.getValue()) {
                List<VariableStack> sl = rwt.get(h);
                if (sl != null) {
                    for (VariableStack s : sl) {
                        g.addEdge(e.getKey(), s, h);
                    }
                }
            }
            
            ThreadingContext tc;
            
            tc = ThreadingContext.get(e.getKey()); 
            for (VariableStack stk : ot.keySet()) {
                if (tc.isStrictlySubContext(ThreadingContext.get(stk))) {
                    g.addEdge(e.getKey(), stk, null);
                }
            }
        }
        
        return g;
    }

    private static boolean findCycles(PrintStream ps, Graph g) {                
        System.out.print("Finding dependency loops...");
        System.out.flush();
        
        Set<VariableStack> seen = new HashSet<VariableStack>();
        LinkedList<Object> cycle = new LinkedList<Object>();
        List<LinkedList<Object>> cycles = new ArrayList<LinkedList<Object>>();
        for (VariableStack t : g.nodeSet()) {
            seen.clear();
            cycle.clear();
            findLoop(t, g, seen, cycle, cycles);
        }
        System.out.println();
        
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
                if (o instanceof VariableStack) {
                    if (prev != null) {
                        ps.println("\t" + Monitor.varWithLine((DSHandle) prev) + " is needed by: ");
                    }
                    else {
                        ps.println("\tthe above must complete before the block below can complete:");
                    }
                    for (String t : Monitor.getSwiftTrace((VariableStack) o)) {
                        ps.println("\t\t" + t);
                    }
                }
                else {
                    prev = o;
                    if (o != null) {
                        ps.println("\twhich produces " + Monitor.varWithLine((DSHandle) o));
                    }
                    ps.println();
                }
            }
        }
        
        // TODO: fail the loops
        if (cycles.size() > 0) {
        	ps.println("----");
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
        VariableStack sa = (VariableStack) a;
        VariableStack sb = (VariableStack) b;
        
        List<Object> ta = Monitor.getSwiftTraceElements(sa);
        List<Object> tb = Monitor.getSwiftTraceElements(sb);
        
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

    private static void findLoop(VariableStack t, Graph g, Set<VariableStack> seen, LinkedList<Object> cycle, List<LinkedList<Object>> cycles) {
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
            public final VariableStack to;
            public final DSHandle contents;
            
            public Edge(VariableStack to, DSHandle contents) {
                this.to = to;
                this.contents = contents;
            }
        }
        
        private Map<VariableStack, List<Edge>> outEdges = new HashMap<VariableStack, List<Edge>>();

        public void addEdge(VariableStack from, VariableStack to, DSHandle contents) {
            List<Edge> l = outEdges.get(from);
            if (l == null) {
                l = new ArrayList<Edge>();
                outEdges.put(from, l);
            }
            l.add(new Edge(to, contents));
        }

        public void dump(PrintStream ps) {
            for (Map.Entry<VariableStack, List<Edge>> e : outEdges.entrySet()) {
                for (Edge edge : e.getValue()) {
                    String tcf = getThreadingContext(e.getKey());
                    String tct = getThreadingContext(edge.to);
                    ps.println(tcf + " -> " + tct);
                }
            }
        }

        private String getThreadingContext(VariableStack s) {
            try {
                return String.valueOf(ThreadingContext.get(s));
            }
            catch (VariableNotFoundException e) {
                return "?";
            }
        }

        public List<Edge> getEdgesFrom(VariableStack t) {
            List<Edge> l = outEdges.get(t);
            if (l == null) {
                return Collections.emptyList();
            }
            else {
                return l;
            }
        }

        public Set<VariableStack> nodeSet() {
            return outEdges.keySet();
        }
    }
}
