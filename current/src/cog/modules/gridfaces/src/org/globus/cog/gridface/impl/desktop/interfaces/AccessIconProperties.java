//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
/*
 * Created on Jul 21, 2004
 */
package org.globus.cog.gridface.impl.desktop.interfaces;

import java.awt.Dimension;

import javax.swing.ImageIcon;

import org.globus.cog.gridface.impl.desktop.util.ObjectPair;

public interface AccessIconProperties {
	public void remove();
	public Dimension getDimension();
	
	public String getAppClass();
	public void setAppClass(String myAppClass) ;
	
	public ObjectPair getAppClassArgsObject();
	public void setAppClassArgs(String args);
	public String getAppClassArgs();
	public void setAppClassArgsObject(ObjectPair args);
	
	//Returns the String "true" if application class is supposed to get
	//instantiated through the main method instead of its constructor
	//NOTE: Using String because it will be easier to interact with
	//using the Abstract Form Panel framework..
	public String getUseMainMethod();
	public void setUseMainMethod(String sBoolean);
	
	//Icon types
	public String getIconType();
	public void setIconType(String iconType);

	//Icon image source
	public String getIconImageURI();
	public void setIconImageURI(String imageURI);
	public void setIconImage(ImageIcon iconImage);
	
	//Icon text
	public void setIconText(String text);
	public String getIconText();
	
	public String getDefaultIconImageURI();
	
	public String getDefaultIconText();
	
	//Get unique icon id
	public int getId();
	
	//Icon selection
	public boolean isSelected();
	public void setSelected(boolean selection);
}
