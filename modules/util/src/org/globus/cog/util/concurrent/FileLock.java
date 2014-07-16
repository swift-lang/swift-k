//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 5, 2012
 */
package org.globus.cog.util.concurrent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * An implementation of file locks using Lamport's Bakery algorithm.
 * It tries to use the PID as thread identifier with fallback to random numbers.
 *
 * The Entering and Number arrays are implemented as sparse arrays of
 * files in some directory. The lock is not re-entrant.
 */
public class FileLock {    
    public static final Logger logger = Logger.getLogger(FileLock.class);
    
    public static final String NUMBER = "locking.number";
    public static final String ENTERING = "locking.entering";
    
    private int myId, myN;
    private File dir;
    private int lockCount;
    
    private static Map<File, Boolean> jvmLocalLocks;
    
    static {
        jvmLocalLocks = new HashMap<File, Boolean>();
    }
    
    public FileLock(String dir) {
        this(new File(dir));
    }
    
    public FileLock(File dir) {
        this.dir = dir;
        dir.mkdirs();
        this.myId = getId();
    }
    
    private int getId() {
    	int id = getIdMgmt();
    	if (id == -1) {
    		return getIdProc();
    	}
    	else {
    		return id;
    	}
    }

    private int getIdProc() {
        try {
            return Integer.parseInt(new File("/proc/self").getCanonicalFile().getName());
        }
        catch (Exception e) {
            logger.info("Failed to get PID of current process", e);
            try {
                SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
                return rnd.nextInt() & 0x7fffffff;
            }
            catch (Exception ee) {
                logger.warn("Failed to get instance of SHA1PRNG", ee);
                return new Random().nextInt() & 0x7fffffff;
            }
        }
    }
    
    private int getIdMgmt() {
    	try {
    	    java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
    	    java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
    	    jvm.setAccessible(true);
    	    sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
    	    java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
    	    pid_method.setAccessible(true);
    	    return (Integer) pid_method.invoke(mgmt);
    	}
    	catch (Exception e) {
    		return -1;
    	}
    }
    
    public void lock() throws IOException, InterruptedException {
        synchronized(jvmLocalLocks) {
            while (jvmLocalLocks.containsKey(dir)) {
                jvmLocalLocks.wait();
            }
            jvmLocalLocks.put(dir, Boolean.TRUE);
        }
        write(ENTERING, myId, 1);
        write(NUMBER, myId, myN = 1 + maxNumber());
        write(ENTERING, myId, 0);
        waitOther();
    }
    
    private void waitOther() throws InterruptedException {
        int last = -1;
        while (true) {
            int minIndex = getMinIndex(last);
            
            if (minIndex == Integer.MAX_VALUE) {
                // all remaining NUMBER[j] and ENTERING[j] are 0
                break;
            }
            
            File e = makeFile(ENTERING, minIndex);
            
            while (e.exists()) {
                Thread.sleep(100);
            }
            
            File n = makeFile(NUMBER, minIndex);
            
            while (n.exists()) {
                int nj = read(n);
                if (nj > myN || ((nj == myN) && (minIndex >= myId))) {
                    break;
                }
                Thread.sleep(100);
            }
            // this guarantees that the getMinIndex(last) call
            // above will only return indices > the_current_minIndex
            last = minIndex;
        }
    }

    private File makeFile(String prefix, int index) {
        return new File(dir, prefix + "." + index);
    }

    private int getMinIndex(final int greaterThan) {
        File[] numbers = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return f.isFile() && (name.startsWith(NUMBER) || name.startsWith(ENTERING)) && (getIndex(f) > greaterThan);
            }
        });
        int min = Integer.MAX_VALUE;
        for (File n : numbers) {
            int in = getIndex(n);
            if (in < min) {
                min = in;
            }
        }
        return min;
    }

    private int getIndex(File n) {
        try {
            return Integer.parseInt(n.getName().substring(n.getName().lastIndexOf('.') + 1));
        }
        catch (Exception e) {
            throw new IllegalArgumentException("A file is conflicting with directory locking: " + n, e);
        }
    }

    private int maxNumber() {
        File[] numbers = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile() && f.getName().startsWith(NUMBER);
            }
        });
        
        int max = 0;
        for (File n : numbers) {
            int in = read(n);
            if (in > max) {
                max = in;
            }
        }
        return max;
    }

    private int read(File n) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(n));
            try {
                return Integer.parseInt(br.readLine());
            }
            finally {
                br.close();
            }
        }
        catch (Exception e) {
            // nothing. The algorithm tolerates incorrect reads
        }
        return 0;
    }

    private void write(String prefix, int id, int value) throws IOException {
        File f = new File(dir, prefix + "." + id);
        if (value == 0) {
            if (!f.delete()) {
                f.deleteOnExit();
                throw new IOException("Failed to delete " + f);
            }
        }
        else {
            BufferedWriter br = new BufferedWriter(new FileWriter(f));
            try {
                br.write(String.valueOf(value));
            }
            finally {
                br.close();
            }
            f.deleteOnExit();
        }
    }

    public void unlock() throws IOException {
        try {
            write("locking.number", myId, 0);
        }
        finally {
            synchronized(jvmLocalLocks) {
                jvmLocalLocks.remove(dir);
                jvmLocalLocks.notifyAll();
            }
        }
    }
    
    public static void main(String[] args) {
        final String dir = args[0];
        int delay = Integer.parseInt(args[1]);
        final FileLock l = new FileLock(dir);
        try {
            System.out.println(". " + l.getId());
            l.lock();
            System.out.println("+ " + l.getId());
            try {
                while (Math.random() < 0.8) {
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                FileLock l = new FileLock(dir);
                                System.out.println(". " + l.getId() + "x");
                                l.lock();
                                System.out.println("+ " + l.getId() + "x");
                                Thread.sleep((int) (Math.random() * 5));
                                l.unlock();
                                System.out.println("- " + l.getId() + "x");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                Thread.sleep(delay);
            }
            finally {
                l.unlock();
                System.out.println("- " + l.getId());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
