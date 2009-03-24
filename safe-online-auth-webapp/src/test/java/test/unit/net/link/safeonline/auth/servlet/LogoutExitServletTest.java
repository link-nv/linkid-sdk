/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;

import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.LogoutExitServlet;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.authentication.service.LogoutState;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.openssl.PEMWriter;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LogoutExitServletTest {

    private static final Log   LOG                = LogFactory.getLog(LogoutExitServletTest.class);

    private ServletTestManager logoutExitServletTestManager;

    private String             servletEndpointUrl = "http://test.auth/servlet";

    private String             inResponseTo       = "test-in-response-to";

    private JndiTestUtils      jndiTestUtils;

    private LogoutService      mockLogoutService;

    private Object[]           mockObjects;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockLogoutService = createMock(LogoutService.class);

        logoutExitServletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("ServletEndpointUrl", servletEndpointUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(ProtocolHandlerManager.PROTOCOL_HANDLER_ID_ATTRIBUTE, Saml2PostProtocolHandler.class.getName());
        initialSessionAttributes.put(LogoutServiceManager.LOGOUT_SERVICE_ATTRIBUTE, mockLogoutService);

        logoutExitServletTestManager.setUp(LogoutExitServlet.class, initParams, null, null, initialSessionAttributes);

        mockObjects = new Object[] { mockLogoutService };
    }

    @After
    public void tearDown()
            throws Exception {

        logoutExitServletTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void handleLogoutResponseSendLogoutRequest()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = logoutExitServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String application2Name = "test-application2-id";
        URL application2SsoLogoutUrl = new URL("http", "test.app", "/logout");
        ApplicationEntity application2 = new ApplicationEntity();
        application2.setSsoLogoutUrl(application2SsoLogoutUrl);

        String userId = UUID.randomUUID().toString();

        String samlLogoutResponse = LogoutResponseFactory.createLogoutResponse(inResponseTo, applicationName, applicationKeyPair,
                servletEndpointUrl);
        String encodedSamlLogoutResponse = Base64.encode(samlLogoutResponse.getBytes());

        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, application2Name, applicationKeyPair,
                servletEndpointUrl, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        NameValuePair[] data = { new NameValuePair("SAMLResponse", encodedSamlLogoutResponse) };
        postMethod.setRequestBody(data);

        // expectations
        expect(mockLogoutService.getLogoutState()).andStubReturn(LogoutState.LOGGING_OUT);
        expect(mockLogoutService.handleLogoutResponse((LogoutResponse) EasyMock.anyObject())).andStubReturn(applicationName);
        expect(mockLogoutService.findSsoApplicationToLogout()).andStubReturn(application2);
        expect(mockLogoutService.getLogoutRequest(application2)).andStubReturn(encodedSamlLogoutRequest);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        String responseBody = postMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        Document responseDocument = DomTestUtils.parseDocument(responseBody);
        LOG.debug("document element name: " + responseDocument.getDocumentElement().getNodeName());
        Node valueNode = XPathAPI.selectSingleNode(responseDocument, "/:html/:body/:form//:input[@name='SAMLRequest']/@value");
        assertNotNull(valueNode);
        String samlRequestValue = valueNode.getTextContent();
        LOG.debug("SAMLRequest value: " + samlRequestValue);
        String samlRequest = new String(org.apache.commons.codec.binary.Base64.decodeBase64(samlRequestValue.getBytes()));
        LOG.debug("SAML Request: " + samlRequest);
        File tmpFile = File.createTempFile("saml-request-", ".xml");
        LOG.debug("tmp filename: " + tmpFile.getAbsolutePath());
        IOUtils.write(samlRequest, new FileOutputStream(tmpFile));

        String xmlFilename = tmpFile.getAbsolutePath();
        String pubFilename = FilenameUtils.getFullPath(xmlFilename) + FilenameUtils.getBaseName(xmlFilename) + ".pem";
        PEMWriter writer = new PEMWriter(new FileWriter(pubFilename));
        writer.writeObject(applicationKeyPair.getPublic());
        writer.close();

        Document samlRequestDocument = DomTestUtils.parseDocument(samlRequest);
        Element nsElement = samlRequestDocument.createElement("nsElement");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        assertNotNull(XPathAPI.selectSingleNode(samlRequestDocument, "/samlp:LogoutRequest/ds:Signature", nsElement));
        assertNotNull(XPathAPI.selectSingleNode(samlRequestDocument, "/samlp:LogoutRequest/saml:NameID", nsElement));
    }

    @Test
    public void sendLogoutRequest()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = logoutExitServletTestManager.getServletLocation();
        GetMethod getMethod = new GetMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        URL applicationSsoLogoutUrl = new URL("http", "test.app", "/logout");
        ApplicationEntity application = new ApplicationEntity();
        application.setSsoLogoutUrl(applicationSsoLogoutUrl);

        String userId = UUID.randomUUID().toString();

        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, applicationName, applicationKeyPair,
                servletEndpointUrl, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        // expectations
        expect(mockLogoutService.getLogoutState()).andStubReturn(LogoutState.INITIALIZED);
        expect(mockLogoutService.findSsoApplicationToLogout()).andStubReturn(application);
        expect(mockLogoutService.getLogoutRequest(application)).andStubReturn(encodedSamlLogoutRequest);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        Document responseDocument = DomTestUtils.parseDocument(responseBody);
        LOG.debug("document element name: " + responseDocument.getDocumentElement().getNodeName());
        Node valueNode = XPathAPI.selectSingleNode(responseDocument, "/:html/:body/:form//:input[@name='SAMLRequest']/@value");
        assertNotNull(valueNode);
        String samlRequestValue = valueNode.getTextContent();
        LOG.debug("SAMLRequest value: " + samlRequestValue);
        String samlRequest = new String(org.apache.commons.codec.binary.Base64.decodeBase64(samlRequestValue.getBytes()));
        LOG.debug("SAML Request: " + samlRequest);
        File tmpFile = File.createTempFile("saml-request-", ".xml");
        LOG.debug("tmp filename: " + tmpFile.getAbsolutePath());
        IOUtils.write(samlRequest, new FileOutputStream(tmpFile));

        String xmlFilename = tmpFile.getAbsolutePath();
        String pubFilename = FilenameUtils.getFullPath(xmlFilename) + FilenameUtils.getBaseName(xmlFilename) + ".pem";
        PEMWriter writer = new PEMWriter(new FileWriter(pubFilename));
        writer.writeObject(applicationKeyPair.getPublic());
        writer.close();

        Document samlRequestDocument = DomTestUtils.parseDocument(samlRequest);
        Element nsElement = samlRequestDocument.createElement("nsElement");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        assertNotNull(XPathAPI.selectSingleNode(samlRequestDocument, "/samlp:LogoutRequest/ds:Signature", nsElement));
        assertNotNull(XPathAPI.selectSingleNode(samlRequestDocument, "/samlp:LogoutRequest/saml:NameID", nsElement));
    }
}
