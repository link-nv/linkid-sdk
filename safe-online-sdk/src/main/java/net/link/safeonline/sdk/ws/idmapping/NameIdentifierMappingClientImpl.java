/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.idmapping;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.ws.BindingProvider;

import net.link.safeonline.idmapping.ws.NameIdentifierMappingConstants;
import net.link.safeonline.idmapping.ws.NameIdentifierMappingServiceFactory;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.SamlpSecondLevelErrorCode;
import net.link.safeonline.ws.common.SamlpTopLevelErrorCode;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingRequestType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingService;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of the name identifier mapping interface. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 * 
 * @author fcorneli
 * 
 */
public class NameIdentifierMappingClientImpl extends AbstractMessageAccessor implements NameIdentifierMappingClient {

    private static final Log                LOG = LogFactory.getLog(NameIdentifierMappingClientImpl.class);

    private final NameIdentifierMappingPort port;

    private final String                    location;


    /**
     * Main constructor.
     * 
     * @param location
     *            the location (host:port) of the attribute web service.
     * @param clientCertificate
     *            the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey
     *            the private key corresponding with the client certificate.
     */
    public NameIdentifierMappingClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        NameIdentifierMappingService service = NameIdentifierMappingServiceFactory.newInstance();
        port = service.getNameIdentifierMappingPort();
        this.location = location + "/safe-online-ws/idmapping";
        setEndpointAddress();

        registerMessageLoggerHandler(port);
        WSSecurityClientHandler.addNewHandler(port, clientCertificate, clientPrivateKey);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    public String getUserId(String username)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException {

        LOG.debug("getUserId: " + username);

        NameIDMappingRequestType request = new NameIDMappingRequestType();
        NameIDType nameId = new NameIDType();
        nameId.setValue(username);
        NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
        nameIdPolicy.setFormat(NameIdentifierMappingConstants.NAMEID_FORMAT_PERSISTENT);
        request.setNameIDPolicy(nameIdPolicy);
        request.setNameID(nameId);

        SafeOnlineTrustManager.configureSsl();

        NameIDMappingResponseType response;
        try {
            response = port.nameIdentifierMappingQuery(request);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location, e);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String errorCode = statusCode.getValue();
        SamlpTopLevelErrorCode topLevelErrorCode = SamlpTopLevelErrorCode.getSamlpTopLevelErrorCode(errorCode);
        if (SamlpTopLevelErrorCode.SUCCESS != topLevelErrorCode) {
            // throw new RuntimeException("error occured on identifier mapping
            // service");
            LOG.error("status code: " + statusCode.getValue());
            LOG.error("status message: " + status.getStatusMessage());
            StatusCodeType secondStatusCode = statusCode.getStatusCode();
            if (null != secondStatusCode) {
                String secondErrorCode = secondStatusCode.getValue();
                SamlpSecondLevelErrorCode secondLevelErrorCode = SamlpSecondLevelErrorCode.getSamlpTopLevelErrorCode(secondErrorCode);
                if (SamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == secondLevelErrorCode)
                    throw new SubjectNotFoundException();
                if (SamlpSecondLevelErrorCode.REQUEST_DENIED == secondLevelErrorCode)
                    throw new RequestDeniedException();
                throw new RuntimeException("error occurred on identifier mapping service: " + secondErrorCode);
            }
        }

        NameIDType responseNameId = response.getNameID();

        String userId = responseNameId.getValue();
        return userId;
    }
}
