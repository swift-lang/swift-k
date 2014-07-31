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
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.slocal;

import java.io.IOException;
import java.io.InputStream;

class BinaryStreamProcessor extends StreamProcessor {
	private InputStream stream;
	private byte[] buffer;
	private int crt;
	private boolean closed;
	private BinaryOutputListener listener;

	public BinaryStreamProcessor(InputStream stream, BinaryOutputListener listener) {
		this.stream = stream;
		this.listener = listener;
		buffer = new byte[1024];
		crt = 0;
	}

	public boolean poll() {
		if (closed) {
			return false;
		}
		try {
			int avail = stream.available();
			if (avail > 0) {
				int count = Math.min(buffer.length - crt, avail);
				crt += stream.read(buffer, crt, count);
				if (crt == buffer.length) {
					commit();
				}
			}
			return true;
		}
		catch (IOException e) {
			close();
			return false;
		}
	}
	
	public void close() {
		poll();
		commit();
		closed = true;
	}
	
	protected void commit() {
		if (crt > 0) {
			listener.dataReceived(buffer, 0, crt);
			crt = 0;
		}
	}

	protected boolean isClosed() {
		return closed;
	}
}