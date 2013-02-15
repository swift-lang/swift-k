//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 20, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class RestrictedClassMapper extends MapperWrapper {
	private static final Logger logger = Logger.getLogger(RestrictedClassMapper.class);

	public static final String PROPERTIES = "karajan-restricted-classes.properties";

	private final ClassMapper wrapped;
	private static Set allowedClasses, disallowedClasses, allowedPackages;

	public RestrictedClassMapper(ClassMapper wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
		synchronized (RestrictedClassMapper.class) {
			if (allowedPackages == null) {
				allowedPackages = new HashSet();
				allowedClasses = new HashSet();
				disallowedClasses = new HashSet();
				loadProperties();
			}
		}
	}

	private void loadProperties() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES);
		if (is == null) {
			throw new SecurityException("Could find class deserialization restrictions");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("#") && !(line.length() == 0)) {
					int index = line.indexOf('=');
					if (index == -1) {
						logger.warn("Invalid line in " + PROPERTIES + ": " + line);
					}
					else {
						String type = line.substring(0, index).trim().toLowerCase();
						String value = line.substring(index + 1).trim().toLowerCase();
						if (type.equals("package.allow")) {
							allowedPackages.add(value);
						}
						else if (type.equals("class.disallow")) {
							disallowedClasses.add(value);
						}
						else {
							logger.warn("Unrecognized property name (" + type + ") in "
									+ PROPERTIES + ", line " + line);
						}
					}
				}
				line = br.readLine();
			}
			br.close();
		}
		catch (Exception e) {
			throw new SecurityException("Could not load class deserialization restrictions");
		}
	}

	protected Class checkClass(Class cls) {
		if (cls == null) {
			return null;
		}
		String name = cls.getName();
		synchronized (allowedClasses) {
			if (allowedClasses.contains(name)) {
				return cls;
			}
		}
		if (disallowedClasses.contains(name)) {
			throw new SecurityException("Deserialization of class " + name + " is not allowed");
		}
		Iterator i = allowedPackages.iterator();
		while (i.hasNext()) {
			String packageName = (String) i.next();
			if (name.startsWith(packageName)) {
				synchronized (allowedClasses) {
					allowedClasses.add(name);
				}
				return cls;
			}
		}
		return cls;
	}

	public Class lookupType(String elementName) {
		return checkClass(super.lookupType(elementName));
	}

	public Class lookupDefaultType(Class baseType) {
		return checkClass(super.lookupDefaultType(baseType));

	}

	public Class getOverrideRootType() {
		return checkClass(super.getOverrideRootType());
	}

	public Class realClass(String elementName) {
		return checkClass(super.realClass(elementName));
	}

	public Class defaultImplementationOf(Class type) {
		return checkClass(super.defaultImplementationOf(type));
	}

	public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType,
			String fieldName) {
		ImplicitCollectionMapping mapping = super.getImplicitCollectionDefForFieldName(itemType,
				fieldName);
		checkClass(mapping.getItemType());
		return mapping;
	}

	public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) {
		return checkClass(super.getItemTypeForItemFieldName(definedIn, itemFieldName));
	}
}
