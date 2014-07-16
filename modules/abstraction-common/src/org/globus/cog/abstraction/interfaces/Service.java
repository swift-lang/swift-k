// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.util.Collection;
import java.util.Enumeration;

/**
 * This interface abstracts the remote Grid Service required for the execution
 * of the Task. It contains the provider specific details such as the service
 * contact and security context.
 */
public interface Service {
    /**
     * Represents a job submission service.
     * 
     * @deprecated For consistency use {@link EXECUTION}
     */
    public static final int JOB_SUBMISSION = 1;
    
    /**
     * Represents an execution service.
     */
    public static final int EXECUTION = 1;

    /**
     * Represents a file transfer service
     */
    public static final int FILE_TRANSFER = 2;

    /**
     * Represents a information query service. Not implemented at this time.
     */
    public static final int INFORMATION_QUERY = 3;

    /**
     * Represents a file operation service
     */
    public static final int FILE_OPERATION = 4;
    
    /**
     * Represents a WS invocation service
     */
    public static final int WS_INVOCATION = 5;

    /**
     * Represents the index for the job submission service among an ordered list
     * of services.
     */
    public static final int JOB_SUBMISSION_SERVICE = 0;

    /**
     * Represents the index for the default service among an ordered list of
     * services.
     */
    public static final int DEFAULT_SERVICE = 0;

    /**
     * Represents the index for the source service in a file transfer task.
     */
    public static final int FILE_TRANSFER_SOURCE_SERVICE = 0;

    /**
     * Represents the index for the destination service in a file transfer task.
     */
    public static final int FILE_TRANSFER_DESTINATION_SERVICE = 1;

    /**
     * Sets a unique <code>Identity</code> for this <code>Service</code>.
     * 
     * @param identity
     *            the unique <code>Identity</code>.
     */
    public void setIdentity(Identity identity);

    /**
     * Returns the unique <code>Identity</code> assigned to this
     * <code>Service</code>.
     */
    public Identity getIdentity();

    /**
     * Sets the name of this <code>Service</code>. Defines a user-friendly
     * name which need not be unique.
     * 
     * @param name
     *            a string specifying the name of this <code>Service</code>.
     */
    public void setName(String name);

    /**
     * Returns the user-friendly name assigned to this <code>Service</code>.
     */
    public String getName();

    /**
     * Sets the provider for this service. Based on the provider, the
     * {@link TaskHandler}will translate all the abstract elements of the
     * {@link Task}bound to this service into provider-specific constructs
     * 
     * @param provider
     *            a String representing the provider for this
     *            <code>Service</code>.
     */
    public void setProvider(String provider);

    /**
     * Returns the provider associated with this <code>Service</code>.
     */
    public String getProvider();

    /**
     * Sets the type of this <code>Service</code>. Valid types are
     * {@link Service#JOB_SUBMISSION},{@link Service#FILE_TRANSFER}, and
     * {@link Service#FILE_OPERATION}
     * 
     * @param type
     *            an integer representing the type of the remote service
     */
    public void setType(int type);

    /**
     * Returns the type of the remote service
     */
    public int getType();

    /**
     * Sets the {@link ServiceContact} associated with this <code>Service</code>.
     */
    public void setServiceContact(ServiceContact serviceContact);

    /**
     * Returns the {@link ServiceContact} associated with this
     * <code>Service</code>.
     */
    public ServiceContact getServiceContact();

    /**
     * Sets the {@link SecurityContext} associated with this
     * <code>Service</code>.
     */
    public void setSecurityContext(SecurityContext securityContext);

    /**
     * Returns the {@link SecurityContext} ssociated with this
     * <code>Service</code>.
     */
    public SecurityContext getSecurityContext();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    /**
     * @deprecated Use {@link getAttributeNames} 
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAllAttributes();
    
    public Collection<String> getAttributeNames();

    public void removeAttribute(String name);
}
