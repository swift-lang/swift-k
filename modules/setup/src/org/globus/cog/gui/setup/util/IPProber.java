
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.setup.util;

import java.net.Socket;

public class IPProber extends Thread {
	private Callback callback;
	private String probedIP;
	private IPProber prober;
	private boolean timer;
	private boolean internal;
	private int timeout;
	private boolean finished;

	public IPProber(boolean internal, boolean timer, int timeout) {
		callback = null;
		probedIP = null;
		this.timer = timer;
		this.internal = internal;
		this.prober = null;
		this.timeout = timeout;
		this.finished = false;
	}

	public IPProber() {
		this(false, true, 20000);
	}

	public IPProber(int timeout) {
		this(false, true, timeout);
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void removeCallback() {
		this.callback = null;
	}

	public void start() {
		if (!internal) {
			prober = new IPProber(true, false, 0);
			prober.start();
		}
		super.start();
	}

	public void run() {
		if (timer) {
			int ms = 0;
			while ((ms < timeout) && (!prober.finished())) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {
				}
				ms += 100;
			}
			if (prober.finished()) {
				probedIP = prober.getProbedIP();
			}
			else {
				probedIP = "127.0.0.1";
			}
			if (callback != null) {
				callback.callback(this, probedIP);
			}
		}
		else {
			try {
				Socket sock = new Socket("www.globus.org", 80);
				sock.setSoTimeout(10);

				probedIP = sock.getLocalAddress().getHostAddress();
				sock.close();
			}
			catch (Exception e) {
				probedIP = "127.0.0.1";
			}
			finished = true;
		}
	}

	public String getProbedIP() {
		return probedIP;
	}

	public boolean finished() {
		return finished;
	}
}
