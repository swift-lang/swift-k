package org.globus.cog.gridface.impl.gftpanel;

import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;


public class GridFTPHost
{
    public static int DEFAULT_PORT = 2811;

    //~ Instance fields ----------------------------------------------------

    private GSSCredential credential;
    private String displayName;
    private String filename;
    private String hostname;
    private int port = DEFAULT_PORT;

    //~ Constructors -------------------------------------------------------
                                                                                 
    public GridFTPHost() { }
                                                                                 
    public GridFTPHost( String hostname, int port, String filename )
    {
        this( hostname, hostname, port, filename );
    }

    public GridFTPHost( String displayName, String hostname,
        int port, String filename )
    {
        this.displayName = displayName;
        this.hostname = hostname;
        this.port =  port;
        this.filename = filename;
    }

    //~ Methods ------------------------------------------------------------

    public void setCredential( GSSCredential credential )
    {
        this.credential = credential;
    }

    public GSSCredential getCredential()
    {
        return credential;
    }

    public void setDisplayName( String displayName )
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setFilename( String filename )
    {
        this.filename = filename;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setHostname( String hostname )
    {
        this.hostname = hostname;
        if ( displayName == null )
            displayName = hostname;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public GlobusURL getGlobusURL()
    {
        GlobusURL url = null;
        try 
        {
            url = new GlobusURL( "gsiftp://" + hostname + ":" + 
                port + "/" + filename );
        } 
        catch ( Exception e ) { }

        return url;
    }

    public String toString()
    {
        return displayName;
    }
}
