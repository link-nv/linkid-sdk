/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.ws;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.auth.ws.client.DeviceAuthenticationClient;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;


/**
 * Test Device Authentication Client implementation
 * 
 * @author wvdhaute
 */

public class DeviceTestAuthenticationClientImpl implements DeviceAuthenticationClient {

    static final Log           LOG            = LogFactory.getLog(DeviceTestAuthenticationClientImpl.class);

    public static final String testDeviceName = "test-device";
    public static final String testUserId     = "test-user-id-" + UUID.randomUUID().toString();


    /**
     * {@inheritDoc}
     */
    public WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request)
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException {

        WSAuthenticationResponseType response = new WSAuthenticationResponseType();
        response.setUserId(testUserId);
        response.setDeviceName(testDeviceName);

        StatusType status = new StatusType();
        StatusCodeType statusCode = new StatusCodeType();
        statusCode.setValue(WSAuthenticationErrorCode.SUCCESS.getErrorCode());
        status.setStatusCode(statusCode);
        status.setStatusMessage(null);
        response.setStatus(status);

        return response;
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
