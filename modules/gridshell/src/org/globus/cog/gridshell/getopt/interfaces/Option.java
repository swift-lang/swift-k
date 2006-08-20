/*
 * Created on Dec 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.getopt.interfaces;

/**
 * Option is a value that is associated a string value of a short key (one character) and/or a long key (more than one character)
 * 
 * 
 */
public interface Option extends Storable, Comparable {	
	/**
	 * Used for flag variables for true
	 */
	public static final Boolean TRUE = Boolean.TRUE;
	/**
	 * Used for flag variables for false
	 */
	public static final Boolean FALSE = Boolean.FALSE;
	/**
	 * Returns the short key associated with this option 
	 * @return
	 */
	String getShort();
	/**
	 * Returns the long key associated with this option
	 * @return
	 */
	String getLong();
	/**
	 * Determines if this is a flag option. 
	 * If isFlag is true, then the value of it is true if the option appears, false if it doesn't.
	 * If isFlag is false, there must be a value specified at the commandline for it. 
	 * @return
	 */
	boolean isFlag();
}
