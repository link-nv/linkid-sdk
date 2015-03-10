package net.link.safeonline.sdk.api.ws.configuration;

/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:22
 */
public class LocalizedImageDO {

    private final String url;
    private final String language;

    public LocalizedImageDO(final String url, final String language) {

        this.url = url;
        this.language = language;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LocalizedImageDO{" +
               "url='" + url + '\'' +
               ", language='" + language + '\'' +
               '}';
    }

    // Accessors

    public String getUrl() {

        return url;
    }

    public String getLanguage() {

        return language;
    }
}
