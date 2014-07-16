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
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping.file;

import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.Path;

public class AirsnMapper extends AbstractFileMapper {
	public AirsnMapper() {
		super(new AirsnFileNameElementMapper());
	}

    @Override
    public String getName() {
        return "AIRSNMapper";
    }

    public Path rmap(AbstractFileMapperParams cp, String name) {	    
		if (!name.startsWith(cp.getPrefix() + "_") && !name.startsWith(cp.getPrefix() + ".")) {
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
							Integer.valueOf(tok.substring(tok.length() - 4)), true);
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
