
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import java.util.Date;

/**
 * A <code>GridFace</code> is an abstract representation of a visual
 * component the is useful to interact with the Grid. GridFaces are
 * tyoically build out of small panel components. Events between
 * gridfaces are propagarted through property change listeners.
 */
public interface GridFace {

    /**
     * updates the displaed gridface. 
     */
    void update();
 
    /**
     * Records the last time the GridFace was updated. The default
     * value is the time of creation.
     *
     * @return <code>Date</code>, the last time when the GridFace was
     * updated.
     */
    Date lastUpdateTime();


    /**
     * sets the name for a GridFace. Names are useful to distinguish
     * gridfaces from each other. A Name is supposed to be
     * unique. Default is null.
     *
     * @param name a <code>String</code> thatreturns a uniqe name.
     */
    void setName (String name);

    /**
     * sets a label for a GridFace. Label are useful to create
     * abbreviations for a GridFace for a graphical display. Labels
     * are ofthe used as a title if applicable. Defaukt is null.
     *
     * @param label a <code>String</code> that returns the assigned label.
     */
    void setLabel (String label);

    /**
     * Registers another GridFace to this GridFace. This will prevent
     * that gridfaces are unnecesarily killed. 
     *
     * @param connection a <code>GridFace</code> value
     */
    void register (GridFace connection);

}
