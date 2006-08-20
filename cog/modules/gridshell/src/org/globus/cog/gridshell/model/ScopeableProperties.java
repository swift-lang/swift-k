/*
 * 
 */
package org.globus.cog.gridshell.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.getopt.app.ArgParserImpl;
import org.globus.cog.gridshell.interfaces.Scope;

/**
 * <p>
 * Allows property values to be processed with a given scope. Keys are NOT
 * processed using the scope.
 * </p>
 * 
 * <p>
 * The default Scope includes <code>ScopeImp.getSystemScope()</code> plus:
 * </p>
 * <ul>
 * <li>this.path.uri=the directory of the current properties file expressed as
 * a uri</li>
 * <li>this.path.string=the directory of the current properties file expressed
 * as a string</li>
 * <li>this.path=the directory of the current properties file expressed as a
 * string</li>
 * <li>this.path.url=the directory of the current properties file expressed as
 * a url</li>
 * </ul>
 * <p>
 * Current properties file is the file object associated with
 * ScopableProerperies and will not work if the user does not use the
 * loadFromFile or the Constructor to load the properties
 * </p>
 * <p>
 * Example configuration file:
 * </p>
 * <code> 
 * # a file location<br/>
 * file.location=${this.path.string}/another-path/file.extention<br/>
 * # a new classpath<br/>
 * new.classpath=${java.class.path}${path.separator}${this.path.string}<br/>
 * # the cog properties file<br/>
 * cog.home=${globus.home}/cog.properties
 * </code>
 * 
 *  
 */
public class ScopeableProperties {
	private static final Logger logger = Logger.getLogger(ScopeableProperties.class);
	
	private Scope _this,_super,scope;

	private File file;
	
	private String superKey = null;
		
	public ScopeableProperties() throws IOException, ScopeException {
		this(null,null);
	}	
	public ScopeableProperties(File file) throws IOException, ScopeException {
		this(file,null);
	}
	public ScopeableProperties(File file,String superKey) throws IOException, ScopeException {
	    this.superKey = superKey;
	    scope = new ScopeImpl(ScopeImpl.getSystemScope());
	    _super = new ScopeImpl();
	    _this = new ScopeImpl(_super);
	    
	    if(file != null) {
	        loadFromFile(file);
	    }
	}		
	/**
	 * Just gives a warning and calls the superclass
	 */
	public void load(InputStream stream) throws IOException {
		logger.warn("Loading from a stream possibly does not allow for variables to be the correct values");
		loadFromStream(stream);
	}	
	
	/**
	 * Loads from a file, if the user does not use this method the value
	 * ${this.path} may not be correct
	 * 
	 * @throws IOException@throws ScopeExceptio
	 * @throws ScopeExceptionn
	 */
	public void loadFromFile(File file) throws IOException, ScopeException {
		logger.info("loadFromFile()");
		this.file = file;
		initScope();
		loadFromStream(new FileInputStream(getFile()));		
	}
	
	private void loadFromStream(InputStream stream) throws IOException {
	    Properties properties = new Properties();
	    properties.load(stream);
	    
	    // process _this
	    Iterator iKeys = properties.keySet().iterator();
	    while(iKeys.hasNext()) {
	        String key = (String)iKeys.next();
	        String value = properties.getProperty(key);
	        try {
                _this.setVariableTo(key,value);
            } catch (ScopeException e) {
                logger.debug("failed to set '"+key+"' for file '"+getFile()+"'",e);
            }
	    }
	    
		// if the superKey is null we are done
		if(superKey == null) {
		    logger.info("returning because superKey==null");
		    return;
		}else {
		    logger.debug("superKey="+superKey);
		}
		
		// process _super
		Collection superFileKeys = createList(keySet(superKey),this.superKey);
		Iterator iSuperFileKeys = superFileKeys.iterator();
		while(iSuperFileKeys.hasNext()) {
		    String fileKey = (String)iSuperFileKeys.next();
		    String fileName = (String)this.getProperty(fileKey);
		    
		    logger.debug("superFileName="+fileName);
		    
		    ScopeableProperties superProperties = null;
		    try {
                superProperties = new ScopeableProperties(new File(fileName),superKey);
            } catch (Exception e) {
                logger.warn("Could not load the super file '"+fileName+"'"+" from '"+this.getFile()+"'",e);
            }
            
            // if we got the superProperties
            if(superProperties!=null) {
                logger.info("loading the super class");
                
                Iterator iVarName = superProperties.keySet().iterator();
                while(iVarName.hasNext()) {
                    String varName = (String)iVarName.next();
                    logger.debug("processing super varName="+varName);
                    try {
                        _super.setVariableTo(varName,superProperties.getProperty(varName));
                    } catch (ScopeException e) {
                        logger.warn("Couldn't set variable '"+varName+"'",e);
                    }
                }
            }
		}
	}

