
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.security.certrequest;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.globus.common.CoGProperties;
import org.globus.gsi.CertUtil;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;
import org.globus.util.PEMUtils;
import org.globus.util.Util;

import COM.claymoresystems.cert.CertRequest;
import COM.claymoresystems.cert.X509Name;

import org.bouncycastle.asn1.DERConstructedSet;
//import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import org.apache.log4j.Logger;

/**
 * CertRequest Command Line Client
 *
 * Things remaining to be done:
 * Support for host or service certificate request. Perhaps this should not be
 * part of this tool since the COG kit is mostly a client library.
 * @author Jean-Claude Cote
 * @author Vladimir Silva
 */
public final class GridCertRequest {
    private static Logger logger =
        Logger.getLogger(GridCertRequest.class.getName());

    public static final String usage =
            "\ngrid-cert-request [-help] [ options ...]"
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
            + "\n                         key will not be password protected (ie implies -nopw)."
            + "\n    -host <FQDN>       : Create certificate for a host named <FQDN>"
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
//            + "\n    -int[eractive]     : Prompt user for each component of the DN <<Not implemented yet>>"
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

    /**
     * Main
     * @param args main prog args
     */
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
        else {
          System.exit(-1);
        }

        File fDir = null;
        fDir = new File(certDir);

            // Create dir if does not exists.
            if (!fDir.exists()){
                fDir.mkdir();
            }

            // Make sure directory exists.
            if (!fDir.exists() || !fDir.isDirectory()) {
                System.out.println("The directory " + certDir + " does not exists.");
                bOk = false;
            }

        // Make sure we can write to it.
            if (!fDir.canWrite()) {
                System.out.println("Can't write to " + certDir);
                bOk = false;
            }

