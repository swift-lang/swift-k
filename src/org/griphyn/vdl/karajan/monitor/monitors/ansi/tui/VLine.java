/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class VLine extends Component {

	protected void draw(ANSIContext context) throws IOException {
		context.bgColor(bgColor);
		context.fgColor(fgColor);
		context.lineArt(true);
		for (int i = 0; i < height; i++) {
			context.moveTo(sx, sy + i);
			context.putChar(ANSI.GCH_V_LINE);
		}
		context.lineArt(false);
	}
}
