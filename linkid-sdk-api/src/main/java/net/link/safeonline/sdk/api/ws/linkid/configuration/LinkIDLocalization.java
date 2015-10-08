package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.util.Map;


/**
 * Created by wvdhaute
 * Date: 30/06/15
 * Time: 15:46
 */
public class LinkIDLocalization {

    private final String                    key;
    private final LinkIDLocalizationKeyType keyType;
    private final Map<String, String>       values;

    public LinkIDLocalization(final String key, final LinkIDLocalizationKeyType keyType, final Map<String, String> values) {

        this.key = key;
        this.keyType = keyType;
        this.values = values;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLocalization{" +
               "key='" + key + '\'' +
               "keyType='" + keyType + '\'' +
               ", values=" + values +
               '}';
    }

    // Accessors

    public String getKey() {

        return key;
    }

    public LinkIDLocalizationKeyType getKeyType() {

        return keyType;
    }

    public Map<String, String> getValues() {

        return values;
    }
}
