/*
 * 
 */
package org.globus.cog.gridshell.getopt.app;

import org.globus.cog.gridshell.getopt.interfaces.Argument;
import org.globus.cog.gridshell.getopt.interfaces.Option;

/**
 * An Implemenation of Argument 
 * 
 */
public class ArgumentImpl extends StorableImpl implements Argument {
	// The option that overrides the need/value for/of this argument
    private Option optionThatOverrides;
    
	public ArgumentImpl(String description, Class type, boolean isRequired, Object defaultValue,Option optionThatOverrides) {
	  super(description,type,isRequired,defaultValue);
      this.optionThatOverrides = optionThatOverrides;		
	}
	public ArgumentImpl(String description, Class type, boolean isRequired, Object defaultValue) {
		this(description,type,isRequired,defaultValue,null);
	}
	public ArgumentImpl(String description,Class type, boolean isRequired) {
		this(description,type,isRequired,null,null);
	}
	public ArgumentImpl(String description,Class type,Option optionThatOverrides) {
		this(description,type,true,null,optionThatOverrides);
	}
	public ArgumentImpl(String description,Class type) {
		this(description,type,true,null,null);
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Argument#getOptionThatOverrides()
	 */
	public Option getOptionThatOverrides() {
		return optionThatOverrides;
	}	
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Storable#getValue()
	 */
	public Object getValue() {
		Object result = super.getValue();
		if(optionThatOverrides != null && optionThatOverrides.isSet()) {
			result = optionThatOverrides.getValue();
		}		
		return result;
	}
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Storable#isSet()
	 */
	public boolean isSet() {
		boolean result = super.isSet();
		if(result == false && optionThatOverrides != null) {
			result = optionThatOverrides.isSet();
		}		
		return result;
	}
	public String toString() {
		return super.toString();
	}
}
