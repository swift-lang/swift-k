/*
 * Created on Feb 1, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class CharacterMap extends Component {
	public CharacterMap() {
		setSize(16, 16);
	}

	protected void draw(ANSIContext context) throws IOException {
		for (int i = 0; i < 16; i++) {
			context.moveTo(sx, sy + i);
			context.lineArt(true);
			for (int j = 0; j < 16; j++) {
				context.putChar((char) (i*16 + j));
				context.putChar(' ');
			}
			context.lineArt(false);
		}
	}
}
