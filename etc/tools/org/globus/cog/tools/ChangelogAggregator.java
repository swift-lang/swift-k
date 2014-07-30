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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.tools;

import java.io.*;
import java.util.*;

public class ChangelogAggregator {
	public static void main(String[] args) {
		try{
			if (args.length != 3) {
				System.err.println("Usage:\n\tChangelogAggregator <destlog> <sourcelog> <name>");
				return;
			}
			File destlog = new File(args[0]);
			destlog.createNewFile();
			File srclog = new File(args[1]);
			if (!srclog.exists()) {
				System.err.println("Warning: source log (" + srclog.getAbsolutePath() + ") does not exist");
				return;
			}
			String name = args[2];
			aggregate(destlog, srclog, name);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
		
	public static void aggregate(File destlog, File srclog, String name) throws IOException {
		TreeMap dest = populate(destlog);
		TreeMap src  = populate(srclog, name);
		insert(dest, src);
		write(dest, destlog);
	}
	
	public static TreeMap populate(File f) throws IOException {
		return populate(f, null);
	}
	
	public static TreeMap populate(File f, String defname) throws IOException {
		/*
		 * Structure:
		 * DATE_HEADING
		 * * MODULE_NAME
		 * *** ENTRY
		 *     MORE_ENTRY
		 * NEWLINE
		 * ------ RELEASE ------ (we ignore these for now)
		 *
		 *
		 */
		BufferedReader br = new BufferedReader(new FileReader(f));
		LogDate date = new LogDate(0, 0, 0);
		String name = defname == null ? null : defname.toUpperCase();
		TreeMap tm = new TreeMap();
		String line = br.readLine();
		StringBuffer entry = new StringBuffer();
		List entries = new LinkedList();
		TreeMap names = new TreeMap();
		while (line != null) {
			line = tabsToSpaces(line);
			if (line.startsWith("(") && line.indexOf(")") >= 8 ) {
				if (date != null) {
					if (entry.length() > 0) {
						entries.add(entry.toString());
						entry = new StringBuffer();	
					}
					if (!entries.isEmpty() && name != null) {
						names.put(name, entries);
						entries = new LinkedList();
					}
					tm.put(date, names);
					names = new TreeMap();
					String rest = line.substring(line.indexOf(")") + 1).trim();
					if (rest.length() > 0) {
						entry.append(rest);
						entry.append('\n');
					}
				}
				date = new LogDate(line.substring(1, line.indexOf(")")));
			}
			else if (line.matches("\\d\\d/\\d\\d/\\d\\d\\d\\d\\s?.*")) {
				if (date != null) {
					if (entry.length() > 0) {
						entries.add(entry.toString());
						entry = new StringBuffer();	
					}
					if (!entries.isEmpty() && name != null) {
						names.put(name, entries);
						entries = new LinkedList();
					}
					tm.put(date, names);
					names = new TreeMap();
					if (line.length() > 10) {
						String rest = line.substring(11).trim();
						if (rest.length() > 0) {
							entry.append(rest);
							entry.append('\n');
						}
					}
				}
				date = new LogDate(line.substring(0, 10));
			}
			else if (line.matches("\\d\\d/\\d\\d/\\d\\d\\s?.*")) {
				if (date != null) {
					if (entry.length() > 0) {
						entries.add(entry.toString());
						entry = new StringBuffer();	
					}
					if (!entries.isEmpty() && name != null) {
						names.put(name, entries);
						entries = new LinkedList();
					}
					tm.put(date, names);
					names = new TreeMap();
					if (line.length() > 8) {
						String rest = line.substring(9).trim();
						if (rest.length() > 0) {
							entry.append(rest);
							entry.append('\n');
						}
					}
				}
				date = new LogDate(line.substring(0, 8));
			}
			else if (line.startsWith("*** ")) {
				entry.append(line.trim());
				entry.append('\n');
			}
			else if (line.startsWith(":::")) {
				if (!entries.isEmpty() && name != null) {
					names.put(name, entries);
					entries = new LinkedList();
				}
				name = line.substring(3).trim().toUpperCase();
			}
			else if (line.trim().length() == 0 && entry.length() > 0) {
				if (date == null) {
					System.err.println("Warning: entry without a date");
				}
				else {
					entries.add(entry.toString());
					entry = new StringBuffer();
				}
			}
			else if (line.trim().length() > 0) {
				entry.append(line);
				entry.append('\n');
			}
			line = br.readLine();
		}
		entries.add(entry.toString());
		if (name != null) {
			names.put(name, entries);
		}
		if (date != null) {
			tm.put(date, names);
		}

		br.close();
		return tm;
	}
	
	public static String tabsToSpaces(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\t') {
				sb.append("    ");
			}
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static void insert(TreeMap dest, TreeMap src) {
		Iterator i = src.keySet().iterator();
		while (i.hasNext()) {
			LogDate date = (LogDate) i.next();
			TreeMap srcnames = (TreeMap) src.get(date);
			if (dest.containsKey(date)) {
				TreeMap destnames = (TreeMap) dest.get(date);
				destnames.putAll(srcnames);
			}
			else {
				dest.put(date, srcnames);
			}
		}
	}
	
	public static void write(TreeMap map, File dest) throws IOException {
		PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(dest)));
		Iterator i = map.keySet().iterator();
		while (i.hasNext()) {
			LogDate date = (LogDate) i.next();
			TreeMap names = (TreeMap) map.get(date);
			bw.print("(");
			bw.print(date.toMMDDYYYY());
			bw.println(")");
			
			Iterator j = names.keySet().iterator();
			while (j.hasNext()) {
				String name = (String) j.next();
				List entries = (List) names.get(name);
				bw.println();
				bw.print("::: ");
				bw.println(name);
				bw.println();
				
				Iterator k = entries.iterator();
				while (k.hasNext()) {
					String entry = (String) k.next();
					bw.println(entry);
				}
			}
		}
		bw.close();
	}
	
	public static class LogDate implements Comparable {
		private int year, month, day;
		
		public LogDate(int year, int month, int day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}
		
		public LogDate(String date) {
			StringTokenizer st = new StringTokenizer(date, "/");
			try {
				this.month = Integer.parseInt(st.nextToken());
				this.day = Integer.parseInt(st.nextToken());
				this.year = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException e) {
				System.err.println("Warning: could not parse date: " + date);
			}
			if (this.year < 100) {
				if (this.year > 50) {
					this.year += 1900;
				}
				else {
					this.year += 2000;
				}
			}
		}
		
		public boolean equals(Object o) {
			if (o instanceof LogDate) {
				LogDate other = (LogDate) o;
				return day == other.day && month == other.month && year == other.year;
			}
			return false;
		}
		
		public int hashCode() {
			return year * 500 + month * 40 + day;
		}
		
		public int compareTo(Object o) {
			LogDate other = (LogDate) o;
			return -((year - other.year) * 500 + (month - other.month) * 40 + (day - other.day));
		}
		
		public String toMMDDYYYY() {
			StringBuffer sb = new StringBuffer();
			if (month < 10) sb.append('0');
			sb.append(month);
			sb.append('/');
			if (day < 10) sb.append('0');
			sb.append(day);
			sb.append('/');
			sb.append(year);
			return sb.toString();
		}
		
		public String toString() {
			return toMMDDYYYY();
		}
	}
}
