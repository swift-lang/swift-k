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
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.DSHandle;


public class RegularExpressionMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("source", "match", "transform");

	private Object source;
	private Object match;
	private Object transform = null;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setSource(DSHandle source) {
		this.source = source;
	}

	public DSHandle getSource() {
		return (DSHandle) source;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getMatch() {
		return (String) match;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

	public String getTransform() {
		return (String) transform;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("source")) {
			this.source = value;
		}
		else if (name.equals("match")) {
			this.match = value;
		}
		else if (name.equals("transform")) {
			this.transform = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(source)) {
			return (AbstractDataNode) source;
		}
		else if (checkOpen(match)) {
			return (AbstractDataNode) match;
		}
		else if (checkOpen(transform)) {
			return (AbstractDataNode) transform;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "source", source);
		addParam(sb, "match", match);
		addParam(sb, "transform", transform);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		if (source == null) {
			throw new IllegalArgumentException("Missing required argument 'source'");
		}
		if (match == null) {
			throw new IllegalArgumentException("Missing required argument 'match'");
		}
		match = unwrap(match, String.class);
		transform = unwrap(transform, String.class);
		super.unwrapPrimitives();
	}


}
