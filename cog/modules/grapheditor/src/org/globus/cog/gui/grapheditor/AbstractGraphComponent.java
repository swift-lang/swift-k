// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.ClassProperty;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.properties.PropertyHolder;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;
import org.globus.cog.gui.grapheditor.util.EventDispatchHelper;

/**
 * Implements the basic functionality for a graph component
 */
public abstract class AbstractGraphComponent implements GraphComponent, PropertyHolder {

	private static Logger logger = Logger.getLogger(AbstractGraphComponent.class);

	private String componentType;

	private HashMap instanceRenderers;

	private NodeComponent parent;

	private List properties;

	private List propertyChangeListeners;

	private List actions;

	private String _ID;

	private static int idCounter = 0;

	private static HashMap classProperties;

	static {
		classProperties = new HashMap();
		setClassRendererClass(AbstractGraphComponent.class, NullRenderer.class);
	}

	public AbstractGraphComponent() {
		_ID = String.valueOf(idCounter++);
	}

	public boolean supportsType(String type) {
		return getComponentType().equals(type);
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String name) {
		componentType = name;
	}

	public synchronized void setInstanceRendererClass(Class cls, String target) {
		if (instanceRenderers == null) {
			instanceRenderers = new HashMap();
		}
		instanceRenderers.put(new ClassTargetPair(getClass(), target), cls);
	}

	public void setInstanceRendererClass(Class cls) {
		setInstanceRendererClass(cls, RendererFactory.getDefaultTarget());
	}

	protected void setClassRendererClass(Class cls) {
		setClassRendererClass(getClass(), cls);
	}

	protected static void setClassRendererClass(Class componentClass, Class cls) {
		RendererFactory.addClassRenderer(componentClass, cls);
	}

	protected void setClassRendererClass(Class cls, String target) {
		setClassRendererClass(getClass(), cls, target);
	}

	protected static void setClassRendererClass(Class componentClass, Class cls, String target) {
		RendererFactory.addClassRenderer(componentClass, target, cls);
	}

	public ComponentRenderer newRenderer() {
		return newRenderer(RendererFactory.getCurrentTarget());
	}

	private synchronized Class getInstanceRendererClass(String target) {
		if (instanceRenderers == null) {
			return null;
		}
		else {
			return (Class) instanceRenderers.get(new ClassTargetPair(getClass(), target));
		}
	}

	private Class getClassRendererClass(String target) {
		Class cls = getClass();
		while (cls != Object.class) {
			Class rendererClass = RendererFactory.getClassRenderer(cls, target);
			if (rendererClass != null) {
				return rendererClass;
			}
			cls = cls.getSuperclass();
		}
		return null;
	}

	public ComponentRenderer newRenderer(String target) {
		try {
			Class rendererClass = getInstanceRendererClass(target);
			if (rendererClass == null) {
				rendererClass = getClassRendererClass(target);
			}
			if (rendererClass != null) {
				ComponentRenderer cr = (ComponentRenderer) rendererClass.newInstance();
				cr.setComponent(this);
				return cr;
			}
			else {
				throw new NoSuchRendererException(getClass() + " - " + target);
			}
		}
		catch (InstantiationException e) {
		}
		catch (IllegalAccessException e) {
		}
		catch (NullPointerException e) {
		}
		return null;
	}

