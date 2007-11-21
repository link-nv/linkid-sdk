/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.AccountMergingDO;
import net.link.safeonline.service.AccountMergingService;
import net.link.safeonline.service.SubscriptionDO;
import net.link.safeonline.user.UserConstants;
import net.link.safeonline.user.merge.MergeOverview;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.core.ResourceBundle;

@Name("mergeOverview")
@Stateful
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX
		+ "MergeOverviewBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class MergeOverviewBean implements MergeOverview {

	private static final Log LOG = LogFactory.getLog(MergeOverviewBean.class);

	private static final String PROVEN_DEVICE_LIST_NAME = "provenDevices";

	private static final String PRESERVED_SUBSCRIPTIONS_LIST_NAME = "preservedSubscriptions";

	private static final String IMPORTED_SUBSCRIPTIONS_LIST_NAME = "importedSubscriptions";

	private static final String PRESERVED_ATTRIBUTES_LIST_NAME = "preservedAttributes";

	private static final String IMPORTED_ATTRIBUTES_LIST_NAME = "importedAttributes";

	private static final String MERGED_ATTRIBUTES_LIST_NAME = "mergedAttributes";

	private static final String CHOOSABLE_ATTRIBUTES_LIST_NAME = "choosableAttributes";

	@In(create = true)
	private FacesMessages facesMessages;

	@In(required = true)
	private String source;

	@In(required = true)
	private ReAuthenticationService reAuthenticationService;

	@EJB
	private AccountMergingService accountMergingService;

	private AccountMergingDO accountMergingDO;

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void init() {
		LOG.debug("init");
		try {
			this.accountMergingDO = this.accountMergingService
					.getAccountMergingDO(this.source);
			this.accountMergingDO.log();
		} catch (SubjectNotFoundException e) {
			LOG.debug("source user " + this.source + " not found");
			this.facesMessages.addToControlFromResourceBundle("user",
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "member attribute type not found";
			LOG.debug(msg);
			this.facesMessages.addToControlFromResourceBundle("name",
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeMemberNotFound");
			return;
		} catch (ApplicationNotFoundException e) {
			LOG.error("application not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
		} catch (EmptyDevicePolicyException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorEmptyDevicePolicy");
			LOG.error("empty device policy");
		}
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(PROVEN_DEVICE_LIST_NAME)
	public List<String> provenDeviceListFactory() {
		LOG.debug("provenDeviceListFactory");
		List<AuthenticationDevice> devices = this.reAuthenticationService
				.getAuthenticatedDevices();
		return deviceNameDecoration(devices);
	}

	private List<String> deviceNameDecoration(List<AuthenticationDevice> devices) {
		List<String> decoratedDevices = new LinkedList<String>();
		for (AuthenticationDevice device : devices) {
			String deviceId = device.getDeviceName();
			try {
				String deviceName = ResourceBundle.instance().getString(
						deviceId);
				if (null == deviceName)
					deviceName = deviceId;
				decoratedDevices.add(deviceName);
			} catch (MissingResourceException e) {
				LOG.debug("resource not found: " + deviceId);
			}
		}
		return decoratedDevices;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(PRESERVED_SUBSCRIPTIONS_LIST_NAME)
	public List<SubscriptionEntity> preservedSubscriptionsListFactory() {
		LOG.debug("preservedSubscriptionsListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getPreservedSubscriptions();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(IMPORTED_SUBSCRIPTIONS_LIST_NAME)
	public List<SubscriptionDO> importedSubscriptionsListFactory() {
		LOG.debug("importedSubscriptionsListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getImportedSubscriptions();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(PRESERVED_ATTRIBUTES_LIST_NAME)
	public List<AttributeDO> preservedAttributesListFactory() {
		LOG.debug("preservedAttributesListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getVisiblePreservedAttributes();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(IMPORTED_ATTRIBUTES_LIST_NAME)
	public List<AttributeDO> importedAttributesListFactory() {
		LOG.debug("importedAttributesListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getVisibleImportedAttributes();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(MERGED_ATTRIBUTES_LIST_NAME)
	public List<AttributeDO> mergedAttributesListFactory() {
		LOG.debug("mergedAttributesListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getVisibleMergedAttributes();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(CHOOSABLE_ATTRIBUTES_LIST_NAME)
	public List<AttributeDO> choosableAttributesListFactory() {
		LOG.debug("choosableAttributesListFactory");
		if (null == this.accountMergingDO)
			return null;
		return this.accountMergingDO.getMergedAttributes();
	}
}
