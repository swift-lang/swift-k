
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/**
 * Copyright (c) 2003, National Research Council of Canada
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice(s) and this licence appear in all copies of the Software or 
 * substantial portions of the Software, and that both the above copyright notice(s) and this 
 * license appear in supporting documentation.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE LIABLE 
 * FOR ANY CLAIM, OR ANY DIRECT, INDIRECT, SPECIAL OR CONSEQUENTIAL 
 * DAMAGES, OR ANY DAMAGES WHATSOEVER (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWSOEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OF THE SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall NOT be used in 
 * advertising or otherwise to promote the sale, use or other dealings in this Software 
 * without specific prior written authorization.  Title to copyright in this software and any 
 * associated documentation will at all times remain with copyright holders.
 */


package org.globus.cog.security.cert.request;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.bouncycastle.asn1.DERConstructedSet;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.cog.security.cert.request.OpenSSLKey;
import org.globus.cog.security.cert.request.BouncyCastleOpenSSLKey;
import org.globus.util.PEMUtils;
import org.globus.util.Util;

import java.util.StringTokenizer;

/**
 * GridCertRequest Command Line Client
 * 
 * Things remaining to be done:
 * Support for host or service certificate request. Perhaps this should not be
 * part of this tool since the COG kit is mostly a client library.
 * Prompt user for each component of the DN. (Interactive mode)
 * @author Jean-Claude Cote
 */
public final class GridCertRequest {

    public static final String usage =
        "\n"
            + "\ngrid-cert-request [-help] [ options ...]"
            + "\n"
            + "\n  Example Usage:"
            + "\n"
            + "\n    Creating a user certifcate:"
            + "\n      grid-cert-request"
            + "\n"
            + "\n    Creating a host or gatekeeper certifcate:"
            + "\n      grid-cert-request -host [my.host.fqdn]"
            + "\n"
            + "\n    Creating a LDAP server certificate:"
            + "\n      grid-cert-request -service ldap -host [my.host.fqdn]"
            + "\n"
            + "\n  Options:"
            + "\n"
            + "\n    -version           : Display version"
            + "\n    -?, -h, -help,     : Display usage"
            + "\n    -usage"
            + "\n    -cn <name>,        : Common name of the user"
            + "\n    -commonname <name>"
            + "\n    -service <service> : Create certificate for a service. Requires"
            + "\n                         the -host option and implies that the generated"
            + "\n                         key will not be password protected (ie implies -nopw). <<Not implemented yet>>"
            + "\n    -host <FQDN>       : Create certificate for a host named <FQDN> <<Not implemented yet>>"
            + "\n    -dir <dir_name>    : Changes the directory the private key and certificate"
            + "\n                         request will be placed in. By default user"
            + "\n                         certificates are placed in /home/user/.globus, host"
            + "\n                         certificates are placed in /etc/grid-security and"
            + "\n                         service certificates are place in"
            + "\n                         /etc/grid-security/<service>."
            + "\n    -prefix <prefix>   : Causes the generated files to be named"
            + "\n                         <prefix>cert.pem, <prefix>key.pem and"
            + "\n                         <prefix>cert_request.pem"
            + "\n    -nopw,             : Create certificate without a passwd"
            + "\n    -nodes,"
            + "\n    -nopassphrase,"
            + "\n    -verbose           : Don't clear the screen <<Not used>>"
            + "\n    -int[eractive]     : Prompt user for each component of the DN <<Not implemented yet>>"
            + "\n    -force             : Overwrites preexisting certifictes";

    private static String message =
        "\nA certificate request and private key will be created."
            + "\nYou will be asked to enter a PEM pass phrase."
            + "\nThis pass phrase is akin to your account password,"
            + "\nand is used to protect your key file."
            + "\nIf you forget your pass phrase, you will need to"
            + "\nobtain a new certificate.\n";

    private static String cn = null;
    private static String service = null;
    private static String gatekeeper = null;
    private static String hostName = null;
    private static String certDir = null;
    private static String certFile = null;
    private static String keyFile = null;
    private static String reqFile = null;
    private static boolean noPswd = false;
    private static boolean interactive = false;
    private static boolean force = false;
    private static boolean resubmit = false;
    private static String version = "1.0";
    private static boolean verbose = false;
    private static String prefix = "user";

