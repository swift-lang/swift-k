//RemoteTreeFrame.java which calls the RemoteTree.java
package org.globus.ogce.beans.filetransfer.gui.remote.common;

import org.apache.log4j.Logger;
import org.globus.ogce.beans.filetransfer.gui.FileTransferMainPanel;
import org.globus.util.GlobusURL;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class renders graphical interface to the remote ftp client
 * It can be used by any class that implements DisplayInterface.
 *
 * @author Beulah Kurian Alunkal

 */
public class RemoteTreeFrame extends JPanel implements ActionListener {
    private static Logger logger =
            Logger.getLogger(RemoteTreeFrame.class.getName());
    JButton dirInfoButton = null;
    public boolean bean = false;

    protected Vector gridlisteners = new Vector();

    /** Register an action listener to be notified when a button is pressed */
    public void addGridListener(GridListener l) {
        gridlisteners.addElement(l);
    }

    /** Remove an Answer listener from our list of interested listeners */
    public void removeGridListener(GridListener l) {
        gridlisteners.removeElement(l);
    }

    /** Send an event to all registered listeners */
    public void fireGridEvent(GridEvent e, String from, String s1, String s2[]) {
        Vector list = (Vector) gridlisteners.clone();
        for (int i = 0; i < list.size(); i++) {
            GridListener listener = (GridListener) list.elementAt(i);
            switch (e.getID()) {
                case GridEvent.GRIDDRAG:
                    listener.setDragDetails(e, from);
                    break;
                case GridEvent.GRIDDROP:
                    listener.setDropDetails(e, s1);
                    break;
                case GridEvent.GRIDEDIT:
                    listener.callGridEditFrame(e, s1, s2);
                    break;
            }
        }
    }

    public RemoteTreeFrame(DisplayInterface displayInterface, boolean bean) {

        this.bean = bean;
        this.displayInterface = displayInterface;
        init();
    }

    public RemoteTreeFrame() {
        init();
    }

