
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.beans;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.globus.mds.gsi.common.GSIMechanism;


/**
 * Bean which does the same functionality as
 * org.globus.tools.GlobusInfoSearch.  Instead of redirecting the
 * output to Standard input and output as in GlobusRun, it redirects
 * to a Print Stream
 */

// we could add: aliasing, referral support
public class GridSearch {

    //Default values
    private static final String version = 
	OgceVersion.getVersion();
    
    private static final String DEFAULT_CTX = 
	"com.sun.jndi.ldap.LdapCtxFactory";

    private String hostname;
    private int port = 2135;
    private String baseDN = "mds-vo-name=local, o=grid";
    private int timeOutInSeconds = 30;
    private int scope = SearchControls.SUBTREE_SCOPE;
    private int ldapVersion = 3;
    private int sizeLimit = 0;
    private int timeLimit = 0;
    private boolean ldapTrace = false;
    private String saslMech = "GSI";
    private String bindDN;
    private String password;
    private String qop = "auth";
    private boolean verbose = false;
    private PrintStream out=System.out;
    private PrintStream err=System.err;
    private boolean system=false;
    private String args[];
    private String msg="";
    public GridSearch() {
    }

    public String getHostname() {
	if (hostname == null) {
	    try {
		setHostname(InetAddress.getLocalHost().getHostName());
	    } catch ( UnknownHostException e ) {
		error( "Error getting hostname: " + e.getMessage() );
		if (system) System.exit(1); 
	    }
	}
	return hostname;
    }

    public void search(String filter, String [] attributes) {

	Hashtable env = new Hashtable();

	String url = "ldap://" + getHostname() + ":" + port;

	if (verbose) {
	    msg("Connecting to: " + url);
	}

	env.put("java.naming.ldap.version", String.valueOf(ldapVersion));
	env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_CTX);
	env.put(Context.PROVIDER_URL, url);

	if (ldapTrace) {
	    env.put("com.sun.jndi.ldap.trace.ber", System.err);
	}

	if (bindDN != null) {
	    env.put(Context.SECURITY_PRINCIPAL, bindDN);
	}

	if (saslMech != null) {
	    
	    if (saslMech.equalsIgnoreCase("GSI") ||
		saslMech.equalsIgnoreCase(GSIMechanism.NAME)) {
		saslMech = GSIMechanism.NAME;
		env.put("javax.security.sasl.client.pkgs", 
			"org.globus.mds.gsi.jndi");
	    }
	    
	    env.put(Context.SECURITY_AUTHENTICATION, saslMech);

	    env.put("javax.security.sasl.qop", qop);

	} else {
	    // default to simple authentication
	    env.put(Context.SECURITY_AUTHENTICATION, "simple");
	    if (password != null) {
		env.put(Context.SECURITY_CREDENTIALS, password);
	    }
	}

