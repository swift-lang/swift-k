
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.util;

import java.awt.Dimension;

import javax.swing.JComponent;

public class GridContainer extends JComponent {
	private Dimension myPreferredSize;

	public GridContainer() {
		super();
		myPreferredSize = null;
	}
	public GridContainer(int rows, int cols) {
		this();
		setLayout(new SimpleGridLayout(rows, cols));
	}

	public void setGridSize(int rows, int cols) {
		setLayout(new SimpleGridLayout(rows, cols));
		
	}

	public void setPreferredSize(Dimension PS) {
		myPreferredSize = PS;
	}

	public void setPreferredSize(int width, int height) {
		setPreferredSize(new Dimension(width, height));
	}

	public Dimension getPreferredSize() {
		return myPreferredSize;
	}
}
