package net.link.safeonline.messaging.bean;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "EmailConfigurationProviderBean")
public class EmailConfigurationProviderBean implements ConfigurationProvider {

	private Map<String, String> config = null;

	private static final String groupName = "E-mail configuration";

	public static final String emailServer = "Outgoing mail server";

	public static final String emailServerDefault = "127.0.0.1";

	public static final String emailServerPort = "Mail server port";

	public static final String emailServerPortDefault = "25";

	public static final String emailSender = "E-mail sender";

	public static final String emailSenderDefault = "Safe Online";

	public static final String emailSubjectPrefix = "Subject prefix";

	public static final String emailSubjectPrefixDefault = "[Safe Online]";

	public EmailConfigurationProviderBean() {
		this.config = new TreeMap<String, String>();
		config.put(emailServer, emailServerDefault);
		config.put(emailServerPort, emailServerPortDefault);
		config.put(emailSender, emailSenderDefault);
		config.put(emailSubjectPrefix, emailSubjectPrefixDefault);
	}

	public Map<String, String> getConfigurationParameters() {
		return config;
	}

	public String getGroupName() {
		return groupName;
	}

}
