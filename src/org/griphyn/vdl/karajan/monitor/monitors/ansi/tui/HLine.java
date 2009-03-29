/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class HLine extends Component {

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			context.lineArt(true);
			context.moveTo(sx, sy);
			for (int i = 0; i < width; i++) {
				context.putChar(ANSI.GCH_H_LINE);
			}
			context.lineArt(false);
		}
		finally {
			context.unlock();
		}
	}
}
