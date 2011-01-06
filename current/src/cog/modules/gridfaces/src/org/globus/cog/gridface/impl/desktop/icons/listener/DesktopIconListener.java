//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.icons.listener;
/*
 * Listener for all mouse events on DesktopIcon
 */

//Local imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.globus.cog.gridface.impl.desktop.AbstractDesktop;
import org.globus.cog.gridface.impl.desktop.icons.AbstractIcon;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessActionProxy;
import org.globus.cog.gridface.impl.desktop.interfaces.CoGTop;
import org.globus.cog.gridface.impl.desktop.interfaces.DesktopToolBar;
import org.globus.cog.gridface.impl.desktop.interfaces.MouseActionProxy;

public class DesktopIconListener
	implements MouseListener, MouseMotionListener {

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(MouseEvent e) {
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		
		if(icon.getParent() instanceof CoGTop){
			boolean isControlDown = e.isControlDown();
			//selection state of icon on which mouse was pressed
			boolean isCurIconSelected = icon.isSelected();

			icon.xPressLoc = e.getX();
			icon.yPressLoc = e.getY();

			//---Left mouse button pressed
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (!isControlDown) {
					icon.getDesktop().deselectAllIcons();
					icon.setSelected(true);
				} else {
					//inverse selection of icon mouse was pressed on
					icon.setSelected(!icon.isSelected());
				}
				//If the icon on mouse pressed on was not already selected then
				//only save icon selection for multiple icon drag
				if (!isCurIconSelected) {
					//Save a copy of selected icons before mouse press on a icon
					//preparations for multiple icon drag.
					//preDragSelecIcons = icon.getDesktop().getSelectedIcons();
					icon.getDesktop().captureIconSelection();
				}
			}
		} else if(icon.getParent() instanceof DesktopToolBar){
				icon.setSelected(!icon.isSelected());
		}
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed
	 * and released) on a component.
	 */
	public void mouseClicked(MouseEvent e) {
//		Logger desktopLogger = LoggerImpl.getInstance(AbstractDesktop.sDefaultLoggerName);
		
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		if(icon instanceof AccessActionProxy){
			MouseActionProxy actionProxy = ((AccessActionProxy)icon).getMouseActionProxy();
			if(actionProxy != null){
				//According to where the icon is, perform actions
				
				//Icon is on desktop
				if(icon.getParent() instanceof AbstractDesktop){
					//Double clicked
					actionProxy.mouseClicked(icon, e.getClickCount(),SwingUtilities.isLeftMouseButton(e),e.getX(), e.getY());
				}
				//Icon is on toolbar
				else if(icon.getParent() instanceof DesktopToolBar){
					//Single click invokes icon
					actionProxy.mouseClicked(icon, 2,SwingUtilities.isLeftMouseButton(e),e.getX(), e.getY());
					
				}
			}
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		
		if(icon.getParent() instanceof AbstractDesktop){
			if (SwingUtilities.isLeftMouseButton(e)) {
				TransferHandler th = icon.getTransferHandler();
				//Before start drag we must set selected icons as they were before
				//mouse drag event
				icon.getDesktop().releaseIconSelection();

				//INITIATE DRAG
				th.exportAsDrag(icon, e, TransferHandler.MOVE);
			}		
		}else if(icon.getParent() instanceof DesktopToolBar){
			if (SwingUtilities.isLeftMouseButton(e)) {
				TransferHandler th = icon.getTransferHandler();
				//INITIATE DRAG
				th.exportAsDrag(icon, e, TransferHandler.MOVE);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		if(icon.getParent() instanceof DesktopToolBar){
			icon.setSelected(false);
		}
	}
	public void mouseEntered(MouseEvent e) {
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		if (icon.getParent() instanceof DesktopToolBar) {
			icon.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLUE));
		}
	}
	public void mouseExited(MouseEvent e) {
		AbstractIcon icon = ((AbstractIcon) e.getSource());
		if (icon.getParent() instanceof DesktopToolBar) {
			icon.setBorder(BorderFactory.createLineBorder(icon.getParent().getBackground()));
		}
	}
	public void mouseMoved(MouseEvent e) {
	}
}