    public static void main(String[] args) {

        boolean bOk = parseCmdLine(args);

        String userCertFile = "";
        String userKeyFile = "";
        String userCertReqFile = "";
        if (bOk) {

            // Get default location of cert.
            CoGProperties props = CoGProperties.getDefault();

            // If no alternate directory specified.
            if (certDir == null) {
                userCertFile = props.getUserCertFile();
                userKeyFile = props.getUserKeyFile();
                // Get root dir of default cert location.
                int pos = userKeyFile.lastIndexOf(File.separator);
                certDir = userKeyFile.substring(0, pos + 1);
            } else {
                // If alternate directory specified set cert locations.
                if (certDir.endsWith(File.separator) == false) {
                    certDir += File.separator;
                }
                userCertFile = certDir + prefix + "cert.pem";
                userKeyFile = certDir + prefix + "key.pem";
            }

            // Cert request file name.
            userCertReqFile =
                userCertFile.substring(0, userCertFile.length() - 4)
                    + "_request.pem";
        }

        File fDir = null;
        fDir = new File(certDir);
        if (bOk) {
            // Create dir if does not exists.
            if (!fDir.exists()){
                fDir.mkdir();
            }
        
            // Make sure directory exists.
            if (!fDir.exists() || !fDir.isDirectory()) {
                System.out.println("The directory " + certDir + " does not exists.");
                bOk = false;
            }
        }
        
        // Make sure we can write to it.
        if (bOk) {
            if (!fDir.canWrite()) {
                System.out.println("Can't write to " + certDir);
                bOk = false;
            }
        }

        // Check not to overwrite any of these files.
        if (bOk) {
            if (force == false) {
                boolean bFileExists = false;
                File f = new File(userKeyFile);
                if (f.exists()) {
                    System.out.println(userKeyFile + " exists");
                    bFileExists = true;
                }
                f = new File(userCertFile);
                if (f.exists()) {
                    System.out.println(userCertFile + " exists");
                    bFileExists = true;
                }
                f = new File(userCertReqFile);
                if (f.exists()) {
                    System.out.println(userCertReqFile + " exists");
                    bFileExists = true;
                }

                if (bFileExists) {
                    System.out.println(
                        "If you wish to overwrite, run the script again with -force.");
                    bOk = false;
                }
            }
        }

        String password = "";
        if (bOk && !noPswd) {
            // Get password from user.                
            bOk = false;
            int attempts = 0;

            System.out.println(message);

            while (bOk == false && attempts < 3) {
                password = Util.getInput("Enter PEM pass phrase: ");
                String password2 =
                    Util.getInput("Verify password Enter PEM pass phrase: ");
                if (password.compareTo(password2) != 0) {
                    System.out.println("Verify failure");
                } else {
                    if (password.length() < 4) {
                        System.out.println(
                            "phrase is too short, needs to be at least 4 chars");
                    } else {
                        bOk = true;
                    }
                }
                attempts++;
            }
        }

        // Generate cert request.            
        if (bOk) {

            try {
                System.out.println("writing new private key to " + userKeyFile);
                genCertificateRequest(
                    cn,
                    "ca@gridcanada.ca",
                    password,
                    userKeyFile,
                    userCertFile,
                    userCertReqFile);
            } catch (Exception e) {
                System.out.println("error: " + e);
                e.printStackTrace();
            }
        }
    }

