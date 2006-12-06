/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Map;

public class SimpleFileMapper extends AbstractFileMapper {

	public SimpleFileMapper() {
		super();
	}

	public void setParams(Map params) {
		super.setParams(params);
		setElementMapper(new DefaultFileNameElementMapper());
	}
}
