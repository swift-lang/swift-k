// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.setup.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.globus.cog.gui.util.GridContainer;
import org.globus.cog.gui.util.GridPosition;
import org.globus.cog.gui.util.SimpleGridLayout;
import org.globus.cog.util.ImageLoader;
import org.globus.util.ConfigUtil;

/**
 * Allows simple editing of file lists through a ComboBox
 */
public class CAControl extends GridContainer implements ActionListener, ListSelectionListener {
	private static Logger logger = Logger.getLogger(CAControl.class);

	private static File COG_CA_DIR = new File(ConfigUtil.globus_dir + File.separator
			+ "cog-certificates");

	public static final String CA_RESOURCE_DIR = "certificates/";
	public static final String CA_RESOURCE_LIST = "certificateauthorities.list";

	private String[] predefinedCANames;
	private Properties predefinedCAMap, hashes;
	private Map predefinedEntries;
	private CATableModel items;
	private JTable table;
	private JButton add, remove, view;
	private String defaultPath;

	private static final ImageIcon I_DIRECTORY = ImageLoader.loadIcon("images/16x16/co/folder.png");
	private static final ImageIcon I_MISSING = ImageLoader.loadIcon("images/16x16/co/button-cancel.png");
	private static final ImageIcon I_BINARY = ImageLoader.loadIcon("images/16x16/co/binary.png");
	private static final ImageIcon I_UNKNOWN = ImageLoader.loadIcon("images/16x16/co/help.png");
	private static final ImageIcon I_PREDEFINED = ImageLoader.loadIcon("images/16x16/co/encrypted.png");

	public CAControl(String initialElement) {
		super(2, 1);

		setPreferredSize(SimpleGridLayout.Expand, 160);

		items = new CATableModel();
		table = new JTable();
		TableColumnModel cModel = new DefaultTableColumnModel();
		JCheckBox cb = new JCheckBox();
		cModel.addColumn(new TableColumn(0, 70, new SwitchRenderer(table), new SwitchEditor()));
		cModel.addColumn(new TableColumn(1, 900, new CAListEntryRenderer(), null));
		cModel.getColumn(0).setHeaderValue("Enabled");

		table.setModel(items);
		table.setColumnModel(cModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);

		JScrollPane spane = new JScrollPane(table);
		spane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spane.setPreferredSize(new Dimension(SimpleGridLayout.Expand, 120));

		loadCAList();

		setFileNames(initialElement);

		if (initialElement != null) {
			File f = new File(initialElement);
			if (f.isDirectory()) {
				defaultPath = f.getAbsolutePath();
			}
			else {
				defaultPath = f.getParent();
			}
		}
		else {
			defaultPath = "";
		}

		add = new JButton("Add...");
		add.addActionListener(this);

		remove = new JButton("Remove Selected");
		remove.addActionListener(this);
		remove.setEnabled(false);

		view = new JButton("View Selected");
		view.addActionListener(this);
		view.setEnabled(false);

		add(spane, new GridPosition(0, 0));

		Container buttons = new Container();
		buttons.setLayout(new GridLayout(1, 3));
		buttons.add(add);
		buttons.add(remove);
		buttons.add(view);
		add(buttons, new GridPosition(1, 0));
	}

	private void loadCAList() {
		try {
			predefinedCAMap = new Properties();
			hashes = new Properties();
			predefinedEntries = new Hashtable();
			InputStream is = getClass().getClassLoader().getResourceAsStream(CA_RESOURCE_LIST);
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			do {
				line = rd.readLine();
				if (line != null) {
					String[] s = line.split(":");
					s[0] = s[0].trim();
					s[1] = s[1].trim();
					predefinedCAMap.setProperty(s[0], s[1]);
					hashes.setProperty(s[1], s[0]);
				}
			} while (line != null);
			predefinedCANames = (String[]) predefinedCAMap.keySet().toArray(new String[0]);
			Arrays.sort(predefinedCANames);
			for (int i = 0; i < predefinedCANames.length; i++) {
				CAListEntry entry = createEntry(predefinedCANames[i]);
				predefinedEntries.put(predefinedCANames[i], entry);
				items.addElement(entry);
			}
		}
		catch (Exception e) {
			logger.warn("Could not load CA list", e);
			e.printStackTrace();
			predefinedCANames = new String[0];
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == add) {
			String defItem = defaultPath;

			JFileChooser JF = new JFileChooser(defItem);

			JF.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			JF.setMultiSelectionEnabled(true);
			JF.setFileHidingEnabled(false);

			int ret = JF.showOpenDialog(this);

			if (ret == JFileChooser.APPROVE_OPTION) {
				File[] files = JF.getSelectedFiles();
				for (int i = 0; i < files.length; i++) {
					items.addElement(createEntry(files[i].getAbsolutePath(), true));
				}
			}
			items.fireTableDataChanged();
		}
		else if (e.getSource() == remove) {
			items.removeEntry(table.getSelectedRow());
			items.fireTableDataChanged();
		}
		else if (e.getSource() == view) {
			CAListEntry entry = items.getEntry(table.getSelectedRow());
			TextFileViewer FW = new TextFileViewer(null, entry.isPreDefined() ? CA_RESOURCE_DIR
					+ entry.getPath() : entry.getPath(), entry.isPreDefined());
			FW.showDialog();

		}
	}

