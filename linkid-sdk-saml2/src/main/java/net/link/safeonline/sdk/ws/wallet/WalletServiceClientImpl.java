/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.wallet;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.wallet.WalletAddCreditRequest;
import net.lin_k.safe_online.wallet.WalletAddCreditResponse;
import net.lin_k.safe_online.wallet.WalletCommitRequest;
import net.lin_k.safe_online.wallet.WalletCommitResponse;
import net.lin_k.safe_online.wallet.WalletEnrollRequest;
import net.lin_k.safe_online.wallet.WalletEnrollResponse;
import net.lin_k.safe_online.wallet.WalletRemoveCreditRequest;
import net.lin_k.safe_online.wallet.WalletRemoveCreditResponse;
import net.lin_k.safe_online.wallet.WalletRemoveRequest;
import net.lin_k.safe_online.wallet.WalletRemoveResponse;
import net.lin_k.safe_online.wallet.WalletServicePort;
import net.link.safeonline.sdk.api.payment.Currency;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletAddCreditException;
import net.link.safeonline.sdk.api.ws.wallet.WalletCommitErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletCommitException;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletEnrollException;
import net.link.safeonline.sdk.api.ws.wallet.WalletRemoveCreditErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.wallet.WalletRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.WalletRemoveException;
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
    public String enroll(final String userId, final String walletOrganizationId, final double amount, final Currency currency)
            throws WalletEnrollException {

        //request
        WalletEnrollRequest request = new WalletEnrollRequest();

        // input
        request.setUserId( userId );
        request.setWalletOrganizationId( walletOrganizationId );
        request.setAmount( amount );
        request.setCurrency( SDKUtils.convert( currency ) );

        // operate
        WalletEnrollResponse response = getPort().enroll( request );

        // response
        if (null != response.getError()) {

            throw new WalletEnrollException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getWalletId();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void addCredit(final String userId, final String walletId, final double amount, final Currency currency)
            throws WalletAddCreditException {

        //request
        WalletAddCreditRequest request = new WalletAddCreditRequest();

        // input
        request.setUserId( userId );
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

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void removeCredit(final String userId, final String walletId, final double amount, final Currency currency)
            throws WalletRemoveCreditException {

        //request
        WalletRemoveCreditRequest request = new WalletRemoveCreditRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( SDKUtils.convert( currency ) );

        // operate
        WalletRemoveCreditResponse response = getPort().removeCredit( request );

        // response
        if (null != response.getError()) {

            throw new WalletRemoveCreditException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void remove(final String userId, final String walletId)
            throws WalletRemoveException {

        //request
        WalletRemoveRequest request = new WalletRemoveRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );

        // operate
        WalletRemoveResponse response = getPort().remove( request );

        // response
        if (null != response.getError()) {
            throw new WalletRemoveException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void commit(final String userId, final String walletId, final String walletTransactionId)
            throws WalletCommitException {

        // request
        WalletCommitRequest request = new WalletCommitRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setWalletTransactionId( walletTransactionId );

        // operate
        WalletCommitResponse response = getPort().commit( request );

        // response
        if (null != response.getError()) {
            throw new WalletCommitException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private WalletEnrollErrorCode convert(final net.lin_k.safe_online.wallet.WalletEnrollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletEnrollErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return WalletEnrollErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_USER_ALREADY_ENROLLED:
                return WalletEnrollErrorCode.ERROR_USER_ALREADY_ENROLLED;
            case ERROR_UNEXPECTED:
                return WalletEnrollErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private WalletAddCreditErrorCode convert(final net.lin_k.safe_online.wallet.WalletAddCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return WalletAddCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return WalletAddCreditErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private WalletRemoveCreditErrorCode convert(final net.lin_k.safe_online.wallet.WalletRemoveCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return WalletRemoveCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return WalletRemoveCreditErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private WalletRemoveErrorCode convert(final net.lin_k.safe_online.wallet.WalletRemoveErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return WalletRemoveErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return WalletRemoveErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return WalletRemoveErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private WalletCommitErrorCode convert(final net.lin_k.safe_online.wallet.WalletCommitErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return WalletCommitErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return WalletCommitErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return WalletCommitErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                return WalletCommitErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
