/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.globus.swift.catalog.transformation;

import org.apache.log4j.Logger;
import org.globus.swift.catalog.TransformationCatalog;
import org.globus.swift.catalog.TCEntry;
import org.globus.swift.catalog.types.SysInfo;
import org.globus.swift.catalog.types.TCType;
import org.globus.swift.catalog.util.Profile;
import org.globus.swift.catalog.util.ProfileParser;
import org.globus.swift.catalog.util.ProfileParserException;
import org.globus.swift.catalog.util.Separator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is the new file based TC implementation storing the contents of the file
 * in memory. For the old tc file implemenation see OldTC.java
 *
 * @author Gaurang Mehta
 * @author Karan Vahi
 * @version $Revision: 1.21 $
 */
public class File
    implements TransformationCatalog {
    public static final Logger logger = Logger.getLogger(File.class);

    /**
     * The singleton handler to the contents of the transformation catalog.
     */
    private static File mTCFileHandle = null;

    /**
     * The LogManager object which is used to log all the messages.
     * It's values are set in the CPlanner (the main toolkit) class.
     */


    /**
     * The Tree Map which stores the contents of the file.
     * The key is the transformationname.
     */
    private Map<String, Map<String, List<TCEntry>>> mTreeMap;

    /**
     * The path to the file based TC.
     */
    private String mTCFile;

    /**
     * Returns an instance of the File TC.
     *
     * @return TransformationCatalog
     */
    public static TransformationCatalog getInstance() {
        if ( mTCFileHandle == null ) {
            mTCFileHandle = new File();
        }
        return mTCFileHandle;

    }

    /**
     * Returns a non singleton instance to the file transformation catalog.
     * The path to the file containing the transformation catalog is automatically
     * picked up from the properties file.
     */
    public static TransformationCatalog getNonSingletonInstance() {
        return new File();
    }

    /**
     * Returns a non singleton instance to the file transformation catalog.
     *
     * @param path the path to the file containing the transformation
     * catalog in ths six column format.
     */
    public static TransformationCatalog getNonSingletonInstance( String path ) {
        return new File( path );
    }

    /**
     * Returns a non singleton instance to the file transformation catalog.
     * It populates the in memory structure by reading from the input stream
     * passed.
     *
     * @param reader  the <code>InputStrean</code> containing the bytes to be
     *                read.
     */
    public static TransformationCatalog getNonSingletonInstance( InputStream
        reader ) {
        return new File();
    }

    /**
     * The private constructor. Initialises the file handles to tc file.
     *
     */
    private File() {
        initialize( null );
        populateTC();
    }

    /**
     * The overloaded constructor.
     *
     * @param path  the path to the file containing the transformation catalog
     *              in six column format.
     */
    private File( String path ) {
        initialize( path );
        populateTC();
    }

    /**
     * The overloaded constructor. It populates the in memory structure by
     * reading from the input stream passed.
     *
     * @param reader  the <code>InputStrean</code> containing the bytes to be
     *                read.
     */
    private File( InputStream reader ) {
        initialize( null );
        populateTC( reader );
    }

    /**
     * Initializes the various class members.
     *
     * @param path the path to file containing the transformation
     * catalog. can be null.
     */
    private void initialize( String path ) {
        if (path == null) {
            throw new IllegalArgumentException("tc.data path cannot be null");
        }
        mTCFile = path;
        mTreeMap = 
            new TreeMap<String, Map<String, List<TCEntry>>>();
    }

    /**
     * Returns a textual description of the transformation mode.
     *
     * @return String containing the description.
     */
    public String getTCMode() {
        String st = "New FILE TC Mode";
        return st;
    }

    /**
     * Returns TC entries for a particular logical transformation and/or on a
     * number of resources and/or of a particular type.
     *
     * @param namespace   the namespace of the logical transformation.
     * @param name        the name of the logical transformation.
     * @param version     the version of the logical transformation.
     * @param resourceids the List resourceid where the transformation is located.
     *                    If <b>NULL</b> it returns all resources.
     * @param type TCType the type of the transformation to search for. If
     *                    <b>NULL</b> it returns all types.
     *
     * @return a list of <code>TransformationCatalogEntry</code> objects
     *         containing the corresponding entries from the TC.
     *         Returns null if no entry found.
     *
     * @throws Exception
     * @see org.globus.swift.catalog.types.TCType
     * @see org.globus.swift.catalog.TCEntry
     */
    public List<TCEntry> getTCEntries( String namespace, String name, String version,
        List<String> resourceids, TCType type ) throws Exception {
        logMessage( "getTCEntries(String namespace,String name,String version," +
            "List resourceids, TCType type" );
        logMessage( "\tgetTCEntries(" + namespace + ", " + name + ", " +
            version + ", " +
            resourceids + ", " + type );
        List<TCEntry> results = null;
        if ( resourceids != null ) {
            for ( Iterator<String> i = resourceids.iterator(); i.hasNext(); ) {
                List<TCEntry> tempresults = getTCEntries( namespace, name, version,
                    i.next(), type );
                if ( tempresults != null ) {
                    if ( results == null ) {
                        results = new ArrayList<TCEntry>();
                    }
                    results.addAll( tempresults );
                }
            }
        } else {
            List<TCEntry> tempresults = getTCEntries( namespace, name, version, ( String )null,
                type );
            if ( tempresults != null ) {
                results = new ArrayList<TCEntry>( tempresults.size() );
                results.addAll( tempresults );
            }

        }
        return results;
    }

    /**
     * Returns TC entries for a particular logical transformation and/or on a
     * particular resource and/or of a particular type.
     *
     * @param namespace   the namespace of the logical transformation.
     * @param name        the name of the logical transformation.
     * @param version     the version of the logical transformation.
     * @param resourceid  the resourceid where the transformation is located.
     *                    If <B>NULL</B> it returns all resources.
     * @param type TCType the type of the transformation to search for.
     *                    If <B>NULL</b> it returns all types.
     *
     * @return a list of <code>TransformationCatalogEntry</code> objects
     *         containing the corresponding entries from the TC.
     *         Returns null if no entry found.
     *
     * @throws Exception
     * @see org.globus.swift.catalog.types.TCType
     * @see org.globus.swift.catalog.TCEntry
     */
    public List<TCEntry> getTCEntries( String namespace, String name, String version,
        String resourceid, TCType type ) throws Exception {
        logMessage(
            "getTCEntries(String namespace, String name, String version, " +
            "String resourceId, TCType type)" );
        logMessage( "\t getTCEntries(" + namespace + ", " + name + ", " +
            version +
            "," + resourceid + ", " + type );
        List<TCEntry> results = null;
        String lfn = Separator.combine( namespace, name, version );
        if (logger.isDebugEnabled()) {
            logger.debug( "Trying to get TCEntries for " +
                lfn +
                " on resource " + ( ( resourceid == null ) ? "ALL" :
                resourceid ) + " of type " +
                ( ( type == null ) ? "ALL" : type.toString() ));
        }
        if ( resourceid != null ) {
            if ( mTreeMap.containsKey( resourceid ) ) {
                Map<String, List<TCEntry>> lfnMap = mTreeMap.get( resourceid );
                if ( lfnMap.containsKey( lfn ) ) {
                    List<TCEntry> l = lfnMap.get( lfn );
                    if ( type != null && l != null ) {
                        for ( Iterator<TCEntry> i = l.iterator(); i.hasNext(); ) {
                            TCEntry tc = i.next();
                            if ( tc.getType().equals( type ) ) {
                                if ( results == null ) {
                                    results = new ArrayList<TCEntry>();
                                }
                                results.add( tc );
                            }
                        }
                    } else {
                        results = l;
                    }
                }
            }
        } else {
            //since resourceid is null return entries for all sites
            if ( !mTreeMap.isEmpty() ) {

                for ( Iterator<Map<String, List<TCEntry>>> j = mTreeMap.values().iterator(); j.hasNext(); ) {
                    //check all maps for the executable.
                    Map<String, List<TCEntry>> lfnMap = j.next();
                    if ( lfnMap.containsKey( lfn ) ) {
                        List<TCEntry> l = lfnMap.get( lfn );
                        if ( type != null && l != null ) {
                            for ( Iterator<TCEntry> i = l.iterator(); i.hasNext(); ) {
                                TCEntry tc = i.next();
                                if ( tc.getType().equals( type ) ) {
                                    if ( results == null ) {
                                        results = new ArrayList<TCEntry>();
                                    }
                                    results.add( tc );
                                }
                            }
                        } else {
                            //if the list returned is not empty keep adding to the result list.
                            if ( l != null ) {
                                if ( results == null ) {
                                    results = new ArrayList<TCEntry>();
                                }
                                results.addAll( l );
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Get the list of Resource ID's where a particular transformation may reside.
     *
     * @param   namespace String The namespace of the transformation to search for.
     * @param   name      String The name of the transformation to search for.
     * @param   version   String The version of the transformation to search for.
     * @param   type      TCType The type of the transformation to search for.<BR>
     *                    (Enumerated type includes SOURCE, STATIC-BINARY,
     *                    DYNAMIC-BINARY, PACMAN, INSTALLED, SCRIPT)<BR>
     *                     If <B>NULL</B> it returns all types.
     *
     * @return  a list of Resource Id's as strings.
     *          Returns <B>NULL</B> if no results found.
     *
     * @throws Exception
     * @see org.globus.swift.catalog.types.TCType
     */
    public List<String> getTCResourceIds( String namespace, String name,
        String version,
        TCType type ) throws Exception {
        logMessage(
            "List getTCResourceIds(String namespace, String name, String " +
            "version, TCType type" );
        logMessage( "\t getTCResourceIds(" + namespace + ", " + name + ", " +
            version +
            ", " + type );
        List<String> results = null;
        List<Map<String, List<TCEntry>>> lfnList = new ArrayList<Map<String, List<TCEntry>>>();
        if ( name == null ) {
            if ( type == null ) {
                //return all the resources only
                results = new ArrayList<String>( mTreeMap.keySet() );
                return results;
            }
        }
        //return all the entries to search for type
        lfnList.addAll( mTreeMap.values() );

        List<TCEntry> entries = null;
        for ( Iterator<Map<String, List<TCEntry>>> i = lfnList.iterator(); i.hasNext(); ) {
            Map<String, List<TCEntry>> lfnMap = i.next();
            if ( entries == null ) {
                entries = new ArrayList<TCEntry>();
            }
            if ( name == null ) {
                for ( Iterator<List<TCEntry>> j = lfnMap.values().iterator(); j.hasNext(); ) {
                    entries.addAll( j.next() );
                }
            } else {
                if ( lfnMap.containsKey( Separator.combine( namespace, name,
                    version ) ) ) {
                    entries.addAll( lfnMap.get( Separator.combine(
                        namespace,
                        name,
                        version ) ) );
                }
            }
        }
        TreeSet<String> rset = null;
        for ( Iterator<TCEntry> i = entries.iterator(); i.hasNext(); ) {
            if ( rset == null ) {
                rset = new TreeSet<String>();
            }
            TCEntry entry = i.next();
            if ( type == null ) {
                rset.add( entry.getResourceId() );
            } else {
                if ( entry.getType().equals( type ) ) {
                    rset.add( entry.getResourceId() );
                }
            }
        }
        if ( rset != null ) {
            results = new ArrayList<String>();
            for ( Iterator<String> i = rset.iterator(); i.hasNext(); ) {
                results.add( i.next() );
            }
        }
        return results;
    }

    /**
     * Get the list of PhysicalNames for a particular transformation on a
     * site/sites for a particular type/types.
     *
     * @param  namespace  the namespace of the transformation to search for.
     * @param  name       the name of the transformation to search for.
     * @param  version    the version of the transformation to search for.
     * @param  resourceid the id of the resource on which you want to search. <BR>
     *                    If <B>NULL</B> then returns entries on all resources
     * @param  type       the type of the transformation to search for. <BR>
     *                    (Enumerated type includes source, binary, dynamic-binary,
     *                     pacman, installed)<BR>
     *                     If <B>NULL</B> then returns entries of all types.
     *
     * @throws Exception
     * @return List       a list of String Arrays.
     *                    Each array contains the resourceid,
     *                    the physical transformation, the type of the tr and
     *                    the systeminfo.
     *                    The last entry in the List is a int array containing
     *                    the column lengths for pretty print.
     *                    Returns <B>NULL</B> if no results found.
     *
     * @see org.globus.swift.catalog.types.TCType
     * @see org.globus.swift.catalog.types.SysInfo
     * @deprecated
     */
    public List<Object> getTCPhysicalNames( String namespace, String name,
        String version,
        String resourceid, TCType type ) throws
        Exception {
        logMessage( "List getTCPhysicalNames(String namespace, String name," +
            "String version, String resourceid,TCType type)" );
        logMessage( "\t getTCPhysicalNames(" + namespace + ", " + name + ", " +
            version + ", " + resourceid + ", " + type + ")" );
        List<Object> results = null;
        List<Map<String, List<TCEntry>>> lfnList = new ArrayList<Map<String, List<TCEntry>>>();
        int count[] = {0, 0, 0};
        if ( resourceid == null ) {
            lfnList.addAll( mTreeMap.values() );
        } else {
            if ( mTreeMap.containsKey( resourceid ) ) {
                lfnList.add( mTreeMap.get( resourceid ) );
            } else {
                return null;
            }
        }

        // for ( Iterator<Map> i = lfnList.iterator(); i.hasNext(); ) {
        //     Map lMap = i.next();
        for (Map<String,List<TCEntry>> lMap : lfnList) {
            if ( lMap.containsKey( Separator.combine( namespace, name, version ) ) ) {
                for ( Iterator<TCEntry> j = lMap.get( Separator.combine(
                    namespace,
                    name, version ) ).iterator(); j.hasNext(); ) {
                    TCEntry entry = j.next();
                    if ( type != null ) {
                        if ( !entry.getType().equals( type ) ) {
                            break;
                        }
                    }
                    String[] s = {entry.getResourceId(),
                        entry.getPhysicalTransformation(),
                        entry.getType().toString(),
                        entry.getSysInfo().toString()};
                    columnLength( s, count );
                    if ( results == null ) {
                        results = new ArrayList<Object>();
                    }
                    results.add( s );
                }
            }
        }
        if ( results != null ) {
            results.add( count );
        }
        return results;
    }

    /**
     * Gets the list of LogicalNames available on a particular resource.
     *
     * @param resourceid the id of the resource on which you want to search
     * @param type       the type of the transformation to search for. <BR>
     *                   (Enumerated type includes source, binary, dynamic-binary,
     *                    pacman, installed)<BR>
     *                   If <B>NULL</B> then return logical name for all types.
     *
     * @throws Exception
     * @return List      Returns a list of String Arrays.
     *                   Each array contains the resourceid, logical transformation
     *                   in the format namespace::name:version and type.
     *                   The last entry in the list is an array of integers
     *                   specifying the column length for pretty print.
     *                   Returns <B>NULL</B> if no results found.
     */
    public List<Object> getTCLogicalNames( String resourceid, TCType type ) throws
        Exception {
        logMessage( "List getTCLogicalNames(String resourceid, TCType type)" );
        logMessage( "\t getTCLogicalNames(" + resourceid + "," + type + ")" );
        List<Object> result = null;
        int[] length = {0, 0};
        List<Map<String, List<TCEntry>>> lfnMap = 
            new ArrayList<Map<String, List<TCEntry>>>();
        String lfn = null, resource = null, tctype = null;
        if ( resourceid == null ) {
            lfnMap.addAll( mTreeMap.values() );
        } else {
            if ( mTreeMap.containsKey( resourceid ) ) {
                lfnMap.add( mTreeMap.get( resourceid ) );
            } else {
                lfnMap = null;
            }
        }
        if ( lfnMap != null ) {
            for ( Iterator<Map<String, List<TCEntry>>> i = lfnMap.iterator(); i.hasNext(); ) {
                for ( Iterator<List<TCEntry>> j = i.next().values().iterator();
                    j.hasNext(); ) {
                    for ( Iterator<TCEntry> k = j.next().iterator();
                        k.hasNext(); ) {
                        TCEntry tc = k.next();
                        String l = null, r = null, t = null;
                        if ( type == null ) {
                            l = tc.getLogicalTransformation();
                            r = tc.getResourceId();
                            t = tc.getType().toString();

                        } else {
                            if ( tc.getType().equals( type ) ) {
                                l = tc.getLogicalTransformation();
                                r = tc.getResourceId();
                                t = tc.getType().toString();
                            }
                        }
                        if ( l != null && r != null && t != null ) {
                            if ( lfn == null ||
                                ! ( lfn.equalsIgnoreCase( l ) &&
                                resource.equalsIgnoreCase( r ) &&
                                tctype.equalsIgnoreCase( t ) ) ) {
                                lfn = l;
                                resource = r;
                                tctype = t;
                                String[] s = {l, r, t};
                                columnLength( s, length );
                                if ( result == null ) {
                                    result = new ArrayList<Object>( 5 );
                                }
                                result.add( s );
                            }
                        }
                    }
                }
            }
        }
        if ( result != null ) {
            result.add( length );
        }
        return result;
    }

    /**
     * Get the list of Profiles associated with a particular logical transformation.
     *
     * @param namespace  the namespace of the transformation to search for.
     * @param name       the name of the transformation to search for.
     * @param version    the version of the transformation to search for.
     *
     * @throws NotImplementedException as not implemented as yet.
     *
     * @return List      Returns a list of Profile Objects containing profiles
     *                   assocaited with the transformation.
     *                   Returns <B>NULL</B> if no profiles found.
     *
     * @see org.globus.swift.catalog.util.Profile
     */
    public List<Profile> getTCLfnProfiles( String namespace, String name,
        String version ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * Get the list of Profiles associated with a particular physical transformation.
     *
     * @param pfn        the physical file name to search the transformation by.
     * @param resourceid the id of the resource on which you want to search.
     * @param type       the type of the transformation to search for. <br>
     *                   (Enumerated type includes source, binary, dynamic-binary,
     *                    pacman, installed)<br>
     *
     * @throws Exception
     * @return a list of <code>Profile</code> containing profiles assocaited with
     *         the transformation. Returns <B>NULL</B> if no profiless found.
     *
     * @see org.globus.swift.catalog.util.Profile
     */
    public List<Profile> getTCPfnProfiles( String pfn, String resourceid, TCType type ) throws
        Exception {
        logMessage(
            "getTCPfnProfiles(String pfn, String resourceid, TCType type)" );
        logMessage( "\t getTCPfnProfiles(" + pfn + "," + resourceid + "," +
            type + ")" );

        List<Profile> result = null;
        List<Map<String, List<TCEntry>>> lfnMap = new ArrayList<Map<String, List<TCEntry>>>();
        if ( mTreeMap.containsKey( resourceid ) ) {
            lfnMap.add( mTreeMap.get( resourceid ) );
        }
        for ( Iterator<Map<String, List<TCEntry>>> i = lfnMap.iterator(); i.hasNext(); ) {
            for ( Iterator<List<TCEntry>> j = i.next().values().iterator();
                j.hasNext(); ) {
                for ( Iterator<TCEntry> k = j.next().iterator(); k.hasNext(); ) {
                    TCEntry tc = k.next();
                    List<Profile> profiles = null;
                    if ( tc.getPhysicalTransformation().equals( pfn ) ) {
                        if ( type == null || tc.getType().equals( type ) ) {
                            profiles = tc.getProfiles();
                        }
                        if ( profiles != null ) {
                            if ( result == null ) {
                                result = new ArrayList<Profile>( 10 );
                            }
                            result.addAll( profiles );
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * List all the contents of the TC in a column format.
     *
     * @return a list of String Arrays. Each string array contains the
     *         resource, lfn, pfn, type, sysinfo and profiles.
     *         The last entry in the list is an array of integers which contain
     *         the column lengths for pretty print.
     *
     * @throws Exception
     */

    public List<TCEntry> getTC() throws Exception {
        List<TCEntry> result=new ArrayList<TCEntry>();
        for ( Iterator<Map<String, List<TCEntry>>> i = 
            mTreeMap.values().iterator(); i.hasNext(); ) {
            for ( Iterator<List<TCEntry>> j = i.next().values().iterator();
                j.hasNext(); ) {
                for ( Iterator<TCEntry> k = j.next().iterator(); k.hasNext(); ) {
                    TCEntry tc = k.next();
                    result.add(tc);
                }

            }
        }
   /*     List result = null;
        int[] length = {0, 0, 0, 0, 0};
        for ( Iterator i = mTreeMap.values().iterator(); i.hasNext(); ) {
            for ( Iterator j = ( ( Map ) i.next() ).values().iterator();
                j.hasNext(); ) {
                for ( Iterator k = ( ( List ) j.next() ).iterator(); k.hasNext(); ) {
                    TransformationCatalogEntry tc = (
                        TransformationCatalogEntry ) k.next();
                    if ( result == null ) {
                        result = new ArrayList( 10 );
                    }
                    String[] s = {tc.getResourceId(),
                        tc.getLogicalTransformation(),
                        tc.getPhysicalTransformation(),
                        tc.getType().toString(), tc.getSysInfo().toString(),
                        ( ( tc.getProfiles() != null ) ?
                        ProfileParser.combine( tc.getProfiles() ) : "NULL" )};
                    columnLength( s, length );
                    result.add( s );
                }
            }
        }
        if ( result != null ) {
            result.add( length );
        }
    */
        return result;
    }

    /**
     *  ADDITIONS
     */

    /**
     * Add multiple TCEntries to the Catalog. Exception is thrown when error
     * occurs.
     *
     * @param entries list of {@link org.globus.swift.catalog.TCEntry}
     * objects as input.
     *
     * @return boolean Return true if succesful, false if error.
     *
     * @throws Exception
     * @see org.globus.swift.catalog.TCEntry
     */
    public boolean addTCEntry( List<TCEntry> entries ) throws
        Exception {
        for ( int i = 0; i < entries.size(); i++ ) {
            TCEntry entry = entries.get( i );
            this.addTCEntry( entry.getLogicalNamespace(),
                entry.getLogicalName(), entry.getLogicalVersion(),
                entry.getPhysicalTransformation(),
                entry.getType(), entry.getResourceId(), null,
                entry.getProfiles(), entry.getSysInfo() );
        }
        return true;

    }

    /**
     * Add an single entry into the transformation catalog.
     *
     * @param namespace    the namespace of the transformation to be added (Can be null)
     * @param name         the name of the transformation to be added.
     * @param version      the version of the transformation to be added. (Can be null)
     * @param physicalname the physical name/location of the transformation to be added.
     * @param type         the type of the physical transformation.
     * @param resourceid   the resource location id where the transformation is located.
     * @param lfnprofiles  the List of <code>Profile</code> objects associated
     *                     with a Logical Transformation. (can be null)
     * @param pfnprofiles  the list of <code>Profile</code> objects associated
     *                     with a Physical Transformation. (can be null)
     * @param system       the System information associated with a physical
     *                     transformation.
     * @return boolean     true if succesfully added, returns false if error and
     *                     throws exception.
     *
     * @throws Exception
     *
     * @see org.globus.swift.catalog.TCEntry
     * @see org.globus.swift.catalog.types.SysInfo
     * @see org.globus.swift.catalog.util.Profile
     */
    public boolean addTCEntry( String namespace, String name,
        String version,
        String physicalname, TCType type,
        String resourceid,
        List<Profile> pfnprofiles, List<Profile> lfnprofiles,
        SysInfo system ) throws
        Exception {

        TCEntry entry = new TCEntry();
        entry.setLogicalNamespace( namespace );
        entry.setLogicalName( name );
        entry.setLogicalVersion( version );
        entry.setPhysicalTransformation( physicalname );
        entry.setType( type );
        entry.setResourceId( resourceid );
        entry.setProfiles( lfnprofiles );
        entry.setProfiles( pfnprofiles );
        entry.setSysInfo( system );
        
        Map<String, List<TCEntry>> lfnMap = null;
        if ( mTreeMap.containsKey( resourceid ) ) {
            lfnMap = mTreeMap.get( resourceid );
        } else {
            lfnMap = new TreeMap<String, List<TCEntry>>();
            mTreeMap.put( resourceid, lfnMap );
        }

        List<TCEntry> pfnList = null;
        if ( lfnMap.containsKey( entry.getLogicalTransformation() ) ) {
            pfnList = lfnMap.get( entry.getLogicalTransformation() );
        } else {
            pfnList = new ArrayList<TCEntry>( 2 );
            lfnMap.put( entry.getLogicalTransformation(), pfnList );
        }
        boolean add = true;
        for ( Iterator<TCEntry> i = pfnList.iterator(); i.hasNext(); ) {
            TCEntry test = i.
                next();
            if ( test.equals( entry ) ) {
                add = false;
            }
        }
        if ( add ) {
            pfnList.add( entry );
            writeTC();
        } else {
            logger.debug( "TC Entry already exists. Skipping");
        }
        return true;

    }

    /**
     * Add additional profiles to a matching logical transformation.
     *
     * @param namespace the nsamespace of the transformation to be added. (can be null)
     * @param name      the name of the transformation to be added.
     * @param version   the version of the transformation to be added.
     * @param profiles  list of <code>Profile</code> objects that are to be
     *                  added to the transformation.
     *
     * @return boolean
     * @throws UnsupportedOperationException as function not implemented.
     */
    public boolean addTCLfnProfile( String namespace, String name,
        String version,
        List<Profile> profiles ) throws Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * Adds additional profiles to a physical transformation.
     *
     * @param pfn          the physical name of the transformation
     * @param type         the type of transformation that the profile is
     *                     associated with. If null the profile is associated
     *                     with all the types.
     * @param resourcename the resource on which the physical transformation exists.
     * @param profiles     the List of <code>Profile</code> objects that are to
     *                     be added to the transformation.
     *
     * @return boolean
     * @throws UnsupportedOperationException as function not implemented.
     */
    public boolean addTCPfnProfile( String pfn, TCType type,
        String resourcename,
        List<Profile> profiles ) throws Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * DELETIONS
     */

    /**
     * Delete all entries in the transformation catalog of the logical
     * transformation either at all resource or on a particular resource
     *
     * @param namespace   the nsamespace of the transformation to be added. (can be null)
     * @param name        the name of the transformation to be added.
     * @param version     the version of the transformation to be added.
     * @param resourceid  the resource id for which the transformation is to be
     *                    deleted. If null then transformation on all resource
     *                    are deleted.
     * @param type        the type of the transformation
     *
     * @throws Exception
     * @return boolean
     */
    public boolean deleteTCbyLogicalName( String namespace, String name,
        String version, String resourceid,
        TCType type ) throws Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    public boolean deleteTCbyPhysicalName( String physicalname,
        String namespace,
        String name, String version,
        String resourceid, TCType type ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * Delete a paricular type of transformation, additionally either on all
     * resources or on a particular resource.
     *
     * @param type        the type of the transformation.
     * @param resourceid  the resource on which the transformation exists.
     *                    If null then that type of transformation is deleted
     *                    from all the resources.
     *
     * @throws UnsupportedOperationException as function not implemented.
     * @return boolean
     */
    public boolean deleteTCbyType( TCType type, String resourceid ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * Delete entries in the catalog of a particular systeminfo.
     *
     * @param sysinfo SysInfo
     *
     * @throws UnsupportedOperationException as function not implemented.
     * @return boolean
     */
    public boolean deleteTCbySysInfo( SysInfo sysinfo ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    /**
     * Delete an entire resource from the transformation catalog.
     * @param resourceid String
     *
     * @return boolean
     * @throws UnsupportedOperationException as function not implemented.
     */
    public boolean deleteTCbyResourceId( String resourceid ) throws Exception {

        if ( mTreeMap.containsKey( resourceid ) ) {
            mTreeMap.remove( resourceid );
        }
        writeTC();
        return true;
    }

    /**
     * Deletes the entire transformation catalog. Whoopa.....
     *
     * @return boolean
     * @throws Exception
     */
    public boolean deleteTC() throws Exception {
        mTreeMap.clear();
        return true;
    }

    public boolean deleteTCPfnProfile( String physicalname, TCType type,
        String resourceid, List<Profile> profiles ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    public boolean deleteTCLfnProfile( String namespace, String name,
        String version, List<Profile> profiles ) throws
        Exception {
        throw new UnsupportedOperationException( "Not Implemented" );
    }

    public boolean connect( java.util.Properties props ) {
        //not implemented
        return true;
    }

    public boolean isClosed() {
        //not impelemented
        return true;
    }

    public void close() {
        //not impelemented
    }

    private void writeTC() {
        PrintWriter writer = null;
        try {
            logger.debug( "Starting to write the TC file");
            writer = new PrintWriter( new BufferedWriter( new FileWriter(
                mTCFile, false ) ) );

        } catch ( IOException e ) {
            logger.warn(
                "Unable to open TC File for writing\"" + mTCFile, e);
        }
        int count = 0;
        for (Map<String,List<TCEntry>> map : 
            mTreeMap.values()) {
            //get all the values from the main map
            for (List<TCEntry> list : map.values()) {
                //for each resource and each logical transformatino get the arraylist.
                for (TCEntry entry : list) {
                    writer.println(entry.toTCString());
                    count++;
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug( "Written " + count +
                " entries back to the TC file");
        }
        writer.flush();
        writer.close();
        logger.debug( "Starting to write the TC file");
    }



    /**
     * Computes the maximum column lenght for pretty printing.
     *
     * @param s String[]
     * @param count int[]
     */
    private static void columnLength( String[] s, int[] count ) {
        for ( int i = 0; i < count.length; i++ ) {
            if ( s[ i ].length() > count[ i ] ) {
                count[ i ] = s[ i ].length();
            }
        }

    }

    /**
     * Populates the internal copy of the transformation catalog from a byte
     * stream (input stream). Used in webservices, when clients upload their files.
     * It uses the default character encoding.
     *
     * @param reader  the <code>InputStrean</code> containing the bytes to be
     *                read.
     */
    private boolean populateTC( InputStream reader ) {
        return populateTC( new InputStreamReader( reader ) );
    }

    /**
     * Populates the internal copy of the transformation catalog from the file
     * containing the transformation catalog in the 6 column format.
     *
     * @return boolean
     */
    private boolean populateTC() {
        boolean result = false;

        try {
            result = populateTC( new FileReader( mTCFile ) );
        } catch ( FileNotFoundException ex ) {
            logger.warn( "The tc text file " + mTCFile +
                " was not found");
            logger.warn( "Considering it as Empty TC");
            return true;
        } 
        return result;
    }

    /**
     * Adds multiple entries into the TC.  Calls the above API multiple times.
     *
     * @param reader  the input stream from where to read the contents of the
     *                transformation catalog.
     * @return boolean
     */
    private boolean populateTC( Reader reader ) {
        BufferedReader buf = new BufferedReader( reader );
        // String profilestring = null;
        int linecount = 0;
        int count = 0;
        try {
            String line = null;
            //buf = new BufferedReader( new FileReader( mTCFile ) );
            while ( ( line = buf.readLine() ) != null ) {
                linecount++;
                if ( ! ( line.startsWith( "#" ) ||
                    line.trim().equalsIgnoreCase( "" ) ) ) {
                    TCEntry tc = new
                        TCEntry();
                    String[] tokens = line.split( "[ \t]+", 6 );
                    for ( int i = 0; i < tokens.length; i++ ) {
                        switch ( i ) {
                            case 0: //poolname
                                tc.setResourceId( tokens[ i ] );
                                break;
                            case 1: //logical transformation name
                                if ( tokens[ i ].indexOf( "__" ) != -1 ) {
                                    logger.warn(
                                        "Logical Transformations in the new File TC " +
                                        "are represented as NS::NAME:VER");
                                    logger.warn( "Assuming " + tokens[ i ] +
                                        " as just the transformation NAME.");
                                }
                                tc.setLogicalTransformation( tokens[ i ] );
                                break;
                            case 2: //pfn
                                tc.setPhysicalTransformation( tokens[ i ] );
                                break;
                            case 3: //type
                                tc.setType( ( tokens[ i ].equalsIgnoreCase(
                                    "null" ) ) ?
                                    TCType.INSTALLED :
                                    TCType.fromString( tokens[ i ] ) );
                                break;
                            case 4: //systeminfo
                                tc.setSysInfo( ( tokens[ i ].equalsIgnoreCase(
                                    "null" ) ) ?
                                    new SysInfo( null ) :
                                    new SysInfo( tokens[ i ] ) );
                                break;
                            case 5: //profile string
                                if ( !tokens[ i ].equalsIgnoreCase( "null" ) ) {
                                    try {
                                        tc.setProfiles( ProfileParser.parse(
                                            tokens[
                                            i ] ) );
                                    } catch ( ProfileParserException ppe ) {
                                        logger.warn(
                                            "Warning: malformed tc.data file on line " +
                                            linecount);

                                    }
                                }
                                break;
                            default:
                                logger.warn( "Line " + linecount +
                                    " : Humm no need to be in default");
                        } //end of switch
                    } //end of for loop
                    // if (count > 0) {

                    //   mLogger.logMessage("Loading line number" + linecount +
                    //                    " to the map", 1);
                    Map<String, List<TCEntry>> lfnMap = null;
                    if ( !mTreeMap.containsKey( tc.getResourceId() ) ) {
                        lfnMap = new TreeMap<String, List<TCEntry>>();
                    } else {
                        lfnMap = mTreeMap.get( tc.getResourceId() );
                    }
                    List<TCEntry> entries = null;
                    if ( !lfnMap.containsKey( tc.getLogicalTransformation() ) ) {
                        entries = new ArrayList<TCEntry>( 3 );
                    } else {
                        entries = lfnMap.get( tc.
                            getLogicalTransformation() );
                    }
                    entries.add( tc );
                    lfnMap.put( tc.getLogicalTransformation(), entries );
                    mTreeMap.put( tc.getResourceId(), lfnMap );
                    count++;
                } //end of if "#"
            } //end of while line
            if (logger.isDebugEnabled()) {
                logger.debug( "Loaded " + count + " entries to the TC Map");
            }
            buf.close();
            return true;
        } catch ( FileNotFoundException ex ) {
            logger.warn( "The tc text file " + mTCFile +
                " was not found");
            logger.warn( "Considering it as Empty TC");
            return true;
        } catch ( IOException e ) {
            logger.warn( "Unable to open the file " +
                mTCFile, e);
            return false;
        } catch ( IllegalStateException e ) {
            logger.warn( "On line " + linecount + "in File " +
                mTCFile + "\n", e);
            return false;
        } catch ( Exception e ) {
            logger.warn(
                "While loading entries into the map on line " + linecount +
                "\n", e);
            return false;
        }
    }

    /**
     * Logs the message to a logging stream. Currently does not log to any stream.
     *
     * @param msg  the message to be logged.
     */
    protected void logMessage( String msg ) {
    	if (logger.isDebugEnabled()) {
    	    logger.debug(msg);
    	}
    }
}
