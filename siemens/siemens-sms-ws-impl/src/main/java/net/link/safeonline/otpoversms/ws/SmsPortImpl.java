/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.ws;

import javax.jws.WebService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sis.mobile.SmsPortType;
import sis.mobile.SmsPortTypeSendSmsGenericFaultFaultMessage;


@WebService(endpointInterface = "sis.mobile.SmsPortType")
public class SmsPortImpl implements SmsPortType {

    private static final Log LOG = LogFactory.getLog(SmsPortImpl.class);


    /**
     * {@inheritDoc}
     */
    public void sendSms(String to, String msg)
            throws SmsPortTypeSendSmsGenericFaultFaultMessage {

        LOG.debug("********************** SMS DUMMY SERVICE **********************");
        LOG.debug("* Send message \"" + msg + "\" to " + to);
        LOG.debug("********************** ----------------- **********************");

    }

}
