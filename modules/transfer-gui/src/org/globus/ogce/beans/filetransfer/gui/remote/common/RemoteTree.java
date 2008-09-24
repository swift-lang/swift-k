//RemoteTree.java displays the remote tree structure d the remote files
package org.globus.ogce.beans.filetransfer.gui.remote.common;


import org.apache.log4j.Logger;
import org.globus.ftp.FileInfo;
import org.globus.ftp.MlsxEntry;
import org.globus.ogce.beans.filetransfer.util.DirInfo;
import org.globus.ogce.beans.filetransfer.util.DirQueue;
import org.globus.ogce.beans.filetransfer.util.GridTransferable;
import org.globus.ogce.beans.filetransfer.util.SortVectorStrings;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * This class implements the tree and drag and drop listeners
 *
 * @author Beulah Kurian Alunkal
 * @version 1.0
 */
public class RemoteTree extends JTree implements TreeExpansionListener, TreeWillExpandListener, DragSourceListener, DragGestureListener, java.awt.dnd.DropTargetListener {
    private static Logger logger =
            Logger.getLogger(RemoteTree.class.getName());

    protected DefaultTreeModel treeModel;
    boolean dirflag;
    private TreeSet InfoVec;
    public String rootName;
    private Object draggedValues[];
    public DropTarget dropTarget;

    public RemoteTreeFrame theApp;
    public JScrollPane sc_pane = null;
    public JTree tree;
    public String RemoteRoot;
    public String selectedPath;
    public Vector RemVector;

    public DefaultMutableTreeNode rootNode;
    public DefaultMutableTreeNode lastExp;
    public Toolkit toolkit;
    public boolean pathView;
    public Vector vector;
    public Vector vector1;

    public long firstClickTime = 0;
    private boolean noselection = true;
    TreePath destinationPath;
    protected TreePath SelectedTreePath = null;
    protected TreePath dragtreepath = null;
    private DirQueue queue;

    private DisplayInterface displayInterface = null;
    private boolean dragEnable = true;
    String deleteFile = null;
    boolean deleteFlag = true;
    DefaultMutableTreeNode defaultmutabletreenode = null;
    MutableTreeNode mutabletreenode = null;
    String selectedDestination = "";


    public RemoteTree(RemoteTreeFrame RemoteTreeframe, String s, String remoteRoot, DisplayInterface displayInterface) {

        InfoVec = new TreeSet();
        selectedPath = new String();
        RemVector = null;
        toolkit = Toolkit.getDefaultToolkit();
        theApp = RemoteTreeframe;
        rootName = s;
        if (theApp.connected) {
            RemoteRoot = remoteRoot;
        } else {
            RemoteRoot = new String();
        }
        makeRemoteInfo();
        queue = new DirQueue();
        this.displayInterface = displayInterface;
    }

