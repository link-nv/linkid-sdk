/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2.request;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory class for Device Operation requests.
 * 
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationRequestFactory {

    private DeviceOperationRequestFactory() {

        // empty
    }


    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
            Configuration.registerObjectProvider(DeviceOperationRequest.DEFAULT_ELEMENT_NAME, new DeviceOperationRequestBuilder(),
                    new DeviceOperationRequestMarshaller(), new DeviceOperationRequestUnmarshaller(), null);
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    /**
     * Creates a SAML2 device operation request. This request will contain a Subject element, containing the OLAS user ID on the destination
     * device node. It will also contain the device with which the user has authenticated before issuing this device operation request.
     * 
     * The request can also optionally contain the device user attribute to specify the specific device registration for this device
     * operation.
     * 
     * 
     * @param issuerName
     * @param subjectName
     *            the subject name which wants to execute a device operation ( register/removal/update/disable ). This is OLAS user ID on
     *            the destination device node.
     * @param signerKeyPair
     * @param serviceURL
     *            the location of the service that will handle the response message.
     * @param destinationURL
     *            the location of the destination IdP.
     * @param deviceOperation
     *            the requested device operation.
     * @param challenge
     *            the challenge (output variable).
     * @param device
     * @param attribute
     */
    public static String createDeviceOperationRequest(String issuerName, String subjectName, KeyPair signerKeyPair, String serviceURL,
                                                      String destinationURL, DeviceOperationType deviceOperation,
                                                      Challenge<String> challenge, String device, String authenticatedDevice,
                                                      String attribute) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");
        if (null == destinationURL)
            throw new IllegalArgumentException("destination url should not be null");
        if (null == deviceOperation)
            throw new IllegalArgumentException("device operation should not be null");
        if (null == serviceURL)
            throw new IllegalArgumentException("service url should not be null");
        if (null == device)
            throw new IllegalArgumentException("device should not be null");

        DeviceOperationRequest request = Saml2Util
                                                  .buildXMLObject(DeviceOperationRequest.class, DeviceOperationRequest.DEFAULT_ELEMENT_NAME);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        request.setID(id);
        if (null != challenge) {
            challenge.setValue(id);
        }
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(new DateTime());
        Issuer issuer = Saml2Util.buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerName);
        request.setIssuer(issuer);

        Subject subject = Saml2Util.buildXMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = Saml2Util.buildXMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(subjectName);
        nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        subject.setNameID(nameID);
        request.setSubject(subject);

        request.setServiceURL(serviceURL);
        request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        request.setDestination(destinationURL);
        request.setDeviceOperation(deviceOperation.name());
        request.setDevice(device);
        request.setAuthenticatedDevice(authenticatedDevice);
        request.setAttribute(attribute);

        return Saml2Util.sign(request, signerKeyPair);
    }
}