	public void valueChanged(ListSelectionEvent e) {
		int row = table.getSelectedRow();
		boolean selection = row != -1;
		remove.setEnabled(selection && !items.getEntry(row).isPreDefined());
		view.setEnabled(selection);
	}

	private void deployCA(String CAName) {
		if (!COG_CA_DIR.exists()) {
			COG_CA_DIR.mkdir();
		}
		CAName = new File(CAName).getName();
		deployResource(COG_CA_DIR, CAName);
		String hash = CAName.substring(0, CAName.lastIndexOf('.'));
		deployResource(COG_CA_DIR, hash + ".signing_policy");
	}

	private void deployResource(File cogCADir, String name) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(CA_RESOURCE_DIR + name);
		if (is == null) {
			JOptionPane.showMessageDialog(this, "Resource not found: " + name, "Internal error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(
					cogCADir, name)));
			byte[] buf = new byte[1024];
			int sz;
			do {
				sz = bis.read(buf);
				if (sz > 0) {
					bos.write(buf, 0, sz);
				}
			} while (sz > 0);
			bis.close();
			bos.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error encountered while deploying " + name + ": "
					+ e.getMessage(), "Internal error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setEnabled(boolean enabled) {
		table.setEnabled(enabled);
		add.setEnabled(enabled);
		remove.setEnabled(enabled);
		view.setEnabled(enabled);
	}

	public String getFileNames() {
		StringBuffer buf = new StringBuffer();
		Iterator i = getSelectedFileNames().iterator();
		while (i.hasNext()) {
			String path = (String) i.next();
			buf.append(path);
			if (i.hasNext()) {
				buf.append(",");
			}
		}
		return buf.toString();
	}

	public Collection getSelectedFileNames() {
		return items.getSelectedFileNames();
	}

	public void setFileNames(String fileNames) {
		items.clear();
		if (fileNames != null) {
			StringTokenizer ST = new StringTokenizer(fileNames, ",");

			while (ST.hasMoreTokens()) {
				String token = ST.nextToken();
				CAListEntry entry = createEntry(token, true);
				if (entry.isPreDefined()) {
					entry.setActive(true);
				}
				else {
					items.addElement(createEntry(token, true));
				}
				
			}
		}
		items.fireTableDataChanged();
	}

	private CAListEntry createEntry(String token) {
		return createEntry(token, false);
	}

	private CAListEntry createEntry(String token, boolean selected) {
		ImageIcon icon;
		CAListEntry entry;
		if (predefinedCAMap.containsKey(token)) {
			entry = new CAListEntry(CAListEntry.PREDEFINED, token, COG_CA_DIR + File.separator
					+ predefinedCAMap.getProperty(token));
		}
		else {
			File f = new File(token);
			String name;
			if (hashes.containsKey(f.getName())) {
				name = hashes.getProperty(f.getName());
				if (f.getParentFile().equals(COG_CA_DIR) && predefinedEntries.containsKey(name)) {
					return (CAListEntry) predefinedEntries.get(name);
				}
			}
			else {
				name = f.getAbsolutePath();
			}
			if (!f.exists()) {
				entry = new CAListEntry(CAListEntry.MISSING, name, f.getAbsolutePath());
			}
			else if (f.isDirectory()) {
				entry = new CAListEntry(CAListEntry.DIRECTORY, name, f.getAbsolutePath());
			}
			else if (f.getParentFile().equals(COG_CA_DIR)) {
				entry = new CAListEntry(CAListEntry.PREDEFINED, name, f.getAbsolutePath());
			}
			else {
				entry = new CAListEntry(CAListEntry.CACERT, name, f.getAbsolutePath());
			}
		}

		if (selected) {
			entry.setActive(true);
		}

		return entry;
	}

	private static class CAListEntryRenderer implements TableCellRenderer {
		private final JLabel label;

		public CAListEntryRenderer() {
			label = new JLabel();
			label.setOpaque(true);
			label.setIcon(I_UNKNOWN);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean cellHasFocus, int row, int col) {
			JLabel r;
			if (value instanceof JLabel) {
				r = (JLabel) value;
				r.setOpaque(true);
			}
			else if (value instanceof CAListEntry) {
				CAListEntry entry = (CAListEntry) value;
				label.setText(entry.getName());
				label.setIcon(entry.getIcon());
				label.setToolTipText(entry.getPath());
				r = label;
			}
			else if (value != null) {
				label.setText(value.toString());
				r = label;
			}
			else {
				label.setText("Null");
				r = label;
			}
			if (isSelected) {
				r.setBackground(table.getSelectionBackground());
				r.setForeground(table.getSelectionForeground());
			}
			else {
				r.setBackground(table.getBackground());
				r.setForeground(table.getForeground());
			}

			return r;
		}

	}

	private static class SwitchRenderer implements TableCellRenderer {
		private final JCheckBox cb;
		private Color selected, normal;

		public SwitchRenderer(JTable table) {
			cb = new JCheckBox();
			normal = table.getBackground();
			selected = table.getSelectionBackground();
		}

		public Component getTableCellRendererComponent(JTable list, Object value,
				boolean isSelected, boolean cellHasFocus, int row, int col) {
			JCheckBox r;
			if (value instanceof Boolean) {
				cb.setSelected(((Boolean) value).booleanValue());
			}
			if (isSelected) {
				cb.setBackground(selected);
			}
			else {
				cb.setBackground(normal);
			}
			return cb;
		}
	}

	private static class SwitchEditor extends DefaultCellEditor implements ActionListener {
		private JCheckBox cb;
		private CAListEntry current;

		public SwitchEditor() {
			super(new JCheckBox());
			cb = (JCheckBox) getComponent();
			cb.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			current = (CAListEntry) table.getModel().getValueAt(row, 1);
			cb.setSelected(current.isActive());
			return cb;
		}

		public void actionPerformed(ActionEvent e) {
			// Defensive programming: make sure something won't happen even
			// though you know it can't happen
			CAListEntry entry = current;
			if (entry != null) {
				entry.setActive(cb.isSelected());
			}
		}
	}

	private static class CATableModel extends AbstractTableModel {
		private List entries, predef;

		public CATableModel() {
			entries = new ArrayList();
			predef = new ArrayList();
		}

		public void addElement(CAListEntry entry) {
			if (entry.isPreDefined()) {
				predef.add(entry);
			}
			else {
				entries.add(entry);
			}
		}

		public void clear() {
			entries.clear();
		}

		public Collection getSelectedFileNames() {
			ArrayList r = new ArrayList();
			addSelected(r, entries);
			addSelected(r, predef);
			return r;
		}

		private void addSelected(List target, Collection source) {
			Iterator i = source.iterator();
			while (i.hasNext()) {
				CAListEntry n = (CAListEntry) i.next();
				if (n.isActive()) {
					target.add(n.getPath());
				}
			}
		}

		public int getRowCount() {
			return entries.size() + predef.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return Boolean.valueOf(getEntry(row).isActive());
			}
			else {
				return getEntry(row);
			}
		}

		public CAListEntry getEntry(int index) {
			if (index < entries.size()) {
				return (CAListEntry) entries.get(index);
			}
			else {
				return (CAListEntry) predef.get(index - entries.size());
			}
		}

		public void removeEntry(int index) {
			entries.remove(index);
		}

		public String getColumnName(int column) {
			if (column == 0) {
				return "Enabled";
			}
			else {
				return "";
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}
	}

	public class CAListEntry {
		public static final int PREDEFINED = 0;
		public static final int CACERT = 1;
		public static final int DIRECTORY = 2;
		public static final int MISSING = 3;
		public static final int UNKNOWN = 4;

		private boolean active;
		private String name, path;
		private int type;
		private ImageIcon icon;

		public CAListEntry(int type, String name, String path) {
			switch (type) {
				case PREDEFINED: {
					icon = I_PREDEFINED;
					break;
				}
				case CACERT: {
					icon = I_BINARY;
					break;
				}
				case DIRECTORY: {
					icon = I_DIRECTORY;
					break;
				}
				case MISSING: {
					icon = I_MISSING;
					break;
				}
				default: {
					icon = I_UNKNOWN;
					break;
				}
			// just for symmetry
			}
			this.type = type;
			this.name = name;
			this.active = false;
			this.path = path;
		}

		public boolean isActive() {
			return active;
		}

		public boolean isPreDefined() {
			return type == PREDEFINED;
		}

		public void setActive(boolean active) {
			this.active = active;
			if (active && isPreDefined()) {
				deployCA(path);
			}
		}

		public ImageIcon getIcon() {
			return icon;
		}

		public void setIcon(ImageIcon icon) {
			this.icon = icon;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

	}
}