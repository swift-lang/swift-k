/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;

import org.griphyn.vdl.mapping.MappingParam;

public class SimpleFileMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_PADDING = new MappingParam("padding", new Integer(4));

	public SimpleFileMapper() {
		super();
	}

	public void setParams(Map params) {
		super.setParams(params);
		int precision = PARAM_PADDING.getIntValue(this);
		setElementMapper(new DefaultFileNameElementMapper(precision));
	}
}
