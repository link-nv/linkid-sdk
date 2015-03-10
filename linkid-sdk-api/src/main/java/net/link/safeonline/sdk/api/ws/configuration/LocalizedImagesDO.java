package net.link.safeonline.sdk.api.ws.configuration;

import java.util.Map;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:22
 */
public class LocalizedImagesDO {

    private final Map<String, LocalizedImageDO> imageMap;

    public LocalizedImagesDO(final Map<String, LocalizedImageDO> imageMap) {

        this.imageMap = imageMap;
    }

    // Helper methods

    @Nullable
    public LocalizedImageDO findImage(final String language) {

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

    public Map<String, LocalizedImageDO> getImageMap() {

        return imageMap;
    }
}
