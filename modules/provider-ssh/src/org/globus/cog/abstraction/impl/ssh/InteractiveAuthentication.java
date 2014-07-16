//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 12, 2009
 */
package org.globus.cog.abstraction.impl.ssh;

public class InteractiveAuthentication {
    private String username, key;
    private char[] password, passphrase;
    
    public InteractiveAuthentication() {   
    }

    public InteractiveAuthentication(String username, char[] password,
            String key, char[] passphrase) {
        this.username = username;
        this.password = password == null ? null : (char[]) password.clone();
        this.key = key;
        this.passphrase = passphrase == null ? null : (char[]) passphrase.clone();
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getKeyFile() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public char[] getPassPhrase() {
        return passphrase;
    }

    public void setPassPhrase(char[] passphrase) {
        this.passphrase = passphrase;
    }

    public void reset() {
    }
}
