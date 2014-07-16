//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on May 22, 2012
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.coaster.channels.PerformanceDiagnosticInputStream;
import org.globus.cog.coaster.channels.PerformanceDiagnosticOutputStream;

public class CPSStatusDisplay {
private static final String CLS = ((char) (27)) + "[2J";
    private static final String HIDE_CURSOR = ((char) (27)) + "[?25l";
    private static final String SHOW_CURSOR = ((char) (27)) + "[?25h";
    private static final String HOME = ((char) (27)) + "[0;0f";
    private static final String BOLD = ((char) (27)) + "[01m";
    private static final String REVERSE = ((char) (27)) + "[07m";
    private static final String NORMAL = ((char) (27)) + "[00m";
    private static final String YELLOW = ((char) (27)) + "[33m";
    private static final String RED = ((char) (27)) + "[31m";
    private static final String BLUE = ((char) (27)) + "[34m";
    private static final String GREEN = ((char) (27)) + "[32m";
    
    private static OutputBuffer out;
    private static PrintStream console;
    private boolean passive;
    
    public CPSStatusDisplay(boolean passive) {
    	this.passive = passive;
    	console = System.out;
    	System.setOut(new PrintStream(out = new OutputBuffer()));
    }
    
    public void initScreen() {
        console.print(HIDE_CURSOR);
        console.print(CLS);
    }
    
    public void close() {
        console.print(SHOW_CURSOR);
        console.print(CLS);
        System.setOut(console);
    }
    
    public void printStats(CoasterService s) {
        Runtime r = Runtime.getRuntime();
        console.print(HOME);
        header(1, "Coaster Service");
        tabbed(1, "URL ", 30, s);
        tabbed(1, "Worker connect URL ", 30, s.getLocalService().getContact());
        if (passive) {
            header(2, "Workers", 32, "Active", 48, "Completed", 64, "Failed");
            tabbed(0, "", 32, Block.totalActiveWorkers, 48, Block.totalCompletedWorkers, 64, Block.totalFailedWorkers);
        }
        else {
            header(2, "Workers", 16, "Requested", 32, "Active", 48, "Completed", 64, "Failed");
            tabbed(0, "", 16, Block.totalRequestedWorkers, 32, Block.totalActiveWorkers, 48, Block.totalCompletedWorkers, 64, Block.totalFailedWorkers);
        }
        
        header(2, "Jobs", 16, "Queued", 32, "Active", 48, "Completed", 64, "Failed");
        tabbed(0, "", 
               16, BlockQueueProcessor.queuedJobs, 
               32, BlockQueueProcessor.runningJobs, 
               48, Cpu.totalCompletedJobs, 
               64, Cpu.totalFailedJobs);
        
        header(2, "Heap", 16, "Max", 32, "Current", 48, "Used", 64, "Free");
        tabbed(0, "",
               16, PerformanceDiagnosticInputStream.units(r.maxMemory()) + "B",
               32, PerformanceDiagnosticInputStream.units(r.totalMemory()) + "B",
               48, PerformanceDiagnosticInputStream.units(r.totalMemory() - r.freeMemory()) + "B",
               64, PerformanceDiagnosticInputStream.units(r.freeMemory()) + "B");
        
        header(2, "I/O", 16, "Total", 32, "Current", 48, "Peak", 64, "Average");
        tabbed(2, "Read ",
               16, format(PerformanceDiagnosticInputStream.getTotal()) + "B",
               32, formatM("R", PerformanceDiagnosticInputStream.getCurrentRate()),
               48, peakM("R"),
               64, format(PerformanceDiagnosticInputStream.getAverageRate()) + "B/s");
        tabbed(2, "Write ",
               16, format(PerformanceDiagnosticOutputStream.getTotal()) + "B",
               32, formatM("W", PerformanceDiagnosticOutputStream.getCurrentRate()),
               48, peakM("W"),
               64, format(PerformanceDiagnosticOutputStream.getAverageRate()) + "B/s");
        
        header(2, "Blocks", 40, "Nodes", 60, "Jobs Completed");
        
        Map<String, Block> blocks = new HashMap<String, Block>();
        
        for (Map.Entry<String, JobQueue> e : s.getQueues().entrySet()) {
            blocks.putAll(((BlockQueueProcessor) e.getValue().getCoasterQueueProcessor()).getBlocks());
        }
        List<Object[]> l = new ArrayList<Object[]>();
        try {
            for (Map.Entry<String, Block> e : blocks.entrySet()) { 
                l.add(new Object[] {e.getKey(), e.getValue().getNodes().size(), e.getValue().getDoneJobCount()});
            }
        }
        catch (ConcurrentModificationException e) {
        }
        
        Collections.sort(l, new Comparator<Object[]>() {
            public int compare(Object[] a, Object[] b) {
                return ((Integer) b[1]) - ((Integer) a[1]);
            }
        });
        
        for (int i = 0; i < Math.min(8, l.size()); i++) {
            tabbed(2, l.get(i)[0], 40, l.get(i)[1], 60, l.get(i)[2]);
        }
        if (l.size() > 8) {
            tabbed(2, "... (" + (l.size() - 8) + " more)");
        }
        
        console.println();
        
        printOut();
    }
    
