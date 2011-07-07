/*
 * This file or a portion of this file is licensed under the terms of
 * the Globus Toolkit Public License, found in file GTPL, or at
 * http://www.globus.org/toolkit/download/license.html. This notice must
 * appear in redistributions of this file, with or without modification.
 *
 * Redistributions of this Software, with or without modification, must
 * reproduce the GTPL in: (1) the Software, or (2) the Documentation or
 * some other similar material which is provided with the Software (if
 * any).
 *
 * Copyright 1999-2004 University of Chicago and The University of
 * Southern California. All rights reserved.
 */
package org.globus.swift.catalog.util;


/**
 * This is the container for all the Data classes.
 *
 * @author Karan Vahi
 * @author Gaurang Mehta
 * @version $Revision: 1.7 $
 */
public abstract class Data implements Cloneable {

    /**
     * The String which stores the message to be stored.
     */
    public String mLogMsg;


    /**
     * The default constructor.
     */
    public Data(){
        mLogMsg = new String();
    }


    /**
     * Returns the String version of the data object, which is in human readable
     * form.
     */
    public abstract String toString();

}