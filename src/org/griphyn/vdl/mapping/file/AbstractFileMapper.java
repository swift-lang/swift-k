/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public abstract class AbstractFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);
	public static final MappingParam PARAM_SUFFIX = new MappingParam("suffix", null);
	public static final MappingParam PARAM_PATTERN = new MappingParam("pattern", null);
	public static final MappingParam PARAM_LOCATION = new MappingParam("location", null);

	protected FileNameElementMapper elementMapper;

	protected AbstractFileMapper(FileNameElementMapper elementMapper) {
		this.elementMapper = elementMapper;
	}

	protected AbstractFileMapper() {
		this(null);
	}
	
	protected FileNameElementMapper getElementMapper() {
		return elementMapper;
	}

	protected void setElementMapper(FileNameElementMapper elementMapper) {
		this.elementMapper = elementMapper;
	}

	public void setParams(Map params) {
		super.setParams(params);
		if (PARAM_SUFFIX.isPresent(this)) {
			String suffix = PARAM_SUFFIX.getStringValue(this);
			if (!suffix.startsWith(".")) {
				PARAM_SUFFIX.setValue(this, "." + suffix);
			}
		}
		if (PARAM_PATTERN.isPresent(this)) {
			String pattern = PARAM_PATTERN.getStringValue(this);
			PARAM_PATTERN.setValue(this, replaceWildcards(pattern));
		}
	}

	public String map(Path path) {
		StringBuffer sb = new StringBuffer();
		final String location = PARAM_LOCATION.getStringValue(this);
		final String prefix = PARAM_PREFIX.getStringValue(this);
		final String suffix = PARAM_SUFFIX.getStringValue(this);
		maybeAppend(sb, location);
		if (location != null && !location.endsWith("/")) {
			sb.append('/');
		}
		if (prefix != null) {
			path = path.addFirst(prefix);
		}

		Iterator pi = path.iterator();
		int level = 0, tokenCount = path.size();
		while (pi.hasNext()) {
			String token = ((Path.Entry) pi.next()).getName();
			if (!pi.hasNext()) {
				sb.append(getElementMapper().mapField(token));
			}
			else {
				if (Character.isDigit(token.charAt(0))) {
					sb.append(getElementMapper().mapIndex(Integer.parseInt(token)));
				}
				else {
					sb.append(getElementMapper().mapField(token));
				}
				if (level < tokenCount - 2) {
					sb.append(getElementMapper().getSeparator(level));
				}
				else {
					sb.append('.');
				}
			}
			level++;
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}

	public boolean exists(Path path) {
		File f = new File(map(path));
		return f.exists();
	}

	public Collection existing() {
		final String location = PARAM_LOCATION.getStringValue(this);
		final String prefix = PARAM_PREFIX.getStringValue(this);
		final String suffix = PARAM_SUFFIX.getStringValue(this);
		final String pattern = PARAM_PATTERN.getStringValue(this);
		List l = new ArrayList();
		final File f;
		if (location == null) {
			f = new File(".");
		}
		else {
			f = new File(location);
		}
		File[] files = f.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return f.equals(dir) && (prefix == null || name.startsWith(prefix))
						&& (suffix == null || name.endsWith(suffix))
						&& (pattern == null || name.matches(pattern));
			}
		});
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				Path p = rmap(files[i].getName());
				if (p != null) {
					l.add(p);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Directory not found: " + location);
		}
		return l;
	}

	public Path rmap(String name) {
		Path path = Path.EMPTY_PATH;
		int level = 0;
		while (true) {
			int index = name.indexOf(getElementMapper().getSeparator(level));
			if (index == -1) {
				String[] e = name.split("\\.");
				if (e.length != 2) {
					return rmapElement(path, name);
				}
				else {
					if (e[0].length() * e[1].length() == 0) {
						return null;
					}
					path = rmapElement(path, e[0]);
					path = rmapElement(path, e[1]);
					return path;
				}
			}
			else {
				path = rmapElement(path, name.substring(0, index));
				name = name.substring(index + 1);
			}
			level++;
		}

	}

	protected Path rmapElement(Path path, String e) {
		if (Character.isDigit(e.charAt(0))) {
			return path.addLast(String.valueOf(getElementMapper().rmapIndex(e)), true);
		}
		else {
			return path.addLast(getElementMapper().rmapField(e));
		}
	}

	private void maybeAppend(StringBuffer sb, Object obj) {
		if (obj != null) {
			sb.append(obj.toString());
		}
	}

	public String getLocation() {
		return PARAM_LOCATION.getStringValue(this);
	}

	public String getPrefix() {
		return PARAM_PREFIX.getStringValue(this);
	}

	public static String replaceWildcards(String wild) {
		StringBuffer buffer = new StringBuffer();

		char[] chars = wild.toCharArray();

		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '*')
				buffer.append(".*");
			else if (chars[i] == '?')
				buffer.append(".");
			else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
				buffer.append('\\').append(chars[i]);
			else
				buffer.append(chars[i]);
		}

		return buffer.toString();

	}
	
	public boolean isStatic() {
		return false;
	}
}
