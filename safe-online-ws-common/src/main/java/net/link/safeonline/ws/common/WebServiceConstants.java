/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ws.common;

import javax.xml.namespace.QName;

/**
 * Holds constants for the SafeOnline web services.
 * 
 * @author fcorneli
 * 
 */
public class WebServiceConstants {

	private WebServiceConstants() {
		// empty
	}

	public static final String SAFE_ONLINE_SAML_NAMESPACE = "urn:net:lin-k:safe-online:saml";

	public static final String SAFE_ONLINE_SAML_PREFIX = "sosaml";

	public static final QName MULTIVALUED_ATTRIBUTE = new QName(
			SAFE_ONLINE_SAML_NAMESPACE, "multivalued", SAFE_ONLINE_SAML_PREFIX);

	public static final QName COMPOUNDED_ATTRIBUTE_ID = new QName(
			SAFE_ONLINE_SAML_NAMESPACE, "attributeId", SAFE_ONLINE_SAML_PREFIX);

	public static final String SAML_ATTRIB_NAME_FORMAT_BASIC = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
}
