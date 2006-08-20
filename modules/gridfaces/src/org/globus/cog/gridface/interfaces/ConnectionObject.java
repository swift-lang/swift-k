
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.interfaces;

import org.globus.cog.abstraction.interfaces.Identity;

// we need to define a coupleof exceptions

public interface ConnectionObject{

	public void setSessionId(Identity  identity);
	public Identity getSessionId();
	
	public void setUsername(String username);
	public void setPassword(String password);
	
    /** Get Hostname of the underlying client **/
    public String getHost();

    /** Set the hostname for the underlying connection **/
    public void setHost(String host);

    /** Get Port of the underlying client **/
    public String getPort();

    /** Set port for the underlying connection **/
    public void setPort(String port);
    
    /** Set the protocol for the underlying connection **/
    public String getProtocol();
    
    /** Set the protocl for the underlyging connection**/
    public void setProtocol(String protocol);

    /** Get Port of the underlying client **/
    public String getSecurityContext();

    /** Set port for the underlying connection **/
    public void setSecurityContext(String port);

    /** Make a new connection *
     * @throws Exception*/
    public GridCommand connect(String protocol, String host, String port) throws Exception;

    /** Close the connection. Disconnect **/
    public GridCommand disConnect();

    /** reconnect the connection *
     * @throws Exception*/
    public GridCommand reConnect() throws Exception;

    /** Call when browser window is closed. Same as disconnect **/
    public GridCommand close();

    /** Get Proxy for this connection**/
    public void getProxy();

    /** Set proxy for this connection **/
    public void setProxy();
    
    

}
