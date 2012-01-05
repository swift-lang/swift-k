/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: Task.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Representation of the abstract Java CoG Kit Task object.
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class Task implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Identity represents a uniform resource name (URN) that
     * uniquely identifies a Grid entity
    **/
    private java.lang.String _identity;

    private java.lang.String _name;

    private java.lang.String _type;

    private java.lang.String _provider;

    private ServiceList _serviceList;

    private Specification _specification;

    /**
     * List of attributes defined as "name" "value" pairs 
    **/
    private AttributeList _attributeList;

    private java.lang.String _status;

    private java.util.Date _submittedTime;

    private java.util.Date _completedTime;


      //----------------/
     //- Constructors -/
    //----------------/

    public Task() {
        super();
    } //-- org.globus.cog.core.xml.Task()


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
    public java.util.Date getCompletedTime()
    {
        return this._completedTime;
    } //-- java.util.Date getCompletedTime() 

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
    public ServiceList getServiceList()
    {
        return this._serviceList;
    } //-- ServiceList getServiceList() 

    /**
    **/
    public Specification getSpecification()
    {
        return this._specification;
    } //-- Specification getSpecification() 

    /**
    **/
    public java.lang.String getStatus()
    {
        return this._status;
    } //-- java.lang.String getStatus() 

    /**
    **/
    public java.util.Date getSubmittedTime()
    {
        return this._submittedTime;
    } //-- java.util.Date getSubmittedTime() 

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
     * @param completedTime
    **/
    public void setCompletedTime(java.util.Date completedTime)
    {
        this._completedTime = completedTime;
    } //-- void setCompletedTime(java.util.Date) 

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
     * @param serviceList
    **/
    public void setServiceList(ServiceList serviceList)
    {
        this._serviceList = serviceList;
    } //-- void setServiceList(ServiceList) 

    /**
     * 
     * @param specification
    **/
    public void setSpecification(Specification specification)
    {
        this._specification = specification;
    } //-- void setSpecification(Specification) 

    /**
     * 
     * @param status
    **/
    public void setStatus(java.lang.String status)
    {
        this._status = status;
    } //-- void setStatus(java.lang.String) 

    /**
     * 
     * @param submittedTime
    **/
    public void setSubmittedTime(java.util.Date submittedTime)
    {
        this._submittedTime = submittedTime;
    } //-- void setSubmittedTime(java.util.Date) 

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
    public static org.globus.cog.abstraction.xml.Task unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.Task) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.Task.class, reader);
    } //-- org.globus.cog.core.xml.Task unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
