package org.globus.ogce.beans.filetransfer.transfer;

import org.apache.log4j.Logger;
import org.globus.ftp.FileInfo;
import org.globus.ftp.MlsxEntry;
import org.globus.ogce.beans.filetransfer.gui.MainInterface;
import org.globus.ogce.beans.filetransfer.gui.local.LocalTreePanel;
import org.globus.ogce.beans.filetransfer.gui.remote.common.DisplayInterface;
import org.globus.ogce.beans.filetransfer.util.FileToTransfer;
import org.globus.ogce.beans.filetransfer.util.GridBrokerQueue;

import javax.swing.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

public class DirTransferRequest {

    private static Logger logger =
            Logger.getLogger(DirTransferRequest.class.getName());

    LocalTreePanel toLocal = null, fromLocal = null;
    DisplayInterface toRemote = null, fromRemote = null;
    String to = "",from = "";

    String fromFileSep = "", toFileSep = "";
    boolean dragLocal = true;
    DisplayInterface display = null;
    boolean dropLocal = false;
    private MainInterface theApp = null;
    int submitCount = 0;
    String rootfrom = null,rootto = null;
    GridBrokerQueue requestQueue = null;
    String provider = null;
    int noOfFiles = 0;
    String fromFullURL = null;
    String toFullURL = null;
    boolean start = true;
    Thread newThread = null;
    String status = null;
    Vector dirVector = null;

    public DirTransferRequest(MainInterface theApp,
                              LocalTreePanel fromLocal,
                              LocalTreePanel toLocal,
                              DisplayInterface fromRemote,
                              DisplayInterface toRemote,
                              String from, String to,
                              boolean dragLocal, boolean dropLocal,
                              String provider) {
        this.toLocal = toLocal;
        this.fromLocal = fromLocal;
        this.toRemote = toRemote;
        this.fromRemote = fromRemote;
        this.to = to;
        this.from = from;
        this.dragLocal = dragLocal;
        this.dropLocal = dropLocal;
        this.theApp = theApp;
        this.provider = provider;
        requestQueue = new GridBrokerQueue();
        dirVector = new Vector();
    }

    public void suspend() {
        status = "Suspended";
        start = false;
        newThread.suspend();
    }

    public void resume() {
        start = true;
        status = "Resumed";
        newThread.resume();
        //	theApp.startActualTransfer(requestQueue,provider);
    }

    public void kill() {

        requestQueue.deleteAll();
        //	suspend();
        status = "Failed";
        newThread.stop();
        newThread = null;

    }

    public String getTotalFiles() {
        return noOfFiles + "";
    }

    public String getFrom() {
        if (dragLocal) {
            fromFullURL = "file:///";
        } else {
            fromFullURL = fromRemote.getRootURL();
        }
        fromFullURL += from;
        return fromFullURL;
    }

    public String getTo() {
        if (dropLocal) {
            toFullURL = "file:///";
        } else {
            toFullURL = toRemote.getRootURL();
        }
        toFullURL += to;
        return toFullURL;
    }

    public void run() {
        newThread = new Thread() {
            public void run() {
                putRequest();
            }
        };
        newThread.start();
    }


