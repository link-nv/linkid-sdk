package net.link.safeonline.service.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.model.Devices;
import net.link.safeonline.service.ApplicationOwnerAccessControlInterceptor;
import net.link.safeonline.service.DeviceService;

import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class DeviceServiceBean implements DeviceService {

	@EJB
	private Devices devices;

	@RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.USER_ROLE })
	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	public List<AllowedDeviceEntity> listAllowedDevices(
			ApplicationEntity application) {
		return this.devices.listAllowedDevices(application);
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	public List<DeviceEntity> listDevices() {
		return this.devices.listDevices();
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	public void setAllowedDevices(ApplicationEntity application,
			List<AllowedDeviceEntity> allowedDeviceList) {
		this.devices.setAllowedDevices(application, allowedDeviceList);
	}

}
