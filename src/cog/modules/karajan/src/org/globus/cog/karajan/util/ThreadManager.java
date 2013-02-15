// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 10, 2004
 */
package org.globus.cog.karajan.util;

public class ThreadManager {
	public static final int DEFAULT_MAX_THREADS = 64;
	public static final boolean DEFAULT_CHECK_MEMORY = true;

	public static final String TM_MAX_THREADS = "TMMaxThreads";
	public static final String TM_CHECK_MEMORY = "TMCheckMemory";

	private int crtThreads;

	private static ThreadManager defaultTM;

	public synchronized static ThreadManager getDefault() {
		if (defaultTM == null) {
			defaultTM = new ThreadManager();
		}
		return defaultTM;
	}

	public ThreadManager() {
		crtThreads = 0;
	}

	public void allocate(int requested) {
		while (true) {
			synchronized (this) {
				if (canAllocate(requested)) {
					crtThreads += requested;
					break;
				}
				/*else {
					try {
						wait(0);
					}
					catch (InterruptedException e) {
					}
				}*/
			}
		}
	}

	public void release(int count) {
		synchronized (this) {
			crtThreads -= count;
			notify();
		}
	}

	public boolean canAllocate(int requested) {
		synchronized (this) {
			if (crtThreads + requested > getMaxThreads()) {
				return false;
			}
		}
		if (getCheckMemory()) {
			if (requested * getMemPerThread() > getFreeMemory()) {
				return false;
			}
		}
		return true;
	}

	public int getMaxThreads() {
		if (System.getProperty(TM_MAX_THREADS) != null) {
			return Integer.parseInt(System.getProperty(TM_MAX_THREADS));
		}
		else {
			return DEFAULT_MAX_THREADS;
		}
	}

	public boolean getCheckMemory() {
		if (System.getProperty(TM_CHECK_MEMORY) != null) {
			return Boolean.getBoolean(TM_CHECK_MEMORY);
		}
		else {
			return DEFAULT_CHECK_MEMORY;
		}
	}

	private long getMemPerThread() {
		return 1024 * 192; //192K by default - how do I get the actual value?
	}

	private long getFreeMemory() {
		return Runtime.getRuntime().maxMemory()
				- (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}
}