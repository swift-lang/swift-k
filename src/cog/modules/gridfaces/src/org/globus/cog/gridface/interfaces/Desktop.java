//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.interfaces;

import java.awt.Dimension;

/*
 +--------------------------------+
 |                                |
 |                                |
 |            Desktop             |
 |                                |
 |                                |
 |                                |
 + --------------------------------+

 * Basic interface for Gridface Desktop with dimensions.
 *  
 */

public interface Desktop extends GridFace {
	//Static Strings Used for popup's and menu bar items
	public static final String sNEWDESKTOP = "New Desktop";
	public static final String sRENAMEDESKTOP = "Rename Desktop";
	
	public static final String sSAVEDESKTOP = "Save Desktop State";
	public static final String sLOADDESKTOP = "Load Desktop State";
	
	public static final String sADD_ICON = "Add icon";
	public static final String sARRANGEICONS = "Arrange Icons";
	
	public static final String sBGCOLOR = "Background Color";
	public static final String sABOUT = "About";

	public static final String sEXIT = "Exit";
	
	//Desktop dimensions
	public void setScreenSize(Dimension screenSize);
	public Dimension getScreenSize();
}
