package org.globus.cog.gridface.impl.gftpanel;

//import org.gjt.sp.util.Log;

//import gridproxy.GridProxy;


// gvl: is the transfer added to the logging window? THis may be a
// feature that needs to be considerd for future. Also it would be
// good to add loging statements that go into the loging window.

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Observable;

import org.globus.cog.gridface.interfaces.GridFTPPanel;
import org.globus.common.CoGProperties;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPRestartMarker;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.HostPortList;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.PerfMarker;
import org.globus.ftp.RetrieveOptions;
import org.globus.ftp.Session;
import org.globus.ftp.StreamModeRestartMarker;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;


public class Transfer extends Observable implements Runnable, MarkerListener
{
    //~ Static fields/initializers --------------------------------------------

    // gvl: why is the binary definition of kilobyte not used?
    // is there a special reason for this?

    // Storage (2^x)
    // static final long KILOBYTE = 1024;
    // static final long MEGABYTE = 1048576;
    // static final long GIGABYTE = 1073741824;

    // Networking (10^x)
    static final long KILOBYTE = 1000;
    static final long MEGABYTE = 1000000;
    static final long GIGABYTE = 1000000000;

    //~ Instance fields -------------------------------------------------------

    public GridFTPHost destination;
    public GridFTPHost source;
    public GridFTPPanel displayPanel;

    protected Exception exception;
    protected GridFTPClient destinationHandle;
    protected GridFTPClient sourceHandle;
    protected String errorDescription;
    protected String status;
    protected String time;
    protected Thread thread;
    protected javax.swing.Timer timer;
    protected boolean canceled;
    protected boolean done;
    protected boolean stats;
    protected int failures;
    protected int repeat;
    protected int repetition;
    protected int sleep;
    protected int successes;
    protected float progress;
    protected float rate;
    protected long totalSize;
    protected long transferSeconds;
    protected long transferSize;
    protected long wallClockSeconds;

    //~ Constructors -----------------------------------------------------------

    public Transfer(GridFTPPanel displayPanel,  
		    GridFTPHost source,
		    GridFTPHost destination, 
		    int repeat, 
		    int sleep, 
		    boolean stats )
    {
        this.destination = destination;
        this.source = source;
        this.repeat = repeat;
        this.sleep = sleep;
        this.stats = stats;
	this.displayPanel = displayPanel;

        // catch this special case
        if ( this.repeat == 1 )
            this.repeat = 2;

        resetState();

        timer = new javax.swing.Timer( 1000,
            new ActionListener()
            {
                public void actionPerformed( ActionEvent ae )
                {
                    updateTime();
                }
            } 
        );

        thread = new Thread( this );
        thread.start();
    }

    public Transfer(GridFTPPanel panel,  
		    GridFTPHost source, 
		    GridFTPHost destination, 
		    boolean stats )
    {
        this(panel, source, destination, 0, 0, stats );
    }

    public Transfer (GridFTPPanel panel,  
		     GridFTPHost source, 
		     GridFTPHost destination )
    {
        this(panel, source, destination, 0, 0, false );
    }

    //~ Methods ----------------------------------------------------------------
    public void SetDisplayPanel(GridFTPPanel aPanel) {
	displayPanel = aPanel;
    }

    public static String formatFloat( float f )
    {
        String pattern = "#,###.##";
        DecimalFormat df = new DecimalFormat( pattern );

        if ( f >= GIGABYTE )
        {
            f = f / GIGABYTE;

            return df.format( f ) + " G";
        }
        else if ( f >= MEGABYTE )
        {
            f = f / MEGABYTE;

            return df.format( f ) + " M";
        }
        else if ( f >= KILOBYTE )
        {
            f = f / KILOBYTE;

            return df.format( f ) + " K";
        }
        else
        {
            return df.format( f ) + " ";
        }
    }

    public String getDisplayFilesize()
    {
        if ( totalSize >= 0 )
        {
            return formatFloat( totalSize ) + "B";
        }
        else
        {
            return null;
        }
    }

    public String getDisplayRate()
    {
        if ( rate == -1 )
        {
            return null;
        }
        else
        {
            return formatFloat( getRate() ) + "b/s";
        }
    }

    public String getDisplayTime()
    {
        return time;
    }

    private void doSleep()
    {
        if ( sleep < 1 )
            return;
        int needToSleep = sleep;

        while ( needToSleep > 0 )
        {
            setStatus( "Sleeping " + needToSleep );
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( Exception e ) { }
            needToSleep--;
        }
    }

