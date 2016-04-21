/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.themes;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 16/12/15
 * Time: 11:24
 */
@SuppressWarnings("unused")
public class LinkIDThemeImageError implements Serializable {

    private final String                    language;
    private final LinkIDThemeImageErrorCode errorCode;
    private final String                    errorMessage;

    public LinkIDThemeImageError(final String language, final LinkIDThemeImageErrorCode errorCode, final String errorMessage) {

        this.language = language;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeImageError{" +
               "language='" + language + '\'' +
               ", errorCode=" + errorCode +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }

    // Accessors

    public String getLanguage() {

        return language;
    }

    public LinkIDThemeImageErrorCode getErrorCode() {

        return errorCode;
    }

    public String getErrorMessage() {

        return errorMessage;
    }
}
