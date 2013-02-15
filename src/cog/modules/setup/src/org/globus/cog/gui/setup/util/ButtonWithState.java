
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.util;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.globus.cog.util.ImageLoader;

/**
 *  Class for a button with an icon representing a state of none/pass/fail
 */
public class ButtonWithState extends JButton {
	private ImageIcon yes;
	private ImageIcon no;
	private ImageIcon none;

	public static int StateNone = 0;

	public static int StateOk = 1;

	public static int StateFailed = 2;

	public static int StateDisabled = 3;

	private int State;

	public ButtonWithState(String title) {
		super(title);
		ImageLoader il = new ImageLoader();
		yes = il.loadImage("images/22x22/co/iconCheck.png");
		no = il.loadImage("images/22x22/co/iconX.png");
		none = il.loadImage("images/22x22/co/iconBlank.png");
		setToolTipText(title);

		setIcon(none);

		setState(StateNone);
	}

	public void setState(int newState) {
		State = newState;
		if (State == StateNone) {
			setIcon(none);
		}
		if (State == StateOk) {
			setIcon(yes);
		}
		if (State == StateFailed) {
			setIcon(no);
		}
		if (State == StateDisabled) {
			setEnabled(false);
		}
		else {
			setEnabled(true);
		}
	}

	public int getState() {
		return State;
	}

}
