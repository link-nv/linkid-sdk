/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.credentials;

import com.google.common.base.MoreObjects;
import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 25/07/16
 * Time: 16:40
 */
public class LinkIDCredential implements Serializable {

    private final String               name;
    private final LinkIDCredentialType type;

    public LinkIDCredential(final String name, final LinkIDCredentialType type) {

        this.name = name;
        this.type = type;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "type", type ).toString();
    }

    // Accessors

    public String getName() {

        return name;
    }

    public LinkIDCredentialType getType() {

        return type;
    }
}
