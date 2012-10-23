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
 * Created on Dec 23, 2006
 */
package org.griphyn.vdl.karajan;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

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

	private List<Entry> entries;

	public void load() {
		try {
			entries = new ArrayList<Entry>();
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
	    for (Entry e : entries) {
			Matcher m = e.pattern.matcher(org);
			if (m.matches()) {
				return replace(m, e.replacement);
			}
		}
		return null;
	}

	private String replace(Matcher m, List<Replacement> replacement) {
		StringBuffer sb = new StringBuffer();
		for (Replacement r : replacement) {
			sb.append(r.get(m));
		}
		return sb.toString();
	}

	public static class Entry {
		public Pattern pattern;
		public ArrayList<Replacement> replacement;

		public Entry(Pattern pattern, String replacement) {
			this.pattern = pattern;
			buildReplacement(replacement);
		}

		private void buildReplacement(String r) {
			replacement = new ArrayList<Replacement>();
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
			return m.group(index).trim();
		}

		public String toString() {
			return "&" + index;
		}
	}

	public static void main(String[] args) {
		final JTextArea ta = new JTextArea();
		ta.setPreferredSize(new Dimension(630, 220));
		final JTextArea tb = new JTextArea();
		tb.setLineWrap(true);
		tb.setPreferredSize(new Dimension(630, 220));
		JButton b = new JButton("Translate");
		b.setSize(100, 30);
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.LEADING));
		frame.getContentPane().add(ta);
		frame.getContentPane().add(b);
		frame.getContentPane().add(tb);
		frame.setSize(640, 480);
		final VDL2ErrorTranslator tr = new VDL2ErrorTranslator();
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    tr.load();
			    String result = tr.translate(ta.getText());
			    if (result == null) {
				tb.setText(ta.getText());
			    }
			    else {
				tb.setText(result);
			    }
			}
		});
		frame.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}

			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
			
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		frame.setVisible(true);
	}
}
