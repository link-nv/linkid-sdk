/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sms.ra.impl;

import java.io.PrintWriter;

import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import net.link.safeonline.sms.GSMModem;
import net.link.safeonline.sms.ra.SMSManagedConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SMSManagedConnectionImpl implements SMSManagedConnection {

    private static final Log   LOG = LogFactory.getLog(SMSManagedConnectionImpl.class);

    private transient GSMModem gsmModem;


    public SMSManagedConnectionImpl() {

        LOG.debug("Connection created, no init");
    }

    public SMSManagedConnectionImpl(String serialPort) {

        this.gsmModem = new GSMModem(serialPort);
        LOG.debug("Connection created: " + serialPort);
    }

    @SuppressWarnings("unused")
    public void addConnectionEventListener(ConnectionEventListener arg0) {

        // empty
    }

    @SuppressWarnings("unused")
    public void associateConnection(Object arg0) {

        // empty
    }

    public void cleanup() {

        LOG.debug("Connection cleanup: " + this.gsmModem.getSerialPortName());
    }

    public void destroy() {

        LOG.debug("Connection destroy: " + this.gsmModem.getSerialPortName());
    }

    @SuppressWarnings("unused")
    public Object getConnection(Subject arg0, ConnectionRequestInfo arg1) {

        return this.gsmModem;
    }

    public LocalTransaction getLocalTransaction() {

        return null;
    }

    public PrintWriter getLogWriter() {

        return null;
    }

    public ManagedConnectionMetaData getMetaData() {

        return null;
    }

    public XAResource getXAResource() {

        return null;
    }

    @SuppressWarnings("unused")
    public void removeConnectionEventListener(ConnectionEventListener arg0) {

        // empty
    }

    @SuppressWarnings("unused")
    public void setLogWriter(PrintWriter arg0) {

        // empty
    }
}
