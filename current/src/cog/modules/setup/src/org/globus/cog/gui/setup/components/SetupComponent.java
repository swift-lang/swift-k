
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Component;
import java.util.LinkedList;

import org.globus.cog.gui.setup.events.ComponentStatusChangedListener;

public interface SetupComponent {
	/**
	 *  Called to activate this component
	 */
	public void enter();

	/**
	 *  Called before de-activating this component If this method fails (by
	 *  returning <code>false</code>) the parent container should not continue the
	 *  activation of the next component.
	 *
	 *@return    <code>true</code> if the de-activation is confirmed
	 */
	public boolean leave();

	/**
	 *  Called after the user pressed the finish button
	 *
	 *@return    Description of the Return Value
	 */
	public boolean finish();

	/**
	 * Returns the title of this component
	 * @return
	 */
	public String getTitle();

	/**
	 * Returns the visual (swing) component of this component
	 * @return
	 */
	public Object getVisualComponent();

	/**
	 * Adds a listener to be notified of status changes for this component
	 * @param CSCL
	 */
	public void addComponentStatusChangedListener(ComponentStatusChangedListener CSCL);

	/**
	 *  Adds a dependency for this component. The container should check the
	 *  dependencies and enable this component when the verify methods of the
	 *  dependencies are all <code>true</code>
	 *
	 *@param  Dep  The feature to be added to the Dependency attribute
	 */
	public void addDependency(SetupComponent Dep);

	/**
	 * Returns the dependencies of this component
	 * @return
	 */
	public LinkedList getDependencies();

	/**
	 *  Completed means that it can have a passed/failed state This is rather a
	 *  hint for the parent container
	 *
	 *@return    Description of the Return Value
	 */
	public boolean completed();

	/**
	 *  Verifies if the settings in this component appear to be correct
	 *
	 *@return    Description of the Return Value
	 */
	public boolean verify();

	/**
	 *  Verifies if this component allows the termination of the setup
	 *
	 *@return    Description of the Return Value
	 */
	public boolean canFinish();

	/**
	 * Sets the label of this component. A label is a Swing component
	 * providing a representation of this component.
	 * @param Label A Swing <code>Component</code> to be used as the label
	 */
	public void setLabel(Component Label);

	/**
	 * Returns this component's label
	 * @return
	 */
	public Component getLabel();
}
