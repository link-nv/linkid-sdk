package net.link.safeonline.sms.ra.impl;

import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.sms.GSMModem;
import net.link.safeonline.sms.ra.SMSManagedConnection;

public class SMSManagedConnectionImpl implements SMSManagedConnection {

	private static final Log LOG = LogFactory
			.getLog(SMSManagedConnectionImpl.class);

	private transient GSMModem gsmModem;

	public SMSManagedConnectionImpl() {
		LOG.debug("Connection created, no init");
	}

	public SMSManagedConnectionImpl(String serialPort) {
		this.gsmModem = new GSMModem(serialPort);
		LOG.debug("Connection created: " + serialPort);
	}

	public void addConnectionEventListener(ConnectionEventListener arg0) {
		// empty
	}

	public void associateConnection(Object arg0) throws ResourceException {
		// empty
	}

	public void cleanup() throws ResourceException {
		LOG.debug("Connection cleanup: " + this.gsmModem.getSerialPortName());
	}

	public void destroy() throws ResourceException {
		LOG.debug("Connection destroy: " + this.gsmModem.getSerialPortName());
	}

	public Object getConnection(Subject arg0, ConnectionRequestInfo arg1)
			throws ResourceException {
		return this.gsmModem;
	}

	public LocalTransaction getLocalTransaction() throws ResourceException {
		return null;
	}

	public PrintWriter getLogWriter() throws ResourceException {
		return null;
	}

	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		return null;
	}

	public XAResource getXAResource() throws ResourceException {
		return null;
	}

	public void removeConnectionEventListener(ConnectionEventListener arg0) {
		// empty
	}

	public void setLogWriter(PrintWriter arg0) throws ResourceException {
		// empty
	}
}
