package org.griphyn.vdl.mapping.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractMapper;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.PhysicalFormat;

public class ExternalMapper extends AbstractMapper {
	public static final Logger logger = Logger.getLogger(ExternalMapper.class);

	private Map map, rmap = new HashMap();

	public static final MappingParam PARAM_EXEC = new MappingParam("exec");
	public static final MappingParam PARAM_BASEDIR = new MappingParam("#basedir", null);

	private static Set ignored;

	static {
		ignored = new HashSet();
		ignored.add("exec");
		ignored.add("input");
		ignored.add("dbgname");
		ignored.add("descriptor");
		ignored.add("#basedir");
	}

	private static final String[] STRING_ARRAY = new String[0];

	public void setParams(Map params) {
		super.setParams(params);
		map = new HashMap();
		rmap = new HashMap();
		String exec = PARAM_EXEC.getStringValue(this);
		String bdir = PARAM_BASEDIR.getStringValue(this);
		if (bdir != null && !exec.startsWith("/")) {
			exec = bdir + File.separator + exec;
		}
		List cmd = new ArrayList();
		cmd.add(exec);
		Iterator i = params.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			String name = e.getKey().toString();
			if (!ignored.contains(name)) {
				MappingParam tp = new MappingParam(name);
				cmd.add('-' + name);
				cmd.add(tp.getStringValue(this));
			}
		}
		try {
			Process p = Runtime.getRuntime().exec((String[]) cmd.toArray(STRING_ARRAY));
			List lines = fetchOutput(p.getInputStream());
			int ec = p.waitFor();
			if (ec != 0) {
				throw new RuntimeException("External executable failed. Exit code: " + ec + "\n\t"
						+ join(lines) + "\n\t" + join(fetchOutput(p.getErrorStream())));
			}
			processLines(lines);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String join(List l) {
		StringBuffer sb = new StringBuffer();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			sb.append('\n');
			sb.append('\t');
		}
		return sb.toString();
	}

	private List fetchOutput(InputStream is) throws IOException {
		ArrayList lines = new ArrayList();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		while (line != null) {
			lines.add(line);
			line = br.readLine();
		}
		return lines;
	}

	private void processLines(List lines) {
		Iterator i = lines.iterator();
		while (i.hasNext()) {
			String line = (String) i.next();
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
	}

	public Collection existing() {
		return map.keySet();
	}

	public Path rmap(String name) {
		if (name == null || name.equals("")) {
			return null;
		}
		return (Path) rmap.get(name);
	}

	public PhysicalFormat map(Path path) {
		return (AbsFile) map.get(path);
	}

	public boolean isStatic() {
		return false;
	}
}
