//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.interfaces;

import java.awt.Point;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;

public interface ImportDataActionProxy extends ActionProxy {
	public boolean importDataToComponent(JComponent dropComponent, Transferable t,JComponent dragComponent,Point dragPoint, Point dropPoint);
}
