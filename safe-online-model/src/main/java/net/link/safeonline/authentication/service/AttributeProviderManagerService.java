/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeProviderNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeProviderException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.AttributeProviderEntity;


@Local
public interface AttributeProviderManagerService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AttributeProviderManagerServiceBean/local";

    List<AttributeProviderEntity> getAttributeProviders(String attributeName) throws AttributeTypeNotFoundException;

    void removeAttributeProvider(AttributeProviderEntity attributeProvider) throws AttributeProviderNotFoundException;

    void addAttributeProvider(String applicationName, String attributeName) throws ApplicationNotFoundException,
                                                                           AttributeTypeNotFoundException,
                                                                           ExistingAttributeProviderException, PermissionDeniedException;
}
