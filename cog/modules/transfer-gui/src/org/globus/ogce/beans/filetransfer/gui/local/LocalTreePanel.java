//LocalTreePanel.java to display the local tree window
package org.globus.ogce.beans.filetransfer.gui.local;

import org.apache.log4j.Logger;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;
import org.globus.ogce.beans.filetransfer.util.DirQueue;
import org.globus.ogce.beans.filetransfer.util.GridTransferable;
import org.globus.ogce.beans.filetransfer.util.SortFtpString;
import org.globus.tools.ui.util.UITools;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


public class LocalTreePanel extends JPanel
        implements ActionListener, DragSourceListener,
        DragGestureListener, DropTargetListener, Serializable {
    private static Logger logger =
            Logger.getLogger(LocalTreePanel.class.getName());
    public long firstClickTime = 0;
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    protected DefaultMutableTreeNode seekNode;
    protected DefaultMutableTreeNode lastExp;

    protected JToolBar toolBar;
    public Toolkit toolkit;
    public String osname;

    public String selectedPath;
    public JLabel statusText;
    public JTextField txtField;
    public boolean noselection;
    public DragSource dragsource = null;
    private int opSystem;
    public int FrameId = 0;
    String dirname;
    public LocalEditFrame editFrame;
    String selectedDestination = null;
    boolean bean = false;

    //FileToTransfer fileToTransfer;
    private String draggedValues[];

    private DirQueue queue;
    private String oldTextField;
    private String as1 = "";
    private String asn = new String("");
    TreePath destinationPath;
    protected TreePath SelectedTreePath = null;
    protected TreePath dragtreepath = null;
    private boolean dragEnable = true;
    File deleteFile = null;
    boolean deleteFlag = true;
    DefaultMutableTreeNode defaultmutabletreenode = null;
    MutableTreeNode mutabletreenode = null;
    JButton deleteButton = null;
    JScrollPane jscrollpane = null;
    public String LclFile_Sep = new String(System.getProperty("file.separator"));
    String userHome = System.getProperty("user.home") + LclFile_Sep;
    boolean home = true;
    private PropertyChangeSupport pceListeners;



    protected Vector dirlisteners = new Vector();

    /** Register an action listener to be notified when a button is pressed */
    public void addDirListener(DirListener l) {
        dirlisteners.addElement(l);
    }

    /** Remove an Answer listener from our list of interested listeners */
    public void removeDirListener(DirListener l) {
        dirlisteners.removeElement(l);
    }

    /** Send an event to all registered listeners */
    public void fireDirEvent(DirEvent e, String path,
                             LocalTreePanel local) {

        Vector list = (Vector) dirlisteners.clone();
        for (int i = 0; i < list.size(); i++) {
            DirListener listener = (DirListener) list.elementAt(i);
            try {
                switch (e.getID()) {
                    case DirEvent.DIR:
                        listener.dropLocal(e, path, local);
                        break;
                    case DirEvent.LOCALDRAG:
                        listener.dragLocal(e, path, local);
                        break;

                }
            } catch (Exception direx) {
                direx.printStackTrace();
            }
        }
    }


    public LocalTreePanel() {
        bean = true;
        init();
        FileTransferMainPanel.mainPanel.registerLocalComponent(this);
        //statusOut("Please click on the view status button to view details of transfer. ");

    }

    public LocalTreePanel(String label) {
        init();
    }

    public void init() {
        seekNode = null;
        lastExp = null;
        toolkit = Toolkit.getDefaultToolkit();
        selectedPath = new String();
        noselection = true;

        osname = new String(System.getProperty("os.name"));
        if (osname.indexOf("Windows") >= 0) {
            opSystem = 0;
        }

        if ((osname.indexOf("Linux") >= 0)
                || (osname.indexOf("Solaris") >= 0)
                || (osname.indexOf("Unix") >= 0)
                || (osname.indexOf("Mac") >= 0)
                || (osname.indexOf("Irix") >= 0)
                || (osname.indexOf("AIX") >= 0)
                || (osname.indexOf("HP-UX") >= 0)
                || (osname.indexOf("MPE") >= 0)
                || (osname.indexOf("FreeBSD") >= 0)) {
            opSystem = 1;
        }
        if ((osname.indexOf("OS/2") >= 0)
                || (osname.indexOf("NetWare") >= 0)) {
            opSystem = 2;
        }

        rootNode = new DefaultMutableTreeNode("Local System");
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new LclTreeModelListener());
        mk_Drives();
        tree = new JTree(treeModel);

        tree.addTreeWillExpandListener(new MyTreeWillExpandListener());
        tree.addMouseListener(new MyAdapter());  // for double clicking
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent treeselectionevent) {
                DefaultMutableTreeNode defaultmutabletreenode =
                        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (defaultmutabletreenode == null) {
                    return;
                } else {
                    javax.swing.tree.TreeNode atreenode[] = null;
                    atreenode = defaultmutabletreenode.getPath();
                    selectedPath = returnPath(atreenode);
                    SelectedTreePath = treeselectionevent.getNewLeadSelectionPath();
                    return;
                }
            }

        });
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(4);
        tree.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(tree);
        LclRenderer lclrenderer = new LclRenderer();
        tree.setCellRenderer(lclrenderer);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        jscrollpane = new JScrollPane(tree);
        new DropTarget(tree, this);
        dragsource = DragSource.getDefaultDragSource();
        dragsource.createDefaultDragGestureRecognizer(tree, 3, this);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);


        JButton jbutton = createButton("images/16x16/reload.png",
                "Click here to Refresh",
                "4");
        toolBar.add(jbutton);
        jbutton = createButton("images/16x16/up.png",
                "Click here to go one Directory Up",
                "6");
        toolBar.add(jbutton);
        jbutton = createButton("images/16x16/view_text.png",
                "Click here to view Directory Info",
                "3");
        toolBar.add(jbutton);
        JToggleButton jbutton1 = createToggleButton("images/16x16/folder_home.png",
                "Toggle between root and home directories",
                "2");
        toolBar.add(jbutton1);


        toolBar.addSeparator();
        toolBar.addSeparator();
        jbutton = createButton("images/16x16/folder.png",
                "Click here to Create a new Directory",
                "7");
        toolBar.add(jbutton);


        jbutton = createButton("images/16x16/folder_rename.png",
                "Click here to Rename a File/Directory",
                "5");
        toolBar.add(jbutton);

        deleteButton = createButton("images/16x16/folder_delete.png",
                "Click here to delete a File/Directory",
                "1");
        toolBar.add(deleteButton);

        if (bean) {
            toolBar.addSeparator();
            toolBar.addSeparator();
            toolBar.addSeparator();

            jbutton = createButton("images/16x16/view_text.png",
                    "View the status window.",
                    "8");
            toolBar.add(jbutton);
            jbutton = createButton("images/16x16/view_msg.png",
                    "View the messages window.",
                    "9");
            toolBar.add(jbutton);
        }


        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        txtField = new JTextField("Address Bar");
        setOldField();
        txtField.addActionListener(this);
        jpanel.add(txtField, "South");
        JPanel jpanel1 = new JPanel(new BorderLayout());
        statusText = new JLabel("Status  :    Local Files displayed     ");
        //jpanel1.add(statusText, "West");


        jpanel.add(toolBar, "North");

        setLayout(new BorderLayout());
        add(jpanel, "North");
        add(jscrollpane, "Center");
        add(jpanel1, "South");
        setVisible(true);
        queue = new DirQueue();
    }

    public String getTextField() {
        return txtField.getText();
    }

    public void setFrameId(int id) {
        FrameId = id;
    }

    public int getFrameId() {
        return FrameId;
    }

    public void setOldField() {
        oldTextField = txtField.getText();
    }

    public void setTextField(String newValue) {
        chDir(newValue);
        pceListeners.firePropertyChange("txtField", oldTextField, newValue);
        setOldField();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    	if (pceListeners == null) {
    		pceListeners = new PropertyChangeSupport(this);
		}
        pceListeners.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pceListeners.removePropertyChangeListener(l);
    }

    public void actionPerformed(ActionEvent actionevent) {
        if (actionevent.getSource() == txtField) {
            setTextField(txtField.getText());
        } else {
            String s = actionevent.getActionCommand();
            int i = 0;
            try {
                i = Integer.valueOf(s).intValue();
            } catch (NumberFormatException numberformatexception) {
                numberformatexception.printStackTrace();
            }
            switch (i) {
                case 1: // delete the file
                    removeCurrentNode();
                    break;

                case 2: //toggle home and root
                    toggleHomeRoot();
                    break;

                case 3: // display dir info
                    _actionDirInfo("");
                    break;

                case 4: // refresh the window
                    _actionRefresh();
                    break;

                case 5: // rename the file or dir
                    _actionRename();
                    break;

                case 6: // go up one directory
                    _actionGo1DirUp();
                    break;

                case 7:
                    _actionMakeDir();
                    break;

                case 8:
                    FileTransferMainPanel.mainPanel.showStatusWindow();
                    break;

                case 9:
                    FileTransferMainPanel.mainPanel.showMessagesWindow();
                    break;

            }
        }

    }

    public void toggleHomeRoot() {
        clear();
        if (!home) {
            DefaultMutableTreeNode defaultmutabletreenode1 = addObject(null, userHome);
            addObject(defaultmutabletreenode1, "");
            home = true;
            JOptionPane.showMessageDialog(this,
                    "Changed to new address: " + userHome);
            txtField.setText(userHome);
        } else {

            if (opSystem == 1) {
                DefaultMutableTreeNode defaultmutabletreenode1 = addObject(null, "//");
                addObject(defaultmutabletreenode1, "");
                home = false;
                JOptionPane.showMessageDialog(this,
                        "Changed to new address: //");
                txtField.setText("//");
            } else {
                mk_Drives();
                home = false;
                JOptionPane.showMessageDialog(this,
                        "Changed to new address: System drives");
                txtField.setText("Local System drives");
            }
        }

    }

    public void displayFile() {
        String as[] = returnSelectedFiles(true);
        if (as != null) {
            editFrame = new LocalEditFrame(as, LclFile_Sep);
            editFrame.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent windowevent) {
                    editFrame = null;
                    System.gc();
                }

            });
            editFrame.pack();
            editFrame.setVisible(true);
        }
    }

    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    public JButton createButton(String s, String s1, String s2) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL jarCogImage = classLoader.getResource(s);
        JButton jbutton = new JButton(new ImageIcon(jarCogImage));
        jbutton.setActionCommand(s2);
        jbutton.addActionListener(this);
        jbutton.setToolTipText(s1);
        Dimension dimension = new Dimension(20, 20);
        jbutton.setMaximumSize(dimension);
        jbutton.setMinimumSize(dimension);
        jbutton.setPreferredSize(dimension);
        jbutton.setRequestFocusEnabled(false);
        return jbutton;
    }

    public JToggleButton createToggleButton(String s, String s1, String s2) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL jarCogImage = classLoader.getResource(s);
        JToggleButton jbutton = new JToggleButton(new ImageIcon(jarCogImage));
        jbutton.setActionCommand(s2);
        jbutton.addActionListener(this);
        jbutton.setToolTipText(s1);
        Dimension dimension = new Dimension(20, 20);
        jbutton.setMaximumSize(dimension);
        jbutton.setMinimumSize(dimension);
        jbutton.setPreferredSize(dimension);
        jbutton.setRequestFocusEnabled(false);
        return jbutton;
    }

    public void mk_Drives() { 
    	
        File[] roots = File.listRoots();
        try {
            for (int i = 0; i < roots.length; i++) {
            	File root = roots[i];
            	if (root.isDirectory()) {
                  DefaultMutableTreeNode defaultmutabletreenode = addObject(null, root.getCanonicalPath());                      
                  addObject(defaultmutabletreenode, "");
            	}
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        //For Windows, let home = false
        if (opSystem == 0) {
        	home = false;
        }    	

    }

    public DefaultMutableTreeNode addObject(Object obj) {
        DefaultMutableTreeNode defaultmutabletreenode = null;
        TreePath treepath = tree.getSelectionPath();
        if (treepath == null) {
            toolkit.beep();
            return null;
        }
        defaultmutabletreenode = (DefaultMutableTreeNode) treepath.getLastPathComponent();
        if (defaultmutabletreenode == rootNode) {
            toolkit.beep();
            return null;
        } else {
            return addObject(defaultmutabletreenode, obj, true);
        }
    }

    public DefaultMutableTreeNode addObject
            (DefaultMutableTreeNode defaultmutabletreenode, Object obj) {
        return addObject(defaultmutabletreenode, obj, false);
    }

    public DefaultMutableTreeNode addObject
            (DefaultMutableTreeNode defaultmutabletreenode, Object obj,
             boolean flag) {
        DefaultMutableTreeNode defaultmutabletreenode1 =
                new DefaultMutableTreeNode(obj);
        if (defaultmutabletreenode == null) {
            defaultmutabletreenode = rootNode;
        }
        treeModel.insertNodeInto(defaultmutabletreenode1,
                defaultmutabletreenode,
                defaultmutabletreenode.getChildCount());
        if (flag) {
            tree.scrollPathToVisible(new TreePath(defaultmutabletreenode1.getPath()));
        }
        return defaultmutabletreenode1;
    }

    public void listDir(DefaultMutableTreeNode defaultmutabletreenode,
                        String s,
                        boolean flag) {

        File file = new File(s);

        String as[] = null;
        as = file.list();
        if (as == null) {
            return;
        }
        int i = as.length;
        SortFtpString.startSort(as);
        if (i == 0) {
            addObject(defaultmutabletreenode, "", false);
            return;
        }
        for (int j = 0; j < i; j++) {
            File file1;
            if (file.toString().endsWith(LclFile_Sep)) {
                file1 = new File(file.toString() + as[j]);
            } else {
                file1 = new File(file.toString() + LclFile_Sep + as[j]);
            }
            if (file1.isDirectory()) {
                DefaultMutableTreeNode defaultmutabletreenode1 = addObject(defaultmutabletreenode, as[j] + LclFile_Sep, flag);
                addObject(defaultmutabletreenode1, "", false);
            }            
        }

        for (int k = 0; k < i; k++) {
            File file2;
            if (file.toString().endsWith(LclFile_Sep)) {
                file2 = new File(file.toString() + as[k]);
            } else {
                file2 = new File(file.toString() + LclFile_Sep + as[k]);
            }
            if (!file2.isDirectory()) {
                addObject(defaultmutabletreenode, as[k], flag);
            }
            Font font = new Font("Dialog", Font.PLAIN, 12);
            Enumeration keys = UIManager.getLookAndFeelDefaults()
            .keys();

            while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            if (UIManager.get(key) instanceof Font) {
            UIManager.put(key, font);
            }
            } 
        }

    }

    public TreePath[] returnSelectedPaths() {
        DefaultMutableTreeNode defaultmutabletreenode =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        TreePath atreepath[] = null;
        if (defaultmutabletreenode == null) {
            return atreepath;
        }
        //atreepath = tree.getSelectionModel().getSelectionPaths();
        if (defaultmutabletreenode.isLeaf()) {
            atreepath = tree.getSelectionModel().getSelectionPaths();
            return atreepath;
        } else {
            TreePath atreepath1[] = tree.getSelectionPaths();
            return atreepath1;
            //		    return null;
        }

    }

    public String[] returnSelectedPaths1() {
        TreePath atreepath[] = tree.getSelectionModel().getSelectionPaths();
        String s = null;
        String as[] = null;
        if (atreepath != null) {
            int i = atreepath.length;
            as = new String[i];
            for (int j = 0; j < i; j++) {
                Object aobj[] = atreepath[j].getPath();
                int k = aobj.length;
                for (int l = 1; l < k; l++) {
                    DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) aobj[l];
                    if (!defaultmutabletreenode.isLeaf()) {
                        if (l == 1 && defaultmutabletreenode
                                != rootNode) {
                            s = new String();
                        }
                        s = s.concat(aobj[l].toString());
                    }
                }

                as[j] = new String(s);
            }

        }
        return as;
    }

    public String[] returnSelectedFiles(boolean flag) {
        TreePath atreepath[] = returnSelectedPaths();
        String as[] = null;
        if (atreepath != null) {
            int i = atreepath.length;
            as = new String[i];
            for (int j = 0; j < i; j++) {
                Object aobj[] = atreepath[j].getPath();
                int k = aobj.length;
                String s = new String();
                for (int l = 1; l < k; l++) {
                    s = s.concat(aobj[l].toString());
                }

                if (flag) {
                    as[j] = new String("file:" + s);
                } else {
                    as[j] = new String(s);
                }
            }

        }
        return as;
    }


    public DefaultMutableTreeNode seekParent(DefaultMutableTreeNode defaultmutabletreenode, String s) {
        if (defaultmutabletreenode == null) {

            return null;
        }

        int i = defaultmutabletreenode.getChildCount();
        for (int j = 0; j < i; j++) {
            javax.swing.tree.TreeNode treenode =
                    defaultmutabletreenode.getChildAt(j);
            String s1 = treenode.toString();
            if (s.compareTo(s1) == 0) {
                DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) treenode;
                return defaultmutabletreenode1;
            }
        }


        return null;
    }


    public boolean chDir(String s) {
        String s1 = s;
        if (s1.endsWith(LclFile_Sep)) {
            s1 = s1.substring(0, s1.lastIndexOf(LclFile_Sep));
        } else {
            return false;
        }
        //JOptionPane.showMessageDialog(null, "string s1=" + s1);
        StringTokenizer stringtokenizer = new StringTokenizer(s1, LclFile_Sep);
        Vector vector = new Vector();
        for (; stringtokenizer.hasMoreTokens(); vector.addElement(stringtokenizer.nextToken() + LclFile_Sep)) {
            logger.info("Adding the file separator");
        }
        if (opSystem == 0) {
            String s2 = s1.substring(0, 1).toUpperCase();
            char c = s2.charAt(0);
            char ac[] = ((String) vector.firstElement()).toCharArray();
            ac[0] = c;
            vector.setElementAt(new String(ac), 0);
        }
        if (opSystem == 1) {
            vector.add(0, new String(LclFile_Sep));
        }
        clear();
        mk_Drives();

        // treeModel.reload();
        DefaultMutableTreeNode defaultmutabletreenode = rootNode;

        int i = vector.size();

        for (int j = 0; j < i; j++) {

            defaultmutabletreenode = seekParent(defaultmutabletreenode, (String) vector.elementAt(j));
            if (defaultmutabletreenode != null) {
                tree.expandPath(new TreePath(defaultmutabletreenode.getPath()));
            }

        }

        if (defaultmutabletreenode == null) {
            setOldField();

            //statusOut("Status : Ready ");
        } else {
            //statusOut("Changed to " + s + "directory");
            //statusOut("Status : Ready ");
        }
        return true;
    }

    public void whereIsNode(DefaultMutableTreeNode defaultmutabletreenode, int i, String s) {
        seekNode = null;
        for (int j = 0; j < i; j++) {
            DefaultMutableTreeNode defaultmutabletreenode1 =
                    (DefaultMutableTreeNode) defaultmutabletreenode.getChildAt(j);
            if (defaultmutabletreenode1.isLeaf()) {
                continue;
            }
            javax.swing.tree.TreeNode atreenode[] = defaultmutabletreenode1.getPath();
            TreePath treepath = new TreePath(atreenode);
            String s1 = defaultmutabletreenode1.toString();
            if (s1.equals(s)) {
                j = i + 1;
                seekNode = defaultmutabletreenode1;
                break;
            }
            if (!tree.isExpanded(treepath)) {
                continue;
            }
            if (s1.equals(s)) {
                j = i + 1;
                seekNode = defaultmutabletreenode1;
                break;
            }
            whereIsNode(defaultmutabletreenode1, defaultmutabletreenode1.getChildCount(), s);
        }

    }

    public void _actionRefresh() {
        String s = txtField.getText();
        chDir(s);
        logger.info("The window was refreshed at dir = " + s);

    }


    public synchronized void statusOut(String s) {
        //statusText.setText(s);

    }

    class LclRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent
                (JTree jtree, Object obj, boolean flag,
                 boolean flag1, boolean flag2, int i,
                 boolean flag3) {
            super.getTreeCellRendererComponent(jtree, obj, flag,
                    flag1, flag2, i, flag3);
            if (!flag2) {
                setToolTipText("Local Path");
                if (isLocalSystem(obj)) {
                    setIcon(LclSystemIcon);
                    setToolTipText("Local Hirarchy");
                }
                if (isLocalDrive(obj)) {
                    setIcon(LclDriveIcon);
                    setToolTipText("Local Drives");
                }
            } else {
                setToolTipText("fileClickMessage");
            }
            return this;
        }

        protected boolean isLocalSystem(Object obj) {
            DefaultMutableTreeNode defaultmutabletreenode =
                    (DefaultMutableTreeNode) obj;
            Object obj1 = defaultmutabletreenode.getUserObject();
            String s = obj1.toString();
            return s.indexOf("local") >= 0;
        }

        protected boolean isLocalDrive(Object obj) {
            DefaultMutableTreeNode defaultmutabletreenode =
                    (DefaultMutableTreeNode) obj;
            Object obj1 = defaultmutabletreenode.getUserObject();
            String s = obj1.toString();
            return s.indexOf("NodeLabel") >= 0;
        }


        ImageIcon LclSystemIcon;
        ImageIcon LclDriveIcon;
        ImageIcon LclFileIcon;

        public LclRenderer() {
            LclSystemIcon = new ImageIcon("images/16x16/konsole2.png");
            LclDriveIcon = new ImageIcon("images/16x16/filesave.png");
            LclFileIcon = new ImageIcon("file.gif");
            super.setBorderSelectionColor(null);
            super.setBackgroundNonSelectionColor(null);
            super.setTextNonSelectionColor(Color.blue);
            super.setLeafIcon(LclFileIcon);
        }
    }

    class MyTreeWillExpandListener
            implements TreeWillExpandListener {
        MyTreeWillExpandListener() {
        }

        public void treeWillExpand(TreeExpansionEvent treeexpansionevent) {
        	  DefaultMutableTreeNode defaultmutabletreenode = null;
            Object aobj[] = null;
            aobj = treeexpansionevent.getPath().getPath();
            int i = aobj.length;
            defaultmutabletreenode = (DefaultMutableTreeNode) aobj[i - 1];
            if (defaultmutabletreenode == rootNode) {
                statusOut("Node was not selected to expand.");
                return;
            } else {

                DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) treeModel.getChild(aobj[i - 1], 0);
                treeModel.removeNodeFromParent(defaultmutabletreenode1);
                String s1 = returnPath(aobj);
                statusOut("Please wait..Expanding dir " + s1);
                listDir(defaultmutabletreenode, s1, false);

                txtField.setText(s1);

                noselection = false;
                tree.getSelectionModel().clearSelection();
                statusOut("Status: Ready");
                return;
            }
        }

        public void treeWillCollapse(TreeExpansionEvent treeexpansionevent) {
            DefaultMutableTreeNode defaultmutabletreenode = null;
            tree.getSelectionModel().clearSelection();
            Object aobj[] = null;
            aobj = treeexpansionevent.getPath().getPath();
            int i = aobj.length;
            if (i > 1) {
                defaultmutabletreenode = (DefaultMutableTreeNode) aobj[i - 1];
            } else {
                defaultmutabletreenode = rootNode;
            }
            if (defaultmutabletreenode == rootNode) {
                // txtField.setText("Local Path ");
                noselection = true;
                treeModel.reload(defaultmutabletreenode);
                return;
            }
            noselection = false;
            defaultmutabletreenode.removeAllChildren();
            addObject(defaultmutabletreenode, "");
            aobj = ((DefaultMutableTreeNode) defaultmutabletreenode.getParent()).getPath();
            lastExp = null;
            getLastExpandeNode(rootNode, treeexpansionevent.getPath());
            if (lastExp != null) {
                javax.swing.tree.TreeNode atreenode[] = treeModel.getPathToRoot(lastExp);
                TreePath treepath = new TreePath(atreenode);
                Object aobj1[] = treepath.getPath();
                String s1 = returnPath(aobj1);
                txtField.setText(s1);
            } else {

                //txtField.setText("Local Path ");
                noselection = true;
            }
            treeModel.reload(defaultmutabletreenode);
        }

    }

    class LclTreeModelListener
            implements TreeModelListener {

        public void treeNodesChanged(TreeModelEvent treemodelevent) {
            DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) treemodelevent.getTreePath().getLastPathComponent();
            try {
                int i = treemodelevent.getChildIndices()[0];
                defaultmutabletreenode = (DefaultMutableTreeNode) defaultmutabletreenode.getChildAt(i);
            } catch (NullPointerException nullpointerexception) {
                nullpointerexception.printStackTrace();
            }
            javax.swing.tree.TreeNode atreenode[] = null;
            atreenode = defaultmutabletreenode.getPath();
            File file = new File(returnPath(atreenode));
            File file1 = new File(selectedPath);
            file1.renameTo(file);
        }

        public void treeNodesInserted(TreeModelEvent treemodelevent) {
        }

        public void treeNodesRemoved(TreeModelEvent treemodelevent) {
        }

        public void treeStructureChanged(TreeModelEvent treemodelevent) {
        }

        LclTreeModelListener() {
        }
    }


    public String returnPath(Object aobj[]) {
        int i = aobj.length;
        String s = new String();
        for (int j = 1; j < i; j++) {
            s = s.concat(aobj[j].toString());
        }
        return s;
    }

    public void setSelectedSource() {
        draggedValues = returnSelectedFiles(false);
        File lcl = new File(draggedValues[0]);
        fireDirEvent(new DirEvent(this, DirEvent.LOCALDRAG),
                lcl.getAbsolutePath(), this);
    }

    public void setSelectedDestination() {
        String as[] = returnSelectedPaths1();
        if (as == null || as[0] == null) {
            as = new String[1];
            as[0] = new String(txtField.getText());
        }
        this.selectedDestination = as[0];
    }

    public void transfer() {
        fireDirEvent(new DirEvent(this, DirEvent.DIR), selectedDestination, this);
    }

    public void dragGestureRecognized(DragGestureEvent draggestureevent) {
        draggedValues = returnSelectedFiles(false);

        dragtreepath = SelectedTreePath;
        if (draggedValues == null) {

            return;
        } else if (!(dragEnable)) {
            JOptionPane.showMessageDialog(null, "Please wait till the status\n bar shows drag enabled.");
            return;

        } else {
            GridTransferable Gridtransferable =
                    new GridTransferable(draggedValues);

            draggestureevent.startDrag(DragSource.DefaultCopyDrop,
                    Gridtransferable, this);
            queue.deleteAll();
            File lcl = new File(draggedValues[0]);
            fireDirEvent(new DirEvent(this, DirEvent.LOCALDRAG),
                    lcl.getAbsolutePath(), this);
            statusOut("Please wait. Copying the directory ... ");
            return;
        }
    }

    public void setDragEnabled(boolean flag) {
        dragEnable = flag;
        if (flag) {

            tree.setBackground(Color.white);
            statusOut("Successfully done dragging.");
            statusOut("Status : Ready ");
        } else {
            tree.setBackground(Color.lightGray);
            statusOut("Frame disabled. Please wait ... till" +
                    " the color changes back to white.");
        }

    }


    public void dragEnter(DragSourceDragEvent dragsourcedragevent) {
    }

    public void dragOver(DragSourceDragEvent dragsourcedragevent) {
    }

    public void dragExit(DragSourceEvent dragsourceevent) {
    }

    public void dropActionChanged(DragSourceDragEvent dragsourcedragevent) {
    }

    public void dragDropEnd(DragSourceDropEvent dragsourcedropevent) {
        if (dragsourcedropevent.getDropSuccess()) {
            int i = dragsourcedropevent.getDropAction();
            logger.info("Value of i :" + i);
        }
    }

    public void dragEnter(java.awt.dnd.DropTargetDragEvent droptargetdragevent) {
//        int i = droptargetdragevent.getDropAction();
//        if ((i & 1) != 0) {
//            statusOut("Dragging ...");
//        }
//        if ((i & 2) != 0) {
//            statusOut("Moving ...");
//        }
//        if ((i & 0x40000000) != 0) {
//            statusOut("Linking ...");
//        }
//        if (!isDragAcceptable(droptargetdragevent)) {
//            droptargetdragevent.rejectDrag();
//            return;
//        } else {
//            return;
//        }

    }

    public void dragExit(java.awt.dnd.DropTargetEvent droptargetevent) {
    }

    public void dragOver(java.awt.dnd.DropTargetDragEvent droptargetdragevent) {

        //set cursor location. Needed in setCursor method
        Point cursorLocationBis = droptargetdragevent.getLocation();
        TreePath dPath =
                tree.getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);

        tree.setSelectionPath(dPath);
        tree.scrollPathToVisible(dPath);
        cursorLocationBis = null;

    }

    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent droptargetdragevent) {
        if (!isDragAcceptable(droptargetdragevent)) {
            droptargetdragevent.rejectDrag();
            return;
        } else {
            return;
        }
    }

    public void drop(java.awt.dnd.DropTargetDropEvent droptargetdropevent) {
        if (!isDropAcceptable(droptargetdropevent)) {
            droptargetdropevent.rejectDrop();
            return;
        } else if (!(dragEnable)) {
            return;
        }
        //get new parent node
        Point loc = droptargetdropevent.getLocation();
        destinationPath = tree.getPathForLocation(loc.x, loc.y);


        final String msg = testDropTarget(destinationPath, dragtreepath);
        if (msg != null) {
            droptargetdropevent.rejectDrop();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(
                            null, msg, "Error Dialog", JOptionPane.ERROR_MESSAGE
                    );
                }
            });
            return;
        }


        java.awt.datatransfer.Transferable transferable = droptargetdropevent.getTransferable();
        java.awt.datatransfer.DataFlavor adataflavor[] = transferable.getTransferDataFlavors();
        for (int i = 0; i < adataflavor.length; i++) {
            java.awt.datatransfer.DataFlavor dataflavor = adataflavor[i];
            try {
                if (dataflavor.equals(java.awt.datatransfer.DataFlavor.javaFileListFlavor)) {
                    List list = (List) transferable.getTransferData(dataflavor);
                    Iterator iterator = list.iterator();
                    Vector vector = new Vector();
                    String s;
                    for (; iterator.hasNext(); vector.add(s)) {
                        s = iterator.next().toString();
                    }


                    doCopyAction(vector);
                }
            } catch (java.awt.datatransfer.UnsupportedFlavorException unsupportedflavorexception) {
                logger.debug("Exception  = " + unsupportedflavorexception.toString());
            } catch (IOException ioexception) {
                logger.debug("Exception  = " + ioexception.toString());
            }
        }

        droptargetdropevent.dropComplete(true);

    }

    private String testDropTarget(TreePath destination, TreePath dropper) {
        //Typical Tests for dropping

        //Test 1.
        boolean destinationPathIsNull = destination == null;
        if (destinationPathIsNull) {
            return "Invalid drop location.";
        }
        if (destination.equals(dropper)) {
            logger.info("destination =" + destination +
                    "\nsource= " + dropper);
            _actionRefresh();
            return "Destination cannot be same as source";
        }


        return null;
    }

    public boolean isDragAcceptable(java.awt.dnd.DropTargetDragEvent droptargetdragevent) {
        return (droptargetdragevent.getDropAction() & 3) != 0;
    }

    public boolean isDropAcceptable(java.awt.dnd.DropTargetDropEvent droptargetdropevent) {
        return (droptargetdropevent.getDropAction() & 3) != 0;
    }


    public void doCopyAction(Vector vector) {

        int i = vector.size();

        String s = null;
        String as[] = returnSelectedPaths1();

        if (as == null || as[0] == null) {
            as = new String[1];
            as[0] = new String(txtField.getText());
        }


        Object aobj[] = destinationPath.getPath();
        int k = aobj.length;
        for (int l = 1; l < k; l++) {
            DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) aobj[l];
            if (!defaultmutabletreenode.isLeaf()) {
                if (l == 1 && defaultmutabletreenode != rootNode) {
                    s = new String();
                }
                s = s.concat(aobj[l].toString());
            }
        }
        asn = as[0];
        for (int j = 0; j < i; j++) {
            as1 = (String) vector.elementAt(j);


        }
        //System.out.println("transfer data");
        logger.info("Called the drop listener");
