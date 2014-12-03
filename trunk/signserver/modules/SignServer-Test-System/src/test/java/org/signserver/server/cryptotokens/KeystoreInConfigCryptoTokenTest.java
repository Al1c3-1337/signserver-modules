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

import org.apache.log4j.Logger;
import org.signserver.common.GlobalConfiguration;
import org.signserver.common.SignServerUtil;
import org.signserver.ejb.interfaces.IGlobalConfigurationSession;
import org.signserver.ejb.interfaces.IWorkerSession;

/**
 * Test cases for the keystore crypto token storing the keystore in the config.
 * 
 * @author Marcus Lundblad
 * @version $Id$
 */
public class KeystoreInConfigCryptoTokenTest extends KeystoreCryptoTokenTestBase {
    private static final Logger LOG =
            Logger.getLogger(KeystoreInConfigCryptoTokenTest.class);
    
    private static final int WORKER_CMS = 30003;
    private static final int CRYPTO_TOKEN = 30103;
    
    private static final String SIGN_KEY_ALIAS = "p12signkey1234";
    private static final String TEST_KEY_ALIAS = "p12testkey1234";
    private static final String KEYSTORE_NAME = "p12testkeystore1234";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SignServerUtil.installBCProvider();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void setCMSSignerPropertiesSeparateToken(final int workerId, final int tokenId, boolean autoActivate) throws Exception {
        // Setup crypto token
        globalSession.setProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER" + tokenId + ".CLASSPATH", "org.signserver.server.signers.CryptoWorker");
        globalSession.setProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER" + tokenId + ".SIGNERTOKEN.CLASSPATH", KeystoreInConfigCryptoToken.class.getName());
        workerSession.setWorkerProperty(tokenId, "NAME", "TestCryptoTokenInConfig");

        if (autoActivate) {
            workerSession.setWorkerProperty(tokenId, "KEYSTOREPASSWORD", pin);
        } else {
            workerSession.removeWorkerProperty(workerId, "KEYSTOREPASSWORD");
        }

        // Setup worker
        globalSession.setProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER" + workerId + ".CLASSPATH", "org.signserver.module.cmssigner.CMSSigner");
        workerSession.setWorkerProperty(workerId, "NAME", "CMSSignerConfigToken");
        workerSession.setWorkerProperty(workerId, "AUTHTYPE", "NOAUTH");
        workerSession.setWorkerProperty(workerId, "CRYPTOTOKEN", "TestCryptoTokenInConfig");
    }
    
    /**
     * Tests setting up a CMS Signer, giving it a certificate and sign a file
     */
    public void testSigning() throws Exception {
        LOG.info("testSigning");
        final int workerId = WORKER_CMS;
        final int tokenId = CRYPTO_TOKEN;
        try {
            setCMSSignerPropertiesSeparateToken(workerId, tokenId, true);
            workerSession.reloadConfiguration(workerId);
            workerSession.reloadConfiguration(tokenId);
            workerSession.generateSignerKey(tokenId, "RSA", "1024", SIGN_KEY_ALIAS, pin.toCharArray());
            workerSession.reloadConfiguration(tokenId);

            cmsSigner(workerId);
        } finally {
            removeWorker(workerId);
        }
    }

}