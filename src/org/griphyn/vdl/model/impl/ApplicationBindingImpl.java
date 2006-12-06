/*
 * XML Type:  ApplicationBinding
 * Namespace: http://www.griphyn.org/2006/08/vdl
 * Java type: org.griphyn.vdl.model.ApplicationBinding
 *
 * Automatically generated - do not modify.
 */
package org.griphyn.vdl.model.impl;
/**
 * An XML ApplicationBinding(@http://www.griphyn.org/2006/08/vdl).
 *
 * This is a complex type.
 */
public class ApplicationBindingImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements org.griphyn.vdl.model.ApplicationBinding
{
    
    public ApplicationBindingImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName APPENV$0 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "appenv");
    private static final javax.xml.namespace.QName EXECUTABLE$2 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "executable");
    private static final javax.xml.namespace.QName ARGUMENT$4 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "argument");
    private static final javax.xml.namespace.QName STDIN$6 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "stdin");
    private static final javax.xml.namespace.QName STDOUT$8 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "stdout");
    private static final javax.xml.namespace.QName STDERR$10 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "stderr");
    private static final javax.xml.namespace.QName PROFILE$12 = 
        new javax.xml.namespace.QName("http://www.griphyn.org/2006/08/vdl", "profile");
    
    
    /**
     * Gets the "appenv" element
     */
    public org.apache.xmlbeans.XmlObject getAppenv()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(APPENV$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "appenv" element
     */
    public boolean isSetAppenv()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(APPENV$0) != 0;
        }
    }
    
    /**
     * Sets the "appenv" element
     */
    public void setAppenv(org.apache.xmlbeans.XmlObject appenv)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(APPENV$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(APPENV$0);
            }
            target.set(appenv);
        }
    }
    
    /**
     * Appends and returns a new empty "appenv" element
     */
    public org.apache.xmlbeans.XmlObject addNewAppenv()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(APPENV$0);
            return target;
        }
    }
    
    /**
     * Unsets the "appenv" element
     */
    public void unsetAppenv()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(APPENV$0, 0);
        }
    }
    
    /**
     * Gets the "executable" element
     */
    public org.apache.xmlbeans.XmlObject getExecutable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(EXECUTABLE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "executable" element
     */
    public void setExecutable(org.apache.xmlbeans.XmlObject executable)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().find_element_user(EXECUTABLE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(EXECUTABLE$2);
            }
            target.set(executable);
        }
    }
    
    /**
     * Appends and returns a new empty "executable" element
     */
    public org.apache.xmlbeans.XmlObject addNewExecutable()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlObject target = null;
            target = (org.apache.xmlbeans.XmlObject)get_store().add_element_user(EXECUTABLE$2);
            return target;
        }
    }
    
    /**
     * Gets array of all "argument" elements
     */
    public org.griphyn.vdl.model.Argument[] getArgumentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ARGUMENT$4, targetList);
            org.griphyn.vdl.model.Argument[] result = new org.griphyn.vdl.model.Argument[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "argument" element
     */
    public org.griphyn.vdl.model.Argument getArgumentArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(ARGUMENT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "argument" element
     */
    public int sizeOfArgumentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ARGUMENT$4);
        }
    }
    
    /**
     * Sets array of all "argument" element
     */
    public void setArgumentArray(org.griphyn.vdl.model.Argument[] argumentArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(argumentArray, ARGUMENT$4);
        }
    }
    
    /**
     * Sets ith "argument" element
     */
    public void setArgumentArray(int i, org.griphyn.vdl.model.Argument argument)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(ARGUMENT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(argument);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "argument" element
     */
    public org.griphyn.vdl.model.Argument insertNewArgument(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().insert_element_user(ARGUMENT$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "argument" element
     */
    public org.griphyn.vdl.model.Argument addNewArgument()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(ARGUMENT$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "argument" element
     */
    public void removeArgument(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ARGUMENT$4, i);
        }
    }
    
    /**
     * Gets the "stdin" element
     */
    public org.griphyn.vdl.model.Argument getStdin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDIN$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "stdin" element
     */
    public boolean isSetStdin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STDIN$6) != 0;
        }
    }
    
    /**
     * Sets the "stdin" element
     */
    public void setStdin(org.griphyn.vdl.model.Argument stdin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDIN$6, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDIN$6);
            }
            target.set(stdin);
        }
    }
    
    /**
     * Appends and returns a new empty "stdin" element
     */
    public org.griphyn.vdl.model.Argument addNewStdin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDIN$6);
            return target;
        }
    }
    
    /**
     * Unsets the "stdin" element
     */
    public void unsetStdin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STDIN$6, 0);
        }
    }
    
    /**
     * Gets the "stdout" element
     */
    public org.griphyn.vdl.model.Argument getStdout()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDOUT$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "stdout" element
     */
    public boolean isSetStdout()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STDOUT$8) != 0;
        }
    }
    
    /**
     * Sets the "stdout" element
     */
    public void setStdout(org.griphyn.vdl.model.Argument stdout)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDOUT$8, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDOUT$8);
            }
            target.set(stdout);
        }
    }
    
    /**
     * Appends and returns a new empty "stdout" element
     */
    public org.griphyn.vdl.model.Argument addNewStdout()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDOUT$8);
            return target;
        }
    }
    
    /**
     * Unsets the "stdout" element
     */
    public void unsetStdout()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STDOUT$8, 0);
        }
    }
    
    /**
     * Gets the "stderr" element
     */
    public org.griphyn.vdl.model.Argument getStderr()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDERR$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "stderr" element
     */
    public boolean isSetStderr()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STDERR$10) != 0;
        }
    }
    
    /**
     * Sets the "stderr" element
     */
    public void setStderr(org.griphyn.vdl.model.Argument stderr)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().find_element_user(STDERR$10, 0);
            if (target == null)
            {
                target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDERR$10);
            }
            target.set(stderr);
        }
    }
    
    /**
     * Appends and returns a new empty "stderr" element
     */
    public org.griphyn.vdl.model.Argument addNewStderr()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Argument target = null;
            target = (org.griphyn.vdl.model.Argument)get_store().add_element_user(STDERR$10);
            return target;
        }
    }
    
    /**
     * Unsets the "stderr" element
     */
    public void unsetStderr()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STDERR$10, 0);
        }
    }
    
    /**
     * Gets array of all "profile" elements
     */
    public org.griphyn.vdl.model.Profile[] getProfileArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PROFILE$12, targetList);
            org.griphyn.vdl.model.Profile[] result = new org.griphyn.vdl.model.Profile[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "profile" element
     */
    public org.griphyn.vdl.model.Profile getProfileArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Profile target = null;
            target = (org.griphyn.vdl.model.Profile)get_store().find_element_user(PROFILE$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "profile" element
     */
    public int sizeOfProfileArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PROFILE$12);
        }
    }
    
    /**
     * Sets array of all "profile" element
     */
    public void setProfileArray(org.griphyn.vdl.model.Profile[] profileArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(profileArray, PROFILE$12);
        }
    }
    
    /**
     * Sets ith "profile" element
     */
    public void setProfileArray(int i, org.griphyn.vdl.model.Profile profile)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Profile target = null;
            target = (org.griphyn.vdl.model.Profile)get_store().find_element_user(PROFILE$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(profile);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "profile" element
     */
    public org.griphyn.vdl.model.Profile insertNewProfile(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Profile target = null;
            target = (org.griphyn.vdl.model.Profile)get_store().insert_element_user(PROFILE$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "profile" element
     */
    public org.griphyn.vdl.model.Profile addNewProfile()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.griphyn.vdl.model.Profile target = null;
            target = (org.griphyn.vdl.model.Profile)get_store().add_element_user(PROFILE$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "profile" element
     */
    public void removeProfile(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PROFILE$12, i);
        }
    }
}
