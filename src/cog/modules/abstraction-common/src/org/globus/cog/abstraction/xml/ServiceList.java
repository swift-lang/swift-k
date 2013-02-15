/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: ServiceList.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class ServiceList implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.util.Vector _serviceList;


      //----------------/
     //- Constructors -/
    //----------------/

    public ServiceList() {
        super();
        _serviceList = new Vector();
    } //-- org.globus.cog.core.xml.ServiceList()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vService
    **/
    public void addService(Service vService)
        throws java.lang.IndexOutOfBoundsException
    {
        _serviceList.addElement(vService);
    } //-- void addService(Service) 

    /**
    **/
    public java.util.Enumeration enumerateService()
    {
        return _serviceList.elements();
    } //-- java.util.Enumeration enumerateService() 

    /**
     * 
     * @param index
    **/
    public Service getService(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Service) _serviceList.elementAt(index);
    } //-- Service getService(int) 

    /**
    **/
    public Service[] getService()
    {
        int size = _serviceList.size();
        Service[] mArray = new Service[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Service) _serviceList.elementAt(index);
        }
        return mArray;
    } //-- Service[] getService() 

    /**
    **/
    public int getServiceCount()
    {
        return _serviceList.size();
    } //-- int getServiceCount() 

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
    public void removeAllService()
    {
        _serviceList.removeAllElements();
    } //-- void removeAllService() 

    /**
     * 
     * @param index
    **/
    public Service removeService(int index)
    {
        Object obj = _serviceList.elementAt(index);
        _serviceList.removeElementAt(index);
        return (Service) obj;
    } //-- Service removeService(int) 

    /**
     * 
     * @param index
     * @param vService
    **/
    public void setService(int index, Service vService)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _serviceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _serviceList.setElementAt(vService, index);
    } //-- void setService(int, Service) 

    /**
     * 
     * @param serviceArray
    **/
    public void setService(Service[] serviceArray)
    {
        //-- copy array
        _serviceList.removeAllElements();
        for (int i = 0; i < serviceArray.length; i++) {
            _serviceList.addElement(serviceArray[i]);
        }
    } //-- void setService(Service) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.ServiceList unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.ServiceList) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.ServiceList.class, reader);
    } //-- org.globus.cog.core.xml.ServiceList unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
