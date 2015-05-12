/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.wallet.LinkIDWalletInfo;


/**
 * linkID wallet WS Client.
 * <p/>
 * Via this interface, wallet admins can manage user's wallets
 */
public interface LinkIDWalletServiceClient {

    /**
     * Enroll users for a wallet. Optionally specify initial credit to add to wallet if applicable
     *
     * @return walletId the enrolled wallet ID
     *
     * @throws LinkIDWalletEnrollException something went wrong, check the error code in the exception
     */
    String enroll(String userId, String walletOrganizationId, double amount, LinkIDCurrency currency)
            throws LinkIDWalletEnrollException;

    /**
     * Get info about a wallet for specified user and wallet organization
     *
     * @param userId               the userId
     * @param walletOrganizationId the wallet organization ID
     *
     * @return wallet info
     *
     * @throws LinkIDWalletGetInfoException the wallet does not exist, user does not exist, ... check the error code
     */
    LinkIDWalletInfo getInfo(String userId, String walletOrganizationId)
            throws LinkIDWalletGetInfoException;

    /**
     * Add credit for a user for a wallet
     *
     * @throws LinkIDWalletAddCreditException something went wrong, check the error code in the exception
     */
    void addCredit(String userId, String walletId, double amount, LinkIDCurrency currency)
            throws LinkIDWalletAddCreditException;

    /**
     * Remove credit for a user for a wallet.
     * If the amount is > than the credit on their wallet or amount==-1, their wallet credit will be set to 0
     *
     * @throws LinkIDWalletRemoveCreditException something went wrong, check the error code in the exception
     */
    void removeCredit(String userId, String walletId, double amount, LinkIDCurrency currency)
            throws LinkIDWalletRemoveCreditException;

    /**
     * Remove the specified wallet from that user
     *
     * @throws LinkIDWalletRemoveException something went wrong, check the error code in the exception
     */
    void remove(String userId, String walletId)
            throws LinkIDWalletRemoveException;

    /**
     * Commit a wallet transaction. The amount payed by the specified wallet transaction ID will be free'd.
     * If not committed, linkID will after a period of time release it.
     *
     * @throws LinkIDWalletCommitException something went wrong, check the error code in the exception
     */
    void commit(String userId, String walletId, String walletTransactionId)
            throws LinkIDWalletCommitException;
}
