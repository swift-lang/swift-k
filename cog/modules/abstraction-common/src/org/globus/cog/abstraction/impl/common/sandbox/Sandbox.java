// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 23, 2004
 */
package org.globus.cog.abstraction.impl.common.sandbox;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Sandbox {
	private static Logger logger = Logger.getLogger(Sandbox.class);

	private ClassLoader loader;
	private Map threadLoaders;

	public Sandbox(ClassLoader loader, String name) {
		this.loader = loader;
		threadLoaders = new HashMap();
	}

	private synchronized void install() {
		Thread crt = Thread.currentThread();
		threadLoaders.put(crt, crt.getContextClassLoader());
		crt.setContextClassLoader(loader);
	}

	private synchronized void uninstall() {
		Thread crt = Thread.currentThread();
		ClassLoader cl = (ClassLoader) threadLoaders.remove(crt);
		crt.setContextClassLoader(cl);
	}

	public Object newObject(final String className, final Class[] argTypes, final Object[] args)
			throws Throwable {
		return wrap(new Wrapped() {
			public Object run() throws Throwable {
				Class cls = loader.loadClass(className);
				if (argTypes == null) {
					return cls.newInstance();
				}
				else {
					Constructor cons = cls.getConstructor(argTypes);
					return cons.newInstance(args);
				}
			}
		});
	}

	public Object invoke(final Object target, final String methodName, final Class[] argTypes,
			final Object[] args) throws Throwable {
		return wrap(new Wrapped() {
			public Object run() throws Throwable {
				Class cls = target.getClass();
				Method method = cls.getMethod(methodName, argTypes);
				return method.invoke(target, args);
			}
		});
	}

	public Object invokeStatic(final String className, final String methodName,
			final Class[] argTypes, final Object[] args) throws Throwable {
		return wrap(new Wrapped() {
			public Object run() throws Throwable {
				Class cls = loader.loadClass(className);
				Method method = cls.getMethod(methodName, argTypes);
				return method.invoke(null, args);
			}
		});
	}

	public static interface Wrapped {
		public Object run() throws Throwable;
	}

	protected Object wrap(Wrapped r) throws Throwable {
		try {
			install();
			return r.run();
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (logger.isDebugEnabled()) {
				logger.debug(t);
			}
			throw t;
		}
		catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			throw e;
		}
		finally {
			uninstall();
		}
	}

	public void boot(String bootClassName) throws Throwable {
		invokeStatic(bootClassName, "boot", null, null);
	}
}