/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.encap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.model.bean.AbstractInitBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "EncapStartableBean")
public class EncapStartableBean extends AbstractInitBean {

	private static final Log LOG = LogFactory.getLog(EncapStartableBean.class);

	public EncapStartableBean() {
		AttributeTypeEntity encapAttributeType = new AttributeTypeEntity(
				SafeOnlineConstants.MOBILE_ENCAP_ATTRIBUTE,
				DatatypeType.STRING, true, false);
		encapAttributeType.setMultivalued(true);
		this.attributeTypes.add(encapAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				encapAttributeType, Locale.ENGLISH.getLanguage(), "Mobile",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				encapAttributeType, "nl", "Gsm nummer", null));

		List<AttributeTypeEntity> encapDeviceAttributeTypeList = new ArrayList<AttributeTypeEntity>();
		encapDeviceAttributeTypeList.add(encapAttributeType);
		this.devices.add(new Device(EncapConstants.ENCAP_DEVICE_ID,
				SafeOnlineConstants.MOBILE_DEVICE_CLASS, "mobile.seam",
				"register-mobile.seam", null, null));
	}

	@Override
	public void preStop() {
		LOG.debug("pre stop");
	}

	@Override
	public int getPriority() {
		return EncapConstants.ENCAP_BOOT_PRIORITY;
	}
}