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
 * Created on Feb 1, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class CharacterMap extends Component {
	public CharacterMap() {
		setSize(16, 16);
	}

	protected void draw(ANSIContext context) throws IOException {
		for (int i = 0; i < 16; i++) {
			context.moveTo(sx, sy + i);
			for (int j = 0; j < 16; j++) {
				context.lineArt((char) (i*16 + j));
				context.putChar(' ');
			}
		}
	}
}
