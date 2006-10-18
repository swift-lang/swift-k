//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2006
 */
package org.globus.cog.abstraction.impl.file;

import java.util.StringTokenizer;

import org.globus.cog.abstraction.interfaces.FileResource;

public class FileResourceUtil {

	public static void createDirectories(FileResource fr, String dir) throws GeneralException {
		// TODO this should be in an AbstractFileResource
		// there is an assumption here on the path separators
		// I'd really suggest enforcing only one of them at the level of the
		// interface
		StringTokenizer st = new StringTokenizer(dir, "/");
		StringBuffer sb = new StringBuffer();
		if (dir.startsWith("/")) {
			sb.append('/');
		}
		while (st.hasMoreTokens()) {
			try {
				String partial = sb.toString();
				if (!fr.exists(partial)) {
					fr.createDirectory(partial);
				}
				sb.append(st.nextToken());
				sb.append('/');
			}
			catch (Exception e) {
				throw new GeneralException("Could not create directory structure " + dir, e);
			}
		}
	}
}
