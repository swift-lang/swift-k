/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.globus.cog.abstraction.impl.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 7, 2004
 */

public class BootUtil {
	private static Logger logger = Logger.getLogger(BootUtil.class);

	public static void checkConfigDir(String configPath, String index, ClassLoader cl)
			throws Exception {
		File configDir = new File(configPath);
		if (configDir.exists() && !configDir.isDirectory()) {
			throw new Exception("The configuration directory is a file. Please remove it: "
					+ configPath);
		}
		if (!configDir.exists()) {
			URL indexURL = cl.getResource(index);
			if (indexURL == null) {
				throw new Exception("Cannot find index file: " + index);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(indexURL.openStream()));
			String line = reader.readLine();
			while (line != null) {
				if (line.equals("") || line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}
				String[] pair = line.split(" ");
				if (pair.length != 2) {
					logger.warn("Invalid line in index file: " + line);
				}
				else {
					URL fileURL = cl.getResource(pair[0]);
					if (fileURL == null) {
						logger.error("Cannot find configuration file: " + pair[0]);
					}
					else {
						File destination = new File(configPath + File.separator + pair[1]);
						copyFile(fileURL.openStream(), destination);
					}
				}
				line = reader.readLine();
			}
		}
	}

	public static void copyFile(InputStream is, File dest) {
		try {
			dest.getParentFile().mkdirs();
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
			byte[] buf = new byte[1024];
			int read = 0;
			while (read != -1) {
				read = bis.read(buf);
				if (read != -1) {
					bos.write(buf, 0, read);
				}
			}
			bis.close();
			bos.close();
		}
		catch (Exception e) {
			logger.warn("Could not create configuratio file " + dest);
		}
	}
}