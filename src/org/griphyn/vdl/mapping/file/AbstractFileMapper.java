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

import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;

public abstract class AbstractFileMapper implements Mapper {
	public static final String PARAM_PREFIX = "prefix";
	public static final String PARAM_SUFFIX = "suffix";
	public static final String PARAM_PATTERN = "pattern";
	public static final String PARAM_LOCATION = "location";

	protected Map params;
	protected FileNameElementMapper elementMapper;
	protected String prefix=null, suffix=null, pattern=null, location=null;

	protected AbstractFileMapper(FileNameElementMapper elementMapper) {
		this.elementMapper = elementMapper;
	}

	protected AbstractFileMapper() {
		this(null);
	}

	public void setParams(Map params) {
		this.params = params;
		if (params.get(PARAM_PREFIX)!=null) {
			prefix = String.valueOf(params.get(PARAM_PREFIX));
		}
		if (params.get(PARAM_SUFFIX)!=null) {
			suffix = String.valueOf(params.get(PARAM_SUFFIX));
			if (!suffix.startsWith("."))
				suffix = "." + suffix;
		}
		if (params.get(PARAM_PATTERN)!=null) {
			pattern = String.valueOf(params.get(PARAM_PATTERN));
			pattern = replaceWildcards(pattern);
		}
		if (params.get(PARAM_LOCATION)!=null) {
            // ?? This check is unnecessary
			location = (String) params.get(PARAM_LOCATION);
		}
	}
	
	protected FileNameElementMapper getElementMapper() {
		return elementMapper;
	}

	protected void setElementMapper(FileNameElementMapper elementMapper) {
		this.elementMapper = elementMapper;
	}

	public String map(Path path) {
		StringBuffer sb = new StringBuffer();
		maybeAppend(sb, location);
		if (location != null && !location.endsWith("/")) {
			sb.append('/');
		}
		if (prefix != null)
			path = path.addFirst(prefix);

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
		if (suffix != null)
			sb.append(suffix);
		return sb.toString();
	}

	public boolean exists(Path path) {
		File f = new File(map(path));
		return f.exists();
	}

	public Collection existing() {
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
		return location;
	}

	public Map getParams() {
		return params;
	}

	public String getPrefix() {
		return prefix;
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
