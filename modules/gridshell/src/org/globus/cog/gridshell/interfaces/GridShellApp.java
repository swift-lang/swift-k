/*
 * The application of GridShell
 */
package org.globus.cog.gridshell.interfaces;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * 
 */
public interface GridShellApp extends Serializable {
  /**
   * Set the prompt to nPrompt
   * @param nPrompt
   */	
  void setPrompt(String nPrompt);
  /**
   * Get the current prompt
   * @return
   */
  String getPrompt();  

  /**
   * Adds pListener as a PropertyChangeListener
   * @param pListener
   */
  void addPropertyChangeListener(PropertyChangeListener pListener);
  
  /**
   * Removes pListener from the list of propertyChangeListeners
   * @param pListener
   */
  void removePropertyChangeListener(PropertyChangeListener pListener);
  
  /**
   * Returns the scope for this GridShell
   * @return
   */
  Scope getScope();
  
  /**
   * Returns the history with this shell
   * @return
   */
  ShellHistory getShellHistory();
  
}
