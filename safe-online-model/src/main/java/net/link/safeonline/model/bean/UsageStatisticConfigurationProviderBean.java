package net.link.safeonline.model.bean;

import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;

import net.link.safeonline.ConfigurationProvider;

@Stateless
@Local(ConfigurationProvider.class)
@LocalBinding(jndiBinding = ConfigurationProvider.JNDI_PREFIX + "/"
		+ "UsageStatisticConfigurationProviderBean")
public class UsageStatisticConfigurationProviderBean implements
		ConfigurationProvider {

	private Map<String, String> config = null;

	public static final String activeLimitInMillis = "Active user limit (ms)";

	public static final String activeLimitInMillisDefault = "600000";

	public static final String ageInMillis = "Keep stats for (ms)";

	public static final String ageInMillisDefault = "6000000";

	private static final String groupName = "User Statistic Generation";

	public UsageStatisticConfigurationProviderBean() {
		this.config = new TreeMap<String, String>();
		this.config.put(activeLimitInMillis, activeLimitInMillisDefault);
		this.config.put(ageInMillis, ageInMillisDefault);
	}

	public Map<String, String> getConfigurationParameters() {
		return config;
	}

	public String getGroupName() {
		return groupName;
	}

}
