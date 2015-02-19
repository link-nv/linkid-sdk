/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.wallet;

import java.util.List;
import net.link.safeonline.sdk.api.payment.Currency;


/**
 * linkID wallet WS Client.
 * <p/>
 * Via this interface, wallet admins can manage user's wallets
 */
public interface WalletServiceClient {

    /**
     * Enroll users for a wallet. Optionally specify initial credit to add to wallet if applicable
     *
     * @throws WalletEnrollException something went wrong, check the error code in the exception
     */
    WalletEnrollResult enroll(List<String> userIds, String walletId, double amount, Currency currency)
            throws WalletEnrollException;

    /**
     * Add credit to users for a wallet
     *
     * @throws WalletAddCreditException something went wrong, check the error code in the exception
     */
    WalletAddCreditResult addCredit(List<String> userIds, String walletId, double amount, Currency currency)
            throws WalletAddCreditException;

    /**
     * Commit a wallet transaction. The amount payed by the specified wallet transaction ID will be free'd.
     * If not committed, linkID will after a period of time release it.
     *
     * @throws WalletCommitException something went wrong, check the error code in the exception
     */
    void commit(String userId, String walletId, String walletTransactionId)
            throws WalletCommitException;
}
