
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.util.beans;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

import org.globus.cog.util.ImageLoader;

/**
 * Base BeanInfo class for CoG beans that simplifies the definition of
 * icons
 */
public class CoGBeanInfo extends SimpleBeanInfo{
	private Image bw16, co16, bw32, co32;
	
	
	public CoGBeanInfo(String iconName){
		ImageLoader il = new ImageLoader();
		bw16 = il.loadImage("images/16x16/bw/"+iconName).getImage();
		co16 = il.loadImage("images/16x16/co/"+iconName).getImage();
		bw32 = il.loadImage("images/32x32/bw/"+iconName).getImage();
		co32 = il.loadImage("images/32x32/co/"+iconName).getImage();
	}
	
	public Image getIcon(int type){
		if (type == ICON_MONO_16x16){
			return bw16;
		}
		if (type == ICON_COLOR_16x16){
			return co16;
		}
		if (type == ICON_MONO_32x32){
			return bw32;
		}
		if (type == ICON_COLOR_32x32){
			return co32;
		}
		return null;
	}
}
