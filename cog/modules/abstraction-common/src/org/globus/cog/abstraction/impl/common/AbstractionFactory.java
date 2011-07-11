// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.FileOperationTaskHandler;
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * This class implements the FACTORY pattern facilitating the creation of
 * {@link org.globus.cog.abstraction.interfaces.TaskHandler}s and
 * {@link org.globus.cog.abstraction.interfaces.SecurityContext}s for the
 * required providers.
 */
public class AbstractionFactory {

    private static final String PROVIDER_PROP = "cog-provider.properties";

    private static Logger logger = Logger.getLogger(AbstractionFactory.class);

    private static boolean trapChecked;

    public static TaskHandler newExecutionTaskHandler()
            throws InvalidProviderException, ProviderMethodException {
        return new ExecutionTaskHandler();
    }

    public static TaskHandler newExecutionTaskHandler(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return newTaskHandler(provider,
                AbstractionProperties.TYPE_EXECUTION_TASK_HANDLER);
    }

    public static TaskHandler newFileTransferTaskHandler()
            throws InvalidProviderException, ProviderMethodException {
        return new FileTransferTaskHandler();
    }

    public static TaskHandler newFileTransferTaskHandler(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return newTaskHandler(provider,
                AbstractionProperties.TYPE_FILE_TRANSFER_TASK_HANDLER);
    }

    public static TaskHandler newFileOperationTaskHandler()
            throws InvalidProviderException, ProviderMethodException {
        return new FileOperationTaskHandler();
    }

    public static TaskHandler newFileOperationTaskHandler(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return newTaskHandler(provider,
                AbstractionProperties.TYPE_FILE_OPERATION_TASK_HANDLER);
    }

    public static TaskHandler newTaskHandler(String provider, String type)
            throws InvalidProviderException, ProviderMethodException {
        AbstractionProperties providerProps = AbstractionProperties
                .getProperties(provider);
        return (TaskHandler) newObject(provider, type);
    }

    /**
     * @deprecated Use the newExecutionTaskHandler(),
     *             newFileTransferTaskHandler(), or
     *             newFileOperationTaskHandler() instead
     */
    public static TaskHandler newTaskHandler(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return newExecutionTaskHandler(provider);
    }

    public static FileResource newFileResource(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return (FileResource) newObject(provider,
                AbstractionProperties.TYPE_FILE_RESOURCE);
    }

    public static SecurityContext newSecurityContext(String provider)
            throws InvalidProviderException, ProviderMethodException {
        return (SecurityContext) newObject(provider,
                AbstractionProperties.TYPE_SECURITY_CONTEXT);
    }

    public static boolean hasObject(String provider, String role) {
        try {
            AbstractionProperties providerProps = AbstractionProperties
                    .getProperties(provider);
            String className = providerProps.getProperty(role);
            return className != null;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public static Object newObject(String provider, String role)
            throws InvalidProviderException, ProviderMethodException {
        AbstractionProperties providerProps = AbstractionProperties
                .getProperties(provider);
        String className = providerProps.getProperty(role);
        boolean sandbox = providerProps.getBooleanProperty(
                AbstractionProperties.SANDBOX, false);
        if (className == null) {
            throw new ProviderMethodException("Provider " + provider
                    + " does not provide a " + role);
        }
        try {
            return newInstance(provider, className, sandbox);
        }
        catch (InvalidClassException e) {
            throw new ProviderMethodException("Provider " + provider
                    + " does not provide a valid " + role, e);
        }
    }

    protected static Object newInstance(String provider, String className,
            boolean useSandbox) throws InvalidProviderException,
            InvalidClassException {
        provider = provider.toLowerCase();
        logger
                .debug("Instantiating " + className + " for provider "
                        + provider);
        ClassLoader cl = AbstractionFactory.class.getClassLoader();
        try {
            return cl.loadClass(className).newInstance();
        }
        catch (Exception e) {
            throw new InvalidClassException(e);
        }
    }
}