package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.io.Serializable;
import java.util.Map;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:22
 */
public class LinkIDLocalizedImages implements Serializable {

    private final Map<String, LinkIDLocalizedImage> imageMap;

    public LinkIDLocalizedImages(final Map<String, LinkIDLocalizedImage> imageMap) {

        this.imageMap = imageMap;
    }

    // Helper methods

    @Nullable
    public LinkIDLocalizedImage findImage(final String language) {

        if (null == language)
            return null;

        return imageMap.get( language );
    }

    @Override
    public String toString() {

        return "LocalizedImagesDO{" +
               "imageMap=" + imageMap +
               '}';
    }

    // Accessors

    public Map<String, LinkIDLocalizedImage> getImageMap() {

        return imageMap;
    }
}
