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


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------
  
package org.globus.cog.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *  Loads a text file from a resource or file loadFromResource() tries to
 *  portably load from both if one fails
 */
public class TextFileLoader {

	/**
	 * Loads text from a file
	 * @param fileName the file name to be loaded
	 * @return a String with the contents of the file
	 * @throws IOException
	 */
	public static String loadFromFile(String fileName) throws IOException {
		return loadFromStream(new FileInputStream(fileName));
	}

	/**
	 * Loads text from a file
	 * @param file the file to be loaded
	 * @return a String with the contents of the file
	 * @throws IOException
	 */
	public static String loadFromFile(File file) throws IOException {
		return loadFromStream(new FileInputStream(file));
	}
	
	/**
	 * Reads text from an input stream
	 * @param is the input stream to read the text from
	 * @return a String with the loaded text
	 * @throws IOException
	 */
	public static String loadFromStream(InputStream is) throws IOException {
		String Text = "";

		BufferedReader bis = new BufferedReader(new InputStreamReader(is));
		String line = "";

		while (line != null) {
			line = bis.readLine();
			if (line != null) {
				Text = Text + line + "\n";
			}
		}
		is.close();
		return Text;
	}

	/**
	 * Reads text from a resource
	 * @param resName the name of the resource
	 * @return a String with the loaded text
	 */
	public static String loadFromResource(String resName) {
		//return loadFromStream(ResName.getClass().getResourceAsStream(ResName));
		
		URL FileURL = TextFileLoader.class.getClassLoader().getResource(resName);
		try {
			if (FileURL != null) {
				return loadFromStream(FileURL.openStream());
			}
			else {
				return loadFromFile(resName);
			}
		}
		catch (IOException e) {
			return "-- Resource not found --";
		}
	}
	
	public static boolean exists(String resName) {
		URL FileURL = TextFileLoader.class.getClassLoader().getResource(resName);
		if (FileURL != null) {
			return true;
		}
		if (new File(resName).exists()) {
			return true;
		}
		return false;
	}
}
