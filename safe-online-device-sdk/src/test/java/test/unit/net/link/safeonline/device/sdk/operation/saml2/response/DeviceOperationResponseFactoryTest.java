package test.unit.net.link.safeonline.device.sdk.operation.saml2.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponse;
import net.link.safeonline.device.sdk.operation.saml2.response.DeviceOperationResponseFactory;
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
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DeviceOperationResponseFactoryTest {

    private static final Log LOG = LogFactory.getLog(DeviceOperationResponseFactoryTest.class);


    @Test
    public void createDeviceOperationResponse()
            throws Exception {

        // setup
        String inResponseTo = "id-in-response-to-test-id";
        String issuerName = "test-issuer-name";
        String subjectName = "test-subject-name";
        int validity = 60 * 10;
        String target = "https://sp.test.com";
        String device = "device";
        DeviceOperationType deviceOperation = DeviceOperationType.REGISTER;
        KeyPair signerKeyPair = PkiTestUtils.generateKeyPair();

        // operate
        long begin = System.currentTimeMillis();
        String result = DeviceOperationResponseFactory.createDeviceOperationResponse(inResponseTo, deviceOperation, issuerName,
                subjectName, device, signerKeyPair, validity, target);
        long end = System.currentTimeMillis();

        // verify
        assertNotNull(result);
        LOG.debug("duration: " + (end - begin) + " ms");
        LOG.debug("result message: " + result);
        File tmpFile = File.createTempFile("saml-device-operation-response-", ".xml");
        FileOutputStream tmpOutput = new FileOutputStream(tmpFile);
        IOUtils.write(result, tmpOutput);
        IOUtils.closeQuietly(tmpOutput);

        Document resultDocument = DomTestUtils.parseDocument(result);

        Element nsElement = createNsElement(resultDocument);
        Element deviceOperationResponseElement = (Element) XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX
                + ":" + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME, nsElement);
        assertNotNull(deviceOperationResponseElement);

        Node inResponseToNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME + "/@" + StatusResponseType.IN_RESPONSE_TO_ATTRIB_NAME, nsElement);
        assertNotNull(inResponseToNode);
        assertEquals(inResponseTo, inResponseToNode.getTextContent());

        Node issuerNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME + "/" + SAMLConstants.SAML20_PREFIX + ":"
                + Issuer.DEFAULT_ELEMENT_LOCAL_NAME, nsElement);
        assertNotNull(issuerNode);
        assertEquals(issuerName, issuerNode.getTextContent());

        Node deviceOperationNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationResponse.DEVICE_OPERATION_ATTRIB_NAME,
                nsElement);
        assertNotNull(deviceOperationNode);
        assertEquals(deviceOperation.name(), deviceOperationNode.getTextContent());

        Node deviceNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME + "/@ " + DeviceOperationResponse.DEVICE_ATTRIB_NAME, nsElement);
        assertNotNull(deviceNode);
        assertEquals(device, deviceNode.getTextContent());

        Node statusNode = XPathAPI.selectSingleNode(resultDocument, "/" + SAMLConstants.SAML20P_PREFIX + ":"
                + DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME + "/" + SAMLConstants.SAML20P_PREFIX + ":Status/"
                + SAMLConstants.SAML20P_PREFIX + ":" + StatusCode.DEFAULT_ELEMENT_LOCAL_NAME + "/@" + StatusCode.VALUE_ATTRIB_NAME);
        assertNotNull(statusNode);
        assertEquals(StatusCode.SUCCESS_URI, statusNode.getTextContent());

        // verify signature
        NodeList signatureNodeList = resultDocument.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");
        assertEquals(1, signatureNodeList.getLength());

        DOMValidateContext validateContext = new DOMValidateContext(signerKeyPair.getPublic(), signatureNodeList.item(0));
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
