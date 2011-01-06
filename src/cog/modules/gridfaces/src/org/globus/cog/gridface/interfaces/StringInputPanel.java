
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import org.globus.cog.gridface.interfaces.Listener;

/**
A panel the inputs a String.

is a Panel that looks similar to <br>
 +--------------------------------+<br>
 | Name : __________________     |<br>
 +--------------------------------+<br>
it is useful to input quickly some names
**/

public interface StringInputPanel {

    /**
     * Set the label that is used in the panel.
     * @param label The new label.
     */
    public void setLabel(String label) ;

    /**
     * Set the value that is used in the panel.
     * @param value,  the new value.
     */
    public void set(String value) ;

    /**
     * gets the value that is used in the panel.
     * @param value,  the value in the text field.
     */
    public void get() ;

    /**
     * Adds a Listener so that changes are propagated.
     *
     * @param listener a <code>Listener</code> that listens to the changes
     */
    public void addListener(Listener listener);

    /**
     * Removes a listener for the component.
     *
     * @param listener a <code>Listener</code> that used to listen to the changes
     */
    public void removeListener(Listener listener); 
    
}
