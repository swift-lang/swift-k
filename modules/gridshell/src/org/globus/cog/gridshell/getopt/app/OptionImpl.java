/*
 * 
 */
package org.globus.cog.gridshell.getopt.app;

import org.globus.cog.gridshell.getopt.interfaces.Option;

/**
 * An implementation of Option
 * 
 */
public class OptionImpl extends StorableImpl implements Option {
	// The keys short and long for this option
	private String shortOption, longOption;
	// tells if this is a flag
	boolean isFlag;
	/**
	 * Creates an instance of Option
	 * @param description - the description
	 * @param type - the type for this option
	 * @param isRequired - is it required (defaults to true)
	 * @param defaultValue - default value (defaults to null)
	 * @param shortOption - the short key associated with this option (must be one character)
	 * @param longOption - the long key associated with this option (must be more than one character)
	 * @param isFlag - is it a flag option (defaults to false)
	 */	
	public OptionImpl(String description, Class type, boolean isRequired, Object defaultValue, String shortOption,String longOption, boolean isFlag) {
		super(description,type,isRequired,defaultValue);
		
		if(shortOption == null && longOption == null) {
			throw new RuntimeException("Error: Cannot have shortOption and longOption both null");
		}else if(shortOption != null && (shortOption.length() != 1 || !shortOption.matches("^\\w$")) ) {
			throw new RuntimeException("Error: Short options must be of length 1 and only word characters. Got '"+shortOption+"'");
		}else if(longOption != null && (longOption.length() < 2 || !longOption.matches("^\\w+$")) ) {
			throw new RuntimeException("Error: Long options must be of length > 1 and only word characters. Got '"+longOption+"'");
		}
		
	    this.shortOption = shortOption;
	    this.longOption = longOption;
	    this.isFlag = isFlag;
	}
	public OptionImpl(String description, Class type, boolean isRequired, Object defaultValue, String shortOption,String longOption) {
		this(description,type,isRequired,defaultValue,shortOption,longOption,true);
	}
	public OptionImpl(String description, Class type, boolean isRequired, String shortOption,String longOption,boolean isFlag) {
	    this(description,type,isRequired,null,shortOption,longOption,isFlag);
	}
	public OptionImpl(String description, Class type, boolean isRequired, String shortOption,String longOption) {
		this(description,type,isRequired,null,shortOption,longOption,true);
	}
	public OptionImpl(String description, Class type, String shortOption,String longOption) {
		this(description,type,true,null,shortOption,longOption,true);
	}
	public OptionImpl(String description, Class type, Object defaultValue, String shortOption,String longOption) {
		this(description,type,true,defaultValue,shortOption,longOption,true);
	}
	public OptionImpl(String description, Class type, String shortOption,String longOption, boolean isFlag) {
		this(description,type,true,null,shortOption,longOption,isFlag);
	}
	public OptionImpl(String description, Class type, Object defaultValue, String shortOption,String longOption, boolean isFlag) {
		this(description,type,true,defaultValue,shortOption,longOption,isFlag);
	}
	public static Option createFlag(String description, String shortOption, String longOption) {
		return createFlag(description,Boolean.FALSE,shortOption,longOption);
	}
	public static Option createFlag(String description, Boolean defaultValue, String shortOption, String longOption) {
		return new OptionImpl(description,Boolean.class,false,defaultValue,shortOption,longOption,true);
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#getShort()
	 */
	public String getShort() {
		return shortOption;
	}

	/* (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#getLong()
	 */
	public String getLong() {
		return longOption;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object that) {
		int result = 0;
		
		Option thatOption = ((Option)that);
		
		if(thatOption.getLong() != null) {
			result = thatOption.getLong().compareTo(getLong());
		}
		
		if(result == 0 && thatOption.getShort() != null) {
			result = thatOption.getShort().compareTo(getShort());
		}
		
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.globus.cog.gridface.impl.gridshell.getopt.interfaces.Option#isFlag()
	 */
	public boolean isFlag() {
		return isFlag;
	}
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "-"+this.getShort()+" --"+this.getLong()+"\n  "+super.toString();
	}
}
