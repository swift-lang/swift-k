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
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.swing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.griphyn.vdl.karajan.monitor.StatefulItemClassSet;
import org.griphyn.vdl.karajan.monitor.items.ApplicationItem;

public class ApplicationTable extends SimpleTableClassRenderer<ApplicationItem> {

	public ApplicationTable(String name, StatefulItemClassSet<ApplicationItem> itemClassSet) {
		super(name, itemClassSet);
		setTableModel(new Model(itemClassSet));
	}
	
	public static class Model extends SimpleTableClassRenderer.Model<ApplicationItem> {
		
		public Model(StatefulItemClassSet<ApplicationItem> items) {
			super(items);
		}

		public int getColumnCount() {
			return 4;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			ApplicationItem app = (ApplicationItem) super.getValueAt(rowIndex, columnIndex);
			switch (columnIndex) {
				case 0: return app.getID().substring(app.getID().indexOf('-') + 1);
				case 1: return app.getName();
				case 2:	return app.getArguments();
				case 3: return app.getHost();
				default: return "?";
			}
		}
		
		private static final DateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSSZZZZZ");
		
		public String getDetails(int rowIndex) {
			ApplicationItem app = (ApplicationItem) getItem(rowIndex);
			StringBuffer sb = new StringBuffer();
			append(sb, "ID: ", app.getID());
			append(sb, "Name: ", app.getName());
			append(sb, "Arguments: ", app.getArguments());
			append(sb, "Host: ", app.getHost());
			append(sb, "Start time: ", TS.format(app.getStartTime()));
			append(sb, "Duration: ", getDuration(app.getStartTime()));
			return sb.toString();
		}
		
		private String getDuration(long date) {
			long diff = System.currentTimeMillis() - date;
			StringBuffer sb = new StringBuffer();
			int ms = (int) (diff % 1000);
			diff /= 1000;
			int s = (int) (diff % 60);
			diff /= 60;
			int m = (int) (diff % 60);
			diff /= 60;
			pad(sb, (int) diff);
			sb.append(':');
			pad(sb, m);
			sb.append(':');
			pad(sb, s);
			sb.append('.');
			if (ms < 100) {
				sb.append('0');
			}
			if (ms < 10) {
				sb.append('0');
			}
			sb.append(ms);
			return sb.toString();
		}
		
		private void pad(StringBuffer sb, int value) {
			if (value < 10) {
				sb.append('0');
			}
			sb.append(value);
		}
		
		private void append(StringBuffer sb, String label, Object value) {
			sb.append(label);
			sb.append(String.valueOf(value));
			sb.append('\n');
		}

		public String getColumnName(int column) {
			switch(column) {
				case 0: return "ID";
				case 1: return "Name";
				case 2: return "Arguments";
				case 3: return "Host";
				default: return "?";
			}
		}
	}
}
