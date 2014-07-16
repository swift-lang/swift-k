//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 30, 2012
 */
package k.thr;

public class Clock extends Thread {
	private static final int STEP = 1;
	
	public static long ms;
	
	private static Clock clock;
	
	public synchronized static void init() {
		if (clock != null) {
			clock = new Clock();
			clock.start();
		}
	}
	
	private Clock() {
		super("Clock");
		setDaemon(true);
		ms = System.currentTimeMillis();
	}
	
	public void run() {
		try {
			while (true) {
				Thread.sleep(STEP);
				ms = System.currentTimeMillis();
			}
		}
		catch (InterruptedException e) {
		}
	}
}
