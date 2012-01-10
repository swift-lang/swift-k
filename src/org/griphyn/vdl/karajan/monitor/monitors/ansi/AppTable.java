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
 * Created on Sep 22, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Dialog;

public class AppTable extends STable {
	private Dialog d;

	protected void createDialog() {
		d = new AppDialog(getScreen(), getApplicationItem());
	}
	
	protected ApplicationItem getApplicationItem() {
		return (ApplicationItem) getItem();
	}
}
