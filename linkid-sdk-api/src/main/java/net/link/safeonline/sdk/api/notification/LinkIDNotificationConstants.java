/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.notification;

public interface LinkIDNotificationConstants {

    //
    // Core notifications
    String TOPIC_PARAM                   = "topic";
    String APPLICATION_NAME_PARAM        = "application";
    String FILTER_PARAM                  = "filter";
    String USER_ID_PARAM                 = "userId";
    String INFO_PARAM                    = "info";
    //
    // Payment Status
    String PAYMENT_ORDER_REF_PARAM       = "orderRef";
    //
    // LTQR
    String LTQR_REF_PARAM                = "ltqrRef";
    String LTQR_PAYMENT_ORDER_REF_PARAM  = "paymentOrderRef";
    String LTQR_CLIENT_SESSION_ID_PARAM  = "clientSessionId";
    //
    // Theme
    String THEME_NAME_PARAM              = "themeName";
    //
    // Vouchers
    String VOUCHER_ORGANIZATION_ID_PARAM = "voucherOrganizationId";
    //
    // ID
    String ID_PARAM                      = "id";

}
