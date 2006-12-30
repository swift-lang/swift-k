/*
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class VDL2ErrorTranslator {
	public static final Logger logger = Logger.getLogger(VDL2ErrorTranslator.class);

	private static VDL2ErrorTranslator translator;

	public synchronized static VDL2ErrorTranslator getDefault() {
		if (translator == null) {
			translator = new VDL2ErrorTranslator();
			translator.load();
		}
		return translator;
	}

	private List entries;

	public void load() {
		try {
			entries = new ArrayList();
			InputStream is = VDL2ErrorTranslator.class.getClassLoader().getResourceAsStream(
					"error.properties");
			if (is == null) {
				throw new FileNotFoundException("Could not open resource");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			StringBuffer sb = new StringBuffer();
			String regexp = null, replacement;

			String line = br.readLine();
			while (line != null) {
				if (line.startsWith("#")) {
					// comment
				}
				else if (line.trim().equals("->")) {
					regexp = sb.toString();
					sb = new StringBuffer();
				}
				else if (line.trim().equals(";")) {
					replacement = sb.toString();
					sb = new StringBuffer();
					entries.add(new Entry(Pattern.compile(regexp, Pattern.DOTALL), replacement));
				}
				else {
					sb.append(line);
				}
				line = br.readLine();
			}
		}
		catch (Exception e) {
			logger.warn("Error encountered while loading the error properties "
					+ "file (etc/error.properties): " + e.getMessage());
		}
	}

	public String translate(String org) {
		Iterator i = entries.iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			Matcher m = e.pattern.matcher(org);
			if (m.matches()) {
				return replace(m, e.replacement);
			}
		}
		return null;
	}

	private String replace(Matcher m, ArrayList replacement) {
		Iterator i = replacement.iterator();
		StringBuffer sb = new StringBuffer();
		while (i.hasNext()) {
			sb.append(((Replacement) i.next()).get(m));
		}
		return sb.toString();
	}

	public static class Entry {
		public Pattern pattern;
		public ArrayList replacement;

		public Entry(Pattern pattern, String replacement) {
			this.pattern = pattern;
			buildReplacement(replacement);
		}

		private void buildReplacement(String r) {
			replacement = new ArrayList();
			int last = 0;
			int index = 0;
			while (true) {
				index = r.indexOf('\\', last);

				if (index == r.length() - 1) {
					replacement.add(new RString("\\"));
					break;
				}
				else if (index == -1) {
					replacement.add(new RString(r.substring(last)));
					break;
				}
				else {
					replacement.add(new RString(r.substring(last, index)));
					char c = r.charAt(index + 1);
					if (c == '\\') {
						replacement.add(new RString("\\"));
					}
					else if (c >= '0' && c <= '9') {
						replacement.add(new Group(c - '0'));
					}
					else if (c == 'n') {
						replacement.add(new RString("\n"));
					}
					else if (c == 't') {
						replacement.add(new RString("\t"));
					}
					else {
						index--;
					}
					last = index + 2;
				}
			}
			replacement.trimToSize();
		}
	}

	public abstract static class Replacement {
		public abstract String get(Matcher m);
	}

	public static class RString extends Replacement {
		private String value;

		public RString(String value) {
			this.value = value;
		}

		public String get(Matcher m) {
			return value;
		}

		public String toString() {
			return value;
		}
	}

	public static class Group extends Replacement {
		private int index;

		public Group(int index) {
			this.index = index;
		}

		public String get(Matcher m) {
			return m.group(index);
		}

		public String toString() {
			return "&" + index;
		}
	}
}
