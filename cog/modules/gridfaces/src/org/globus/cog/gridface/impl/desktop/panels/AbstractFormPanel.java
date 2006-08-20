//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.panels;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;

import org.globus.cog.gridface.impl.desktop.interfaces.FormPanel;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;

public abstract class AbstractFormPanel extends JComponent implements FormPanel {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractFormPanel.class.getName());

	JScrollPane panelScroll = null;
	JPanel panel = null;

	/** Default Object from which form data is loaded and exported from */
	Object dataObject = null;

	// Using arraylist instead of hash to maintain insert order
	ArrayList keys = new ArrayList();
	ArrayList values = new ArrayList();

	protected int formMode;

	protected String getPrefix = null;
	protected String setPrefix = null;
	// Used to remove attributes during HASHMODE
	protected String removePrefix = null;

	public AbstractFormPanel() {
		this(null);
	}

	public AbstractFormPanel(String panelTitle) {
		this(panelTitle, SETTERGETTERMODE, null, null);
	}

	public AbstractFormPanel(String panelTitle, int mode, String getterPrefix, String setterPrefix) {
		this(panelTitle, mode, getterPrefix, setterPrefix, null);
	}

	public AbstractFormPanel(String panelTitle, int mode, String getterPrefix, String setterPrefix,
			String removePrefix) {
		this.formMode = mode;
		// this.panel = new JPanel();
		this.panelScroll = new JScrollPane(this);

		switch (mode) {
			case HASHMODE:
				this.getPrefix = ((getterPrefix == null) ? "getAttribute" : getterPrefix);
				this.setPrefix = ((setterPrefix == null) ? "setAttribute" : setterPrefix);
				this.removePrefix = ((removePrefix == null) ? "removeAttribute" : removePrefix);
				break;
			case SETTERGETTERMODE:
				this.getPrefix = ((getterPrefix == null) ? "get" : getterPrefix);
				this.setPrefix = ((setterPrefix == null) ? "set" : setterPrefix);
				break;
			default:
				break;
		}
		setBorder(BorderFactory.createTitledBorder(panelTitle));
		setLayout(new SpringLayout());
	}

	public final void clear() {
		removeAll();
	}

	protected Component getNewComponentForObject(Object hashValue) {
		if (hashValue instanceof Collection) {
			return new JComboBox(new Vector((Collection) hashValue));
		}
		else if (hashValue instanceof String) {
			String stringVal = (String) hashValue;
			// This will be considered as a check box item
			if (stringVal.equalsIgnoreCase("true") || stringVal.equalsIgnoreCase("false")) {
				// return getNewComponentForObject(new Boolean(stringVal));
				return getNewComponentForObject(Boolean.valueOf(stringVal));
			}
			// Else its just a text component
			else {
				// If String value starts with "uneditablePREFIX"
				// then return a JLabel
				if (stringVal.startsWith(uneditablePREFIX)) {
					return new JLabel(stringVal.substring(uneditablePREFIX.length()));
				}
				// else return an editable JTextField
				else {
					return new JTextField(stringVal);
				}
			}
		}
		else if (hashValue instanceof Boolean) {
			return new JCheckBox("", ((Boolean) hashValue).booleanValue());
		}
		else if (hashValue == null) {
			return new JTextField(null);
		}
		return null;
	}

	protected String getValueForComponent(Component dispComp) {

		if (dispComp instanceof JComboBox) {
			return (String) ((JComboBox) dispComp).getSelectedItem();
		}
		else if (dispComp instanceof JTextComponent) {
			return (((JTextComponent) dispComp).getText()).equals("") ? null
					: (((JTextComponent) dispComp).getText());
		}
		else if (dispComp instanceof JCheckBox) {
			return Boolean.valueOf(((JCheckBox) dispComp).isSelected()).toString();
		}
		else if (dispComp instanceof JScrollPane) {
			// support for 1 component contained in scrollpane
			if (((JScrollPane) dispComp).getComponentCount() == 1) {
				return getValueForComponent(((JScrollPane) dispComp).getComponent(0));
			}
		}
		else if (dispComp instanceof JLabel) {
			return uneditablePREFIX + (String) ((JLabel) dispComp).getText();
		}
		return null;
	}

	protected String getValue(String attName) {
		return getValueForComponent(getComponentForKey(attName));
	}

	protected Component getComponentForKey(String attName) {
		return (Component) values.get(keys.indexOf(attName));
	}

	protected boolean isValidSetterMethod(Method method, Class[] parameterType) {
		int acceptedParameterSize = parameterType.length;

		boolean prefixCheck = method.getName().startsWith(setPrefix);
		boolean isInKeys;
		// only SETTERGETTERMODE methods have key value embedded in them
		// example: setProvider(String)
		if (this.formMode == SETTERGETTERMODE) {
			isInKeys = keys.contains(method.getName().substring(setPrefix.length()));
		}
		// HASHMODE only uses the setPrefix , getPrefix for method names
		// example: setAttribute(String,Object)
		else {
			isInKeys = true;
		}
		boolean correctParameterLength = method.getParameterTypes().length == acceptedParameterSize;
		boolean correctParameterType = false;

		if (correctParameterLength) {
			for (int i = 0; i < acceptedParameterSize; i++) {
				if (method.getParameterTypes()[i].getName().equals(parameterType[i].getName())) {
					correctParameterType = true;
				}
			}

		}

		return prefixCheck && isInKeys && correctParameterLength && correctParameterType;
	}

	protected boolean isValidGetterMethod(Method method) {
		boolean prefixCheck = method.getName().startsWith(getPrefix);
		boolean isInKeys;
		// only SETTERGETTERMODE methods have key value embedded in them
		// example: getProvider()
		if (this.formMode == SETTERGETTERMODE) {
			isInKeys = keys.contains(method.getName().substring(getPrefix.length()));
		}
		// HASHMODE only uses the setPrefix , getPrefix for method names
		// example: getAttribute(String)
		else {
			isInKeys = true;
		}

		// boolean correctReturnType =
		// (method.getReturnType().getName().equals(returnType.getName()));

		return prefixCheck && isInKeys;
	}

	public void load(ArrayList newKeys, Object origObject) {
		if (newKeys == null) {
			newKeys = keys;
		}
		this.keys = newKeys;
		this.values = new ArrayList(newKeys.size());
		this.dataObject = origObject;

		try {
			for (int i = 0; i < newKeys.size(); i++) {
				Method method = null;
				switch (this.formMode) {
					case SETTERGETTERMODE:
						// example: get method from object class getKeyValue
						method = origObject.getClass().getMethod(getPrefix + newKeys.get(i),
								(Class[]) null);
						if (isValidGetterMethod(method)) {
							values.add(i, getNewComponentForObject(method.invoke(origObject,
									(Object[]) null)));
						}

						break;
					case HASHMODE:
						method = origObject.getClass().getMethod(getPrefix,
								new Class[] { String.class });
						Object[] objArgs = new Object[] { newKeys.get(i).toString().toLowerCase(), };

						if (isValidGetterMethod(method)) {
							values.add(i, getNewComponentForObject(method.invoke(origObject,
									objArgs)));
						}
						break;
					default:
						break;
				}

			}
			// render the form after loading
			display();

		}
		catch (NoSuchMethodException ne) {
			ne.printStackTrace();
		}
		catch (IllegalAccessException ilE) {
			ilE.printStackTrace();
		}
		catch (InvocationTargetException inE) {
			inE.printStackTrace();
		}

	}

	public void display() {
		clear();
		for (int i = 0; i < keys.size(); i++) {
			String labelName = (String) keys.get(i);
			JLabel l = new JLabel(labelName, JLabel.TRAILING);
			add(l);
			Component labelFor = (Component) values.get(i);
			if (labelFor != null) {
				add(labelFor);
				l.setLabelFor(labelFor);

			}

		}
		// Lay out the panel.
		DesktopUtilities.makeCompactGrid(this, keys.size(), 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad

	}

	public void export() {
		export(this.dataObject);
	}

	public void export(Object updateObject) {
		logger.debug("Exporting Form Panel in mode: " + this.formMode);
		try {
			if (this.formMode == SETTERGETTERMODE) {
				Method[] methods = updateObject.getClass().getMethods();
				Class[] methodParamTypes = null;
				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					// Check to see if setter method only takes String.class as
					// parameter
					if (isValidSetterMethod(method, new Class[] { String.class })) {
						// example: getting key attribute "Provider" from method
						// getProvider()
						String keyValue = getValue(method.getName().substring(
								this.setPrefix.length()));
						logger.debug(method.getName() + " : " + keyValue);
						method.invoke(updateObject, new Object[] { keyValue });
					}

				}
			}
			else if (this.formMode == HASHMODE) {
				logger.debug(updateObject.getClass() + ":" + this.setPrefix);
				// only accept setAttribute(String s,Object o) parameters
				Method method = updateObject.getClass().getMethod(this.setPrefix,
						new Class[] { String.class, Object.class });
				for (int i = 0; i < keys.size(); i++) {
					String value = getValue((String) keys.get(i));

					logger.debug(this.setPrefix + ":" + keys.get(i) + "=" + value);
					if (value != null) {
						method.invoke(updateObject, new Object[] { keys.get(i), value });
					}
					else {
						Method methodRemove = updateObject.getClass().getMethod(this.removePrefix,
								new Class[] { String.class });
						methodRemove.invoke(updateObject, new Object[] { keys.get(i) });
					}
				}
			}

		}
		catch (IllegalAccessException ilE) {
			ilE.printStackTrace();
		}
		catch (InvocationTargetException inE) {
			inE.printStackTrace();
		}
		catch (NoSuchMethodException ne) {
			ne.printStackTrace();
		}
	}

	public JScrollPane getScrollContainer() {
		return this.panelScroll;
	}

	public JPanel getPanel() {
		return this.panel;
	}
}
