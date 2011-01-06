//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.icons;

/*
 * Grouping icons together
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.globus.cog.gridface.impl.desktop.interfaces.DesktopIcon;

public class DesktopIconGroup extends Vector  {

	public Collection getIconTypes(){
		Collection iconTypes = new ArrayList();
		for (Iterator iter = iterator(); iter.hasNext();) {
			DesktopIcon element = (DesktopIcon) iter.next();
			iconTypes.add(element.getIconType());
		}
		return iconTypes;
	}
	public boolean areAllIconsOfType(String iconType){
		for (Iterator iter = getIconTypes().iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			if(!type.equals(iconType)){
				return false;
			}
		}
		return true;
	}
	public boolean containsIconType(String iconType){
		for (Iterator iter = getIconTypes().iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			if(type.equals(iconType)){
				return true;
			}
		}
		return false;
	}
	
	public void sortByType(){
		//TODO
		Collections.sort(this);
		
	}
}
