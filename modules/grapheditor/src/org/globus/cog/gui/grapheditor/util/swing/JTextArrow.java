
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 27, 2003
 */
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Color;
import java.awt.Rectangle;

import org.globus.cog.gui.grapheditor.util.RectUtil;

public class JTextArrow extends JArrow {

	public JTextArrow() {
		setArrow(new TextArrow(0, 0, 32, 32, (short) 1, (short) 6, (short) 6, null));
		setColor(Color.BLACK);
	}

	public String getText() {
		return getTextArrow().getText();
	}

	public void setText(String text) {
		getTextArrow().setText(text);
		repaint();
	}

	public TextArrow getTextArrow() {
		return (TextArrow) getArrow();
	}
	
	public Rectangle getBoundingBox() {
		return RectUtil.border(super.getBoundingBox(), 5);
	}
}
