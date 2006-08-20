/*
 * Created on Jun 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.repository.impl.jdbc;

/**
 * Test Exception class. Will be standardized later to include more informative exceptions for the repository.
 */
public class MetaDataNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	public MetaDataNotFoundException(String s){
		super(s);
	}
}
