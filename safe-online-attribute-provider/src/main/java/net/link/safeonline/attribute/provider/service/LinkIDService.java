/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.service;

import net.link.safeonline.attribute.provider.AttributeProvider;


/**
 * LinkID Service holds all available LinkID services for {@link AttributeProvider} implementations.
 */
public interface LinkIDService {

    ConfigurationService getConfigurationService();

    AttributeService getAttributeService();

    PersistenceService getPersistenceService();

    LocalizationService getLocalizationService();
}
