package org.globus.cog.gridface.impl.gftpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;


public class TransferTableModel extends AbstractTableModel implements Observer
{
    //~ Instance fields ----------------------------------------------------

    // holds our data
    public Vector data = new Vector();
    protected javax.swing.Timer timer;

    // holds the data types for all our columns
    final Class columnClasses[] = 
    {
        JProgressBar.class, // progressbar
        String.class, // source display name
        String.class, // dest display name
        String.class, // source filename 
        String.class, // destination filename
        String.class, // size
        String.class, // rate
        String.class, // time
        String.class // status
    };

    // holds the strings to be displayed in the column headers of our table
    final String columnNames[] = 
    {
        "Progress", // 0
        "Source", // 1
        "Destination", // 2
        "Source File", // 3
        "Dest File", // 4
        "Size", // 5
        "Rate", // 6
        "Time", // 7
        "Status" // 8
    };

    public TransferTableModel()
    {
        timer = new javax.swing.Timer( 1000,
            new ActionListener()
            {
                public void actionPerformed( ActionEvent ae )
                {
                    for ( int i = 0; i < getRowCount(); i++ )
                        fireTableCellUpdated( i, 7 );
                }
            } 
        );

        timer.start();
    }

    //~ Methods ------------------------------------------------------------

    public boolean isCellEditable( int row, int col )
    {
        return false;
    }

    public Class getColumnClass( int c )
    {
        return columnClasses[c];
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName( int col )
    {
        return columnNames[col];
    }

    public int getRowCount()
    {
        return data.size();
    }

    public Object getValueAt( int row, int col )
    {
        Transfer transfer = ( Transfer ) data.elementAt( row );

        if ( col == 0 )
        {
            return new Float( transfer.getProgress() );
        }
        else if ( col == 1 )
        {
            return transfer.source.getDisplayName();
        }
        else if ( col == 2 )
        {
            return transfer.destination.getDisplayName();
        }
        else if ( col == 3 )
        {
            return transfer.source.getFilename();
        }
        else if ( col == 4 )
        {
            return transfer.destination.getFilename();
        }
        else if ( col == 5 )
        {
            return transfer.getDisplayFilesize();
        }
        else if ( col == 6 )
        {
            return transfer.getDisplayRate();
        }
        else if ( col == 7 )
        {
            return transfer.getDisplayTime();
        }
        else if ( col == 8 )
        {
            return transfer.getStatus();
        }
        else
        {
            return null;
        }
    }

    // adds a row
    public void addTransfer( Transfer t )
    {
        data.addElement( t );

        // the table model is interested in changes of the rows
        t.addObserver( this );
        fireTableRowsInserted( data.size() - 1, data.size() - 1 );
    }

    // deletes a row
    public void removeTransfer( Transfer t )
    {
        data.addElement( t );

        // the table model is interested in changes of the rows
        t.addObserver( this );
        fireTableRowsInserted( data.size() - 1, data.size() - 1 );
    }

    // is called by transfer object when its state changes
    public void update( Observable observable, Object o )
    {
        int index = data.indexOf( o );
        if ( index != -1 )
        {
            fireTableRowsUpdated( index, index );
        }
    }
}
