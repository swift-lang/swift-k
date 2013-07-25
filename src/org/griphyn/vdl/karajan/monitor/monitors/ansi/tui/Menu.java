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
import java.util.List;

public class Menu extends Container {
	private String title;
	private LabelWithAccelerator tmtext;
	private MenuItem active;

	public Menu() {
		bgColor = ANSI.WHITE;
		fgColor = ANSI.BLACK;
	}

	public Menu(String title) {
		this();
		setTitle(title);
	}

	public void setTitle(String title) {
		this.title = title;
		tmtext = new LabelWithAccelerator(title);
	}

	public void drawTitle(ANSIContext context) throws IOException {
		tmtext.draw(context);
	}

	protected void drawTree(ANSIContext context) throws IOException {
		super.drawTree(context);
	}

	protected void drawChild(Component c, ANSIContext context) throws IOException {
		context.lock();
		try {
			super.drawChild(c, context);
			if (c.width < width - 1) {
				context.spaces(width - 1 - c.width);
			}
		}
		finally {
			context.unlock();
		}
	}

	public static int inc = 10;

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.frame(sx, sy, width, height);
		}
		finally {
			context.unlock();
		}
	}

	public void activate() {
		Component parent = getParent();
		if (parent instanceof MenuBar) {
			((MenuBar) getParent()).setActive(this);
		}
		if (components.size() > 0) {
			((MenuItem) components.get(0)).activate();
		}
	}
	
	public void deactivate() {
	    Component parent = getParent();
        if (parent instanceof MenuBar) {
            ((MenuBar) getParent()).setActive(null);
        }
        unfocus();
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		height = getComponents().size() + 2;
		int max = 10;
		List<Component> l = getComponents();
		for (int i = 0; i < l.size(); i++) {
			Component c = l.get(i);
			c.setLocation(sx + 1, sy + i + 1);
			c.validate();
			int sz = c.width;
			if (sz > max) {
				max = sz;
			}
		}
		for (int i = 0; i < l.size(); i++) {
			Component c = l.get(i);
			c.setSize(max, 1);
		}
		width = max + 2;
		super.validate();
	}

	public void setAcceleratorKey(Key key) {
		tmtext.setAcceleratorKey(key);
	}

	public Key getAcceleratorKey() {
		return tmtext.getAcceleratorKey();
	}
	
	public MenuItem getMenuItem(int index) {
		return (MenuItem) components.get(index);
	}

	public boolean keyboardEvent(Key key) {
		if (key.equals(getAcceleratorKey())) {
			activate();
			return true;
		}
		else if (hasFocus()) {
			if (key.getKey() == Key.DOWN) {
				int index = (components.indexOf(active) + 1) % components.size();
				setActive(getMenuItem(index));
				return true;
			}
			else if (key.getKey() == Key.UP) {
				int index = (components.indexOf(active) - 1) % components.size();
				setActive(getMenuItem(index));
				return true;
			}
			else if (key.isEnter()) {
				if (active != null) {
					active.itemSelected();
				}
			}
			else if (key.equals(Key.ESC)) {
			    deactivate();
			    return true;
			}
			else {
			    for (int i = 0; i < components.size(); i++) {
			        MenuItem item = getMenuItem(i);
			        if (key.equals(item.getAcceleratorKey())) {
			            item.activate();
			            item.itemSelected();
			            return true;
			        }
			    }
			}
			return false;
		}
		else {
			return false;
		}
	}

	public void setActive(MenuItem menu) {
		if (active != null) {
			active.setActive(false);
		}
		active = menu;
		if (active != null) {
			active.setActive(true);
			active.focus();
		}
	}

    public void childUnfocused(Component component) {
    }	
}
