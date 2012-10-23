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

public class Tab extends Container {
	private String title;
	private LabelWithAccelerator tmtext;
	private Component contents;

	public Tab() {
		bgColor = ANSI.BLACK;
		fgColor = ANSI.WHITE;
	}

	public Tab(String title) {
		this();
		setTitle(title);
	}

	public void setTitle(String title) {
		this.title = title;
		tmtext = new LabelWithAccelerator(title);
	}
	
	public LabelWithAccelerator getLabel() {
	    return tmtext;
	}

	public Component getContents() {
		return contents;
	}

	public void setContents(Component contents) {
		this.contents = contents;
		contents.setParent(this);
		invalidate();
	}

	public void drawTitle(ANSIContext context) throws IOException {
		tmtext.draw(context);
	}

	protected void drawTree(ANSIContext context) throws IOException {
		super.drawTree(context);
		if (contents != null) {
			contents.drawTree(context);
		}
		else {
			// context.filledFrame(sx, sy, width, height);
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
		if (parent instanceof TabbedContainer) {
			((TabbedContainer) getParent()).setActive(this);
		}
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		if (contents != null) {
			contents.setLocation(1, 1);
			contents.setSize(width - 2, height - 2);
			contents.validate();
		}
		super.validate();
	}

	public void setAcceleratorKey(Key key) {
		tmtext.setAcceleratorKey(key);
	}

	public Key getAcceleratorKey() {
		return tmtext.getAcceleratorKey();
	}

	public boolean keyboardEvent(Key key) {
		if (key.equals(getAcceleratorKey())) {
			activate();
			return true;
		}
		else if (contents != null) {
			return contents.keyboardEvent(key);
		}
		else {
			return false;
		}
	}
}
