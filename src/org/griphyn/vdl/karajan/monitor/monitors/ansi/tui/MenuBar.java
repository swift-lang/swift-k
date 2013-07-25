/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;
import java.util.Iterator;

public class MenuBar extends Container {
	public static final int TABS_AT_TOP = 0;
	public static final int TABS_AT_BOTTOM = 1;

	private int activeBgColor, activeFgColor;
	private Menu active;

	public MenuBar() {
		bgColor = ANSI.WHITE;
		fgColor = ANSI.BLACK;
		activeBgColor = ANSI.BLACK;
		activeFgColor = ANSI.WHITE;
		setLayer(TOP_LAYER);
	}

	public int getActiveBgColor() {
		return activeBgColor;
	}

	public void setActiveBgColor(int activeBgColor) {
		this.activeBgColor = activeBgColor;
	}

	public int getActiveFgColor() {
		return activeFgColor;
	}

	public void setActiveFgColor(int activeFgColor) {
		this.activeFgColor = activeFgColor;
	}

	public void addMenu(Menu menu) {
		super.add(menu);
		invalidate();
	}

	public void add(Component comp) {
		if (comp instanceof Menu) {
			super.add(comp);
			invalidate();
		}
		else {
			throw new ClassCastException("Component must be a Menu");
		}
	}

	protected void drawTree(ANSIContext context) throws IOException {
		validate();
		Component parent = getParent();
		if (parent != null) {
			sx = parent.sx + x;
			sy = parent.sy + y;
		}
		context.lock();
		try {
			draw(context);

			context.moveTo(sx, sy);
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			context.spaces(width);

			context.moveTo(sx + 1, sy);

			Iterator<Component> i = components.iterator();
			while (i.hasNext()) {
				Menu c = (Menu) i.next();

				if (c != active) {
					context.bgColor(bgColor);
					context.fgColor(fgColor);
					context.bold(false);
				}
				else {
					context.bgColor(activeBgColor);
					context.fgColor(activeFgColor);
					context.bold(true);
				}
				context.spaces(1);
				c.drawTitle(context);
				context.spaces(1);
			}
			context.bold(false);

			if (active != null) {
				context.bgColor(bgColor);
				context.fgColor(fgColor);
				active.drawTree(context);
			}
		}
		finally {
			context.unlock();
		}
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		int cx = 1;
		Iterator<Component> i = components.iterator();
		while (i.hasNext()) {
			Menu c = (Menu) i.next();
			c.setLocation(cx, 1);
			c.setSize(width, height - 1);
			cx += c.width + 2;
		}
		setValid(true);
	}

	public boolean childFocused(Component component) {
		boolean f = true;
		if (focused != null && focused != component) {
			f = focused.unfocus();
			if (f) {
				focused.focusLost();
			}
		}
		if (f) {
			focused = component;
			focused.focusGained();
		}
		return f;
	}
	
    public void childUnfocused(Component component) {
        unfocus();
    }

    public void focusGained() {
	}

	public void setActive(Menu menu) {
		if (active != null) {
			active.setVisible(false);
		}
		active = menu;
		if (active != null) {
			active.setVisible(true);
			active.focus();
		}
	}
}
