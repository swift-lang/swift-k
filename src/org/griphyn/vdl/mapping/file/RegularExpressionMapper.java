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


package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.GenericMappingParamSet;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Types;

public class RegularExpressionMapper extends AbstractMapper {	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(RegularExpressionMapperParams.NAMES);
        super.getValidMappingParams(s);
    }

	public RegularExpressionMapper() {
	}	

	@Override
    protected MappingParamSet newParams() {
        return new RegularExpressionMapperParams();
    }

    @Override
    public String getName() {
        return "RegexpMapper";
    }

    @Override
    public void initialize(RootHandle root) {
		super.initialize(root);
		RegularExpressionMapperParams cp = getParams();
		if (!cp.getSource().getType().isPrimitive()) {
            throw new IllegalArgumentException("Non-primitive value specified for 'source';" +
            		" maybe you meant filename(" + cp.getSource().toString() + ")?");
        }
		else if (!Types.STRING.equals(cp.getSource().getType())) {
		    throw new IllegalArgumentException("'source' parameter must be a string");
		}
	}

	public Collection<Path> existing() {
		try {
            if (exists(Path.EMPTY_PATH))
            	return Arrays.asList(new Path[] { Path.EMPTY_PATH });
            else {
            	return Collections.emptyList();
            }
        }
        catch (InvalidPathException e) {
            throw new RuntimeException("Unexpected error", e);
        }
	}
	
	@Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

	public PhysicalFormat map(Path path) {
	    RegularExpressionMapperParams cp = getParams();

	    String source = (String) cp.getSource().getValue();
	    Pattern p = Pattern.compile(cp.getMatch());
		Matcher m = p.matcher(source);
		
		if (!m.find()) {
			throw new RuntimeException("No match found! source='" + source + 
			    "' match = '" + cp.getMatch() + "'");
		}
		// find group number to replace
		// What. The. Flywheel.
		Pattern p2 = Pattern.compile("(\\\\\\d)");
		Matcher m2 = p2.matcher(cp.getTransform());
		StringBuffer sb = new StringBuffer();
		while (m2.find()) {
		    String group = m2.group(1);
			int g = Integer.parseInt(group.substring(1));
            try { 
                m2.appendReplacement(sb, m.group(g));
            }
            catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("regexp_mapper error: No group: \\\\" + g);
            }
		}
		m2.appendTail(sb);
		return newFile(sb.toString());
	}

	public boolean isStatic() {
		return true;
	}

	public static void main(String[] args) {
		RegularExpressionMapper reMapper = new RegularExpressionMapper();
		GenericMappingParamSet params = new GenericMappingParamSet("regex");
		params.put("source", "2mass-j1223.fits");
		params.put("match", "(.*)\\.(.*)");
		params.put("transform", "\\1_area.\\2");
		reMapper.setParameters(params);
		System.out.println(reMapper.map(Path.EMPTY_PATH));
	}
}
