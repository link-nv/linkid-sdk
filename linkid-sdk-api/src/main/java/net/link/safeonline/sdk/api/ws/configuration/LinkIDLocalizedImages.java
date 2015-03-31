package net.link.safeonline.sdk.api.ws.configuration;

import java.util.Map;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:22
 */
public class LinkIDLocalizedImages {

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
