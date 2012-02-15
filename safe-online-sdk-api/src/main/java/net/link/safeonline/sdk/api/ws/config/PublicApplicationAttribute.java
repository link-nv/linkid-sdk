/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.config;

import com.lyndir.lhunath.opal.system.util.MetaObject;
import java.io.Serializable;
import net.link.safeonline.sdk.api.attribute.AttributeType;


/**
 * <h2>{@link PublicApplicationAttribute}
 * <p/>
 * An application identity attribute stub containing only public information.</h2>
 * <p/>
 * <p> A stub that represents the publicly available information from an application identity attribute.
 * This object can be used for providing unauthenticated users access
 * to public application data. </p
 *
 * @author wvdhaute
 */
public class PublicApplicationAttribute extends MetaObject implements Serializable {

    private final String  name;
    private final String  friendly;
    private final boolean required;

    private final AttributeType attributeType;

    private boolean rejected;

    public PublicApplicationAttribute(final String name, final String friendly, final boolean required, final AttributeType attributeType) {

        this.name = name;
        this.friendly = friendly;
        this.required = required;
        this.attributeType = attributeType;
        rejected = false;
    }

    public String getName() {

        return name;
    }

    public String getFriendly() {

        return friendly;
    }

    public boolean isRequired() {

        return required;
    }

    public AttributeType getAttributeType() {

        return attributeType;
    }

    public boolean isRejected() {

        return rejected;
    }

    public void setRejected(final boolean rejected) {

        this.rejected = rejected;
    }
}
