/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AttributeTypeEntity converter
 * 
 * @author wvdhaute
 * 
 */
public class AttributeTypeConverter implements Converter {

	private static final Log LOG = LogFactory
			.getLog(AttributeTypeConverter.class);

	public Object getAsObject(FacesContext facesContext, UIComponent component,
			String value) {

		AttributeTypeEntity attributeType;

		AttributeTypeDAO attributeTypeDAO = EjbUtils
				.getEJB("SafeOnline/AttributeTypeDAOBean/local",
						AttributeTypeDAO.class);
		if (null == attributeTypeDAO) {
			LOG.debug("attribute type dao not found");
			attributeType = new AttributeTypeEntity();
			attributeType.setName(value);
			return attributeType;
		}
		LOG.debug("attribute type dao found");
		attributeType = attributeTypeDAO.findAttributeType(value);
		return attributeType;

	}

	public String getAsString(FacesContext facesContext, UIComponent component,
			Object value) {
		AttributeTypeEntity attributeType = (AttributeTypeEntity) value;
		return attributeType.getName();
	}

}
