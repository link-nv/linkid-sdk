package net.link.safeonline.sdk.api.ws.linkid.ltqr;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 18/11/15
 * Time: 14:28
 */
public class LinkIDFavoritesConfiguration implements Serializable {

    private final String title;
    private final String info;
    private final String logoEncoded;
    private final String backgroundColor;
    private final String textColor;

    public LinkIDFavoritesConfiguration(final String title, final String info, final String logoEncoded, final String backgroundColor, final String textColor) {

        this.title = title;
        this.info = info;
        this.logoEncoded = logoEncoded;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    @Override
    public String toString() {

        return "LinkIDFavoritesConfiguration{" +
               "title='" + title + '\'' +
               ", info='" + info + '\'' +
               ", logoEncoded='" + logoEncoded + '\'' +
               ", backgroundColor='" + backgroundColor + '\'' +
               ", textColor='" + textColor + '\'' +
               '}';
    }

    // Accessors

    public String getTitle() {

        return title;
    }

    public String getInfo() {

        return info;
    }

    public String getLogoEncoded() {

        return logoEncoded;
    }

    public String getBackgroundColor() {

        return backgroundColor;
    }

    public String getTextColor() {

        return textColor;
    }
}
