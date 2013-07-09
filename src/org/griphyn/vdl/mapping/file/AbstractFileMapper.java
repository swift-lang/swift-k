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

import java.io.File;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.util.Base64;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

/** A base class to build mappers which map based on filename patterns.
  * It provides a large amount of default behaviour which can be
  * reused or overridden as necessary by subclasses.
  * <br />
  * Subclasses must specify a FileNameElementMapper, which can be the
  * Swift supplied DefaultFileNameElementMapper or an application
  * specific mapper.
  * <br />
  * The default mapping algorithm implemented by the map and rmap methods uses
  * a number of mapper parameters:
  * <ul>
  *   <li>location - if specified, then all generated filenames
  *                  will be prefixed with this directory, and all lookups
  *                  for files will happen in this directory. If not specified, 
  *                  the current directory will be used.</li>
  *   <li>prefix - if specified, then all file names will be prefixed
  *                  with this string</li>
  *   <li>suffix - if specified, then all filenames will be suffixed with
  *                  this string.</li>
  *   <li>pattern - if specified, then filenames will be selected from
  *                 the location directory when they match the unix glob
  *                 pattern supplied in this parameter.</li>
  * </ul>
  */

public abstract class AbstractFileMapper extends AbstractMapper {
	public static final MappingParam PARAM_PREFIX = new MappingParam("prefix", null);
	public static final MappingParam PARAM_SUFFIX = new MappingParam("suffix", null);
	public static final MappingParam PARAM_PATTERN = new MappingParam("pattern", null);
	public static final MappingParam PARAM_LOCATION = new MappingParam("location", null);
	
