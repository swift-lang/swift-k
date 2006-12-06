/*
 * Created on Sep 20, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;

public class ConcurrentMapper extends SimpleFileMapper {
	public static final String PARAM_THREAD_PREFIX = "thread_prefix";

	public void setParams(Map params) {
		String prefix = (String) params.get(PARAM_PREFIX);
		if (prefix == null) {
			prefix = "";
		}
		prefix = prefix + '-' + params.get(PARAM_THREAD_PREFIX);
		params.put(PARAM_PREFIX, prefix);
		super.setParams(params);
	}
}
