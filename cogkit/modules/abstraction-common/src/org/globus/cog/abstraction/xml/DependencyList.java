/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: DependencyList.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * List of dependencies defined as "from" "to" pairs 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class DependencyList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _dependencyList;


      //----------------/
     //- Constructors -/
    //----------------/

    public DependencyList() {
        super();
        _dependencyList = new Vector();
    } //-- org.globus.cog.core.xml.DependencyList()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vDependency
    **/
    public void addDependency(Dependency vDependency)
        throws java.lang.IndexOutOfBoundsException
    {
        _dependencyList.addElement(vDependency);
    } //-- void addDependency(Dependency) 

    /**
    **/
    public java.util.Enumeration enumerateDependency()
    {
        return _dependencyList.elements();
    } //-- java.util.Enumeration enumerateDependency() 

    /**
     * 
     * @param index
    **/
    public Dependency getDependency(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _dependencyList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Dependency) _dependencyList.elementAt(index);
    } //-- Dependency getDependency(int) 

    /**
    **/
    public Dependency[] getDependency()
    {
        int size = _dependencyList.size();
        Dependency[] mArray = new Dependency[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Dependency) _dependencyList.elementAt(index);
        }
        return mArray;
    } //-- Dependency[] getDependency() 

    /**
    **/
    public int getDependencyCount()
    {
        return _dependencyList.size();
    } //-- int getDependencyCount() 

    /**
    **/
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
    **/
    public void removeAllDependency()
    {
        _dependencyList.removeAllElements();
    } //-- void removeAllDependency() 

    /**
     * 
     * @param index
    **/
    public Dependency removeDependency(int index)
    {
        Object obj = _dependencyList.elementAt(index);
        _dependencyList.removeElementAt(index);
        return (Dependency) obj;
    } //-- Dependency removeDependency(int) 

    /**
     * 
     * @param index
     * @param vDependency
    **/
    public void setDependency(int index, Dependency vDependency)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _dependencyList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _dependencyList.setElementAt(vDependency, index);
    } //-- void setDependency(int, Dependency) 

    /**
     * 
     * @param dependencyArray
    **/
    public void setDependency(Dependency[] dependencyArray)
    {
        //-- copy array
        _dependencyList.removeAllElements();
        for (int i = 0; i < dependencyArray.length; i++) {
            _dependencyList.addElement(dependencyArray[i]);
        }
    } //-- void setDependency(Dependency) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.DependencyList unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.DependencyList) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.DependencyList.class, reader);
    } //-- org.globus.cog.core.xml.DependencyList unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
