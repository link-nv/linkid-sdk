/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.config;

import com.lyndir.lhunath.opal.system.util.MetaObject;
import java.io.Serializable;
import java.net.URL;
import java.util.List;


public class PublicApplicationConfiguration extends MetaObject implements Serializable {

    private final String                           name;
    private final URL                              url;
    private final String                           ua;
    private final List<PublicApplicationAttribute> attributes;

    public PublicApplicationConfiguration(final String name, final URL url, final String ua,
                                          final List<PublicApplicationAttribute> attributes) {

        this.name = name;
        this.url = url;
        this.ua = ua;
        this.attributes = attributes;
    }

    public String getName() {

        return name;
    }

    public URL getUrl() {

        return url;
    }

    public String getUa() {

        return ua;
    }

    public List<PublicApplicationAttribute> getAttributes() {

        return attributes;
    }
}
