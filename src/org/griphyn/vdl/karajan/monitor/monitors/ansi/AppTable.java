/*
 * Created on Sep 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;

public class AppTable extends STable {
	private Dialog d;

	protected void createDialog() {
		d = new AppDialog(getScreen(), getApplicationItem());
	}
	
	protected ApplicationItem getApplicationItem() {
		return (ApplicationItem) getItem();
	}
}
