/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

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
}
