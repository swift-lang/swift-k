/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.Set;

import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;

public class SimpleFileMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_PADDING = new MappingParam("padding", new Integer(4));
	
	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    addParams(s, PARAM_PADDING);
        super.getValidMappingParams(s);
    }

	public SimpleFileMapper() {
		super();
	}

	public void setParams(MappingParamSet params) {
		super.setParams(params);
		int precision = PARAM_PADDING.getIntValue(this);
		setElementMapper(new DefaultFileNameElementMapper(precision));
	}
}
