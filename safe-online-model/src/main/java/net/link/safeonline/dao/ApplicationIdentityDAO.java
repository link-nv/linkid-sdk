/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationIdentityEntity;
import net.link.safeonline.entity.AttributeTypeEntity;


@Local
public interface ApplicationIdentityDAO {

    ApplicationIdentityEntity addApplicationIdentity(ApplicationEntity application, long identityVersion);

    ApplicationIdentityEntity getApplicationIdentity(ApplicationEntity application, long identityVersion)
            throws ApplicationIdentityNotFoundException;

    List<ApplicationIdentityEntity> listApplicationIdentities(ApplicationEntity application);

    /**
     * Removes an application identity. This will also remove the application identity attributes of this application
     * identity.
     *
     * @param applicationIdentity
     */
    void removeApplicationIdentity(ApplicationIdentityEntity applicationIdentity);

    ApplicationIdentityAttributeEntity addApplicationIdentityAttribute(ApplicationIdentityEntity applicationIdentity,
            AttributeTypeEntity attributeType, boolean required, boolean dataMining);

    void removeApplicationIdentityAttribute(ApplicationIdentityAttributeEntity applicationIdentityAttribute);

    List<ApplicationIdentityEntity> listApplicationIdentities();
}
