/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.config;

import java.io.Serializable;
import java.util.List;


/**
 * <h2>{@link ApplicationAttributeGroup}
 * <p/>
 * An attribute group of application identity attributes stub containing only public information.</h2>
 * <p/>
 * <p> A stub that represents an attribute group containing the publicly available information from an application's
 * identity attributes.
 * This object can be used for providing unauthenticated users access to public application data. </p
 *
 * @author wvdhaute
 */
public class ApplicationAttributeGroup implements Serializable {

    private final String name;
    private final String friendly;

    private final List<PublicApplicationAttribute> attributes;

    public ApplicationAttributeGroup(final String name, final String friendly, final List<PublicApplicationAttribute> attributes) {

        this.name = name;
        this.friendly = friendly;
        this.attributes = attributes;
    }

    public String getName() {

        return name;
    }

    public String getFriendly() {

        return friendly;
    }

    public List<PublicApplicationAttribute> getAttributes() {

        return attributes;
    }
}
