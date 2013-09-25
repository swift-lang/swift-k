// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.util.Collection;
import java.util.Enumeration;

/**
 * The <code>FileTransferSpecification</code> represents all the parameters
 * required for the Grid file transfer <code>Task</code>. The source and
 * destination file URLs can be formulated by specifying individual elements
 * such as hostname, port, directory, and file; or by specifying the complete
 * URL string.
 */

public interface FileTransferSpecification extends Specification {
    public static final long OFFSET_FILE_START = 0;
    public static final long LENGTH_ENTIRE_FILE = Long.MAX_VALUE;

    /**
     * Sets the absolute path name of the directory containing the source file
     */
    public void setSourceDirectory(String directory);

    /**
     * Returns the absolute path name of the directory containing the source
     * file
     */
    public String getSourceDirectory();

    /**
     * Sets the absolute path name of the directory containing the destination
     * file
     */
    public void setDestinationDirectory(String directory);

    /**
     * Returns the absolute path name of the directory containing the
     * destination file
     */
    public String getDestinationDirectory();

    /**
     * Sets the name of the source file
     */
    public void setSourceFile(String file);

    /**
     * Returns the name of the source file
     */
    public String getSourceFile();

    /**
     * Sets the name of the destination file
     */
    public void setDestinationFile(String file);

    /**
     * Returns the name of the destination file
     */
    public String getDestinationFile();

    /**
     * Sets the entire source URL string
     */
    public void setSource(String source);

    /**
     * Returns the entire source URL string
     */
    public String getSource();

    /**
     * Sets the entire destination URL string.
     */
    public void setDestination(String destination);

    /**
     * Returns the entire source URL string
     */
    public String getDestination();

    /**
     * Specifies if this is a third party transfer. For third party transfers,
     * both, the source and dsetination file servers need to be remote servers.
     */
    public void setThirdParty(boolean bool);

    /**
     * Checks if the transfer is a third party file transfer.
     */
    public boolean isThirdParty();
    
    /**
     * Specifies that the implementation should attempt a third party transfer if possible
     * given the other constraints, but it should fall back to second party or simulated
     * third party transfers otherwise. This setting has no effect if third party transfers
     * are forced using <code>setThirdParty(true)</code> 
     */
    public void setThirdPartyIfPossible(boolean bool);
    
    public boolean isThirdPartyIfPossible();

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);
    
    public Collection<String> getAttributeNames();

    /**
     * @deprecated Use getAttributeNames
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAllAttributes();

    /**
     * Allows the specification of an offset inside the source file from which
     * the data starts to be read. By default the offset is set to 0 (the
     * beginning of the source file)
     */
    public void setSourceOffset(long offset);

    /**
     * Returns the offset in the source file from which the data starts to be
     * read
     */
    public long getSourceOffset();

    /**
     * Sets the total number of bytes that will be transfered (partial
     * transfer). By default the source file is transfered up to its end.
     */
    public void setSourceLength(long length);

    /**
     * Returns the number of bytes that will be transfered.
     */
    public long getSourceLength();

    /**
     * Sets an offset within the destination file from which data starts being
     * written. By default the offset is 0.
     */
    public void setDestinationOffset(long offset);

    /**
     * Returns the offset within the destination file from which data starts to
     * be written.
     */
    public long getDestinationOffset();
    
    /**
     * If set to true, recursive transfer of directories is requested. If set
     * to false, a single file transfer is requested.
     */
    public void setRecursive(boolean recursive);
    
    public boolean isRecursive();
}