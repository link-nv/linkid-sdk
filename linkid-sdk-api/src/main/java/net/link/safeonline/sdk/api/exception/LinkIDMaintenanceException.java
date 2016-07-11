/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.exception;

/**
 * linkID is in maintenance mode right now, please try again later
 * <p>
 * Created by wvdhaute
 * Date: 24/06/16
 * Time: 14:25
 */
public class LinkIDMaintenanceException extends RuntimeException {

    public LinkIDMaintenanceException(final String message) {

        super( message );
    }

}
