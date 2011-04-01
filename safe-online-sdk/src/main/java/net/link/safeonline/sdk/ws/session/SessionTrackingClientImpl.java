/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import com.sun.xml.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.session.*;
import net.link.safeonline.sdk.logging.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.SessionTrackingErrorCode;
import net.link.safeonline.session.ws.SessionTrackingServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
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
public class SessionTrackingClientImpl extends AbstractWSClient<SessionTrackingPort> implements SessionTrackingClient {

    private static final Log LOG = LogFactory.getLog( SessionTrackingClientImpl.class );

    private final String location;


    /**
     * Main constructor.
     *
     * @param location the location (host:port) of the attribute web service.
     * @param sslCertificate If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     * @param configuration
     */
    public SessionTrackingClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super(SessionTrackingServiceFactory.newInstance().getSessionTrackingPort() );
        getBindingProvider().getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = location + "/session" );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
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
            response = getPort().getAssertions( request );
        } catch (ClientTransportException e) {
            LOG.debug( "Failed to send notification" );
            throw new WSClientTransportException( getBindingProvider(), e );
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