    public void putRequest() {

        status = "Active";
        if (dropLocal) {
            rootto = "file:///";
            toFileSep = toLocal.LclFile_Sep;
        } else {
            rootto = toRemote.getRootURL();
            toFileSep = "/";
        }
        boolean isDir = false;
        File dir = null;
        if (dragLocal) {
            rootfrom = "file:///";
            dir = new File(from);
            isDir = dir.isDirectory();
            fromFileSep = fromLocal.LclFile_Sep;
        } else {
            rootfrom = fromRemote.getRootURL();
            isDir = fromRemote.chdir(from);
            fromFileSep = "/";
        }

        if (isDir) {
            String fromCopy = from;
            if (from.endsWith(fromFileSep)) {
                from = from.substring(0, from.length() - 1);
            }
            int index = from.lastIndexOf(fromFileSep);
            String firstDir = from.substring(index + 1);
            logger.info("This is a directory");
            firstDir.replace(fromFileSep.charAt(0), toFileSep.charAt(0));
            String fromSource = getFrom();
            String toDest = getTo() + firstDir;

            if (!toDest.startsWith("file")) {
                toDest = toDest.replaceAll("///", "//");
            }

            logger.info("From :" + fromSource + "To: " + toDest);
            if (fromSource.equals(toDest)) {
                String alertMsg = "Destination cannot be same as Source.";
                JOptionPane.showMessageDialog(theApp, alertMsg);
                status = "Failed";
                return;
            }
            firstDir = firstDir + fromFileSep;
            logger.info("First Dir:" + firstDir);
            if (dragLocal) {
                if (createDestDir(firstDir, "")) {
                    transferLocal(dir, firstDir);
                    if (checkVector()) {
                        status = "Finished";
                    } else {
                        status = "Failed";
                    }
                } else {
                    return;
                }

            } else {
                if (createDestDir(firstDir, "")) {
                    transferRemote(from + fromFileSep, firstDir);
                    if (checkVector()) {
                        status = "Finished";
                    } else {
                        status = "Failed";
                    }
                } else {
                    return;
                }
            }
        } else {
            logger.info("This is a file");
            putFile(from);
            status = "Finished";
        }

        if (start && status.equals("Finished")) {
            theApp.startActualTransfer(requestQueue, provider);
        }
    }

    public String getStatus() {
        return status;
    }

    public void transferLocal(File fromDir, String startdir) {
        if (!start) {
            return;
        }

        File[] files = fromDir.listFiles();
        logger.info("\nLISTING DIR: " + fromDir);

        for (int i = 0; i < files.length; i++) {
            File current = files[i];
            if (current.isDirectory()) {

                if (!createDestDir(current.getName(), startdir)) {
                    logger.info("\nDir creation failed =" + current.getName());
                    break;
                } else {
                    logger.info("\nDir created successfully =" + current.getName());
                    transferLocal(current, startdir + current.getName() + fromFileSep);
                }
            } else {
                putFile(fromDir.getAbsolutePath() + fromFileSep + current.getName());
            }
        }
    }

    public void putFile(String fromFullPath) {
        noOfFiles++;
        theApp.msgOut("\nCounting number of files :" + noOfFiles);
        String fromSourceURL = rootfrom + fromFullPath;
        int index = from.lastIndexOf(fromFileSep);
        String toFullPath = fromFullPath.substring(index + 1);
        toFullPath = toFullPath.replace(fromFileSep.charAt(0), toFileSep.charAt(0));
        String toDestinationURL = rootto + to + toFullPath;

        String checkFrom = fromSourceURL.replaceAll("//", "/");
        String checkTo = toDestinationURL.replaceAll("//", "/");
        checkFrom = checkFrom.replaceAll("//", "/");
        checkTo = checkTo.replaceAll("//", "/");

        if (checkFrom.equals(checkTo)) {
            String alertMsg = "Destination cannot be same as Source.";
            JOptionPane.showMessageDialog(theApp, alertMsg);
            status = "Failed";
            return;
        } else {
            FileToTransfer fileToTransfer = new FileToTransfer(fromSourceURL, toDestinationURL, provider);

            requestQueue.put(fileToTransfer);
            return;
        }

    }

