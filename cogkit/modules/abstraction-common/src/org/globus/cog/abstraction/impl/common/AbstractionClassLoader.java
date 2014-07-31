/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;

public class AbstractionClassLoader extends URLClassLoader {

	private static Logger logger = Logger.getLogger(AbstractionClassLoader.class);

	private static Map<String, ClassLoader> loaders = new HashMap<String, ClassLoader>();

	private String[] prefixes;

	private String[] excludes;

	private HelperClassLoader helper;

	private String name;

	private WeakHashMap<String, Class<?>> sysclasses;

	private static ClassLoader extClassLoader = ClassLoader.getSystemClassLoader().getParent();

	public static boolean isLoaderInitialized(String provider) {
		if (provider == null) {
			return true;
		}
		return loaders.containsKey(provider.toLowerCase());
	}

	public static ClassLoader getClassLoader(String provider) throws InvalidProviderException {
		if (provider == null) {
			return AbstractionClassLoader.class.getClassLoader();
		}
		if (!loaders.containsKey(provider.toLowerCase())) {
			throw new InvalidProviderException("Invalid provider: " + provider);
		}
		return loaders.get(provider);
	}

	public static void setLoader(String provider, ClassLoader loader) {
		loaders.put(provider, loader);
	}

	public static void initializeLoader(String loaderName, String loaderProps, String bootClass,
			boolean system) {
		if (loaderName == null) {
			return;
		}
		loaderName = loaderName.toLowerCase();
		ClassLoader prev = AbstractionClassLoader.class.getClassLoader();
		if ((loaderProps == null) || system) {
			logger.debug("Using system class loader for provider " + loaderName);
			loaders.put(loaderName, prev);
			boot(prev, bootClass);
			return;
		}
		logger.debug("Instantiating new abstraction class loader for provider " + loaderName);
		URL[] classpath;
		if (prev instanceof URLClassLoader) {
			classpath = ((URLClassLoader) prev).getURLs();
		}
		else {
			classpath = getURLs(System.getProperty("java.class.path", "."));
		}

		URL providerLibs = prev.getResource(loaderProps);
		if (providerLibs != null) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						providerLibs.openStream()));

				List<URL> jars = new ArrayList<URL>();
				List<String> pref = new ArrayList<String>();
				List<String> excl = new ArrayList<String>();
				String line;
				String relpath = null;
				String ref = null;

				while ((line = br.readLine()) != null) {
					String[] prop = AbstractionProperties.Property.splitProperty(line.trim());

					if (prop[0].equals("relative")) {
						String relref = prop[1];
						for (int i = 0; i < classpath.length; i++) {
							if (classpath[i].getPath().lastIndexOf(relref) != -1) {
								String path = classpath[i].toExternalForm();
								int index = path.lastIndexOf(relref);
								ref = path.substring(0, index);
								logger.debug("Found relative reference " + ref);
								break;
							}
						}
						if (ref == null) {
							if (!relref.endsWith(".jar")) {
								relref = relref + "*.jar";
							}
							throw new Error("Invalid loader configuration for " + loaderName
									+ ". The relative property is invalid. Please make sure that "
									+ relref + " is in your class path.");
						}
						else {
							logger.debug("Using relative loading: " + prop[1]);
						}
					}
					if (prop[0].equals("absolute")) {
						ref = prop[1];
						logger.debug("Using absolute loading: " + prop[1]);
					}
					if (prop[0].equals("package")) {
						pref.add(prop[1]);
						logger.debug("Adding prefix " + prop[1]);
					}
					if (prop[0].equals("exclude")) {
						excl.add(prop[1]);
						logger.debug("Adding exclude " + prop[1]);
					}
					if (prop[0].equals("jar")) {
						boolean found = false;
						for (int i = 0; i < classpath.length; i++) {
							if (classpath[i].getPath().endsWith("/" + prop[1])) {
								jars.add(classpath[i]);
								found = true;
								logger.debug("Adding file " + classpath[i]);
								break;
							}
						}
						if (!found) {
							logger.warn("Provider jar not found in classpath: " + prop[1]);
						}
					}
					if (prop[0].equals("rjar")) {
						if (ref == null) {
							logger.warn("rjar specified but relative disabled/not found");
						}
						else {
							jars.add(new URL(ref + "/" + prop[1]));
							logger.debug("Adding relative jar " + ref + "/" + prop[1]);
						}
					}
				}

				AbstractionClassLoader ccl = new AbstractionClassLoader(
						pref.toArray(new String[0]),
						excl.toArray(new String[0]), jars.toArray(new URL[0]),
						prev, classpath, loaderName);
				loaders.put(loaderName, ccl);
				boot(ccl, bootClass);
				return;
			}
			catch (IOException e) {
				logger.debug("Exception caugh while opening library list", e);
			}
		}
		logger.warn("Provider libraries list (abstraction." + loaderName.toLowerCase()
				+ ".libs) not found. Using default class loader.");
		loaders.put(loaderName, prev);
		boot(prev, bootClass);
	}

	private static void boot(ClassLoader cl, String bootClass) {
		if (bootClass != null) {
			try {
				Class<?> cls = cl.loadClass(bootClass);
				Method method = cls.getMethod("boot", new Class[0]);
				if (Modifier.isStatic(method.getModifiers())) {
					method.invoke(null, new Object[0]);
				}
			}
			catch (Exception e) {
				logger.debug(e);
			}
		}
	}

	private static URL[] getURLs(String classpath) {
		StringTokenizer st = new StringTokenizer(classpath, ":");
		List<URL> l = new LinkedList<URL>();
		while (st.hasMoreTokens()) {
			try {
				String path = st.nextToken();
				File f = new File(path);
				if (!f.exists()) {
					logger.debug("Invalid path in classpath: " + f);
				}
				else {
					l.add(new URL("file:///" + f.getAbsolutePath()));
				}
			}
			catch (Exception e) {
				logger.debug(e);
			}
		}
		return l.toArray(new URL[0]);
	}

	public AbstractionClassLoader(String[] prefixes, String[] excludes, URL[] urls,
			ClassLoader parent, URL[] classpath, String name) {
		super(urls, parent);
		this.prefixes = prefixes;
		this.excludes = excludes;
		this.name = name;
		helper = new HelperClassLoader(classpath, this);
		sysclasses = new WeakHashMap<String, Class<?>>();
	}

	public URL getResource(String name) {
		if (name.endsWith(".class")) {
			URL url = super.getResource(name);
			return url;
		}
		else {
			URL url = findResource(name);
			if (url == null) {
				url = super.findResource(name);
			}
			return url;
		}
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class<?> c = null;
		try {
			c = extClassLoader.loadClass(name);
			// boot or extension class
			// logger.debug(this.name + ": bootclass " + name + " loaded.");
			return c;
		}
		catch (Exception e) {
		}

		boolean exclude = false;
		boolean prefix = false;

		for (int i = 0; i < excludes.length; i++) {
			if (name.startsWith(excludes[i])) {
				exclude = true;
				break;
			}
		}

		if (!exclude) {
			try {
				c = super.findLoadedClass(name);
				if (c == null) {
					c = super.findClass(name);
					if (c == null) {
						// logger.debug("Class not found: "+name);
					}
				}
				return c;
			}
			catch (Exception e) {
			}

			for (int i = 0; i < prefixes.length; i++) {
				if (name.startsWith(prefixes[i])) {
					prefix = true;
					break;
				}
			}
		}

		if (!prefix && (getParent() != null) && !exclude) {
			Class<?> cls = sysclasses.get(name);
			if (cls == null) {
				// logger.debug(this.name + ": sysclass " + name + " loaded.");
				cls = getParent().loadClass(name);
				sysclasses.put(name, cls);
			}
			return cls;
		}
		else {
			// logger.debug(this.name + ": prefixclass " + name + " loaded.");
			return helper.loadClass2(name);
		}
	}

	private class HelperClassLoader extends URLClassLoader {

		public HelperClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public URL getResource(String name) {
			if (name.endsWith(".class")) {
				// already dealt with
				return super.getResource(name);
			}
			else {
				URL url = getParent().getResource(name);
				if (url == null) {
					url = super.getResource(name);
				}
				return url;
			}
		}

		public Class<?> loadClass2(String name) throws ClassNotFoundException {
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				c = findClass(name);
			}
			return c;
		}

		public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			return getParent().loadClass(name);
		}
	}
}