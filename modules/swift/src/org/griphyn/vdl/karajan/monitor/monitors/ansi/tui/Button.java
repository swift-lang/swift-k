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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Button extends Component {
	private String label;
	private LabelWithAccelerator twlabel;
	private List<ActionListener> listeners;

	public Button(String label) {
		setLabel(label);
		bgColor = ANSI.WHITE;
		fgColor = ANSI.BLACK;
	}

	public Button() {
		this(null);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		if (label != null) {
			twlabel = new LabelWithAccelerator(label);
		}
		else {
			twlabel = null;
		}
	}

	protected void draw(ANSIContext context) throws IOException {
		int len = (twlabel == null ? 0 : twlabel.getLabelSize());
		int pad = (width - len - 2) / 2;
		context.lock();
		try {
			context.moveTo(sx, sy);
			context.fgColor(getFgColor());
			context.bgColor(getBgColor());
			context.putChar('[');
			if (this.hasFocus()) {
			    context.bgColor(ANSI.YELLOW);
			}
			context.spaces(pad);
			twlabel.draw(context);
			if (this.hasFocus()) {
                context.bgColor(ANSI.YELLOW);
            }
			context.spaces(width - 2 - pad - len);
			
            context.bgColor(getBgColor());
            context.fgColor(getFgColor());
			context.putChar(']');
		}
		finally {
			context.unlock();
		}
	}

	public boolean keyboardEvent(Key e) {
		if (e.equals(twlabel.getAcceleratorKey())) {
			notifyListeners();
			return true;
		}
		else if (hasFocus() && e.isEnter()) {
			notifyListeners();
			return true;
		}
		else {
			return super.keyboardEvent(e);
		}
	}

	public void addActionListener(ActionListener l) {
		if (listeners == null) {
			listeners = new LinkedList<ActionListener>();
		}
		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		if (listeners == null) {
			return;
		}
		listeners.remove(l);
	}

	public void notifyListeners() {
		if (listeners == null) {
			return;
		}
		List<ActionListener> l = new LinkedList<ActionListener>(listeners);
		Iterator<ActionListener> i = l.iterator();
		while (i.hasNext()) {
			i.next().actionPerformed(this);
		}
	}
}
