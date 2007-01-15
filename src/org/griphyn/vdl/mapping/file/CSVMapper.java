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

import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public class CSVMapper extends AbstractMapper {
	public static final MappingParam PARAM_FILE = new MappingParam("file");
	// whether the file has a line describing header info, default is true
	public static final MappingParam PARAM_HEADER = new MappingParam("header");
	// skip a number of lines at the beginning
	public static final MappingParam PARAM_SKIP = new MappingParam("skip");
	// header delimiter, default is white space
	public static final MappingParam PARAM_HDELIMITER = new MappingParam("hdelim");
	// content delimiter, default is white space
	public static final MappingParam PARAM_DELIMITER = new MappingParam("delim");

	// keep a list of column name
	private List cols = new ArrayList();

	// keep the column name to index map
	private Map colindex = new HashMap();

	// keep the content of the CSV file
	private List content = new ArrayList();

	private boolean read;

	public void setParams(Map params) {
		super.setParams(params);
		if (!PARAM_FILE.isPresent(this)) {
			throw new RuntimeException("CSV mapper must have a file parameter!");
		}
		if (!PARAM_HEADER.isPresent(this)) {
			PARAM_HEADER.setValue(this, "true");
		}
		if (!PARAM_DELIMITER.isPresent(this)) {
			PARAM_DELIMITER.setValue(this, " \t");
		}
		if (!PARAM_HDELIMITER.isPresent(this)) {
			PARAM_HDELIMITER.setValue(this, PARAM_DELIMITER.getValue(this));
		}
	}

	private void readFile() {
		if (read) {
			return;
		}
		String file = PARAM_FILE.getFileName(this);
		String delim = PARAM_DELIMITER.getStringValue(this);
		String hdelim = PARAM_HDELIMITER.getStringValue(this);
		boolean header = PARAM_HEADER.getBooleanValue(this);
		int skip = PARAM_SKIP.getIntValue(this);
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
			if (line != null && !header) {
				st = new StringTokenizer(line, delim);
				int j = 0;
				while (j < st.countTokens()) {
					String colname = "column" + j;
					cols.add(colname);
					colindex.put(colname, new Integer(j));
					j++;
				}
			}

			while (line != null) {
				st = new StringTokenizer(line, delim);
				List colContent = new ArrayList();
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					colContent.add(tok);
				}
				line = br.readLine();
				++i;
				content.add(colContent);
			}
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public Collection existing() {
		readFile();
		List l = new ArrayList();
		Iterator itl = content.iterator();
		int ii = 0;
		while (itl.hasNext()) {
			Path path = Path.EMPTY_PATH;
			path = path.addFirst(String.valueOf(ii), true);
			List colContent = (List) itl.next();
			Iterator itc = colContent.iterator();
			int j = 0;
			while (itc.hasNext()) {
				Path p = path.addLast((String)cols.get(j));
				l.add(p);
				itc.next();
				j++;
			}
			ii++;
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
		readFile();
		if (path == null || path == Path.EMPTY_PATH) {
			return null;
		}

		Iterator pi = path.iterator();
		Path.Entry pe = (Path.Entry) pi.next();
		if (!pe.isIndex()) {
			return null;
		}
		int i = 0;
		try {
			i = Integer.parseInt(pe.getName());
		}
		catch (NumberFormatException e) {
			return null;
		}
		if (i > content.size()) {
			return null;
		}
		List cl = (List) content.get(i);
		if (cl == null) {
			return null;
		}

		if (!pi.hasNext()) {
			return (String) cl.get(0);
		}

		pe = (Path.Entry) pi.next();
		String col = pe.getName();
		if (!colindex.containsKey(col)) {
			return null;
		}
		int ci = ((Integer) colindex.get(col)).intValue();
		return (String) cl.get(ci);
	}
}
