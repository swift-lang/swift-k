/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class Frame extends Container {
	private String title;
	private float titleAlignment;
	private boolean filled;

	public Frame() {
		bgColor = ANSI.WHITE;
		fgColor = ANSI.BLACK;
	}

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			if (filled) {
				context.filledRect(sx + 1, sy + 1, width - 2, height - 2);
			}

			context.frame(sx, sy, width, height);
			context.bgColor(bgColor);
			if (title != null) {
				int tl = title.length() + 2;
				int space = width - tl;
				int tp = (int) (titleAlignment * space) + 1;
				context.moveTo(sx + tp, sy);
				context.putChar(' ');
				context.text(title);
				context.putChar(' ');
			}
		}
		finally {
			context.unlock();
		}
		super.draw(context);

	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public float getTitleAlignment() {
		return titleAlignment;
	}

	public void setTitleAlignment(float titleAlignment) {
		this.titleAlignment = titleAlignment;
	}

	public boolean isFilled() {
		return filled;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}
}
