
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.util;

import java.io.IOException;

import org.globus.cog.util.ntp.NTPClient;

public class NTPThreadedClient extends Thread {
	private NTPClient NTP;
	private String host;
	private volatile boolean needsUpdate;
	private boolean done;
	private String error = null;
	private volatile Callback callback;

	public NTPThreadedClient(String host) {
		this.host = host;
		needsUpdate = false;
		done = false;
		callback = null;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void removeCallback() {
		callback = null;
	}

	public void run() {
		NTP = new NTPClient(host);
		while (!done) {
			if (needsUpdate) {
				if (NTP != null) {
					try {
						NTP.update();
						if (callback != null) {
							callback.callback(this, null);
						}
					}
					catch (IOException e) {
						if (callback != null) {
							callback.callback(this, "Error contacting time server.");
						}
					}
				}
				else {
					if (callback != null) {
						callback.callback(this, error);
					}
				}
				needsUpdate = false;
			}
			try {
				sleep(100);
			}
			catch (InterruptedException e) {

			}
		}
	}

	public void update() {
		needsUpdate = true;
	}

	public long getVariation() {
		if (NTP != null) {
			return NTP.getVariation();
		}
		else {
			return 0;
		}
	}

	public boolean withinDelta(long delta) {
		if (NTP != null) {
			return NTP.withinDelta(delta);
		}
		else {
			return true;
		}
	}

	public boolean isValid() {
		return (NTP != null);
	}

	public void quit() {
		done = true;
	}
}
