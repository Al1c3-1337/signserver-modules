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
package org.signserver.module.xades.signer;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.signserver.common.GenericSignRequest;
import org.signserver.common.GenericSignResponse;
import org.signserver.common.RequestContext;
import org.signserver.common.WorkerConfig;
import org.signserver.module.xades.signer.MockedTimeStampTokenProvider.MockedTimeStampVerificationProvider;
import org.signserver.server.WorkerContext;
import org.signserver.server.cryptotokens.ICryptoToken;
import org.signserver.test.utils.builders.CertBuilder;
import org.signserver.test.utils.builders.CryptoUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xades4j.properties.AllDataObjsCommitmentTypeProperty;
import xades4j.properties.QualifyingProperties;
import xades4j.properties.SignedDataObjectProperty;
import xades4j.properties.SignedProperties;
import xades4j.providers.CertificateValidationProvider;
import xades4j.providers.impl.PKIXCertificateValidationProvider;
import xades4j.verification.SignatureSpecificVerificationOptions;
import xades4j.verification.XAdESVerificationResult;
import xades4j.verification.XadesVerificationProfile;
import xades4j.verification.XadesVerifier;

/**
 * Unit tests for the XAdESSigner class.
 *
 * @author Markus Kilås
 * @version $Id$
 */
public class XAdESSignerUnitTest {

    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(XAdESSignerUnitTest.class);
    
