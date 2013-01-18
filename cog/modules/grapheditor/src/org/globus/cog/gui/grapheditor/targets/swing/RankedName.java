
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 8, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;

public class RankedName {
	public CanvasAction menuItem;
	public String[] names;
	public int[] ranks;

	public RankedName() {
		names = null;
		ranks = null;
	}

	public RankedName(CanvasAction mi) {
		String[] levels = mi.getName().split(">");
		ranks = new int[levels.length];
		names = new String[levels.length];
		menuItem = mi;
		for (int i = 0; i < levels.length; i++) {
			String[] pair = levels[i].split("#");
			if (levels[i].endsWith("#")) {
				names[i] = null;
				ranks[i] = Integer.parseInt(pair[0]);
			}
			else {
				if (pair.length == 2) {
					ranks[i] = Integer.parseInt(pair[0]);
					names[i] = pair[1];
				}
				else {
					names[i] = pair[0];
					ranks[i] = -1;
				}
			}
		}
	}

	public boolean equals(Object o) {
		if (o instanceof RankedName) {
			RankedName rn = (RankedName) o;
			if (rn.names.length == names.length) {
				for (int i = 0; i < names.length; i++) {
					if (!names[i].equals(rn.names[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		int val = 0;
		for (int i = 0; i < names.length; i++) {
			val += names[i].hashCode();
		}
		return val;
	}

	public String toString() {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			tmp.append(names[i]);
			tmp.append(">");
		}
		return tmp.toString();
	}
}