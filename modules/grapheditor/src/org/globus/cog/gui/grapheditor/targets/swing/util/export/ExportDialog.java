
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Mar 4, 2004
 *
 */
package org.globus.cog.gui.grapheditor.targets.swing.util.export;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.views.SwingView;
import org.globus.cog.gui.util.UITools;

public class ExportDialog extends JDialog
	implements
		ItemListener,
		ActionListener {
	private static Logger logger = Logger.getLogger(ExportDialog.class);
	private CanvasView view;
	private JComboBox format;
	private LinkedHashMap formatMap;
	private JPanel panelContainer;
	private JButton export, cancel;
	private ExportPanel crtPanel;

	public ExportDialog(CanvasView view) {
		this.view = view;
		setModal(true);
		setTitle("Export Graph");
		formatMap = new LinkedHashMap();
		formatMap.put("Select format...", null);
		formatMap.put("HTML", HTMLExportPanel.class);
		formatMap.put("PostScript", EPSExportPanel.class);
		formatMap.put("SVG", SVGExportPanel.class);
		formatMap.put("Dot", DotExportPanel.class);
		getContentPane().setLayout(new BorderLayout());
		JLabel label = new JLabel("Export format: ");
		Container container = new Container();
		container.setLayout(new FlowLayout(FlowLayout.LEFT));
		container.add(label);
		format = new JComboBox();
		Iterator i = formatMap.keySet().iterator();
		while (i.hasNext()) {
			format.addItem(i.next());
		}
		format.addItemListener(this);
		container.add(format);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(container, BorderLayout.NORTH);
		panelContainer = new JPanel();
		panelContainer.setLayout(new BorderLayout());
		getContentPane().add(panelContainer, BorderLayout.CENTER);
		container = new Container();
		container.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		container.add(cancel);
		export = new JButton("Export");
		export.addActionListener(this);
		export.setEnabled(false);
		container.add(export);
		getContentPane().add(container, BorderLayout.SOUTH);
		setSize(400, 300);
		UITools.center((Container) ((SwingView) view).getComponent(), this);
		show();
	}

	public void itemStateChanged(ItemEvent e) {
		Class panelClass = (Class) formatMap.get(e.getItem());
		panelContainer.removeAll();
		if (panelClass != null) {
			try {
				ExportPanel panel = (ExportPanel) panelClass.newInstance();
				panel.setView(view);
				panel.setup();
				panelContainer.add(panel, BorderLayout.CENTER);
				panelContainer.setBorder(BorderFactory.createTitledBorder(e
					.getItem()
					+ " export options:"));
				if (panel != null) {
					export.setEnabled(true);
				}
				crtPanel = panel;
			}
			catch (Exception ex) {
				logger.warn("Could not instantiate panel " + panelClass, ex);
				export.setEnabled(false);
			}
		}
		else {
			export.setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancel) {
			hide();
			dispose();
		}
		else if (e.getSource() == export) {
			if (crtPanel != null) {
				crtPanel.export();
				hide();
				dispose();
			}
			else {
				logger.warn("Export clicked but no panel is selected");
			}
		}
	}
}
