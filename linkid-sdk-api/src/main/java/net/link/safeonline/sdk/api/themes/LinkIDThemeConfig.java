/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.themes;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.sdk.api.ws.linkid.configuration.LinkIDLocalizedImage;


@SuppressWarnings("unused")
public class LinkIDThemeConfig implements Serializable {

    private final String                     name;
    private final String                     friendlyName;
    private final boolean                    defaultTheme;
    //
    private final List<LinkIDLocalizedImage> logos;
    private final List<LinkIDLocalizedImage> authLogos;
    private final List<LinkIDLocalizedImage> backgrounds;
    private final List<LinkIDLocalizedImage> tabletBackgrounds;
    private final List<LinkIDLocalizedImage> alternativeBackgrounds;
    //
    private final String                     backgroundColor;
    private final String                     textColor;

    public LinkIDThemeConfig(final String name, final String friendlyName, final boolean defaultTheme, final List<LinkIDLocalizedImage> logos,
                             final List<LinkIDLocalizedImage> authLogos, final List<LinkIDLocalizedImage> backgrounds,
                             final List<LinkIDLocalizedImage> tabletBackgrounds, final List<LinkIDLocalizedImage> alternativeBackgrounds,
                             final String backgroundColor, final String textColor) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.defaultTheme = defaultTheme;
        this.logos = logos;
        this.authLogos = authLogos;
        this.backgrounds = backgrounds;
        this.tabletBackgrounds = tabletBackgrounds;
        this.alternativeBackgrounds = alternativeBackgrounds;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDThemeConfig{" +
               "name='" + name + '\'' +
               ", friendlyName=" + friendlyName +
               ", defaultTheme=" + defaultTheme +
               ", logos=" + logos +
               ", authLogos=" + authLogos +
               ", backgrounds=" + backgrounds +
               ", tabletBackgrounds=" + tabletBackgrounds +
               ", alternativeBackgrounds=" + alternativeBackgrounds +
               ", backgroundColor='" + backgroundColor + '\'' +
               ", textColor='" + textColor + '\'' +
               '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public boolean isDefaultTheme() {

        return defaultTheme;
    }

    public List<LinkIDLocalizedImage> getLogos() {

        return logos;
    }

    public List<LinkIDLocalizedImage> getAuthLogos() {

        return authLogos;
    }

    public List<LinkIDLocalizedImage> getBackgrounds() {

        return backgrounds;
    }

    public List<LinkIDLocalizedImage> getTabletBackgrounds() {

        return tabletBackgrounds;
    }

    public List<LinkIDLocalizedImage> getAlternativeBackgrounds() {

        return alternativeBackgrounds;
    }

    public String getBackgroundColor() {

        return backgroundColor;
    }

    public String getTextColor() {

        return textColor;
    }
}
