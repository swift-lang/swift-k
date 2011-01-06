//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.interfaces;

import org.globus.cog.gridface.impl.desktop.util.AttributesHolder;

/*
 * Basic interface for DesktopIcon
 */
public interface DesktopIcon 
	extends AccessPopup, AccessIconProperties,
	AccessActionProxy,ExecutesLaunch,AccessPropertiesPanel,AccessPreferences {
		
	public static final String sNEWICON = "New Icon";
	public static final String sDELETEICON = "Delete Icon";
	public static final String sPROPERTIES = "Properties";
	
	//Icon attributes holder
	public AttributesHolder getAttributesHolder();
	
	//Checked during drag-n-drop import
	public boolean canImportTypePair(String type1,String type2);
	
	//DesktopIcon related constants
	public static final int ICONTEXT_WIDTH = 50;
	public static final int ICONTEXT_HEIGHT = 30;
	
	//Indicates "find" a default location when adding icon
	public static final int DEFAULT_LOCATION = -1;
	
	// Icon image URI to specify NATIVE unknown location
	//TESTING
	//public final static String NATIVEURI = FormPanel.uneditablePREFIX+"NATIVE_URI";
	public final static String NATIVEURI = "NATIVE_URI";
}
