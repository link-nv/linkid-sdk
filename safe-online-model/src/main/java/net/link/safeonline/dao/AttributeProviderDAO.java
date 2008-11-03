/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeEntity;


@Local
public interface AttributeProviderDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AttributeProviderDAOBean/local";

    AttributeProviderEntity findAttributeProvider(ApplicationEntity application, AttributeTypeEntity attributeType);

    List<AttributeProviderEntity> listAttributeProviders(AttributeTypeEntity attributeType);

    void removeAttributeProvider(AttributeProviderEntity attributeProvider);

    void addAttributeProvider(ApplicationEntity application, AttributeTypeEntity attributeType);

    /**
     * Remove all attribute providers for the given application.
     * 
     * @param application
     */
    void removeAttributeProviders(ApplicationEntity application);

    /**
     * Remove all attribute providers for the given attribute type.
     * 
     * @param attributeType
     */
    void removeAttributeProviders(AttributeTypeEntity attributeType);
}
