
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
    
package org.globus.cog.gui.setup.components;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.globus.cog.gui.setup.events.ComponentStatusChangedEvent;
import org.globus.cog.gui.setup.events.ComponentStatusChangedListener;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.cog.util.TextFileLoader;

/**
 *  Implementation of the basic methods for a SetupComponent
 */
public abstract class AbstractSetupComponent extends GridContainer implements SetupComponent {

	private String title;
	private LinkedList dependencies;
	private boolean completed;
	private JTextArea description;
	private Component label;
	private String errorMessage = null;

	/**
	 *  Constructor for the AbstractSetupComponent object
	 *
	 *@param  title     The title of the component
	 *@param  descPath  The path to the main text
	 */
	public AbstractSetupComponent(String title, String descPath) {
		super(3, 1);

		this.title = title;
		this.setVisible(false);

		completed = false;
		dependencies = new LinkedList();

		//Rows: 0 - User stuff
		//		1 - Description
		//		2 - User stuff
		setLayout(new SimpleGridLayout(3, 1));

		if (descPath != null) {
			TextFileLoader tfl = new TextFileLoader();
			String desc = tfl.loadFromResource(descPath);

			description = new JTextArea(desc);
			description.setEditable(false);
			description.setLineWrap(true);
			description.setWrapStyleWord(true);

			JScrollPane textSP = new JScrollPane(description);

			textSP.setPreferredSize(new Dimension(SimpleGridLayout.Expand, SimpleGridLayout.Expand));

			add(textSP, new GridPosition(1, 0));
			//add(new InstructionsPanel(Desc), new GridPosition(1,0));
		}
	}

	/**
	 *  Adds a dependency for this component. The container should check the
	 *  dependencies and enable this component when the verify methods of the
	 *  dependencies are all <code>true</code>
	 *
	 *@param  SC  The dependency to be added
	 */
	public void addDependency(SetupComponent SC) {
		dependencies.add(SC);
	}

	/**
	 *  Called to activate this component
	 */
	public void enter() {
		setVisible(true);
	}

	/**
	 *  Called before de-activating this component If this method fails (by
	 *  returning <code>false</code>) the parent container should not continue the
	 *  activation of the next component.
	 *
	 *@return    <code>true</code> if the de-activation is confirmed
	 */
	public boolean leave() {

		if (!verify()) {
			boolean confirm = confirmBogusSettings();

			if (confirm) {
				setVisible(false);
				completed = true;
				return true;
			}
			else {
				return false;
			}
		}
		setVisible(false);
		completed = true;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#getVisualComponent()
	 */
	public Object getVisualComponent() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#addComponentStatusChangedListener(org.globus.cog.gui.setup.events.ComponentStatusChangedListener)
	 */
	public void addComponentStatusChangedListener(ComponentStatusChangedListener CSCL) {
		listenerList.add(ComponentStatusChangedListener.class, CSCL);
	}

	protected void fireComponentStatusChangedEvent() {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ComponentStatusChangedListener.class) {
				((ComponentStatusChangedListener) listeners[i + 1]).componentStatusChanged(
					new ComponentStatusChangedEvent(this));
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#getDependencies()
	 */
	public LinkedList getDependencies() {
		return dependencies;
	}

	/**
	 *  Completed means that it can have a passed/failed status
	 *
	 *@return    Description of the Return Value
	 */
	public boolean completed() {
		return completed;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#setLabel(java.awt.Component)
	 */
	public void setLabel(Component label) {
		this.label = label;
	}

	/**
	 *  Verifies if this component allows the termination of the setup
	 *
	 *@return    Description of the Return Value
	 */
	public boolean canFinish() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gui.setup.components.SetupComponent#getLabel()
	 */
	public Component getLabel() {
		return label;
	}

	/**
	 *  Displays a dialog allowing the user to confirm settings that appear not to
	 *  be correct
	 *
	 *@return    Description of the Return Value
	 */
	public boolean confirmBogusSettings() {
		if (errorMessage == null) {
			return true;
		}

		int ret =
			JOptionPane.showConfirmDialog(
				null,
				errorMessage + "\nAre you sure you want to continue?",
				"Warning!",
				JOptionPane.YES_NO_OPTION);

		if (ret == JOptionPane.YES_OPTION) {
			return true;
		}
		return false;
	}

	/**
	 *  Displays an error message. This method is used for components that cannot
	 *  be skipped without the correct settings
	 */
	public void displayErrorMessage() {
		JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 *  Sets the error message to be (eventually) later used by
	 *  confirmBogusSettings() or displayErrorMessage()
	 *
	 *@param  Message  The new errorMessage value
	 */
	public void setErrorMessage(String Message) {
		errorMessage = Message;
	}

	public boolean verify() {
		Iterator deps = getDependencies().listIterator();
		while (deps.hasNext()) {
			SetupComponent dep = (SetupComponent) deps.next();
			if (!dep.verify()) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  Called after the user pressed the finish button
	 *
	 *@return    Description of the Return Value
	 */
	public boolean finish() {
		if (!canFinish()) {
			return false;
		}
		return verify();
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean b) {
		completed = b;
	}

}
