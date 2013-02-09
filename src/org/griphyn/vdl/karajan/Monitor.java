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

import k.rt.ConditionalYield;
import k.rt.Future;
import k.rt.FutureListener;
import k.rt.FutureValue;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.globus.cog.karajan.compiled.nodes.Node;
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
	private List<LWThread> wt;
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
					if (f instanceof FutureValue) {
						try {
							((FutureValue) f).getValue();
							entry.add("Closed");
						}
						catch (ConditionalYield y) {
							entry.add("Open");
						}
					}
					else {
						entry.add("-");
					}
					entry.add(sz);
					String fs;
					// TODO
					/*if (f instanceof FutureWrapper) {
						fs = String.valueOf(((FutureWrapper) f).listenerCount());
					}
					else {*/
						fs = f.toString();
						fs = fs.substring(fs.indexOf(' ') + 1);
					//}
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
			wt = new ArrayList<LWThread>();
			Map<LWThread, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
			for (Map.Entry<LWThread, DSHandle> entry : c.entrySet()) {
				try {
					al.add(entry.getKey().getName());
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
		Map<LWThread, DSHandle> c = WaitingThreadsMonitor.getAllThreads();
		for (Map.Entry<LWThread, DSHandle> e : c.entrySet()) {
		    dumpThread(pw, e.getKey(), e.getValue());
			pw.println();
		}
		pw.println("----");
	}

	public static void dumpThread(PrintStream pw, LWThread thr, DSHandle handle) {
	    try {
            pw.println("Thread: " + thr.getName() 
                + (handle == null ? "" : ", waiting on " + varWithLine(handle)));

            for (String t : getSwiftTrace(thr)) {
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
    
    public static String getLastCall(LWThread thr) {
        List<Object> trace = thr.getTrace();
        if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    int line = n.getLine();
                    return(n.getTextualName() + ", " + 
                            fileName(n) + ", line " + line);
                }
            }
        }
        return "?";
    }
    
    public static List<String> getSwiftTrace(LWThread thr) {
    	List<String> ret = new ArrayList<String>();
    	List<Object> trace = thr.getTrace();
    	if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    int line = n.getLine();
                	ret.add(n.getTextualName() + ", " + 
                            fileName(n) + ", line " + line);
                
                }
            }
    	}
        return ret;
    }
    
    public static List<Object> getSwiftTraceElements(LWThread thr) {
        List<Object> ret = new ArrayList<Object>();
        List<Object> trace = thr.getTrace();
        if (trace != null) {
            for (Object o : trace) {
                if (o instanceof Node) {
                    Node n = (Node) o;
                    ret.add(n.getLine());
                }
            }
        }
        return ret;
    }

    private static String fileName(Node n) {
        return new File(n.getFileName()).getName().replace(".kml", ".swift");
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
				List<FutureListener> l = Monitor.this.getListeners(rowIndex);
				if (l != null) {
					ArrayList<Object> a = new ArrayList<Object>();
					for (int i = 0; i < l.size(); i++) {
						FutureListener o = l.get(i);
						if (o instanceof LWThread.Listener) {
						    a.add(((LWThread.Listener) o).getThread().getName());
						}
						else {
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
				List<FutureListener> ls = getListeners(row);
				if (ls != null) {
					try {
					    for (FutureListener l : ls) {
					        
							displayPopup("Stack trace for " + t.getValueAt(row, 1),
									getTrace(l));
						}
					}
					catch (NullPointerException ex) {
						throw ex;
					}
				}
			}
			else if (crtdisp == THREADS) {
				Object o = wt.get(row);
				if (o instanceof Stack) {
					displayPopup("Stack trace for " + t.getValueAt(row, 0), " N/A");
				}
			}
		}
	}

	private String getTrace(FutureListener l) {
        if (l instanceof LWThread.Listener) {
            LWThread.Listener lt = (LWThread.Listener) l;
            return String.valueOf(lt.getThread().getTrace());
        }
        else {
            return "unknown";
        }
    }

    private void displayPopup(String title, String s) {
		JOptionPane.showMessageDialog(frame, s, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public List<FutureListener> getListeners(int wrindex) {
		Object o = wr.get(wrindex);
		// TODO
		/*if (o instanceof FutureWrapper) {
			return ((FutureWrapper) o).getListeners();
		}
		else {
		    return null;
		}*/
		return null;
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
