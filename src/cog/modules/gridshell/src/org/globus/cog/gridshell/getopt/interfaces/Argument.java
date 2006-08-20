/*
 */
package org.globus.cog.gridshell.getopt.interfaces;

/**
 * An argument is a value that is not associated with an option
 * 
 * 
 */
public interface Argument extends Storable {
  /**
   * Arguments can have options that override them, this returns the option that overrides the need/value for this argument
   * @return
   */
  Option getOptionThatOverrides();   
}
