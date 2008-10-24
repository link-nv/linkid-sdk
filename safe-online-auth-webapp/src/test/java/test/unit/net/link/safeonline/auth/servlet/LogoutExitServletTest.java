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

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;

import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.LogoutExitServlet;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.util.ee.IdentityServiceClient;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LogoutExitServletTest {

    private static final Log      LOG                = LogFactory.getLog(LogoutExitServletTest.class);

    private ServletTestManager    logoutExitServletTestManager;

    private String                servletEndpointUrl = "http://test.auth/servlet";

    private String                protocolErrorUrl   = "protocol-error";

    private String                inResponseTo       = "test-in-response-to";

    private String                target             = "http://test.target";

    private JndiTestUtils         jndiTestUtils;

    private AuthenticationService mockAuthenticationService;

    private Object[]              mockObjects;


    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockAuthenticationService = createMock(AuthenticationService.class);

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        this.logoutExitServletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("ServletEndpointUrl", this.servletEndpointUrl);
        initParams.put("ProtocolErrorUrl", this.protocolErrorUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(ProtocolHandlerManager.PROTOCOL_HANDLER_ID_ATTRIBUTE, Saml2PostProtocolHandler.class.getName());
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, this.mockAuthenticationService);

        this.logoutExitServletTestManager.setUp(LogoutExitServlet.class, initParams, null, null, initialSessionAttributes);

        this.mockObjects = new Object[] { this.mockAuthenticationService };
    }

    @After
    public void tearDown() throws Exception {

        this.logoutExitServletTestManager.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void handleLogoutResponseSendLogoutRequest() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = this.logoutExitServletTestManager.getServletLocation();
        PostMethod postMethod = new PostMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        String application2Name = "test-application2-id";
        URL application2SsoLogoutUrl = new URL("http", "test.app", "/logout");
        ApplicationEntity application2 = new ApplicationEntity();
        application2.setSsoLogoutUrl(application2SsoLogoutUrl);

        String userId = UUID.randomUUID().toString();

        String samlLogoutResponse = LogoutResponseFactory.createLogoutResponse(this.inResponseTo, applicationName, applicationKeyPair,
                this.target);
        String encodedSamlLogoutResponse = Base64.encode(samlLogoutResponse.getBytes());

        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, application2Name, applicationKeyPair,
                this.servletEndpointUrl, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        NameValuePair[] data = { new NameValuePair("SAMLResponse", encodedSamlLogoutResponse) };
        postMethod.setRequestBody(data);

        // expectations
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.LOGGING_OUT);
        expect(this.mockAuthenticationService.handleLogoutResponse((HttpServletRequest) EasyMock.anyObject())).andStubReturn(
                applicationName);
        expect(this.mockAuthenticationService.findSsoApplicationToLogout()).andStubReturn(application2);
        expect(this.mockAuthenticationService.getLogoutRequest(application2)).andStubReturn(encodedSamlLogoutRequest);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        String responseBody = postMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        Document responseDocument = DomTestUtils.parseDocument(responseBody);
        LOG.debug("document element name: " + responseDocument.getDocumentElement().getNodeName());
        Node valueNode = XPathAPI.selectSingleNode(responseDocument, "/:html/:body/:form/:div/:input[@name='SAMLRequest']/@value");
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
    public void sendLogoutRequest() throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        String servletLocation = this.logoutExitServletTestManager.getServletLocation();
        GetMethod getMethod = new GetMethod(servletLocation);

        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        String applicationName = "test-application-id";
        URL applicationSsoLogoutUrl = new URL("http", "test.app", "/logout");
        ApplicationEntity application = new ApplicationEntity();
        application.setSsoLogoutUrl(applicationSsoLogoutUrl);

        String userId = UUID.randomUUID().toString();

        String samlLogoutRequest = LogoutRequestFactory.createLogoutRequest(userId, applicationName, applicationKeyPair,
                this.servletEndpointUrl, null);
        String encodedSamlLogoutRequest = Base64.encode(samlLogoutRequest.getBytes());

        // expectations
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.INITIALIZED);
        expect(this.mockAuthenticationService.findSsoApplicationToLogout()).andStubReturn(application);
        expect(this.mockAuthenticationService.getLogoutRequest(application)).andStubReturn(encodedSamlLogoutRequest);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        Document responseDocument = DomTestUtils.parseDocument(responseBody);
        LOG.debug("document element name: " + responseDocument.getDocumentElement().getNodeName());
        Node valueNode = XPathAPI.selectSingleNode(responseDocument, "/:html/:body/:form/:div/:input[@name='SAMLRequest']/@value");
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
