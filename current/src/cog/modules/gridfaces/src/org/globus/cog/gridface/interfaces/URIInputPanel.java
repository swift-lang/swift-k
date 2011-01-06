
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import java.awt.event.KeyListener;
import java.net.URI;

/**

A panel the inputs a URI.

is a Panel that looks similar to <br>
 +--------------------------------+<br>
 | URI : ___________________     |<br>
 +--------------------------------+<br>

it is useful to input quickly some names IMplementations to this
panel could include browsing windows or help strings.
**/

public interface URIInputPanel {

    /**
     * set the label that is used in the panel.
     * @param label The new label.
     */
    public void setLabel(String label) ;

    /**
     * Set the value that is used in the panel.
     * @param value,  the new value.
     */
    public void set(URI value) ;

    /**
     * gets the value that is used in the panel.
     * @param value,  the value in the text field.
     */
    public void get() ;
    
    public void addKeyListener(KeyListener listener);
    
    public URI getURI();


}
