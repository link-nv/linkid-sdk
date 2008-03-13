package net.link.safeonline.beid.bean;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import net.link.safeonline.beid.BeidConstants;
import net.link.safeonline.beid.Main;
import net.link.safeonline.device.sdk.DeviceManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;

@Stateful
@Name("main")
@LocalBinding(jndiBinding = BeidConstants.JNDI_PREFIX + "MainBean/local")
public class MainBean implements Main {

	private final static Log LOG = LogFactory.getLog(MainBean.class);

	private String redirectUrl;

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	@PostConstruct
	public void init() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		this.redirectUrl = DeviceManager
				.getSafeOnlineDeviceExitServiceUrl(session);
		try {
			facesContext.getExternalContext().redirect(this.redirectUrl);
		} catch (IOException e) {
			LOG.debug("failed to redirect to: " + this.redirectUrl);
		}
		LOG.debug("redirect to " + this.redirectUrl);
	}

	public String getRedirectUrl() {
		return this.redirectUrl;
	}

}
