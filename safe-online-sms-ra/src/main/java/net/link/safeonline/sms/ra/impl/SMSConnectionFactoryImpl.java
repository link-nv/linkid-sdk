/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms.ra.impl;

import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import net.link.safeonline.sms.ra.SMSConnection;
import net.link.safeonline.sms.ra.SMSConnectionFactory;
import net.link.safeonline.sms.ra.SMSManagedConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SMSConnectionFactoryImpl implements SMSConnectionFactory {

    private static final Log                      LOG = LogFactory.getLog(SMSConnectionFactoryImpl.class);

    private transient ConnectionManager           connectionManager;

    private transient SMSManagedConnectionFactory smsManagedConnectionFactory;


    public SMSConnectionFactoryImpl(ConnectionManager connectionManager,
            SMSManagedConnectionFactory smsManagedConnectionFactory) {

        LOG.debug("created");
        this.connectionManager = connectionManager;
        this.smsManagedConnectionFactory = smsManagedConnectionFactory;
    }

    public SMSConnection getConnection() throws NamingException {

        LOG.debug("Getting connection");
        SMSConnection smsConnection = null;
        try {
            smsConnection = (SMSConnection) this.connectionManager.allocateConnection(this.smsManagedConnectionFactory,
                    null);
        } catch (ResourceException e) {
            throw new NamingException("Unable to get Connection: " + e);
        }
        return smsConnection;
    }
}
