
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.targets.swing.views;


import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.globus.cog.gui.grapheditor.canvas.CanvasEvent;
import org.globus.cog.gui.grapheditor.canvas.CanvasEventListener;
import org.globus.cog.gui.grapheditor.canvas.views.CanvasView;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.util.ColumnsEditor;
import org.globus.cog.gui.grapheditor.util.tables.ExtendedTable;
import org.globus.cog.gui.grapheditor.util.tables.IntrospectiveTableModel;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphListener;
import org.globus.cog.util.graph.Node;

/**
 * Implements a table view of the objects and their properties. It allows for
 * filtering of the nodes based on their class types.
 */
public class ListView extends SwingView
	implements
		CanvasView,
		GraphListener,
		CanvasActionListener,
		ColumnsEditor.Listener,
		CanvasEventListener, DragGestureListener, ListSelectionListener, DragSourceListener {
	private IntrospectiveTableModel tableModel;
	private Collection nodes;
	private JTable table;
	private CanvasAction columns;
	private ColumnsEditor columnsEditor;
	private Component lastSelected;
	
	private DragSource dragSource;

	public ListView() {
		super();
		setName("List View");
		columns = new CanvasAction("30#View>51#Columns...",
			CanvasAction.ACTION_NORMAL);
		columns.addCanvasActionListener(this);
		tableModel = new IntrospectiveTableModel();
		table = new ExtendedTable(tableModel);
		table.setAutoCreateColumnsFromModel(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getSelectionModel().addListSelectionListener(this);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_COPY, this);
		setComponent(table);
	}

	private void updateObjects() {
		if (getGraph() == null) {
			return;
		}
		nodes = new ArrayList(getGraph().nodeCount());
		Iterator i = getGraph().getNodesIterator();
		while (i.hasNext()) {
			Node node = (Node) i.next();
			nodes.add(node.getContents());
		}
		tableModel.setObjects(nodes);
	}

	public void graphChanged(GraphChangedEvent e) {

	}

	public void invalidate() {
		super.invalidate();
		updateObjects();
	}

	public void canvasActionPerformed(CanvasActionEvent e) {
		if (e.getType() == CanvasActionEvent.PERFORM) {
			if (e.getCanvasAction() == columns) {
				if (tableModel == null) {
					return;
				}
				if (columnsEditor != null) {
					columnsEditor.toFront();
					return;
				}
				columnsEditor = new ColumnsEditor(tableModel.getColumns(), this);
				columnsEditor.setVisible(true);
			}
		}
	}

	public void activate() {
		super.activate();
		getSwingRenderer().addMenuItem(columns);
	}

	public void enable() {
		updateObjects();
		if (getCanvas().getOwner().hasProperty("listview.columns")) {
			LinkedHashMap columns = (LinkedHashMap) getCanvas().getOwner()
				.getPropertyValue("listview.columns");
			tableModel.setColumns(columns);
		}
		getCanvas().addCanvasEventListener(this);
	}

	public void clean() {
		getSwingRenderer().removeMenuItem(columns);
		super.clean();
	}
	
	public void disable() {
		getCanvas().removeCanvasEventListener(this);
		getCanvas().getOwner().setPropertyValue("listview.columns",
			tableModel.getColumns());
		if (columnsEditor != null) {
			columnsEditor.close();
			columnsEditor = null;
		}
		tableModel.setObjects(null);
	}

	public void columnsUpdated(ColumnsEditor e) {
		tableModel.setColumns(e.getColumns());
	}

	public void editorClosed(ColumnsEditor e) {
		columnsEditor = null;
	}

	public void canvasEvent(CanvasEvent e) {
		updateObjects();
	}

	public void dragGestureRecognized(DragGestureEvent dge) {
		Object o = table.getComponentAt(dge.getDragOrigin());
		System.err.println(o);
		dragSource.startDrag(dge, DragSource.DefaultCopyDrop, new StringSelection(""), this);
	}

	public void valueChanged(ListSelectionEvent e) {
		Component c = table.getEditorComponent();
		System.err.println(c);
		if (c != null) {
			dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY, this);
		}
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}
}