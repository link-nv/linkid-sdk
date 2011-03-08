/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;


/**
 * General WS Client interface. Provides access to the outbound and inbound SOAP messages, HTTP headers and optional unilateral TLS
 * authentication.
 * 
 * @author fcorneli
 */
public interface WSClient {

    /**
     * Enables or disables the message capturing.
     */
    void setCaptureMessages(boolean captureMessages);

    /**
     * Gives back the current message capturing setting.
     */
    boolean isCaptureMessages();

    /**
     * Gives back the inbound message, i.e., the response SOAP message.
     */
    Document getInboundMessage();

    /**
     * Gives back the outbound message, i.e., the request SOAP message.
     */
    Document getOutboundMessage();

    /**
     * Retrieve all header data from the result of the previous service request.
     */
    Map<String, List<String>> getHeaders();
}
