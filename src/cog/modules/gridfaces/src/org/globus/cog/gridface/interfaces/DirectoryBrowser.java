
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import java.net.URI;

/** different functions needed for the directory browser to be implemented as 
combination of methods available in the underlying file transfer client 
**/

//removed the extensionof the FileTransferObject
public interface DirectoryBrowser extends GridFace{

    /** Set the URI **/
    public void setURI(URI uri);

    /** Get the currently selected files URI **/
    public URI getURI();

    /** Select the file corresponding to the file name **/
    public void setSelected(String filename);

    /** Get the name of the currently selected file **/
    public String getSelected();
    
    /** The window directory browser is going to close **/
    //public void close();
  
}
