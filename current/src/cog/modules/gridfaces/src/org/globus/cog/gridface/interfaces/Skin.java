
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/**
A skin is used to define fonts, icons and their sizes.

Sets font size and icon size. This way we can display it during demos
more easily. It actually is not intended to set the frame look. This
is overkill.

The icons are kept in a directory 

.../share/sizexsize/...

**/

public interface Skin {

    // sets the font size
    public void setFontSize (int size);
 
    // sets the iconsize
    public void setIconSize (int size);

    //sets font an iconsize to the default
    public void setDefault(); 

    public void setIconDir (String dir);    

}
