/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2013-2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;


public class SingleFileMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("file");

	private Object file;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public AbsFile getFile() {
		return (AbsFile) file;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("file")) {
			this.file = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(file)) {
			return (AbstractDataNode) file;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "file", file);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives(AbstractMapper m) {
		if (file == null) {
			throw new IllegalArgumentException("Missing required argument 'file'");
		}
		file = m.newFile((String) unwrap(file, String.class));
		super.unwrapPrimitives(m);
	}


}
