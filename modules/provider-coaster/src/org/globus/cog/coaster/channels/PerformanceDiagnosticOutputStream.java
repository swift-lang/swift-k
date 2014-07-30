/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 16, 2011
 */
package org.globus.cog.coaster.channels;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class PerformanceDiagnosticOutputStream extends OutputStream {
	public static final Logger logger = Logger.getLogger(PerformanceDiagnosticOutputStream.class);

	private final OutputStream delegate;

	private static volatile long bytes, last;
	private static int count = 1;
	
	public static final int INTERVAL = 10; //seconds

	static {
		Timer.every(1000, new Runnable() {
			public void run() {
				count += 1;
				if (count % INTERVAL == 0 && getTotal() > 0) {
    				String s;
    				logger.info(s = "[OUT] Total transferred: "
    						+ units(getTotal()) + "B, current rate: "
    						+ units(getCurrentRate())
    						+ "B/s, average rate: "
    						+ units(getAverageRate())
    						+ "B/s");
				}
				last = bytes;
			}
		});
	}
	
	private static String units(long v) {
		return PerformanceDiagnosticInputStream.units(v);
	}

	public PerformanceDiagnosticOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}
	
	public static long getTotal() {
		return bytes;
	}
	
	public static long getCurrentRate() {
		return bytes - last;
	}
	
	public static long getAverageRate() {
		return bytes / count;
	}
	
	public static void bytesWritten(int count) {
	    bytes += count;
	}

	public void write(int b) throws IOException {
		delegate.write(b);
		bytes++;
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public void write(byte[] b) throws IOException {
		delegate.write(b);
		bytes += b.length;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
		bytes += len;
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public void flush() throws IOException {
		delegate.flush();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public String toString() {
		return delegate.toString();
	}
}
