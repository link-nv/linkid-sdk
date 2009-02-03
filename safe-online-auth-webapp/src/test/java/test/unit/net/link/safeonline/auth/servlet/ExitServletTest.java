/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.protocol.ProtocolHandlerManager;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.auth.servlet.ExitServlet;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.sdk.auth.saml2.AuthnResponseFactory;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.bouncycastle.openssl.PEMWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ExitServletTest {

    static final Log              LOG              = LogFactory.getLog(ExitServletTest.class);

    private String                protocolErrorUrl = "protocol-error";

    private ServletTestManager    exitServletTestManager;

    private String                userid           = UUID.randomUUID().toString();

    private String                target           = "http://test.target";

    private DeviceEntity          device;

    private String                inResponseTo     = "test-in-response-to";

    private String                applicationId    = "test-application-id";

    private JndiTestUtils         jndiTestUtils;

    private Object[]              mockObjects;

    private AuthenticationService mockAuthenticationService;

    private KeyService            mockKeyService;

    private KeyPair               olasKeyPair;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        int validity = 60 * 10;

        mockAuthenticationService = createMock(AuthenticationService.class);
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.USER_AUTHENTICATED);

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        olasKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        mockObjects = new Object[] { mockAuthenticationService };
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        exitServletTestManager = new ServletTestManager();
        Map<String, String> servletInitParams = new HashMap<String, String>();
        servletInitParams.put("ProtocolErrorUrl", protocolErrorUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();

        DeviceClassEntity deviceClass = new DeviceClassEntity(SafeOnlineConstants.PKI_DEVICE_CLASS,
                SafeOnlineConstants.PKI_DEVICE_AUTH_CONTEXT_CLASS);
        device = new DeviceEntity(BeIdConstants.BEID_DEVICE_ID, deviceClass, null, null, null, null, null, null, null, null, null);

        initialSessionAttributes.put(ProtocolHandlerManager.PROTOCOL_HANDLER_ID_ATTRIBUTE, Saml2PostProtocolHandler.class.getName());
        initialSessionAttributes.put(LoginManager.USERID_ATTRIBUTE, userid);
        initialSessionAttributes.put(LoginManager.TARGET_ATTRIBUTE, target);
        initialSessionAttributes.put(LoginManager.APPLICATION_ID_ATTRIBUTE, applicationId);
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, mockAuthenticationService);
        initialSessionAttributes.put(LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE, device);

        exitServletTestManager.setUp(ExitServlet.class, servletInitParams, null, null, initialSessionAttributes);

        String samlResponseToken = AuthnResponseFactory.createAuthResponse(inResponseTo, applicationId, applicationId, userid,
                device.getAuthenticationContextClass(), olasKeyPair, validity, target);
        String encodedSamlResponseToken = org.apache.xml.security.utils.Base64.encode(samlResponseToken.getBytes());
        expect(mockAuthenticationService.finalizeAuthentication()).andStubReturn(encodedSamlResponseToken);

    }

    @After
    public void tearDown()
            throws Exception {

        exitServletTestManager.tearDown();
        jndiTestUtils.tearDown();
        // this.jmxTestUtils.tearDown();
    }

    @Test
    public void saml2Response()
            throws Exception {

        // setup
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(exitServletTestManager.getServletLocation());
        getMethod.setFollowRedirects(false);

        // expectations
        mockAuthenticationService.commitAuthentication("en");

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(getMethod);

        // verify
        verify(mockObjects);
        assertEquals(HttpStatus.SC_OK, statusCode);
        String responseBody = getMethod.getResponseBodyAsString();
        LOG.debug("response body: " + responseBody);

        Document responseDocument = DomTestUtils.parseDocument(responseBody);
        LOG.debug("document element name: " + responseDocument.getDocumentElement().getNodeName());
        Node valueNode = XPathAPI.selectSingleNode(responseDocument, "/:html/:body/:form//:input[@name='SAMLResponse']/@value");
        assertNotNull(valueNode);
        String samlResponseValue = valueNode.getTextContent();
        LOG.debug("SAMLResponse value: " + samlResponseValue);
        String samlResponse = new String(Base64.decodeBase64(samlResponseValue.getBytes()));
        LOG.debug("SAML Response: " + samlResponse);
        File tmpFile = File.createTempFile("saml-response-", ".xml");
        LOG.debug("tmp filename: " + tmpFile.getAbsolutePath());
        IOUtils.write(samlResponse, new FileOutputStream(tmpFile));

        String xmlFilename = tmpFile.getAbsolutePath();
        String pubFilename = FilenameUtils.getFullPath(xmlFilename) + FilenameUtils.getBaseName(xmlFilename) + ".pem";
        PEMWriter writer = new PEMWriter(new FileWriter(pubFilename));
        writer.writeObject(olasKeyPair.getPublic());
        writer.close();

        Document samlResponseDocument = DomTestUtils.parseDocument(samlResponse);
        Element nsElement = samlResponseDocument.createElement("nsElement");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        assertNotNull(XPathAPI.selectSingleNode(samlResponseDocument, "/samlp:Response/ds:Signature", nsElement));
        assertNotNull(XPathAPI.selectSingleNode(samlResponseDocument, "/samlp:Response/saml:Assertion/saml:Subject/saml:NameID", nsElement));
    }
}
