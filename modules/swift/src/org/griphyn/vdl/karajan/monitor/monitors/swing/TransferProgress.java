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
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

public class TransferProgress {
	private long current, total;

	public TransferProgress(long current, long total) {
		this.current = current;
		this.total = total;
	}

	public TransferProgress(String progress) {
		if (progress == null) {
			total = -1;
			return;
		}
		int index = progress.indexOf('/');
		if (index == -1) {
			total = -1;
			return;
		}
		current = Long.parseLong(progress.substring(0, index));
		total = Long.parseLong(progress.substring(index + 1));
	}

	public long getCurrent() {
		return current;
	}

	public long getTotal() {
		return total;
	}
}
