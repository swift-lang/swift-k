/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: Service.java,v 1.2 2005/06/08 08:22:16 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision: 1.2 $ $Date: 2005/06/08 08:22:16 $
**/
public class Service implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Identity represents a uniform resource name (URN) that
     * uniquely identifies a Grid entity
    **/
    private java.lang.String _identity;

    private java.lang.String _name;

    private java.lang.String _provider;

    private java.lang.String _type;

    /**
     * Represents the endpoint address of the remote Grid service
    **/
    private java.lang.String _serviceContact;

    private SecurityContext _securityContext;

    /**
     * List of attributes defined as "name" "value" pairs 
    **/
    private AttributeList _attributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Service() {
        super();
    } //-- org.globus.cog.abstraction.xml.Service()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public AttributeList getAttributeList()
    {
        return this._attributeList;
    } //-- AttributeList getAttributeList() 

    /**
    **/
    public java.lang.String getIdentity()
    {
        return this._identity;
    } //-- java.lang.String getIdentity() 

    /**
    **/
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
    **/
    public java.lang.String getProvider()
    {
        return this._provider;
    } //-- java.lang.String getProvider() 

    /**
    **/
    public SecurityContext getSecurityContext()
    {
        return this._securityContext;
    } //-- SecurityContext getSecurityContext() 

    /**
    **/
    public java.lang.String getServiceContact()
    {
        return this._serviceContact;
    } //-- java.lang.String getServiceContact() 

    /**
    **/
    public java.lang.String getType()
    {
        return this._type;
    } //-- java.lang.String getType() 

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
     * @param identity
    **/
    public void setIdentity(java.lang.String identity)
    {
        this._identity = identity;
    } //-- void setIdentity(java.lang.String) 

    /**
     * 
     * @param name
    **/
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * 
     * @param provider
    **/
    public void setProvider(java.lang.String provider)
    {
        this._provider = provider;
    } //-- void setProvider(java.lang.String) 

    /**
     * 
     * @param securityContext
    **/
    public void setSecurityContext(SecurityContext securityContext)
    {
        this._securityContext = securityContext;
    } //-- void setSecurityContext(SecurityContext) 

    /**
     * 
     * @param serviceContact
    **/
    public void setServiceContact(java.lang.String serviceContact)
    {
        this._serviceContact = serviceContact;
    } //-- void setServiceContact(java.lang.String) 

    /**
     * 
     * @param type
    **/
    public void setType(java.lang.String type)
    {
        this._type = type;
    } //-- void setType(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.Service unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.Service) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.Service.class, reader);
    } //-- org.globus.cog.abstraction.xml.Service unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
