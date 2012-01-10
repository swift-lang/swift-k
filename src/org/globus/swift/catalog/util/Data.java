/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
