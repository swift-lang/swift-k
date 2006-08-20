
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
 +--------------------------------+

 * A Image viewer panel contains a location and a URI pointing to a peace of
 * information to be viewed. This could be files, or even a query to
 * an information system or database.  a viewer can have support for
 * multiple protocols. Only protocols that are registered can be
 * viewed. It is a specilized viewer panel.
*  Should be able to select an image from the browser.
*
*
*/

//import java.net.URI;

public interface ImageViewerPanel extends ViewerPanel {

//Gregor removed these methods:
        //Obtain the uri from the panel
//        public URI getURI();

        //Set the URI for the new image..called from save as
//        public void setURI(URI uri);
///////////////////

        //Load the image corresponding to the URI
        public void load();

        //rotate the image by the given angle
        public void rotate(int angle);

        //flip the image vertically
        public void flipVertical();

        //flip the image horizontally
        public void flipHorizontal();

        //Resize the image
        public void changeSize(double changeSizeFactor);
        
        
        //operations to be performed when the window closes
        //public void close();


        //Save the changed image
        public void save();

        //Save as a new image
        public void saveAS();


}