        // Check not to overwrite any of these files.
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
                    System.exit(-1);
                }
            }

        String password = "";

        // host cert?
        if ( service != null && service.length() != 0 ) {
          noPswd = true;
          cn = "host/" + hostName;
        }

        if (!noPswd) {
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
            try {
                System.out.println("Writing new request to " + userCertFile);
                System.out.println("Writing new private key to " + userKeyFile);

                makeCertificateRequest(cn, password, userKeyFile, userCertFile, userCertReqFile);
            }
            catch (Exception e) {
                System.out.println("error: " + e);
                e.printStackTrace();
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
                else if (args[i].equalsIgnoreCase("-service")) {
                    // user certificate directory specified
                    service = args[++i];
                }

                else if (args[i].equalsIgnoreCase("-host")) {
                    // host name specified
                    service =  prefix = "host";
                    hostName = args[++i];
                }
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
     * @param dname common name
     * @param password CSR password
     * @param privKeyLoc Full path to priv key
     * @param certLoc Full path to user cert (Will be empty)
     * @param certReqLoc Full path to CSR
     * @throws java.lang.Exception
     */
    static public void makeCertificateRequest(
        String dname,
        String password,
        String privKeyLoc,
        String certLoc,
        String certReqLoc)
        throws Exception
    {
        logger.debug("genCertificateRequest cn=" + dname + " pwd=" + password + " Priv key=" + privKeyLoc + " Cert loc=" + certLoc + " CSR loc=" + certReqLoc);
        makeCertificateRequest(dname, password, new FileOutputStream(privKeyLoc), new FileOutputStream(certReqLoc));

        // set read only permissions
        Util.setFilePermissions(privKeyLoc, 600);

        // Create an empty cert file.
        File f = new File(certLoc);
        f.createNewFile();
    }

    /**
     * Certficate generation main function
     * @param dname Distinguished name (e.g John Doe)
     * @param password CSR password
     * @param outKey Out stream to the private key
     * @param outCertReq CSR out stream
     * @throws java.lang.Exception if error
     */
    static public void makeCertificateRequest(
        String dname,
        String password,
        OutputStream outKey,
        OutputStream outCertReq)
        throws Exception
    {
      String sigAlgName = "MD5WithRSA";
      String keyAlgName = "RSA";

      CertUtil.init();

      // load CA certs and grab the DN for the request (every thing but the CN part)
      TrustedCertificates tcerts = TrustedCertificates.getDefaultTrustedCertificates();
      String CADN = "";

      if(tcerts != null){
          X509Certificate[] caCerts = tcerts.getCertificates();
          if(caCerts == null){
              System.out.println("Warning: No trusted certificates found.");
          } else {
              CADN = (((caCerts.length == 0) || (caCerts[0] == null)) ? "" : (caCerts[0].getSubjectDN().toString()));
          }
      } else {
          System.out.println("Warning: No trusted certificates found.");
      }
      if(CADN == null){
          CADN = "";
      }

      // replace CA dn's CN elem with the user's CN
      if ( CADN.indexOf("CN") != -1 ) CADN =  CADN.substring(0, CADN.indexOf("CN") );
      if ( CADN.indexOf("cn") != -1 ) CADN =  CADN.substring(0, CADN.indexOf("cn") );

      dname = CADN + "CN=" + dname;


      logger.debug("Using DN=" + dname);
      KeyPair kp = null;
      byte[] data = null;

      // pure TLS can only create encrypted CSRs, OpenSSLKey gives an exception when encrypting
      if (password.length() != 0) {
        StringWriter sw = new StringWriter(); // will contain the priv key PEM
        BufferedWriter bw = new BufferedWriter(sw);

        kp = CertRequest.generateKey(keyAlgName, 1024, password, bw, true); // gen pub/priv keys
        data = CertRequest.makePKCS10Request(kp, makePTLSX509Name(dname));

        // save encrypted private key
        outKey.write(sw.toString().getBytes());

      }
      else {
        // OpenSSLKey gives an exception when encrypting, thus
        // use for unenc CSRs only...until fixed
        kp = KeyPairGenerator.getInstance(keyAlgName).generateKeyPair();
        data = new PKCS10CertificationRequest(
            sigAlgName,
            new org.bouncycastle.asn1.x509.X509Name(dname),
            kp.getPublic(),
            new DERConstructedSet(),
            kp.getPrivate()).getEncoded();

        // save unencrypted priv key
        OpenSSLKey key = new BouncyCastleOpenSSLKey(kp.getPrivate());
        key.writeTo(outKey);
      }

      // Save the certificate request to a .pem file.
      PrintStream ps = new PrintStream(outCertReq);
      ps.println(makeRequestInfoHeader(dname));
      ps.print(toPEM(data));
      ps.close();

    }

    /**
     * Converts to PEM encoding.
     * @param data cert bytes
     * @return pem enoded CSR
     */
    static private String toPEM(byte[] data) {
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
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
        return new String(out.toByteArray());
    }

    /* Cert RQ info header */
    static private String makeRequestInfoHeader(String subject) {
            StringBuffer buff = new StringBuffer("This is a Certificate Request file:\nIt should be mailed to to a CA for signature");
            buff.append("\n===============================================================");
            buff.append("\nCertificate Subject:\n\t");
            buff.append(subject);
            buff.append("\n\n");
            return buff.toString();
    }


    /*
     * makeCertDN: Creates an X509 Identity based on a string subject
     * e.g:  "C=US,O=Grid,OU=OGSA,OU=Foo,CN=John Doe"
     */
    private static X509Name makePTLSX509Name(String subject) throws Exception
    {
            Vector tdn = new Vector();
            Vector elems = new Vector();
            StringTokenizer st = new StringTokenizer(subject,",");

            for (; st.hasMoreTokens() ;) {
                    String s = st.nextToken(); // [key=value]
                    if (  s.indexOf("=") == -1 )
                            throw new Exception("Invalid subject format: " + subject + " Offending value: " + s);

                    String key = s.substring(0, s.indexOf("=")).trim();
                    String val = s.substring(s.indexOf("=") + 1).trim();

                    if ( val == null || val.equals(""))
                            throw new Exception("Invalid subject format: " + subject + " Offending value: " + s);

                    //logger.debug(key + "=" + val);
                    String[] temp = {key, val};
                    tdn.addElement(temp);
            }
            // COM.claymoresystems.cert (puretls.jar)
            return CertRequest.makeSimpleDN(tdn);
    }
}
