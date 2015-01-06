/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.capture;

/**
 * linkID Capture WS Client.
 * <p/>
 * Via this interface, applications can capture payments
 */
public interface CaptureServiceClient {

    /**
     * Capture a payment
     *
     * @throws CaptureException something went wrong, check the error code in the exception
     */
    void capture(String orderReference)
            throws CaptureException;
}
