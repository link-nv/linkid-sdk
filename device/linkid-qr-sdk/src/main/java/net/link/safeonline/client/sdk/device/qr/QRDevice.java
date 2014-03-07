/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.client.sdk.device.qr;

public interface QRDevice {

    String NAME = "qr";

    // WS_Authentication
    String WS_REGISTRATION_ID = "urn:net:lin-k:safe-online:qr:ws:auth:registrationId";
    String WS_SESSION_ID      = "urn:net:lin-k:safe-online:qr:ws:auth:sessionId";
    String WS_QR_CODE         = "urn:net:lin-k:safe-online:qr:ws:auth:qrCode";
    String WS_QR_CODE_URL     = "urn:net:lin-k:safe-online:qr:ws:auth:qrCodeURL";

    // format to get QR code in
    String WS_QR_CODE_FORMAT_URL = "urn:net:lin-k:safe-online:qr:ws:auth:qrCodeFormatURL";
}
