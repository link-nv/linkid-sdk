/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.BindingProvider;

import net.lin_k.safe_online.session.ApplicationPoolType;
import net.lin_k.safe_online.session.AssertionType;
import net.lin_k.safe_online.session.SessionTrackingPort;
import net.lin_k.safe_online.session.SessionTrackingRequestType;
import net.lin_k.safe_online.session.SessionTrackingResponseType;
import net.lin_k.safe_online.session.SessionTrackingService;
import net.link.safeonline.sdk.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.session.ws.SessionTrackingServiceFactory;
import net.link.safeonline.ws.common.SessionTrackingErrorCode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * <h2>{@link SessionTrackingClientImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SessionTrackingClientImpl extends AbstractMessageAccessor implements SessionTrackingClient {

    private static final long         serialVersionUID = 1L;

    private static final Log          LOG              = LogFactory.getLog(SessionTrackingClientImpl.class);

    private final SessionTrackingPort port;

    private final String              location;


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
    public SessionTrackingClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        SessionTrackingService service = SessionTrackingServiceFactory.newInstance();
        port = service.getSessionTrackingPort();
        this.location = location + "/safe-online-ws/session";

        setEndpointAddress();

        registerMessageLoggerHandler(port);
        WSSecurityClientHandler.addNewHandler(port, clientCertificate, clientPrivateKey);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    /**
     * {@inheritDoc}
     */
    public List<SessionAssertion> getAssertions(String session, String subject, List<String> applicationPools)
            throws WSClientTransportException, ApplicationPoolNotFoundException, SubjectNotFoundException {

        LOG.debug("get assertions: session=" + session + " subject=" + subject);
        SessionTrackingRequestType request = new SessionTrackingRequestType();
        request.setSession(session);
        request.setSubject(subject);
        if (null != applicationPools && !applicationPools.isEmpty()) {
            for (String applicationPool : applicationPools) {
                LOG.debug("application pool: " + applicationPool);
                ApplicationPoolType applicationPoolType = new ApplicationPoolType();
                applicationPoolType.setName(applicationPool);
                request.getApplicationPools().add(applicationPoolType);
            }
        }

        SafeOnlineTrustManager.configureSsl();

        SessionTrackingResponseType response;
        try {
            response = port.getAssertions(request);
        } catch (ClientTransportException e) {
            LOG.debug("Failed to send notification");
            throw new WSClientTransportException(location, e);
        }

        checkStatus(response);

        return getAssertions(response);
    }

    private List<SessionAssertion> getAssertions(SessionTrackingResponseType response) {

        List<SessionAssertion> assertions = new LinkedList<SessionAssertion>();
        for (AssertionType assertionType : response.getAssertions()) {
            assertions.add(new SessionAssertion(assertionType));
        }
        return assertions;
    }

    private void checkStatus(SessionTrackingResponseType response)
            throws ApplicationPoolNotFoundException, SubjectNotFoundException {

        if (response.getStatus().getValue().equals(SessionTrackingErrorCode.SUCCESS.getErrorCode()))
            return;
        else if (response.getStatus().getValue().equals(SessionTrackingErrorCode.APPLICATION_POOL_NOT_FOUND.getErrorCode()))
            throw new ApplicationPoolNotFoundException();
        else if (response.getStatus().equals(SessionTrackingErrorCode.SUBJECT_NOT_FOUND.getErrorCode()))
            throw new SubjectNotFoundException();
    }
}
