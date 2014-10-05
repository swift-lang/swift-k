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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.FileSystemLister;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.RootHandle;

public class ExternalMapper extends AbstractMapper {
	public static final Logger logger = Logger.getLogger(ExternalMapper.class);	
	
	private Map<Path, AbsFile> map;
    private Map<String, Path> rmap;
    
	@Override
    public String getName() {
        return "Ext";
    }

    @Override
    protected void getValidMappingParams(Set<String> s) {
	    s.addAll(ExternalMapperParams.NAMES);
	    s.add("*");
        super.getValidMappingParams(s);
    }

	private static final String[] STRING_ARRAY = new String[0];

	@Override
	public void initialize(RootHandle root) {
		super.initialize(root);
		
		ExternalMapperParams cp = getParams();
		map = new HashMap<Path, AbsFile>();
		rmap = new HashMap<String, Path>();
		String exec = cp.getExec();
		String bdir = getBaseDir();
		if (bdir != null && !exec.startsWith("/")) {
			exec = bdir + File.separator + exec;
		}
		List<String> cmd = new ArrayList<String>();
		cmd.add(exec);
		Map<String, Object> other = cp.getOtherParams();
		for (Map.Entry<String, Object> e : other.entrySet()) {
		    cmd.add('-' + e.getKey());
			cmd.add(String.valueOf(e.getValue()));
		}
		try {
		    if (logger.isDebugEnabled()) {
		        logger.debug("invoking external mapper: " + cmd);
		    }
			Process p = Runtime.getRuntime().exec(cmd.toArray(STRING_ARRAY));
			process(p.getInputStream());
			int ec = p.waitFor();
			if (ec != 0) {
				throw new RuntimeException("External executable failed. Exit code: " + ec + "\n\t"
						+ join(fetchOutput(p.getErrorStream())));
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String join(List<?> l) {
		StringBuffer sb = new StringBuffer();
		for (Object o : l) {
			sb.append(o);
			sb.append('\n');
			sb.append('\t');
		}
		return sb.toString();
	}
	
	private void process(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
        while (line != null) {
            processLine(line);
            line = br.readLine();
        }
	}

	private List<String> fetchOutput(InputStream is) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		while (line != null) {
			lines.add(line);
			line = br.readLine();
		}
		return lines;
	}

	private void processLine(String line) {
		int s = line.indexOf(' ');
		int t = line.indexOf('\t');
		int m = Math.min(s == -1 ? t : s, t == -1 ? s : t);
		if (m == -1) {
			throw new RuntimeException("Invalid line in mapper script output: " + line);
		}
		String spath = line.substring(0, m);
		Path p = Path.parse(spath);
		AbsFile f = new AbsFile(line.substring(m + 1).trim());
		map.put(p, f);
		rmap.put(spath, p);
	}

	@Override
	public Collection<Path> existing() {
		return map.keySet();
	}
	
	@Override
    public Collection<Path> existing(FileSystemLister l) {
        throw new UnsupportedOperationException();
    }

	public Path rmap(String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		return rmap.get(name);
	}

	@Override
	public PhysicalFormat map(Path path) throws InvalidPathException {
		PhysicalFormat p = map.get(path);
		if (p == null) {
		    throw new InvalidPathException(path);
		}
		return p;
	}

	public boolean isStatic() {
		return true;
	}

    @Override
    public MappingParamSet newParams() {
        return new ExternalMapperParams();
    }
}
