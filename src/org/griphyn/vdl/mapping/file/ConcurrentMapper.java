/*
 * Created on Sep 20, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;

import org.griphyn.vdl.mapping.MappingParam;

public class ConcurrentMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_THREAD_PREFIX = new MappingParam("thread_prefix", "");

	public ConcurrentMapper() {
		super(new ConcurrentElementMapper());
	}

	public void setParams(Map params) {
		String prefix = (String) PARAM_PREFIX.getValue(params);
		prefix = "_concurrent/" + prefix + "-" + PARAM_THREAD_PREFIX.getValue(params);
		PARAM_PREFIX.setValue(params, prefix);
		super.setParams(params);
	}
}

