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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

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
	
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(AbstractFileMapperParams.NAMES);
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

	@Override
    public MappingParamSet newParams() {
        return new AbstractFileMapperParams();
    }

    @Override
	public void initialize(RootHandle root) {
		super.initialize(root);
		AbstractFileMapperParams cp = getParams(); 
		StringBuilder pattern = new StringBuilder();
		boolean wildcard = false; 
		if (cp.getPrefix() != null) {
		    pattern.append(cp.getPrefix());
		    pattern.append('*');
		    wildcard = true;
		}
		if (cp.getPattern() != null) {
            pattern.append(cp.getPattern());
            wildcard = false;
        }
		if (cp.getSuffix() != null) {
			if (!wildcard) {
			    pattern.append('*');
			}
			pattern.append(cp.getSuffix());
		}
		cp.setPattern(pattern.toString());
	}
	
	@Override
	public PhysicalFormat map(Path path) {
	    AbstractFileMapperParams cp = getParams();
	    return map(cp, path, cp.getPrefix());
	}
	
	protected PhysicalFormat map(AbstractFileMapperParams cp, Path path, String prefix) {
	    return map(cp, path, prefix, getElementMapper());
	}

	protected PhysicalFormat map(AbstractFileMapperParams cp, Path path, String prefix, FileNameElementMapper em) {
		if (logger.isDebugEnabled()) {
			logger.debug("mapper id=" + this.hashCode() + " starting to map " + path);
		}
		StringBuilder sb = new StringBuilder();
		
		String location = cp.getLocation();
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
                String f = em.mapIndex(key, level);
                sb.append(f);
            }
            else {
                String token = (String) nextPathElement.getKey();
                if (logger.isDebugEnabled()) {
                    logger.debug("Mapping path component field " + token);
                }
                String f = em.mapField(token);
                if (logger.isDebugEnabled()) {
                    logger.debug("field is mapped to: " + f);
                }
                sb.append(f);
            }

            appendSeparator(sb, level, tokenCount, em);
            level++;
        }
		
		String suffix = cp.getSuffix();
		if (suffix != null) {
			sb.append(suffix);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("mapper id=" + this.hashCode() + " finished mapping " 
			    + path + " to " + sb.toString());
		}
		return new AbsFile(sb.toString());
	}

    protected void appendSeparator(StringBuilder sb, int level, int totalTokenCount, FileNameElementMapper em) {
        if (level == totalTokenCount - 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("last element in name - not using a separator");
            }
        }
        else if (level == totalTokenCount - 2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding mapper-specified separator");
            }
            sb.append(em.getSeparator(level));
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding '.' instead of mapper-specified separator");
            }
            sb.append('.');
        }
    }

    @Override
    public Collection<Path> existing() {
        return existing(FileSystemLister.DEFAULT);
    }

    @Override
    public Collection<Path> existing(FileSystemLister fsl) {
		if (logger.isDebugEnabled()) {
			logger.debug("list existing paths for mapper id=" + this.hashCode());
		}
		List<Path> result = new ArrayList<Path>();
		final AbsFile f;
		AbstractFileMapperParams cp = getParams();
		String location = cp.getLocation();
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
		String pattern = cp.getPattern();
		List<AbsFile> files = glob(f, fsl, pattern);
		if (files != null) {
			for (AbsFile file : files) {
				if (logger.isDebugEnabled()) {
				    logger.debug("Processing existing file " + file.getName());
				}
				Path p = rmap(cp, file);
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
    
    protected List<AbsFile> glob(AbsFile f, FileSystemLister fsl, String pattern) {
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
        globRecursive(f, fsl, l, tokens, 0);
        return l;
    }
	
    private void globRecursive(AbsFile f, FileSystemLister fsl, List<AbsFile> l, List<String> tokens, int pos) {
        String token = tokens.get(pos);
        if (pos == tokens.size() - 1) {
            if (token.equals("**")) {
                throw new IllegalArgumentException("** cannot be the last path element in a path pattern");
            }
            // at the file level
            globFiles(f, fsl, l, token);
        }
        else if (token.equals("**")) {
            // recursively go through all sub-directories and match the remaining pattern tokens
            DirectoryScanner ds = new DirectoryScanner(f);
            while (ds.hasNext()) {
                AbsFile dir = ds.next();
                globRecursive(dir, fsl, l, tokens, pos + 1);
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
                globRecursive(dir, fsl, l, tokens, pos + 1);
            }
        }
    }

    private void globFiles(AbsFile f, FileSystemLister fsl, List<AbsFile> l, String token) {
        final String regex = replaceWildcards(token);
        List<AbsFile> files = fsl.listFiles(f, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(regex);
            }
        });
        l.addAll(files);
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
	protected Path rmap(AbstractFileMapperParams cp, AbsFile file) {
	    String name = file.getName();
		logger.debug("rmap "+name);

		String prefix = cp.getPrefix();
		if (prefix != null) {
			if (name.startsWith(prefix)) {
				name = name.substring(prefix.length());
			} 
			else {
				throw new RuntimeException("filename '"+name+"' does not begin with prefix '"+prefix+"'");
			}
		}

		String suffix = cp.getSuffix();
		if (suffix != null) {
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
	private void maybeAppend(StringBuilder sb, Object obj) {
		if (obj != null) {
			sb.append(obj.toString());
		}
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

    @Override
    public Collection<AbsFile> getPattern(Path path, Type type) {
        // it makes no sense to call this method without an array
        if (!type.isArray()) {
            throw new IllegalArgumentException("getPattern() must be called on an array");
        }
        AbstractFileMapperParams cp = getParams();

        /*
         * figure out all possible paths to leaves, then map them but 
         * replace all array indices by wildcards
         */
        FileNameElementMapper m = new WildcardElementMapper(getElementMapper(), path.size());
        Path cpath = path.addLast(getTemplateIndex(type.keyType()), true);
        
        if (!type.itemType().isComposite()) {
            AbsFile f = (AbsFile) this.map(cp, cpath, cp.getPrefix(), m);
            return Collections.singletonList(f);
        }
        else {
            // build all possible paths to leaves
            List<Path> l = new ArrayList<Path>();
            traverseTypes(l, cpath, type.itemType());
            
            List<AbsFile> rl = new ArrayList<AbsFile>();
            
            for (Path p : l) {
                AbsFile f = (AbsFile) this.map(cp, cpath, cp.getPrefix(), m);
            }
            
            return rl;
        }        
    }
    
    private static class WildcardElementMapper implements FileNameElementMapper {
        private FileNameElementMapper delegate;
        private int fixed;
        
        public WildcardElementMapper(FileNameElementMapper delegate, int fixed) {
            this.delegate = delegate;
            this.fixed = fixed;
        }

        @Override
        public String mapField(String fieldName) {
            return delegate.mapField(fieldName);
        }

        @Override
        public String rmapField(String pathElement) {
            return delegate.rmapField(pathElement);
        }

        @Override
        public String mapIndex(int index, int pos) {
            String s = delegate.mapIndex(index, pos);
            if (pos > fixed) {
                return nChars(s.length());
            }
            else {
                return s;
            }
        }

        @Override
        public String mapIndex(Object index, int pos) {
            String s = delegate.mapIndex(index, pos);
            if (pos >= fixed) {
                return nChars(s.length());
            }
            else {
                return s;
            }
        }

        @Override
        public int rmapIndex(String pathElement) {
            return delegate.rmapIndex(pathElement);
        }

        @Override
        public String getSeparator(int depth) {
            return delegate.getSeparator(depth);
        }
        
        private String nChars(int length) {
            char[] c = new char[length];
            for (int i = 0; i < length; i++) {
                c[i] = '?';
            }
            return new String(c);
        }
    }

    private void traverseTypes(List<Path> l, Path path, Type t) {
        if (!t.isComposite()) {
            // done
            l.add(path);
        }
        else if (t.isArray()) {
            Path cpath = path.addLast(getTemplateIndex(t.keyType()), true);
            traverseTypes(l, cpath, t.itemType());
        }
        else {
            // struct
            for (Field f : t.getFields()) {
                Path cpath = path.addLast(f.getId());
                traverseTypes(l, cpath, f.getType());
            }
        }
    }

    private Comparable<?> getTemplateIndex(Type keyType) {
        if (keyType.equals(Types.INT)) {
            return 1;
        }
        else if (keyType.equals(Types.FLOAT)) {
            return 1.1;
        }
        else {
            return "index";
        }
    }
}

