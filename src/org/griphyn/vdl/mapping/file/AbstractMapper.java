/*
 * Created on Jan 8, 2007
 */
package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.Mapper;

public abstract class AbstractMapper implements Mapper {

	protected Map params;

	public synchronized void setParam(String name, Object value) {
		if (params == null) {
			params = new HashMap();
		}
		params.put(name, value);
	}

	public synchronized Object getParam(String name) {
		if (params != null) {
			return params.get(name);
		}
		else {
			return null;
		}
	}

	public void setParams(Map params) {
		this.params = params;
	}


}