    public void doRepetition()
    {
        repetition++;
        setStatus( status );
        if ( repetition < repeat )
        {
            doSleep();
            resetState();
            runTransfer();
        }
    }

    public void cancel()
    {
        canceled = true;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void done()
    {
        successes++;
        displayPanel.transferFinished( totalSize, getRate() );
        done = true;
        doRepetition();
    }

    protected void resetState()
    {
        done = false;
        canceled = false;
        errorDescription = null;
        exception = null;
        progress = 0.0f;
        rate = -1;
        time = null;
        wallClockSeconds = 0;
        transferSeconds = 0;
        status = null;
        totalSize = -1;
        transferSize = -1;
    } 

    public boolean isDone()
    {
        return done;
    }

    public long getFilesize()
    {
        return totalSize;
    }

    public float getProgress()
    {
        return progress;
    }

    public float getRate()
    {
        return rate * 8;
    }

    public void setSendAnonymousStats( boolean stats )
    {
        this.stats = stats;
    }

    public boolean getSendAnonymousStats()
    {
        return stats;
    }

    public String getStatus()
    {
        if ( repeat > 1 )
            return "[" + repeat + "/" + repetition + "/" +
                successes + "/" + failures + "] " + status;
        return status;
    }

    protected void setProgress( float progress )
    {
        this.progress = progress;
    }

    protected void setRate( float rate )
    {
        this.rate = rate;
    }

    protected void setStatus( String status )
    {
        this.status = status;
        setChanged();
        notifyObservers( this );
    }

    protected void error( String description, Exception exception )
    {
        cancel();

        errorDescription = description;
        this.exception = exception;

        failures++;

        setStatus( description + ": " + exception.getMessage() );
        timer.stop();

        doRepetition();
    }


    protected void readProxy()
    {
        if ( canceled )
            return;

        try
        {
            setStatus( "Reading proxy" );

            GSSCredential credential;
            GlobusCredential globusCredential;

            //Added CoGProperties way of getting proxy file, 9/24/04
            globusCredential = new GlobusCredential( CoGProperties.getDefault().getProxyFile() );
            credential = new GlobusGSSCredentialImpl( globusCredential,
                GSSCredential.INITIATE_AND_ACCEPT );

            source.setCredential( credential );
            destination.setCredential( credential );
        }
        catch ( Exception e )
        {
            error( "Error reading proxy", e );
        }
    }

    protected void authenticateSource()
    {
        if ( canceled )
            return;

        try
        {
            setStatus( "Authenticating Source" );
            sourceHandle = new GridFTPClient( source.getHostname(),
                    source.getPort() );
            sourceHandle.setAuthorization( HostAuthorization.getInstance() );
            sourceHandle.authenticate( source.getCredential() );
        }
        catch ( Exception e )
        {
            error( "Error authenticating with source", e );
        }
    } 

    protected void authenticateDestination()
    {
        if ( canceled )
            return;

        try
        {
            setStatus( "Authenticating Destination" );
            destinationHandle = new GridFTPClient( destination.getHostname(),
                    destination.getPort() );
            destinationHandle.setAuthorization( HostAuthorization.getInstance() );
            destinationHandle.authenticate( destination.getCredential() );
        }
        catch ( Exception e )
        {
            error( "Error authenticating with destination", e );
        }
    }

    protected void getFileSize()
    {
        if ( canceled )
            return;

        try
        {
            setStatus( "Getting file size" );

            sourceHandle.setType( Session.TYPE_IMAGE );
            destinationHandle.setType( Session.TYPE_IMAGE );
            totalSize = sourceHandle.getSize( source.getFilename() );
        }
        catch ( Exception e )
        {
            error( "Error getting file size", e );
        }
    }

    protected void transferFile() 
    {
        if ( canceled )
            return;

        try
        {
            sourceHandle.setMode( GridFTPSession.MODE_EBLOCK );
            destinationHandle.setMode( GridFTPSession.MODE_EBLOCK );
                                                                                     
            sourceHandle.setProtectionBufferSize( 16384 );
            destinationHandle.setProtectionBufferSize( 16384 );

            // sourceHandle.setDataChannelAuthentication( 
            //    DataChannelAuthentication.SELF );
            // sourceHandle.setDataChannelProtection(
            //    GridFTPSession.PROTECTION_SAFE );
            // destinationHandle.setDataChannelAuthentication( 
            //    DataChannelAuthentication.SELF );
            // destinationHandle.setDataChannelProtection(
            //    GridFTPSession.PROTECTION_SAFE );

            sourceHandle.setOptions( new RetrieveOptions( 5 ) );

            HostPortList hpl = destinationHandle.setStripedPassive();
            sourceHandle.setStripedActive( hpl );

            setStatus( "Initiating Transfer" );

            sourceHandle.extendedTransfer( source.getFilename(),
                destinationHandle, destination.getFilename(), this );

            setStatus( "Finished" );
            timer.stop();

            setProgress( 100 );

            sourceHandle.close();
            destinationHandle.close();

            sendAnonymousStats();
            done();
        }
        catch ( Exception e )
        {
            if ( e instanceof InterruptedException )
                error( "Thread interrupted", e );

            error( "Error transferring", e );
        }
    }

    public void run()
    {
        runTransfer();
    }

    public void runTransfer()
    {
        timer.start();
	 readProxy();
	 authenticateSource();
	 authenticateDestination();
        getFileSize();
        transferFile();
    }

    protected void sendAnonymousStats()
    {
        if ( ! stats )
            return;

        try
        {
	    // gvl: this is hardcoded and should possibly read by a
	    // configuration file. at least all hardcoded strings must
	    // be located in one class.

            Socket socket = new Socket( "wiggum.mcs.anl.gov", 50444 );
            PrintWriter out = new PrintWriter( new OutputStreamWriter( 
                        socket.getOutputStream() ) );
            out.println( "gridftp;" + totalSize + ";" + getRate() + ";" + time );
            out.flush();
        }
        catch ( Exception e ) { }
    }

    protected void updateTime()
    {
        StringBuffer str = new StringBuffer();
        long tt;
        long time = wallClockSeconds;

        tt = ( time / 3600 );
        if ( tt == 0 )
        {
            str.append( "00" );
        }
        else
        {
            if ( tt < 10 )
            {
                str.append( "0" );
            }

            str.append( tt );
            time -= ( tt * 3600 );
        }

        str.append( ":" );

        tt = ( time / 60 );

        if ( tt == 0 )
        {
            str.append( "00" );
        }
        else
        {
            if ( tt < 10 )
            {
                str.append( "0" );
            }

            str.append( tt );
            time -= ( tt * 60 );
        }

        str.append( ":" );

        tt = ( time % 60 );

        if ( tt == 0 )
        {
            str.append( "00" );
        }
        else
        {
            if ( time < 10 )
            {
                str.append( "0" );
            }

            str.append( time );
        }

        this.time = str.toString();

        // setChanged();
        // notifyObservers( this );

        wallClockSeconds++;
    }

    public void markerArrived( Marker m )
    {
        setStatus( "Transferring" );

        // start timing the actual transfer
        if ( transferSeconds == 0 )
        {
            transferSeconds = wallClockSeconds;
        }

        if ( m instanceof PerfMarker )
        {
            PerfMarker pm = ( PerfMarker )m;
            // System.out.println( "  DEBUG: *** PERF marker arrived ***" );

            try
            {
                transferSize = pm.getStripeBytesTransferred();
            }
            catch ( Exception e )
            {
                System.out.println( "  DEBUG: caught perf marker exception " +
                    e.getMessage() );
            }
        }
        else if ( m instanceof GridFTPRestartMarker )
        {
            // System.out.println( "  DEBUG: *** RESTART marker arrived ***" );
        }
        else if ( m instanceof StreamModeRestartMarker )
        {
            // System.out.println( "  DEBUG: *** STREAM MODE RESTART " +
                // "marker arrrivd ***" );
        }
        else
        {
            // System.out.println( "  DEBUG: *** UNKNOWN marker arrived ***" );
        }

        if ( transferSize != 0 )
        {
            setProgress( ( transferSize * 100 ) / totalSize );
            if ( ( wallClockSeconds - transferSeconds ) > 0 )
                setRate( transferSize / ( wallClockSeconds - transferSeconds ) );

            setChanged();
            notifyObservers( this );
        }
    }

    public String toString()
    {
        return source.getDisplayName() + ";" + 
            source.getHostname() + ";" + 
            source.getPort() + ";" + 
            source.getFilename() + ";" +
            destination.getDisplayName() + ";" + 
            destination.getHostname() + ";" + 
            destination.getPort() + ";" + 
            destination.getFilename() + ";" + 
            repeat + ";" + 
            sleep + ";" + 
            stats ;
    }

}
