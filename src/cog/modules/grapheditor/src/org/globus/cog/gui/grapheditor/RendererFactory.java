// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/* 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class RendererFactory {
	private static Logger logger = Logger.getLogger(RendererFactory.class);
	private static HashMap classRenderers;
	private static ClassTargetPair tmp;
	private static HashMap rootContainers;
	private static String defaultTarget = "swing";
	private static String currentTarget;

	static {
		classRenderers = new HashMap();
		rootContainers = new HashMap();
		tmp = new ClassTargetPair(null, null);
		currentTarget = defaultTarget;
	}

	public static void addClassRenderer(Class cls, Class renderer) {
		addClassRenderer(cls, defaultTarget, renderer);
	}

	public static synchronized void addClassRenderer(Class cls, String target, Class renderer) {
		classRenderers.put(new ClassTargetPair(cls, target), renderer);
	}

	public static synchronized Class getClassRenderer(Class cls, String target) {
		tmp.cls = cls;
		tmp.target = target;
		while (tmp.cls != Object.class) {
			if (classRenderers.containsKey(tmp)) {
				return (Class) classRenderers.get(tmp);
			}
			else {
				tmp.cls = tmp.cls.getSuperclass();
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Class renderers: " + classRenderers);
		}
		throw new NoSuchRendererException(cls.toString() + " - " + target);
	}

	public static Class getClassRenderer(Class cls) {
		return getClassRenderer(cls, currentTarget);
	}

	public static void addRootContainer(String target, Class cls) {
		rootContainers.put(target, cls);
	}

	public static void setDefaultRootContainer(Class cls) {
		addRootContainer(defaultTarget, cls);
	}

	public static Class getRootContainerClass() {
		return getRootContainerClass(currentTarget);
	}

	public static synchronized Class getRootContainerClass(String target) {
		if (rootContainers.containsKey(target)) {
			return (Class) rootContainers.get(target);
		}
		else {
			throw new NoSuchRendererException("Root Container - " + target);
		}
	}

	public static RootContainer newRootContainer() throws RootContainerInstantiationException {
		return newRootContainer(currentTarget);
	}

	public static synchronized RootContainer newRootContainer(String target)
			throws RootContainerInstantiationException {
		try {
			Class rootContainerClass = getRootContainerClass(target);
			RootContainer rootContainer = (RootContainer) rootContainerClass.newInstance();
			return rootContainer;
		}
		catch (Exception e) {
			throw new RootContainerInstantiationException("Could not instantiate root container", e);
		}
	}

	public static String getDefaultTarget() {
		return defaultTarget;
	}

	public static void setDefaultTarget(String defaultTarget) {
		RendererFactory.defaultTarget = defaultTarget;
		logger.debug("Setting default target to " + defaultTarget);
	}

	public static String getCurrentTarget() {
		return currentTarget;
	}

	public static void setCurrentTarget(String currentTarget) {
		RendererFactory.currentTarget = currentTarget;
		logger.debug("Setting current target to " + currentTarget);
	}

}