    public void init() {
        try {
            url = new GlobusURL("ftp://dummy.edu:0/dev/null");
        } catch (Exception e) {
            logger.debug("This is a real bad error");
        }
        host = url.getHost();
        protocol = url.getProtocol();
        port = url.getPort();
        user = "user";
        pwd = "pass";
        file = "";
        rightView = null;
        ServerOpSys = 0;
        connected = false;
        vector = null;
        rootRemote = " ";
        currentdir = " ";

        this.FrameId = FrameId;

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
        dirInfoButton = createButton("images/16x16/view_text.png",
                "Directory Info currently disabled",
                "0");
        toolBar.add(dirInfoButton);


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
                    "View the status window",
                    "8");
            toolBar.add(jbutton);
            jbutton = createButton("images/16x16/view_msg.png",
                    "View the messages window",
                    "9");
            toolBar.add(jbutton);
        }


        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.addSeparator();
        toolBar.addSeparator();
        disconnect = createButton("images/16x16/folder_delete.png", "Disconnect", "12");
        disconnect.setEnabled(false);
        toolBar.add(disconnect);
        setBackground(Color.lightGray);

        JPanel jpanel = new JPanel();
        jpanel.setLayout(new BorderLayout());
        urlField = new JTextField(url.getURL());
        urlField.addActionListener(this);
        //urlField.setActionCommand("14");
        urlField.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent keyevent) {
                char c = keyevent.getKeyChar();
                if (c == '\n') {
                    setUrl(urlField.getText());
                }
            }

        });
        right = new RemoteTree(this, "Remote Tree -> Not connected", rootRemote, displayInterface);

        rightView = right.sc_pane;
        jpanel.add(toolBar, "North");
        jpanel.add(urlField, "South");
        JPanel jpanel1 = new JPanel(new BorderLayout());
        statusText = new JLabel(" Status :  Remote Window   ");
        //jpanel1.add(statusText);
        setLayout(new BorderLayout());
        add(jpanel, "North");
        add(rightView, "Center");
        add(jpanel1, "South");
        setToolsEn(true);
        right.setDragEnabled(false);
        disconnect.setEnabled(false);
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

    public void getUrlFromField() {
        try {
            urlstring = urlField.getText();
        } catch (Exception e) {
            error("getURLFromField", "unkown format");
            logger.info(e);
        }
    }


    public void setUrl(String newUrl) {
        try {
            GlobusURL oldUrl = url;
            url = new GlobusURL(newUrl);
            logger.info("\nOld url=" + oldUrl);
            logger.info("\nNew url=" + url);
            if ((oldUrl.getHost()).equals(url.getHost())) {
                if(!connected){
					statusOut("Connecting ... Please wait");	
                    _actionConnect(true);
                }else{

                    getUrlFromField();
                    filepath = getFile();
                    statusOut("Changed the directory to " + filepath);
                    index = filepath.lastIndexOf("/");
                    dirname = filepath.substring(0, index);
                    if (dirname != null) {
                        if (dirname.indexOf(rootRemote) > 0) {
                            _actionChngDir(dirname);
                        }
                    }
                }
            } else {
                host = url.getHost();
                port = url.getPort();
                statusOut("Connecting ... Please wait");
                pceListeners.firePropertyChange("url", oldUrl.getURL(), newUrl);
            }
            urlField.setText(newUrl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid URL.");
            //error("setURL", newUrl);
        }

    }

    public String getFile() {
        file = url.getPath();
        return file;
    }

    public String getHost() {

        return host;
    }

    public String getProtocol() {

        return protocol;
    }

    public String getUrl() {
        return url.getURL();
    }

    public int getPort() {

        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return pwd;
    }


    /**
     * Method is used to retrieve the url of the site without file appended
     *
     * @return string which represents the url
     */
    public String getBaseUrl() {
        GlobusURL tempurl = null;
        if (protocol.equals("ftp")) {
            try {
                tempurl = new GlobusURL(protocol + "://" + user + ":" + pwd + "@" + host + ":" + port + "/");
            } catch (Exception e) {
                logger.info("exception in globus url formation");
            }

            return tempurl.getURL();
        } else {
            setDirToFile("");
            return getUrl();
        }
    }


    public void setFrameId(int id) {
        FrameId = id;
    }

    public int getFrameId() {
        return FrameId;
    }

    /**
     * Method retrieves only the file from the entire url.
     *
     * @return path of the current directory
     */
    public String getDirFromFile() {
        getUrlFromField();
        filepath = getFile();
        index = filepath.lastIndexOf("/");
        dirname = filepath.substring(0, index) + "/";
        return dirname;
    }

    public void setDirToFile(String dirname) {
        setFile(dirname);
        //                                 getUrlFromField();
    }

    private void message(String s) {
        msg.setText(s);
    }

    private void error(String method, String e) {
        message("Error: " + method + " <" + e + ">");
    }


    private void parseURL() {
        try {
            host = getHost();
        } catch (Exception e) {
            error("parseURL, host", url.getURL());
        }
        try {
            port = getPort();
        } catch (Exception e) {
            error("parseURL, port", url.getURL());
        }
        try {
            protocol = getProtocol();
        } catch (Exception e) {
            error("parseURL, protocol", url.getURL());
        }
        try {
            file = getFile();
        } catch (Exception e) {
            error("parseURL, filename", url.getURL());
        }
    }

    private void updateURL() {
        try {

            url = new GlobusURL(protocol + "://" + host + ":" + port + "/" + file);
            urlField.setText(url.getURL());
        } catch (Exception e) {
            error("update : filename", "file");//url.getPath());
        }
    }

    public void setFile(String filename) {
        parseURL();
        file = filename;
        updateURL();
    }

    public void setHost(String h) {
        parseURL();
        this.host = h;
        updateURL();
    }

    public void setProtocol(String protocol) {
        parseURL();
        this.protocol = protocol;
        updateURL();
    }

    public void setUser(String user) {
        parseURL();
        this.user = user;
        updateURL();
    }

    public void setPassword(String pwd) {
        parseURL();
        this.pwd = pwd;
        updateURL();
    }


    public void setPort(int port) {
        parseURL();
        this.port = port;
        updateURL();
    }

    public void setServerOpSys(int no) {
        ServerOpSys = no;
    }

    public void setConnected(boolean value) {
        connected = value;
        if (value) {
            right.setDragEnabled(true); //do nothing
            disconnect.enable();
        } else {
            _actionDisconnect();
        }
        //	disconnect.enable();
    }

    public boolean getConnected() {
        return connected;
    }

    public void setRootRemote(String r) {
        currentdir = r;

    }


    public void setIsDir(boolean isDir) {
        this.isDir = isDir;
    }

    public void _actionConnect(boolean interactive) {
        connected = false;
        statusOut("Connecting ... Please wait");
        displayInterface.connectRemote();
        if (!connected) {
            // JOptionPane.showMessageDialog(this, "Please try again.");
            if (interactive) {
                displayInterface.connectDlg(null);
            } else {
                _actionDisconnect();
            }
            return;
        }


        remove(rightView);
        updateURL();
        rootpath = "";

        //rootRemote = "//";
        userHomeDir = displayInterface.getCurrentDir();
        rootRemote = userHomeDir;

        createRemoteTree();
        dirInfoButton.setEnabled(false);

    }

    public void createRemoteTree() {
        right = new RemoteTree(this, "Remote System" + " ->" + host, rootRemote, displayInterface);
        rightView = right.sc_pane;
        if (connected) {
            right.dropTarget.setActive(true);
        }
        add(rightView);
        setToolsEn(true);
        validate();
        if (rootRemote.equals("//")) {
            rootpath = "//";
        } else {
            rootpath = getDirFromFile();
        }
        if (rootpath.length() > 0) {
            if (rootpath.endsWith("/")) {
                rootpath = rootpath.substring(0, rootpath.length() - 1);
            }
            if (!rootRemote.equals("//")) {
                rootpath = rootpath.substring(0, rootpath.lastIndexOf("/")) + "/";
            }

        } else {
            rootpath = "//";
        }
        statusOut("Successfully connected to " + host + "At root " + rootpath);
    }

    public void _actionDisconnect() {
        if(bean){
  /*          rootRemote = "";
            try {
                url = new GlobusURL("ftp://dummy.edu:0/dev/null");
            } catch (Exception e) {
                logger.debug("This is a real bad error");
            }
            urlField.setText(url.getURL());
            right = new RemoteTree(this, "Remote Tree -> Not connected", rootRemote, displayInterface);

            rightView = right.sc_pane;
            add(rightView);  */
            right.tree.removeAll();
            right.tree.setBackground(Color.lightGray);
            displayInterface.disconnectRemote(false);
        }else{
            displayInterface.disconnectRemote(true);
            right.tree.removeAll();
            right.dropTarget.setActive(false);
            remove(rightView);
            setVisible(false);
            System.gc();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    urlField.setText("Remote site address bar");
                }

            });
        }
        disconnect.setEnabled(false);
        if (connected) {
            connected = false;
        }else{
            return;
        }

    }

    public void _actionRename() {
        if (connected) {
            String tpath[] = right.returnSelectedFiles();

            if (tpath == null) {
                statusOut("No Selection made");
                return;
            } else {
                new RemoteRenameDialog(this, tpath[0]);
                return;
            }

        } else {
            JOptionPane.showMessageDialog(this, "Remote site is disconnected already");
            _actionDisconnect();
            return;
        }

    }


    public DefaultMutableTreeNode seekParent(DefaultMutableTreeNode defaultmutabletreenode, String s) {
        if (defaultmutabletreenode == null) {
            return null;
        }

        int i = defaultmutabletreenode.getChildCount();

        for (int j = 0; j < i; j++) {
            javax.swing.tree.TreeNode treenode = defaultmutabletreenode.getChildAt(j);
            String s1 = treenode.toString();
            if (s.compareTo(s1) == 0) {
                DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) treenode;
                return defaultmutabletreenode1;
            }
        }

        return null;
    }

    public void _actionChngDir(String s) {
        if (connected) {
            String s1 = s;
            int l = 0;
            if (rootpath.length() <= 0) {
                l = 0;
            } else {
                l = rootpath.lastIndexOf("/");
            }
            if (s1.lastIndexOf("/") > l + 1) {
                s1 = s1.substring(l + 1, s1.lastIndexOf("/"));
                StringTokenizer stringtokenizer = new StringTokenizer(s1, "/");
                Vector vector = new Vector();
                for (; stringtokenizer.hasMoreTokens(); vector.addElement(stringtokenizer.nextToken() + "/")) {
                    logger.info("Appending the file separator");
                }

                clear();
                //right.makeRemoteInfo();
                DefaultMutableTreeNode defaultmutabletreenode1 = right.addObject(null, right.RemoteRoot);
                right.addObject(defaultmutabletreenode1, "");
                right.treeModel.reload();

                DefaultMutableTreeNode defaultmutabletreenode = right.rootNode;

                String path = "";
                int i = vector.size();

                for (int j = 0; j < i; j++) {
                    if (flag != false) {
                        path = rootpath + (String) vector.elementAt(j);
                        flag = false;
                    } else {
                        path = (String) vector.elementAt(j);
                    }
                    defaultmutabletreenode = seekParent(defaultmutabletreenode, path);

                    if (defaultmutabletreenode != null) {
                        right.tree.expandPath(new TreePath(defaultmutabletreenode.getPath()));
                    }

                }
                if (defaultmutabletreenode == null) {
                    statusOut("Path not changed ");
                } else {
                    statusOut("Changed to " + s + " directory");
                }
            } else {
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Remote site is disconnected already");
            _actionDisconnect();
        }

    }

    public void _actionRefresh() {
        if (connected) {
            flag = true;
            right.tree.getSelectionModel().clearSelection();
            String s = getDirFromFile();
            if (s == null) {
                _actionChngDir(rootRemote);
            } else {
                _actionChngDir(s);
            }
            statusOut("Status: Ready");
        } else {
            JOptionPane.showMessageDialog(this, "Remote site is disconnected already");
            _actionDisconnect();
        }

    }

    public void _actionGo1DirUp() {
        if (connected) {
            String s = getDirFromFile();
            if (s == null) {
                return;
            } else {
                if (s.endsWith("/")) {
                    s = s.substring(0, s.lastIndexOf("/"));
                }
                StringTokenizer stringtokenizer = new StringTokenizer(s, "/");
                Vector vector = new Vector();
                for (; stringtokenizer.hasMoreTokens(); vector.addElement(stringtokenizer.nextToken() + "/")) {
                    logger.info("appending separators.");
                }
                int i = vector.size();
                if (i < 2) {
                    clear();

                    DefaultMutableTreeNode defaultmutabletreenode = right.addObject(null, right.RemoteRoot);
                    right.addObject(defaultmutabletreenode, "");

                    right.treeModel.reload();
                    setDirToFile(right.RemoteRoot);
                } else {
                    vector.removeElementAt(i - 1);
                    i = vector.size();
                    String s1 = new String();
                    for (int j = 0; j < i; j++) {
                        s1 = s1.concat(vector.elementAt(j).toString());
                    }

                    flag = true;
                    _actionChngDir("/" + s1);
                }

            }
        } else {
            JOptionPane.showMessageDialog(this, "Remote site is disconnected already");
            _actionDisconnect();
        }

    }

    public void whereIsNode(DefaultMutableTreeNode defaultmutabletreenode, int i, String s) {
        seekNode = null;
        for (int j = 0; j < i; j++) {
            DefaultMutableTreeNode defaultmutabletreenode1 = (DefaultMutableTreeNode) defaultmutabletreenode.getChildAt(j);
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
            if (!right.tree.isExpanded(treepath)) {
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

    public void setError(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void _actionMakeDir(String s) {
        if (connected) {
            String as[] = right.returnSelectedPaths1();
            if (as == null || as[0] == null) {
                as = new String[1];
                as[0] = new String(getDirFromFile());
            }
            String dirname = null;
            if (s.equals("")) {
                String dirname1 = JOptionPane.showInputDialog("Please Enter the Dir Name:");
                if (dirname1 != null) {
                    dirname = as[0] + dirname1;
                }

            } else {

                dirname = as[0] + s;

            }
            if (dirname != null) {

                if (displayInterface.mkdir(dirname)) {
                    if (errorMsg.equals("exists")) {
                        JOptionPane.showMessageDialog(this,
                                "This filename already exists. ");
                        return;
                    }
                    String path = as[0] + s + "/";
                    flag = true;
                    _actionChngDir(path);
                    whereIsNode(right.rootNode, right.rootNode.getChildCount(),
                            "New Folder" + right.RemoteRoot);
                    if (seekNode != null) {
                        javax.swing.tree.TreeNode atreenode[]
                                = right.treeModel.getPathToRoot(seekNode);
                        TreePath treepath = new TreePath(atreenode);
                        right.tree.scrollPathToVisible(treepath);
                        right.tree.setSelectionPath(treepath);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Permission Denied.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Remote site is disconnected already");
            _actionDisconnect();
        }

    }


    public void actionPerformed(ActionEvent actionevent) {
        String s = actionevent.getActionCommand();
        int i = 0;
        try {
            i = Integer.valueOf(s).intValue();
        } catch (NumberFormatException numberformatexception) {
            //theApp.toolkit.beep();
            statusOut("Action Error: " + numberformatexception.getMessage());
        }
        //RemRenameDialog RemRenameDialog;
        switch (i) {
            default :
                break;
            case 1: //delete node
                right.removeCurrentNode();
                break;
            case 2: //shift between home and root
                shiftHomeToRoot();
                break;
            case 4: // refresh. right now refreshes to show the home dir
                _actionRefresh();
                break;
            case 5: // rename
                _actionRename();
                break;
            case 6: // goes to home dir
                _actionGo1DirUp();
                break;
            case 7: // create new folder
                _actionMakeDir("");
                break;

            case 8:
                FileTransferMainPanel.mainPanel.showStatusWindow();
                break;

            case 9:
                FileTransferMainPanel.mainPanel.showMessagesWindow();
                break;

            case 12: //remote site disconnect
                _actionDisconnect();
                break;
            case 14:
                getUrlFromField();
                filepath = getFile();
                index = filepath.lastIndexOf("/");
                dirname = filepath.substring(0, index);
                _actionChngDir(dirname);
                break;
        }
        dirInfoButton.setEnabled(false);
    }

    public void shiftHomeToRoot() {
        if (!connected) {
            JOptionPane.showMessageDialog(this,
                    "Remote host could not be connected.");
            _actionDisconnect();
            return;
        }
        remove(rightView);
        updateURL();
        rootpath = "";

        if (home) {
            rootRemote = "//";
            home = false;
        } else {
            rootRemote = userHomeDir;
            home = true;
        }
        createRemoteTree();
        JOptionPane.showMessageDialog(this,
                "Changed to new root " + rootRemote);
    }

    public void displayRemoteFile() {
        String as[] = right.returnSelectedFiles();
        if (as == null) {
            //     theApp.toolkit.beep();
            statusOut("noAction");
            return;
        }
        int i = as.length;
        if (i == 0) {
            //   theApp.toolkit.beep();
            statusOut("noAction");
            return;
        } else {
            String s = "/";

            fireGridEvent(new GridEvent(this, GridEvent.GRIDEDIT), null, s, as);
            //theApp.createEditFrame(as, ftpClnt, right.Remote_fileSep);
            return;
        }
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

    public synchronized void statusOut(String s) {
        //statusText.setText(s);
    }

    public void setToolsEn(boolean flag) {
        for (int i = 0; i <= 60; i++) {
            Component component = toolBar.getComponentAtIndex(i);
            if (component == null) {
                break;
            }
            component.setEnabled(flag);
        }
    }


    public void clear() {
        right.rootNode.removeAllChildren();
        right.treeModel.reload();
    }


    public void setFilesFromRemDir(File f[]) {
        files = f;
    }

    public void setRemFlag(boolean b) {
        remflag = b;
    }

    public void enableDeleteButton(boolean flag) {
        deleteButton.setEnabled(flag);
    }

    private JButton deleteButton = null;
    private JTextField urlField;
    private JLabel msg;
    private GlobusURL url;

    private String protocol;
    private String host;
    private int port;
    private String user;
    private String file;
    private String pwd;
    private PropertyChangeSupport pceListeners;

    protected String rootRemote = null;
    private String userHomeDir = null;
    protected String currentdir;
    public boolean isDir = false;
    public File[] files = null;
    public boolean remflag = false;

    public DisplayInterface displayInterface = null;
    protected JScrollPane rightView;
    protected JButton disconnect = null;
    protected DefaultMutableTreeNode seekNode;
    public int FrameId;

    public JLabel statusText;
    public RemoteTree right;
    public int ServerOpSys;
    public boolean connected;
    public boolean flag = true;
    public String rootpath;
    public JToolBar toolBar;
    //variables used frequently
    String filepath;
    int index;
    String dirname;
    Vector vector;
    public String RemoteFileSep = "/";
    boolean home = true;
    String urlstring = null;
    String errorMsg = "No errors";
}
