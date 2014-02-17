
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.security.cert.request;

import java.util.StringTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.Writer;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Key;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.globus.util.Base64;
import org.globus.util.PEMUtils;
import org.globus.util.Util;

/**
 * Represents a OpenSSL-style PEM-formatted private key. It supports encryption
 * and decryption of the key. Currently, only RSA keys are supported, 
 * and only TripleDES encryption is supported.
 * This is based on work done by Ming Yung at DSTC.
 */
public abstract class OpenSSLKey {

    public static final String HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    
    private String keyAlg       = null;
    private boolean isEncrypted = false;
    private byte[] encodedKey   = null;
    private PrivateKey intKey   = null;
    private IvParameterSpec iv  = null;
    private Cipher cipher       = null;
    private String encAlg       = null;
    private byte[] keyData      = null;
  
  /**
   * Reads a OpenSSL private key from the specified input stream.
   * The private key must be PEM encoded and can be encrypted.
   *
   * @param is input stream with OpenSSL key in PEM format.
   * @exception IOException if I/O problems.
   * @exception GeneralSecurityException if problems with the key
   */
  public OpenSSLKey(InputStream is) 
       throws IOException,  GeneralSecurityException {
	 InputStreamReader isr = new InputStreamReader(is);
	 readPEM(isr);
  }

  /**
   * Reads a OpenSSL private key from the specified file.
   * The private key must be PEM encoded and can be encrypted.
   *
   * @param file file containing the OpenSSL key in PEM format.
   * @exception IOException if I/O problems.
   * @exception GeneralSecurityException if problems with the key
   */
  public OpenSSLKey(String file)
       throws IOException, GeneralSecurityException {
	 FileReader f = null;
	 try {
	   f = new FileReader(file);
	   readPEM(f);
	 } finally {
	   if (f != null) f.close();
	 }
  }

  /**
   * Converts a RSAPrivateCrtKey into OpenSSL key.
   *
   * @param key private key - must be a RSAPrivateCrtKey
   */
  public OpenSSLKey(PrivateKey key) {
    intKey      = key;
    isEncrypted = false;
    keyData     = getEncoded(key);
  }

  /**
   * Initializes the OpenSSL key from raw byte array.
   *
   * @param algorithm  the algorithm of the key. Currently
   *                   only RSA algorithm is supported.
   * @param data       the DER encoded key data. If RSA
   *                   algorithm, the key must be in
   *                   PKCS#1 format.
   * @exception GeneralSecurityException if any security 
   *            problems.
   */
  public OpenSSLKey(String algorithm, byte [] data) 
       throws GeneralSecurityException {
	 keyData     = data;
	 isEncrypted = false;
	 intKey      = getKey(algorithm, data);
  }
  
  private void readPEM(Reader rd) 
       throws IOException, GeneralSecurityException {

	 BufferedReader in = new BufferedReader(rd);
	 
	 StringBuffer sb = new StringBuffer();
	 
	 String next = null;

	 while( (next = in.readLine()) != null) {
	   if (next.indexOf("PRIVATE KEY") != -1) {
	     keyAlg = getAlgorithm(next);
	     break;
	   }
	 }
	 
	 if (next == null) {
	   throw new InvalidKeyException("PRIVATE KEY section not found.");
	 }

	 if (keyAlg == null) {
	   throw new InvalidKeyException("Algorithm not supported.");
	 }
	 
	 next = in.readLine();
	 if (next.startsWith("Proc-Type: 4,ENCRYPTED")) {
	   isEncrypted = true;
	   checkEncrypted(in.readLine());
	   in.readLine();
	 } else {
	   sb.append(next);
	 }
	 
	 while ( (next = in.readLine()) != null ) {
	   if (next.startsWith("-----END")) break;
	   sb.append(next); 
	 }
	 
	 encodedKey = sb.toString().getBytes();
	 
	 if (!isEncrypted()) {
	   keyData = Base64.decode( encodedKey );
	   intKey  = getKey(keyAlg, keyData);
	 } else {
	   keyData = null;
	 }
  }
  
