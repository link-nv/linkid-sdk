/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.password.auth.ws;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;


/**
 * Test Name ID Mapping client implementation
 * 
 * @author wvdhaute
 */

public class PasswordTestNameIdentifierMappingClientImpl implements NameIdentifierMappingClient {

    static final Log           LOG        = LogFactory.getLog(PasswordTestNameIdentifierMappingClientImpl.class);

    public static final String testUserId = "test-user-id-" + UUID.randomUUID().toString();


    /**
     * {@inheritDoc}
     */
    public String getUserId(String username)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException {

        return testUserId;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getHeaders() {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Document getInboundMessage() {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Document getOutboundMessage() {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCaptureMessages() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setCaptureMessages(boolean captureMessages) {

    }

}
