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


package org.griphyn.vdl.mapping.file;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.griphyn.vdl.mapping.Path;

public class ROIMapper extends AbstractFileMapper {
	private int count;
	private Map names;
	
	public ROIMapper() {
		super(new ROIFileNameElementMapper());
		names = new HashMap();
		count = 0;
	}

	public Path rmap(String name) {
		if (name.indexOf(".ROI") == -1) {
			return null;
		}
		Path path = Path.EMPTY_PATH;
		int ri = name.indexOf(".ROI");
		if (ri==-1)
			return null;
		
		// skip the basename
		String basename = name.substring(0, ri);
		
		// get the right index
		Integer idx = (Integer) names.get(basename);
		boolean notseen = (idx == null);
		
		StringTokenizer st = new StringTokenizer(name.substring(ri+1), ".");
		int i = 1;
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (i == 1 && tok.equals("ROI")) {
				path = path.addLast("roi");
				if (notseen) {
					idx = new Integer(count);
				}
				path = path.addLast(idx, true);
				if (st.hasMoreElements()) {
					i++;
					continue;
				}
				else {
					if (notseen) {
						// we have not processed the name
						elementMapper.rmapIndex(basename);
						names.put(basename, new Integer(count));
						++count;
					}
					path = path.addLast("image");
				}
			}
			else if (i == 2 && tok.equals("center")) {
				path = path.addLast(tok);
				if (notseen) {
					// we have not processed the name
					elementMapper.rmapIndex(basename);
					names.put(basename, new Integer(count));
					++count;
				}
			} else {
				return null;
			}
		}
		System.out.println(name + " parsed into " + path);
		return path;
	}
}
