
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

/**
A viewer panel hs an area in which an object is rendered for viewing.

<br>
 +--------------------------------+<br>
 | Location: ___________________  |<br>
 +--------------------------------+<br>
 |                                |<br>
 |                                |<br>
 |         Viewer Window          |<br>
 |                                |<br>
 |                                |<br>
 |                                |<br>
 + -------------------------------+<br>

 * A viewer panel contains a location and a URI pointing to a peace of
 * information to be viewed. This could be files, or even a query to
 * an information system or database.  a viewer can have support for
 * multiple protocols. Only protocols that are registered can be
 * viewed.
*/

public interface ViewerPanel extends GridFace {

    /**
     * Regoisters a viewer for a particulat protocol.
     *
     * @param mimetype a <code>String</code> the mimetype. For now we
     * use a combination of endings and
     * prefixes. E.g. gridftp://..../a.jpg. Will fetch and display in
     * an image in the viewer. The file will be downloaded to the
     * local computer.
     * @param viewer an <code>Object</code> The viewer the displays
     * the result in the view window.
     */
    public void register (String mimetype, Object viewer);

    /**
     * Sets the default viewer to
     *
     * @param viewer an <code>Object</code> value
     */
    public void setDefault (Object viewer);

    // we may want to do an ObjectViewer or ObjectRenderer

}
