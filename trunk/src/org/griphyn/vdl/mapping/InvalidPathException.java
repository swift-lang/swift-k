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


/*
 * Created on Jun 6, 2006
 */
package org.griphyn.vdl.mapping;

public class InvalidPathException extends Exception {
	public InvalidPathException(String path, DSHandle source) {
		super("Invalid path (" + path + ") for "
			+ source.toString());
	}
	
	public InvalidPathException(Path path, DSHandle source) {
		this(path.toString(), source);
	}

    public InvalidPathException(String string) {
        super(string);
    }
}
