//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 13, 2007
 */
package k.thr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import k.rt.Abort;
import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Future;
import k.rt.FutureListener;
import k.rt.KRunnable;
import k.rt.Stack;
import k.rt.WaitYield;
import k.thr.Scheduler.RootThread;

import org.apache.log4j.Logger;

public class LWThread implements Runnable {
	public static final Logger logger = Logger.getLogger(LWThread.class);
	
	public static final boolean DEBUG = false;
	
	public long TIME_SLICE = 20;

    public static final int SLEEPING = 0x01;
    public static final int ALIVE = 0x02;
    public static final int WAITING_FOR_CHILDREN = 0x04;
    public static final int ABORT_ON_CHILD_FAILURE = 0x08;
    public static final int ABORTING = 0x10;

    private int tstate;
    private LWThread parent;
    private Map<Integer, LWThread> children;
    private KRunnable runnable;
    private State state;
    private String name;
    private RuntimeException t;
    private Stack stack;
    private int runCount;
    private long deadline;

    public static int contextSwitches;

    private static int id = 1;
    
    public class Listener implements FutureListener {
		@Override
        public void futureUpdated(Future fv) {
            if (DEBUG)
                System.out.println(LWThread.this + " futureUpdated()");
            Scheduler.getScheduler().awake(LWThread.this);
        }
		
		public LWThread getThread() {
			return LWThread.this;
		}
    }

    private synchronized static String nextId() {
        return String.valueOf(id++);
    }

    public LWThread(KRunnable r, Stack stack) {
        this(nextId(), r, stack);
        Exception e = new Exception();
        System.out.println(this + " created by " + e.getStackTrace()[1].getClassName());
    }

    public LWThread(String name, KRunnable r, Stack stack) {
        this.name = name;
        this.runnable = r;
        this.stack = stack;
    }

    public void start() {
        if (DEBUG)
            System.out.println(this + " start()");
        synchronized(this) {
        	setState(ALIVE, true);
        }
        parent = LWThread.currentThread();
        if (parent != null) {
        	parent.addChild(this);
        }
        Scheduler.getScheduler().schedule(this);
    }

    protected synchronized void setState(int mask, boolean value) {
        if (value) {
            tstate |= mask;
        }
        else {
            tstate &= ~mask;
        }
    }

    private synchronized boolean getState(int mask) {
        return (tstate & mask) != 0;
    }
    
    public String getState() {
    	return ((tstate & SLEEPING) != 0 ? "S" : "-") +
    		   ((tstate & ALIVE) != 0 ? "A" : "-") + 
    		   ((tstate & WAITING_FOR_CHILDREN) != 0 ? "W" : "-") +
    		   ((tstate & ABORT_ON_CHILD_FAILURE) != 0 ? "A" : "-") +
    		   ((tstate & ABORTING) != 0 ? "X" : "-");
    }

    private static ThreadLocal<LWThread> lwthread = new ThreadLocal<LWThread>() {
        @Override
        protected LWThread initialValue() {
            return null;
        }
    };

    public static final LWThread currentThread() {
        return lwthread.get();
    }

    private void schedule() {

    }

    public final void checkSlice() {
        if (isSliceExpired() && (state == null || state.isEmpty())) {
            // System.out.println(this + " slice expired");
            yield();
        }
    }

    private final void yield() {
        synchronized (this) {
            Yield y = new Yield();
            state = y.getState();
            throw y;
        }
    }

    public final int checkSliceAndPopState() {
        if (state == null) {
            if (isSliceExpired()) {
                yield();
            }
            return 0;
        }
        else {
            if (isSliceExpired()) {
                if (state.isEmpty()) {
                    yield();
                }
            }
            return popIntState();
        }
    }

    protected final boolean isSliceExpired() {
        return Clock.ms > deadline;
    }

    public final int popIntState() {
        Integer i = (Integer) popState();
        return i == null ? 0 : i;
    }
    
    public final Object popState() {
        if (state == null) {
            return null;
        }
        else {
            if (state.isEmpty()) {
            	if (getState(ABORTING)) {
            		throw new Abort();
            	}
                state = null;
                return null;
            }
            else {
                return state.pop();
            }
        }
    }