  /**
   * Check if the key was encrypted or not.
   *
   * @return true if the key is encrypted, false
   *         otherwise.
   */
  public boolean isEncrypted() {
    return isEncrypted;
  }

  /**
   * Decrypts the private key with given password.
   * Does nothing if the key is not encrypted.
   *
   * @param password password to decrypt the key with.
   * @exception GeneralSecurityException
   *            whenever an error occurs during decryption.
   * @exception InvalidKeyException
   *            whenever an error occurs during decryption.
   */
  public void decrypt(String password) 
       throws GeneralSecurityException, InvalidKeyException {
	 decrypt(password.getBytes());
  }
  
  /**
   * Decrypts the private key with given password.
   * Does nothing if the key is not encrypted.
   *
   * @param password password to decrypt the key with.
   * @exception GeneralSecurityException
   *            whenever an error occurs during decryption.
   * @exception InvalidKeyException
   *            whenever an error occurs during decryption.
   */
  public void decrypt(byte [] password) 
       throws GeneralSecurityException, InvalidKeyException {
	 if (!isEncrypted()) return;
	 
	 byte [] enc = Base64.decode( encodedKey );
	 
	 SecretKeySpec key = getSecretKey(password, iv.getIV());
	 
	 cipher = getCipher(encAlg);
	 cipher.init(Cipher.DECRYPT_MODE, key, iv);
	 enc = cipher.doFinal(enc);
	 
	 intKey = getKey(keyAlg, enc);
	 
	 keyData = enc;
	 
	 isEncrypted = false;
  }
    
  /**
   * Encrypts the private key with given password.
   * Does nothing if the key is encrypted already.
   *
   * @param password password to encrypt the key with.
   * @exception GeneralSecurityException
   *            whenever an error occurs during encryption.
   */
  public void encrypt(String password) 
       throws GeneralSecurityException {
	 encrypt(password.getBytes());
  }
  
  /**
   * Encrypts the private key with given password.
   * Does nothing if the key is encrypted already.
   *
   * @param password password to encrypt the key with.
   * @exception GeneralSecurityException
   *            whenever an error occurs during encryption.
   */
  public void encrypt(byte [] password) 
       throws GeneralSecurityException {
	 
     encAlg = "DESede";
    
	 if (isEncrypted()) return;
	 
	 if (iv == null) iv = generateIV();
	 
	 Key key = getSecretKey(password, iv.getIV());
	 
	 cipher = getCipher(encAlg);
	 cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	 
	 /* encrypt the raw PKCS11 */
	 
	 keyData = cipher.doFinal( getEncoded(intKey) );
	 
	 isEncrypted = true;
  }
  
  /**
   * Returns the JCE (RSAPrivateCrtKey) key.
   *
   * @return the private key, null if the key
   *         was not decrypted yet.
   */
  public PrivateKey getPrivateKey() {
    return intKey;
  }
  
  /**
   * Writes the private key to the specified output stream in PEM
   * format. If the key was encrypted it will be encoded as an encrypted
   * RSA key. If not, it will be encoded as a regular RSA key.
   *
   * @param output output stream to write the key to.
   * @exception IOException if I/O problems writing the key
   */
  public void writeTo(OutputStream output) 
       throws IOException {
	 if (keyData == null) throw new IOException("No key info");
	 output.write( toPEM().getBytes() );
  }
  
  /**
   * Writes the private key to the specified writer in PEM format. 
   * If the key was encrypted it will be encoded as an encrypted
   * RSA key. If not, it will be encoded as a regular RSA key.
   *
   * @param writer writer to output the key to.
   * @exception IOException if I/O problems writing the key
   */
  public void writeTo(Writer w) 
       throws IOException {
	 if (keyData == null) throw new IOException("No key info");
	 w.write( toPEM() );
  }
  
