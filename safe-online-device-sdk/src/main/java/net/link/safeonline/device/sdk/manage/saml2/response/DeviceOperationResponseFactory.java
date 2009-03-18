/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.manage.saml2.response;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import net.link.safeonline.device.sdk.manage.saml2.DeviceOperationType;
import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory for SAML2 authentication responses.
 * 
 * @author fcorneli
 * 
 */
public class DeviceOperationResponseFactory {

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
            Configuration.registerObjectProvider(DeviceOperationResponse.DEFAULT_ELEMENT_NAME, new DeviceOperationResponseBuilder(),
                    new DeviceOperationResponseMarshaller(), new DeviceOperationResponseUnmarshaller(), null);
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    private DeviceOperationResponseFactory() {

        // empty
    }

    /**
     * Creates a signed device operation response with status SUCCESS.
     * 
     * @param deviceOperation
     *            The device operation executed
     */
    public static String createDeviceOperationResponse(String inResponseTo, DeviceOperationType deviceOperation, String issuerName,
                                                       String subjectName, String device, KeyPair signerKeyPair, int validity, String target) {

        return createDeviceOperationResponse(inResponseTo, deviceOperation, issuerName, subjectName, device, signerKeyPair, validity,
                target, StatusCode.SUCCESS_URI);
    }

    /**
     * Creates a signed device operation response with status failed.
     */
    public static String createDeviceOperationResponseFailed(String inResponseTo, DeviceOperationType deviceOperation, String issuerName,
                                                             String subjectName, String device, KeyPair signerKeyPair, int validity,
                                                             String target) {

        return createDeviceOperationResponse(inResponseTo, deviceOperation, issuerName, subjectName, device, signerKeyPair, validity,
                target, DeviceOperationResponse.FAILED_URI);
    }

    /**
     * Creates a signed authentication response with status unsupported.
     */
    public static String createDeviceOperationResponseUnsupported(String inResponseTo, DeviceOperationType deviceOperation,
                                                                  String issuerName, String subjectName, String device,
                                                                  KeyPair signerKeyPair, int validity, String target) {

        return createDeviceOperationResponse(inResponseTo, deviceOperation, issuerName, subjectName, device, signerKeyPair, validity,
                target, StatusCode.REQUEST_UNSUPPORTED_URI);
    }

    private static String createDeviceOperationResponse(String inResponseTo, DeviceOperationType deviceOperation, String issuerName,
                                                        String subjectName, String device, KeyPair signerKeyPair, int validity,
                                                        String target, String statusCodeURI) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");
        if (null == deviceOperation)
            throw new IllegalArgumentException("deviceOperation should not be null");
        if (null == device)
            throw new IllegalArgumentException("device should not be null");
        if (null == subjectName)
            throw new IllegalArgumentException("subjectName should not be null");

        DeviceOperationResponse response = Saml2Util.buildXMLObject(DeviceOperationResponse.class,
                DeviceOperationResponse.DEFAULT_ELEMENT_NAME);

        DateTime now = new DateTime();

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        response.setID(idGenerator.generateIdentifier());
        response.setVersion(SAMLVersion.VERSION_20);
        response.setInResponseTo(inResponseTo);
        response.setIssueInstant(now);

        Issuer responseIssuer = Saml2Util.buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setValue(issuerName);
        response.setIssuer(responseIssuer);

        response.setDestination(target);
        response.setDeviceOperation(deviceOperation.name());
        response.setDevice(device);
        response.setSubjectName(subjectName);

        Status status = Saml2Util.buildXMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = Saml2Util.buildXMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(statusCodeURI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        if (statusCodeURI.equals(StatusCode.SUCCESS_URI) && deviceOperation.equals(DeviceOperationType.NEW_ACCOUNT_REGISTER)) {
            Assertion assertion = Saml2Util.getAssertion(inResponseTo, null, subjectName, issuerName, device, validity, target,
                    new DateTime(), Saml2SubjectConfirmationMethod.BEARER, null);
            response.getAssertions().add(assertion);
        }

        return Saml2Util.sign(response, signerKeyPair);
    }
}
