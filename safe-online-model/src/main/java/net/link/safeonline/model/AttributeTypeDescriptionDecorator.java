/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;


/**
 * Interface for attribute type description decorator. The component implementing this interface will convert the incoming lists to lists
 * that have been decorated with attribute descriptions, internationalized according to the given locale.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeTypeDescriptionDecorator extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AttributeTypeDescriptionDecoratorBean/local";

    /**
     * @param identityAttributes
     * @param locale
     *            the optional locale.
     */
    List<AttributeDO> addDescriptionFromIdentityAttributes(Collection<ApplicationIdentityAttributeEntity> identityAttributes, Locale locale);
}
