package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.io.Serializable;
import net.link.safeonline.sdk.api.common.LinkIDRequestStatusCode;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:19
 */
@SuppressWarnings("unused")
public class LinkIDTheme implements Serializable {

    private final String                  name;
    private final String                  friendlyName;
    private final LinkIDRequestStatusCode status;
    private final boolean                 defaultTheme;
    private final boolean                 owner;
    //
    private final LinkIDLocalizedImages   logo;
    private final LinkIDLocalizedImages   background;
    private final LinkIDLocalizedImages   tabletBackground;
    private final LinkIDLocalizedImages   alternativeBackground;
    //
    private final String                  backgroundColor;
    private final String                  textColor;

    public LinkIDTheme(final String name, final String friendlyName, final LinkIDRequestStatusCode status, final boolean defaultTheme, final boolean owner,
                       final LinkIDLocalizedImages logo, final LinkIDLocalizedImages background, final LinkIDLocalizedImages tabletBackground,
                       final LinkIDLocalizedImages alternativeBackground, final String backgroundColor, final String textColor) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.status = status;
        this.defaultTheme = defaultTheme;
        this.owner = owner;
        this.logo = logo;
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
               ", friendlyName=" + friendlyName +
               ", status=" + status +
               ", defaultTheme=" + defaultTheme +
               ", owner=" + owner +
               ", logo=" + logo +
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

    public String getFriendlyName() {

        return friendlyName;
    }

    public LinkIDRequestStatusCode getStatus() {

        return status;
    }

    public boolean isDefaultTheme() {

        return defaultTheme;
    }

    public boolean isOwner() {

        return owner;
    }

    public LinkIDLocalizedImages getLogo() {

        return logo;
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
