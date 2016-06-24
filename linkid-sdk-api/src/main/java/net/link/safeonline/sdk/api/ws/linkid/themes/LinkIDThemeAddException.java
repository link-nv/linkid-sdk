/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.themes;

import net.link.safeonline.sdk.api.themes.LinkIDThemeError;
import org.jetbrains.annotations.NotNull;


public class LinkIDThemeAddException extends Exception {

    private final LinkIDThemeError error;

    public LinkIDThemeAddException(@NotNull final LinkIDThemeError error) {

        this.error = error;

    }

    public LinkIDThemeError getError() {

        return error;
    }
}
