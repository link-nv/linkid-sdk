/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.themes;

import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 16/12/15
 * Time: 11:20
 */
@SuppressWarnings("unused")
public class LinkIDThemeStatusErrorReport implements Serializable {

    @Nullable
    private final List<LinkIDThemeImageError> logoErrors;
    @Nullable
    private final List<LinkIDThemeImageError> backgroundErrors;
    @Nullable
    private final List<LinkIDThemeImageError> tabletBackgroundErrors;
    @Nullable
    private final List<LinkIDThemeImageError> alternativeBackgroundErrors;

    public LinkIDThemeStatusErrorReport(@Nullable final List<LinkIDThemeImageError> logoErrors, @Nullable final List<LinkIDThemeImageError> backgroundErrors,
                                        @Nullable final List<LinkIDThemeImageError> tabletBackgroundErrors,
                                        @Nullable final List<LinkIDThemeImageError> alternativeBackgroundErrors) {

        this.logoErrors = logoErrors;
        this.backgroundErrors = backgroundErrors;
        this.tabletBackgroundErrors = tabletBackgroundErrors;
        this.alternativeBackgroundErrors = alternativeBackgroundErrors;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeError{" +
               "logoErrors=" + logoErrors +
               ", backgroundErrors=" + backgroundErrors +
               ", tabletBackgroundErrors=" + tabletBackgroundErrors +
               ", alternativeBackgroundErrors=" + alternativeBackgroundErrors +
               '}';
    }

    // Accessors

    @Nullable
    public List<LinkIDThemeImageError> getLogoErrors() {

        return logoErrors;
    }

    @Nullable
    public List<LinkIDThemeImageError> getBackgroundErrors() {

        return backgroundErrors;
    }

    @Nullable
    public List<LinkIDThemeImageError> getTabletBackgroundErrors() {

        return tabletBackgroundErrors;
    }

    @Nullable
    public List<LinkIDThemeImageError> getAlternativeBackgroundErrors() {

        return alternativeBackgroundErrors;
    }

}
