/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.capture;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.capture.CaptureRequest;
import net.lin_k.safe_online.capture.CaptureResponse;
import net.lin_k.safe_online.capture.CaptureServicePort;
import net.link.safeonline.sdk.api.ws.capture.CaptureException;
import net.link.safeonline.sdk.api.ws.capture.CaptureServiceClient;
import net.link.safeonline.sdk.api.ws.capture.ErrorCode;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.capture.CaptureServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class CaptureServiceClientImpl extends AbstractWSClient<CaptureServicePort> implements CaptureServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public CaptureServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public CaptureServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                    final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private CaptureServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( CaptureServiceFactory.newInstance().getCaptureServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.capture.path" ) ) );
    }

    @Override
    public void capture(final String orderReference)
            throws CaptureException {

        CaptureRequest request = new CaptureRequest();

        request.setOrderReference( orderReference );

        // operate
        CaptureResponse response = getPort().capture( request );

        if (null != response.getError()) {
            throw new CaptureException( convert( response.getError().getErrorCode() ) );
        }

        // all good...
    }

    // Helper methods

    private ErrorCode convert(final net.lin_k.safe_online.capture.ErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_CAPTURE_UNKNOWN:
                return ErrorCode.ERROR_CAPTURE_UNKNOWN;
            case ERROR_CAPTURE_FAILED:
                return ErrorCode.ERROR_CAPTURE_FAILED;
            case ERROR_CAPTURE_TOKEN_NOT_FOUND:
                return ErrorCode.ERROR_CAPTURE_TOKEN_NOT_FOUND;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
