package net.link.safeonline.sdk.api.ws.configuration;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:19
 */
public class ThemeDO implements Serializable {

    private final String            name;
    private final boolean           defaultTheme;
    //
    private final LocalizedImagesDO logo;
    private final LocalizedImagesDO authLogo;
    private final LocalizedImagesDO background;
    private final LocalizedImagesDO tabletBackground;
    private final LocalizedImagesDO alternativeBackground;
    //
    private final String            backgroundColor;
    private final String            textColor;

    public ThemeDO(final String name, final boolean defaultTheme, final LocalizedImagesDO logo, final LocalizedImagesDO authLogo,
                   final LocalizedImagesDO background, final LocalizedImagesDO tabletBackground, final LocalizedImagesDO alternativeBackground,
                   final String backgroundColor, final String textColor) {

        this.name = name;
        this.defaultTheme = defaultTheme;
        this.logo = logo;
        this.authLogo = authLogo;
        this.background = background;
        this.tabletBackground = tabletBackground;
        this.alternativeBackground = alternativeBackground;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    // Helper methods

    @Override
    public String toString() {

        return "ThemeDO{" +
               "name='" + name + '\'' +
               ", defaultTheme=" + defaultTheme +
               ", logo=" + logo +
               ", authLogo=" + authLogo +
               ", background=" + background +
               ", tabletBackground=" + tabletBackground +
               ", alternativeBackground=" + alternativeBackground +
               ", backgroundColor='" + backgroundColor + '\'' +
               ", textColor='" + textColor + '\'' +
               '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    public boolean isDefaultTheme() {

        return defaultTheme;
    }

    public LocalizedImagesDO getLogo() {

        return logo;
    }

    public LocalizedImagesDO getAuthLogo() {

        return authLogo;
    }

    public LocalizedImagesDO getBackground() {

        return background;
    }

    public LocalizedImagesDO getTabletBackground() {

        return tabletBackground;
    }

    public LocalizedImagesDO getAlternativeBackground() {

        return alternativeBackground;
    }

    public String getBackgroundColor() {

        return backgroundColor;
    }

    public String getTextColor() {

        return textColor;
    }
}
