/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

import net.link.safeonline.sdk.api.payment.LinkIDCurrency;


/**
 * linkID wallet WS Client.
 * <p/>
 * Via this interface, wallet admins can manage user's wallets
 */
public interface WalletServiceClient {

    /**
     * Enroll users for a wallet. Optionally specify initial credit to add to wallet if applicable
     *
     * @return walletId the enrolled wallet ID
     *
     * @throws WalletEnrollException something went wrong, check the error code in the exception
     */
    String enroll(String userIds, String walletOrganizationId, double amount, LinkIDCurrency currency)
            throws WalletEnrollException;

    /**
     * Add credit for a user for a wallet
     *
     * @throws WalletAddCreditException something went wrong, check the error code in the exception
     */
    void addCredit(String userId, String walletId, double amount, LinkIDCurrency currency)
            throws WalletAddCreditException;

    /**
     * Remove credit for a user for a wallet.
     * If the amount is > than the credit on their wallet or amount==-1, their wallet credit will be set to 0
     *
     * @throws WalletRemoveCreditException something went wrong, check the error code in the exception
     */
    void removeCredit(String userId, String walletId, double amount, LinkIDCurrency currency)
            throws WalletRemoveCreditException;

    /**
     * Remove the specified wallet from that user
     *
     * @throws WalletRemoveException something went wrong, check the error code in the exception
     */
    void remove(String userId, String walletId)
            throws WalletRemoveException;

    /**
     * Commit a wallet transaction. The amount payed by the specified wallet transaction ID will be free'd.
     * If not committed, linkID will after a period of time release it.
     *
     * @throws WalletCommitException something went wrong, check the error code in the exception
     */
    void commit(String userId, String walletId, String walletTransactionId)
            throws WalletCommitException;
}
