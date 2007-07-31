package net.link.safeonline.sms.ra;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

public class SMSManagedConnectionFactory implements ManagedConnectionFactory,
		ResourceAdapterAssociation {

	private static final long serialVersionUID = 1L;

	private ResourceAdapter resourceAdapter = null;

	public Object createConnectionFactory() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object createConnectionFactory(ConnectionManager arg0)
			throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public ManagedConnection createManagedConnection(Subject arg0,
			ConnectionRequestInfo arg1) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public PrintWriter getLogWriter() throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public ManagedConnection matchManagedConnections(Set arg0, Subject arg1,
			ConnectionRequestInfo arg2) throws ResourceException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLogWriter(PrintWriter arg0) throws ResourceException {
		// TODO Auto-generated method stub

	}

	public ResourceAdapter getResourceAdapter() {
		return this.resourceAdapter;
	}

	public void setResourceAdapter(ResourceAdapter resourceAdapter)
			throws ResourceException {
		this.resourceAdapter = resourceAdapter;
	}
}
