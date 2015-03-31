package net.link.safeonline.sdk.api.ws.configuration;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:19
 */
@SuppressWarnings("unused")
public class LinkIDTheme implements Serializable {

    private final String                name;
    private final boolean               defaultTheme;
    //
    private final LinkIDLocalizedImages logo;
    private final LinkIDLocalizedImages authLogo;
    private final LinkIDLocalizedImages background;
    private final LinkIDLocalizedImages tabletBackground;
    private final LinkIDLocalizedImages alternativeBackground;
    //
    private final String                backgroundColor;
    private final String                textColor;

    public LinkIDTheme(final String name, final boolean defaultTheme, final LinkIDLocalizedImages logo, final LinkIDLocalizedImages authLogo,
                       final LinkIDLocalizedImages background, final LinkIDLocalizedImages tabletBackground, final LinkIDLocalizedImages alternativeBackground,
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

        return "LinkIDTheme{" +
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

    public LinkIDLocalizedImages getLogo() {

        return logo;
    }

    public LinkIDLocalizedImages getAuthLogo() {

        return authLogo;
    }

    public LinkIDLocalizedImages getBackground() {

        return background;
    }

    public LinkIDLocalizedImages getTabletBackground() {

        return tabletBackground;
    }

    public LinkIDLocalizedImages getAlternativeBackground() {

        return alternativeBackground;
    }

    public String getBackgroundColor() {

        return backgroundColor;
    }

    public String getTextColor() {

        return textColor;
    }
}
