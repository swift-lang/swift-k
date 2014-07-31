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
 * Created on Apr 3, 2014
 */
package k.rt;

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class DebugFrame extends Frame {
	private final Object owner;
	private String[] names;

	public DebugFrame(Object owner, int count, Frame prev) {
		super(count, prev);
		if (CompilerSettings.DEBUG) {
			names = new String[count];
		}
		this.owner = owner;
	}
	
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(owner);
        sb.append(":");
        for (int i = 0; i < a.length; i++) {
            sb.append("\n\t");
            sb.append(i);
            sb.append(" - ");
            if (CompilerSettings.DEBUG) {
                if (names[i] != null) {
                    sb.append(names[i]);
                    sb.append(" = ");
                }
            }
            String s = str(a[i]);
            if (s.length() > 32) {
                sb.append(s.subSequence(0, 24));
                sb.append("... ");
                sb.append(s.subSequence(s.length() - 8, s.length()));
            }
            else {
                sb.append(s);
            }
        }
        return sb.toString();
    }
	
	public void setName(int index, String name) {
        names[index] = name;
    }
}
