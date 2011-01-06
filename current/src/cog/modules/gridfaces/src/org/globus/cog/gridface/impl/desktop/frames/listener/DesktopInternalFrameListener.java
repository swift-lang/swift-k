//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.frames.listener;
/*
 * Listener for frame close veto and internal frame listener
 */

//Local imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.globus.cog.gridface.impl.desktop.frames.DesktopInternalFrameImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessClose;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;

public class DesktopInternalFrameListener
	implements InternalFrameListener, VetoableChangeListener {

	/**
	 * Invoked when a internal frame has been opened.
	 * @see javax.swing.JInternalFrame#show
	 */
	public void internalFrameOpened(InternalFrameEvent e) {

	}

	/**
	 * Invoked when an internal frame is in the process of being closed.
	 * The close operation can be overridden at this point.
	 * @see javax.swing.JInternalFrame#setDefaultCloseOperation
	 */
	public void internalFrameClosing(InternalFrameEvent e) {

	}

	/**
	 * Invoked when an internal frame has been closed.
	 * @see javax.swing.JInternalFrame#setClosed
	 */
	public void internalFrameClosed(InternalFrameEvent e) {
		DesktopInternalFrameImpl frame = (DesktopInternalFrameImpl) e.getInternalFrame();
		for(int i=0; i<frame.getContentPane().getComponentCount();i++){
			if(frame.getContentPane().getComponent(i) instanceof AccessClose){
				//desktopLogger.debug("calling AccessClose on :"+frame.getContentPane().getComponent(i).getClass().toString());
				AccessClose closeComponent = (AccessClose)frame.getContentPane().getComponent(i);
				closeComponent.close();
			}
		}
		
	}

	/**
	 * Invoked when an internal frame is iconified.
	 * @see javax.swing.JInternalFrame#setIcon
	 */
	public void internalFrameIconified(InternalFrameEvent e) {
	}

	/**
	 * Invoked when an internal frame is de-iconified.
	 * @see javax.swing.JInternalFrame#setIcon
	 */
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	/**
	 * Invoked when an internal frame is activated.
	 * @see javax.swing.JInternalFrame#setSelected
	 */
	public void internalFrameActivated(InternalFrameEvent e) {
	}

	/**
	 * Invoked when an internal frame is de-activated.
	 * @see javax.swing.JInternalFrame#setSelected
	 */
	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	/**
	 * This method gets called when a constrained property is changed.
	 *
	 * @param     evt a <code>PropertyChangeEvent</code> object describing the
	 *   	      event source and the property that has changed.
	 * @exception PropertyVetoException if the recipient wishes the property
	 *              change to be rolled back.
	 */
	public void vetoableChange(PropertyChangeEvent evt)
		throws PropertyVetoException {
		String propName = evt.getPropertyName();
		DesktopInternalFrameImpl internalFrame = (DesktopInternalFrameImpl) evt.getSource();

		if (propName.equals(JInternalFrame.IS_CLOSED_PROPERTY)& internalFrame.isSaveChanges()) {
			Boolean oldVal = (Boolean) evt.getOldValue();
			Boolean newVal = (Boolean) evt.getNewValue();
			if (oldVal == Boolean.FALSE && newVal == Boolean.TRUE) {
				int answer = DesktopUtilities.optionConfirmation((JInternalFrame) internalFrame,"Close "
				+ ((JInternalFrame) internalFrame).getTitle()
				+ "?","Close Confirmation",JOptionPane.YES_NO_OPTION);
				
				if (answer == JOptionPane.NO_OPTION) {
					throw new PropertyVetoException("exit cancelled", evt);
				}
				
			}
		}
	}

}