//         System.out.println("called the drop listener");
//        FileTransferMainPanel.mainPanel.showStatusWindow();
//        FileTransferMainPanel.mainPanel.showMessagesWindow();
        fireDirEvent(new DirEvent(this, DirEvent.DIR), as[0], this);
    }

    public void getLastExpandeNode(DefaultMutableTreeNode defaultmutabletreenode, TreePath treepath) {
        int i = defaultmutabletreenode.getChildCount();
        for (int j = 0; j < i; j++) {
            DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) defaultmutabletreenode.getChildAt(j);
            if (!defaultmutabletreenode1.isLeaf()) {
                javax.swing.tree.TreeNode atreenode[] = defaultmutabletreenode1.getPath();
                TreePath treepath1 = new TreePath(atreenode);
                if (!treepath1.equals(treepath) && tree.isExpanded(treepath1)) {
                    lastExp = defaultmutabletreenode1;
                    getLastExpandeNode(defaultmutabletreenode1, treepath);
                }
            }
        }

    }

    public void removeCurrentNode() {
        int i = tree.getSelectionModel().getSelectionCount();
        if (i == 0) {
            toolkit.beep();
            statusOut("Nothing was selected");
            statusOut("Status : Ready ");
            return;
        }
        for (int j = 0; j < i; j++) {
            TreePath treepath = tree.getSelectionPath();
            if (treepath != null) {

                defaultmutabletreenode
                        = (DefaultMutableTreeNode) treepath.getLastPathComponent();
                mutabletreenode = (MutableTreeNode) defaultmutabletreenode.getParent();
                javax.swing.tree.TreeNode atreenode[] = null;
                atreenode = defaultmutabletreenode.getPath();
                String s = returnPath(atreenode);
                if (mutabletreenode != null && mutabletreenode != rootNode) {
                    String msg = null;
                    deleteFile = new File(s);

                    if (deleteFile.isDirectory()) {
                        msg = "directory";

                    } else {
                        msg = "file";
                    }
                    Object aobj[] = {
                        "Cancel", "Delete"};
                    int k = JOptionPane.showOptionDialog(this, "Do you really want to delete this " + msg, "Delete Alert", -1, 2, null, aobj, aobj[0]);
                    if (k != 0) {
                        if (deleteFile.isDirectory()) {
                            dirDelete();
                        } else {
                            deleteFlag = deleteFile.delete();

                            if (deleteFlag) {
                                logger.info("\nThe" + msg + "is successfully deleted " + deleteFile);
                                treeModel.removeNodeFromParent(defaultmutabletreenode);
                                if (treeModel.getChildCount(mutabletreenode) == 0) {
                                    addObject((DefaultMutableTreeNode) mutabletreenode, "");
                                }
                                statusOut("Successfully deleted" +
                                        " File: " + deleteFile);
                                statusOut("Status : Ready ");
                            } else {
                                toolkit.beep();
                                statusOut("Permission Denied");
                                statusOut("Status : Ready ");
                                JOptionPane.showMessageDialog(null, deleteFile + " : Permission Denied.");
                            }
                        }
                    } else {
                        toolkit.beep();
                        statusOut("No selection was made");
                        statusOut("Status : Ready ");
                    }
                }
            }
        }
    }

    public void dirDelete() {
        Thread dirDelete = new Thread() {
            public void run() {
                deleteButton.setEnabled(false);
                setDragEnabled(false);
                deleteFlag = deleteDir(deleteFile);
                if (deleteFlag) {
                    logger.info("\nThe Directory is successfully" +
                            " deleted " + deleteFile);
                    treeModel.removeNodeFromParent(defaultmutabletreenode);
                    if (treeModel.getChildCount(mutabletreenode) == 0) {
                        addObject((DefaultMutableTreeNode) mutabletreenode, "");
                    }
                    _actionRefresh();
                    statusOut("Successfully deleted" +
                            " Directory: " + deleteFile);
                    statusOut("Status : Ready ");
                } else {
                    toolkit.beep();
                    JOptionPane.showMessageDialog(null, deleteFile
                            + " : Permission Denied.");
                    statusOut("Directory: Permission Denied");
                    statusOut("Status : Ready ");
                }
                deleteButton.setEnabled(true);
                setDragEnabled(true);
            }
        };
        dirDelete.start();
    }

    public boolean deleteDir(File dirname) {

        File[] files = dirname.listFiles();

        for (int i = 0; i < files.length; i++) {
            File current = files[i];
            if (current.isDirectory()) {
                deleteDir(current);

            } else {
                current.delete();
                statusOut("Deleting:  " + current.getName());
                logger.info("Deleted the file " + current.getName());
            }
        }

        logger.info("Deleted the directory " + dirname.getName());
        boolean flag = dirname.delete();
        return flag;


    }

    public boolean mkNewDir(String dir, String path) {
        File newfile = new File(path);
        if (!(newfile.exists())) {
            try {
                newfile.mkdirs();
                return true;
            } catch (Exception e) {
                logger.debug("\nThere is no permission to create directory" + path);
                return false;

            }
        } else {
            logger.info("\nDirectory already exists : " + path);
            return true;

        }
    }

    public void _actionMakeDir(String s) {
        String as[] = returnSelectedPaths1();
        if (as == null || as[0] == null) {
            if (noselection) {
                toolkit.beep();
                statusOut("no Path");
                return;
            }
            as = new String[1];
            as[0] = new String(txtField.getText());
        }

        File file = new File(as[0] + s); //"newFolder");
        if (file.exists()) {
            JOptionPane.showMessageDialog(this, "File name already exists.");
            return;
        }
        if (file.mkdir()) {
            statusOut("New Folder created : " + s);
            statusOut("Status : Ready ");
        } else {
            statusOut("New Folder could not be created : " + s);
            statusOut("Status : Ready ");
            JOptionPane.showMessageDialog(this, "Permission denied.");
            return;
        }
        chDir(as[0]);
        whereIsNode(rootNode, rootNode.getChildCount(), s + LclFile_Sep);
        if (seekNode != null) {
            javax.swing.tree.TreeNode atreenode[]
                    = treeModel.getPathToRoot(seekNode);
            TreePath treepath1
                    = new TreePath(atreenode);
            tree.scrollPathToVisible(treepath1);
            tree.setSelectionPath(treepath1);
        }

    }

    public void _actionMakeDir() {
        String dirname = JOptionPane.showInputDialog("Please Enter the Dir Name:");
        if (dirname != null) {
            _actionMakeDir(dirname);
        }
    }

    public File[] _actionDirInfo(String s1) {
        File file = null;
        String as[] = null;
        File as2[] = null;
        //LclDirInfoFrame lclDirInfoFrame;

        if (s1.equals("")) {
            //JOptionPane.showMessageDialog(null, "into actionDirInfor");
            file = new File(txtField.getText() + ".");
            as = file.list();
        } else {
            file = new File(s1 + ".");
            as2 = file.listFiles();
            return as2;
        }
        if (as == null && as2 == null) {
            return null;
        }

        SortFtpString.startSort(as);
        String s = txtField.getText();
        if (s.endsWith(LclFile_Sep)) {
            s = s.substring(0, s.lastIndexOf(LclFile_Sep));
        }
        String as1[] = new String[4];
        as1[0] = new String("Info Window");
        as1[1] = new String("Name");
        as1[2] = new String("Size");
        as1[3] = new String("Last Modified");

        new LclDirInfoFrame(as1, s + LclFile_Sep, as);
        return null;
    }


    public void _actionGo1DirUp() {
        String s = txtField.getText();
        if (s.endsWith(LclFile_Sep)) {
            s = s.substring(0, s.lastIndexOf(LclFile_Sep));
        }
        StringTokenizer stringtokenizer = new StringTokenizer(s, LclFile_Sep);
        Vector vector = new Vector();
        for (; stringtokenizer.hasMoreTokens(); vector.addElement(stringtokenizer.nextToken() + LclFile_Sep)) {
            logger.info("Adding the separator");
        }
        int i = vector.size();


        if (i < 2) {
            try {
                tree.fireTreeWillCollapse(new TreePath(rootNode.getPath()));
            } catch (ExpandVetoException expandvetoexception) {
                expandvetoexception.printStackTrace();
            }
        } else {
            vector.removeElementAt(i - 1);
            i = vector.size();
            String s1 = new String();
            for (int j = 0; j < i; j++) {
                s1 = s1.concat(vector.elementAt(j).toString());
            }

            chDir(s1);
        }
    }

    public void _actionRename() {
        String tpath[] = returnSelectedFiles(false);

        if (tpath == null) {
            toolkit.beep();
            statusOut("No Selection made");
            return;
        } else {
            new LclRenameDialog(this, tpath[0]);
            return;
        }

    }

    class MyAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            //JOptionPane.showMessageDialog(null, "into mouseclicked method");
            long clickTime = System.currentTimeMillis();
            long clickInterval = clickTime - firstClickTime;
            if (clickInterval < 300) {
                Thread display = new Thread() {
                    public void run() {
                        displayFile();
                        firstClickTime = 0;
                    }
                };
                display.start();
            } else {
                firstClickTime = clickTime;
            } // end of if - else
        } // end of mouseclicked

    } // end of class
    public static void main(String arg[]){
        LocalTreePanel localPanel = new LocalTreePanel();
        JFrame sFrame = new JFrame("Local File System");
        sFrame.getContentPane().setLayout(new GridLayout(1, 1));
        sFrame.getContentPane().add(localPanel);
        sFrame.pack();
        sFrame.setSize(300, 400);
        sFrame.setVisible(true);
        UITools.center(null, sFrame);

    }
}
