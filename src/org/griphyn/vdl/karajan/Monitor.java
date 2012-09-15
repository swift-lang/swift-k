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
 * Created on Jun 17, 2006
 */
package org.griphyn.vdl.karajan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.griphyn.vdl.engine.Karajan;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.MappingParam;
import org.griphyn.vdl.mapping.Path;

public class Monitor implements ActionListener, MouseListener {
	public static final int VARS = 0;
	public static final int THREADS = 1;
	private JFrame frame;
	private JPanel buttons, display;
	private JTable t;
	private JButton futures, waiting, tasks;
	private List<Future> wr;
	private List<VariableStack> wt;
	private int crtdisp;

	public Monitor() {
		Service s = new Service();
		new Thread(s, "network debugger").start();
	}

	private synchronized void init() {
		frame = new JFrame();
		frame.setTitle("Swift Debugger");
		buttons = new JPanel();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(buttons, BorderLayout.NORTH);
		display = new JPanel();
		display.setPreferredSize(new Dimension(500, 400));
		display.setLayout(new BorderLayout());
		frame.getContentPane().add(display, BorderLayout.CENTER);

		buttons.setLayout(new FlowLayout());

		futures = new JButton("Variable dump");
		buttons.add(futures);
		futures.addActionListener(this);

		waiting = new JButton("Waiting threads");
		buttons.add(waiting);
		waiting.addActionListener(this);

		tasks = new JButton("Tasks");
		buttons.add(tasks);
		tasks.addActionListener(this);

		frame.pack();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == futures) {
			if (t != null) {
				t.removeMouseListener(this);
			}
			crtdisp = VARS;
			ArrayList<List<Object>> al = new ArrayList<List<Object>>();
			wr = new ArrayList<Future>();
			Map<DSHandle, Future> map = FutureTracker.get().getMap();
			synchronized (map) {
			    for (Map.Entry<DSHandle, Future> en : map.entrySet()) {
					List<Object> entry = new ArrayList<Object>();
					Future f = en.getValue();
					DSHandle handle = en.getKey();
					String value = "-";
					Object v;
					try {
						v = handle.getValue();
						if (v != null) {
							value = v.toString();
						}
					}
					catch (DependentException ex) {
						v = "<text color=\"red\">Exception</text>";
					}
					String h = handle.toString();
					if (h.indexOf(' ') != -1) {
						h = h.substring(0, h.indexOf(' '));
					}
					String sz = "-";
					if (handle.getType().isArray()) {
						if (handle instanceof ArrayDataNode) {
							sz = String.valueOf(((ArrayDataNode) handle).size());
						}
						else{
							sz = "unknown";
						}
					}
					entry.add(handle.getType());
					entry.add(h);
					entry.add(value);
					entry.add(f.isClosed() ? "Closed" : "Open");
					entry.add(sz);
					String fs;
					if (f instanceof FutureWrapper) {
						fs = String.valueOf(((FutureWrapper) f).listenerCount());
					}
					else {
						fs = f.toString();
						fs = fs.substring(fs.indexOf(' ') + 1);
					}
					entry.add(fs);
					entry.add("2");
					al.add(entry);
					wr.add(f);
				}
			}
			VariableModel m = new VariableModel(al);
			t = new JTable(m);
			t.getColumnModel().getColumn(1).setPreferredWidth(120);
			t.getColumnModel().getColumn(2).setPreferredWidth(50);
			t.getColumnModel().getColumn(4).setPreferredWidth(40);
			t.getColumnModel().getColumn(5).setPreferredWidth(60);
			t.getColumnModel().getColumn(6).setPreferredWidth(200);
			t.addMouseListener(this);
			JScrollPane sp = new JScrollPane(t);
			display.removeAll();
			display.setBorder(BorderFactory.createLineBorder(Color.BLUE));
			display.add(sp, BorderLayout.CENTER);
			display.validate();
			display.repaint();
		}
		else if (e.getSource() == waiting) {
			if (t != null) {
				t.removeMouseListener(this);
			}
			crtdisp = THREADS;
			ArrayList<String> al = new ArrayList<String>();
			wt = new ArrayList<VariableStack>();
			Map<VariableStack, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
			for (Map.Entry<VariableStack, DSHandle> entry : c.entrySet()) {
				try {
					al.add(String.valueOf(ThreadingContext.get(entry.getKey())));
				}
				catch (VariableNotFoundException e1) {
					al.add("unknown thread");
				}
				wt.add(entry.getKey());
			}

			ThreadModel m = new ThreadModel(al);
			t = new JTable(m);
			t.addMouseListener(this);
			JScrollPane sp = new JScrollPane(t);
			display.removeAll();
			display.setBorder(BorderFactory.createLineBorder(Color.RED));
			display.add(sp, BorderLayout.CENTER);
			display.validate();
			display.repaint();
		}
		else if (e.getSource() == tasks) {

		}
	}

	public void dumpVariables() {
		dumpVariables(System.out);
	}

	public static void dumpVariables(PrintStream ps) {
		ps.println("\nRegistered futures:");
		Map<DSHandle, Future> copy = FutureTracker.get().getMapSafe();
		for (Map.Entry<DSHandle, Future> en : copy.entrySet()) {
			Future f = en.getValue();
			AbstractDataNode handle = (AbstractDataNode) en.getKey();
			if (handle.isClosed()) {
				continue;
			}
			String value = "-";
			try {
				if (handle.getValue() != null) {
					value = "";
				}
			}
			catch (DependentException e) {
				value = "<dependent exception>";
			}
			catch (Exception e) {
			    value = "<exception>";
			}
			try {
			    ps.println(handle.getType() + " " + handle.getDisplayableName() + " " + value + " " + f);
			}
			catch (Exception e) {
				if (!handle.isClosed()) {
				    ps.println(handle.getDisplayableName() + " - error");
				    e.printStackTrace(ps);
				}
			}
			ps.println("----");
		}
	}
	
    public void dumpThreads() {
		dumpThreads(System.out);
	}

	public static void dumpThreads(PrintStream pw) {
		pw.println("\nWaiting threads:");
		Map<VariableStack, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
		for (Map.Entry<VariableStack, DSHandle> e : c.entrySet()) {
		    dumpThread(pw, e.getKey(), e.getValue());
			pw.println();
		}
		pw.println("----");
	}

	public static void dumpThread(PrintStream pw, VariableStack stack, DSHandle handle) {
	    try {
            pw.println("Thread: " + String.valueOf(ThreadingContext.get(stack)) 
                + (handle == null ? "" : ", waiting on " + varWithLine(handle)));

            for (String t : getSwiftTrace(stack)) {
                pw.println("\t" + t);
            }
        }
        catch (VariableNotFoundException e1) {
            pw.println("unknown thread");
        }
    }

    public static String varWithLine(DSHandle value) {
		String line = value.getRoot().getParam(MappingParam.SWIFT_LINE);
		Path path = value.getPathFromRoot();
		return value.getRoot().getParam(MappingParam.SWIFT_DBGNAME) + 
            (value == value.getRoot() ? "" : (path.isArrayIndex(0) ? path : "." + path)) + 
            (line == null ? "" : " (declared on line " + line + ")");
    }
    
    public static String getLastCall(VariableStack stack) {
        List<Object> trace = Trace.getAsList(stack);
        for (Object o : trace) {
            if (o instanceof FlowNode) {
                FlowNode n = (FlowNode) o;
                String traceLine = (String) n.getProperty("_traceline");
                if (traceLine != null) {
                    return(Karajan.demangle(n.getTextualName()) + ", " + 
                            fileName(n) + ", line " + traceLine);
                }
            }
        }
        return "?";
    }
    
    public static List<String> getSwiftTrace(VariableStack stack) {
    	List<String> ret = new ArrayList<String>();
    	List<Object> trace = Trace.getAsList(stack);
        for (Object o : trace) {
            if (o instanceof FlowNode) {
                FlowNode n = (FlowNode) o;
                String traceLine = (String) n.getProperty("_traceline");
                if (traceLine != null) {
                	ret.add(Karajan.demangle(n.getTextualName()) + ", " + 
                            fileName(n) + ", line " + traceLine);
                }
            }
        }
        return ret;
    }
    
    public static List<Object> getSwiftTraceElements(VariableStack stack) {
        List<Object> ret = new ArrayList<Object>();
        List<Object> trace = Trace.getAsList(stack);
        for (Object o : trace) {
            if (o instanceof FlowNode) {
                FlowNode n = (FlowNode) o;
                String traceLine = (String) n.getProperty("_traceline");
                if (traceLine != null) {
                    ret.add(o);
                }
            }
        }
        return ret;
    }

    private static String fileName(FlowNode n) {
        return new File((String) FlowNode.getTreeProperty(FlowElement.FILENAME, n)).getName().replace(".kml", ".swift");
    }

    public class VariableModel extends AbstractTableModel {
		private List<Object[]> l;

		public VariableModel(List<List<Object>> lp) {
			l = new ArrayList<Object[]>();
			Iterator<List<Object>> i = lp.iterator();
			while (i.hasNext()) {
				List<Object> s = i.next();
				Object[] e = s.toArray();
				l.add(e);
			}
		}

		public int getRowCount() {
			return l.size();
		}

		public int getColumnCount() {
			return 7;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex < 6) {
				return l.get(rowIndex)[columnIndex];
			}
			else {
				EventTargetPair[] l = Monitor.this.getListeners(rowIndex);
				if (l != null) {
					ArrayList<Object> a = new ArrayList<Object>();
					for (int i = 0; i < l.length; i++) {
						try {
							a.add(ThreadingContext.get(l[i].getEvent()));
						}
						catch (VariableNotFoundException e) {
							a.add("unknown");
						}
					}
					return a.toString();
				}
				else {
					return "[]";
				}
			}
		}

		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Type";
				case 1:
					return "Path";
				case 2:
					return "Value";
				case 3:
					return "State";
				case 4:
					return "Crt. Size";
				case 5:
					return "# of listeners";
				case 6:
					return "Threads";
				default:
					return "??";
			}
		}
	}

	public static class ThreadModel extends AbstractTableModel {
		private List<String> l;

		public ThreadModel(List<String> lp) {
			l = lp;
		}

		public int getRowCount() {
			return l.size();
		}

		public int getColumnCount() {
			return 1;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return l.get(rowIndex);
		}

		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Thread id";
				default:
					return "??";
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int row = t.rowAtPoint(e.getPoint());
			if (crtdisp == VARS) {
				EventTargetPair[] l = getListeners(row);
				if (l != null) {
					try {
						for (int i = 0; i < l.length; i++) {
							displayPopup("Stack trace for " + t.getValueAt(row, 1),
									Trace.get(l[i].getEvent()));
						}
					}
					catch (NullPointerException ex) {
						throw ex;
					}
				}
			}
			else if (crtdisp == THREADS) {
				Object o = wt.get(row);
				if (o instanceof VariableStack) {
					displayPopup("Stack trace for " + t.getValueAt(row, 0),
							Trace.get((VariableStack) o));
				}
			}
		}
	}

	private void displayPopup(String title, String s) {
		JOptionPane.showMessageDialog(frame, s, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public EventTargetPair[] getListeners(int wrindex) {
		Object o = wr.get(wrindex);
		if (o instanceof FutureWrapper) {
			return ((FutureWrapper) o).getListenerEvents();
		}
		else {
		    return null;
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public synchronized void toggle() {
		if (frame == null) {
			init();
		}
		frame.setVisible(!frame.isVisible());
	}

	private class Service implements Runnable {

		public void run() {
			try {
				ServerSocket s = new ServerSocket(10479, 0, InetAddress.getByName("localhost"));
				while (true) {
					Socket ins = s.accept();
					try {
						InputStream is = ins.getInputStream();
						OutputStream os = ins.getOutputStream();
						PrintStream ps = new PrintStream(os, true);
						ps.println("Swift Debugger");
						while (true) {
							ps.print("\n> ");
							char c = (char) is.read();
							ps.println(c + '\n');
							switch (c) {
								case 'd': {
									toggle();
									break;
								}
								case 'v': {
									dumpVariables(ps);
									break;
								}
								case 't': {
									dumpThreads(ps);
									break;
								}
								case 'q': {
									ps.println("Ending the session");
									ins.close();
									break;
								}
								default: {
									os.write("? Unknown command. Try d, v, t, or q\n".getBytes());
								}
							}
						}
					}
					catch (IOException e) {
						break;
					}
				}
			}
			catch (Exception e) {
			}
		}
	}
}
