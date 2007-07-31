package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

import org.griphyn.vdl.type.Field;

public class StructuredRegularExpressionMapper extends AbstractFileMapper {
	public static final MappingParam PARAM_SOURCE = new MappingParam("source");
	public static final MappingParam PARAM_MATCH = new MappingParam("match");
	public static final MappingParam PARAM_TRANSFORM = new MappingParam("transform");

	public StructuredRegularExpressionMapper() {
	}

	public void setParams(Map params) {
		super.setParams(params);
		if (!PARAM_MATCH.isPresent(this)) {
			throw new RuntimeException("Missing parameter match!");
		}
	}

	public Collection existing() {

		DSHandle sourceHandle = (DSHandle)PARAM_SOURCE.getRawValue(this);

		Collection output = new ArrayList();
		Collection sourceFields;
		try {
			sourceFields = sourceHandle.getFields(Path.parse("[*]"));
		} catch(InvalidPathException ipe) {
			return Collections.EMPTY_LIST;
		} catch(HandleOpenException hoe) {
			throw new RuntimeException("Handle open. Throwing this exception may not be the right thing to do. TODO");
		}
		Iterator i = sourceFields.iterator();
		while(i.hasNext()) {
			DSHandle f = (DSHandle) i.next();
			output.add(f.getPathFromRoot());
		}

		return output;
	}

	public String map(Path path) {
		String match = PARAM_MATCH.getStringValue(this);
		String transform = PARAM_TRANSFORM.getStringValue(this);

		DSHandle sourceHandle = (DSHandle)PARAM_SOURCE.getRawValue(this);
		DSHandle hereHandle;
		try {
			hereHandle = sourceHandle.getField(path);
		} catch(InvalidPathException ipe) {
			throw new RuntimeException("Cannot get requested path "+path+" from source data structure");
		}

		String source = hereHandle.getFilename();
		Pattern p = Pattern.compile(match);
		Matcher m = p.matcher(source);
		if (!m.find()) {
			throw new RuntimeException("No match found! source='" + source + "' match = '" + match
					+ "'");
		}
		// find group number to replace
		Pattern p2 = Pattern.compile("(\\\\\\d)");
		Matcher m2 = p2.matcher(transform);
		StringBuffer sb = new StringBuffer();
		while (m2.find()) {
			String group = m2.group(1);
			int g = Integer.parseInt(group.substring(1));
			m2.appendReplacement(sb, m.group(g));
		}
		m2.appendTail(sb);
		return sb.toString();
	}

	public Path rmap(String name) {
		return Path.EMPTY_PATH;
	}

	public boolean isStatic() {
		return true;
	}

}
