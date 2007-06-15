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

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;

/**
 * Interface for attribute type description decorator. The component
 * implementing this interface will convert the incoming lists to lists that
 * have been decorated with attribute descriptions, internationalized according
 * to the given locale.
 * 
 * @author fcorneli
 * 
 */
@Local
public interface AttributeTypeDescriptionDecorator {

	/**
	 * @param identityAttributes
	 * @param locale
	 *            the optional locale.
	 * @return
	 */
	List<AttributeDO> addDescriptionFromIdentityAttributes(
			Collection<ApplicationIdentityAttributeEntity> identityAttributes,
			Locale locale);

	/**
	 * @param attributeTypes
	 * @param locale
	 *            the optional locale.
	 * @return
	 */
	List<AttributeDO> addDescriptionFromAttributeTypes(
			List<AttributeTypeEntity> attributeTypes, Locale locale);
}
