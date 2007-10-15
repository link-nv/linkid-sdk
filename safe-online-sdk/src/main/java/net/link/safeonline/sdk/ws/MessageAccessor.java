/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Message Accessor interface. Provides access to the outbound and inbound SOAP
 * messages.
 * 
 * @author fcorneli
 * 
 */
public interface MessageAccessor {

	void setCaptureMessages(boolean captureMessages);

	boolean isCaptureMessages();

	Document getInboundMessage();

	Document getOutboundMessage();

	/**
	 * Retrieve header data from the given header out of the result of the
	 * previous service request.
	 * 
	 * @param name
	 *            The name of the header.
	 */
	LinkedList<Object> getHeader(String name);

	/**
	 * Retrieve all header data from the result of the previous service request.
	 */
	Map<String, List<Object>> getHeaders();
}
