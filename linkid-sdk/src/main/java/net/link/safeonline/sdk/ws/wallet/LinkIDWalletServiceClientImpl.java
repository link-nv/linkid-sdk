/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.wallet;

import java.security.cert.X509Certificate;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.wallet._2.WalletAddCreditRequest;
import net.lin_k.safe_online.wallet._2.WalletAddCreditResponse;
import net.lin_k.safe_online.wallet._2.WalletCommitRequest;
import net.lin_k.safe_online.wallet._2.WalletCommitResponse;
import net.lin_k.safe_online.wallet._2.WalletEnrollRequest;
import net.lin_k.safe_online.wallet._2.WalletEnrollResponse;
import net.lin_k.safe_online.wallet._2.WalletGetInfoRequest;
import net.lin_k.safe_online.wallet._2.WalletGetInfoResponse;
import net.lin_k.safe_online.wallet._2.WalletReleaseRequest;
import net.lin_k.safe_online.wallet._2.WalletReleaseResponse;
import net.lin_k.safe_online.wallet._2.WalletRemoveCreditRequest;
import net.lin_k.safe_online.wallet._2.WalletRemoveCreditResponse;
import net.lin_k.safe_online.wallet._2.WalletRemoveRequest;
import net.lin_k.safe_online.wallet._2.WalletRemoveResponse;
import net.lin_k.safe_online.wallet._2.WalletServicePort;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletAddCreditErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletAddCreditException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletCommitErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletCommitException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletEnrollErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletEnrollException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletGetInfoErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletGetInfoException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletReleaseErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletReleaseException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveCreditErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveCreditException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveErrorCode;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletRemoveException;
import net.link.safeonline.sdk.api.ws.wallet.LinkIDWalletServiceClient;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.wallet.LinkIDWalletServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import org.jetbrains.annotations.Nullable;


public class LinkIDWalletServiceClientImpl extends AbstractWSClient<WalletServicePort> implements LinkIDWalletServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDWalletServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDWalletServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                         final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDWalletServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDWalletServiceFactory.newInstance().getWalletServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.wallet.path" ) ) );
    }

    @Override
    public String enroll(final String userId, final String walletOrganizationId, final double amount, @Nullable final LinkIDCurrency currency,
                         @Nullable final String walletCoin)
            throws LinkIDWalletEnrollException {

        //request
        WalletEnrollRequest request = new WalletEnrollRequest();

        // input
        request.setUserId( userId );
        request.setWalletOrganizationId( walletOrganizationId );
        request.setAmount( amount );
        request.setCurrency( LinkIDSDKUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );

        // operate
        WalletEnrollResponse response = getPort().enroll( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletEnrollException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return response.getSuccess().getWalletId();
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public LinkIDWalletInfo getInfo(final String userId, final String walletOrganizationId)
            throws LinkIDWalletGetInfoException {

        // request
        WalletGetInfoRequest request = new WalletGetInfoRequest();

        // input
        request.setUserId( userId );
        request.setWalletOrganizationId( walletOrganizationId );

        // operate
        WalletGetInfoResponse response = getPort().getInfo( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletGetInfoException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            if (null == response.getSuccess().getWalletId()) {
                return null;
            }

            return new LinkIDWalletInfo( response.getSuccess().getWalletId() );
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void addCredit(final String userId, final String walletId, final double amount, @Nullable final LinkIDCurrency currency,
                          @Nullable final String walletCoin)
            throws LinkIDWalletAddCreditException {

        //request
        WalletAddCreditRequest request = new WalletAddCreditRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( LinkIDSDKUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );

        // operate
        WalletAddCreditResponse response = getPort().addCredit( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletAddCreditException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void removeCredit(final String userId, final String walletId, final double amount, @Nullable final LinkIDCurrency currency,
                             @Nullable final String walletCoin)
            throws LinkIDWalletRemoveCreditException {

        //request
        WalletRemoveCreditRequest request = new WalletRemoveCreditRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setAmount( amount );
        request.setCurrency( LinkIDSDKUtils.convert( currency ) );
        request.setWalletCoin( walletCoin );

        // operate
        WalletRemoveCreditResponse response = getPort().removeCredit( request );

        // response
        if (null != response.getError()) {

            throw new LinkIDWalletRemoveCreditException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {

            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void remove(final String userId, final String walletId)
            throws LinkIDWalletRemoveException {

        //request
        WalletRemoveRequest request = new WalletRemoveRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );

        // operate
        WalletRemoveResponse response = getPort().remove( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDWalletRemoveException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void commit(final String userId, final String walletId, final String walletTransactionId)
            throws LinkIDWalletCommitException {

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
            throw new LinkIDWalletCommitException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    @Override
    public void release(final String userId, final String walletId, final String walletTransactionId)
            throws LinkIDWalletReleaseException {

        // request
        WalletReleaseRequest request = new WalletReleaseRequest();

        // input
        request.setUserId( userId );
        request.setWalletId( walletId );
        request.setWalletTransactionId( walletTransactionId );

        // operate
        WalletReleaseResponse response = getPort().release( request );

        // response
        if (null != response.getError()) {
            throw new LinkIDWalletReleaseException( convert( response.getError().getErrorCode() ) );
        }

        if (null != response.getSuccess()) {
            // all good <o/
            return;
        }

        throw new InternalInconsistencyException( "No success nor error element in the response ?!" );
    }

    // Helper methods

    private LinkIDWalletEnrollErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletEnrollErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletEnrollErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletEnrollErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_USER_ALREADY_ENROLLED:
                return LinkIDWalletEnrollErrorCode.ERROR_USER_ALREADY_ENROLLED;
            case ERROR_UNEXPECTED:
                return LinkIDWalletEnrollErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletEnrollErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletGetInfoErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletGetInfoErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletGetInfoErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletGetInfoErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletAddCreditErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletAddCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletAddCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletAddCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletAddCreditErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletRemoveCreditErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletRemoveCreditErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_WALLET_INVALID_CURRENCY:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_WALLET_INVALID_CURRENCY;
            case ERROR_UNKNOWN_WALLET_COIN:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_WALLET_COIN;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveCreditErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletRemoveErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletRemoveErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_USER:
                return LinkIDWalletRemoveErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNEXPECTED:
                return LinkIDWalletRemoveErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletRemoveErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletCommitErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletCommitErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletCommitErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                return LinkIDWalletCommitErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletCommitErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDWalletReleaseErrorCode convert(final net.lin_k.safe_online.wallet._2.WalletReleaseErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_USER:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_USER;
            case ERROR_UNKNOWN_WALLET:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET;
            case ERROR_UNKNOWN_WALLET_TRANSACTION:
                return LinkIDWalletReleaseErrorCode.ERROR_UNKNOWN_WALLET_TRANSACTION;
            case ERROR_UNEXPECTED:
                return LinkIDWalletReleaseErrorCode.ERROR_UNEXPECTED;
            case ERROR_MAINTENANCE:
                return LinkIDWalletReleaseErrorCode.ERROR_MAINTENANCE;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
