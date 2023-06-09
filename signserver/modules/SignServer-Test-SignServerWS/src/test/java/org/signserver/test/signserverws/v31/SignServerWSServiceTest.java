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
package org.signserver.test.signserverws.v31;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.signserver.common.GenericSignRequest;
import org.signserver.common.GenericSignResponse;
import org.signserver.common.RequestAndResponseManager;
import org.signserver.test.signserverws.signserverws.v31
        .CryptoTokenOfflineException_Exception;
import org.signserver.test.signserverws.signserverws.v31
        .IllegalRequestException_Exception;
import org.signserver.test.signserverws.signserverws.v31
        .InvalidWorkerIdException_Exception;
import org.signserver.test.signserverws.signserverws.v31.ProcessRequestWS;
import org.signserver.test.signserverws.signserverws.v31.ProcessResponseWS;
import org.signserver.test.signserverws.signserverws.v31
        .SignServerException_Exception;
import org.signserver.test.signserverws.signserverws.v31.SignServerWS;
import org.signserver.test.signserverws.signserverws.v31.SignServerWSService;
import org.signserver.test.signserverws.signserverws.v31.WorkerStatusWS;
import org.signserver.testutils.ModulesTestCase;

/**
 * Test calling SignServerWSService using SignServer 3.1 WSDL.
 *
 * This tests assumes that test-configuration.properties as been applied to
 * SignServer.
 *
 * @author Markus Kilås
 * @version $Id$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignServerWSServiceTest extends ModulesTestCase {

    /** Logger for this class. */
    private static final Logger LOG
            = Logger.getLogger(SignServerWSServiceTest.class);

    /** Endpoint URL. */
    private final String ENDPOINT =
            "https://" + getHTTPHost() + ":" + getPublicHTTPSPort() + "/signserver/signserverws/signserverws?wsdl";

    private static final String[] CONF_FILES = {
        "signserver_deploy.properties",
        "conf/signserver_deploy.properties",
    };
    
    /** Worker ID as defined in test-configuration.properties. **/
    private static final String WORKERID = "7003";

    /** A worker ID assumed to not be existing. */
    private static final String NONEXISTING_WORKERID = "1231231";

    
    
    private SignServerWS ws;

    public SignServerWSServiceTest() {
        super();
        setupKeystores();
    }

    /** Setup keystores for SSL. **/
    private void setupKeystores() {
        Properties config = new Properties();
        
        final File home;
        final File path1 = new File("../..");
        final File path2 = new File(".");
        if (new File(path1, "res/deploytools/app.properties").exists()) {
            home = path1;
        } else if (new File(path2, "res/deploytools/app.properties").exists()) {
            home = path2;
            } else {
            throw new RuntimeException("Unable to detect SignServer path");
            }
        
        File confFile = null;
        for (String file : CONF_FILES) {
            final File f = new File(home, file);
            if (f.exists()) {
                confFile = f;
                break;
            }
        }
        if (confFile == null) {
            throw new RuntimeException("No signserver_deploy.properties found");
        } else {
        
            try {
                config.load(new FileInputStream(confFile));
        } catch (FileNotFoundException ignored) {
            LOG.debug("No signserver_deploy.properties");
        } catch (IOException ex) {
            LOG.error("Not using signserver_deploy.properties: " + ex.getMessage());
        }
            final String truststore = new File(home, "p12/truststore.jks").getAbsolutePath();
            System.out.println("Truststore: " + truststore);
            System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword",
                config.getProperty("java.trustpassword", "changeit"));
        //System.setProperty("javax.net.ssl.keyStore", "../../p12/testadmin.jks");
        //System.setProperty("javax.net.ssl.keyStorePassword", "foo123");
    }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LOG.info("Initilizing test using WS URL: " + getWsEndPointUrl());
        QName qname = new QName("gen.ws.protocol.signserver.org",
                "SignServerWSService");
        SignServerWSService signServerWSService = new SignServerWSService(
               new URL(getWsEndPointUrl()), qname);
        ws =  signServerWSService.getSignServerWSPort();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /** Overridden by org.signserver.test.signserverws.v32.SignServerWSServiceTest */
    protected String getWsEndPointUrl() {
    	return ENDPOINT;
    }
    
    public void test00SetupDatabase() throws Exception {
        addDummySigner(7003, "SignServerWSServiceTest_XMLSigner1", true);
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void test01GetStatusExisting() {
        try {
            final List<WorkerStatusWS> statuses = ws.getStatus(WORKERID);
            assertEquals("Number of results", 1, statuses.size());
            final WorkerStatusWS status = statuses.get(0);
            LOG.debug("Status: " + toString(status));

            assertEquals("workerName", "7003", status.getWorkerName());
            assertEquals("errormessage", null, status.getErrormessage());
            assertEquals("overallStatus", "ALLOK", status.getOverallStatus());
        } catch (InvalidWorkerIdException_Exception ex) {
            fail("Worker not found: " + WORKERID
                    + " Hasn't test-configuration.properties been applied?");
        }
    }

    public void test02GetStatusNonExisting() {
        try {
            final List<WorkerStatusWS> statuses
                    = ws.getStatus(NONEXISTING_WORKERID);
            fail("Should have thrown InvalidWorkerIdException_Exception but got "
                    + statuses);
        } catch (InvalidWorkerIdException_Exception ok) {
            // OK
        }
    }

    public void test03ProcessOk() {
        try {
            final List<ProcessRequestWS> requests = new ArrayList<>();
            final ProcessRequestWS request = new ProcessRequestWS();
            request.setRequestDataBase64(new String(Base64.encode(RequestAndResponseManager.serializeProcessRequest(new GenericSignRequest(4711, "<root/>".getBytes())))));
            requests.add(request);
            final List<ProcessResponseWS> responses = ws.process(WORKERID, requests);
            assertEquals("Number of results", 1, responses.size());
            final GenericSignResponse response = (GenericSignResponse) RequestAndResponseManager.parseProcessResponse(Base64.decode(responses.get(0).getResponseDataBase64()));
            LOG.trace("Response: " + new String(response.getProcessedData()));
            assertEquals("requestID", 4711, response.getRequestID());
            final Certificate certificate = response.getSignerCertificate();
            assertNotNull("Certificate", certificate);
        } catch (IOException | CryptoTokenOfflineException_Exception | IllegalRequestException_Exception | SignServerException_Exception ex) {
            LOG.error(ex, ex);
            fail(ex.getMessage());
        } catch (InvalidWorkerIdException_Exception ex) {
            fail("Worker not found: " + WORKERID
                    + " Hasn't test-configuration.properties been applied?");
        }
    }

    public void test04ProcessNonExisting() {
        try {
            final List<ProcessRequestWS> requests = new ArrayList<>();
            final ProcessRequestWS request = new ProcessRequestWS();
            request.setRequestDataBase64(new String(Base64.encode(RequestAndResponseManager.serializeProcessRequest(new GenericSignRequest(4711, "<root/>".getBytes())))));
            requests.add(request);
            ws.process(NONEXISTING_WORKERID, requests);
            fail("Should have thrown InvalidWorkerIdException_Exception");
        } catch (IOException | CryptoTokenOfflineException_Exception | IllegalRequestException_Exception | SignServerException_Exception ex) {
            LOG.error(ex, ex);
            fail(ex.getMessage());
        } catch (InvalidWorkerIdException_Exception ok) {
            // OK
        }
    }

    public void test05ProcessIllegalRequest() {
        try {
            final List<ProcessRequestWS> requests = new ArrayList<>();
            final ProcessRequestWS request = new ProcessRequestWS();
            request.setRequestDataBase64(new String(Base64.encode(RequestAndResponseManager.serializeProcessRequest(new GenericSignRequest(4711, "< not-an-well-formed-xml-doc".getBytes())))));
            requests.add(request);
            final List<ProcessResponseWS> responses = ws.process(WORKERID, requests);
            fail("Should have thrown IllegalRequest or SignServerException but got: "
                    + responses);
        } catch (IOException | CryptoTokenOfflineException_Exception ex) {
            LOG.error(ex, ex);
            fail(ex.getMessage());
        } catch (IllegalRequestException_Exception | SignServerException_Exception ex) {
            // OK
        }catch (InvalidWorkerIdException_Exception ex) {
            fail("Worker not found: " + WORKERID
                    + " Hasn't test-configuration.properties been applied?");
        }
        // OK (sort of, better would have been an illegalrequest)
        
    }
    
    public void test99RemoveDatabase() throws Exception {
        removeWorker(7003);
    }

    private String toString(WorkerStatusWS status) {
        final StringBuilder builder = new StringBuilder();
        builder.append("WorkerStatusWS {");
        builder.append("\n\t");

        builder.append("errormessage: ");
        builder.append(status.getErrormessage());
        builder.append("\n\t");

        builder.append("overallStatus: ");
        builder.append(status.getOverallStatus());
        builder.append("\n\t");

        builder.append("workerName: ");
        builder.append(status.getWorkerName());
        
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }

}
