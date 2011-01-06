//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
//Created on Sep 3, 2004

package org.globus.cog.gridface.impl.desktop.panels;


public class SimpleFormPanel extends AbstractFormPanel{

	public SimpleFormPanel() {
		this(null);
	}
	public SimpleFormPanel(String panelTitle){
		this(panelTitle,SETTERGETTERMODE,null,null);
	}
	public SimpleFormPanel(String panelTitle,int mode,String getterPrefix,String setterPrefix) {
		super(panelTitle,mode,getterPrefix,setterPrefix);
		display();
	}

}