    private static MockedCryptoToken token;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        final KeyPair signerKeyPair = CryptoUtils.generateRSA(1024);
        final Certificate[] certChain =
                new Certificate[] {new JcaX509CertificateConverter().getCertificate(new CertBuilder().
                        setSelfSignKeyPair(signerKeyPair).
                        setNotBefore(new Date(MockedTimeStampTokenProvider.TIMESTAMP)).build())};
        final Certificate signerCertificate = certChain[0];
        token = new MockedCryptoToken(signerKeyPair.getPrivate(), signerKeyPair.getPublic(), signerCertificate, Arrays.asList(certChain), "BC");
    }

    /**
     * Test of init method, of class XAdESSigner.
     */
    @Test
    public void testInit_ok() {
        LOG.info("init");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("XADESFORM", "T");
        config.setProperty("TSA_URL", "http://example.com/?test=5");
        config.setProperty("TSA_USERNAME", "username123");
        config.setProperty("TSA_PASSWORD", "password123");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        XAdESSignerParameters param = instance.getParameters();
        
        assertEquals("XADESFORM", "T", param.getXadesForm().name());
        assertEquals("TSA_URL", "http://example.com/?test=5", param.getTsaParameters().getUrl());
        assertEquals("TSA_USERNAME", "username123", param.getTsaParameters().getUsername());
        assertEquals("TSA_PASSWORD", "password123", param.getTsaParameters().getPassword());
        
        assertEquals(Collections.EMPTY_LIST, instance.getFatalErrors());
    }
    
    /**
     * Test of init method with incorrect XADESFORM, of class XAdESSigner.
     */
    @Test
    public void testInit_incorrectXADESFORM() {
        LOG.info("init");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("XADESFORM", "_NonExisting_");
        config.setProperty("TSA_URL", "http://example.com/?test=5");
        config.setProperty("TSA_USERNAME", "username123");
        config.setProperty("TSA_PASSWORD", "password123");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        String errors = instance.getFatalErrors().toString();
        assertTrue("error: " + errors, errors.contains("XADESFORM"));
    }
    
    /**
     * Test of init method with missing TSA_URL, of class XAdESSigner.
     */
    @Test
    public void testInit_missingTSA_URL() {
        LOG.info("init");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("XADESFORM", "T");
        // Not set: config.setProperty("TSA_URL", ...
        config.setProperty("TSA_USERNAME", "username123");
        config.setProperty("TSA_PASSWORD", "password123");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        String errors = instance.getFatalErrors().toString();
        assertTrue("error: " + errors, errors.contains("TSA_URL"));
    }
    
    /**
     * Test of init method default value for XADESFORM, of class XAdESSigner.
     */
    @Test
    public void testInit_defaultXADESFORM() {
        LOG.info("init");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        // Not set: config.setProperty("XADESFORM", "T");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        XAdESSignerParameters param = instance.getParameters();
        
        assertEquals("XADESFORM", "BES", param.getXadesForm().name());
        
        assertEquals(Collections.EMPTY_LIST, instance.getFatalErrors());
    }

    /**
     * Run a signing test with default form and varying commitment types.
     * 
     * @param commitmentTypesProperty COMMITMENT_TYPES property to test with
     *                                if null, doesn't set the property
     * @param expectedCommitmentTypeUris List of expected commitment type URIs
     * @throws Exception
     */
    private void testProcessData_basicSigningInternal(final String commitmentTypesProperty,
            final Collection<String> expectedCommitmentTypeUris) throws Exception {
        LOG.info("processData");

        XAdESSigner instance = new MockedXAdESSigner(token);
        WorkerConfig config = new WorkerConfig();
        
        if (commitmentTypesProperty != null) {
            config.setProperty("COMMITMENT_TYPES", commitmentTypesProperty);
        }
        
        instance.init(4711, config, null, null);
        
        RequestContext requestContext = new RequestContext();
        requestContext.put(RequestContext.TRANSACTION_ID, "0000-100-1");
        GenericSignRequest request = new GenericSignRequest(100, "<test100/>".getBytes("UTF-8"));
        GenericSignResponse response = (GenericSignResponse) instance.processData(request, requestContext);
        
        byte[] data = response.getProcessedData();
        final String signedXml = new String(data);
        LOG.debug("signedXml: " + signedXml);
        
        // Validation: setup
        CertStore certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(token.getCertificateChain(ICryptoToken.PURPOSE_SIGN)));
        KeyStore trustAnchors = KeyStore.getInstance("JKS");
        trustAnchors.load(null, "foo123".toCharArray());
        trustAnchors.setCertificateEntry("cert", token.getCertificate(ICryptoToken.PURPOSE_SIGN));

        CertificateValidationProvider certValidator = new PKIXCertificateValidationProvider(trustAnchors, false, certStore);

        XadesVerificationProfile p = new XadesVerificationProfile(certValidator);
        XadesVerifier verifier = p.newVerifier();
        
        // Validation: parse
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new ByteArrayInputStream(data));
        Element node = doc.getDocumentElement();
        
        XAdESVerificationResult r = verifier.verify(node, new SignatureSpecificVerificationOptions());

        assertEquals("BES", r.getSignatureForm().name());
        assertEquals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", r.getSignatureAlgorithmUri());

        final QualifyingProperties qp = r.getQualifyingProperties();
        
        final Set<String> foundUris = new HashSet<String>();
        
        final SignedProperties sp = qp.getSignedProperties();
        
        for (final SignedDataObjectProperty signedObjProp : sp.getDataObjProps()) {
            LOG.debug("object property: " + signedObjProp.getClass().getName() + ": " + signedObjProp.toString());
            
            if (signedObjProp instanceof AllDataObjsCommitmentTypeProperty) {
                final AllDataObjsCommitmentTypeProperty commitmentType =
                        (AllDataObjsCommitmentTypeProperty) signedObjProp;
                
                final String uri = commitmentType.getUri();
                LOG.debug("Found commitment type: " + uri);
                if (expectedCommitmentTypeUris.contains(uri)) {
                    foundUris.add(uri);
                } else {
                    fail("Unexpected commitment type: " + uri);
                }
            }
        }
        
        assertTrue("Should contain expected commitment types: " + expectedCommitmentTypeUris.toString(),
                foundUris.size() == expectedCommitmentTypeUris.size());
    }
    
    /**
     * Test of processData method for basic signing, of class XAdESSigner.
     * Test that by default, no commitment types are included.
     */
    @Test
    public void testProcessData_basicSigning() throws Exception {
        testProcessData_basicSigningInternal(null, Collections.<String>emptyList());
    }
    
    /**
     * Test with explicitly setting a single commitment type.
     * 
     * @throws Exception
     */
    @Test
    public void testProcessData_basicSigningSingleCommitmentType() throws Exception {
        testProcessData_basicSigningInternal("PROOF_OF_ORIGIN", Collections.singletonList(AllDataObjsCommitmentTypeProperty.proofOfOrigin().getUri()));
    }
    
    /**
     * Test with explicitly setting multiple commitment types.
     * 
     * @throws Exception
     */
    @Test
    public void testProcessData_basicSigningMultipleCommitmentTypes() throws Exception {
        testProcessData_basicSigningInternal("PROOF_OF_APPROVAL, PROOF_OF_ORIGIN",
                Arrays.asList(AllDataObjsCommitmentTypeProperty.proofOfApproval().getUri(),
                              AllDataObjsCommitmentTypeProperty.proofOfOrigin().getUri()));
    }
    
    /**
     * Test with explictly setting the value NONE.
     * 
     * @throws Exception
     */
    @Test
    public void testProcessData_basicSigningCommitmentTypesNone() throws Exception {
        testProcessData_basicSigningInternal("NONE", Collections.<String>emptyList());
    }
    
    /**
     * Test with an empty COMMITMENT_TYPES list.
     * 
     * @throws Exception
     */
    @Test
    public void testProcessData_basicSigningNoCommitmentType() throws Exception {
        LOG.info("testProcessData_basicSigningNoCommitmentType");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("COMMITMENT_TYPES", "");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        String errors = instance.getFatalErrors().toString();
        assertTrue("error: " + errors, errors.contains("can not be empty"));
    }
    
    @Test
    public void testProcessData_basicSigningXAdESFormT() throws Exception {
        LOG.info("testProcessData_basicSigningXAdESFormT");

        XAdESSigner instance = new MockedXAdESSigner(token);
        WorkerConfig config = new WorkerConfig();
        
        config.setProperty("XADESFORM", "T");
        config.setProperty("TSA_URL", "http://example.com/?test=5");
        
        instance.init(4711, config, null, null);
        instance.setTimeStampTokenProviderImplementation(MockedTimeStampTokenProvider.class);
        
        // reset mock counters
        MockedTimeStampTokenProvider.reset();
        
        RequestContext requestContext = new RequestContext();
        requestContext.put(RequestContext.TRANSACTION_ID, "0000-100-1");
        GenericSignRequest request = new GenericSignRequest(100, "<test100/>".getBytes("UTF-8"));
        GenericSignResponse response = (GenericSignResponse) instance.processData(request, requestContext);
        
        byte[] data = response.getProcessedData();
        final String signedXml = new String(data);
        LOG.debug("signedXml: " + signedXml);
        
        // Validation: setup
        CertStore certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(token.getCertificateChain(ICryptoToken.PURPOSE_SIGN)));
        KeyStore trustAnchors = KeyStore.getInstance("JKS");
        trustAnchors.load(null, "foo123".toCharArray());
        trustAnchors.setCertificateEntry("cert", token.getCertificate(ICryptoToken.PURPOSE_SIGN));
        
        CertificateValidationProvider certValidator = new PKIXCertificateValidationProvider(trustAnchors, false, certStore);
        
        XadesVerificationProfile p =
                new XadesVerificationProfile(certValidator).withTimeStampTokenVerifier(new MockedTimeStampVerificationProvider());
        XadesVerifier verifier = p.newVerifier();
        
        // Validation: parse
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new ByteArrayInputStream(data));
        Element node = doc.getDocumentElement();
        
        XAdESVerificationResult r = verifier.verify(node, new SignatureSpecificVerificationOptions());
        
        LOG.debug("signature form: " + r.getSignatureForm().name());
        assertEquals("T", r.getSignatureForm().name());
        assertEquals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", r.getSignatureAlgorithmUri());
        
        // check that a time stamp token was requested
        assertTrue("Should request a time stamp token", MockedTimeStampTokenProvider.hasRequestedTimeStampToken());
        
        // check that the time stamp token was verified
        assertTrue("Should try to verify timestamp", MockedTimeStampTokenProvider.hasPerformedTimeStampVerification());
    }

    /**
     * Test that setting an unknown commitment type results in a configuration error.
     * 
     * @throws Exception
     */
    @Test
    public void testUnknownCommitmentType() throws Exception {
        LOG.info("testUnknownCommitmentType");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("COMMITMENT_TYPES", "foobar");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        String errors = instance.getFatalErrors().toString();
        assertTrue("error: " + errors, errors.contains("commitment type"));
    }
    
    /**
     * Test that setting an unknown commitment type in combination with
     * a valid one results in a configuration error.
     * 
     * @throws Exception
     */
    @Test
    public void testUnknownAndKnownCommitmentType() throws Exception {
        LOG.info("testUnknownCommitmentType");
        int signerId = 4711;
        WorkerConfig config = new WorkerConfig();
        config.setProperty("COMMITMENT_TYPES", "PROOF_OF_ORIGIN, foobar");
        
        WorkerContext workerContext = null;
        EntityManager em = null;
        XAdESSigner instance = new MockedXAdESSigner(token);
        instance.init(signerId, config, workerContext, em);
        
        String errors = instance.getFatalErrors().toString();
        assertTrue("error: " + errors, errors.contains("commitment type"));
    }
}