    public synchronized void waitFor() {
        if (DEBUG)
            System.out.println(this + " waitFor()");
        try {
            while (getState(ALIVE)) {
                wait();
            }
            if (t != null) {
                throw t;
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void addChild(LWThread thread) {
        if (children == null) {
            children = new HashMap<Integer, LWThread>();
        }
        children.put(System.identityHashCode(thread), thread);
    }
    
    private boolean running;
    
    public void run() {
        try {
        	synchronized(this) {
        		if (running) {
        			throw new RuntimeException(this + " already running " + tstate);
        		}
        		running = true;
        		if (isAborting()) {
        			if (runCount == 0) {
        				throw new Abort();
        			}
        		}
        		setState(SLEEPING, false);
        		runCount++;
        		deadline = Clock.ms + TIME_SLICE;
        	}
            contextSwitches++;
            lwthread.set(this);
            if (runnable != null) {
                if (DEBUG)
                    System.out.println(this + " run()+");
            	runnable.run(this);
                if (DEBUG)
                    System.out.println(this + " run()-");
            }
            synchronized(this) {
            	running = false;
            }
            done(null);
        }
        catch (ConditionalYield e) {
            if (DEBUG) {
                System.out.println(this + " caught ConditionalYield "
                        + e.getFuture());
                //e.printStackTrace();
            }
            boolean sleeping;
            synchronized(this) {
            	running = false;
            	this.state = e.getState();
            	sleeping = setSleeping();
            }
            if (sleeping) {
            	e.getFuture().addListener(new Listener());
            }
        }
        catch (WaitYield e) {
        	synchronized(this) {
        		running = false;
        		this.state = e.getState();
        		if (setSleeping()) {
        			Scheduler.getScheduler().awakeAfter(this, e.getDelay());
        		}
        	}
        }
        catch (Yield e) {
            if (DEBUG)
                System.out.println(this + " caught Yield " + e.getState());
            synchronized(this) {
            	running = false;
            	this.state = e.getState();
            }
            Scheduler.getScheduler().schedule(this);
        }
        catch (LWThreadDeath e) {
            if (DEBUG)
                System.out.println(this + " caught LWThreadDeath");
            synchronized(this) {
            	running = false;
            }
            done(null);
        }
        catch (Abort e) {
            if (DEBUG)
                System.out.println(this + " caught Abort");
            synchronized(this) {
            	running = false;
            }
            done(null);
        }
        catch (RuntimeException e) {
            if (DEBUG)
                System.out.println(this + " caught exception");
            synchronized(this) {
            	running = false;
            }
            // System.out.println(this);
            e.printStackTrace();
            System.out.println(this + " Run count: " + runCount);
            System.out.println(this + " crt state: " + state);
            done(e);
        }
    }

    private boolean setSleeping() {
    	if (isAborting()) {
    		Scheduler.getScheduler().schedule(this);
    		return false;
    	}
    	else {
    		setState(SLEEPING, true);
    		Scheduler.getScheduler().putToSleep(this);
    		return true;
    	}
	}

	protected synchronized void done(RuntimeException e) {
        if (DEBUG)
            System.out.println(this + " done()");
        setState(ALIVE, false);
        if (parent == null) {
        	System.out.println(this + " no parent");
        }
        parent.childFinished(this, e);
        notifyAll();
    }

    protected synchronized void childFinished(LWThread thread,
            RuntimeException t) {
        children.remove(System.identityHashCode(thread));
        if (t != null) {
            if (isAbortOnChildFailure()) {
                this.t = t;
                abortChildren();
            }
            else {
                try {
                    printException(t);
                }
                catch (Exception e) {
                    printException(e);
                }
            }
        }
        if (children.isEmpty() && getState(WAITING_FOR_CHILDREN)) {
            setState(WAITING_FOR_CHILDREN, false);
            Scheduler.getScheduler().awake(this);
        }
    }

    private void printException(Exception e) {
        if (e instanceof ExecutionException) {
            e.printStackTrace();
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString());
            sb.append('\n');
            StackTraceElement[] st = e.getStackTrace();
            for (StackTraceElement ste : st) {
                translateStackTraceElement(ste, sb);
            }
            System.err.print(sb.toString());
        }
    }

    private boolean translateStackTraceElement(StackTraceElement e,
            StringBuilder sb) {
        if (true) {
            sb.append('\t');
            sb.append(e.toString());
            sb.append('\n');
            return true;
        }
        Pattern p = Pattern.compile("(.*)_(\\d*)_\\d\\d\\d\\d");
        Matcher m = p.matcher(e.getMethodName());
        if (m.matches()) {
            sb.append("\tat ");
            sb.append(m.group(1));
            sb.append(" (");
            sb.append(e.getClassName());
            sb.append(".k:");
            sb.append(m.group(2));
            sb.append(")\n");
            return true;
        }
        else {
            sb.append('\t');
            sb.append(e.toString());
            sb.append('\n');
            return true;
        }
    }

    public void abortChildren() {
        if (DEBUG)
            System.out.println(this + " abortChildren()");
        for (LWThread child : children.values()) {
            child.abort();
        }
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "LWThread-" + name;
    }
    
    private static final NumberFormat NF = new DecimalFormat("00000000");
    private volatile String cid;
    
    public String getID() {
    	if (cid == null) {
    		cid = "T[" + NF.format(System.identityHashCode(this)) + "]";
    	}
    	return cid;
    }

    public final synchronized void sleep(long delay) {
        sleep(delay, 0);
    }

    public final synchronized void sleep(long delay, int state) {
        if (DEBUG)
            System.out.println(this + " sleep(" + delay + ")");
        if (isAborting()) {
        	throw new Abort();
        }
        WaitYield y = new WaitYield(state, delay);
        this.state = y.getState();
        setState(SLEEPING, true);
        throw y;
    }

    public final synchronized void sleepIndefinitely(int state) {
        if (DEBUG)
            System.out.println(this + " sleepIndefinitely()");
        if (isAborting()) {
        	throw new Abort();
        }
        Yield y = new Yield(state);
        this.state = y.getState();
        setState(SLEEPING, true);
        throw y;
    }

    public final boolean isSleeping() {
        return getState(SLEEPING);
    }

    public final boolean isAlive() {
        return getState(ALIVE);
    }

    public final boolean isAbortOnChildFailure() {
        return getState(ABORT_ON_CHILD_FAILURE);
    }

    public void setAbortOnChildFailure(boolean b) {
        setState(ABORT_ON_CHILD_FAILURE, b);
    }

    public final void abort() {
        if (DEBUG)
            System.out.println(this + " abort()");
        synchronized(this) {
        	if (!getState(ALIVE)) {
        		return;
        	}
        	setState(ABORTING, true);
        	if (getState(SLEEPING)) {
        		Scheduler.getScheduler().awake(this);
        	}
        }
    }

    public final void awake() {
        if (DEBUG)
            System.out.println(this + " awake()");
        Scheduler.getScheduler().awake(this);
    }

    public boolean isAborting() {
        return getState(ABORTING);
    }

    public final synchronized LWThread fork(int state) {
        if (DEBUG)
            System.out.println(this + " fork(" + state + ")");
        int id = (children == null ? 1 : children.size() + 1);
        LWThread child = new LWThread(name + "-" + id, runnable, stack.copy());
        
        if (children == null) {
            children = new HashMap<Integer, LWThread>();
        }
        children.put(System.identityHashCode(child), child);

        child.state = new State();
        child.state.replaceBottom(state);
        if (child.state.isEmpty()) {
            System.err.println("Child state empty");
            System.exit(1);
        }
        return child;
    }
    
    public final synchronized LWThread fork(KRunnable r) {
        if (DEBUG)
            System.out.println(this + " fork(" + state + ")");
        int id = (children == null ? 1 : children.size() + 1);
        LWThread child = new LWThread(name + "-" + id, r, stack.copy());
        
        if (children == null) {
            children = new HashMap<Integer, LWThread>();
        }
        children.put(System.identityHashCode(child), child);
        
        if (DEBUG) {
        	Exception e = new Exception();
        	System.out.println(child + " forked by " + e.getStackTrace()[1].getClassName());
        }
        
        return child;
    }
    
    public final synchronized void waitForChildren() {
        if (DEBUG)
            System.out.println(this + " waitForChildren()");
        if (children == null || !children.isEmpty()) {
        	if (isAborting()) {
        		throw new Abort();
        	}
            Yield y = new Yield(0);
            this.state = y.getState();
            setState(SLEEPING, true);
            setState(WAITING_FOR_CHILDREN, true);
            throw y;
        }
        else if (t != null) {
            throw t;
        }
    }
    
    public final void die() {
        if (DEBUG)
            System.out.println(this + " die()");
        throw new LWThreadDeath();
    }

    public int getChildCount() {
        return children.size();
    }

    public RootThread getRoot() {
        if (parent == null) {
            return null;
        }
        else {
            return parent.getRoot();
        }
    }

    public LWThread getParent() {
        return parent;
    }
    
    public Stack getStack() {
    	return stack;
    }
    
    public void setStack(Stack stack) {
    	this.stack = stack;
    }
    
    /**
     * Only available if thread is waiting on a condition or yielding.
     */
    public synchronized List<Object> getTrace() {
        if (state == null) {
            return null;
        }
        else {
            return state.getTrace();
        }
    }
}
