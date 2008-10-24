/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.exception;

import javax.ejb.ApplicationException;

import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;


@ApplicationException(rollback = true)
public class SafeOnlineResourceException extends SafeOnlineException {

    private static final long       serialVersionUID = 1L;

    private final ResourceNameType  resourceName;

    private final ResourceLevelType resourceLevel;

    private final String            sourceComponent;


    public SafeOnlineResourceException(ResourceNameType resourceName, ResourceLevelType resourceLevel, String sourceComponent) {

        this.resourceName = resourceName;
        this.resourceLevel = resourceLevel;
        this.sourceComponent = sourceComponent;

    }

    public ResourceLevelType getResourceLevel() {

        return this.resourceLevel;
    }

    public ResourceNameType getResourceName() {

        return this.resourceName;
    }

    public String getSourceComponent() {

        return this.sourceComponent;
    }

}
