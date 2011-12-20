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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
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
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;

public class Monitor implements ActionListener, MouseListener {
	public static final int VARS = 0;
	public static final int THREADS = 1;
	private JFrame frame;
	private JPanel buttons, display;
	private JTable t;
	private JButton futures, waiting, tasks;
	private List wr, wt;
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
			ArrayList al = new ArrayList();
			wr = new ArrayList();
			Map<DSHandle, Future> map = FutureTracker.get().getMap();
			synchronized (map) {
			    for (Map.Entry<DSHandle, Future> en : map.entrySet()) {
					List entry = new ArrayList();
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
			ArrayList al = new ArrayList();
			wt = new ArrayList();
			Collection c = WaitingThreadsMonitor.getAllThreads();
			Iterator i = c.iterator();
			while (i.hasNext()) {
				VariableStack stack = (VariableStack) i.next();
				try {
					al.add(String.valueOf(ThreadingContext.get(stack)));
				}
				catch (VariableNotFoundException e1) {
					al.add("unknown thread");
				}
				wt.add(stack);
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
		Map<DSHandle, Future> map = FutureTracker.get().getMapSafe();
		synchronized (map) {
			for (Map.Entry<DSHandle, Future> en : map.entrySet()) {
				Future f = en.getValue();
				AbstractDataNode handle = (AbstractDataNode) en.getKey();
				String value = "-";
				try {
					if (handle.getValue() != null) {
						value = "";
					}
				}
				catch (DependentException e) {
					value = "Dependent exception";
				}
				ps.println(handle.getType() + " " + handle.getDisplayableName() + " " + value + " " + f);
			}
			ps.println("----");
		}
	}

	public void dumpThreads() {
		dumpThreads(System.out);
	}

	public static void dumpThreads(PrintStream pw) {
		pw.println("\nWaiting threads:");
		Collection c = WaitingThreadsMonitor.getAllThreads();
		Iterator i = c.iterator();
		while (i.hasNext()) {
			VariableStack stack = (VariableStack) i.next();
			try {
				pw.println(String.valueOf(ThreadingContext.get(stack)));
			}
			catch (VariableNotFoundException e1) {
				pw.println("unknown thread");
			}
		}
		pw.println("----");
	}

	public class VariableModel extends AbstractTableModel {
		private List l;

		public VariableModel(List lp) {
			l = new ArrayList();
			Iterator i = lp.iterator();
			while (i.hasNext()) {
				List s = (List) i.next();
				Iterator j = s.iterator();
				Object[] e = new Object[6];
				e = s.toArray();
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
				return ((Object[]) l.get(rowIndex))[columnIndex];
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
		private List l;

		public ThreadModel(List lp) {
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
