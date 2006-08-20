/*
 * 
 */
package org.globus.cog.gridshell.getopt.interfaces;

/**
 * The goal of an ArgParser is to take an input and then set all the correct values of getOpt associated with the parser
 * 
 */
public interface ArgParser {
  
	/**
	 * Parses a command line input into the getGetOpt()
	 * @param commandLine - the value entered at the commandline to parse
	 */
	public void parse(GetOpt getopt, String commandLine);
}
