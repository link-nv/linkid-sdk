/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attrib.ws;

import oasis.names.tc.saml._2_0.protocol.AttributeQueryType;
import oasis.names.tc.saml._2_0.protocol.ResponseType;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributePort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SAMLAttributePortImpl implements SAMLAttributePort {

	private static final Log LOG = LogFactory
			.getLog(SAMLAttributePortImpl.class);

	public ResponseType attributeQuery(AttributeQueryType request) {
		LOG.debug("attribute query");
		return null;
	}
}