	public GraphComponent newInstance() {
		try {
			return (GraphComponent) getClass().newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public NodeComponent getParent() {
		return parent;
	}

	public void setParent(NodeComponent parent) {
		this.parent = parent;
	}

	public void firePropertyChange(String property, Object oldValue, Object newValue) {
		PropertyChangeEvent e = new PropertyChangeEvent(this, property, oldValue, newValue);
		firePropertyChange(e);
	}

	public void firePropertyChange(String property) {
		firePropertyChange(property, null, null);
	}

	public void addProperty(Property property) {
		synchronized (this) {
			if (properties == null) {
				properties = new ConservativeArrayList(1);
			}
		}
		synchronized (properties) {
			if (logger.isDebugEnabled()) {
				if (hasClassProperty(this.getClass(), property.getName())) {
					logger.warn("Overridig existing class property " + property.getName() + " for "
							+ this.getClass(), new Throwable());
				}
				if (hasProperty(property.getName())) {
					logger.warn("Overridig existing property " + property.getName() + " for "
							+ this, new Throwable());
				}
			}
			properties.add(property);
		}
	}

	protected synchronized static void addClassProperty(ClassProperty prop) {
		String propName = prop.getName();
		HashMap classes = (HashMap) classProperties.get(propName);
		if (classes == null) {
			classes = new HashMap();
			classProperties.put(propName, classes);
		}
		else {
			if (hasClassProperty(classes, prop.getOwnerClass(), propName)) {
				return;
			}
		}
		classes.put(prop.getOwnerClass(), prop);
	}

	protected synchronized static ClassProperty getClassProperty2(Class cls, String name) {
		HashMap classes = (HashMap) classProperties.get(name);
		if (classes != null) {
			ClassProperty cached = (ClassProperty) classes.get(cls);
			if (cached != null) {
				return cached;
			}
			Iterator i = classes.keySet().iterator();
			while (i.hasNext()) {
				Class cls2 = (Class) i.next();
				if (cls2.isAssignableFrom(cls)) {
					ClassProperty prop = (ClassProperty) classes.get(cls2);
					classes.put(cls, prop);
					return prop;
				}
			}
		}
		return null;
	}

	protected static ClassProperty getClassProperty(Class cls, String name) {
		ClassProperty prop = getClassProperty2(cls, name);
		if (prop == null) {
			throw new RuntimeException("Invalid property (" + name + ") for class " + cls.getName());
		}
		else {
			return prop;
		}
	}

	protected static boolean hasClassProperty(Class cls, String name) {
		if (classProperties.containsKey(name)) {
			HashMap classes = (HashMap) classProperties.get(name);
			return hasClassProperty(classes, cls, name);
		}
		return false;
	}

	public static boolean hasClassProperty(HashMap classes, Class cls, String name) {
		if (classes.containsKey(cls)) {
			return true;
		}
		Iterator i = classes.keySet().iterator();
		while (i.hasNext()) {
			Class cls2 = (Class) i.next();
			if (cls2.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}

	public void removeProperty(Property property) {
		synchronized (properties) {
			properties.remove(property);
		}
	}

	public void removeProperty(String name) {
		if (hasProperty(name)) {
			removeProperty(getProperty(name));
		}
	}

	public Property getProperty(String name) {
		ClassProperty prop = getClassProperty2(getClass(), name);
		if (prop != null) {
			Property prop2 = prop.getInstance(this);
			return prop2;
		}
		else {
			Property p = _getP(name);
			if (p == null) {
				throw new RuntimeException(this.getClass().getName() + " has no property named '"
						+ name + "'");
			}
			else {
				return p;
			}
		}
	}

	protected Property getProperty2(String name) {
		ClassProperty prop = getClassProperty2(getClass(), name);
		if (prop != null) {
			Property prop2 = prop.getInstance(this);
			return prop2;
		}
		else {
			return _getP(name);
		}
	}

	private Property _getP(String name) {
		if (name == null) {
			return null;
		}
		if (properties == null) {
			return null;
		}
		synchronized (properties) {
			for (int i = 0; i < properties.size(); i++) {
				if (((Property) properties.get(i)).getName().equals(name)) {
					return (Property) properties.get(i);
				}
			}
		}
		return null;
	}

	private boolean _containsP(String name) {
		if (name == null) {
			return false;
		}
		if (properties == null) {
			return false;
		}
		synchronized (properties) {
			for (int i = 0; i < properties.size(); i++) {
				if (((Property) properties.get(i)).getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasProperty(String name) {
		return hasClassProperty(getClass(), name) || _containsP(name);
	}

	public Object getPropertyValue(String name) {
		Property prop = getProperty2(name);
		if (prop != null) {
			return prop.getValue();
		}
		else {
			return null;
		}
	}

	public void setPropertyValue(String name, Object value) {
		Property prop = getProperty2(name);
		if (prop == null) {
			prop = new OverlayedProperty(this, name, Property.RWH);
			addProperty(prop);
		}
		prop.setValue(value);
	}

	public Collection getProperties() {
		LinkedList props = new LinkedList();
		props.addAll(getClassProperties(this));
		if (properties != null) {
			props.addAll(properties);
		}
		return props;
	}

	public Collection getClassProperties(GraphComponent owner) {
		LinkedList props = new LinkedList();
		Iterator i = classProperties.keySet().iterator();
		while (i.hasNext()) {
			String propName = (String) i.next();
			HashMap classes = (HashMap) classProperties.get(propName);
			Iterator j = classes.keySet().iterator();
			while (j.hasNext()) {
				Class cls = (Class) j.next();
				if (cls.isAssignableFrom(owner.getClass())) {
					ClassProperty prop = (ClassProperty) classes.get(cls);
					Property prop2 = prop.getInstance(this);
					props.add(prop2);
					break;
				}
			}
		}
		return props;
	}

	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (propertyChangeListeners == null) {
			propertyChangeListeners = new ConservativeArrayList(1);
		}
		if (!propertyChangeListeners.contains(l)) {
			propertyChangeListeners.add(l);
		}
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
		if (propertyChangeListeners != null) {
			propertyChangeListeners.remove(l);
		}
	}

	public void firePropertyChange(PropertyChangeEvent e) {
		EventDispatchHelper.firePropertyChangeEvent(propertyChangeListeners, e);
	}

	public String get_ID() {
		return _ID;
	}

	public void set_ID(String _id) {
		this._ID = _id;
	}

	public NodeComponent getRootNode() {
		if (getParent() != null) {
			return getParent().getRootNode();
		}
		else {
			return (NodeComponent) this;
		}
	}

	public Object clone() {
		GraphComponent clone = newInstance();
		Iterator props = getProperties().iterator();
		while (props.hasNext()) {
			Property prop = (Property) props.next();
			clone.setPropertyValue(prop.getName(), prop.getValue());
		}
		return clone;
	}
}