    protected static boolean parseCmdLine(String[] args) {
        boolean bOk = true;
        if (args.length == 0) {
            System.out.println(usage);
            bOk = false;
        } else {
            for (int i = 0; i < args.length && bOk; i++) {
                if (args[i].equalsIgnoreCase("-version")) {
                    System.out.println(version);
                } else if (
                    args[i].equalsIgnoreCase("-help")
                        || args[i].equalsIgnoreCase("-h")
                        || args[i].equalsIgnoreCase("-?")) {
                    System.out.println(usage);
                    bOk = false;
                } else if (
                    args[i].equalsIgnoreCase("-cn")
                        || args[i].equalsIgnoreCase("-commonname")) {
                    // common name specified
                    cn = args[++i];
                }
                /*
                else if (args[i].equalsIgnoreCase("-service")) {
                    // user certificate directory specified
                    service = args[++i];
                }*/
                /*
                else if (args[i].equalsIgnoreCase("-host")) {
                    // host name specified
                    service = "host";
                    hostName = args[++i];
                }
                */
                else if (args[i].equalsIgnoreCase("-dir")) {
                    // user certificate directory specified
                    certDir = args[++i];
                } else if (args[i].equalsIgnoreCase("-prefix")) {
                    prefix = args[++i];
                } else if (
                    args[i].equalsIgnoreCase("-nopw")
                        || args[i].equalsIgnoreCase("-nodes")
                        || args[i].equalsIgnoreCase("-nopassphrase")) {
                    // no password
                    noPswd = true;
                } else if (args[i].equalsIgnoreCase("-verbose")) {
                    verbose = true;
                }
                /*
                else if ((args[i].equalsIgnoreCase("-int")) || (args[i].equalsIgnoreCase("-interactive"))) {
                    // interactive mode
                    interactive = true;
                }
                */
                else if (args[i].equalsIgnoreCase("-force")) {
                    // overwrite existing credentials
                    force = true;
                } else {
                    System.out.println(
                        "Error: argument #"
                            + i
                            + "("
                            + args[i]
                            + ") : unknown");
                }
            }
        }
        return bOk;
    }

    /**
     * Generates a encrypted private key and certificate request.
     */
    static public void genCertificateRequest(
        String dname,
        String emailAddressOfCA,
        String password,
        String privKeyLoc,
        String certLoc,
        String certReqLoc)
        throws Exception {

        String sigAlgName = "MD5WithRSA";
        String keyAlgName = "RSA";

        CertUtil.init();

        // Generate a new key pair.
        KeyPairGenerator keygen = KeyPairGenerator.getInstance(keyAlgName);
        KeyPair keyPair = keygen.genKeyPair();
        PrivateKey privKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();

        // Generate the certificate request.        
        X509Name name = new X509Name(dname);
        DERConstructedSet derSet = new DERConstructedSet();
        PKCS10CertificationRequest request =
            new PKCS10CertificationRequest(
                sigAlgName,
                name,
                pubKey,
                derSet,
                privKey);

        // Save the certificate request to a .pem file.
        byte[] data = request.getEncoded();
        PrintStream ps = new PrintStream(new FileOutputStream(certReqLoc));

        // build / delimited name.        
        String certSubject = "";
        StringTokenizer tokens = new StringTokenizer(dname, ",");
        while(tokens.hasMoreTokens()){
            certSubject = certSubject + "/" + tokens.nextToken();
        }

        ps.print( "\n\n"
            + "Please mail the following certificate request to " + emailAddressOfCA + "\n"
            + "\n"
            + "==================================================================\n"
            + "\n"
            + "Certificate Subject:\n"
            + "\n"
            + certSubject
            + "\n"
            + "\n"
            + "The above string is known as your user certificate subject, and it \n"
            + "uniquely identifies this user.\n"
            + "\n"
            + "To install this user certificate, please save this e-mail message\n"
            + "into the following file.\n"
            + "\n"
            + "\n"
            + certLoc
            + "\n"
            + "\n"
            + "\n"
            + "      You need not edit this message in any way. Simply \n"
            + "      save this e-mail message to the file.\n"
            + "\n"
            + "\n"
            + "If you have any questions about the certificate contact\n"
            + "the Certificate Authority at " + emailAddressOfCA + "\n"
            + "\n");
        ps.print(toPEM(data));
        ps.close();

        // Save private key to a .pem file.
        OpenSSLKey key = new BouncyCastleOpenSSLKey(privKey);
        if (password.length() != 0) {
            key.encrypt(password);
        }
        key.writeTo(new File(privKeyLoc).getAbsolutePath());
        // set read only permissions
        Util.setFilePermissions(privKeyLoc, 600);

        // Create an empty cert file.
        File f = new File(certLoc);
        f.createNewFile();
    }

    /**
     * Converts to PEM encoding.
     */
    static public String toPEM(byte[] data) {
        byte[] enc_data = Base64.encode(data);
        String header = "-----BEGIN CERTIFICATE REQUEST-----";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PEMUtils.writeBase64(
                out,
                header,
                enc_data,
                "-----END CERTIFICATE REQUEST-----");
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
        return new String(out.toByteArray());
    }

}
