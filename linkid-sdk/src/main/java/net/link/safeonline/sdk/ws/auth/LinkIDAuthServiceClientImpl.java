/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.auth.AuthServicePort;
import net.lin_k.safe_online.auth.CancelErrorCode;
import net.lin_k.safe_online.auth.CancelRequest;
import net.lin_k.safe_online.auth.CancelResponse;
import net.lin_k.safe_online.auth.PollErrorCode;
import net.lin_k.safe_online.auth.PollRequest;
import net.lin_k.safe_online.auth.PollResponse;
import net.lin_k.safe_online.auth.StartErrorCode;
import net.lin_k.safe_online.auth.StartRequest;
import net.lin_k.safe_online.auth.StartResponse;
import net.link.safeonline.sdk.api.payment.LinkIDPaymentState;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthServiceClient;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthenticationState;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnErrorCode;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDAuthnSession;
import net.link.safeonline.sdk.api.ws.auth.LinkIDCancelErrorCode;
import net.link.safeonline.sdk.api.ws.auth.LinkIDCancelException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollErrorCode;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollException;
import net.link.safeonline.sdk.api.ws.auth.LinkIDPollResponse;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.auth.LinkIDAuthServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.saml.SamlUtils;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
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
public class LinkIDAuthServiceClientImpl extends AbstractWSClient<AuthServicePort> implements LinkIDAuthServiceClient<AuthnRequest, Response> {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDAuthServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDAuthServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                       final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDAuthServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDAuthServiceFactory.newInstance().getAuthServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.auth.path" ) ) );
    }

    @Override
    public LinkIDAuthnSession start(final AuthnRequest authnRequest, final String language, final String userAgent, final boolean forceRegistration)
            throws LinkIDAuthnException {

        StartRequest request = new StartRequest();

        request.setAny( SamlUtils.marshall( authnRequest ) );

        request.setLanguage( language );
        request.setUserAgent( userAgent );
        request.setForceRegistration( forceRegistration );

        // operate
        StartResponse response = getPort().start( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDAuthnException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            // convert base64 encoded QR image
            byte[] qrCodeImage;
            try {
                qrCodeImage = Base64.decode( response.getSuccess().getEncodedQRCode() );
            }
            catch (Base64DecodingException e) {
                throw new InternalInconsistencyException( "Could not decode the QR image!" );
            }

            return new LinkIDAuthnSession( response.getSuccess().getSessionId(), qrCodeImage, response.getSuccess().getEncodedQRCode(),
                    response.getSuccess().getQrCodeURL() );
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public LinkIDPollResponse<Response> poll(final String sessionId, final String language)
            throws LinkIDPollException {

        PollRequest request = new PollRequest();

        request.setSessionId( sessionId );
        request.setLanguage( language );

        // operate
        PollResponse response = getPort().poll( request );

        // convert response
        if (null != response.getError()) {
            throw new LinkIDPollException( convert( response.getError().getError() ), response.getError().getInfo() );
        }

        if (null != response.getSuccess()) {

            // authenticate state
            LinkIDAuthenticationState linkIDAuthenticationState = LinkIDConversionUtils.convert( response.getSuccess().getAuthenticationState() );

            LinkIDPaymentState paymentState = null;
            if (null != response.getSuccess().getPaymentState()) {
                paymentState = LinkIDConversionUtils.convert( response.getSuccess().getPaymentState() );
            }

            String paymentMenuURL = response.getSuccess().getPaymentMenuURL();

            // parse authentication request
            XMLObject responseXMLObject = null;
            if (null != response.getSuccess().getAuthenticationResponse()) {
                Element authnResponseElement = (Element) response.getSuccess().getAuthenticationResponse().getAny();
                if (null != authnResponseElement) {
                    Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller( authnResponseElement );
                    try {
                        responseXMLObject = unmarshaller.unmarshall( authnResponseElement );
                    }
                    catch (UnmarshallingException e) {
                        throw new InternalInconsistencyException( "Failed to unmarshall SAML v2.0 authentication response?!", e );
                    }
                }
            }

            return new LinkIDPollResponse<Response>( linkIDAuthenticationState, paymentState, paymentMenuURL, (Response) responseXMLObject );
        }

        throw new InternalInconsistencyException( "No sessionId nor error element in the response ?!" );
    }

    @Override
    public void cancel(final String sessionId)
            throws LinkIDCancelException {

        CancelRequest request = new CancelRequest();
        request.setSessionId( sessionId );

        // operate
        CancelResponse response = getPort().cancel( request );

        if (null != response.getError()) {
            throw new LinkIDCancelException( convert( response.getError().getError() ), response.getError().getInfo() );
        }
    }

    private LinkIDAuthnErrorCode convert(final StartErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_REQUEST_INVALID:
                return LinkIDAuthnErrorCode.ERROR_REQUEST_INVALID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDPollErrorCode convert(final PollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_RESPONSE_INVALID_SESSION_ID:
                return LinkIDPollErrorCode.ERROR_RESPONSE_INVALID_SESSION_ID;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDCancelErrorCode convert(final CancelErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_INVALID_SESSION_ID:
                return LinkIDCancelErrorCode.ERROR_INVALID_SESSION_ID;
            case ERROR_PERMISSION_DENIED:
                return LinkIDCancelErrorCode.ERROR_PERMISSION_DENIED;
            case ERROR_UNEXPECTED:
                return LinkIDCancelErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
