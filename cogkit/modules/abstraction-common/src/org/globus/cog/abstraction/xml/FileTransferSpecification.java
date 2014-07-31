/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: FileTransferSpecification.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class FileTransferSpecification implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _source;

    private java.lang.String _destination;

    private boolean _thirdParty;

    /**
     * keeps track of state for field: _thirdParty
    **/
    private boolean _has_thirdParty;

    /**
     * List of attributes defined as "name" "value" pairs 
    **/
    private AttributeList _attributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public FileTransferSpecification() {
        super();
    } //-- org.globus.cog.core.xml.FileTransferSpecification()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public void deleteThirdParty()
    {
        this._has_thirdParty= false;
    } //-- void deleteThirdParty() 

    /**
    **/
    public AttributeList getAttributeList()
    {
        return this._attributeList;
    } //-- AttributeList getAttributeList() 

    /**
    **/
    public java.lang.String getDestination()
    {
        return this._destination;
    } //-- java.lang.String getDestination() 

    /**
    **/
    public java.lang.String getSource()
    {
        return this._source;
    } //-- java.lang.String getSource() 

    /**
    **/
    public boolean getThirdParty()
    {
        return this._thirdParty;
    } //-- boolean getThirdParty() 

    /**
    **/
    public boolean hasThirdParty()
    {
        return this._has_thirdParty;
    } //-- boolean hasThirdParty() 

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
     * 
     * @param attributeList
    **/
    public void setAttributeList(AttributeList attributeList)
    {
        this._attributeList = attributeList;
    } //-- void setAttributeList(AttributeList) 

    /**
     * 
     * @param destination
    **/
    public void setDestination(java.lang.String destination)
    {
        this._destination = destination;
    } //-- void setDestination(java.lang.String) 

    /**
     * 
     * @param source
    **/
    public void setSource(java.lang.String source)
    {
        this._source = source;
    } //-- void setSource(java.lang.String) 

    /**
     * 
     * @param thirdParty
    **/
    public void setThirdParty(boolean thirdParty)
    {
        this._thirdParty = thirdParty;
        this._has_thirdParty = true;
    } //-- void setThirdParty(boolean) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.FileTransferSpecification unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.FileTransferSpecification) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.FileTransferSpecification.class, reader);
    } //-- org.globus.cog.core.xml.FileTransferSpecification unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