    public void makeRemoteInfo() {
        rootNode = new DefaultMutableTreeNode(rootName);
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new RemTreeModelListener());
        RemoteRoot = theApp.rootRemote + "/";
        DefaultMutableTreeNode defaultmutabletreenode1 = addObject(null, RemoteRoot);
        addObject(defaultmutabletreenode1, "");
        //makeDirInfo(RemoteRoot);
        if (theApp.connected) {
            theApp.setDirToFile(RemoteRoot);
        }
        tree = new JTree(treeModel);
        dropTarget = new DropTarget(tree, this);
        dropTarget.setActive(true);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(4);
        sc_pane = new JScrollPane(tree);
        tree.addTreeWillExpandListener(this);
        tree.addMouseListener(new MyAdapter()); // for double clicking
        ToolTipManager.sharedInstance().registerComponent(tree);
        RemRenderer remrenderer = new RemRenderer(theApp);
        tree.setCellRenderer(remrenderer);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent treeselectionevent) {
                DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
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
        DragSource dragsource = DragSource.getDefaultDragSource();
        dragsource.createDefaultDragGestureRecognizer(tree, 3, this);
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

    public String returnPath(Object aobj[]) {
        int i = aobj.length;
        String s = new String();
        for (int j = 1; j < i; j++) {
            s = s.concat(aobj[j].toString());
        }
        return s;
    }

    public void createNodes(DefaultMutableTreeNode defaultmutabletreenode, boolean flag, String s) {
        if (flag) {
            DefaultMutableTreeNode defaultmutabletreenode1 = new DefaultMutableTreeNode(s);
            defaultmutabletreenode.add(defaultmutabletreenode1);
            DefaultMutableTreeNode defaultmutabletreenode2 = new DefaultMutableTreeNode("");
            defaultmutabletreenode1.add(defaultmutabletreenode2);
        } else {
            DefaultMutableTreeNode defaultmutabletreenode3 = new DefaultMutableTreeNode(s);
            defaultmutabletreenode.add(defaultmutabletreenode3);
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

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode defaultmutabletreenode, Object obj) {
        return addObject(defaultmutabletreenode, obj, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode defaultmutabletreenode, Object obj, boolean flag) {
        DefaultMutableTreeNode defaultmutabletreenode1 = new DefaultMutableTreeNode(obj);
        if (defaultmutabletreenode == null) {
            defaultmutabletreenode = rootNode;
        }
        treeModel.insertNodeInto(defaultmutabletreenode1, defaultmutabletreenode, defaultmutabletreenode.getChildCount());
        if (flag) {
            tree.scrollPathToVisible(new TreePath(defaultmutabletreenode1.getPath()));
        }
        return defaultmutabletreenode1;
    }

    public void statusOut(String msg) {
        //theApp.statusOut(msg);
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

                defaultmutabletreenode = (DefaultMutableTreeNode) treepath.getLastPathComponent();
                mutabletreenode = (MutableTreeNode) defaultmutabletreenode.getParent();
                javax.swing.tree.TreeNode atreenode[] = null;
                atreenode = defaultmutabletreenode.getPath();
                deleteFile = returnPath(atreenode);
                String msg = null;
                if (mutabletreenode != null) {
                    if (deleteFile.endsWith("/")) {
                        msg = "Directory";
                    } else {
                        msg = "File";
                    }
                    Object aobj[] = {"Cancel", "Delete"};
                    int k = JOptionPane.showOptionDialog(theApp, "Do you really want to delete this " + msg + " ?", "Delete Alert", -1, 2, null, aobj, aobj[0]);
                    if (k != 0) {

                        if (deleteFile.endsWith("/")) {
                            dirDelete();


                        } else {
                            deleteFlag = displayInterface.removeFile(deleteFile);

                            if (deleteFlag) {
                                treeModel.removeNodeFromParent(defaultmutabletreenode);
                                if (treeModel.getChildCount(mutabletreenode) == 0) {
                                    addObject((DefaultMutableTreeNode) mutabletreenode, "");
                                }
                                statusOut("Successfully deleted :" + deleteFile);
                            } else {
                                JOptionPane.showMessageDialog(this, msg + " : Permission Denied.");
                            }

                            statusOut("Status : Ready ");
                        }
                    }
                }
            } else {
                toolkit.beep();
                statusOut("No selection was made");
                statusOut("Status : Ready ");
            }
        }
    } // end of removecurrentNode

    public void dirDelete() {
        Thread dirDelete = new Thread() {
            public void run() {
                theApp.enableDeleteButton(false);
                setDragEnabled(false);
                deleteFlag = deleteDir(deleteFile);
                if (deleteFlag) {
                    treeModel.removeNodeFromParent(defaultmutabletreenode);
                    if (treeModel.getChildCount(mutabletreenode) == 0) {
                        addObject((DefaultMutableTreeNode) mutabletreenode
                                , "");
                    }
                    theApp._actionRefresh();
                    statusOut("Successfully deleted: " + deleteFile);
                } else {
                    JOptionPane.showMessageDialog(theApp, deleteFile +
                            " : Permission Denied.");
                }
                theApp.enableDeleteButton(true);
                setDragEnabled(true);
            }
        };
        dirDelete.start();
        statusOut("Status : Ready ");
    }

    public boolean deleteDir(String dirname) {
        if (!theApp.connected) {
            JOptionPane.showMessageDialog(this, "Connection got disconnected");
            theApp._actionDisconnect();
            return false;
        } else {
            Vector vector = displayInterface.listDeleteDir(dirname);
            if (vector == null) {
                logger.info("\nThis is an empty directory.");
            } else {
                FileInfo temp[] = new FileInfo[vector.size()];
                int p = 0;
                Enumeration enum1 = vector.elements();
                while (enum1.hasMoreElements()) {
                    FileInfo file = (FileInfo) enum1.nextElement();
                    temp[p] = file;
                    p++;
                }


                for (int i = 0; i < temp.length; i++) {
                    FileInfo current = temp[i];
                    String currentFullpath = dirname + "/" + current.getName();
                    if (current.isDirectory()) {

                        //	    statusOut( "Deleting the files in directory : " + currentFullpath);
                        deleteDir(currentFullpath);

                    } else {
                        displayInterface.removeFile(currentFullpath);
                        logger.info("\nDeleting: " + currentFullpath);
                        statusOut("\nDeleting: " + current.getName());
                    }
                }


            }
            boolean flag = displayInterface.removeDir(dirname);
            if (flag) {
                logger.info("\nDeleted the directory " + dirname);

            }
            return flag;
        }
    }

    public void doServList(String s) {
        Vector vector = null;
        if (!theApp.connected) {
            JOptionPane.showMessageDialog(this, "Connection got disconnected");
            theApp._actionDisconnect();
            return;
        }
        displayInterface.setType(true);
        vector = displayInterface.listDir(s);

        if (RemVector != null) {
            RemVector.clear();
        }
        RemVector = (Vector) vector.clone();
        InfoVec.addAll(vector);
        pathView = true;
    } // end of doServList

    public Vector ReturnInfoVec() {
        return RemVector;
    }


    public DefaultMutableTreeNode seekParent(DefaultMutableTreeNode defaultmutabletreenode, String s) {
        if (defaultmutabletreenode == null || s.compareTo(RemoteRoot) == 0) {
            return null;
        }
        int i = defaultmutabletreenode.getChildCount();
        String s1 = s.substring(s.indexOf("/") + 1);
        for (int j = 0; j < i; j++) {
            javax.swing.tree.TreeNode treenode = defaultmutabletreenode.getChildAt(j);
            String s2 = treenode.toString();
            if (s1.compareTo(s2) == 0) {
                DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) treenode;
                return defaultmutabletreenode1;
            }
        }
        return null;
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
                        if (l == 1) {
                            s = new String("/");
                        }
                        s = s.concat(aobj[l].toString());
                    }
                }
                if (s != null) {
                    as[j] = new String(s);
                }
            }
        }
        return as;
    }

    public TreePath[] returnSelectedPaths() {
        DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        TreePath atreepath[] = null;
        if (defaultmutabletreenode == null) {
            return atreepath;
        }
        if (defaultmutabletreenode.isLeaf()) {
            atreepath = tree.getSelectionModel().getSelectionPaths();
            return atreepath;
        } else {
            TreePath atreepath1[] = tree.getSelectionPaths();
            return atreepath1;
        }
    }

    public String[] returnSelectedFiles() {
        TreePath atreepath[] = returnSelectedPaths();
        String as[] = null;
        if (atreepath != null) {
            int i = atreepath.length;
            as = new String[i];
            for (int j = 0; j < i; j++) {
                Object aobj[] = atreepath[j].getPath();
                int k = aobj.length;
                String s = new String("");
                for (int l = 1; l < k; l++) {
                    s = s.concat(aobj[l].toString());
                }
                as[j] = new String(s);
            }
        }
        return as;
    }

    public void makeDirInfo(String s) //DefaultMutableTreeNode defaultmutabletreenode, String s)
    {
        vector = new Vector();
        vector1 = new Vector();
        if (theApp.getConnected() == false) {
            return;
        }
        doServList(s);
        if (RemVector.size() == 0) {
            //            DefaultMutableTreeNode defaultmutabletreenode1 = new DefaultMutableTreeNode("");
            //defaultmutabletreenode.add(defaultmutabletreenode1);
            return;
        }
        for (int i = 0; i < RemVector.size(); i++) {
            //The tokenizer uses the default delimiter set, which is "\t\n\r":
            StringTokenizer stringtokenizer = new StringTokenizer((String) RemVector.elementAt(i));
            int j = stringtokenizer.countTokens();
            String s1 = new String();
            int i1 = 0;
            if (theApp.ServerOpSys == 2) {
                for (int j1 = 1; j1 <= j; j1++) {
                    if (j1 <= 3) {
                        stringtokenizer.nextToken();
                    }
                    if (j1 == 4) {
                        s1 = stringtokenizer.nextToken();
                    }
                    if (j1 > 4) {
                        s1 = s1 + " " + stringtokenizer.nextToken();
                    }
                }
                i1 = ((String) RemVector.elementAt(i)).lastIndexOf("<DIR>");
            } else {
                for (int k1 = 1; k1 <= j; k1++) {
                    if (k1 <= 8) {
                        stringtokenizer.nextToken();
                    }
                    if (k1 == 9) {
                        s1 = stringtokenizer.nextToken();
                    }
                    if (k1 > 9) {
                        s1 = s1 + " " + stringtokenizer.nextToken();
                    }
                }
            }
            if (((String) RemVector.elementAt(i)).charAt(0) == 'd' || i1 > 0) {
                if (s1.compareTo(".") != 0 && s1.compareTo("..") != 0) {
                    vector.addElement(s1 + "/");
                }
            } else if (((String) RemVector.elementAt(i)).charAt(0) != 'l' && !(((String) RemVector.elementAt(i)).startsWith("total"))) //startsWith("total"))
            {
                vector1.addElement(s1);
            }
        }
        SortVectorStrings.startSort(vector);
        SortVectorStrings.startSort(vector1);
    }

    public void treeWillExpand(TreeExpansionEvent treeexpansionevent) {
        logger.info("\nenter the tree will expand.");
        if (!theApp.connected) {
            JOptionPane.showMessageDialog(this, "Connection got disconnected");
            theApp._actionDisconnect();
            return;
        }
        DefaultMutableTreeNode defaultmutabletreenode = null;
        Object aobj[] = null;
        aobj = treeexpansionevent.getPath().getPath();
        int i = aobj.length;
        defaultmutabletreenode = (DefaultMutableTreeNode) aobj[i - 1];
        if (defaultmutabletreenode == rootNode) {
            return;
        } else {
            DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) treeModel.getChild(aobj[i - 1], 0);
            treeModel.removeNodeFromParent(defaultmutabletreenode1);
            String s1 = returnPath(aobj);
            statusOut("Please wait..Expanding dir " + s1);
            listDir(defaultmutabletreenode, s1, false);
            if (!theApp.connected) {
                JOptionPane.showMessageDialog(null, "Connection got disconnected");
                theApp._actionDisconnect();
                statusOut("Application disconnected.");
                return;
            } else {

                theApp.setDirToFile(s1);
                noselection = false;
                tree.getSelectionModel().clearSelection();
                statusOut("Status: Ready");
                return;
            }
        }
    }

    public void listDir(DefaultMutableTreeNode defaultmutabletreenode, String s, boolean flag) {

        logger.info("\nEntered the listdir function.");
        FileInfo file2,file1;
        if (!theApp.connected) {
            JOptionPane.showMessageDialog(this, "Connection got disconnected");
            theApp._actionDisconnect();
            return;
        }

        vector = null;
        vector = displayInterface.listDir(s);
        logger.info("\nGot the list.");

        if (vector == null) {
            logger.info("\nThe vector is null.Using the Parameterized LIST Aand Customized Parser");

            vector = displayInterface.listAllDir(s);
            statusOut("Parsing failed. Using new Parser");

            FileParser parser = new FileParser();
            vector = parser.parse(vector, "unix", "/");


            /*	    if (vector == null){
                JOptionPane.showMessageDialog(null,"Connection reset. Please try connecting again.");
                theApp._actionDisconnect();
                return ;
                }*/

        }
        //FileInfo temp[] = new FileInfo[vector.size()];
        MlsxEntry temp[] = new MlsxEntry[vector.size()];
        String as[] = new String[vector.size()];
        int p = 0;
        Enumeration enum1 = vector.elements();
        while (enum1.hasMoreElements()) {
        	MlsxEntry entry = (MlsxEntry) enum1.nextElement();        	
        	if (null != entry) {        		
                as[p] = entry.getFileName();
                temp[p] = entry;
                p++;
        	}
        	//System.out.println(entry);
            
        }

        if (as == null) {
            return;
        }
        int i = as.length;
        //SortFtpString.startSort(as);
        if ((i == 0) || (as[0] == null)) {
            addObject(defaultmutabletreenode, "", false);
            return;
        }
        statusOut("Listing the directories...");
        String fullName = null;
        for (int j = 0; j < i; j++) {
            if (s.endsWith("/")) {
            	fullName = s + as[j];
                //temp[j].setName(s + as[j]);
            } else {
            	fullName = s + "/" + as[j];
                //temp[j].setName(s + "/" + as[j]);
            }
            //file1 = (FileInfo) temp[j];
            
            if ("dir".equals(temp[j].get("type"))) {

                if (temp[j].get("unix.slink") != null) {
                    ;
                } else {

//                    if ((file1.getName().equals("//dev")) ||
//                            (file1.isDevice())) {
//                        logger.info("\nIt is a device directory");
//                        addObject(defaultmutabletreenode, as[j], flag);
//                        statusOut("Disabled the device directory" + file1.getName());
//
//                    } else if ((file1.getName().equals("?"))) {
//                        logger.info("\nDir name could not be parsed");
//                        statusOut("The Parser could not parse filename");
//
//                    } else {
            		    //System.out.println(as[j]);
                        DefaultMutableTreeNode defaultmutabletreenode1
                                = addObject(defaultmutabletreenode, as[j] + "/", flag);
                        addObject(defaultmutabletreenode1, "", false);
                    //}
                }
            }
        }
        statusOut("Listing the files...");

        for (int k = 0; k < i; k++) {
            if (s.endsWith("/")) {
            	fullName = s + as[k];
                //temp[k].setName(s + as[k]);
            } else {
            	fullName = s + "/" + as[k];
                //temp[k].setName(s + "/" + as[k]);
            }
            //file2 = temp[k];

            if ("file".equals(temp[k].get("type"))) {
                if (temp[k].get("unix.slink") != null) {
                    logger.info("\nThis is a softlink.");
//                } else if ((file2.getName().equals("?"))) {
//                    logger.info("\nFile name could not be parsed");
//                    statusOut("The Parser could not parse file");
//
                } else {
                    addObject(defaultmutabletreenode, as[k], flag);
                    statusOut("Done. Status: Ready");
                }

            }

        }


    }
    /* Alternate parsing method

    }catch(Exception e){
    logger.info("The vector is returned as Strings ");
    String buffer[] = new String[vector.size()];
    while (enum.hasMoreElements()) {
    String sbuffer = (String)enum.nextElement();
    StringTokenizer stringtokenizer = new StringTokenizer(sbuffer);
    int m = stringtokenizer.countTokens();
    String s1 = new String();
    int i1 = 0;
    for(int k1 = 1; k1 <= m; k1++)
    {
    if(k1 <= 8)
    stringtokenizer.nextToken();
    if(k1 == 9)
    s1 = stringtokenizer.nextToken();
    if(k1 > 9)
    s1 = s1 + " " + stringtokenizer.nextToken();
    }
    as[p] = s1;
    buffer[p] = sbuffer;
    p ++ ;
    }

    if (as == null)
    return ;
    int i = as.length;
    if (i == 0) {
    addObject(defaultmutabletreenode,"",false);
    return ;
    }

    for (int j = 0; j < i; j ++ ) {


    if (s.endsWith("/")){
    as[j] = s+as[j];
    }
    else{
    if(buffer[i].charAt(0) == 'd' ){
    if(as[j].compareTo (".") != 0 && as[j].compareTo("..") != 0){
    DefaultMutableTreeNode defaultmutabletreenode1 = addObject(defaultmutabletreenode,as[j]
    +"/",flag);
    addObject(defaultmutabletreenode1,"",false);
    }
    }
    }
    }
    for (int k = 0; k < i; k ++ ) {
    if(buffer[i].charAt(0) == 'd' ){
    addObject(defaultmutabletreenode,as[k],flag);
    }

    }
    }*/



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
            theApp.setDirToFile(s1);
        } else {
            //txtField.setText("Local Path ");
            noselection = true;
        }
        treeModel.reload(defaultmutabletreenode);
    }

    public void buildirStructure(DefaultMutableTreeNode defaultmutabletreenode) {
        for (int k = 0; k < vector.size(); k++) {
            createNodes(defaultmutabletreenode, true, vector.elementAt(k).toString());
        }
        if (vector1.size() == 0) {
            //createNodes(defaultmutabletreenode, false, "");
            return;
        } else {
            for (int l = 0; l < vector1.size(); l++) {
                //JOptionPane.showMessageDialog(null, vector1.elementAt(l));
                createNodes(defaultmutabletreenode, false, vector1.elementAt(l).toString());
            }
        }
    }

    public void treeExpanded(TreeExpansionEvent treeexpansionevent) {
    }

    public void treeCollapsed(TreeExpansionEvent treeexpansionevent) {
    }


    public void transferfile(String startdir, String currdir, String fullpath) {

        final DirInfo dirInfo = new DirInfo(fullpath, startdir, currdir);
        queue.put(dirInfo);
        return;

    }

    public void dragGestureRecognized(DragGestureEvent draggestureevent) {
        if (!theApp.connected) {
            return;
        }
        TreePath treepath[] = null;
        Object aobj1[] = null;
        String fullpath = "";

        try {
            treepath = returnSelectedPaths();
            dragtreepath = SelectedTreePath;
            aobj1 = treepath[0].getPath();
            fullpath = returnPath(aobj1);
            logger.info("\nfull path=" + fullpath);

        } catch (NullPointerException e) {
            logger.info("Drag Recognized failed.");
        }


        if (treepath == null || aobj1 == null || dragtreepath == null || fullpath == null) {
            return;
        } else if (!(dragEnable)) {
            JOptionPane.showMessageDialog(this, "Please wait till the status\n bar shows drag enabled.");
            return;
        } else {
            draggedValues = returnSelectedFiles();
            GridTransferable Gridtransferable = new GridTransferable(draggedValues);
            draggestureevent.startDrag(DragSource.DefaultCopyDrop, Gridtransferable, this);
            String tempto[] = {""};
            queue.deleteAll();
            statusOut("Copying the file ...");
            String startdir = draggedValues[0].toString();
            logger.info("\nfull path =" + fullpath);
            theApp.fireGridEvent(new GridEvent(theApp, GridEvent.GRIDDRAG), startdir, "", tempto);
            theApp.setDirToFile(fullpath.substring(0,
                    fullpath.lastIndexOf("/")) + "/");

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
            statusOut("Current window disabled. Please wait ... till" +
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
            logger.info("value of i:" + i);
        }
    }


    public void dragEnter(java.awt.dnd.DropTargetDragEvent droptargetdragevent) {
//        int i = droptargetdragevent.getDropAction();
//        if ((i & 1) != 0) {
//            statusOut("Copying");
//        }
//        if ((i & 2) != 0) {
//            statusOut("Moving");
//        }
//        if ((i & 1073741824) != 0) {
//            statusOut("Linking");
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
        TreePath dPath = tree.getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
        tree.setSelectionPath(dPath);
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
        Point loc = droptargetdropevent.getLocation();
        destinationPath = tree.getPathForLocation(loc.x, loc.y);
        final String msg = testDropTarget(destinationPath, dragtreepath);
        if (msg != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JOptionPane.showMessageDialog(theApp, msg, "Error Dialog", JOptionPane.ERROR_MESSAGE);
                }
            });
            return;
        }
        droptargetdropevent.acceptDrop(1);
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
                    //droptargetdropevent.rejectDrop();
                }
            } catch (java.awt.datatransfer.UnsupportedFlavorException unsupportedflavorexception) {
                logger.debug("Exception _ufe = " + unsupportedflavorexception.toString());
            } catch (IOException ioexception) {
                logger.debug("Exception _ioe = " + ioexception.toString());
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
            logger.info("\nDestination =" + destination +
                    "\nSource= " + dropper);
            theApp._actionRefresh();
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
        String as[] = returnSelectedPaths1();
        if (as == null || as[0] == null) {
            as = new String[1];
            as[0] = new String(theApp.getDirFromFile());
        }
        String as1 = "";
        for (int j = 0; j < i; j++) {
            as1 = (String) vector.elementAt(j);
        } // end of for loop
        //  dragtreepath = as1;

        String temp[] = {""};
        if(theApp.bean){
        FileTransferMainPanel.mainPanel.showStatusWindow();
        FileTransferMainPanel.mainPanel.showMessagesWindow();
        }
        theApp.fireGridEvent(new GridEvent(theApp, GridEvent.GRIDDROP), null, as[0], temp);
        theApp.setDirToFile(as[0]);

        return;
    } // end of doCopyAction

    public void setSelectedSource() {
        draggedValues = returnSelectedFiles();
        String tempto[] = {""};
        String startdir = draggedValues[0].toString();
        theApp.fireGridEvent(new GridEvent(theApp, GridEvent.GRIDDRAG), startdir, "", tempto);
    }

    public void setSelectedDestination() {
        String as[] = returnSelectedPaths1();
        if (as == null || as[0] == null) {
            as = new String[1];
            as[0] = new String(theApp.getDirFromFile());
        }
        this.selectedDestination = as[0];
    }

    public void transfer() {
        String temp[] = {""};
        theApp.fireGridEvent(new GridEvent(theApp, GridEvent.GRIDDROP), null, selectedDestination, temp);
    }


    class MyAdapter extends MouseAdapter {

        public MyAdapter() {
        }

        public void mouseClicked(MouseEvent evt) {
            long clickTime = System.currentTimeMillis();
            long clickInterval = clickTime - firstClickTime;
            if (clickInterval < 300) {
                theApp.displayRemoteFile();
                firstClickTime = 0;
            } else {
                firstClickTime = clickTime;
            } // end of if - else
        } // end of mouseclicked
    }


    class RemTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent treemodelevent) {
            DefaultMutableTreeNode defaultmutabletreenode = (DefaultMutableTreeNode) treemodelevent.getTreePath().getLastPathComponent();
            try {
                int i = treemodelevent.getChildIndices()[0];
                defaultmutabletreenode = (DefaultMutableTreeNode) defaultmutabletreenode.getChildAt(i);
            } catch (NullPointerException nullpointerexception) {
                nullpointerexception.getMessage();
            }
            javax.swing.tree.TreeNode atreenode[] = null;
            atreenode = defaultmutabletreenode.getPath();
            // file1.renameTo(file);
            displayInterface.rename(selectedPath, returnPath(atreenode));
        }

        public void treeNodesInserted(TreeModelEvent treemodelevent) {
        }

        public void treeNodesRemoved(TreeModelEvent treemodelevent) {
        }

        public void treeStructureChanged(TreeModelEvent treemodelevent) {
        }

        RemTreeModelListener() {
        }
    }
}
