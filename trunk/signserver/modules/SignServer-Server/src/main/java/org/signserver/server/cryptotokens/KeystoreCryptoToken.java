/*************************************************************************
 *                                                                       *
 *  SignServer: The OpenSource Automated Signing Server                  *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.signserver.server.cryptotokens;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.ejbca.util.keystore.KeyTools;
import org.signserver.common.*;

/**
 * Class that uses a PKCS12 or JKS file on the file system for signing.
 *
 * If no DEFAULTKEY property is specified the first key found will be used.
 *
 * Loads on activation and releases the keys from memory when deactivating
 *
 * Available properties are:
 * KEYSTOREPATH : The full path to the key store to load. (required)
 * KEYSTOREPASSWORD : The password that locks the key store.
 * KEYSTORETYPE : PKCS12 or JKS. (required)
 * DEFAULTKEY : Alias of keystore entry to use. (optional)
 * NEXTCERTSIGNKEY : Can be used to hold the alias of the next key. (optional)
 *
 * @author Philip Vendil, Markus Kilas
 * @version $Id$
 */
public class KeystoreCryptoToken implements ICryptoToken, ICryptoTokenV2 {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(KeystoreCryptoToken.class);

    public static final String KEYSTOREPATH = "KEYSTOREPATH";
    public static final String KEYSTOREPASSWORD = "KEYSTOREPASSWORD";
    public static final String KEYSTORETYPE = "KEYSTORETYPE";
    public static final String DEFAULTKEY = "DEFAULTKEY";
    public static final String NEXTKEY = "NEXTCERTSIGNKEY";

    public static final String TYPE_PKCS12 = "PKCS12";
    public static final String TYPE_JKS = "JKS";

    private static final String SUBJECT_DUMMY = "L=_SignServer_DUMMY_CERT_";

    private String keystorepath = null;
    private String keystorepassword = null;

    private KeyStore ks;
    private String keystoretype;
    private Properties properties;

    /** Mapping from alias or key purpose to KeyEntry. */
    private Map<Object, KeyEntry> entries;

    private char[] authenticationCode;


    @Override
    public void init(int workerId, Properties properties) throws CryptoTokenInitializationFailureException {
        this.properties = properties;
        keystorepath = properties.getProperty(KEYSTOREPATH);
        keystorepassword = properties.getProperty(KEYSTOREPASSWORD);
        keystoretype = properties.getProperty(KEYSTORETYPE);

        // check keystore type
        if (keystoretype == null) {
            throw new CryptoTokenInitializationFailureException("Missing KEYSTORETYPE property");
        }

        if (!TYPE_PKCS12.equals(keystoretype) &&
            !TYPE_JKS.equals(keystoretype)) {
            throw new CryptoTokenInitializationFailureException("KEYSTORETYPE should be either PKCS12 or JKS");
        }

        // check keystore file
        if (keystorepath == null) {
            throw new CryptoTokenInitializationFailureException("Missing KEYSTOREPATH property");
        } else {
            final File keystoreFile = new File(keystorepath);

            if (!keystoreFile.isFile()) {
                throw new CryptoTokenInitializationFailureException("File not found: " + keystorepath);
            }
        }
    }

    @Override
    public int getCryptoTokenStatus() {
        if (entries != null && entries.get(PURPOSE_SIGN) != null
                && (!properties.containsKey(NEXTKEY)
                    || entries.get(PURPOSE_NEXTKEY) != null)) {
            return WorkerStatus.STATUS_ACTIVE;
        }

        return WorkerStatus.STATUS_OFFLINE;
    }

