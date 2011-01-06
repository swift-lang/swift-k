//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.globus.cog.gridface.impl.desktop.interfaces;

import java.util.prefs.Preferences;
/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
public interface AccessPreferences {
	public static final String SUFFIX_TAG = "#ATTRIB:";
	public static final String NO_TOSTRING = "NOT_A_STRING";
	
	public void savePreferences(Preferences startNode);
	public void loadPreferences(Preferences startNode) throws Exception;
}
