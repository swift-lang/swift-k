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
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/**
 * This class encapsulates the elements required for public key authentication. 
 */
package org.globus.cog.abstraction.impl.common;

import java.io.File;

public class PublicKeyAuthentication {
	private String username;
	private File privateKeyFile;
	private char[] passPhrase;
	
	public PublicKeyAuthentication(String username, File privateKeyFile, char[] passPhrase) {
		this.username = username;
		this.privateKeyFile = privateKeyFile;
		this.passPhrase = passPhrase == null ? null : (char[]) passPhrase.clone();
	}
	
	public PublicKeyAuthentication(String username, String privateKeyFile, char[] passPhrase) {
		this(username, new File(privateKeyFile), passPhrase);
	}

	public char[] getPassPhrase() {
		return passPhrase;
	}

	public void setPassPhrase(char[] passPhrase) {
		this.passPhrase = passPhrase;
	}

	public File getPrivateKeyFile() {
		return privateKeyFile;
	}

	public void setPrivateKeyFile(File privateKeyFile) {
		this.privateKeyFile = privateKeyFile;
	}
	
	public void setPrivateKeyFile(String privateKeyFile) {
		this.privateKeyFile = new File(privateKeyFile);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String toString() {
	    return username + ":<key>";
	}
}