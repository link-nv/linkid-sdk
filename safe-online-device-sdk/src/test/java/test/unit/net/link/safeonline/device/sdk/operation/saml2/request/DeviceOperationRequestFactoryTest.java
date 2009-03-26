/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.device.sdk.operation.saml2.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.util.Collections;
import java.util.UUID;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequest;
import net.link.safeonline.device.sdk.operation.saml2.request.DeviceOperationRequestFactory;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestAbstractType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Unit test for device operation request factory.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationRequestFactoryTest {

    private static final Log LOG = LogFactory.getLog(DeviceOperationRequestFactoryTest.class);


    @Test
    public void createDeviceOperationAuthnRequest()
            throws Exception {

        // setup
        String nodeName = "test-node-name";
        String subject = UUID.randomUUID().toString();
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        String serviceURL = "http://test.consumer.service";
        Challenge<String> challenge = new Challenge<String>();
        String destinationURL = "https://test.idp.com/entry";
        String device = "device";
        String authenticatedDevice = "authenticated-device";
        DeviceOperationType deviceOperation = DeviceOperationType.REGISTER;

        // operate
        long begin = System.currentTimeMillis();
        String result = DeviceOperationRequestFactory.createDeviceOperationRequest(nodeName, subject, keyPair, serviceURL, destinationURL,
                deviceOperation, challenge, device, Collections.singletonList(authenticatedDevice), null, null);
        long end = System.currentTimeMillis();

        // verify
        assertNotNull(result);
        LOG.debug("duration: " + (end - begin) + " ms");
        LOG.debug("result message: " + result);
        File tmpFile = File.createTempFile("saml-device-operation-request-", ".xml");
        FileOutputStream tmpOutput = new FileOutputStream(tmpFile);
        IOUtils.write(result, tmpOutput);
        IOUtils.closeQuietly(tmpOutput);

        String challengeValue = challenge.getValue();
        LOG.debug("challenge value: " + challengeValue);
        assertNotNull(challengeValue);

        Document resultDocument = DomTestUtils.parseDocument(result);

        Element nsElement = createNsElement(resultDocument);
        Element deviceOperationRequestElement = (Element) XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX
                + ":" + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME, nsElement);
        assertNotNull(deviceOperationRequestElement);

        Element issuerElement = (Element) XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/" + SAMLConstants.SAML20_PREFIX + ":"
                + Issuer.DEFAULT_ELEMENT_LOCAL_NAME, nsElement);
        assertNotNull(issuerElement);
        assertEquals(nodeName, issuerElement.getTextContent());

        Node resultServiceURLNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationRequest.SERVICE_URL_ATTRIB_NAME, nsElement);
        assertNotNull(resultServiceURLNode);
        assertEquals(serviceURL, resultServiceURLNode.getTextContent());

        Node protocolBindingNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationRequest.PROTOCOL_BINDING_ATTRIB_NAME,
                nsElement);
        assertNotNull(protocolBindingNode);
        assertEquals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", protocolBindingNode.getTextContent());

        Node destinationNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + RequestAbstractType.DESTINATION_ATTRIB_NAME, nsElement);
        assertNotNull(destinationNode);
        assertEquals(destinationURL, destinationNode.getTextContent());

        Node deviceOperationNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationRequest.DEVICE_OPERATION_ATTRIB_NAME,
                nsElement);
        assertNotNull(deviceOperationNode);
        assertEquals(deviceOperation.name(), deviceOperationNode.getTextContent());

        Node deviceNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationRequest.DEVICE_ATTRIB_NAME, nsElement);
        assertNotNull(deviceNode);
        assertEquals(device, deviceNode.getTextContent());

        // verify signature
        NodeList signatureNodeList = resultDocument.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());

        DOMValidateContext validateContext = new DOMValidateContext(keyPair.getPublic(), signatureNodeList.item(0));
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM", new org.jcp.xml.dsig.internal.dom.XMLDSigRI());

        XMLSignature signature = signatureFactory.unmarshalXMLSignature(validateContext);
        boolean resultValidity = signature.validate(validateContext);
        assertTrue(resultValidity);

        Element dsNsElement = resultDocument.createElement("nsElement");
        dsNsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        XObject xObject = XPathAPI.eval(resultDocument, "count(//ds:Reference)", dsNsElement);
        LOG.debug("count: " + xObject.num());
        assertEquals(1.0, xObject.num(), 0);
    }

    private Element createNsElement(Document document) {

        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:" + SAMLConstants.SAML20P_PREFIX, SAMLConstants.SAML20P_NS);
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:" + SAMLConstants.SAML20_PREFIX, SAMLConstants.SAML20_NS);
        return nsElement;
    }
}
