
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*GridFTPPanel originally part of Entrada by Scott Gose
  Modified by Jonathan DiCarlo to be a GridFace
  Swing implementation is in gridfaces/impl/gftpanel/GridFTPPanelImpl
  Related classes are GridFTPHost, Transfer,and TransferTableModel.*/
package org.globus.cog.gridface.interfaces;


public interface GridFTPPanel extends GridFace{

    //Saves all transfers to file, default ~/.entrada/transfers.txt
    public void saveTransfers();
    //Reads them out again:
    public void loadTransfers();

    //Used by Transfer to notify the panel that it's done.
    public void transferFinished(long size, float rate);

    //Give the panel an error message to display:
    public void error( String description, String error );

}
