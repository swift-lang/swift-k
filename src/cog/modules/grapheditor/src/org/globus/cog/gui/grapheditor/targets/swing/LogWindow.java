//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 26, 2006
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;

import org.globus.cog.gui.grapheditor.canvas.LogConsole;

public class LogWindow extends JFrame implements WindowListener, ComponentListener, LogConsole {
	private Frame frame;
	private boolean previouslyVisible;
	private JList log;
	private JScrollPane sp;
	private Model model;
	private int height;

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public LogWindow() {
		this.height = 130;
		this.setUndecorated(true);
		this.setTitle("Log");
		this.setResizable(true);
	}

	public void attach(Frame frame) {
		this.frame = frame;
		frame.addWindowListener(this);
		frame.addComponentListener(this);
		setLocation(frame.getX() + 10, frame.getY() + frame.getHeight());
		setSize(frame.getWidth() - 20, height);
		model = new Model(this);
		log = new JList(model);
		sp = new JScrollPane(log);
		log.setCellRenderer(new Renderer());
		log.addComponentListener(this);
		sp.setViewportBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		sp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(sp, BorderLayout.CENTER);
	}

	public void scrollToEnd() {
		sp.getViewport().scrollRectToVisible(new Rectangle(1, 9999999, 10, height));
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		setVisible(false);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
		previouslyVisible = isVisible();
		setVisible(false);
	}

	public void windowDeiconified(WindowEvent e) {
		setVisible(previouslyVisible);
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		if (e.getComponent() == frame) {
			this.setLocation(frame.getX() + 10, frame.getY() + frame.getHeight());
			this.setSize(frame.getWidth() - 20, height);
		}
		else if (e.getComponent() == log) {
			scrollToEnd();
		}
	}

	public void componentMoved(ComponentEvent e) {
		if (e.getComponent() == frame) {
			this.setLocation(frame.getX() + 10, frame.getY() + frame.getHeight());
			this.setSize(frame.getWidth() - 20, height);
		}
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void output(Color color, String message) {
		model.add(new ColoredString(color, message));
	}

	public static class Model extends AbstractListModel {
		private ArrayList items;
		private LogWindow lw;

		public Model(LogWindow lw) {
			items = new ArrayList();
			this.lw = lw;
		}

		public int getSize() {
			return items.size();
		}

		public Object getElementAt(int index) {
			return items.get(index);
		}

		public void add(ColoredString cs) {
			items.add(cs);
			super.fireIntervalAdded(cs, items.size() - 1, items.size() - 1);
		}
	}

	public static class ColoredString {
		private Color color;
		private String str;

		public ColoredString(Color color, String str) {
			this.color = color;
			this.str = str;
		}

		public Color getColor() {
			return color;
		}

		public String getStr() {
			return str;
		}
	}

	public static class Renderer implements ListCellRenderer {
		private JLabel label;

		public Renderer() {
			label = new JLabel();
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			ColoredString cs = (ColoredString) value;
			label.setText(cs.getStr());
			label.setForeground(cs.getColor());
			return label;
		}

	}
}
