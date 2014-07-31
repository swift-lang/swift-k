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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 27, 2007
 */
package org.globus.cog.abstraction.impl.file;

/**
 * Signifies an exception that puts a file resource in an irrecoverable
 * state. If a resource throws this exception during a request, future
 * requests on this resource will will fail or cause unpredictable
 * behavior.
 */
public class IrrecoverableResourceException extends FileResourceException {

	public IrrecoverableResourceException() {
		super();
	}

	public IrrecoverableResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public IrrecoverableResourceException(String message) {
		super(message);
	}

	public IrrecoverableResourceException(Throwable cause) {
		super(cause);
	}
}
