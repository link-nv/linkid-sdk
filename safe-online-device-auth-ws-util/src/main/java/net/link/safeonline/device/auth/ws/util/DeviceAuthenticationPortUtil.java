/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.auth.ws.util;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.EJBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;


/**
 * <h2>{@link DeviceAuthenticationPortUtil}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * Utility class for the device's auth-ws implementation.
 * </p>
 * 
 * <p>
 * <i>Jan 7, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceAuthenticationPortUtil {

    public static WSAuthenticationResponseType generateResponse(String id, String issuerName, String deviceName) {

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new EJBException("datatype config error");
        }

        WSAuthenticationResponseType response = new WSAuthenticationResponseType();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date());
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }

        // Issuer
        NameIDType nameIDType = new NameIDType();
        nameIDType.setValue(issuerName);

        response.setID(idGenerator.generateIdentifier());
        response.setVersion(SAMLVersion.VERSION_20.toString());
        response.setIssueInstant(now);
        response.setInResponseTo(id);
        response.setIssuer(nameIDType);
        response.setDeviceName(deviceName);

        return response;
    }

    public static void setStatus(WSAuthenticationResponseType response, WSAuthenticationErrorCode code, String message) {

        StatusType status = new StatusType();
        StatusCodeType statusCode = new StatusCodeType();
        statusCode.setValue(code.getErrorCode());
        status.setStatusCode(statusCode);
        status.setStatusMessage(message);
        response.setStatus(status);
    }
}
