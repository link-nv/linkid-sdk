/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.session.*;
import net.link.safeonline.sdk.ws.SessionTrackingErrorCode;
import net.link.safeonline.sdk.logging.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import net.link.safeonline.session.ws.SessionTrackingServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link SessionTrackingClientImpl}</h2>
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
public class SessionTrackingClientImpl extends AbstractWSClient implements SessionTrackingClient {

    private static final Log LOG = LogFactory.getLog( SessionTrackingClientImpl.class );

    private final SessionTrackingPort port;

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param clientCertificate the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey the private key corresponding with the client certificate.
     * @param serverCertificate the X509 certificate of the server
     * @param maxOffset the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *            {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public SessionTrackingClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                                     X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        SessionTrackingService service = SessionTrackingServiceFactory.newInstance();
        port = service.getSessionTrackingPort();
        this.location = location + "/session";

        setEndpointAddress();

        registerMessageLoggerHandler( port );

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    /**
     * {@inheritDoc}
     */
    public List<SessionAssertion> getAssertions(String session, String subject, List<String> applicationPools)
            throws WSClientTransportException, ApplicationPoolNotFoundException, SubjectNotFoundException, RequestDeniedException {

        LOG.debug( "get assertions: session=" + session + " subject=" + subject );
        SessionTrackingRequestType request = new SessionTrackingRequestType();
        request.setSession( session );
        request.setSubject( subject );
        if (null != applicationPools && !applicationPools.isEmpty())
            for (String applicationPool : applicationPools) {
                LOG.debug( "application pool: " + applicationPool );
                ApplicationPoolType applicationPoolType = new ApplicationPoolType();
                applicationPoolType.setName( applicationPool );
                request.getApplicationPools().add( applicationPoolType );
            }

        SessionTrackingResponseType response;
        try {
            response = port.getAssertions( request );
        } catch (ClientTransportException e) {
            LOG.debug( "Failed to send notification" );
            throw new WSClientTransportException( location, e );
        }

        checkStatus( response );

        return getAssertions( response );
    }

    private List<SessionAssertion> getAssertions(SessionTrackingResponseType response) {

        List<SessionAssertion> assertions = new LinkedList<SessionAssertion>();
        for (AssertionType assertionType : response.getAssertions())
            assertions.add( new SessionAssertion( assertionType ) );
        return assertions;
    }

    private void checkStatus(SessionTrackingResponseType response)
            throws ApplicationPoolNotFoundException, SubjectNotFoundException, RequestDeniedException {

        if (response.getStatus().getValue().equals( SessionTrackingErrorCode.SUCCESS.getErrorCode() ))
            return;
        else if (response.getStatus().getValue().equals( SessionTrackingErrorCode.APPLICATION_POOL_NOT_FOUND.getErrorCode() ))
            throw new ApplicationPoolNotFoundException();
        else if (response.getStatus().equals( SessionTrackingErrorCode.SUBJECT_NOT_FOUND.getErrorCode() ))
            throw new SubjectNotFoundException();
        else if (response.getStatus().equals( SessionTrackingErrorCode.TRUSTED_DEVICE_NOT_FOUND.getErrorCode() ))
            throw new RequestDeniedException();
    }
}