    public boolean createDestDir(String newDir, String location) {
        newDir = newDir.replace(fromFileSep.charAt(0), toFileSep.charAt(0));
        location = location.replace(fromFileSep.charAt(0), toFileSep.charAt(0));
        String remoteNewDir = null;
        if (location != null) {
            remoteNewDir = to + location + newDir;
        } else {
            remoteNewDir = to + newDir;
        }
        String toCheck = remoteNewDir.replaceAll("//", "/");
        if (toCheck.endsWith(toFileSep)) {
            toCheck = toCheck.substring(0, toCheck.length() - 1);
        }
        if (toCheck.equals(from)) {
            String alertMsg = "Destination cannot be same as Source.";
            JOptionPane.showMessageDialog(theApp, alertMsg);
            status = "Failed";
            return false;
        }
        if (remoteNewDir.indexOf(from) >= 0) {
            logger.info("\nStore in hashtable remote dir =" + remoteNewDir +
                    "\nFrom : " + from);
            dirVector.add(remoteNewDir);
            return true;
        } else {

            logger.info("\nCreating remote dir =" + remoteNewDir +
                    "\nAT : " + to + location);

            boolean created = createDestDir(remoteNewDir);
            if (created) {
                logger.info("DESTINATION DIRECTORY CREATED");
            } else {
                logger.info("DESTINATION DIRECTORY not created");
            }
            return created;
        }
    }

    public boolean createDestDir(String remoteNewDir) {
        theApp.msgOut("\nCreating remote dir =" + remoteNewDir);
        if (dropLocal) {

            try {
                File newfile = new File(remoteNewDir);
                if (newfile.exists()) {
                    return true;
                }
                if (newfile.mkdir()) {
                    logger.info("Directory successfully created : " + remoteNewDir);

                    return true;
                } else {
                    String alertMsg = "You do not have permissions to " +
                            "to create new directories in the local machine.\n Transfer failed. Please set the permissions and try again.";
                    JOptionPane.showMessageDialog(theApp, alertMsg);
                    status = "Failed";
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            if (toRemote.mkdir(remoteNewDir)) {
                logger.info("Directory successfully created : " + remoteNewDir);
                return true;
            } else {
                String alertMsg = "You do not have permissions to " +
                        "to create new directories at Destination.\n" +
                        " Transfer failed. Please set the permissions and try again. ";
                JOptionPane.showMessageDialog(theApp, alertMsg);
                status = "Failed";
                return false;
            }
        }

    }

    /**
     * This method was to avoid recursive creation and travesing of
     * directory when a directory is dragged and dropped into a one of its
     * child directories.
     *
     */
    public boolean checkVector() {
        boolean success = true;
        while (dirVector.size() > 0) {
            String newDir = (String) dirVector.remove(0);
            if (!createDestDir(newDir)) {
                success = false;
                break;
            }
        }
        return success;
    }
    //Here there is no way to get the absolute path. So send the fullpath.

    public void transferRemote(String fullpath, String startdir) {
        if (!start) {
            return;
        }
        Vector vector = fromRemote.listTransferDir(fullpath);
        if (vector == null) {
            logger.info("\nThis is an empty directory.");
        } else {
            Enumeration enum1 = vector.elements();
            while (enum1.hasMoreElements()) {            	
            	MlsxEntry current = (MlsxEntry) enum1.nextElement();
            	if (null != current.getFileName() && 
            			!"cdir".equals(current.get("type")) && !"pdir".equals(current.get("type"))) {            		
            		//System.out.println(current);
                	//it's a soft link
                    if (current.get("unix.slink") != null) {
                        ;
                    } else {
                        if ("dir".equals(current.get("type"))) {
//                            if ((current.getName().equals("//dev")) ||
//                                    (current.isDevice())) {
//                                ;
//                            } else {                        	
                        	  if(!".".equals(current.getFileName()) && !"..".equals(current.getFileName())) {
                        		  if (!createDestDir(current.getFileName(), startdir)) {
                                      break;
                                  }
                                  transferRemote(fullpath + current.getFileName()
                                          + fromFileSep,
                                          startdir + current.getFileName()
                                          + fromFileSep);
                        	  }                              

                            //}

                        } else {
                            putFile(fullpath + fromFileSep + current.getFileName());
                        }
                    }
            	}
            }
        }
    }
}
