/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.SamlpSecondLevelErrorCode;
import net.link.safeonline.ws.common.SamlpTopLevelErrorCode;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of authentication client. This class is using JAX-WS and server-side SSL.
 * 
 * @author wvdhaute
 * 
 */
public class AuthenticationClientImpl extends AbstractMessageAccessor implements AuthenticationClient {

    private static final Log         LOG = LogFactory.getLog(AuthenticationClientImpl.class);

    private final AuthenticationPort port;

    private final String             location;


    /**
     * Main constructor.
     * 
     * @param location
     *            the location (host:port) of the authentication web service.
     */
    public AuthenticationClientImpl(String location, W3CEndpointReference endpoint) {

        AuthenticationService authenticationService = AuthenticationServiceFactory.newInstance();
        this.port = authenticationService.getPort(endpoint, AuthenticationPort.class, new AddressingFeature(true));
        this.location = location + "/safe-online-ws/auth";

        registerMessageLoggerHandler(this.port);

        // TODO: disable logging when finished
        LoggingHandler.addNewHandler(this.port);
        setCaptureMessages(true);
    }

    /**
     * {@inheritDoc}
     */
    public void authenticate(String applicationId, String deviceName, Object deviceCredentials, PublicKey publicKey)
            throws RequestDeniedException, WSClientTransportException, DatatypeConfigurationException {

        LOG.debug("authentication for application " + applicationId + " using device " + deviceName);

        WSAuthenticationRequestType request = getAuthenticationRequest(applicationId, deviceName, deviceCredentials, publicKey);

        SafeOnlineTrustManager.configureSsl();

        WSAuthenticationResponseType response = getResponse(request);

        checkStatus(response);

    }

    @SuppressWarnings("unchecked")
    private WSAuthenticationRequestType getAuthenticationRequest(String applicationId, String deviceName, Object deviceCredentials,
                                                                 PublicKey publicKey)
            throws DatatypeConfigurationException {

        WSAuthenticationRequestType authenticationRequest = new WSAuthenticationRequestType();

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        XMLGregorianCalendar now = getCurrentXmlGregorianCalendar();

        authenticationRequest.setID(id);
        authenticationRequest.setVersion(SAMLVersion.VERSION_20.toString());
        authenticationRequest.setIssueInstant(now);

        // Issuer
        NameIDType issuerName = new NameIDType();
        issuerName.setValue(applicationId);
        authenticationRequest.setIssuer(issuerName);

        authenticationRequest.setApplicationId(applicationId);
        authenticationRequest.setDeviceName(deviceName);

        if (null != deviceCredentials) {
            DeviceCredentialsType deviceCredentialsType = new DeviceCredentialsType();
            if (deviceCredentials instanceof Map) {
                for (Map.Entry<String, String> entry : ((Map<String, String>) deviceCredentials).entrySet()) {
                    NameValuePairType nameValuePair = new NameValuePairType();
                    nameValuePair.setName(entry.getKey());
                    nameValuePair.setValue(entry.getValue());
                    deviceCredentialsType.getNameValuePair().add(nameValuePair);
                }
            } else {
                // XXX: change following, need JAXB object ...
                deviceCredentialsType.getAny().add(deviceCredentials);
            }
            authenticationRequest.setDeviceCredentials(deviceCredentialsType);
        }

        if (null != publicKey) {
            KeyInfoType keyInfo = getKeyInfo(publicKey);
            authenticationRequest.setKeyInfo(keyInfo);
        }

        return authenticationRequest;
    }

    private KeyInfoType getKeyInfo(PublicKey publicKey) {

        KeyInfoType keyInfo = new KeyInfoType();
        ObjectFactory dsigObjectFactory = new ObjectFactory();

        if (publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus(rsaPublicKey.getModulus().toByteArray());
            rsaKeyValue.setExponent(rsaPublicKey.getPublicExponent().toByteArray());
            keyInfo.getContent().add(dsigObjectFactory.createRSAKeyValue(rsaKeyValue));
        } else if (publicKey instanceof DSAPublicKey) {
            DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setY(dsaPublicKey.getY().toByteArray());
            dsaKeyValue.setG(dsaPublicKey.getParams().getG().toByteArray());
            dsaKeyValue.setP(dsaPublicKey.getParams().getP().toByteArray());
            dsaKeyValue.setQ(dsaPublicKey.getParams().getQ().toByteArray());
            keyInfo.getContent().add(dsaKeyValue);
        } else
            throw new IllegalArgumentException("Only RSAPublicKey and DSAPublicKey are supported");

        return keyInfo;
    }

    private XMLGregorianCalendar getCurrentXmlGregorianCalendar()
            throws DatatypeConfigurationException {

        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date now = new Date();
        gregorianCalendar.setTime(now);
        XMLGregorianCalendar currentXmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        return currentXmlGregorianCalendar;
    }

    private WSAuthenticationResponseType getResponse(WSAuthenticationRequestType request)
            throws WSClientTransportException {

        try {
            return this.port.authenticate(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(this.location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(this.port);
        }
    }

    private void checkStatus(WSAuthenticationResponseType response)
            throws RequestDeniedException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        SamlpTopLevelErrorCode samlpTopLevelErrorCode = SamlpTopLevelErrorCode.getSamlpTopLevelErrorCode(statusCodeValue);
        if (SamlpTopLevelErrorCode.SUCCESS != samlpTopLevelErrorCode) {
            LOG.error("status code: " + statusCodeValue);
            LOG.error("status message: " + status.getStatusMessage());
            StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
            if (null != secondLevelStatusCode) {
                String secondLevelStatusCodeValue = secondLevelStatusCode.getValue();
                SamlpSecondLevelErrorCode samlpSecondLevelErrorCode = SamlpSecondLevelErrorCode
                                                                                               .getSamlpTopLevelErrorCode(secondLevelStatusCodeValue);
                if (SamlpSecondLevelErrorCode.REQUEST_DENIED == samlpSecondLevelErrorCode)
                    throw new RequestDeniedException();
                LOG.debug("second level status code: " + secondLevelStatusCode.getValue());
            }
            throw new RuntimeException("error: " + statusCodeValue);
        }
    }
}
