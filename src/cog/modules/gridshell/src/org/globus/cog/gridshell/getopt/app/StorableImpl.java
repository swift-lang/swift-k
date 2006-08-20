/*
 * 
 */
package org.globus.cog.gridshell.getopt.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.getopt.interfaces.Option;
import org.globus.cog.gridshell.getopt.interfaces.Storable;
import org.globus.cog.gridshell.getopt.interfaces.Validator;

/**
 * An implemenation of Storable
 * 
 */
public class StorableImpl implements Storable {
	private static Logger logger = Logger.getLogger(StorableImpl.class);
	
	// The description of this variable
	private String description;
	// The type of this variable
	private Class type;
	// Is this variable required
	private boolean isRequired;
	// The value of this variable
	private Object value;
	// A mapping of classes to validators
	private static Map validators = new HashMap();
	// This is just a boolean to see if we have already inited the default validators
	private static boolean hasSetValidators = false;
	/**
	 * A Constructor for a Storable
	 * @param description - the description
	 * @param type - The type of this storable
	 * @param isRequired - Is this storable requried
	 * @param defaultValue - A default value for the storable
	 */
	public StorableImpl(String description, Class type, boolean isRequired, Object defaultValue) {
		// set member variables
		this.description = description;	    
	    this.type = type;
	    this.isRequired = isRequired;
	    // if we have not set the default validators set them
	    if(!hasSetValidators) {
	      hasSetValidators = true;
	      resetDefaultValidators();
	    }
	    
	    // Set the value after validators are added
	    setValue(defaultValue);	    
	}	
	
	/************************************
	 * Static methods
	 ************************************/
	
	/**
	 * Remove all validators and then sets the default validators, synchronized on validators
	 */
	public static void resetDefaultValidators() {	    
		synchronized (validators) {
			// reset validators
			validators = new HashMap();
			
			// add our validators
			setValidator(Boolean.class,Validator.booleanValidator);
			setValidator(Integer.class,Validator.intValidator);
			setValidator(Double.class,Validator.doubleValidator);
			setValidator(String.class,Validator.stringValidator);
			setValidator(java.net.URL.class,Validator.urlValidator);
			setValidator(java.net.URI.class,Validator.uriValidator);
		}
	}
	
	/**
	 * Adds a validator 
	 * @param type - the type identified by a class that will be validated
	 * @param validator  - the validator for the class, if it is null removes a validator for this class
	 */
	public static void setValidator(Class type, Validator validator) {
		if(validator != null) {
			validators.put(type,validator);
		}else if(validator == null && validators.containsKey(type)) {
			validators.remove(type);
		}		
	}
	/**
	 * Returns an immutable mapping of validators
	 * @return
	 */
	public static Map getValidators() {
		return Collections.unmodifiableMap(validators);
	}
	// end static	

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#getType()
	 */
	public Class getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#getValue()
	 */
	public Object getValue() {
		return value;
	}
	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#isRequired()
	 */
	public boolean isRequired() {
		return isRequired;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#isSet()
	 */
	public boolean isSet() {
		return getValue() != null && getValue() != Option.FALSE;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#setValue(java.lang.Object)
	 */
	public synchronized void setValue(final Object value) {
		Object result = value;
		// if validator provided, validate, and set value to the type
		if(getValidators().containsKey(type) && value instanceof String) {
			Validator thisValidator = (Validator)getValidators().get(type);
			logger.debug("Trying to validate: '"+value+"' for "+this);
			try {				
				result = thisValidator.validate((String)value);
				logger.debug("valid valid");
			}catch (Exception e) {
				logger.debug("Invalid value");
				// ensure our value is the old value
				result = getValue();
				throw new RuntimeException("Error: Value '"+value 
						+ "' cannot be validated by validator '"+thisValidator+"' as type '"+getType()+"'", e);
			}				
		}else {
			logger.debug("Did not try to validate: '"+value+"' for "+this);
		}
		
		this.value = result;
	}
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "\""+getDescription()+"\" <"+getType()+"> "+getValue();
	}
}
