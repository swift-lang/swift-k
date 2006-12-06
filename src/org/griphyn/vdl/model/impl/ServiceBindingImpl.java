/*
 * XML Type:  ServiceBinding
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.ServiceBinding
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML ServiceBinding(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class ServiceBindingImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.ServiceBinding
{
    
    public ServiceBindingImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName WSDLURI$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "wsdluri");
    private static final javax.xml.namespace.QName PORTYPE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "portype");
    private static final javax.xml.namespace.QName OPERATION$4 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "operation");
    private static final javax.xml.namespace.QName REQUEST$6 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "request");
    private static final javax.xml.namespace.QName RESPONSE$8 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "response");
    
    
    /**
     * Gets the "wsdluri" element
     */
    public org.apache.xmlbeans.XmlObject getWsdluri()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(WSDLURI$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "wsdluri" element
     */
    public void setWsdluri(org.apache.xmlbeans.XmlObject wsdluri)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(WSDLURI$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(WSDLURI$0);
            }
            target.set(wsdluri);
        }
    }
    
    /**
     * Appends and returns a new empty "wsdluri" element
     */
    public org.apache.xmlbeans.XmlObject addNewWsdluri()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(WSDLURI$0);
            return target;
        }
    }
    
    /**
     * Gets the "portype" element
     */
    public org.apache.xmlbeans.XmlObject getPortype()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(PORTYPE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "portype" element
     */
    public boolean isSetPortype()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PORTYPE$2) != 0;
        }
    }
    
    /**
     * Sets the "portype" element
     */
    public void setPortype(org.apache.xmlbeans.XmlObject portype)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(PORTYPE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(PORTYPE$2);
            }
            target.set(portype);
        }
    }
    
    /**
     * Appends and returns a new empty "portype" element
     */
    public org.apache.xmlbeans.XmlObject addNewPortype()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(PORTYPE$2);
            return target;
        }
    }
    
    /**
     * Unsets the "portype" element
     */
    public void unsetPortype()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PORTYPE$2, 0);
        }
    }
    
    /**
     * Gets the "operation" element
     */
    public org.apache.xmlbeans.XmlObject getOperation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(OPERATION$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "operation" element
     */
    public void setOperation(org.apache.xmlbeans.XmlObject operation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(OPERATION$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(OPERATION$4);
            }
            target.set(operation);
        }
    }
    
    /**
     * Appends and returns a new empty "operation" element
     */
    public org.apache.xmlbeans.XmlObject addNewOperation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(OPERATION$4);
            return target;
        }
    }
    
    /**
     * Gets the "request" element
     */
    public org.griphyn.vdl.model.Message getRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().find_element_user(REQUEST$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "request" element
     */
    public boolean isSetRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(REQUEST$6) != 0;
        }
    }
    
    /**
     * Sets the "request" element
     */
    public void setRequest(org.griphyn.vdl.model.Message request)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().find_element_user(REQUEST$6, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Message)get_store().add_element_user(REQUEST$6);
            }
            target.set(request);
        }
    }
    
    /**
     * Appends and returns a new empty "request" element
     */
    public org.griphyn.vdl.model.Message addNewRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().add_element_user(REQUEST$6);
            return target;
        }
    }
    
    /**
     * Unsets the "request" element
     */
    public void unsetRequest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(REQUEST$6, 0);
        }
    }
    
    /**
     * Gets the "response" element
     */
    public org.griphyn.vdl.model.Message getResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().find_element_user(RESPONSE$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "response" element
     */
    public boolean isSetResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RESPONSE$8) != 0;
        }
    }
    
    /**
     * Sets the "response" element
     */
    public void setResponse(org.griphyn.vdl.model.Message response)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().find_element_user(RESPONSE$8, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Message)get_store().add_element_user(RESPONSE$8);
            }
            target.set(response);
        }
    }
    
    /**
     * Appends and returns a new empty "response" element
     */
    public org.griphyn.vdl.model.Message addNewResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Message target = null;
            target = (org.griphyn.vdl.model.Message)get_store().add_element_user(RESPONSE$8);
            return target;
        }
    }
    
    /**
     * Unsets the "response" element
     */
    public void unsetResponse()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RESPONSE$8, 0);
        }
    }
}
