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
 * messages and the HTTP headers.
 * 
 * @author fcorneli
 * 
 */
public interface MessageAccessor {

	/**
	 * Enables or disables the message capturing.
	 * 
	 * @param captureMessages
	 */
	void setCaptureMessages(boolean captureMessages);

	/**
	 * Gives back the current message capturing setting.
	 * 
	 * @return
	 */
	boolean isCaptureMessages();

	/**
	 * Gives back the inbound message, i.e., the response SOAP message.
	 * 
	 * @return
	 */
	Document getInboundMessage();

	/**
	 * Gives back the outbound message, i.e., the request SOAP message.
	 * 
	 * @return
	 */
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
