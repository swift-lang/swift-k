/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Aug 28, 2008
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.io.PrintStream;

import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;

public class LocalANSIDisplay extends AbstractANSIDisplay {
    public PrintStream sout, serr;
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
        if (getContext().isInitialized()) {
            getContext().bgColor(ANSI.BLACK);
            getContext().fgColor(ANSI.WHITE);
            getContext().clear();
            getContext().sync();
        }
        System.setOut(sout);
        System.setErr(serr);
    }
}
