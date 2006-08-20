/*
 * 
 */
package org.globus.cog.gridshell.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * An implementation of Scope
 * 
 */
public class ScopeImpl implements Scope {
	private static final Logger logger = Logger.getLogger(ScopeImpl.class);
	
	private static Scope systemScope;
	
	private PropertyChangeSupport pcListeners = new PropertyChangeSupport(this);
	
	private static final Scope.Mode DEFAULT_MODE = Scope.READ_WRITE;
	private Scope _super;
		
	private Map variableValues = new HashMap();
	private Map variableMode = new HashMap();
	
	/**
	 * Allows a superclass to be specified
	 * @param _super - the superclass
	 */
	public ScopeImpl(Scope _super) {
		this._super = _super;
	}
	/**
	 * Default Constructor
	 */
	public ScopeImpl() {
		this(null);
	}	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#getAvailableVariables()
	 */
	public Set getVariableNames() {
		synchronized(variableValues) {
			Set result = new TreeSet();
			result.addAll(variableValues.keySet());
			if(getSuper() != null) {
				result.addAll(getSuper().getVariableNames());
			}
			if(logger.isDebugEnabled()) {
				logger.debug("variableNames="+result);
			}
			return result;
		}
	}
	public Collection getValues() {
	    Collection result = new LinkedList();
	    result.addAll(variableValues.values());
	    if(getSuper()!=null) {
	        result.addAll(getSuper().getValues());
	    }
	    return result;
	}
	public Set getUniqueValues() {
	    Set result = new TreeSet();
	    result.addAll(getValues());
	    return result;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#getSuper()
	 */
	public Scope getSuper() {
		return _super;
	}	
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#getValue(java.lang.Object)
	 */
	public Object getValue(String name) {		
		synchronized(variableValues) {
			// can't use this.variableExists since 
			// looks up in the super class too
			if(variableValues.containsKey(name)) {
				return variableValues.get(name);
			}else if(getSuper() != null) {
				return getSuper().getValue(name);
			}else {
				return null;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#setVariableTo(java.lang.Object, java.lang.Object)
	 */
	public void setVariableTo(String name, Object value) throws ScopeException {
		synchronized(variableValues) {
			if(Scope.READ_ONLY.equals(getMode(name))) {
				String message = "Variable '"+name+"' is set to read only cannot set variable to '"+value+"'.";
				ScopeException exception = new ScopeException(message);
				logger.debug(message,exception);
				throw exception;
			}else if(value != null) {
				// set the value for the first time
				
				// fire a property change event
				pcListeners.firePropertyChange(name,null,value);			
				// set the value
			    variableValues.put(name,value);
			}else {
				// requesting to remove, but didn't exist
				logger.warn("Variable '"+name+"' can't be removed because it does not exist.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#variableExists(java.lang.Object)
	 */
	public boolean variableExists(String name) {
		synchronized(variableValues) { 
			return variableValues.containsKey(name) || 
			  (getSuper() != null && getSuper().variableExists(name));
		}
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcListener) {
		synchronized(pcListeners) {
			pcListeners.addPropertyChangeListener(pcListener);
		}
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String name, PropertyChangeListener pcListener) {
		synchronized(pcListeners) {
			pcListeners.addPropertyChangeListener(name,pcListener);
		}
	}
	/*  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcListener) {
		synchronized(pcListeners) {
			pcListeners.removePropertyChangeListener(pcListener);
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#isReadOnly(java.lang.String)
	 */
	public Mode getMode(String name) {
		synchronized(variableMode) {
			if(this.variableMode.containsKey(name)) {
				return (Mode) this.variableMode.get(name);
			}else if(variableExists(name)) {
				return DEFAULT_MODE;
			}else {
				return null;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.interfaces.Scope#isReadOnly(java.lang.String, boolean)
	 */
	public void setMode(String name, Mode value) {
		synchronized(variableMode) {
			if(this.variableExists(name)) {
				variableMode.put(name,value);
			}else {
				logger.warn("Mode can't be set for variable '"+name+"' it does not exist");
			}
		}
	}
	/**
	 * Creates a scope whos variables can't be modified
	 * @param scope
	 * @return
	 */
	public static Scope createImmutableScope(final Scope scope) {
	    if(scope==null) {
	        return null;
	    }
		Scope result = new Scope() {

			public Set getVariableNames() {				
				return scope.getVariableNames();
			}

			public Object getValue(String name) {
				return scope.getValue(name);
			}

			public Scope getSuper() {
				return scope.getSuper();
			}

			public void setVariableTo(String name, Object value) throws ScopeException {
				immutableException();
			}

			public boolean variableExists(String name) {
				return scope.variableExists(name);
			}

			public Mode getMode(String name) {			
				return Scope.READ_ONLY;
			}

			public void setMode(String name, Mode value) {
				immutableException();
			}

			public void addPropertyChangeListener(PropertyChangeListener pcListener) {
				scope.addPropertyChangeListener(pcListener);
			}

			public void addPropertyChangeListener(String propertyName, PropertyChangeListener pcListener) {
				scope.addPropertyChangeListener(propertyName,pcListener);				
			}

			public void removePropertyChangeListener(PropertyChangeListener pcListener) {
				scope.removePropertyChangeListener(pcListener);				
			}			
            public Collection getValues() {                
                return scope.getValues();
            }
            public Set getUniqueValues() {                
                return scope.getUniqueValues();
            }
            private void immutableException() {
				throw new RuntimeException("This is an immutable version of scope");
			}
			
		};
		return result;
	}
	
	
	/**
	 * Returns a Scope of the system 
	 * @return
	 */
	public static Scope getSystemScope() {
		if(systemScope == null) {
			systemScope = new ScopeImpl();
			synchronized(systemScope) {
				// all the system variables
				Properties system = System.getProperties();
				Iterator iSystemPropName = system.keySet().iterator();
				while(iSystemPropName.hasNext()) {
					String pName = (String)iSystemPropName.next();
					String value = system.getProperty(pName);
					logger.debug("adding system prop key='"+pName+"' value='"+value+"'");
					try {
						systemScope.setVariableTo(pName,value);
						systemScope.setMode(pName,Scope.READ_ONLY);
					} catch (ScopeException e) {				
						logger.warn("Couldn't inialize systemScope",e);
					}
				}
				// additional variables to add
				try {
					systemScope.setVariableTo("globus.home", System.getProperty("user.home")+File.separator+".globus"+File.separator);
				} catch (ScopeException e) {
					logger.warn("Couldn't inialize systemScope",e);
				}
				systemScope = createImmutableScope(systemScope);
			}
		}
		return systemScope;
	}
}
