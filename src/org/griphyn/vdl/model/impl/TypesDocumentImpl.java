/*
 * An XML document type.
 * Localname: types
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.TypesDocument
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * A document containing one types(@http://www.griphyn.org/2006/08/vdl) element.
 *
 * This is a complex type.
 */
public class TypesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.TypesDocument
{
    
    public TypesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName TYPES$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "types");
    
    
    /**
     * Gets the "types" element
     */
    public org.griphyn.vdl.model.TypesDocument.Types getTypes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.TypesDocument.Types target = null;
            target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().find_element_user(TYPES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "types" element
     */
    public void setTypes(org.griphyn.vdl.model.TypesDocument.Types types)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.TypesDocument.Types target = null;
            target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().find_element_user(TYPES$0, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().add_element_user(TYPES$0);
            }
            target.set(types);
        }
    }
    
    /**
     * Appends and returns a new empty "types" element
     */
    public org.griphyn.vdl.model.TypesDocument.Types addNewTypes()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.TypesDocument.Types target = null;
            target = (org.griphyn.vdl.model.TypesDocument.Types)get_store().add_element_user(TYPES$0);
            return target;
        }
    }
    /**
     * An XML types(@http://www.griphyn.org/2006/08/vdl).
     *
     * This is a complex type.
     */
    public static class TypesImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.TypesDocument.Types
    {
        
        public TypesImpl(org.apache.xmlbeans.SchemaType sType)
        {
            super(sType);
        }
        
        
    }
}
