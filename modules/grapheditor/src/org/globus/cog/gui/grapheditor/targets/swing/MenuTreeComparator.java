
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

import java.util.Comparator;

public class MenuTreeComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		ActionTree mt1 = (ActionTree) o1;
		ActionTree mt2 = (ActionTree) o2;
		return mt1.rank - mt2.rank;
	}

}