
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
/*
 * Created on Aug 18, 2003
 */
package org.globus.cog.gui.grapheditor.util.swing;

import javax.swing.JLayeredPane;



public class FastContainer extends JLayeredPane {
	public void validate(){
		
	}
	
	public void reallyValidate(){
		super.validate();
	}
	
	public void repaint() {
		super.paintImmediately(getBounds());
	}
}
