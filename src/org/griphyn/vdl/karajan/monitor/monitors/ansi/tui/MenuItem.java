/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class MenuItem extends LabelWithAccelerator {
	protected boolean active;

	public MenuItem(String text) {
		super(text);
		bgColor = ANSI.WHITE;
		fgColor = ANSI.BLACK;
	}

	protected void validate() {
		width = getText().length();
		super.validate();
	}

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.moveTo(sx, sy);
			if (active) {
				context.bgColor(fgColor);
				context.fgColor(bgColor);
			}
			else {
				context.bgColor(bgColor);
				context.fgColor(fgColor);
			}
			super.draw(context);
		}
		finally {
			context.unlock();
		}
	}

	public void activate() {
		Component parent = getParent();
		if (parent instanceof Menu) {
			((Menu) getParent()).setActive(this);
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void itemSelected() {
	}

	private Key acceleratorKey;
	
    public Key getAcceleratorKey() {
        if (acceleratorKey == null) {
            //no modifiers
            acceleratorKey = new Key(super.getAcceleratorKey().getKey());
        }
        return acceleratorKey;
    }
}
