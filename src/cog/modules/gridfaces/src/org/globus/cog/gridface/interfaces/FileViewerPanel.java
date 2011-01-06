
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/*
 +--------------------------------+
 | Location: ___________________  |
 +--------------------------------+
 |                                |
 |                                |
 |         Viewer Window          |
 |                                |
 |                                |
 |                                |
 + --------------------------------+

 * A File viewer panel contains a location and a URI pointing to a peace of
 * information to be viewed. This could be files, or even a query to
 * an information system or database.  a viewer can have support for
 * multiple protocols. Only protocols that are registered can be
 * viewed. It is a specilized viewer panel.
 * 
 * Provide most of the custom editor features.
*/

import java.net.URI;

public interface FileViewerPanel extends ViewerPanel {
	
    public URI getURI();
    
    public void open();
    
    public void save();
    
    public void saveAs();
    
    public void find();
    
    public void findReplace();
    
    
    /**
     * These methods should be able to interact with lot of data. There parameters are to
     * be changed accordingly.
     */
    public void cut();
    
    public void copy();
    
    public void paste();      	
}

