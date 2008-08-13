/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms.ra.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.sms.GSMModem;
import net.link.safeonline.sms.SMS;
import net.link.safeonline.sms.exception.SMSException;
import net.link.safeonline.sms.ra.SMSConnection;


public class SMSConnectionImpl implements SMSConnection {

    private static final Log LOG = LogFactory.getLog(SMSConnectionImpl.class);

    private GSMModem         gsmModem;


    public SMSConnectionImpl(GSMModem gsmModem) {

        LOG.debug("created");
        this.gsmModem = gsmModem;
    }

    public void sendSMS(String number, String message) {

        LOG.debug("Sending SMS");
        try {
            this.gsmModem.open();
            this.gsmModem.sendSMS(new SMS(number, message));
            LOG.debug("Success");
        } catch (SMSException e) {
            LOG.debug("Could not send SMS");
        }
        this.gsmModem.close();
    }
}
