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

import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.RootHandle;

public class SimpleFileMapper extends AbstractFileMapper {

    @Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(SimpleFileMapperParams.NAMES);
	    super.getValidMappingParams(s);
    }

	public SimpleFileMapper() {
		super();
	}

	@Override
    public MappingParamSet newParams() {
        return new SimpleFileMapperParams();
    }

    @Override
    public String getName() {
        return "SimpleMapper";
    }

    public void initialize(RootHandle root) {
		super.initialize(root);
		SimpleFileMapperParams cp = getParams();
		setElementMapper(new DefaultFileNameElementMapper(cp.getPadding()));
	}
}
