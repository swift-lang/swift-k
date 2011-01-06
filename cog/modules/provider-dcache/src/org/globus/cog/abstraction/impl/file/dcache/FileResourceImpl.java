package org.globus.cog.abstraction.impl.file.dcache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.file.FileResourceException;
import org.globus.cog.abstraction.interfaces.ProgressMonitor;

/**
 * enables access to local file system through the file resource interface
 * Supports absolute and relative path names
 */
public class FileResourceImpl extends
        org.globus.cog.abstraction.impl.file.local.FileResourceImpl {
    public static final Logger logger = Logger
            .getLogger(FileResourceImpl.class);

    public static final String PROPERTIES = "provider-dcache.properties";
    public static final String DCCP = "dccp";
    public static final String OPTIONS = "dccp.options";
    private static Properties properties;

    protected synchronized static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.put(DCCP, "dccp");
            properties.put(OPTIONS, "-d 1");
            URL res = FileResourceImpl.class.getClassLoader().getResource(
                    PROPERTIES);
            if (res != null) {
                try {
                    properties.load(res.openStream());
                }
                catch (IOException e) {
                    logger.warn("Failed to load dcache provider properties",
                            e);
                }
            }
        }
        return properties;
    }

    private static String[] dccpCmd;

    protected synchronized static String[] getDCCPCmd() {
        if (dccpCmd == null) {
            StringTokenizer st = new StringTokenizer(getProperties()
                    .getProperty(OPTIONS));
            dccpCmd = new String[st.countTokens() + 1];
            dccpCmd[0] = getProperties().getProperty(DCCP);
            for (int i = 0; i < dccpCmd.length; i++) {
                dccpCmd[i + 1] = st.nextToken();
            }
        }
        return dccpCmd;
    }

    public FileResourceImpl() {
        this("dcache");
    }

    public FileResourceImpl(String name) {
        super(name);
    }

    public void getFile(String remoteFileName, String localFileName,
            ProgressMonitor progressMonitor) throws FileResourceException {
        try {
            String[] opts = getDCCPCmd();
            String[] cmd = new String[opts.length + 2];
            System.arraycopy(opts, 0, cmd, 0, opts.length);
            cmd[cmd.length - 2] = resolve(remoteFileName).getAbsolutePath();
            cmd[cmd.length - 1] = resolve(localFileName).getAbsolutePath();
            Process p = Runtime.getRuntime().exec(cmd);
            String stderr = consumeOutput(p);
            int exitcode = p.waitFor();
            if (exitcode != 0) {
                throw new FileResourceException("Failed to copy \""
                        + remoteFileName + "\" to \"" + localFileName
                        + "\". dccp failed with an exit code of " + exitcode
                        + ": " + stderr);
            }
        }
        catch (FileResourceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new FileResourceException(e);
        }
    }

    private String consumeOutput(Process p) throws IOException {
        StringBuffer errstr = new StringBuffer();
        Reader out = new InputStreamReader(p.getInputStream());
        BufferedReader err = new BufferedReader(new InputStreamReader(p
                .getErrorStream()));

        while (out.read() != -1) {
            // keep reading
        }

        String line = err.readLine();
        while (line != null) {
            errstr.append(line);
            errstr.append('\n');
            line = err.readLine();
        }
        return errstr.toString();
    }
}
