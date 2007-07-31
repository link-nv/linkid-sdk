package net.link.safeonline.sms.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMSResourceAdapter implements ResourceAdapter {

	Log LOG = LogFactory.getLog(SMSResourceAdapter.class);

	public void endpointActivation(MessageEndpointFactory arg0,
			ActivationSpec arg1) throws ResourceException {
		LOG.debug("endpointActivation called");

	}

	public void endpointDeactivation(MessageEndpointFactory arg0,
			ActivationSpec arg1) {
		LOG.debug("endpointDeactivation called");

	}

	public XAResource[] getXAResources(ActivationSpec[] arg0)
			throws ResourceException {
		LOG.debug("getXAResources called");
		return null;
	}

	public void start(BootstrapContext arg0)
			throws ResourceAdapterInternalException {
		LOG.debug("start called");

	}

	public void stop() {
		LOG.debug("stop called");

	}

}
