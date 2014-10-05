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

import org.griphyn.vdl.mapping.nodes.AbstractDataNode;


public class InvalidPathException extends Exception {
	public InvalidPathException(String path, DSHandle source) {
		super(getMessage(path, source));
	}
	
	private static String getMessage(String path, DSHandle source) {
		if (source.getType().isArray()) {
			return "Array index '" + path + "' not found for " + getName(source) + " of size " + getSize(source);
		}
		else if (source.getType().isComposite()) {
			return "Invalid field name '" + path + "' for " + getName(source) + " of type " + source.getType();
		}
		else {
			return "Invalid path (" + path + ") for " + source.toString();
		}
    }

    private static int getSize(DSHandle source) {
        return source.getArrayValue().size();
    }

    private static String getName(DSHandle source) {
        if (source instanceof AbstractDataNode) {
        	return ((AbstractDataNode) source).getDisplayableName();
        }
        else {
        	return source.toString();
        }
    }

    public InvalidPathException(Path path, DSHandle source) {
		this(path.toString(), source);
	}
    
    public InvalidPathException(Object path, DSHandle source) {
        this(path.toString(), source);
    }
    
    public InvalidPathException(String string) {
        super(string);
    }
    
    public InvalidPathException(Path path) {
        super(path.toString());
    }
    
    public InvalidPathException(DSHandle source) {
        this(source.getPathFromRoot(), source);
    }
}
