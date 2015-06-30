package net.link.safeonline.sdk.api.ws.configuration;

import java.util.Map;


/**
 * Created by wvdhaute
 * Date: 30/06/15
 * Time: 15:46
 */
public class LinkIDLocalization {

    private final String              key;
    private final Map<String, String> values;

    public LinkIDLocalization(final String key, final Map<String, String> values) {

        this.key = key;
        this.values = values;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDLocalization{" +
               "key='" + key + '\'' +
               ", values=" + values +
               '}';
    }

    // Accessors

    public String getKey() {

        return key;
    }

    public Map<String, String> getValues() {

        return values;
    }
}
