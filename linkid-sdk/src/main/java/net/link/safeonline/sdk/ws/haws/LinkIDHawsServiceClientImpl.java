/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.haws;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.haws._2.HawsServicePort;
import net.lin_k.safe_online.haws._2.PullRequest;
import net.lin_k.safe_online.haws._2.PullResponse;
import net.lin_k.safe_online.haws._2.PushRequest;
import net.lin_k.safe_online.haws._2.PushResponse;
import net.link.safeonline.sdk.api.haws.LinkIDPullErrorCode;
import net.link.safeonline.sdk.api.haws.LinkIDPullException;
import net.link.safeonline.sdk.api.haws.LinkIDPushErrorCode;
import net.link.safeonline.sdk.api.haws.LinkIDPushException;
import net.link.safeonline.sdk.api.ws.haws.LinkIDHawsServiceClient;
import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDAuthnRequestFactory;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.haws.LinkIDHawsServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.saml.SamlUtils;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;


/**
 * Created by wvdhaute
 * Date: 29/01/14
 * Time: 15:47
 */
public class LinkIDHawsServiceClientImpl extends AbstractWSClient<HawsServicePort> implements LinkIDHawsServiceClient<AuthnRequest, Response> {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDHawsServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDHawsServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                       final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDHawsServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDHawsServiceFactory.newInstance().getHawsServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.haws.path" ) ) );

        // bootstrap opensaml2 if needed
        if (Configuration.getParserPool() == null) {
            LinkIDAuthnRequestFactory.bootstrapSaml2();
        }
    }

    @Override
    public String push(final AuthnRequest authnRequest, final String language)
            throws LinkIDPushException {

        PushRequest request = new PushRequest();

        request.setAny( SamlUtils.marshall( authnRequest ) );

        request.setLanguage( language );

        // operate
        PushResponse response = getPort().push( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDPushException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSessionId()) {

            return response.getSessionId();
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public Response pull(final String sessionId)
            throws LinkIDPullException {

        PullRequest request = new PullRequest();
        request.setSessionId( sessionId );

        // operate
        PullResponse response = getPort().pull( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDPullException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            // parse authentication request
            Element authnRequestElement = (Element) response.getSuccess().getAny();
            if (null == authnRequestElement) {
                throw new InternalInconsistencyException( "No SAML v2.0 authentication response found ?!" );
            }

            Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller( authnRequestElement );
            XMLObject responseXMLObject;
            try {
                responseXMLObject = unmarshaller.unmarshall( authnRequestElement );
            }
            catch (UnmarshallingException e) {
                throw new InternalInconsistencyException( "Failed to unmarshall SAML v2.0 authentication response?!", e );
            }

            return (Response) responseXMLObject;
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    private LinkIDPushErrorCode convert(final net.lin_k.safe_online.haws._2.PushErrorCode pushErrorCode) {

        switch (pushErrorCode) {

            case ERROR_REQUEST_INVALID:
                return LinkIDPushErrorCode.ERROR_REQUEST_INVALID;
            case ERROR_UNEXPECTED:
                return LinkIDPushErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDPushErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", pushErrorCode.name() ) );
    }

    private LinkIDPullErrorCode convert(final net.lin_k.safe_online.haws._2.PullErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDPullErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
            case ERROR_UNEXPECTED:
                return LinkIDPullErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDPullErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
