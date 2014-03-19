/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.haws;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.haws.HawsServicePort;
import net.lin_k.safe_online.haws.PullRequest;
import net.lin_k.safe_online.haws.PullResponse;
import net.lin_k.safe_online.haws.PushRequestV2;
import net.lin_k.safe_online.haws.PushResponse;
import net.link.safeonline.sdk.api.haws.PullErrorCode;
import net.link.safeonline.sdk.api.haws.PullException;
import net.link.safeonline.sdk.api.haws.PushErrorCode;
import net.link.safeonline.sdk.api.haws.PushException;
import net.link.safeonline.sdk.api.ws.haws.HawsServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.haws.HawsServiceFactory;
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
public class HawsServiceClientImpl extends AbstractWSClient<HawsServicePort> implements HawsServiceClient<AuthnRequest, Response> {

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public HawsServiceClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        this( location, sslCertificate );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the ltqr web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public HawsServiceClientImpl(final String location, final X509Certificate sslCertificate, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificate );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private HawsServiceClientImpl(final String location, final X509Certificate sslCertificate) {

        super( HawsServiceFactory.newInstance().getHawsServicePort(), sslCertificate );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.haws.path" ) ) );
    }

    @Override
    public String push(final AuthnRequest authnRequest, final String language)
            throws PushException {

        PushRequestV2 request = new PushRequestV2();

        request.setAny( SamlUtils.marshall( authnRequest ) );

        request.setLanguage( language );

        // operate
        PushResponse response = getPort().pushV2( request );

        // convert response
        if (null != response.getError()) {
            throw new PushException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSessionId()) {

            return response.getSessionId();
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public Response pull(final String sessionId)
            throws PullException {

        PullRequest request = new PullRequest();
        request.setSessionId( sessionId );

        // operate
        PullResponse response = getPort().pull( request );

        // convert response
        if (null != response.getError()) {
            throw new PullException( convert( response.getError().getError() ), response.getError().getInfo() );
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

    private PushErrorCode convert(final net.lin_k.safe_online.haws.PushErrorCode pushErrorCode) {

        switch (pushErrorCode) {

            case ERROR_REQUEST_INVALID:
                return PushErrorCode.ERROR_REQUEST_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", pushErrorCode.name() ) );
    }

    private PullErrorCode convert(final net.lin_k.safe_online.haws.PullErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return PullErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
