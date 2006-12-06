/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.Path;

public class AirsnMapper extends AbstractFileMapper {
	public AirsnMapper() {
		super(new AirsnFileNameElementMapper());
	}

	public Path rmap(String name) {
		if (!name.startsWith(getPrefix() + "_") && !name.startsWith(getPrefix() + ".")) {
			return null;
		}
		Path path = Path.EMPTY_PATH;
		StringTokenizer st = new StringTokenizer(name, "_.");
		// skip the prefix
		st.nextToken();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.matches(".*\\d\\d\\d\\d\\z")) {
				if (tok.length() == 4) {
					return null;
				}
				else {
					path = path.addLast("v");
					path = path.addLast(
							Integer.valueOf(tok.substring(tok.length() - 4)).toString(), true);
				}
			}
			else {
				path = path.addLast(tok);
			}
		}
		System.out.println(name + " parsed into " + path);
		return path;
	}
}
