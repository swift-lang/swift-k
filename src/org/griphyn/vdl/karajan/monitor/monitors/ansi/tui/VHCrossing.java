/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class VHCrossing extends Component {

	protected void draw(ANSIContext context) throws IOException {
		context.bgColor(bgColor);
		context.fgColor(fgColor);
		context.lineArt(true);
		context.moveTo(sx, sy);
		context.putChar(ANSI.GCH_CROSS);
		context.lineArt(false);
	}
}
