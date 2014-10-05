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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.GeneralizedFileFormat;
import org.griphyn.vdl.mapping.InvalidMappingParameterException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Types;

public class CSVMapper extends AbstractMapper {
    
	@Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(CSVMapperParams.NAMES);
	    super.getValidMappingParams(s);
    }
	
	private List<String> cols = new ArrayList<String>();
    private Map<String, Integer> colindex = new HashMap<String, Integer>();
    private List<List<String>> content = new ArrayList<List<String>>();
    private boolean read = false;

	@Override
    public String getName() {
        return "CSVMapper";
    }

    public void initialize(RootHandle root) {
		super.initialize(root);
		CSVMapperParams cp = getParams();
		if (cp.getFile() == null) {
			throw new InvalidMappingParameterException("CSV mapper must have a file parameter.");
		}
		if (cp.getHdelim() == null) {
		    cp.setHdelim(cp.getDelim());
		}
	}

	private synchronized void readFile(CSVMapperParams cp) {
		if (read) {
			return;
		}
				
		String file = getCSVFile(cp);
		
		try {
			BufferedReader br = 
			    new BufferedReader(new FileReader(file));

			String line;
			StringTokenizer st;
			boolean header = Boolean.TRUE.equals(cp.getHeader());

			if (header) {
				line = br.readLine();
				if (line == null) {
				    throw new RuntimeException("Invalid CSV file (" + file + "): missing header.");
				}
				st = new StringTokenizer(line, cp.getHdelim());
				int ix = 0;
				
				while (st.hasMoreTokens()) {
					String column = st.nextToken();
					column = column.replaceAll("\\s", "_");
					
					cols.add(column);
					colindex.put(column, new Integer(ix));
					++ix;
				}
			}
			while (cp.getSkip() > 0) {
				br.readLine();
				cp.setSkip(cp.getSkip() - 1);
			}

			int i = 0;
			line = br.readLine();
			if (line != null && !header) {
				st = new StringTokenizer(line, cp.getDelim());
				int j = 0;
				while (j < st.countTokens()) {
					String colname = "column" + j;
					cols.add(colname);
					colindex.put(colname, new Integer(j));
					j++;
				}
			}

			while (line != null) {
				st = new StringTokenizer(line, cp.getDelim());
				List<String> colContent = new ArrayList<String>();
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					colContent.add(tok);
				}
				line = br.readLine();
				++i;
				content.add(colContent);
			}
			read = true;
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getCSVFile(CSVMapperParams cp) {
	    String result = null;
        DSHandle handle = cp.getFile();
        GeneralizedFileFormat fileFormat;
        if (handle.getType().equals(Types.STRING)) {
            String path = (String) handle.getValue();
            fileFormat = new AbsFile(path);
        }
        else {
            PhysicalFormat format = handle.map();
            fileFormat = (GeneralizedFileFormat) format;
        }
        result  = fileFormat.getPath();
        return result;
    }

	@Override
    public Collection<Path> existing() {
	    CSVMapperParams cp = getParams();
		readFile(cp);
				
		List<Path> l = new ArrayList<Path>();
		
		Iterator<List<String>> itl = content.iterator();
		int ii = 0;
		while (itl.hasNext()) {
			Path path = Path.EMPTY_PATH;
			path = path.addFirst(ii, true);
			List<String> colContent = itl.next();
			Iterator<String> itc = colContent.iterator();
			int j = 0;
			while (itc.hasNext()) {
				Path p = path.addLast(cols.get(j));
				l.add(p);
				itc.next();
				j++;
			}
			ii++;
		}
		return l;
	}

	@Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public PhysicalFormat map(Path path) throws InvalidPathException {
		if (path == null || path == Path.EMPTY_PATH) {
			throw new InvalidPathException(path);
		}
		
		CSVMapperParams cp = getParams();
		
		readFile(cp);

		Iterator<Path.Entry> pi = path.iterator();
		Path.Entry pe = pi.next();
		if (!pe.isIndex()) {
			throw new InvalidPathException(path);
		}
		int i = 0;
		if (pe.getKey() instanceof Integer) {
		    i = ((Integer) pe.getKey()).intValue();
		}
		else {
			throw new InvalidPathException(path);
		}
		if (i > content.size()) {
			return null;
		}
		List<String> cl = content.get(i);
		if (cl == null) {
			throw new InvalidPathException(path);
		}

		if (!pi.hasNext()) {
			return new AbsFile(cl.get(0));
		}

		pe = pi.next();
		String col = String.valueOf(pe.getKey());
		if (!colindex.containsKey(col)) {
			throw new InvalidPathException(path);
		}
		int ci = colindex.get(col).intValue();
		return new AbsFile(cl.get(ci));
	}

    @Override
    public MappingParamSet newParams() {
        return new CSVMapperParams();
    }
}
