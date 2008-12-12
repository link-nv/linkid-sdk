/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.sms;

import java.net.ConnectException;


/**
 * <h2>{@link SmsService}<br>
 * <sub>SMS service API. </sub></h2>
 * 
 * <p>
 * SMS service API. OSGi SMS service implementations should implement this interface.
 * </p>
 * 
 * <p>
 * <i>Aug 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface SmsService {

    void sendSms(String mobile, String message)
            throws ConnectException;

}
