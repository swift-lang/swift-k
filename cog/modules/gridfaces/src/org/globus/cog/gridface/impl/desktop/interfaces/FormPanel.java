//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.interfaces;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public interface FormPanel {
	/** uses the format methodName("key",object); where methodName
	 * is configurable and default is setAttribute, getAttribute
	 */
	public static final int HASHMODE = 0;
	/** uses the format getName , setName(String) to access
	 * key values for from data
	 */
	public static final int SETTERGETTERMODE = 1;
	
	/** Flag to mark String as uneditable */
	public static final String uneditablePREFIX = "#%#";
	public static final String UNDISPLAYABLE= uneditablePREFIX+"UNDISPLAYABLE";
	
	/**Place form elements in a container */
	public void display();

	public void clear();
	
	/** load keys in newKeys from orgiObject */
	public void load(ArrayList newKeys,Object origObject);
	/**Uses object used in load to export */
	public void export();
	/** Export panel data to updateObject */
	public void export(Object updateObject);
	
	public JScrollPane getScrollContainer();
	public JPanel getPanel();
}
