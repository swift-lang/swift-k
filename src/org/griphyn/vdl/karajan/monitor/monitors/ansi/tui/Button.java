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
	private List listeners;

	public Button(String label) {
		setLabel(label);
		bgColor = ANSI.BLACK;
		fgColor = ANSI.WHITE;
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
			if (this.hasFocus()) {
				context.putChar('[');
			}
			else {
				context.putChar(' ');
			}
			context.spaces(pad);
			twlabel.draw(context);
			context.spaces(width - 2 - pad - len);
			if (this.hasFocus()) {
				context.putChar(']');
			}
			else {
				context.putChar(' ');
			}
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
			listeners = new LinkedList();
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
		List l = new LinkedList(listeners);
		Iterator i = l.iterator();
		while (i.hasNext()) {
			((ActionListener) i.next()).actionPerformed(this);
		}
	}
}
