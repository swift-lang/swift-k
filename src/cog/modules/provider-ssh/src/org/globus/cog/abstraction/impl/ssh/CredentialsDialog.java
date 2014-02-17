//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.ssh;

import java.awt.GraphicsEnvironment;
import java.io.File;

public abstract class CredentialsDialog extends org.globus.cog.abstraction.impl.common.CredentialsDialog {
    private static final String SSH_HOME = System.getProperty("user.home")
            + File.separator + ".ssh";

    protected String privateKey;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privatekey) {
        this.privateKey = privatekey;
    }


    public abstract char[][] getResults();

    public static Object showCredentialsDialog(String host) {
        return showCredentialsDialog(host, null, (String) null);
    }

    public static Object showCredentialsDialog(String host, String userName,
            String privateKey) {
        return showCredentialsDialog(host, userName, privateKey, false);
    }

    public static char[][] showCredentialsDialog(String host, String userName,
            String privateKey, boolean forceTextMode) {
        return showCredentialsDialog(host, new Prompt[] {
                new Prompt("Username: ", Prompt.TYPE_TEXT, userName),
                new Prompt("Private Key: ", Prompt.TYPE_FILE, privateKey) },
            forceTextMode);
    }

    public static char[][] showCredentialsDialog(String title, Prompt[] prompts) {
        return showCredentialsDialog(title, prompts, false);
    }

    public static char[][] showCredentialsDialog(String title,
            Prompt[] prompts, boolean forceTextMode) {
        org.globus.cog.abstraction.impl.common.CredentialsDialog cd;
        try {
            if (GraphicsEnvironment.isHeadless() || forceTextMode) {
                cd = new ConsoleCredentialsDialog(title, prompts);
            }
            else {
                cd = new SwingCredentialsDialog(title, prompts);
            }
        }
        catch (InternalError e) {
            cd = new ConsoleCredentialsDialog(title, prompts);
        }
        return cd.getResults();
    }

    public static class SwingCredentialsDialog extends org.globus.cog.abstraction.impl.common.CredentialsDialog.SwingCredentialsDialog {
    	
    	public SwingCredentialsDialog(String title, Prompt[] prompts) {
    		super(title, prompts);
    	}
    	
        @Override
        protected String getDefaultFilePath() {
            return SSH_HOME;
        }
    }

    public static class ConsoleCredentialsDialog extends org.globus.cog.abstraction.impl.common.CredentialsDialog.ConsoleCredentialsDialog {

        public ConsoleCredentialsDialog(String title, Prompt[] prompts) {
            super(title, prompts);
        }

        protected String getDefaultPrivateKey() {
            File pk;
            pk = new File(SSH_HOME, "identity");
            if (pk.exists()) {
                return pk.getAbsolutePath();
            }
            pk = new File(SSH_HOME);
            if (pk.exists()) {
                return pk.getAbsolutePath();
            }
            return "";
        }
    }
}