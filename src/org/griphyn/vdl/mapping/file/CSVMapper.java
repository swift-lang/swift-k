package org.griphyn.vdl.mapping.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.Mapper;
import org.griphyn.vdl.mapping.Path;

public class CSVMapper implements Mapper {
    public static final String PARAM_FILE = "file";
    // whether the file has a line describing header info, default is true
    public static final String PARAM_HEADER = "header";
    // skip a number of lines at the beginning
    public static final String PARAM_SKIP = "skip";
    // header delimiter, default is white space
    public static final String PARAM_HDELIMITER = "hdelim";
    // content delimiter, default is white space
    public static final String PARAM_DELIMITER = "delim";

    private String file;
    private boolean header = true;
    private int skip = 0;
    private String hdelim = " \t";
    private String delim = " \t";

    // keep a list of column name
	private List cols = new ArrayList();
	
	// keep the column name to index map
	private Map colindex = new HashMap();
	
	// keep the content of the CSV file
    private List content = new ArrayList();
    
	public void setParams(Map params) {
		file = (String) params.get(PARAM_FILE);
		if (file == null)
			throw new RuntimeException("CSV mapper must have a file parameter!");
		String hdr = (String) params.get(PARAM_HEADER);
		if (hdr != null)
			header = Boolean.valueOf(hdr).booleanValue();

		String p = (String)params.get(PARAM_DELIMITER);
		if (p != null) {
			delim = p;
			hdelim = p;
		}
		
		p = (String)params.get(PARAM_HDELIMITER);
		if (p != null) {
			hdelim = p;
		}
		
		p = (String)params.get(PARAM_SKIP);
		if (p != null) {
			skip = Integer.parseInt(p);
		}
	}

	public Collection existing() {
		List l = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
		
			String line;
			StringTokenizer st;
			
			if (header) {
				line = br.readLine();
				st = new StringTokenizer(line, hdelim);
				int ix = 0;
				while (st.hasMoreTokens()) {
					String column = st.nextToken();
					column.replaceAll("\\s", "_");
					cols.add(column);
					colindex.put(column, new Integer(ix));
					++ix;
				}
			}
			while (skip > 0) {
				br.readLine();
				--skip;
			}

			int i = 0;
			line = br.readLine();
			if (line != null && ! header) {
				st = new StringTokenizer(line, delim);
				int j=0;
				while (j < st.countTokens()) {
					String colname = "column" + j;
					cols.add(colname);
					colindex.put(colname, new Integer(j));
					j++;
				}
			}
			
			while (line != null) {
				st = new StringTokenizer(line, delim);
				Path path = Path.EMPTY_PATH;
				path = path.addFirst(String.valueOf(i), true);
				int j = 0;
				List colContent = new ArrayList();
				while (st.hasMoreTokens()) {
					colContent.add(st.nextToken());
					Path p = path.addLast((String)cols.get(j));
					l.add(p);
					++j;
				}
				line = br.readLine();
				++i;
				content.add(colContent);
			}
		} catch (FileNotFoundException e){
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return l;
	}

	public boolean exists(Path path) {
		File f = new File(map(path));
		return f.exists();
	}

	public boolean isStatic() {
		return false;
	}

	public String map(Path path) {
		if (path == null || path == Path.EMPTY_PATH)
			return null;

		Iterator pi = path.iterator();
		Path.Entry pe = (Path.Entry)pi.next();
		if (!pe.isIndex()) {
			return null;
		}
		int i = 0;
		try {
			i = Integer.parseInt(pe.getName());
		} catch (NumberFormatException e) {
			return null;
		}
		if (i>content.size())
			return null;
		List cl = (List) content.get(i);
		if (cl == null)
			return null;

		if (!pi.hasNext())
			return null;
		
		pe = (Path.Entry)pi.next();
		String col = pe.getName();
		if (!colindex.containsKey(col))
			return null;
		int ci = ((Integer)colindex.get(col)).intValue();
		return (String)cl.get(ci);
	}
}
