//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 18, 2006
 */
package org.globus.cog.abstraction.impl.file;

import org.globus.cog.abstraction.interfaces.FileResource;

public class FileResourceUtil {

	public static void createDirectories(FileResource fr, String dir) throws GeneralException {
		// TODO this should be in an AbstractFileResource
		// there is an assumption here on the path separators
		// I'd really suggest enforcing only one of them (hint: '/') at the
		// level of the
		// interface
		if (dir.equals("/")) {
			return;
		}
		try {
			if (!fr.exists(dir)) {
				int i = dir.lastIndexOf('/');
				if (i <= 0) {
					fr.createDirectory(dir);
				}
				else {
					createDirectories(fr, dir.substring(0, i));
					if (i != dir.length() - 1) {
						fr.createDirectory(dir);
					}
					else {
						// trailing '/'
					}
				}
			}
		}
		catch (GeneralException e) {
			if (!fr.isDirectory(dir)) {
				throw e;
			}
		}
		catch (FileNotFoundException e) {
			// [m] why on earth is this thrown here?
			throw new GeneralException(e.getMessage(), e);
		}
	}
}
