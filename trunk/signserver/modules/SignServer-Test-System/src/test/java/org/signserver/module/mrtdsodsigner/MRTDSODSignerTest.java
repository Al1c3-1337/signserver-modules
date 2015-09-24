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
package org.signserver.module.mrtdsodsigner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import org.bouncycastle.util.encoders.Base64;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.signserver.cli.CommandLineInterface;
import org.signserver.common.*;
import org.signserver.module.mrtdsodsigner.jmrtd.SODFile;
import org.signserver.testutils.ModulesTestCase;
import org.signserver.testutils.TestingSecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.signserver.ejb.interfaces.IWorkerSession;

/**
 * Tests the MRTDSODSigner.
 *
 * @version $Id$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MRTDSODSignerTest extends ModulesTestCase {

    /** Worker7897: Default algorithms, default hashing setting */
    private static final int WORKER1 = 7897;

    /** Worker7898: SHA512, default hashing setting */
    private static final int WORKER2 = 7898;

    /** Worker7899: Default algorithms, DODATAGROUPHASHING=true */
    private static final int WORKER3 = 7899;

    /** Worker7900: SHA512, DODATAGROUPHASHING=true */
    private static final int WORKER4 = 7900;

    /** Worker7901: Same as WORKER1 but with P12CryptoToken. */
    private static final int WORKER1B = 7901;

    private static final int WORKER1C = 7902;
    
    private static final int WORKER1D = 7903;
    
    /** Worker7904: SHA256WithECDSA, DODATAGROUPHASHING=true */
    private static final int WORKER5 = 7904;
    
    private static final String SIGNER1C_CERT = 
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDBzCCAe+gAwIBAgIBATANBgkqhkiG9w0BAQUFADAeMRwwGgYDVQQDExNUZXN0\n" +
            "IFYzIENlcnRpZmljYXRlMB4XDTI1MDEwMTA5MDAwMFoXDTMwMDEwMTA5MDAwMFow\n" +
            "HjEcMBoGA1UEAxMTVGVzdCBWMyBDZXJ0aWZpY2F0ZTCCASIwDQYJKoZIhvcNAQEB\n" +
            "BQADggEPADCCAQoCggEBALGmSmtv9FtplILfe8kOjmEBdqo96WVMp6oy8bHMC59e\n" +
            "aE9Wu03kquoI2JMTwNcgapWKMYambzxr4rCVzBcc3kHpLaefnpq/5kFi9d8U6tgv\n" +
            "3T8Q8ZH1kMx/CH/fYQol0nMEqgl+S557zjNdBuSD36m45cN/UmI4K8Ie3S7a1xRx\n" +
            "2TuQZsxBxZUAU4SYgbC0DzwPXWB+EWJcGWXiqqZzKRNAnawNShkdEfVrCleg/Vff\n" +
            "T/iYOxMu3W/LQEpe69g/HmFYwYAATl7zm4jnVAhEZbEj8J4GhHW0gEo1qrf6CugO\n" +
            "a8/dEtGOSLLm9eilJIxydSiiTu9v9MAzs1LY3Ryt2wUCAwEAAaNQME4wHQYDVR0O\n" +
            "BBYEFDZcLOA/gL2djDmuITgKN+4JaT8IMC0GA1UdEAQmMCSgEBgOMjAxNTAxMDEx\n" +
            "MDAwMDChEBgOMjAyMDAxMDExMDAwMDAwDQYJKoZIhvcNAQEFBQADggEBAAGBmxuH\n" +
            "Z7VDnJacGonJOJxk5dDTA50c7Y8ggJZ3bpRW9afKTiAryq49ozm5sOv+XzWPf8FK\n" +
            "mbKhmknm3z8cfPL1LjA6c8dA0yvIpeT4IUYPqfWLHVpWTnbfnXQY97qKluRJF0sQ\n" +
            "AVNzxTE+ZDHBPFKzl/t8Zo9C7/ffNwxCVl0x5Ss8ie7q0y7PIm6yqnShPofvEnBE\n" +
            "F+dD33k8WXID2D/OLWQolrM6dnIGA9c1cFQ9v8kUXfX02fW4KGB09DAthgLv84zz\n" +
            "UoTzGmMnqBlb99BYjoDViVq4yWcxczJcjjIuj4hSNlH0Q/uWhqpmw4dqnlbH3fGO\n" +
            "ZvlQhYEZsj0eJMc=\n" +
            "-----END CERTIFICATE-----";
    
    private static final String SIGNER1D_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDBzCCAe+gAwIBAgIBATANBgkqhkiG9w0BAQUFADAeMRwwGgYDVQQDExNUZXN0\n" +
            "IFYzIENlcnRpZmljYXRlMB4XDTE1MDEwMTA5MDAwMFoXDTIwMDEwMTA5MDAwMFow\n" +
            "HjEcMBoGA1UEAxMTVGVzdCBWMyBDZXJ0aWZpY2F0ZTCCASIwDQYJKoZIhvcNAQEB\n" +
            "BQADggEPADCCAQoCggEBAJq2VhSoCMsOCI4YyGAOWHJk+8GNkz9/xsqDd4+YCkXl\n" +
            "pgBZvUrmCEhxd8IMS6LEvlAtv/TEGyh1FlL5ncUBFjPIbSvS7zM8f1gm06iNSdC9\n" +
            "dVwTHhu+L+mvajuFWlUr/agPaCM9rvUcUE5bceRioM0ORway1uyGGg5agecLbEKE\n" +
            "KB7mmmK4sJgwk6Ol/AbRk2bw2ep6XeZusEdplySTM3PFpbS97wRzJLQuo0pg9fZ3\n" +
            "yTvxlgRFbOJ7uGVY1H1ac2RcgvC3E+oSxg5Hk/xFn7R1iGMukn2exPVp0lPOz+QH\n" +
            "kgl+PefojU1MRTV4Nqf4jDp7zawhZz5yUvme3ZGg6tsCAwEAAaNQME4wHQYDVR0O\n" +
            "BBYEFCGmTJsyJQ/esdU6lsZXShnqS0LtMC0GA1UdEAQmMCSgEBgOMjAyNTAxMDEx\n" +
            "MDAwMDChEBgOMjAzMDAxMDExMDAwMDAwDQYJKoZIhvcNAQEFBQADggEBAB61vYmr\n" +
            "5fEuoN78Yiu3qZhOrJzA6I4D4F6NEX3vQOTfzjcdVLVRUhOmFHi33UfPcugWU1Nt\n" +
            "GLxa0nIdT+Pnc7AnoblBeNWNdYiM93DLHuQTwYyQVcQMlltHs2LhGesQ+LLijcTE\n" +
            "Cm+t1/HTuhQcENbS3IUyvp1borH+txTh+YBWKVmrvis+2SlLZSF8MppNO4NysaEa\n" +
            "ehTHOn0XLy9LSXHypmTqR/Jx5kfG12OcAx58baIMPbTbGxqR1tNceKb7+Sjy+urI\n" +
            "sX/0d4c1L1hohgpeJ2nbSeZ3SbSx/eNqhglBls/PSdEFqTpbAK7d+LfqkjDbKWbB\n" +
            "iVFbkjAvj/aacdk=\n" +
            "-----END CERTIFICATE-----";
    
    private static byte[] certbytes1 = Base64.decode((
              "MIIElTCCAn2gAwIBAgIITz1ZKtegWpgwDQYJKoZIhvcNAQELBQAwTTEXMBUGA1UE"
            + "AwwORFNTIFJvb3QgQ0EgMTAxEDAOBgNVBAsMB1Rlc3RpbmcxEzARBgNVBAoMClNp"
            + "Z25TZXJ2ZXIxCzAJBgNVBAYTAlNFMB4XDTExMDUyNzA5NTE0NVoXDTIxMDUyNzA5"
            + "NTE0NVowRzERMA8GA1UEAwwIU2lnbmVyIDQxEDAOBgNVBAsMB1Rlc3RpbmcxEzAR"
            + "BgNVBAoMClNpZ25TZXJ2ZXIxCzAJBgNVBAYTAlNFMIIBIjANBgkqhkiG9w0BAQEF"
            + "AAOCAQ8AMIIBCgKCAQEAnCGlYABPTW3Jx607cdkHPDJEGXpKCXkI29zj8BxCIvC3"
            + "3kyGZB6M7EICU+7vt200u1TmSjx2auTfZI6sA2cDsESlMhKJ+8nj2uj1f5g9MYRb"
            + "+IIq1IIhDArWwICswnZkWL/5Ncggg2bNcidCblDy5SUQ+xMeXtJQWCU8Zn3a+ySZ"
            + "Z1ZiYZ10gUu5JValsuOb8YpcT/pqBPF0cgEy6mIe3ANolzxLKNUBYAsQzQnCvgx+"
            + "GqgbzYHo8fkppSGUFVYdFI0MC9CBT72eOxxQoguICWXus8BdIwebZDGQdluKvTNs"
            + "ig4hM39G6WvPqoEi9I86VhY9mSyY+WOeU5Y3ZsC8CQIDAQABo38wfTAdBgNVHQ4E"
            + "FgQUGqddBv2s8iEa5B98MVTbQ2HiFkAwDAYDVR0TAQH/BAIwADAfBgNVHSMEGDAW"
            + "gBQgeiHe6K27Aqj7cVikCWK52FgFojAOBgNVHQ8BAf8EBAMCBeAwHQYDVR0lBBYw"
            + "FAYIKwYBBQUHAwIGCCsGAQUFBwMEMA0GCSqGSIb3DQEBCwUAA4ICAQB8HpFOfiTb"
            + "ylu06tg0yqvix93zZrJWSKT5PjwpqAU+btQ4fFy4GUBG6VuuVr27+FaFND3oaIQW"
            + "BXdQ1+6ea3Nu9WCnKkLLjg7OjBNWw1LCrHXiAHdIYM3mqayPcf7ezbr6AMnmwDs6"
            + "/8YAXjyRLmhGb23M8db+3pgTf0Co/CoeQWVa1eJObH7aO4/Koeg4twwbKG0MjwEY"
            + "ZPi0ZWB93w/llEHbvMNI9dsQWSqIU7W56KRFN66WdqFhjdVPyg86NudH+9jmp4x1"
            + "Ac9GKGNOYYfDnQCdsrJwZMvcI7bZykbd77ZC3zBzuaISAeRJq3rjHygSeKPHCYDW"
            + "zAVEP9yaO0fL7HMZ2uqHxokvuOo5SxgVfvLr+kT4ioQHz+r9ehkCf0dbydm7EqyJ"
            + "Y7YSFUDEqk57dnZDxy7ZgUA/TZf3I3rPjSopDxqiqJbm9L0GPW3zk0pAZx7dgLcq"
            + "2I8fv+DBEKqJ47/H2V5aopxsRhiKC5u8nEEbAMbBYgjGQT/5K4mBt0gUJFNek7vS"
            + "a50VH05u8P6yo/3ppDxGCXE2d2JfWlEIx7DRWWij2PuOgDGkvVt2soxtp8Lx+kS6"
            + "K+G+tA5BGZMyEPdqAakyup7udi4LoB0wfJ58Jr5QNHCx4icUWvCBUM5CTcH4O/pQ"
            + "oj/7HSYZlqigM72nR8f/gv1TwLVKz+ygzg==").getBytes());
    
    private static final String ALIAS_DEMODSEC = "MRTD Sod Signer";
    private static final String ALIAS_DEMODS1 = "demods1";

    private final IWorkerSession workerSession = getWorkerSession();
    
    @Before
    protected void setUp() throws Exception {
        SignServerUtil.installBCProvider();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    protected void tearDown() throws Exception {
        TestingSecurityManager.remove();
    }

    @Test
    public void test00SetupDatabase() throws Exception {
        assertEquals(CommandLineInterface.RETURN_SUCCESS, 
                getAdminCLI().execute("setproperties", getSignServerHome().getAbsolutePath() + "/res/test/test-mrtdsodsigner-configuration.properties"));

        // WORKER1 uses a P12 keystore
        workerSession.setWorkerProperty(WORKER1, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "demods1.p12");
        workerSession.setWorkerProperty(WORKER1, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER1, "DEFAULTKEY", ALIAS_DEMODS1);

        // WORKER1B uses a P12 keystore
        workerSession.setWorkerProperty(WORKER1B, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "dss10/dss10_signer1.p12");
        workerSession.setWorkerProperty(WORKER1B, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER1B, "DEFAULTKEY", "Signer 1");

        // WORKER2 uses a P12 keystore
        workerSession.setWorkerProperty(WORKER2, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "demods1.p12");
        workerSession.setWorkerProperty(WORKER2, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER2, "DEFAULTKEY", ALIAS_DEMODS1);

        // WORKER3 uses a P12 keystore
        workerSession.setWorkerProperty(WORKER3, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "demods1.p12");
        workerSession.setWorkerProperty(WORKER3, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER3, "DEFAULTKEY", ALIAS_DEMODS1);

        // WORKER4 uses a P12 keystore
        workerSession.setWorkerProperty(WORKER4, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "demods1.p12");
        workerSession.setWorkerProperty(WORKER4, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER4, "DEFAULTKEY", ALIAS_DEMODS1);

        // WORKER5 uses a P12 keystore and ECC
        workerSession.setWorkerProperty(WORKER5, "KEYSTOREPATH",
                getSignServerHome().getAbsolutePath()
                + File.separator + "res" + File.separator + "test"
                + File.separator + "demodsecc1.p12");
        workerSession.setWorkerProperty(WORKER5, "KEYSTOREPASSWORD", "foo123");
        workerSession.setWorkerProperty(WORKER5, "DEFAULTKEY", ALIAS_DEMODSEC);

        workerSession.reloadConfiguration(WORKER1);
        workerSession.reloadConfiguration(WORKER2);
        workerSession.reloadConfiguration(WORKER3);
        workerSession.reloadConfiguration(WORKER4);
        workerSession.reloadConfiguration(WORKER5);
        workerSession.reloadConfiguration(WORKER1B);

        addSigner("org.signserver.module.mrtdsodsigner.MRTDSODSigner", WORKER1C, "TestMRTDSODSigner1c", true);
        workerSession.setWorkerProperty(WORKER1C, "SIGNERCERT", SIGNER1C_CERT);
        workerSession.setWorkerProperty(WORKER1C, "SIGNERCERTCHAIN", SIGNER1C_CERT);
        workerSession.reloadConfiguration(WORKER1C);
        addSigner("org.signserver.module.mrtdsodsigner.MRTDSODSigner", WORKER1D, "TestMRTDSODSigner1d", true);
        workerSession.setWorkerProperty(WORKER1D, "SIGNERCERT", SIGNER1D_CERT);
        workerSession.setWorkerProperty(WORKER1D, "SIGNERCERTCHAIN", SIGNER1D_CERT);
        workerSession.reloadConfiguration(WORKER1D);
    }

    /**
     * Requests signing of some data group hashes, using two different signers
     * with different algorithms and verifies the result.
     * @throws Exception
     */
    @Test
    public void test02SignData() throws Exception {
        // DG1, DG2 and default values
        Map<Integer, byte[]> dataGroups1 = new LinkedHashMap<Integer, byte[]>();
        dataGroups1.put(1, digestHelper("Dummy Value 1".getBytes(), "SHA256"));
        dataGroups1.put(2, digestHelper("Dummy Value 2".getBytes(), "SHA256"));
        signHelper(WORKER1, 12, dataGroups1, false, "SHA256", "SHA256withRSA");

        // DG3, DG7, DG8, DG13 and default values
        Map<Integer, byte[]> dataGroups2 = new LinkedHashMap<Integer, byte[]>();
        dataGroups2.put(3, digestHelper("Dummy Value 3".getBytes(), "SHA256"));
        dataGroups2.put(7, digestHelper("Dummy Value 4".getBytes(), "SHA256"));
        dataGroups2.put(8, digestHelper("Dummy Value 5".getBytes(), "SHA256"));
        dataGroups2.put(13, digestHelper("Dummy Value 6".getBytes(), "SHA256"));
        signHelper(WORKER1, 13, dataGroups2, false, "SHA256", "SHA256withRSA");

        // DG1, DG2 with the other worker which uses SHA512 and SHA512withRSA
        Map<Integer, byte[]> dataGroups3 = new LinkedHashMap<Integer, byte[]>();
        dataGroups3.put(1, digestHelper("Dummy Value 7".getBytes(), "SHA512"));
        dataGroups3.put(2, digestHelper("Dummy Value 8".getBytes(), "SHA512"));
        signHelper(WORKER2, 14, dataGroups3, false, "SHA512", "SHA512withRSA");

        // DG1, DG2 with the other worker which uses SHA256 and SHA256withECDSA
        signHelper(WORKER5, 15, dataGroups2, false, "SHA256", "SHA256withECDSA");
    }

    /**
     * Requests signing of some data groups, using two different signers
     * with different algorithms and verifies the result. The signer does the
     * hashing.
     * @throws Exception
     */
    @Test
    public void test03SignUnhashedData() throws Exception {
        // DG1, DG2 and default values
        Map<Integer, byte[]> dataGroups1 = new LinkedHashMap<Integer, byte[]>();
        dataGroups1.put(1, "Dummy Value 1".getBytes());
        dataGroups1.put(2, "Dummy Value 2".getBytes());
        signHelper(WORKER3, 15, dataGroups1, true, "SHA256", "SHA256withRSA");

        // DG3, DG7, DG8, DG13 and default values
        Map<Integer, byte[]> dataGroups2 = new LinkedHashMap<Integer, byte[]>();
        dataGroups2.put(3, "Dummy Value 3".getBytes());
        dataGroups2.put(7, "Dummy Value 4".getBytes());
        dataGroups2.put(8, "Dummy Value 5".getBytes());
        dataGroups2.put(13, "Dummy Value 6".getBytes());
        signHelper(WORKER3, 16, dataGroups2, true, "SHA256", "SHA256withRSA");

        // DG1, DG2 with the other worker which uses SHA512 and SHA512withRSA
        Map<Integer, byte[]> dataGroups3 = new LinkedHashMap<Integer, byte[]>();
        dataGroups3.put(1, "Dummy Value 7".getBytes());
        dataGroups3.put(2, "Dummy Value 8".getBytes());
        signHelper(WORKER4, 17, dataGroups3, true, "SHA512", "SHA512withRSA");
    }

    @Test
    public void test04MinRemainingCertVValidity() throws Exception {
        // A signing operation that will work
        Map<Integer, byte[]> dataGroups1 = new LinkedHashMap<Integer, byte[]>();
        dataGroups1.put(1, digestHelper("Dummy Value 1".getBytes(), "SHA256"));
        dataGroups1.put(2, digestHelper("Dummy Value 2".getBytes(), "SHA256"));
        signHelper(WORKER1, 12, dataGroups1, false, "SHA256", "SHA256withRSA");

        // Set property to limit remaining cert validity
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certbytes1));

        workerSession.uploadSignerCertificate(WORKER1, cert.getEncoded(), GlobalConfiguration.SCOPE_GLOBAL);
        workerSession.setWorkerProperty(WORKER1, SignServerConstants.MINREMAININGCERTVALIDITY, "6300");
        workerSession.reloadConfiguration(WORKER1);
        // Signing operation should not work now
        boolean thrown = false;
        try {
            signHelper(WORKER1, 12, dataGroups1, false, "SHA256", "SHA256withRSA");
        } catch (CryptoTokenOfflineException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // Test that there is an error as the signer is not valid yet
        WorkerStatus status = workerSession.getStatus(WORKER1);
        String errors = status.getFatalErrors().toString();
        assertTrue(errors, errors.contains("xpired"));
    }

    @Test
    public void test04bMinRemainingCertVValidityWithSoftKeystore()
            throws Exception {
        // A signing operation that will work
        Map<Integer, byte[]> dataGroups1 = new LinkedHashMap<Integer, byte[]>();
        dataGroups1.put(1, digestHelper("Dummy Value 1".getBytes(), "SHA256"));
        dataGroups1.put(2, digestHelper("Dummy Value 2".getBytes(), "SHA256"));
        signHelper(WORKER1B, 12, dataGroups1, false, "SHA256", "SHA256withRSA");

        // Set property to limit remaining cert validity
        workerSession.setWorkerProperty(WORKER1B,
                SignServerConstants.MINREMAININGCERTVALIDITY, "6300");
        workerSession.reloadConfiguration(WORKER1B);
        
        System.out.println("remaining: " + workerSession.getSigningValidityNotAfter(WORKER1B));
        
        // Signing operation should not work now
        boolean thrown = false;
        try {
            signHelper(WORKER1B, 12, dataGroups1, false, "SHA256",
                    "SHA256withRSA");
        } catch (CryptoTokenOfflineException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // Test that there is an error as the signer is not valid yet
        WorkerStatus status = workerSession.getStatus(WORKER1B);
        String errors = status.getFatalErrors().toString();
        assertTrue(errors, errors.contains("xpired"));
    }

    /**
     * Tests all validities: certificate, privatekey and min remaining period.
     * @throws Exception in case of error.
     */
    @Test
    public void test04cRemainingValidity() throws Exception {
        Calendar cal = Calendar.getInstance();

        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTVALIDITY", "True");
        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTPRIVATEKEYVALIDITY",
                "False");
        workerSession.setWorkerProperty(WORKER1C, "MINREMAININGCERTVALIDITY", "0");

        //    Certificate with: cert#1: priv=[2015, 2020], cert=[2025, 2030]
        //              cert#2: priv=[2025, 2030], cert=[2015, 2020]
        //
        //
        //    test#1: 	getSignerValidityNotAfter:  cert#1, bCert 	= 2030
        Date d = workerSession.getSigningValidityNotAfter(WORKER1C);
        assertNotNull("test#1 not null", d);
        cal.setTime(d);
        assertEquals(2030, cal.get(Calendar.YEAR));

        //    test#2	getSignerValidityNotBefore: cert#1, bCert       = 2025
        d = workerSession.getSigningValidityNotBefore(WORKER1C);
        assertNotNull("test#2 not null", d);
        cal.setTime(d);
        assertEquals(2025, cal.get(Calendar.YEAR));

        //    test#3: 	getSignerValidityNotAfter:  cert#1, bPriv       = 2020
        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTVALIDITY", "False");
        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTPRIVATEKEYVALIDITY",
                "True");
        d = workerSession.getSigningValidityNotAfter(WORKER1C);
        assertNotNull("test#3 not null", d);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));

        //    test#4	getSignerValidityNotBefore: cert#1, bPriv       = 2015
        d = workerSession.getSigningValidityNotBefore(WORKER1C);
        assertNotNull("test#4 not null", d);
        cal.setTime(d);
        assertEquals(2015, cal.get(Calendar.YEAR));

        //    test#5: 	getSignerValidityNotAfter:  cert#1, bCert, bPriv	  = 2020
        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTVALIDITY", "True");
        d = workerSession.getSigningValidityNotAfter(WORKER1C);
        assertNotNull("test#5 not null", d);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));

        //    test#6		getSignerValidityNotBefore: cert#1, bCert, bPrive = 2015
        d = workerSession.getSigningValidityNotBefore(WORKER1C);
        assertNotNull("test#6 not null", d);
        cal.setTime(d);
        assertEquals(2015, cal.get(Calendar.YEAR));

        //    test#7: 	getSignerValidityNotAfter:  cert#1, bCert, r10 		  = 2020
        workerSession.setWorkerProperty(WORKER1C, "CHECKCERTPRIVATEKEYVALIDITY",
                "False");
        workerSession.setWorkerProperty(WORKER1C, "MINREMAININGCERTVALIDITY", "3650");
        d = workerSession.getSigningValidityNotAfter(WORKER1C);
        assertNotNull("test#7 not null", d);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));

        //    test#8		getSignerValidityNotBefore: cert#1, bCert, r10	  = 2015
        d = workerSession.getSigningValidityNotBefore(WORKER1C);
        assertNotNull("test#8 not null", d);
        cal.setTime(d);
        assertEquals(2025, cal.get(Calendar.YEAR));

        //    test#9: 	getSignerValidityNotAfter:  cert#1, bCert, r4 		  = 2026
        workerSession.setWorkerProperty(WORKER1C, "MINREMAININGCERTVALIDITY", "1460");
        d = workerSession.getSigningValidityNotAfter(WORKER1C);
        assertNotNull("test#9 not null", d);
        cal.setTime(d);
        assertEquals(2026, cal.get(Calendar.YEAR));

        //    test#10:	getSignerValidityNotBefore: cert#1, bCert, r4		  = 2025
        d = workerSession.getSigningValidityNotBefore(WORKER1C);
        assertNotNull("test#10 not null", d);
        cal.setTime(d);
        assertEquals(2025, cal.get(Calendar.YEAR));

        //    test#21: 	getSignerValidityNotAfter:  cert#2, bCert 		  = 2020
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTVALIDITY", "True");
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTPRIVATEKEYVALIDITY",
                "False");
        workerSession.setWorkerProperty(WORKER1D, "MINREMAININGCERTVALIDITY", "0");
        d = workerSession.getSigningValidityNotAfter(WORKER1D);
        assertNotNull("test#21 not null", d);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));

        //    test#22		getSignerValidityNotBefore: cert#2, bCert	  = 2015
        assertNotNull("test#22 not null", d);
        d = workerSession.getSigningValidityNotBefore(WORKER1D);
        cal.setTime(d);
        assertEquals(2015, cal.get(Calendar.YEAR));

        //    test#23: 	getSignerValidityNotAfter:  cert#2, bPriv 		  = 2030
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTVALIDITY", "False");
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTPRIVATEKEYVALIDITY",
                "True");
        d = workerSession.getSigningValidityNotAfter(WORKER1D);
        assertNotNull("test#23 not null", d);
        cal.setTime(d);
        assertEquals(2030, cal.get(Calendar.YEAR));

        //    test#24		getSignerValidityNotBefore: cert#2, bPriv	 = 2025
        d = workerSession.getSigningValidityNotBefore(WORKER1D);
        assertNotNull("test#24 not null", d);
        cal.setTime(d);
        assertEquals(2025, cal.get(Calendar.YEAR));

        //    test#25: 	getSignerValidityNotAfter:  cert#2, bCert, bPriv	 = 2020
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTVALIDITY", "True");
        d = workerSession.getSigningValidityNotAfter(WORKER1D);
        assertNotNull("test#25 not null", d);
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));

        //    test#26		getSignerValidityNotBefore: cert#2, bCert, bPriv = 2025
        d = workerSession.getSigningValidityNotBefore(WORKER1D);
        assertNotNull("test#26 not null", d);
        cal.setTime(d);
        assertEquals(2025, cal.get(Calendar.YEAR));

        //    test#27: 	getSignerValidityNotAfter:  cert#2, bCert, r10 		  = 2010 r10 -> 3650
        workerSession.setWorkerProperty(WORKER1D, "CHECKCERTPRIVATEKEYVALIDITY",
                "False");
        workerSession.setWorkerProperty(WORKER1D, "MINREMAININGCERTVALIDITY", "3650");
        d = workerSession.getSigningValidityNotAfter(WORKER1D);
        assertNotNull("test#27 not null", d);
        cal.setTime(d);
        assertEquals(2010, cal.get(Calendar.YEAR));

        //    test#28		getSignerValidityNotBefore: cert#2, bCert, r10	  = 2015
        d = workerSession.getSigningValidityNotBefore(WORKER1D);
        assertNotNull("test#28 not null", d);
        cal.setTime(d);
        assertEquals(2015, cal.get(Calendar.YEAR));

        //    test#29: 	getSignerValidityNotAfter:  cert#2, bCert, r4 		  = 2016 r4 -> 1460
        workerSession.setWorkerProperty(WORKER1D, "MINREMAININGCERTVALIDITY", "1460");
        d = workerSession.getSigningValidityNotAfter(WORKER1D);
        assertNotNull("test#29 not null", d);
        cal.setTime(d);
        assertEquals(2016, cal.get(Calendar.YEAR));

        //    test#30:	getSignerValidityNotBefore: cert#2, bCert, r4		  = 2015
        d = workerSession.getSigningValidityNotBefore(WORKER1D);
        assertNotNull("test#30 not null", d);
        cal.setTime(d);
        assertEquals(2015, cal.get(Calendar.YEAR));
    }

    private void signHelper(int workerId, int requestId, Map<Integer, byte[]> dataGroups, boolean signerDoesHashing, String digestAlg, String sigAlg) throws Exception {
        // Create expected hashes
        Map<Integer, byte[]> expectedHashes;
        if (signerDoesHashing) {
            MessageDigest d = MessageDigest.getInstance(digestAlg, "BC");
            expectedHashes = new HashMap<Integer, byte[]>();
            for (Map.Entry<Integer, byte[]> entry : dataGroups.entrySet()) {
                expectedHashes.put(entry.getKey(), d.digest(entry.getValue()));
                d.reset();
            }
        } else {
            expectedHashes = dataGroups;
        }

        SODSignResponse res = (SODSignResponse) workerSession.process(workerId, new SODSignRequest(requestId, dataGroups), new RequestContext());
        assertNotNull(res);
        assertEquals(requestId, res.getRequestID());
        Certificate signercert = res.getSignerCertificate();
        assertNotNull(signercert);

        byte[] sodBytes = res.getProcessedData();
        SODFile sod = new SODFile(new ByteArrayInputStream(sodBytes));
        boolean verify = sod.checkDocSignature(signercert);
        assertTrue("Signature verification", verify);

        // Check the SOD
        Map<Integer, byte[]> actualDataGroupHashes = sod.getDataGroupHashes();
        assertEquals(expectedHashes.size(), actualDataGroupHashes.size());
        for (Map.Entry<Integer, byte[]> entry : actualDataGroupHashes.entrySet()) {
            assertTrue("DG" + entry.getKey(), Arrays.equals(expectedHashes.get(entry.getKey()), entry.getValue()));
        }
        assertEquals(digestAlg, sod.getDigestAlgorithm());
        assertEquals(sigAlg, sod.getDigestEncryptionAlgorithm());
    }

    private byte[] digestHelper(byte[] data, String digestAlgorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
        return md.digest(data);
    }

    /**
     * Test method for 'org.signserver.server.MRTDSigner.getStatus()'
     */
    @Test
    public void test05GetStatus() throws Exception {
        StaticWorkerStatus stat = (StaticWorkerStatus) workerSession.getStatus(7897);
        assertTrue(stat.getTokenStatus() == WorkerStatus.STATUS_ACTIVE);
    }

    /**
     * Test that setting INCLUDE_CERTIFICATE_LEVELS is not supported.
     * @throws Exception
     */
    @Test
    public void test06IncludeCertificateLevelsNotSupported() throws Exception {
        try {
            workerSession.setWorkerProperty(WORKER1, WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS, "2");
            workerSession.reloadConfiguration(WORKER1);
            
            final List<String> errors = workerSession.getStatus(WORKER1).getFatalErrors();
            
            assertTrue("Should contain error", errors.contains(WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS + " is not supported."));
        } finally {
            workerSession.removeWorkerProperty(WORKER1, WorkerConfig.PROPERTY_INCLUDE_CERTIFICATE_LEVELS);
            workerSession.reloadConfiguration(WORKER1);
        }
    }
    
    @Test
    public void test99TearDownDatabase() throws Exception {
        removeWorker(WORKER1);
        removeWorker(WORKER2);
        removeWorker(WORKER3);
        removeWorker(WORKER4);
        removeWorker(WORKER5);
        removeWorker(WORKER1B);
        removeWorker(WORKER1C);
        removeWorker(WORKER1D);
    }
}
