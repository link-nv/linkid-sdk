/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.wallet;

import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.wallet.WalletAddCreditRequest;
import net.lin_k.safe_online.wallet.WalletAddCreditResponse;
import net.lin_k.safe_online.wallet.WalletEnrollRequest;
import net.lin_k.safe_online.wallet.WalletEnrollResponse;
import net.lin_k.safe_online.wallet.WalletServicePort;
import net.link.safeonline.sdk.api.payment.Currency;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditException;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditResult;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollException;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollResult;
import net.link.safeonline.sdk.api.ws.wallet.WalletServiceClient;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.wallet.WalletServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class WalletServiceClientImpl extends AbstractWSClient<WalletServicePort> implements WalletServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public WalletServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public WalletServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                   final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private WalletServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( WalletServiceFactory.newInstance().getWalletServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.wallet.path" ) ) );
    }

    @Override
    public WalletEnrollResult enroll(final List<String> userIds, final String walletId, final double amount, final Currency currency)
            throws WalletEnrollException {

        //request
        WalletEnrollRequest request = new WalletEnrollRequest();

        // inout
        request.getUserIds().addAll( userIds );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( SDKUtils.convert( currency ) );

        // operate
        WalletEnrollResponse response = getPort().enroll( request );

        // response
        if (null != response.getError()) {

            throw new WalletEnrollException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return new WalletEnrollResult( response.getSuccess().getUnknownUsers(), response.getSuccess().getAlreadyEnrolledUsers() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public WalletAddCreditResult addCredit(final List<String> userIds, final String walletId, final double amount, final Currency currency)
            throws WalletAddCreditException {

        //request
        WalletAddCreditRequest request = new WalletAddCreditRequest();

        // inout
        request.getUserIds().addAll( userIds );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( SDKUtils.convert( currency ) );

        // operate
        WalletAddCreditResponse response = getPort().addCredit( request );

        // response
        if (null != response.getError()) {

            throw new WalletAddCreditException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return new WalletAddCreditResult( response.getSuccess().getUnknownUsers(), response.getSuccess().getNotEnrolledUsers() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private WalletEnrollErrorCode convert(final net.lin_k.safe_online.wallet.WalletEnrollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletEnrollErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNEXPECTED:
                return WalletEnrollErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private WalletAddCreditErrorCode convert(final net.lin_k.safe_online.wallet.WalletAddCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNEXPECTED:
                return WalletAddCreditErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