	LdapContext ctx = null;
	SearchControls constraints=null;
	try {
	    ctx = new InitialLdapContext(env, null);

	    constraints = new SearchControls();

	    constraints.setSearchScope(scope);  
	    constraints.setCountLimit(sizeLimit);
	    constraints.setTimeLimit(timeLimit);
	    constraints.setReturningAttributes(attributes);
	    NamingEnumeration results = ctx.search(baseDN, filter, constraints);

	    displayResults(results);


	} catch (Exception e) {
	    error("Failed to search: "+getStackTrace(e) );

	} finally {
	    if (ctx != null) {
		try { ctx.close(); } catch (Exception e) {}
	    }
	}

    }

    public void displayResults(NamingEnumeration results) 
	throws NamingException {

	if (results == null) return;
	
	String dn;
	String attribute;
	Attributes attrs;
	Attribute at;
	SearchResult si;

	while (results.hasMoreElements()) {
	    si = (SearchResult)results.next(); 
	    attrs = si.getAttributes();

	    if (si.getName().trim().length() == 0) {
		dn = baseDN;
	    } else {
		dn = si.getName() + ", " + baseDN;
	    }
	    msg("dn: " + dn);

	    for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();) {
		at = (Attribute)ae.next();
		
		attribute = at.getID();

		Enumeration vals = at.getAll();
		while(vals.hasMoreElements()) {
		    msg(attribute + ": " + vals.nextElement());
		}
	    }
	}
    }

     public static String getVersion() {
	return version;
    }

    public void setScope( int scope ) {
	this.scope = scope;
    }

    public void setLdapVersion ( int version ) {
	this.ldapVersion = version;
    }

    public void setSizeLimit ( int limit ) {
	this.sizeLimit = limit;
    }

    public void setTimeLimit ( int limit ) {
	this.timeLimit = limit;
    }

    public void setLdapTrace( boolean trace ) {
	this.ldapTrace = trace;
    }

    public void setHostname( String hostname ) {
	this.hostname = hostname;
    }

    public void setPort( int port ) {
	this.port = port;
    }

    public void setBaseDN( String baseDN ) {
	this.baseDN = baseDN;
    }

    public void setTimeout( int timeOutInSeconds ) {
	this.timeOutInSeconds = timeOutInSeconds;
    }

    public void setSaslMech( String mech ) {
	this.saslMech = mech;
    }

    public void setQOP( String qop ) {
	this.qop = qop;
    }

    public void setBindDN( String bindDN ) {
	this.bindDN = bindDN;
    }

    public void setPassword( String pwd ) {
	this.password = pwd;
    }

    public void setVerbose( boolean verbose ) {
	this.verbose = verbose;
    }

    public boolean isVerbose() {
	return this.verbose;
    }

    public String getSyntaxString() {
	return
	    "\n"
	    + "Syntax : grid-info-search [ options ] "
	    + "<search filter> [attributes]\n\n"
	    + "Use -help to display full usage.";
    }

    public String getHelpString() {
	return
	    "\n"
	    + "grid-info-search [ options ] <search filter> [attributes]\n\n"
	    + "    Searches the MDS server based on the search filter, where some\n"
	    + "    options are:\n"
	    + "       -help\n"
	    + "               Displays this message\n"
	    + "\n"
	    + "       -version\n"
	    + "               Displays the current version number\n"
	    + "\n"
	    + "       -mdshost host (-h)\n"
	    + "               The host name on which the MDS server is running\n"
	    + "               The default is " + getHostname() + ".\n"
	    + "\n"
	    + "       -mdsport port (-p)\n"
	    + "               The port number on which the MDS server is running\n"
	    + "               The default is " + String.valueOf( port ) + "\n"
	    + "\n"
	    + "       -mdsbasedn branch-point (-b)\n"
	    + "               Location in DIT from which to start the search\n"
	    + "               The default is '" + baseDN + "'\n"
	    + "\n"
	    + "       -mdstimeout seconds (-T)\n"
	    + "               The amount of time (in seconds) one should allow to\n"
	    + "               wait on an MDS request. The default is "
	    + String.valueOf( timeOutInSeconds ) + "\n"
	    + "\n" 
	    + "       -anonymous (-x)\n"
	    + "               Use anonymous binding instead of GSSAPI."
	    + "\n\n"
	    + "     grid-info-search also supports some of the flags that are\n"
	    + "     defined in the LDAP v3 standard.\n" 
	    + "     Supported flags:\n\n" 
	    + "      -s scope   one of base, one, or sub (search scope)\n" 
	    + "      -P version procotol version (default: 3)\n" 
	    + "      -l limit   time limit (in seconds) for search\n" 
	    + "      -z limit   size limit (in entries) for search\n" 
	    + "      -Y mech    SASL mechanism\n"
	    + "      -D binddn  bind DN\n" 
	    + "      -v         run in verbose mode (diagnostics to standard output)\n"
	    + "      -O props   SASL security properties (auth, auth-conf, auth-int)\n" 
	    + "      -w passwd  bind passwd (for simple authentication)\n"
	    + "\n";
    }

    public  String getValue(int i, String [] args, String option) {
	if ( i >= args.length ) {
	    System.err.println("Error: argument required for : " + option);
	    if (system) System.exit(1);  
	}
	return args[i];
    }

    public  int getValueAsInt(int i, String [] args, String option) {
	String value = getValue(i, args, option);
	try {
	    return Integer.parseInt(value);
	} catch (Exception e) {
	    System.err.println("Error: value '" + value + "' is not an integer for : " + option);
	    	    if (system) System.exit(1);  
	    return -1;
	}
    }

    public static void main( String [] args ) {
	GridSearch gridInfoSearch = new GridSearch();	
	gridInfoSearch.execute(args,gridInfoSearch);
    }
    public  void execute( String [] args,GridSearch gridInfoSearch ) {
	this.args=args;
	

	int i;
	for (i=0;i<args.length;i++) {
	    if (args[i].startsWith("-")) {
		
		String option = args[i];

		// no arg required
		if ( option.equalsIgnoreCase( "-ldapTrace" ) ) {
		    gridInfoSearch.setLdapTrace(true);
		} else if ( option.equalsIgnoreCase( "-help" ) ) {
		    System.err.println( gridInfoSearch.getHelpString() );
		    if (system) System.exit(1);  else return;
		} else if ( option.equalsIgnoreCase( "-version" ) ) {
		    System.err.println( GridSearch.getVersion() );
		    if (system) System.exit(1);  else return;		    
		} else if ( option.equalsIgnoreCase( "-mdshost" ) ||
			    option.equals( "-h" ) ) {
		    gridInfoSearch.setHostname( getValue(++i, args, option) );
		} else if ( option.equalsIgnoreCase( "-mdsport" ) ||
			    option.equals( "-p" ) ) {
		    gridInfoSearch.setPort( getValueAsInt(++i, args, option) );
		} else if ( option.equalsIgnoreCase( "-mdsbasedn" ) ||
			    option.equals( "-b" ) ) {
		    gridInfoSearch.setBaseDN(  getValue(++i, args, option) );
		} else if ( option.equalsIgnoreCase( "-mdstimeout" ) ||
			    option.equals( "-T" ) ) {
		    gridInfoSearch.setTimeout( getValueAsInt(++i, args, option) );
		} else if ( option.equals( "-s" ) ) {
		    String value = getValue(++i, args, option);
		    if (value.equalsIgnoreCase("one")) {
			gridInfoSearch.setScope(SearchControls.ONELEVEL_SCOPE);
		    } else if (value.equalsIgnoreCase("base")) {
			gridInfoSearch.setScope(SearchControls.OBJECT_SCOPE);
		    } else if (value.equalsIgnoreCase("sub")) {
			gridInfoSearch.setScope(SearchControls.SUBTREE_SCOPE);
		    } else {
			System.err.println("Error: invalid scope parameter : " + value);
			if (system) System.exit(1);  else return;
		    }
		} else if ( option.equals( "-P" ) ) {
		    gridInfoSearch.setLdapVersion( getValueAsInt(++i, args, option) );
		} else if ( option.equals( "-l" ) ) {
		    gridInfoSearch.setTimeLimit(  getValueAsInt(++i, args, option) );
		} else if ( option.equals( "-z" ) ) {
		    gridInfoSearch.setSizeLimit( getValueAsInt(++i, args, option) );
		} else if ( option.equals( "-Y" ) ) {
		    gridInfoSearch.setSaslMech(  getValue(++i, args, option) );
		} else if ( option.equals( "-D" ) ) {
		    gridInfoSearch.setBindDN(  getValue(++i, args, option) );
		} else if ( option.equals( "-v" ) ) {
		    gridInfoSearch.setVerbose(true);
		} else if ( option.equals( "-x" ) ||
			    option.equalsIgnoreCase( "-anonymous" ) ) {
		    gridInfoSearch.setSaslMech( null );
		} else if ( option.equals( "-O" )) {
		    gridInfoSearch.setQOP( getValue(++i, args, option) );
		} else if ( option.equals( "-w" ) ) {
		    gridInfoSearch.setPassword( getValue(++i, args, option) );
		} else {
		    System.err.println("Error: unrecognized argument : " + option);
		    if (system) System.exit(1);  else return;
		}
	    } else {
		break;
	    }
	}
	
	String filter = null;
	if (i == args.length) {
	    filter = "(objectclass=*)";
	} else {
	    filter = args[i];
	}
	
	String [] attribs = null;

	if (++i < args.length) {
	    int size = args.length - i;
	    attribs = new String[size];
	    System.arraycopy(args, i, attribs, 0, size);
	}

	if (gridInfoSearch.isVerbose()) {
	    msg("filter: " + filter);
	    if (attribs == null) {
		msg("attribs: none");
	    } else {
		System.out.print("attribs: ");
		for (i=0;i<attribs.length;i++) {
		    System.out.print(attribs[i] + " ");
		}
	    }
	}

	gridInfoSearch.search(filter, attribs);
    }
    public  void msg(String s){
	out.println(s);
	msg+=s+"\n";
    }
    public  void msg(int n){
	msg(n+"");
    }
    public  void msg(Object o){
	msg(o.toString()+"");
    }
    public  void error(int n){
	error(n+"");
    }
    public  void error(Object o){
	error(o.toString()+"");
    }
    public  void error(String s){
	err.println("GRIS ERROR MESSAGE: \n"+s);
	msg+="GRIS ERROR MESSAGE: \n"+s;
    }     
    public  PrintStream getOutputStream(){
	return out;
    } 
    public  void setOutputStream(PrintStream out){
	this.out=out;
    } 
    public  PrintStream getInputStream(){
	return err;
    } 
    public  void setInputStream(PrintStream err){
	this.err=err;
    } 
    public  String[] getArgs(){
	return args;
    } 
    public  void setArgs(String args[]){
	this.args=args;
    } 
    public  boolean getSystem(){
	return system;
    } 
    public  void setSystem(boolean system){
	this.system=system;
    } 
    public String getMessage(){
	return msg;
    } 
    public  void setMessage(String msg){
	this.msg=msg;
    }     
   private String getStackTrace(Exception e)   {
       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter(sw);
       e.printStackTrace(pw);
       return sw.toString();
   }
    
}
