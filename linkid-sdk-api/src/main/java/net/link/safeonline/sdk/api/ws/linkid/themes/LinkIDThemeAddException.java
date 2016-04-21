/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.themes;

import net.link.safeonline.sdk.api.themes.LinkIDThemeError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class LinkIDThemeAddException extends Exception {

    @Nullable
    private final LinkIDThemeAddErrorCode errorCode;
    @Nullable
    private final LinkIDThemeError        error;

    public LinkIDThemeAddException(final String message, @NotNull final LinkIDThemeAddErrorCode errorCode) {

        super( message );
        this.errorCode = errorCode;
        this.error = null;
    }

    public LinkIDThemeAddException(@NotNull final LinkIDThemeError error) {

        this.errorCode = null;
        this.error = error;

    }

    @Nullable
    public LinkIDThemeAddErrorCode getErrorCode() {

        return errorCode;
    }

    @Nullable
    public LinkIDThemeError getError() {

        return error;
    }
}