	private String location, prefix, suffix, pattern; 
	
	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    addParams(s, PARAM_PREFIX, PARAM_SUFFIX, PARAM_PATTERN, PARAM_LOCATION);
        super.getValidMappingParams(s);
    }

	public static final Logger logger = Logger.getLogger(AbstractFileMapper.class);

	protected FileNameElementMapper elementMapper;

	protected AbstractFileMapper(FileNameElementMapper elementMapper) {
		if(logger.isDebugEnabled())
			logger.debug("Creating abstract file mapper id="+this.hashCode()+" class="+this.getClass().getName());

		this.elementMapper = elementMapper;
	}
	
	

	/** Creates an AbstractFileMapper without specifying a
	  * FileNameElementMapper. The elementMapper must be specified
	  * in another way, such as through the setElementMapper method.
	  */
	protected AbstractFileMapper() {
		this(null);
	}

	protected FileNameElementMapper getElementMapper() {
		return elementMapper;
	}

	protected void setElementMapper(FileNameElementMapper elementMapper) {
		this.elementMapper = elementMapper;
	}

	public void setParams(MappingParamSet params) throws HandleOpenException {
		super.setParams(params);
		StringBuilder pattern = new StringBuilder();
		boolean wildcard = false; 
		if (PARAM_PREFIX.isPresent(this)) {
		    prefix = PARAM_PREFIX.getStringValue(this);
		    pattern.append(prefix);
		    pattern.append('*');
		    wildcard = true;
		}
		if (PARAM_PATTERN.isPresent(this)) {
            pattern.append(PARAM_PATTERN.getStringValue(this));
            wildcard = false;
        }
		if (PARAM_SUFFIX.isPresent(this)) {
			suffix = PARAM_SUFFIX.getStringValue(this);
			if (!wildcard) {
			    pattern.append('*');
			}
			pattern.append(suffix);
		}
		location = PARAM_LOCATION.getStringValue(this);
        prefix = PARAM_PREFIX.getStringValue(this);
        suffix = PARAM_SUFFIX.getStringValue(this);
        this.pattern = pattern.toString();
	}

	public PhysicalFormat map(Path path) {
		if(logger.isDebugEnabled())
			logger.debug("mapper id="+this.hashCode()+" starting to map "+path);
		StringBuffer sb = new StringBuffer();
		maybeAppend(sb, location);
		if (location != null && !location.endsWith("/")) {
			sb.append('/');
		}
		if (prefix != null) {
			sb.append(prefix);
		}

		Iterator<Path.Entry> pi = path.iterator();
		int level = 0, tokenCount = path.size();
		while (pi.hasNext()) {
			Path.Entry nextPathElement = pi.next();
			if (nextPathElement.isIndex()) {
			    Comparable<?> key = nextPathElement.getKey();
			    String f, token;
			    if (key instanceof Integer) {
			        token = key.toString();
			        f = getElementMapper().mapIndex(((Integer) key).intValue());
			    }
			    else if (key instanceof Double) {
			        token = Double.toHexString(((Double) key).doubleValue());
			        f = getElementMapper().mapField(token);
			    }
			    else {
			        MessageDigest md = getDigest();
			        byte[] buf = md.digest(key.toString().getBytes());
			        token = encode(buf);
			        f = getElementMapper().mapField(token);
			    }
    			if (logger.isDebugEnabled()) {
    			    logger.debug("Mapping path component to " + token);
    			}
    			sb.append(f);
			}
			else {
			    String token = (String) nextPathElement.getKey();
				if (logger.isDebugEnabled()) {
					logger.debug("Mapping path component field " + token);
				}
				String f = getElementMapper().mapField(token);
				if (logger.isDebugEnabled()) {
					logger.debug("field is mapped to: " + f);
				}
				sb.append(f);
			}

			if (!pi.hasNext()) {
				logger.debug("last element in name - not using a separator");
			}
			else if (level < tokenCount - 2) {
				logger.debug("Adding mapper-specified separator");
				sb.append(getElementMapper().getSeparator(level));
			}
			else {
				logger.debug("Adding '.' instead of mapper-specified separator");
				sb.append('.');
			}
			level++;
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("mapper id=" + this.hashCode() + " finished mapping " 
			    + path + " to " + sb.toString());
		}
		return new AbsFile(sb.toString());
	}

	private String encode(byte[] buf) {
        buf = Base64.encode(buf);
        char[] c = new char[buf.length];
        for (int i = 0; i < buf.length; i++) {
            c[i] = (char) buf[i];
        }
        return String.copyValueOf(c);
    }

    private MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("JVM error: SHA-1 not available");
        }
    }

    public Collection<Path> existing() {
		if (logger.isDebugEnabled()) {
			logger.debug("list existing paths for mapper id=" + this.hashCode());
		}
		List<Path> result = new ArrayList<Path>();
		final AbsFile f;
		if (location == null) {
			f = new AbsFile(".");
		}
		else {
			f = new AbsFile(location);
			if (!f.exists()) {
			    throw new IllegalArgumentException("Directory not found: " + location);
			}
		}
		logger.debug("Processing file list.");
		List<AbsFile> files = glob(f, pattern);
		if (files != null) {
			for (AbsFile file : files) {
				if (logger.isDebugEnabled()) {
				    logger.debug("Processing existing file " + file.getName());
				}
				Path p = rmap(file.getName());
				if (p != null) {
					if (logger.isDebugEnabled()) {
					    logger.debug("reverse-mapped to path " + p);
					}
					result.add(p);
				}
				else {
					logger.debug("reverse-mapped to nothing");
				}
			}
		}
		else {
			logger.debug("No files found id=" + this.hashCode());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Finish list existing paths for mapper " + this.hashCode() + " list=" + result);
		}
		return result;
	}
    
    protected List<AbsFile> glob(AbsFile f, String pattern) {
        if (pattern.length() == 0) {
            pattern = "*";
        }
        List<AbsFile> l = new ArrayList<AbsFile>();
        List<String> tokens;
        StringTokenizer st = new StringTokenizer(pattern, File.separator);
        // avoid creating an array list if only one token exists
        String firstToken;
        if (st.hasMoreTokens()) {
            firstToken = st.nextToken();
        }
        else {
            return Collections.emptyList();
        }
        if (st.hasMoreTokens()) {
            tokens = new ArrayList<String>();
            tokens.add(firstToken);
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken());
            }
        }
        else {
            tokens = Collections.singletonList(firstToken);
        }
        globRecursive(f, l, tokens, 0);
        return l;
    }
	
    private void globRecursive(AbsFile f, List<AbsFile> l, List<String> tokens, int pos) {
        String token = tokens.get(pos);
        if (pos == tokens.size() - 1) {
            if (token.equals("**")) {
                throw new IllegalArgumentException("** cannot be the last path element in a path pattern");
            }
            // at the file level
            globFiles(f, l, token);
        }
        else if (token.equals("**")) {
            // recursively go through all sub-directories and match the remaining pattern tokens
            DirectoryScanner ds = new DirectoryScanner(f);
            while (ds.hasNext()) {
                AbsFile dir = ds.next();
                globRecursive(dir, l, tokens, pos + 1);
            }
        }
        else {
            // not the last path element, so a directory
            final String regex = replaceWildcards(token);
            List<AbsFile> dirs = f.listDirectories(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(regex);
                }
            });
            for (AbsFile dir : dirs) {
                globRecursive(dir, l, tokens, pos + 1);
            }
        }
    }

    private void globFiles(AbsFile f, List<AbsFile> l, String token) {
        final String regex = replaceWildcards(token);
        List<AbsFile> files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(regex);
            }
        });
        l.addAll(files);
    }


    private String getVarName() {
        AbstractDataNode var = (AbstractDataNode) getParam(MappingParam.SWIFT_HANDLE);
        return var == null ? "" : var.getDisplayableName();
    }

    /** Returns the SwiftScript path for a supplied filename.
	  *
	  * Splits the filename into components using the separator
	  * supplied by the relevant FileNameElementMapper.
	  *
	  * If no separator is provided at any time during the processing,
	  * then somewhat different behaviour occurs:
	  *   the remaining filename is split using "."
	  *   if the remaining path does not have two components, then 
	  *      map the whole remaining path (including dots) at once
	  *   otherwise (when the remaining path has exactly two components):
	  *      if either of the components is of length 0, then return null
	  *      otherwise construct path out of two path components
	  *
	  * @param name the filename to map to a path
	  * @return a Path to the supplied filename, null on failure
	  */

	public Path rmap(String name) {
		logger.debug("rmap "+name);

		if(prefix != null) {
			if (name.startsWith(prefix)) {
				name = name.substring(prefix.length());
			} 
			else {
				throw new RuntimeException("filename '"+name+"' does not begin with prefix '"+prefix+"'");
			}
		}

		if(suffix != null) {
			if (name.endsWith(suffix)) {
				name = name.substring(0,name.length() - suffix.length());
			}
			else {
				throw new RuntimeException("filename '"+name+"' does not end with suffix '"+suffix+"'");
			}
		}

		Path path = Path.EMPTY_PATH;
		int level = 0;
		while (true) {
			int index = name.indexOf(getElementMapper().getSeparator(level));
			if (index == -1) {
				String[] e = name.split("\\.");
				if (e.length != 2) {
					Path p = rmapElement(path, name);
					logger.debug("rmap filename "+name+" to path "+p+", codepath 1");
					return p;
				}
                if (e[0].length() == 0 || e[1].length() == 0) {
                	logger.debug("e[0] or e[1] was zero - returning null. e[0]"+e[0]+" e[1]="+e[1]);
                	return null;
                }
                path = rmapElement(path, e[0]);
                path = rmapElement(path, e[1]);
                logger.debug("rmap filename "+name+" to path "+path+", codepath 2");
                return path;
			}
            path = rmapElement(path, name.substring(0, index));
            name = name.substring(index + 1);
			level++;
		}
	}

	/** maps the supplied filename component to a path segment and appends it
	  * to the supplied path. If the filename component is a numerical
	  * digit, then it will be treated as an array index; otherwise it
	  * will be treated as a structure field name.
	  */
	protected Path rmapElement(Path path, String e) {
		if (Character.isDigit(e.charAt(0))) {
			return path.addLast(getElementMapper().rmapIndex(e), true);
		}
        return path.addLast(getElementMapper().rmapField(e));
	}

	/** Appends the string representation of obj to the string buffer
	    if obj is not null. */
	private void maybeAppend(StringBuffer sb, Object obj) {
		if (obj != null) {
			sb.append(obj.toString());
		}
	}

	public String getLocation() {
		return location;
	}

	public String getPrefix() {
		return prefix;
	}


	/** Converts a unix-style glob pattern into a regular expression. */
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

