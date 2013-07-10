
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.test;

public class Wrapper extends Thread {

	private int timeout;
	private TestInterface test;
	private String machine;
	private volatile boolean finished;
	private Exception exception;

	public Wrapper(TestInterface test, String machine, int timeout) {
		this.timeout = timeout;
		this.test = test;
		this.machine = machine;
		this.finished = false;
		this.exception = null;
	}

	public boolean execute() throws Exception {
		start();
		int count = timeout;
		while ((count > 0) && (!finished) && (exception == null)) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			count--;
		}
		if (!finished) {
			System.out.println("Timeout!");
			stop();
		}
		if (exception != null) {
			throw exception;
		}
		return finished;
	}

	public void run() {
		try {
			test.test(machine);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			exception = e;
		}
		finished = true;
	}
}
