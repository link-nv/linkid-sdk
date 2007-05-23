/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionPK;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.model.AttributeTypeDescriptionDecorator;

@Stateless
public class AttributeTypeDescriptionDecoratorBean implements
		AttributeTypeDescriptionDecorator {

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	public List<AttributeDO> addDescriptionFromAttributeTypes(
			List<AttributeTypeEntity> attributeTypes, Locale locale) {
		List<AttributeDO> attributes = new LinkedList<AttributeDO>();
		String language = null;
		if (null != locale) {
			language = locale.getLanguage();
		}
		for (AttributeTypeEntity attributeType : attributeTypes) {
			String name = attributeType.getName();
			String datatype = attributeType.getType();
			String humanReadableName = null;
			String description = null;
			if (null != language) {
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(name,
								language));
				if (null != attributeTypeDescription) {
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeDO attribute = new AttributeDO(name, datatype, 0,
					humanReadableName, description, attributeType
							.isUserEditable(), true, null, null);
			attributes.add(attribute);
		}
		return attributes;
	}

	public List<AttributeDO> addDescriptionFromIdentityAttributes(
			List<ApplicationIdentityAttributeEntity> identityAttributes,
			Locale locale) {
		List<AttributeDO> attributes = new LinkedList<AttributeDO>();
		String language = null;
		if (null != locale) {
			language = locale.getLanguage();
		}
		for (ApplicationIdentityAttributeEntity identityAttribute : identityAttributes) {
			String name = identityAttribute.getAttributeTypeName();
			String datatype = identityAttribute.getAttributeType().getType();
			String humanReadableName = null;
			String description = null;
			if (null != language) {
				AttributeTypeDescriptionEntity attributeTypeDescription = this.attributeTypeDAO
						.findDescription(new AttributeTypeDescriptionPK(name,
								language));
				if (null != attributeTypeDescription) {
					humanReadableName = attributeTypeDescription.getName();
					description = attributeTypeDescription.getDescription();
				}
			}
			AttributeDO attribute = new AttributeDO(name, datatype, 0,
					humanReadableName, description, identityAttribute
							.getAttributeType().isUserEditable(),
					identityAttribute.isDataMining(), null, null);
			attributes.add(attribute);
		}
		return attributes;
	}
}
