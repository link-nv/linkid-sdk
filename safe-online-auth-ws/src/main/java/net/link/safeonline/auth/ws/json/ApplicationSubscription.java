package net.link.safeonline.auth.ws.json;

import java.net.URL;
import java.util.List;
import net.link.safeonline.config.ws.json.PublicApplicationAttribute;
import net.link.safeonline.config.ws.json.PublicApplicationConfiguration;


public class ApplicationSubscription extends PublicApplicationConfiguration {

    private final List<PublicApplicationAttribute> rejectedAttributes;

    public ApplicationSubscription(final String name, final URL url, final String ua, final List<PublicApplicationAttribute> attributes,
                                   final List<PublicApplicationAttribute> rejectedAttributes) {

        super( name, url, ua, attributes );

        this.rejectedAttributes = rejectedAttributes;
    }

    public List<PublicApplicationAttribute> getRejectedAttributes() {

        return rejectedAttributes;
    }
}
