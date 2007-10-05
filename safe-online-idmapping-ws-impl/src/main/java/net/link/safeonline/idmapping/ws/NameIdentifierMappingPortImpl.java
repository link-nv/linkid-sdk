/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.idmapping.ws;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import net.link.safeonline.ws.util.ri.Injection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oasis.names.tc.saml._2_0.protocol.NameIDMappingRequestType;
import oasis.names.tc.saml._2_0.protocol.NameIDMappingResponseType;
import oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort;

/**
 * Implementation of SAML Name Identifier Mapping Service.
 * 
 * <p>
 * Specification: Assertions and Protocols for the OASIS Security Assertion
 * Markup Language (SAML) V2.0.
 * </p>
 * 
 * @author fcorneli
 * 
 */
@WebService(endpointInterface = "oasis.names.tc.saml._2_0.protocol.NameIdentifierMappingPort")
@HandlerChain(file = "app-auth-ws-handlers.xml")
@Injection
public class NameIdentifierMappingPortImpl implements NameIdentifierMappingPort {

	private static final Log LOG = LogFactory
			.getLog(NameIdentifierMappingPortImpl.class);

	public NameIDMappingResponseType nameIdentifierMappingQuery(
			NameIDMappingRequestType request) {
		LOG.debug("name identifier mapping query");
		return null;
	}
}
