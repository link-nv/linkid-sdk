/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.exception;

/**
 * You called a deprecated operation
 * <p>
 * Created by wvdhaute
 * Date: 24/06/16
 * Time: 14:25
 */
public class LinkIDDeprecatedException extends RuntimeException {

    public LinkIDDeprecatedException(final String message) {

        super( message );
    }

}
