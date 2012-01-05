// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 17, 2005
 */
package org.globus.cog.karajan.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class AtomMapping {
	private Map map;

	public AtomMapping(String mappingFile) {
		load(mappingFile);
	}

	public final void load(String mappingFile) {
		Properties props = new Properties();
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream(mappingFile);
			if (is == null) {
				is = new FileInputStream(mappingFile);
			}
			props.load(is);
			map = new Hashtable();
			Iterator i = props.keySet().iterator();
			while (i.hasNext()) {
				String name = (String) i.next();
				String cls = props.getProperty(name);
				try {
					map.put(name, getClass().getClassLoader().loadClass(cls));
				}
				catch (ClassNotFoundException e) {
					throw new GrammarException("Invalid atom class (" + cls + ") for atom " + name);
				}
			}
		}
		catch (IOException e) {
			throw new GrammarException("Could not load atom mapping file: " + mappingFile, e);
		}
	}

	public Class get(String value) {
		return (Class) map.get(value);
	}
}