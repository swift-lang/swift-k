
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.events;

import java.awt.Component;
import java.util.EventObject;

/**
 *  Navigation event
 */
public class NavEvent extends EventObject {

	public static int Cancel = 1;

	public static int Prev = 2;

	public static int Next = 3;

	public static int Finish = 4;

	public static int Jump = 5;

	private int navAction;
	private int jumpIndex;

	public NavEvent(Component source, int navAction) {
		super(source);
		this.navAction = navAction;
		this.jumpIndex = 0;
	}

	public NavEvent(Component source, int navAction, int jumpIndex) {
		super(source);
		this.navAction = navAction;
		this.jumpIndex = jumpIndex;
	}

	public int getNavAction() {
		return navAction;
	}

	public int getJumpIndex() {
		return jumpIndex;
	}
}
