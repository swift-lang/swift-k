/*
 * Created on Jun 19, 2006
 */
package org.griphyn.vdl.karajan;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InHook extends InputStream implements Runnable {
	public synchronized static void install(Monitor m) {
		if (!(System.in instanceof InHook)) {
			System.setIn(new InHook(System.in, m));
		}
	}

	private BufferedInputStream is;
	private Monitor m;

	private InHook(InputStream is, Monitor m) {
		if (is instanceof BufferedInputStream) {
			this.is = (BufferedInputStream) is;
			this.m = m;
			Thread t = new Thread(this, "stdin debugger");
			t.setDaemon(true);
			t.start();
		}
	}

	public int read() throws IOException {
		return is.read();
	}

	public void run() {
		while (true) {
			try {
				while (is.available() > 0) {
					is.mark(1);
					int c = is.read();
					if (c == 'd') {
						m.toggle();
					}
					else if (c == 'v') {
						m.dumpVariables();
					}
					else if (c == 't') {
						m.dumpThreads();
					}
					else {
						is.reset();
					}
				}
				if (is.available() == 0) {
					Thread.sleep(250);
				}
			}
			catch (IOException e) {
				return;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
