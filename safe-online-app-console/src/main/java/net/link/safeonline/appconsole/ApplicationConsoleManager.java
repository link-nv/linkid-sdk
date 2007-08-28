package net.link.safeonline.appconsole;

import static net.link.safeonline.appconsole.Messages.IDENTITY;
import static net.link.safeonline.appconsole.Messages.LOCATION;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Observable;

import net.link.safeonline.sdk.ws.MessageAccessor;

import org.w3c.dom.Document;

/**
 * 
 * Application console data class, observable for all the views
 * 
 */
public class ApplicationConsoleManager extends Observable {

	private String identityLabelPrefix = IDENTITY.getMessage() + " : ";
	private String locationLabelPrefix = LOCATION.getMessage() + " : ";

	private String identityLabel = null;
	private String locationLabel = null;
	private String location = "localhost";
	private PrivateKeyEntry identity = null;

	private boolean captureMessages = true;
	private MessageAccessor messageAccessor = null;

	private static ApplicationConsoleManager manager = null;

	public static ApplicationConsoleManager getInstance() {
		if (null == manager)
			manager = new ApplicationConsoleManager();
		return manager;
	}

	private ApplicationConsoleManager() {
		identityLabel = identityLabelPrefix;
		locationLabel = locationLabelPrefix + location;
	}

	public String getIdentityLabel() {
		return identityLabel;
	}

	public String getLocationLabel() {
		return locationLabel;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
		this.locationLabel = locationLabelPrefix + location;
		setChanged();
		notifyObservers();

	}

	public PrivateKeyEntry getIdentity() {
		return identity;
	}

	public void setIdentity(PrivateKeyEntry identity) {
		if (null == identity) {
			this.identity = null;
			this.identityLabel = identityLabelPrefix;
		} else {
			this.identity = identity;
			this.identityLabel = identityLabelPrefix
					+ ((X509Certificate) identity.getCertificate())
							.getSubjectX500Principal().getName();
		}
		setChanged();
		notifyObservers();
	}

	public void setMessageAccessor(MessageAccessor messageAccessor) {
		this.messageAccessor = messageAccessor;
		if (captureMessages) {
			setChanged();
			notifyObservers(messageAccessor);
		}
	}

	public void setCaptureMessages(boolean captureMessages) {
		this.captureMessages = captureMessages;
	}

	public Document getInboundMessage() {
		if (!captureMessages)
			return null;
		return messageAccessor.getInboundMessage();
	}

	public Document getOutboundMessage() {
		if (!captureMessages)
			return null;
		return messageAccessor.getOutboundMessage();
	}

}
