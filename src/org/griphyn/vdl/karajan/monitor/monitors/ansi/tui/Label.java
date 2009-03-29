/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class Label extends Component {
	public static final float LEFT = 0.0f;
	public static final float RIGHT = 1.0f;
	public static final float CENTER = 0.5f;

	private float justification;
	private String text;

	public Label(String text) {
		bgColor = ANSI.BLACK;
		fgColor = ANSI.WHITE;
		this.text = text;
	}

	public Label() {
		this(null);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float getJustification() {
		return justification;
	}

	public void setJustification(float justification) {
		this.justification = justification;
	}

	protected void draw(ANSIContext context) throws IOException {
		int lpad = 0;
		int rpad = 0;
		int empty = width - text.length();
		lpad = (int) (justification * empty);
		rpad = empty - lpad;
		
		context.lock();
		try {
			context.moveTo(sx, sy);
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			context.spaces(lpad);
			if (width == 0) {
				// not much we can do
			}
			else if (width < text.length()) {
				if (width <= 2) {
					context.text(">");
				}
				else {
					context.text(text.substring(0, width - 2));
					context.text(" >");
				}
			}
			else {
				context.text(text);
			}
			context.spaces(rpad);
		}
		finally {
			context.unlock();
		}
	}
}
