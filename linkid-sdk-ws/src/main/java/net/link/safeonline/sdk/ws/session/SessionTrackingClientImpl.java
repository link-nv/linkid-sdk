/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.session;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.sun.xml.internal.ws.client.ClientTransportException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.session.*;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.SessionTrackingErrorCode;
import net.link.safeonline.sdk.api.ws.session.SessionAssertion;
import net.link.safeonline.sdk.api.ws.session.client.SessionTrackingClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.session.SessionTrackingServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


/**
 * <h2>{@link SessionTrackingClientImpl}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public class SessionTrackingClientImpl extends AbstractWSClient<SessionTrackingPort> implements SessionTrackingClient {

    private static final Logger logger = Logger.get( SessionTrackingClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public SessionTrackingClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( SessionTrackingServiceFactory.newInstance().getSessionTrackingPort(), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.session.tracking.path" ) ) );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    @Override
    public List<SessionAssertion> getAssertions(String session, String subject, List<String> applicationPools)
            throws WSClientTransportException, ApplicationPoolNotFoundException, SubjectNotFoundException, RequestDeniedException {

        logger.dbg( "get assertions: session=%s subject=%s", session, subject );
        SessionTrackingRequestType request = new SessionTrackingRequestType();
        request.setSession( session );
        request.setSubject( subject );
        if (null != applicationPools && !applicationPools.isEmpty())
            for (String applicationPool : applicationPools) {
                logger.dbg( "application pool: %s", applicationPool );
                ApplicationPoolType applicationPoolType = new ApplicationPoolType();
                applicationPoolType.setName( applicationPool );
                request.getApplicationPools().add( applicationPoolType );
            }

        SessionTrackingResponseType response;
        try {
            response = getPort().getAssertions( request );
        }
        catch (ClientTransportException e) {
            logger.dbg( "Failed to send notification" );
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( response );

        return getAssertions( response );
    }

    private static List<SessionAssertion> getAssertions(SessionTrackingResponseType response) {

        List<SessionAssertion> assertions = new LinkedList<SessionAssertion>();
        for (AssertionType assertionType : response.getAssertions())
            assertions.add( toSessionAssertion( assertionType ) );
        return assertions;
    }

    private static SessionAssertion toSessionAssertion(AssertionType assertion) {

        Map<Date, String> authentications = new HashMap<Date, String>();
        for (AuthnStatementType statement : assertion.getAuthnStatement())
            authentications.put( statement.getTime().toGregorianCalendar().getTime(), statement.getDevice() );

        return new SessionAssertion( assertion.getSubject(), assertion.getApplicationPool(), authentications );
    }

    private static void validateStatus(SessionTrackingResponseType response)
            throws ApplicationPoolNotFoundException, SubjectNotFoundException, RequestDeniedException {

        if (response.getStatus().getValue().equals( SessionTrackingErrorCode.SUCCESS.getErrorCode() )) {
        } else if (response.getStatus().getValue().equals( SessionTrackingErrorCode.APPLICATION_POOL_NOT_FOUND.getErrorCode() ))
            throw new ApplicationPoolNotFoundException();
        else if (response.getStatus().getValue().equals( SessionTrackingErrorCode.SUBJECT_NOT_FOUND.getErrorCode() ))
            throw new SubjectNotFoundException();
        else if (response.getStatus().getValue().equals( SessionTrackingErrorCode.TRUSTED_DEVICE_NOT_FOUND.getErrorCode() ))
            throw new RequestDeniedException();
    }
}
