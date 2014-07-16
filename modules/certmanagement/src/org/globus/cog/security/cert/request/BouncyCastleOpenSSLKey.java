
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.security.cert.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;


import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERConstructedSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.globus.gsi.bc.BouncyCastleUtil;

/**
 * BouncyCastle-based implementation of OpenSSLKey.
 */
public class BouncyCastleOpenSSLKey extends OpenSSLKey {

    public BouncyCastleOpenSSLKey(InputStream is) 
	throws IOException, GeneralSecurityException {
	super(is);
    }

    public BouncyCastleOpenSSLKey(String file)
	throws IOException, GeneralSecurityException {
	super(file);
    }

    public BouncyCastleOpenSSLKey(PrivateKey key) {
	super(key);
    }

    public BouncyCastleOpenSSLKey(String algorithm, byte [] data) 
	throws GeneralSecurityException {
	super(algorithm, data);
    }
  
    protected PrivateKey getKey(String alg, byte [] data) 
	throws GeneralSecurityException {
	if (alg.equals("RSA")) {
	    try {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DERInputStream derin = new DERInputStream(bis);
		DERObject keyInfo = derin.readObject();
		
		DERObjectIdentifier rsa_oid = PKCSObjectIdentifiers.rsaEncryption;    	   
		AlgorithmIdentifier rsa = new AlgorithmIdentifier(rsa_oid);
		PrivateKeyInfo pkeyinfo = new PrivateKeyInfo(rsa, keyInfo);
		DERObject derkey = pkeyinfo.getDERObject();		
		
		byte[] keyData = BouncyCastleUtil.toByteArray(derkey);

		// The DER object needs to be mangled to 
		// create a proper ProvateKeyInfo object 
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyData);
		KeyFactory kfac = KeyFactory.getInstance("RSA");
		
		return kfac.generatePrivate(spec);
	    } catch (IOException e) {
		// that should never happen
		return null;
	    }
	    
	} else {
	    return null;
	}
    }
    
    protected byte[] getEncoded(PrivateKey key) {
	String format = key.getFormat();
	if (format != null && 
	    (format.equalsIgnoreCase("PKCS#8") ||
	     format.equalsIgnoreCase("PKCS8"))) {
	    try {
		DERObject keyInfo = BouncyCastleUtil.toDERObject(key.getEncoded());
		PrivateKeyInfo pkey = new PrivateKeyInfo((DERConstructedSequence)keyInfo);
		DERObject derKey = pkey.getPrivateKey();
		return BouncyCastleUtil.toByteArray(derKey);
	    } catch (IOException e) {
		// that should never happen
		e.printStackTrace();
		return null;
	    }
	} else if (format != null && 
		   format.equalsIgnoreCase("PKCS#1") &&
		   key instanceof RSAPrivateCrtKey) { // this condition will rarely be true
	    RSAPrivateCrtKey pKey = (RSAPrivateCrtKey)key;
	    RSAPrivateKeyStructure st = 
		new RSAPrivateKeyStructure(pKey.getModulus(),
					   pKey.getPublicExponent(),
					   pKey.getPrivateExponent(),
					   pKey.getPrimeP(),
					   pKey.getPrimeQ(),
					   pKey.getPrimeExponentP(),
					   pKey.getPrimeExponentQ(),
					   pKey.getCrtCoefficient());
	    DERObject ob = st.getDERObject();
	    
	    try {
		return BouncyCastleUtil.toByteArray(ob);
	    } catch (IOException e) {
		// that should never happen
		return null;
	    }
	} else {
	    return null;
	}
    }

    protected String getProvider() {
	return "BC";
    }
}
