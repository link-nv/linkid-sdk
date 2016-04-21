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
 * Time: 11:20
 */
public class LinkIDThemeError implements Serializable {

    @Nullable
    private final LinkIDThemeColorError backgroundColorError;
    @Nullable
    private final LinkIDThemeColorError textColorError;

    public LinkIDThemeError(@Nullable final LinkIDThemeColorError backgroundColorError, @Nullable final LinkIDThemeColorError textColorError) {

        this.backgroundColorError = backgroundColorError;
        this.textColorError = textColorError;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeError{" +
               "backgroundColorError=" + backgroundColorError +
               ", textColorError=" + textColorError +
               '}';
    }

    // Accessors

    @Nullable
    public LinkIDThemeColorError getBackgroundColorError() {

        return backgroundColorError;
    }

    @Nullable
    public LinkIDThemeColorError getTextColorError() {

        return textColorError;
    }
}
