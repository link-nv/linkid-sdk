/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.idmapping.client.NameIdentifierMappingClient;
import org.w3c.dom.Document;


/**
 * <h2>{@link DummyNameIdentifierMappingClient}<br>
 * <sub>An linkID id mapping service client used inside unit tests.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DummyNameIdentifierMappingClient implements NameIdentifierMappingClient {

    private static String userId;


    public static void setUserId(String userId) {

        DummyNameIdentifierMappingClient.userId = userId;
    }

    public static String getUserId() {

        return userId;
    }

    /**
     * {@inheritDoc}
     */
    public String getUserId(String attributeTypeName, String identifier)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException {

        return userId;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getHeaders() {

        return new HashMap<String, List<String>>();
    }

    /**
     * {@inheritDoc}
     */
    public Document getInboundMessage() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Document getOutboundMessage() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCaptureMessages() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setCaptureMessages(boolean captureMessages) {

        throw new UnsupportedOperationException();
    }
}