  /**
   * Writes the private key to the specified file in PEM format.
   * If the key was encrypted it will be encoded as an encrypted
   * RSA key. If not, it will be encoded as a regular RSA key.
   *
   * @param file file to write the key to.
   * @exception IOException if I/O problems writing the key
   */
  public void writeTo(String file) 
       throws IOException {
	 if (keyData == null) throw new IOException("No key info");
	 PrintWriter p = null;
	 try {
	   p = new PrintWriter(new FileOutputStream(file));
	   Util.setFilePermissions(file, 600);
	   p.write( toPEM() );
	 } finally {
	   if (p != null) p.close();
	 }
  }
 

    /**
     * Returns DER encoded byte array (PKCS#1). 
     */
    protected abstract byte[] getEncoded(PrivateKey key);
    
    /**
     * Returns PrivateKey object initialized from give byte array (in PKCS#1 format)
     */
    protected abstract PrivateKey getKey(String alg, byte [] data) 
	throws GeneralSecurityException;

    protected String getProvider() {
	return null;
    }
    
    private Cipher getCipher(String encAlg) 
	throws GeneralSecurityException {
	if (cipher == null) {
	    String provider = getProvider();
	    if (provider == null) {
		cipher = Cipher.getInstance(encAlg + "/CBC/PKCS5Padding");
	    } else {
		cipher = Cipher.getInstance(encAlg + "/CBC/PKCS5Padding",
					    provider);
	    }
	}
	return cipher;
    }

  private String getAlgorithm(String line) {
    if (line.indexOf("RSA") != -1) {
      return "RSA";
    } else if (line.indexOf("DSA") != -1) {
      return "DSA";
    } else {
      return null;
    }
  }

  private void checkEncrypted(String line) {
    String keyInfo = line.substring(10);
    StringTokenizer tknz = new StringTokenizer(keyInfo, ",", false);
    
    if (tknz.nextToken().equals("DES-EDE3-CBC")) {
      encAlg = "DESede";
    }
    
    iv = getIV(tknz.nextToken());
  }
  
  private IvParameterSpec getIV(String s) {
    byte[] ivBytes = new byte[8];
    for (int j=0; j<8; j++) {
      ivBytes[j] = (byte)Integer.parseInt(s.substring(j*2, j*2 + 2), 16);
    }        
    return new IvParameterSpec(ivBytes);
  }
  
  private IvParameterSpec generateIV() {
    byte [] b = new byte[8];
    SecureRandom sr = new SecureRandom(); //.getInstance("PRNG");
    sr.nextBytes(b);
    return new IvParameterSpec(b);
  }
  
  private SecretKeySpec getSecretKey(byte [] pwd, byte [] iv) 
       throws NoSuchAlgorithmException {
	 byte[] keyMat = new byte[24];
	 
	 MessageDigest md = MessageDigest.getInstance("MD5");
	 md.update(pwd);
	 md.update(iv);
	 byte[] data = md.digest();
	 System.arraycopy(data, 0, keyMat, 0, 16);
	 
	 md.update(data);
	 md.update(pwd);
	 md.update(iv);
	 data = md.digest();
	 System.arraycopy(data, 0, keyMat, 16, 8);
	 
	 return new SecretKeySpec(keyMat, encAlg);
  }
  
  // -------------------------------------------

    /**
     * Converts to PEM encoding.
     * Assumes keyData is initialized.
     */
    private String toPEM() {
    
	byte [] data = Base64.encode( keyData );
    
	String header = HEADER;

	if (isEncrypted()) {
	    StringBuffer buf = new StringBuffer(header);
	    buf.append(PEMUtils.lineSep);
	    buf.append("Proc-Type: 4,ENCRYPTED");
	    buf.append(PEMUtils.lineSep);
	    buf.append("DEK-Info: DES-EDE3-CBC,").append(PEMUtils.toHex(iv.getIV()));
	    buf.append(PEMUtils.lineSep);
	    header = buf.toString();
	}

	ByteArrayOutputStream out = new ByteArrayOutputStream();

	try {
	    PEMUtils.writeBase64(out,
				 header,
				 data,
				 "-----END RSA PRIVATE KEY-----");
	} catch (IOException e) {
	    throw new RuntimeException("Unexpected error: " + 
				       e.getMessage());
	}
	
	return new String(out.toByteArray());
  }
  
  
}
