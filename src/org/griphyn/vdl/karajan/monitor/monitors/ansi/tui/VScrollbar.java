/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class VScrollbar extends Component {
	private int total, current;

	protected void draw(ANSIContext context) throws IOException {
		context.bgColor(bgColor);
		context.fgColor(fgColor);
		context.lineArt(true);
		for (int i = 0; i < height; i++) {
			context.moveTo(sx, sy + i);
			context.putChar(ANSI.GCH_HASH);
		}
		int pos = 0;
		if (total > 1) {
			if (current > total) {
				current = total;
			}
			if (current < 0) {
				current = 0;
			}
			pos = (height - 1) * current / (total - 1);
		}
		context.bgColor(fgColor);
		context.fgColor(bgColor);
		context.moveTo(sx, sy + pos);
		context.putChar(' ');
		context.lineArt(false);
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

}
