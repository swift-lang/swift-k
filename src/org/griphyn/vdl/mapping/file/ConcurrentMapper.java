/*
 * Created on Sep 20, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;

import org.griphyn.vdl.mapping.MappingParam;

public class ConcurrentMapper extends SimpleFileMapper {
	public static final MappingParam PARAM_THREAD_PREFIX = new MappingParam("thread_prefix");

	public void setParams(Map params) {
		String prefix = (String) PARAM_PREFIX.getValue(params);
		if (prefix == null) {
			prefix = "";
		}
		prefix = prefix + '-' + PARAM_THREAD_PREFIX.getValue(params);
		PARAM_PREFIX.setValue(params, prefix);
		super.setParams(params);
	}
}