    private Map<String, Long> MAX = new HashMap<String, Long>();
    
    private String formatM(String key, long v) {
        Long max = MAX.get(key);
        if (max == null) {
            max = 0L;
        }
        if (v > max) {
            max = v;
            MAX.put(key, max);
        }
        if (v < max * 0.1) {
            return RED + format(v) + "B/s" + NORMAL;
        }
        else if (v > max * 0.8) {
            return GREEN + format(v) + "B/s" + NORMAL;
        }
        else {
            return YELLOW + format(v) + "B/s" + NORMAL;
        }
    }
    
    private String peakM(String key) {
        Long max = MAX.get(key);
        if (max == null) {
            max = 0L;
        }
        return format(max) + "B/s";
    }

    private String format(long total) {
        return PerformanceDiagnosticInputStream.units(total);
    }

    private void printOut() {
        console.print(YELLOW);
        List<String> l = out.getBuffer();
        for (String s : l) {
            console.println(s);
        }
        console.print(NORMAL);
    }

    private void spaces(int len) {
        for (int i = 0; i < len; i++) {
            console.print(' ');
        }
    }
    
    private void header(Object... p) {
        spaces(80);
        console.println();
        console.print(REVERSE);
        spaces(80);
        console.print(NORMAL);
        console.println();
        _tabbed(REVERSE, NORMAL, REVERSE, p);
        spaces(80);
        console.println();
    }
    
    private void tabbed(Object... p) {
        _tabbed(NORMAL, BOLD, NORMAL, p);
    }
    
    private void _tabbed(String beforeSpace, String beforeFirst, String beforeItem, Object... p) {
        int index = 0;
        int crt = 0;
        while (index < p.length) {
            int nextPos = (Integer) p[index];
            console.print(beforeSpace);
            spaces(nextPos - crt);
            crt = nextPos;
            
            String msg = String.valueOf(p[index + 1]);
            if (index == 0) {
                console.print(beforeFirst);
            }
            else {
                console.print(beforeItem);
            }
            console.print(" ");
            console.print(msg);
            console.print(" ");
            crt += len(msg) + 2;
            console.print(beforeSpace);
            index += 2;
        }
        
        spaces(80 - crt);
        
        console.print(NORMAL);
        console.println();
    }

    private int len(String msg) {
        int len = 0;
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == 27) {
                len -= 4;
            }
            else {
                len++;
            }
        }
        return len;
    }

    private static class OutputBuffer extends OutputStream {
        private LinkedList<String> buffer;
        private StringBuilder crtline;
        
        public OutputBuffer() {
            buffer = new LinkedList<String>();
            crtline = new StringBuilder();
        }

        @Override
        public void write(int c) throws IOException {
            if (c == '\n') {
                addLine(crtline.toString());
                crtline = new StringBuilder();
            }
            else {
                crtline.append((char) c);
            }
        }

        private synchronized void addLine(String line) {
            if (buffer.size() > 1) {
                buffer.removeFirst();
            }
            buffer.addLast(line);
        }
        
        public synchronized List<String> getBuffer(){
            return new ArrayList<String>(buffer);
        }
    }
}
