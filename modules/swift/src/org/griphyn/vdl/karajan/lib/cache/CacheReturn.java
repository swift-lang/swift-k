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
 * Created on Dec 29, 2006
 */
package org.griphyn.vdl.karajan.lib.cache;

import java.util.List;

public class CacheReturn {
	public final boolean alreadyCached;
	public final List<?> remove;
	public final File cached;
	
	public CacheReturn(boolean alreadyCached, List<?> remove, File cached) {
		this.alreadyCached = alreadyCached;
		this.remove = remove;
		this.cached = cached;
	}
}
