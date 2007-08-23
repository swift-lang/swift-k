// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.sandbox.Sandbox;
import org.globus.cog.abstraction.impl.common.sandbox.SandboxingTaskHandler;
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

    private static final String INVALID_CLASSPATH_ERROR = "The class path configuration is invalid. The CoG Kit uses "
            + "separate class loaders for possibly conflicting libraries "
            + "in different providers. These libraries can be found in "
            + "directories named \"lib-<provider-name>\" and they must NOT "
            + "be added to the system class path, in order to not interfere "
            + "with the loading mechanism. It is however required that "
            + "the \"lib-<provider-name>\" directories be placed in the same"
            + " place as the \"lib\" directory, since the CoG class loader "
            + "attempts to find these libraries relatively to the 'lib' directory.";

    private static Logger logger = Logger.getLogger(AbstractionFactory.class);

    private static Hashtable sandboxes = new Hashtable();

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
        boolean sandbox = providerProps.getBooleanProperty(
                AbstractionProperties.SANDBOX, false);
        if (sandbox) {
            return new SandboxingTaskHandler(getSandbox(provider),
                    (TaskHandler) newObject(provider, type));
        }
        else {
            return (TaskHandler) newObject(provider, type);
        }
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
        if (useSandbox) {
            Sandbox sandbox = getSandbox(provider);
            try {
                return sandbox.newObject(className, null, null);
            }
            catch (Throwable e) {
                throw new InvalidClassException("Cannot instantiate "
                        + className, e);
            }
        }
        else {
            ClassLoader cl = getLoader(provider);
            try {
                return cl.loadClass(className).newInstance();
            }
            catch (Exception e) {
                throw new InvalidClassException(e);
            }
        }
    }

    private static synchronized Sandbox getSandbox(String provider)
            throws InvalidProviderException {
        if (!trapChecked) {
            URL url = ClassLoader.getSystemClassLoader()
                    .getResource("cog.trap");
            if (url != null) {
                String path = null;
                try {
                    String surl = ClassLoader.getSystemClassLoader()
                            .getResource("cog.trap").toString();
                    surl = surl.substring("jar:file:".length(), surl
                            .indexOf('!'));
                    surl = surl.substring(0, surl.lastIndexOf('/'));
                    path = surl;
                }
                catch (Exception e) {
                }
                if (path == null) {
                    throw new InvalidProviderException(INVALID_CLASSPATH_ERROR);
                }
                else {
                    throw new InvalidProviderException(INVALID_CLASSPATH_ERROR
                            + "\nOffending directory: " + path);
                }
            }
            else {
                trapChecked = true;
            }
        }
        provider = provider.toLowerCase();
        Sandbox sandbox = (Sandbox) sandboxes.get(provider);
        if (sandbox == null) {
            sandbox = new Sandbox(getLoader(provider, false), provider
                    + " sandbox");
            sandboxes.put(provider, sandbox);
            String bootClass = AbstractionProperties.getProperties(provider)
                    .getProperty(AbstractionProperties.SANDBOX_BOOTCLASS);
            if (bootClass != null) {
                try {
                    sandbox.boot(bootClass);
                }
                catch (Throwable e) {
                    logger.error("Cannot boot sandbox for " + provider
                            + ". Some things may not work as expected.", e);
                }
            }
        }
        return sandbox;
    }

    protected static ClassLoader getLoader(String provider)
            throws InvalidProviderException {
        return getLoader(provider.toLowerCase(), true);
    }

    protected static ClassLoader getLoader(String provider, boolean doBoot)
            throws InvalidProviderException {
        AbstractionProperties providerProps = AbstractionProperties
                .getProperties(provider);
        String loader = providerProps
                .getProperty(AbstractionProperties.CLASS_LOADER_NAME);
        if (loader == null) {
            loader = provider;
        }
        if (!AbstractionClassLoader.isLoaderInitialized(provider)) {
            String props = providerProps
                    .getProperty(AbstractionProperties.CLASS_LOADER_PROPERTIES);
            String boot = providerProps
                    .getProperty(AbstractionProperties.CLASS_LOADER_BOOTCLASS);
            boolean system = providerProps.getBooleanProperty(
                    AbstractionProperties.CLASS_LOADER_USESYSTEM, true);
            ClassLoader cl;
            if (doBoot) {
                AbstractionClassLoader.initializeLoader(loader, props, boot,
                        system);
            }
            else {
                AbstractionClassLoader.initializeLoader(loader, props, null,
                        system);
            }
        }

        return AbstractionClassLoader.getClassLoader(loader);
    }

    public static void executeRunnable(String provider, String className)
            throws InvalidProviderException, ProviderMethodException {
        ((Runnable) newObject(provider.toLowerCase(), className)).run();
    }

    public static void runApplication(String provider, String className,
            String[] args) throws NoSuchMethodException, NoSuchMethodException,
            ClassNotFoundException, InvalidProviderException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        ClassLoader cl = getLoader(provider.toLowerCase());
        Class c = cl.loadClass(className);
        Method m = c.getMethod("main", new Class[] { String[].class });
        m.invoke(null, new Object[] { args });
    }
}