	/**
	 * Allows a default value to be assciated with the command
	 * 
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String propertyName, String defaultValue) {
		String result = getProperty(propertyName);
		if (result == null) {
			return defaultValue;
		} else {
			return ArgParserImpl.processVariablesForScope(result, getScope());
		}
	}

	/**
	 * Override the return of getProperty to the variable's values
	 */
	public String getProperty(String propertyName) {
		String result = (String)_this.getValue(propertyName);
		result = ArgParserImpl.processVariablesForScope(result, getScope());
		return result;
	}
	
	public Set keySet() {
	    return _this.getVariableNames();
	}
	
	/**
	 * Gets the key set such that includes each key that starts with prefix
	 * @param prefix
	 * @return
	 */
	public Set keySet(String prefix) {
		Set result = new TreeSet();
		if(prefix != null) {
			Iterator iKeys = this.keySet().iterator();
			while(iKeys.hasNext()) {
				String key = (String)iKeys.next();
				if(key.startsWith(prefix)) {
					result.add(key);
				}
			}
		}		
		return result;
	}
	
	public Scope getSubScope(String prefix) {
	    Scope result = new ScopeImpl();
	    if(prefix != null) {
			Iterator iKeys = this.keySet().iterator();
			while(iKeys.hasNext()) {
				String key = (String)iKeys.next();
				if(key.startsWith(prefix)) {
				    String subKey = key.substring(prefix.length(),key.length());
					try {
                        result.setVariableTo(subKey,getProperty(key));
                    } catch (ScopeException e) {
                        logger.warn("Couldn't set variable '"+subKey+"'",e);
                    }
				}
			}
		}
	    return result;
	}

	/**
	 * Resturns the file object associated with this Properties
	 * 
	 * @return
	 */
	public File getFile() {
		synchronized (file) {
			return file;
		}
	}
	/**
	 * Returns the scope
	 * 
	 * @return
	 */
	public Scope getScope() {
	    if(scope==null) {
	        return null;
	    }
		synchronized (scope) {
			return scope;
		}
	}

	/**
	 * These are the default variables supported ${this.path} -> the path to the
	 * directory of this file to add more variables override this method
	 * @throws ScopeException
	 */
	public void initScope() throws ScopeException {
		File dir = getFile().getParentFile();
		scope.setVariableTo("this.path.uri", dir.getAbsoluteFile().toURI());
		scope.setVariableTo("this.path.string", dir.getAbsoluteFile().toString());		
		scope.setVariableTo("this.path", dir.getAbsoluteFile().toString());
		try {
			scope.setVariableTo("this.path.url", dir.getAbsoluteFile().toURL().toString());
		} catch (MalformedURLException malformedURLException) {		
			logger.warn("Couldn't add this.path.url",malformedURLException);
		}
		
	}
	/**
     * <p>
     * Trys to convert keys to a number if can't then it compares as String
     * </p>
     */
	public static class KeyComparator implements Comparator {
	    private String prefix;
	    
	    public KeyComparator(String prefix) {
	        this.prefix = prefix;
	    }
        public int compare(Object objA, Object objB) {
            String aStr = String.valueOf(objA);
            String bStr = String.valueOf(objB);
            
            Comparable a = null, b = null;
            
            if(prefix!=null) {
                if(!aStr.startsWith(prefix) || !bStr.startsWith(prefix)) {
                    throw new RuntimeException("keys must start with the prefix: "+prefix);
                }
                aStr = aStr.substring(prefix.length(),aStr.length());
                bStr = bStr.substring(prefix.length(),bStr.length());
                
                try {
                    a = new Integer(Integer.parseInt(aStr));
                }catch(Exception e) {
                    a = aStr;
                }
                try {
                    b = new Integer(Integer.parseInt(bStr));
                }catch(Exception e) {
                    b = bStr;
                }
            }else {
                a = aStr;
                b = bStr;
            }
            
            return a.compareTo(b);
        }
	}
	
	public static List createList(Set keys,String prefix) {
	    Comparator comparator = new KeyComparator(prefix);	    
	    List result = new ArrayList();
	    result.addAll(keys);
	    Collections.sort(result,comparator);
	    return result;
	}
}