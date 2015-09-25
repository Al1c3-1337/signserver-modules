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
package org.signserver.client.validationservice;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.util.encoders.Base64;
import org.cesecore.keys.util.KeyTools;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.signserver.cli.CommandLineInterface;
import org.signserver.client.cli.ClientCLI;
import org.signserver.client.cli.validationservice.ValidateCertificateCommand;
import org.signserver.common.GlobalConfiguration;
import org.signserver.common.ServiceLocator;
import org.signserver.common.SignServerUtil;
import org.signserver.ejb.interfaces.IGlobalConfigurationSession;
import org.signserver.ejb.interfaces.IWorkerSession;
import org.signserver.testutils.CLITestHelper;
import static org.signserver.testutils.CLITestHelper.assertPrinted;
import org.signserver.testutils.TestingSecurityManager;
import org.signserver.validationservice.server.ValidationTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.signserver.common.util.PathUtil;
import org.signserver.testutils.ModulesTestCase;

/**
 * Tests for the ValidateCertificateCommand.
 *
 * @version $Id$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValidationCLITest extends ModulesTestCase {

    private static String signserverhome;
    
    private static IGlobalConfigurationSession.IRemote gCSession;
    private static IWorkerSession.IRemote sSSession;
    
    private static String validCert1;
    private static String revokedCert1;
    private static String validcert1derpath;
    private static String validcert1path;
    private static String revokedcertpath;

    private CLITestHelper clientCLI = new CLITestHelper(ClientCLI.class);
    
    @Before
    public void setUp() throws Exception {
        SignServerUtil.installBCProvider();
        gCSession = ServiceLocator.getInstance().lookupRemote(
                IGlobalConfigurationSession.IRemote.class);
        sSSession = ServiceLocator.getInstance().lookupRemote(
                IWorkerSession.IRemote.class);
        signserverhome = PathUtil.getAppHome().getAbsolutePath();
    }

    @Test
    public void test00SetupDatabase() throws Exception {

        KeyPair validRootCA1Keys = KeyTools.genKeys("1024", "RSA");
        X509Certificate validRootCA1 = ValidationTestUtils.genCert(1000000, "CN=ValidRootCA1", "CN=ValidRootCA1", validRootCA1Keys.getPrivate(), validRootCA1Keys.getPublic(), new Date(0), new Date(System.currentTimeMillis() + 1000000), true);

        KeyPair validSubCA1Keys = KeyTools.genKeys("1024", "RSA");
        X509Certificate validSubCA1 = ValidationTestUtils.genCert(1000000, "CN=ValidSubCA1", "CN=ValidRootCA1", validRootCA1Keys.getPrivate(), validSubCA1Keys.getPublic(), new Date(0), new Date(System.currentTimeMillis() + 1000000), true);

        KeyPair validCert1Keys = KeyTools.genKeys("1024", "RSA");
        validCert1 = new String(Base64.encode(ValidationTestUtils.genCert(1000000, "CN=ValidCert1", "CN=ValidSubCA1", validSubCA1Keys.getPrivate(), validCert1Keys.getPublic(), new Date(0), new Date(System.currentTimeMillis() + 1000000), false, X509KeyUsage.digitalSignature + X509KeyUsage.keyEncipherment).getEncoded()));
        revokedCert1 = new String(Base64.encode(ValidationTestUtils.genCert(1000000, "CN=revokedCert1", "CN=ValidSubCA1", validSubCA1Keys.getPrivate(), validCert1Keys.getPublic(), new Date(0), new Date(System.currentTimeMillis() + 1000000), false).getEncoded()));
        ArrayList<X509Certificate> validChain1 = new ArrayList<X509Certificate>();
        // Add in the wrong order
        validChain1.add(validRootCA1);
        validChain1.add(validSubCA1);

        gCSession.setProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER16.CLASSPATH", "org.signserver.validationservice.server.ValidationServiceWorker");
        gCSession.setProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER16.SIGNERTOKEN.CLASSPATH", "org.signserver.server.cryptotokens.KeystoreCryptoToken");


        sSSession.setWorkerProperty(16, "AUTHTYPE", "NOAUTH");
        sSSession.setWorkerProperty(16, "NAME", "ValTest");
        sSSession.setWorkerProperty(16, "KEYSTORETYPE", "PKCS12");
        sSSession.setWorkerProperty(16, "KEYSTOREPATH",
                getSignServerHome() + File.separator + "res" + File.separator +
                "test" + File.separator + "dss10" + File.separator +
                        "dss10_signer1.p12");
        sSSession.setWorkerProperty(16, "KEYSTOREPASSWORD", "foo123");
        sSSession.setWorkerProperty(16, "DEFAULTKEY", "Signer 1");
        sSSession.setWorkerProperty(16, "VAL1.CLASSPATH", "org.signserver.validationservice.server.DummyValidator");
        sSSession.setWorkerProperty(16, "VAL1.TESTPROP", "TEST");
        sSSession.setWorkerProperty(16, "VAL1.ISSUER1.CERTCHAIN", ValidationTestUtils.genPEMStringFromChain(validChain1));
        assertNotNull(signserverhome);

        sSSession.reloadConfiguration(16);

        validcert1derpath = signserverhome + "/tmp/validcert1.cer";
        validcert1path = signserverhome + "/tmp/validcert1.pem";
        revokedcertpath = signserverhome + "/tmp/revokedcert1.pem";

        FileOutputStream fos = new FileOutputStream(validcert1derpath);
        fos.write(Base64.decode(validCert1.getBytes()));
        fos.close();
        fos = new FileOutputStream(validcert1path);
        fos.write("-----BEGIN CERTIFICATE-----\n".getBytes());
        fos.write(validCert1.getBytes());
        fos.write("\n-----END CERTIFICATE-----\n".getBytes());
        fos.close();
        fos = new FileOutputStream(revokedcertpath);
        fos.write("-----BEGIN CERTIFICATE-----\n".getBytes());
        fos.write(revokedCert1.getBytes());
        fos.write("\n-----END CERTIFICATE-----\n".getBytes());
        fos.close();

        TestingSecurityManager.remove();
    }

    @Test
    public void test01Help() throws Exception {
        int result = clientCLI.execute("validatecertificate");
        assertEquals(ValidateCertificateCommand.RETURN_BADARGUMENT, result);
        assertPrinted("", clientCLI.getOut(), "Usage: ");
        result = clientCLI.execute("validatecertificate", "-help");
        assertPrinted("", clientCLI.getOut(), "Usage: ");
        assertEquals(ValidateCertificateCommand.RETURN_BADARGUMENT, result);
    }

    @Test
    public void test02ValidationCLI() throws Exception {
        final String jksFile = new File(new File(signserverhome), "p12/truststore.jks").getAbsolutePath();

        assertEquals(CommandLineInterface.RETURN_SUCCESS, clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-cert", validcert1path, "-truststore", jksFile, "-truststorepwd", "changeit", "-port", String.valueOf(getPublicHTTPSPort())));
        int result = clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-cert", validcert1path, "-truststore", jksFile, "-truststorepwd", "changeit", "-port", String.valueOf(getPublicHTTPSPort()));
        assertEquals(ValidateCertificateCommand.RETURN_BADARGUMENT, result);
        result = clientCLI.execute("validatecertificate", "-service", "16", "-cert", validcert1path, "-truststore", jksFile, "-truststorepwd", "changeit", "-port", String.valueOf(getPublicHTTPSPort()));
        assertEquals(ValidateCertificateCommand.RETURN_BADARGUMENT, result);
        result = clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-pem", "-cert", validcert1path, "-truststore", jksFile, "-truststorepwd", "changeit", "-port", String.valueOf(getPublicHTTPSPort()));
        assertEquals(ValidateCertificateCommand.RETURN_BADARGUMENT, result);
        assertEquals(CommandLineInterface.RETURN_SUCCESS, clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-pem", "-cert", validcert1path, "-truststore", jksFile, "-truststorepwd", "changeit", "-port", String.valueOf(getPublicHTTPSPort())));
        assertEquals(CommandLineInterface.RETURN_SUCCESS, clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-port", String.valueOf(getPublicHTTPSPort()), "-cert", validcert1derpath, "-truststore", jksFile, "-truststorepwd", "changeit"));
        result = clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-port", String.valueOf(getPublicHTTPSPort()), "-cert", revokedcertpath, "-truststore", jksFile, "-truststorepwd", "changeit");
        assertEquals(ValidateCertificateCommand.RETURN_REVOKED, result);
        assertEquals(CommandLineInterface.RETURN_SUCCESS, clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-port", String.valueOf(getPublicHTTPSPort()), "-certpurposes", "IDENTIFICATION", "-cert", validcert1derpath, "-truststore", jksFile, "-truststorepwd", "changeit"));
        assertEquals(CommandLineInterface.RETURN_SUCCESS, clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-port", String.valueOf(getPublicHTTPSPort()), "-certpurposes", "IDENTIFICATION,ELECTROINIC_SIGNATURE", "-cert", validcert1derpath, "-truststore", jksFile, "-truststorepwd", "changeit"));
        result = clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-der", "-port", String.valueOf(getPublicHTTPSPort()), "-certpurposes", "ELECTROINIC_SIGNATURE", "-cert", revokedcertpath, "-truststore", jksFile, "-truststorepwd", "changeit");
        assertEquals(ValidateCertificateCommand.RETURN_BADCERTPURPOSE, result);
        
        // test using the HTTP protocol
        assertEquals(CommandLineInterface.RETURN_SUCCESS,
                clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-cert", validcert1path, "-truststore", jksFile,
                        "-truststorepwd", "changeit", "-protocol", "HTTP", "-port", String.valueOf(getPublicHTTPSPort())));
        // test explicitly specifying the WEBSERVICES protocol (the default)
        assertEquals(CommandLineInterface.RETURN_SUCCESS,
                clientCLI.execute("validatecertificate", "-hosts", getHTTPHost(), "-service", "16", "-cert", validcert1path, "-truststore", jksFile,
                        "-truststorepwd", "changeit", "-protocol", "WEBSERVICES", "-port", String.valueOf(getPublicHTTPSPort())));
    }

    @Test
    public void test99RemoveDatabase() throws Exception {

        gCSession.removeProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER16.CLASSPATH");
        gCSession.removeProperty(GlobalConfiguration.SCOPE_GLOBAL, "WORKER16.SIGNERTOKEN.CLASSPATH");

        sSSession.removeWorkerProperty(16, "AUTHTYPE");
        sSSession.removeWorkerProperty(16, "VAL1.CLASSPATH");
        sSSession.removeWorkerProperty(16, "VAL1.TESTPROP");
        sSSession.removeWorkerProperty(16, "VAL1.ISSUER1.CERTCHAIN");


        sSSession.reloadConfiguration(16);

        TestingSecurityManager.remove();
    }
}
