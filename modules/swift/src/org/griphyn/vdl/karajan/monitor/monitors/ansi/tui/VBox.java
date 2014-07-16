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
 * Created on Feb 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

public class VBox extends Container {
	private static final Component DUMMY = new Component();
	private boolean dividerLine;
	private float split;
	private HLine hline;
	
	public VBox() {
		add(DUMMY);
		add(DUMMY);
		split = 0.5f;
	}
	
	public void setTop(Component comp) {
		set(0, comp);
	}
	
	public void setBottom(Component comp) {
		set(1, comp);
	}
	
	public Component getTop() {
		return get(0);
	}
	
	public Component getBottom() {
		return get(1);
	}
	
	protected Component get(int index) {
		Component c = components.get(index);
		if (c == DUMMY) {
			return null;
		}
		else {
			return c;
		}
	}
	
	protected void set(int index, Component comp) {
		components.set(index, comp);
		comp.setParent(this);
	}

	public boolean hasDividerLine() {
		return dividerLine;
	}

	public void setDividerLine(boolean dividerLine) {
		this.dividerLine = dividerLine;
	}

	public float getSplit() {
		return split;
	}

	public void setSplit(float split) {
		if (split < 0 && split > 1) {
			throw new IllegalArgumentException("Split not within [0, 1]");
		}
		this.split = split;
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		int h = height;
		if (dividerLine) {
			h--;
		}
		int t = (int) (h*split);
		int b = h - t;
		if (dividerLine) {
			if (hline == null) {
				hline = new HLine();
				hline.setBgColor(bgColor);
				hline.setFgColor(fgColor);
				add(hline);
			}
			hline.setLocation(0, t + 1);
			hline.setSize(width, 1);
		}
		Component top = getTop();
		top.setLocation(0, 0);
		top.setSize(width, t);
		Component bottom = getBottom();
		bottom.setLocation(0, height - b);
		bottom.setSize(width, b);
		super.validate();
	}
}
