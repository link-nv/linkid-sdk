/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.permissions;

import java.util.Arrays;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 02/05/16
 * Time: 10:08
 */
public enum LinkIDApplicationPermissionType {

    //
    // Wallet organization permissions
    WALLET_ADD_CREDIT, WALLET_REMOVE_CREDIT, WALLET_REMOVE, WALLET_ENROLL, WALLET_USE,
    //
    // Voucher organization permissions
    VOUCHER_REWARD, VOUCHER_LIST, VOUCHER_REDEEM;

    public static List<LinkIDApplicationPermissionType> getVoucherOrganizationPermissions() {

        return Arrays.asList( VOUCHER_REWARD, VOUCHER_LIST, VOUCHER_REDEEM );
    }

    public static List<LinkIDApplicationPermissionType> getWalletOrganizationPermissions() {

        return Arrays.asList( WALLET_ADD_CREDIT, WALLET_REMOVE_CREDIT, WALLET_REMOVE, WALLET_ENROLL, WALLET_USE );
    }

}