    @Override
    public void activate(String authenticationcode)
            throws CryptoTokenAuthenticationFailureException,
            CryptoTokenOfflineException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Keystore type is " + keystoretype +
                    " and path is " + keystorepath);
        }

        try {
            this.ks = getKeystore(keystoretype, keystorepath,
                    authenticationcode.toCharArray());

            this.authenticationCode = authenticationcode.toCharArray();

            entries = new HashMap<Object, KeyEntry>();

            Enumeration<String> e = ks.aliases();
            while (e.hasMoreElements()) {
                final String alias = e.nextElement();
                if (ks.isKeyEntry(alias)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Alias " + alias + " is KeyEntry.");
                    }
                    final Key key = ks.getKey(alias, authenticationcode.toCharArray());
                    if (key instanceof PrivateKey) {
                        final Certificate[] chain = KeyTools.getCertChain(ks,
                                alias);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Loaded certificate chain with length "
                                    + chain.length + " from keystore.");
                        }

                        final KeyEntry entry = new KeyEntry((PrivateKey) key, 
                                chain[0], Arrays.asList(chain));

                        entries.put(alias, entry);
                    } else {
                        LOG.error("Not a private key for alias " + alias);
                    }
                }
            }

            // Use the first entry as default key if none specified
            if (properties.getProperty(DEFAULTKEY) == null) {
                e = ks.aliases();

                while (e.hasMoreElements()) {
                    final String alias = e.nextElement();
                    if (ks.isKeyEntry(alias)) {
                        if (ks.getKey(alias,
                                authenticationcode.toCharArray()) != null) {
                            LOG.debug("Aliases " + alias + " is KeyEntry.");
                                properties.setProperty(DEFAULTKEY, alias);
                            break;
                        }
                    }
                }
            }

            final String defaultKey = properties.getProperty(DEFAULTKEY);
            if (defaultKey != null) {
                final KeyEntry entry = entries.get(defaultKey);
                if (entry != null) {
                    entries.put(ICryptoToken.PURPOSE_SIGN, entry);
                    entries.put(ICryptoToken.PURPOSE_DECRYPT, entry);
                } else {
                    LOG.error("Not a private key for alias " + defaultKey);
                }
            }

            final String nextKey = properties.getProperty(NEXTKEY);
            if (nextKey != null) {
                final KeyEntry entry = entries.get(nextKey);
                if (entry != null) {
                    entries.put(ICryptoToken.PURPOSE_NEXTKEY, entry);
                } else {
                    LOG.error("Not a private key for alias " + defaultKey);
                }
            }

        } catch (KeyStoreException e1) {
            LOG.error("Error :", e1);
            throw new CryptoTokenAuthenticationFailureException("KeyStoreException " + e1.getMessage());
        } catch (FileNotFoundException e) {
            LOG.error("Error :", e);
            throw new CryptoTokenAuthenticationFailureException("Keystore file not found : " + e.getMessage());
        } catch (NoSuchProviderException e1) {
            LOG.error("Error :", e1);
            throw new CryptoTokenAuthenticationFailureException("NoSuchProviderException " + e1.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error :", e);
            throw new CryptoTokenAuthenticationFailureException("NoSuchAlgorithmException " + e.getMessage());
        } catch (CertificateException e) {
            LOG.error("Error :", e);
            throw new CryptoTokenAuthenticationFailureException("CertificateException " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Error :", e);
            throw new CryptoTokenAuthenticationFailureException("IOException " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            LOG.error("Error :", e);
            throw new CryptoTokenAuthenticationFailureException("UnrecoverableKeyException " + e.getMessage());
        }
    }

    @Override
    public boolean deactivate() {
        entries = null;
        ks = null;
        if (authenticationCode != null) {
            Arrays.fill(authenticationCode, '\0');
        }
        this.authenticationCode = null;
        return true;
    }

    @Override
    public PrivateKey getPrivateKey(int purpose)
            throws CryptoTokenOfflineException {

        if (entries == null) {
            if (keystorepassword != null) {
                try {
                    activate(keystorepassword);
                } catch (CryptoTokenAuthenticationFailureException e) {
                    throw new CryptoTokenOfflineException("Error trying to autoactivating the keystore, wrong password set? " + e.getMessage());
                }
            } else {
                throw new CryptoTokenOfflineException("Signtoken isn't active.");
            }
        }
        KeyEntry entry = entries.get(purpose);
        // If key for 'purpose' not available and no nextKey defined, try with
        // default
        if ((entry == null || entry.getPrivateKey() == null)
                && !properties.containsKey(NEXTKEY)) {
            entry = entries.get(PURPOSE_SIGN);
        }
        if (entry == null || entry.getPrivateKey() == null) {
            throw new CryptoTokenOfflineException(
                    "No key available for purpose: " + purpose);
        }
        return entry.getPrivateKey();
    }

    @Override
    public PublicKey getPublicKey(int purpose) throws
            CryptoTokenOfflineException {
        final Certificate cert = getKeyEntry(purpose).getCertificate();
        return cert.getPublicKey();
    }

    @Override
    public String getProvider(int providerUsage) {
        return "BC";
    }

    private KeyEntry getKeyEntry(final Object purpose) throws CryptoTokenOfflineException {
        if (entries == null) {
            if (keystorepassword != null) {
                try {
                    activate(keystorepassword);
                } catch (CryptoTokenAuthenticationFailureException e) {
                    throw new CryptoTokenOfflineException(
                        "Error trying to autoactivating the keystore, wrong password set? "
                        + e.getMessage());
                }
            } else {
                throw new CryptoTokenOfflineException("Signtoken isn't active.");
            }
        }
        KeyEntry entry = entries.get(purpose);
        // If key for 'purpose' not available and no nextKey defined, try with
        // default
        if ((entry == null || entry.getCertificate() == null)
                && !properties.containsKey(NEXTKEY)) {
            entry = entries.get(PURPOSE_SIGN);
        }
        if (entry == null || entry.getCertificate() == null) {
            throw new CryptoTokenOfflineException(
                    "No key available for purpose: " + purpose);
        }
        return entry;
    }

    @Override
    public Certificate getCertificate(int purpose) throws CryptoTokenOfflineException {
        return getCertificateFromEntries(purpose);
    }

    @Override
    public List<Certificate> getCertificateChain(int purpose) throws CryptoTokenOfflineException {
        return getCertificateChainFromEntries(purpose);
    }

    private Certificate getCertificateFromEntries(Object purposeOrAlias) throws CryptoTokenOfflineException {
        try {
            final KeyEntry entry = getKeyEntry(purposeOrAlias);
            Certificate result = entry.getCertificate();

            // Do not return the dummy certificate
            if (result instanceof X509Certificate) {
                if (((X509Certificate) result).getSubjectDN().getName()
                        .contains(SUBJECT_DUMMY)) {
                    result = null;
                }
            }
            return result;
        } catch (CryptoTokenOfflineException ex) {
            return null;
        }
    }

    private List<Certificate> getCertificateChainFromEntries(Object purposeOrAlias) throws CryptoTokenOfflineException {
        final KeyEntry entry = getKeyEntry(purposeOrAlias);
        List<Certificate> result = entry.getCertificateChain();
        // Do not return the dummy certificate
        if (result.size() == 1) {
            if (result.get(0) instanceof X509Certificate) {
                if (((X509Certificate) result.get(0)).getSubjectDN().getName()
                        .contains(SUBJECT_DUMMY)) {
                    result = null;
                }
            }
        }
        return result;
    }

    @Override
    public Certificate getCertificate(String alias) throws CryptoTokenOfflineException {
        return getCertificateFromEntries(alias);
    }

    @Override
    public List<Certificate> getCertificateChain(String alias) throws CryptoTokenOfflineException {
        return getCertificateChainFromEntries(alias);
    }

    @Override
    public ICertReqData genCertificateRequest(ISignerCertReqInfo info,
            final boolean explicitEccParameters, final boolean defaultKey)
            throws CryptoTokenOfflineException {
        final int purpose = defaultKey ? PURPOSE_SIGN : PURPOSE_NEXTKEY;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Purpose: " + purpose);
        }
        try {
            return CryptoTokenHelper.genCertificateRequest(info, getPrivateKey(purpose), getProvider(ICryptoToken.PROVIDERUSAGE_SIGN), getPublicKey(purpose), explicitEccParameters);
        } catch (IllegalArgumentException ex) {
            throw new CryptoTokenOfflineException(ex.getMessage(), ex);
        }
    }

    /**
     * Method not supported
     */
    @Override
    public boolean destroyKey(int purpose) {
        return false;
    }

    @Override
    public Collection<KeyTestResult> testKey(final String alias,
            final char[] authCode) throws CryptoTokenOfflineException,
            KeyStoreException {
        return CryptoTokenHelper.testKey(getKeyStore(), alias, authCode, "BC");
    }

    @Override
    public void generateKey(String keyAlgorithm, String keySpec, String alias, char[] authCode) throws CryptoTokenOfflineException, IllegalArgumentException {
        if (keySpec == null) {
            throw new IllegalArgumentException("Missing keyspec parameter");
        }
        if (alias == null) {
            throw new IllegalArgumentException("Missing alias parameter");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("keyAlgorithm: " + keyAlgorithm + ", keySpec: " + keySpec
                    + ", alias: " + alias);
        }
        try {

            final KeyStore keystore = getKeyStore();

            final KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyAlgorithm, "BC");

            if ("ECDSA".equals(keyAlgorithm)) {
                kpg.initialize(ECNamedCurveTable.getParameterSpec(keySpec));
            } else {
                kpg.initialize(Integer.valueOf(keySpec));
            }

            final String sigAlgName = "SHA1With" + keyAlgorithm;

            LOG.debug("generating...");
            final KeyPair keyPair = kpg.generateKeyPair();
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = getSelfCertificate("CN=" + alias + ", " + SUBJECT_DUMMY
                + ", C=SE",
                                  (long)30*24*60*60*365, sigAlgName, keyPair);
            LOG.debug("Creating certificate with entry "+alias+'.');

            keystore.setKeyEntry(alias, keyPair.getPrivate(), authCode, chain);
            keystore.store(new FileOutputStream(new File(keystorepath)), 
                    authenticationCode);

        } catch (Exception ex) {
            LOG.error(ex, ex);
            throw new CryptoTokenOfflineException(ex);
        }
    }

    @Override
    public KeyStore getKeyStore() throws UnsupportedOperationException,
            CryptoTokenOfflineException, KeyStoreException {
        if (ks == null) {
            throw new CryptoTokenOfflineException("Not activated");
        }
        return ks;
    }

    private X509Certificate getSelfCertificate (String myname,
                                                long validity,
                                                String sigAlg,
                                                KeyPair keyPair) throws Exception {
        final long currentTime = new Date().getTime();
        final Date firstDate = new Date(currentTime-24*60*60*1000);
        final Date lastDate = new Date(currentTime + validity * 1000);

        // Add all mandatory attributes
        LOG.debug("keystore signing algorithm " + sigAlg);

        final PublicKey publicKey = keyPair.getPublic();
        if (publicKey == null) {
            throw new Exception("Public key is null");
        }

        X509v3CertificateBuilder cg = new JcaX509v3CertificateBuilder(new X500Principal(myname), BigInteger.valueOf(firstDate.getTime()), firstDate, lastDate, new X500Principal(myname), publicKey);
        final JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(sigAlg);
        contentSignerBuilder.setProvider(getProvider(PROVIDERUSAGE_SIGN));

        final ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getPrivate());

        return new JcaX509CertificateConverter().getCertificate(cg.build(contentSigner));
    }

    private static KeyStore getKeystore(final String type, final String path,
            final char[] authCode) throws
            KeyStoreException, CertificateException, NoSuchProviderException,
            NoSuchAlgorithmException, FileNotFoundException, IOException {
        final KeyStore result;
        if (TYPE_PKCS12.equalsIgnoreCase(type)) {
            result = KeyStore.getInstance("PKCS12", "BC");
        } else {
            result = KeyStore.getInstance("JKS");
        }

        if (path == null) {
            throw new FileNotFoundException("Missing property "
                    + KeystoreCryptoToken.KEYSTOREPATH + ".");
        }
        InputStream in = null;
        try {
            in = new FileInputStream(path);
            result.load(in, authCode);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    LOG.error("Error closing file", ex);
                }
            }
        }
        return result;
    }

    @Override
    public boolean removeKey(String alias) throws CryptoTokenOfflineException, KeyStoreException, SignServerException {
        final KeyStore keyStore = getKeyStore();
        boolean result = CryptoTokenHelper.removeKey(keyStore, alias);
        if (result) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(new File(keystorepath));
                keyStore.store(out, authenticationCode);
            } catch (IOException ex) {
                LOG.error("Unable to persist new keystore after key removal: " + ex.getMessage(), ex);
                throw new SignServerException("Unable to persist key removal");
            } catch (NoSuchAlgorithmException ex) {
                LOG.error("Unable to persist new keystore after key removal: " + ex.getMessage(), ex);
                throw new SignServerException("Unable to persist key removal");
            } catch (CertificateException ex) {
                LOG.error("Unable to persist new keystore after key removal: " + ex.getMessage(), ex);
                throw new SignServerException("Unable to persist key removal");
            } finally {
                IOUtils.closeQuietly(out);
            }
        }
        return result;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) throws CryptoTokenOfflineException {
        return getKeyEntry(alias).getPrivateKey();
    }

    @Override
    public PublicKey getPublicKey(String alias) throws CryptoTokenOfflineException {
        return getKeyEntry(alias).getCertificate().getPublicKey();
    }

    @Override
    public ICertReqData genCertificateRequest(ISignerCertReqInfo info, boolean explicitEccParameters, String keyAlias) throws CryptoTokenOfflineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Alias: " + keyAlias);
        }
        try {
            return CryptoTokenHelper.genCertificateRequest(info, getPrivateKey(keyAlias), getProvider(ICryptoToken.PROVIDERUSAGE_SIGN), getPublicKey(keyAlias), explicitEccParameters);
        } catch (IllegalArgumentException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Certificate request error", ex);
            }
            throw new CryptoTokenOfflineException(ex.getMessage(), ex);
        }
    }

    private static class KeyEntry {
        private final PrivateKey privateKey;
        private final Certificate certificate;
        private final List<Certificate> certificateChain;

        public KeyEntry(final PrivateKey privateKey,
                final Certificate certificate,
                final List<Certificate> certificateChain) {
            this.privateKey = privateKey;
            this.certificate = certificate;
            this.certificateChain = certificateChain;
        }

        public Certificate getCertificate() {
            return certificate;
        }

        public List<Certificate> getCertificateChain() {
            return certificateChain;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }
    }
}