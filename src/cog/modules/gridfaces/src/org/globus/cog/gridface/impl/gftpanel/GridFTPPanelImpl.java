package org.globus.cog.gridface.impl.gftpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import org.globus.cog.gridface.interfaces.GridFTPPanel;
import org.globus.cog.gridface.interfaces.GridFace;
import org.globus.tools.ui.util.UITools;

/* Originally part of Entrada by Scott Gose Modified by Jonathan
 * DiCarlo to be a GridFace Since it extends JPanel, it can easily be
 * included in any Swing application The main method puts the JPanel in a
 * JFrame, so it can be run standalone too. 
 */

public class GridFTPPanelImpl extends JPanel implements ActionListener, 
							MouseListener, 
							GridFTPPanel
{
    /* Has been modified not to use static anything, because it is now
     * an implementation of interface GridFTPPanel and interface
     * methods can't be static.
     */

    public JTable table = new JTable();
    public TransferTableModel model = new TransferTableModel();

    // gvl: what is this?
    public String transferFile = System.getProperty( "user.home" ) +
        System.getProperty( "file.separator" ) + ".entrada" +
        System.getProperty( "file.separator" ) + "transfers.txt";

    protected JTextField totalBytesField = new JTextField();
    protected JTextField avgRateField = new JTextField();
    protected JTextField numTransfersField = new JTextField();
    protected float avgRate = 0;
    protected long totalBytes = 0;
    protected int numTransfers = 0;
    //~ Instance fields --------------------------------------------------------
    protected JButton clearButton = new JButton();
    protected JButton startButton = new JButton();
    protected JCheckBox statsCheckBox = new JCheckBox();
    protected JLabel destLabel = new JLabel();
    protected JLabel sourceLabel = new JLabel();
    protected JMenuItem cancelMI = new JMenuItem();
    protected JMenuItem removeMI = new JMenuItem();
    protected JPanel panel = new JPanel();
    protected JPopupMenu popupMenu = new JPopupMenu();

    protected JTextField destinationField = new JTextField();
    protected JTextField destinationFilenameField = new JTextField();

    protected JTextField destinationPortField = new JTextField();
    protected JTextField repeatField = new JTextField();
    protected JTextField sleepField = new JTextField();

    protected JTextField sourceField = new JTextField();
    protected JTextField sourceFilenameField = new JTextField();

    protected JTextField sourcePortField = new JTextField();

    //~ Data members added to support GridFace:
    protected Date lastUpdateTime;
    protected String name, label;

    //~ Constructors -----------------------------------------------------------

    public GridFTPPanelImpl(String sourceName, 
			    String sourcePort, 
			    String sourceFile,
			    String destinationName, 
			    String destinationPort, 
			    String destinationFile){
    	this();
    	sourceField.setText(sourceName);
    	sourceFilenameField.setText(sourceFile);
    	sourcePortField.setText(sourcePort);
    	destinationField.setText(destinationName);
    	destinationFilenameField.setText(destinationFile);
    	destinationPortField.setText(destinationPort);
    	
    }
    
    public GridFTPPanelImpl()
    {
        setLayout( new BorderLayout() );
        
        panel.setLayout( new FlowLayout( FlowLayout.LEFT, 5, 5 ) );

        setupSourcePanel();
        setupDestinationPanel();
        setupButtonPanel();
        setupNumberPanel();
        setupMenus();
        setupTable();
    
        add( panel, BorderLayout.NORTH );

        JScrollPane sp = new JScrollPane( table );
        sp.getViewport().setBackground( Color.white );
        add( sp, BorderLayout.CENTER );

	lastUpdateTime = new Date();
	label = "";
	name = "";
    }

    //~ Methods ----------------------------------------------------------------

    public static void main( String args[] )
    {
        // create and setup the window
        JFrame frame = new JFrame( "GridFTP" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        // create and setup the content pane
        GridFTPPanelImpl gftp = new GridFTPPanelImpl();
        frame.setContentPane( gftp );

        // display the window
        frame.pack();
        UITools.center( null, frame );
        frame.setVisible( true );
    }

    // need to synchronize this
    public void transferFinished( long size, float rate )
    {
        numTransfers++;
        totalBytes += size;

        if ( rate > 0 )
        {
            if ( avgRate == 0 ) 
                avgRate = rate;
            else 
                avgRate = ( avgRate + rate ) / 2;
        }

        numTransfersField.setText( Integer.toString( numTransfers ) );
        totalBytesField.setText( Transfer.formatFloat( totalBytes ) + "B" );
        avgRateField.setText( Transfer.formatFloat( avgRate ) + "b/s" );
    }

    protected void setupTable()
    {
        table.setModel( model );
        table.setRowSelectionAllowed( true );
        table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        table.setShowGrid( false );

        /*
        TableColumn tc = table.getColumnModel().getColumn( 0 );
        tc.setPreferredWidth( 60 );
        tc = table.getColumnModel().getColumn( 5 );
        tc.setPreferredWidth( 40 );
        tc = table.getColumnModel().getColumn( 6 );
        tc.setPreferredWidth( 40 );
        tc = table.getColumnModel().getColumn( 7 );
        tc.setPreferredWidth( 40 );
        */

        ProgressBarRenderer pbr = new ProgressBarRenderer( 0, 100 );
        pbr.setStringPainted( true );
        table.setDefaultRenderer( JProgressBar.class, pbr );
    }

    protected void setupMenus()
    {
        removeMI.setActionCommand( "removeMI" );
        removeMI.addActionListener( this );
        popupMenu.add( removeMI );

        cancelMI.setActionCommand( "cancelMI" );
        cancelMI.addActionListener( this );
        popupMenu.add( cancelMI );
    }

    protected void setupNumberPanel()
    {
        numTransfersField.setColumns( 6 );
        numTransfersField.setEditable( false );
        numTransfersField.setBorder( null );
        numTransfersField.setText( Integer.toString( numTransfers ) );

        totalBytesField.setColumns( 6 );
        totalBytesField.setEditable( false );
        totalBytesField.setBorder( null );
        totalBytesField.setText( Long.toString( totalBytes ) );

        avgRateField.setColumns( 6 );
        avgRateField.setEditable( false );
        avgRateField.setBorder( null );
        avgRateField.setText( Float.toString( avgRate ) );

        JPanel numberPanel = new JPanel();

        numberPanel.setLayout( new GridLayout( 3, 2 ) );
        numberPanel.add( new JLabel( "Transfers:  " ) );
        numberPanel.add( numTransfersField );
        numberPanel.add( new JLabel( "Total Amount:  " ) );
        numberPanel.add( totalBytesField );
        numberPanel.add( new JLabel( "Average Rate:  " ) );
        numberPanel.add( avgRateField );

        panel.add( numberPanel );
    }

    protected void setupButtonPanel()
    {
        startButton.setText( "Start" );
        startButton.addActionListener( this );
        clearButton.setText( "Clear" );
        clearButton.addActionListener( this );
                                                                                     
        statsCheckBox.setText( "Send anonymous statistics" );
        statsCheckBox.setSelected( true );
                                                                                     
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.Y_AXIS ) );
                                                                                     
        buttonPanel.add( clearButton );
        buttonPanel.add( startButton );
        buttonPanel.add( statsCheckBox );

        panel.add( buttonPanel );
    }

    protected void setupSourcePanel()
    {
        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout( new BoxLayout( sourcePanel, BoxLayout.Y_AXIS ) );
        sourcePanel.setBorder( BorderFactory.createTitledBorder( " Source " ) );

        sourceField.setColumns( 16 );
        sourceFilenameField.setColumns( 25 );
        sourcePortField.setColumns( 5 );
        sourcePortField.setText( "2811" );

        JPanel s1 = new JPanel();
        s1.add( new JLabel( "Host" ) );
        s1.add( sourceField );
        s1.add( new JLabel( "Port" ) );
        s1.add( sourcePortField );

        JPanel s2 = new JPanel();
        s2.add( new JLabel( "File" ) );
        s2.add( sourceFilenameField );

        sourcePanel.add( s1 );
        sourcePanel.add( s2 );

        panel.add( sourcePanel );
    }

    protected void setupDestinationPanel()
    {
        JPanel destinationPanel = new JPanel();
        destinationPanel.setLayout( new BoxLayout( destinationPanel,
            BoxLayout.PAGE_AXIS ) );
        destinationPanel.setBorder( BorderFactory.createTitledBorder( 
            " Destination " ) );

        destinationField.setColumns( 16 );
        destinationFilenameField.setColumns( 25 );
        destinationFilenameField.addActionListener( this );
        destinationPortField.setColumns( 5 );
        destinationPortField.setText( "2811" );

        JPanel d1 = new JPanel();
        d1.add( new JLabel( "Host" ) );
        d1.add( destinationField );
        d1.add( new JLabel( "Port" ) );
        d1.add( destinationPortField );

        JPanel d2 = new JPanel();
        d2.add( new JLabel( "File" ) );
        d2.add( destinationFilenameField );

        destinationPanel.add( d1 );
        destinationPanel.add( d2 );

        panel.add( destinationPanel );
    }

    public void actionPerformed( ActionEvent ae )
    {
        Object object = ae.getSource();

        if ( object instanceof JTextField )
        {
            object = startButton;
        }

        if ( object == startButton )
        {
            if ( ! validateFields() )
            {
                return;
            }

            GridFTPHost source = new GridFTPHost();
            GridFTPHost destination = new GridFTPHost();

            source.setHostname( sourceField.getText() );
            source.setPort( Integer.parseInt( sourcePortField.getText() ) );
            source.setFilename( sourceFilenameField.getText() );

            destination.setHostname( destinationField.getText() );
            destination.setPort( Integer.parseInt( 
                destinationPortField.getText() ) );
            destination.setFilename( destinationFilenameField.getText() );

            model.addTransfer( new Transfer(this, source, destination,
                    statsCheckBox.isSelected() ) );
        }
        else if ( ae.getSource() == clearButton )
        {
            sourceField.setText( null );
            sourcePortField.setText( "2811" );
            sourceFilenameField.setText( null );

            destinationField.setText( null );
            destinationPortField.setText( "2811" );
            destinationFilenameField.setText( null );
        }

        if ( ae.getSource() == removeMI )
        {
            System.out.println( "  DEBUG: remove this row" );

            // remove the current row
        }

        String s = ae.getActionCommand();
        if ( s.equals( "removeMI" ) )
        {
            System.out.println( "  DEBUG: hmm" );

            // popupMenu.show( ae.getSource(), ae.getX(), ae.getY() );
        }
    }

    public void error( String description, String error )
    {
        JOptionPane.showMessageDialog( this, error, description,
            JOptionPane.ERROR_MESSAGE );
    }

    public void mouseClicked( MouseEvent me )
    {
        JComponent src = ( JComponent )me.getSource();
        popupMenu.show( src, me.getX(), me.getY() );

        if ( ( ( me.getModifiers() & InputEvent.BUTTON2_MASK ) != 0 ) ||
                ( ( me.getModifiers() & InputEvent.BUTTON3_MASK ) != 0 ) )
        {
            System.out.println( "  DEBUG: displaying menu... " );
            popupMenu.show( src, me.getX(), me.getY() );
        }

        if ( me.getSource() == table )
        {
            popupMenu.show( src, me.getX(), me.getY() );
        }

        if ( me.isPopupTrigger() )
        {
            System.out.println( "  DEBUG: pop trigger pulled " );
        }
    }

    public void mouseEntered( MouseEvent me ) { }

    public void mouseExited( MouseEvent me ) { }

    public void mousePressed( MouseEvent me )
    {
        popupMenu.show( me.getComponent(), me.getX(), me.getY() );
    }

    public void mouseReleased( MouseEvent me ) { }

    private boolean validateFields()
    {
        if ( ( sourceField.getText().length() == 0 ) ||
                ( sourcePortField.getText().length() == 0 ) ||
                ( sourceFilenameField.getText().length() == 0 ) ||
                ( destinationField.getText().length() == 0 ) ||
                ( destinationPortField.getText().length() == 0 ) ||
                ( destinationFilenameField.getText().length() == 0 ) )
        {
            JOptionPane.showMessageDialog( this, "Missing field",
                "Missing Field", JOptionPane.WARNING_MESSAGE );

            return false;
        }
        else
        {
            return true;
        }
    }

    public void loadTransfers()
    {
        File f = new File( transferFile );

        if ( ! f.exists() )
            return;

        StringTokenizer st;
        Vector tokens = new Vector();

        try
        {
            String line;
            BufferedReader br = new BufferedReader( new FileReader( f ) );

            while ( ( line = br.readLine() ) != null )
            {
                st = new StringTokenizer( line, ";" );
                while ( st.hasMoreTokens() )
                {
                    tokens.add( st.nextToken() );
                }

                GridFTPHost source = new GridFTPHost();
                GridFTPHost destination = new GridFTPHost();

                source.setDisplayName( ( String ) tokens.get( 0 ) );
                source.setHostname( ( String ) tokens.get( 1 ) );
                source.setPort( Integer.parseInt( ( String ) tokens.get( 2 ) ) );
                source.setFilename( ( String ) tokens.get( 3 ) );

                destination.setDisplayName( ( String ) tokens.get( 4 ) );
                destination.setHostname( ( String ) tokens.get( 5 ) );
                destination.setPort( Integer.parseInt( 
                    ( String ) tokens.get( 6 ) ) );
                destination.setFilename( ( String ) tokens.get( 7 ) );

                int repeat = Integer.parseInt( ( String ) tokens.get( 8 ) ); 
                int sleep = Integer.parseInt( ( String ) tokens.get( 9 ) ); 

                String s = ( String ) tokens.get( 10 );

                model.addTransfer( new Transfer(this, source, destination, 
                        repeat, sleep, s.equals( "true" ) ) );

                tokens.clear();
            }
        }
        catch ( IOException ioe ) { }
    }

    public void saveTransfers()
    {
        if ( table.getRowCount() == 0 )
            return;

        Enumeration transfers = model.data.elements();

        try 
        {
            BufferedWriter out = new BufferedWriter(
                new FileWriter( transferFile ) );

            while ( transfers.hasMoreElements() )
            {
                Transfer t = ( Transfer ) transfers.nextElement();
                out.write( t.toString() + "\n" );
            }
            
            out.close();
        } 
        catch ( IOException e ) { } 
    }

    //~ Methods implementing GridFace ------------------------------------

    public void update() {
	lastUpdateTime.setTime(System.currentTimeMillis());
    }

    public Date lastUpdateTime() {
   
	return lastUpdateTime;
    }

    public void setName (String name){
	this.name = name;
    }

    public void setLabel (String label){
	this.label = label;
    }

    public void register (GridFace connection){
	/*What is this method supposed to do?*/
    }



    //~ Inner Classes ----------------------------------------------------------

    class ProgressBarRenderer extends JProgressBar implements TableCellRenderer
    {
        //~ Constructors -------------------------------------------------------

        public ProgressBarRenderer()
        {
            super();
        }

        public ProgressBarRenderer( BoundedRangeModel newModel )
        {
            super( newModel );
        }

        public ProgressBarRenderer( int orient )
        {
            super( orient );
        }

        public ProgressBarRenderer( int min, int max )
        {
            super( min, max );
        }

        public ProgressBarRenderer( int orient, int min, int max )
        {
            super( orient, min, max );
        }

        //~ Methods ------------------------------------------------------------

        public Component getTableCellRendererComponent( JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column )
        {
            setValue( ( int )( ( Float )value ).floatValue() );

            return this;
        }
    }
}
