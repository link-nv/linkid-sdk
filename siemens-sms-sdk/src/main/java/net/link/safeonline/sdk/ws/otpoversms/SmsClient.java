/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.otpoversms;

import java.net.ConnectException;


/**
 * Interface for sms client. The sms web service is used by the OTP over SMS device.
 * 
 * @author wvdhaute
 * 
 */
public interface SmsClient {

    void sendSms(String to, String message)
            throws ConnectException;
}
