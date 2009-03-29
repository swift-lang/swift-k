/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.io.PrintStream;

import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;

public class LocalANSIDisplay extends AbstractANSIDisplay {
    private PrintStream sout, serr;
    private ANSIMonitor m;
    
	public LocalANSIDisplay(ANSIMonitor m) {
		super(m.getState(), System.in, System.out);
		this.m = m;
		sout = System.out;
		serr = System.err;
		System.setOut(new PrintStream(System.out) {
            public void write(byte[] buf, int off, int len) {
            }

            public void write(int b) {
            }
		});
		
		System.setErr(new PrintStream(System.err) {
            public void write(byte[] buf, int off, int len) {
            }

            public void write(int b) {
            }
        });
	}

    protected void cleanup() throws IOException {
        m.remove(this);
        super.cleanup();
        getContext().bgColor(ANSI.BLACK);
        getContext().fgColor(ANSI.WHITE);
        getContext().clear();
        getContext().sync();
        System.setOut(sout);
        System.setErr(serr);
    }
}
