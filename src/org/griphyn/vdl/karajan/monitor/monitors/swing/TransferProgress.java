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
