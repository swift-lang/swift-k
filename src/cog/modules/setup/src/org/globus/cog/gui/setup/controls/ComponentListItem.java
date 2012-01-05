
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.controls;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;

import org.globus.cog.gui.setup.util.ButtonWithState;

/**
 *  Class describing the items in the list on the left, and their possible state
 */
public class ComponentListItem extends ButtonWithState {

	public ComponentListItem(String title) {
		super(title);

		setPreferredSize(new Dimension(140, 20));
		setHorizontalAlignment(SwingConstants.LEFT);

		setBackground(new Color(162, 162, 200));
		setBorder(BorderFactory.createEmptyBorder());
	}

	public void setActive(boolean active) {
		if (active) {
			setBackground(new Color(192, 192, 230));
		}
		else {
			setBackground(new Color(162, 162, 200));
		}
	}
}
