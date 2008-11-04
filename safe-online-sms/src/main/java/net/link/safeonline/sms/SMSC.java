/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms;

import net.link.safeonline.sms.exception.SMSException;


public interface SMSC {

    void sendSMS(SMS sms)
            throws SMSException;

}
