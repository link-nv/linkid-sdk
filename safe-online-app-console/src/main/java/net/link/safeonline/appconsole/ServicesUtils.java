package net.link.safeonline.appconsole;

import java.net.ConnectException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingworker.SwingWorker;

import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;

public class ServicesUtils extends Observable {

	private static final Log LOG = LogFactory.getLog(ServicesUtils.class);

	private static ServicesUtils servicesUtilsInstance = null;

	private ApplicationConsoleManager consoleManager = ApplicationConsoleManager
			.getInstance();

	private ServicesUtils() {
	}

	public static ServicesUtils getInstance() {
		if (null == servicesUtilsInstance)
			servicesUtilsInstance = new ServicesUtils();
		return servicesUtilsInstance;
	}

	public void getAttributes(final String user) {
		SwingWorker<Map<String, Object>, Object> worker = new SwingWorker<Map<String, Object>, Object>() {

			@Override
			protected Map<String, Object> doInBackground() throws Exception {
				Map<String, Object> attributes = null;
				AttributeClient attributeClient = new AttributeClientImpl(
						consoleManager.getLocation(),
						(X509Certificate) consoleManager.getIdentity()
								.getCertificate(), consoleManager.getIdentity()
								.getPrivateKey());
				attributes = attributeClient.getAttributeValues(user);
				consoleManager.setMessageAccessor(attributeClient);
				consoleManager.pushMessages();
				return attributes;
			}

			@Override
			protected void done() {
				setChanged();
				try {
					notifyObservers(get());
				} catch (InterruptedException e) {
					ConsoleError error = new ConsoleError(
							"Retrieving attributes interrupted ...");
					setChanged();
					notifyObservers(error);
					e.printStackTrace();
				} catch (ExecutionException e) {
					ConsoleError error = new ConsoleError(
							"Retrieving attributes failed to execute ...");
					setChanged();
					notifyObservers(error);
					e.printStackTrace();
				}
			}

		};
		worker.execute();
	}
}
