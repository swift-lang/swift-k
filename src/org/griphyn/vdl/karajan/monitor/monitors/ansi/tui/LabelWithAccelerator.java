/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class LabelWithAccelerator extends Component {
	private String text;
	private Key acceleratorKey;

	public LabelWithAccelerator(String text) {
		this.text = text;
	}

	public LabelWithAccelerator() {
		this(null);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getLabelSize() {
		if (text == null) {
			return 0;
		}
		else {
			return text.length() - (text.indexOf('&') == -1 ? 0 : 1);
		}
	}

	protected void draw(ANSIContext context) throws IOException {
		if (text == null) {
			return;
		}
		boolean underline = false;
		context.lock();
		try {
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c == '&') {
					underline = true;
					context.underline(true);
				}
				else {
					context.putChar(c);
					if (underline) {
						acceleratorKey = new Key(Key.MOD_ALT, Character.toLowerCase(c));
						underline = false;
						context.underline(false);
					}
				}
			}
		}
		finally {
			context.unlock();
		}
	}

	public Key getAcceleratorKey() {
		return acceleratorKey;
	}

	public void setAcceleratorKey(Key key) {
		this.acceleratorKey = key;
	}
}
