/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.capture;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.capture._2.CaptureRequest;
import net.lin_k.safe_online.capture._2.CaptureResponse;
import net.lin_k.safe_online.capture._2.CaptureServicePort;
import net.link.safeonline.sdk.api.ws.capture.LinkIDCaptureException;
import net.link.safeonline.sdk.api.ws.capture.LinkIDCaptureServiceClient;
import net.link.safeonline.sdk.api.ws.capture.LinkIDErrorCode;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.capture.LinkIDCaptureServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class LinkIDCaptureServiceClientImpl extends AbstractWSClient<CaptureServicePort> implements LinkIDCaptureServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDCaptureServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDCaptureServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                          final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDCaptureServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDCaptureServiceFactory.newInstance().getCaptureServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.capture.path" ) ) );
    }

    @Override
    public void capture(final String orderReference)
            throws LinkIDCaptureException {

        CaptureRequest request = new CaptureRequest();

        request.setOrderReference( orderReference );

        // operate
        CaptureResponse response = getPort().capture( request );

        if (null != response.getError()) {
            throw new LinkIDCaptureException( convert( response.getError().getErrorCode() ) );
        }

        // all good...
    }

    // Helper methods

    private LinkIDErrorCode convert(final net.lin_k.safe_online.capture._2.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CAPTURE_UNKNOWN:
                return LinkIDErrorCode.ERROR_CAPTURE_UNKNOWN;
            case ERROR_CAPTURE_FAILED:
                return LinkIDErrorCode.ERROR_CAPTURE_FAILED;
            case ERROR_CAPTURE_TOKEN_NOT_FOUND:
                return LinkIDErrorCode.ERROR_CAPTURE_TOKEN_NOT_FOUND;
            case ERROR_MAINTENANCE:
                return LinkIDErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
