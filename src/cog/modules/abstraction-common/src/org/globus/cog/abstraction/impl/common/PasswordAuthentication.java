//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 12, 2009
 */
package org.globus.cog.abstraction.impl.common;

public class PasswordAuthentication {
    private String username;
    private char[] password;
    
    public PasswordAuthentication(String username, char[] password) {
        this.username = username;
        this.password = password == null ? null : (char[]) password.clone();
    }
    
    public String getUsername() {
        return username;
    }
    
    public char[] getPassword() {
        return password;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(char[] password) {
        this.password = password;
    }
}
