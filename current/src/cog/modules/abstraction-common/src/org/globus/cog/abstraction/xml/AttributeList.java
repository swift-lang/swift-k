/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: AttributeList.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * List of attributes defined as "name" "value" pairs 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class AttributeList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _attributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public AttributeList() {
        super();
        _attributeList = new Vector();
    } //-- org.globus.cog.core.xml.AttributeList()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vAttribute
    **/
    public void addAttribute(Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeList.addElement(vAttribute);
    } //-- void addAttribute(Attribute) 

    /**
    **/
    public java.util.Enumeration enumerateAttribute()
    {
        return _attributeList.elements();
    } //-- java.util.Enumeration enumerateAttribute() 

    /**
     * 
     * @param index
    **/
    public Attribute getAttribute(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Attribute) _attributeList.elementAt(index);
    } //-- Attribute getAttribute(int) 

    /**
    **/
    public Attribute[] getAttribute()
    {
        int size = _attributeList.size();
        Attribute[] mArray = new Attribute[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Attribute) _attributeList.elementAt(index);
        }
        return mArray;
    } //-- Attribute[] getAttribute() 

    /**
    **/
    public int getAttributeCount()
    {
        return _attributeList.size();
    } //-- int getAttributeCount() 

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
    public void removeAllAttribute()
    {
        _attributeList.removeAllElements();
    } //-- void removeAllAttribute() 

    /**
     * 
     * @param index
    **/
    public Attribute removeAttribute(int index)
    {
        Object obj = _attributeList.elementAt(index);
        _attributeList.removeElementAt(index);
        return (Attribute) obj;
    } //-- Attribute removeAttribute(int) 

    /**
     * 
     * @param index
     * @param vAttribute
    **/
    public void setAttribute(int index, Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _attributeList.setElementAt(vAttribute, index);
    } //-- void setAttribute(int, Attribute) 

    /**
     * 
     * @param attributeArray
    **/
    public void setAttribute(Attribute[] attributeArray)
    {
        //-- copy array
        _attributeList.removeAllElements();
        for (int i = 0; i < attributeArray.length; i++) {
            _attributeList.addElement(attributeArray[i]);
        }
    } //-- void setAttribute(Attribute) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.AttributeList unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.AttributeList) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.AttributeList.class, reader);
    } //-- org.globus.cog.core.xml.AttributeList unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
