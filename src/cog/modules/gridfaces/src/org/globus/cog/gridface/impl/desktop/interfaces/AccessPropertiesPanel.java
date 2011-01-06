//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Aug 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.gridface.impl.desktop.interfaces;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

public interface AccessPropertiesPanel {
	public static final String sOK_button = "OK";
	public static final String sCancel_button = "Cancel";
	
	public void showIconProperties();
	
	public JComponent getPropertiesPanel();
	
	public void processPropertiesCancelButton();
	public void processPropertiesOKButton();
	
	public JInternalFrame getPropertiesFrame();
}
