/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.themes;

import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 16/12/15
 * Time: 11:24
 */
public class LinkIDThemeColorError implements Serializable {

    private final LinkIDThemeColorErrorCode errorCode;
    private final String                    errorMessage;

    public LinkIDThemeColorError(final LinkIDThemeColorErrorCode errorCode, @Nullable final String errorMessage) {

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeColorError{" +
               "errorCode=" + errorCode +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }

    // Accessors

    public LinkIDThemeColorErrorCode getErrorCode() {

        return errorCode;
    }

    @Nullable
    public String getErrorMessage() {

        return errorMessage;
    }
}
