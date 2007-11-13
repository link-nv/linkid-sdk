/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SafeOnlineException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.dao.AllowedDeviceDAO;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationIdentityDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityPK;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeProviderEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.model.ApplicationIdentityManager;
import net.link.safeonline.model.UsageAgreementManager;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractInitBean implements Startable {

	protected final Log LOG = LogFactory.getLog(this.getClass());

	protected Map<String, AuthenticationDevice> authorizedUsers;

	protected static class AuthenticationDevice {
		final String password;

		final String[] weakMobiles;

		final String[] strongMobiles;

		public AuthenticationDevice(String password, String[] weakMobiles,
				String[] strongMobiles) {
			this.password = password;
			this.weakMobiles = weakMobiles;
			this.strongMobiles = strongMobiles;
		}
	}

	protected Map<String, String> applicationOwnersAndLogin;

	protected static class Application {
		final String name;

		final String description;

		final URL applicationUrl;

		final String owner;

		final boolean allowUserSubscription;

		final boolean removable;

		final X509Certificate certificate;

		final boolean idmappingAccess;

		final IdScopeType idScope;

		public Application(String name, String owner, String description,
				URL applicationUrl, boolean allowUserSubscription,
				boolean removable, X509Certificate certificate,
				boolean idmappingAccess, IdScopeType idScope) {
			this.name = name;
			this.owner = owner;
			this.description = description;
			this.applicationUrl = applicationUrl;
			this.allowUserSubscription = allowUserSubscription;
			this.removable = removable;
			this.certificate = certificate;
			this.idmappingAccess = idmappingAccess;
			this.idScope = idScope;
		}

		public Application(String name, String owner, String description,
				URL applicationUrl, boolean allowUserSubscription,
				boolean removable) {
			this(name, owner, description, applicationUrl,
					allowUserSubscription, removable, null, false,
					IdScopeType.USER);
		}

		public Application(String name, String owner,
				X509Certificate certificate, IdScopeType idScope) {
			this(name, owner, null, null, true, true, certificate, false,
					idScope);
		}
	}

	protected List<Application> registeredApplications;

	protected static class Subscription {
		final String user;

		final String application;

		final SubscriptionOwnerType subscriptionOwnerType;

		public Subscription(SubscriptionOwnerType subscriptionOwnerType,
				String user, String application) {
			this.subscriptionOwnerType = subscriptionOwnerType;
			this.user = user;
			this.application = application;
		}
	}

	protected static class Identity {
		final String application;

		final IdentityAttributeTypeDO[] identityAttributes;

		public Identity(String application,
				IdentityAttributeTypeDO[] identityAttributes) {
			this.application = application;
			this.identityAttributes = identityAttributes;
		}
	}

	protected static class UsageAgreement {
		final String application;

		final Set<UsageAgreementText> usageAgreementTexts;

		public UsageAgreement(String application) {
			this.application = application;
			this.usageAgreementTexts = new HashSet<UsageAgreementText>();
		}

		public void addUsageAgreementText(UsageAgreementText usageAgreementText) {
			this.usageAgreementTexts.add(usageAgreementText);
		}
	}

	protected static class UsageAgreementText {
		final String language;

		final String text;

		public UsageAgreementText(String language, String text) {
			this.language = language;
			this.text = text;
		}
	}

	protected static class Device {
		final String deviceName;

		final DeviceType deviceType;

		public Device(String deviceName, DeviceType deviceType) {
			this.deviceName = deviceName;
			this.deviceType = deviceType;
		}

	}

	protected List<Subscription> subscriptions;

	protected List<AttributeTypeEntity> attributeTypes;

	protected List<AttributeTypeDescriptionEntity> attributeTypeDescriptions;

	protected List<Identity> identities;

	protected List<UsageAgreement> usageAgreements;

	protected List<X509Certificate> trustedCertificates;

	protected List<AttributeProviderEntity> attributeProviders;

	protected Map<String, List<String>> allowedDevices;

	@EJB
	private ApplicationIdentityManager applicationIdentityService;

	@EJB
	private UsageAgreementManager usageAgreementManager;

	public abstract int getPriority();

	protected List<AttributeEntity> attributes;

	protected Map<Device, List<AttributeTypeEntity>> devices;

	public AbstractInitBean() {
		this.applicationOwnersAndLogin = new HashMap<String, String>();
		this.attributeTypes = new LinkedList<AttributeTypeEntity>();
		this.authorizedUsers = new HashMap<String, AuthenticationDevice>();
		this.registeredApplications = new LinkedList<Application>();
		this.subscriptions = new LinkedList<Subscription>();
		this.identities = new LinkedList<Identity>();
		this.usageAgreements = new LinkedList<UsageAgreement>();
		this.attributeTypeDescriptions = new LinkedList<AttributeTypeDescriptionEntity>();
		this.trustedCertificates = new LinkedList<X509Certificate>();
		this.attributeProviders = new LinkedList<AttributeProviderEntity>();
		this.attributes = new LinkedList<AttributeEntity>();
		this.devices = new HashMap<Device, List<AttributeTypeEntity>>();
		this.allowedDevices = new HashMap<String, List<String>>();
	}

	public void postStart() {
		try {
			this.LOG.debug("postStart");
			initTrustDomains();
			initAttributeTypes();
			initAttributeTypeDescriptions();
			initSubjects();
			initApplicationOwners();
			initApplications();
			initSubscriptions();
			initIdentities();
			initUsageAgreements();
			initApplicationTrustPoints();
			initAttributeProviders();
			initAttributes();
			initDevices();
			initAllowedDevices();
		} catch (SafeOnlineException e) {
			this.LOG.fatal("safeonline exception", e);
			throw new EJBException(e);
		}
	}

	public void preStop() {
		this.LOG.debug("preStop");
	}

	@EJB
	private SubjectService subjectService;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private ApplicationOwnerDAO applicationOwnerDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	protected TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	@EJB
	private ApplicationIdentityDAO applicationIdentityDAO;

	@EJB
	private UsageAgreementDAO usageAgreementDAO;

	@EJB
	private AttributeProviderDAO attributeProviderDAO;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private PasswordManager passwordManager;

	private void initApplicationTrustPoints() {
		for (X509Certificate certificate : this.trustedCertificates) {
			addCertificateAsTrustPoint(certificate);
		}
	}

	private void initAttributes() {
		for (AttributeEntity attribute : this.attributes) {
			String attributeTypeName = attribute.getPk().getAttributeType();
			String subjectLogin = attribute.getPk().getSubject();

			SubjectEntity subject;
			try {
				subject = this.subjectService
						.getSubjectFromUserName(subjectLogin);
			} catch (SubjectNotFoundException e) {
				throw new EJBException("subject not found: " + subjectLogin);
			}

			AttributeEntity existingAttribute = this.attributeDAO
					.findAttribute(attributeTypeName, subject);
			if (null != existingAttribute) {
				continue;
			}

			AttributeTypeEntity attributeType;
			try {
				attributeType = this.attributeTypeDAO
						.getAttributeType(attributeTypeName);
			} catch (AttributeTypeNotFoundException e) {
				throw new EJBException("attribute type not found: "
						+ attributeTypeName);
			}

			String stringValue = attribute.getStringValue();
			AttributeEntity persistentAttribute = this.attributeDAO
					.addAttribute(attributeType, subject, stringValue);
			persistentAttribute.setBooleanValue(attribute.getBooleanValue());
		}
	}

	private void initAttributeProviders() {
		for (AttributeProviderEntity attributeProvider : this.attributeProviders) {
			String applicationName = attributeProvider.getApplicationName();
			String attributeName = attributeProvider.getAttributeTypeName();
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			if (null == application) {
				throw new EJBException("application not found: "
						+ applicationName);
			}
			AttributeTypeEntity attributeType = this.attributeTypeDAO
					.findAttributeType(attributeName);
			if (null == attributeType) {
				throw new EJBException("attribute type not found: "
						+ attributeName);
			}
			AttributeProviderEntity existingAttributeProvider = this.attributeProviderDAO
					.findAttributeProvider(application, attributeType);
			if (null != existingAttributeProvider) {
				continue;
			}
			this.attributeProviderDAO.addAttributeProvider(application,
					attributeType);
		}
	}

	private void addCertificateAsTrustPoint(X509Certificate certificate) {
		TrustDomainEntity applicationTrustDomain = this.trustDomainDAO
				.findTrustDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		if (null == applicationTrustDomain) {
			this.LOG.fatal("application trust domain not found");
			return;
		}

		TrustPointEntity demoTrustPoint = this.trustPointDAO.findTrustPoint(
				applicationTrustDomain, certificate);
		if (null != demoTrustPoint) {
			try {
				/*
				 * In this case we still update the certificate.
				 */
				demoTrustPoint.setEncodedCert(certificate.getEncoded());
			} catch (CertificateEncodingException e) {
				this.LOG.error("cert encoding error");
			}
			return;
		}

		this.trustPointDAO.addTrustPoint(applicationTrustDomain, certificate);
	}

	private void initTrustDomains() {
		TrustDomainEntity applicationsTrustDomain = this.trustDomainDAO
				.findTrustDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		if (null != applicationsTrustDomain) {
			return;
		}

		applicationsTrustDomain = this.trustDomainDAO
				.addTrustDomain(
						SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN,
						true);
	}

	private void initAttributeTypes() {
		for (AttributeTypeEntity attributeType : this.attributeTypes) {
			if (null != this.attributeTypeDAO.findAttributeType(attributeType
					.getName())) {
				continue;
			}
			this.attributeTypeDAO.addAttributeType(attributeType);
		}
	}

	private void initAttributeTypeDescriptions() {
		for (AttributeTypeDescriptionEntity attributeTypeDescription : this.attributeTypeDescriptions) {
			AttributeTypeDescriptionEntity existingDescription = this.attributeTypeDAO
					.findDescription(attributeTypeDescription.getPk());
			if (null != existingDescription) {
				continue;
			}
			AttributeTypeEntity attributeType;
			try {
				attributeType = this.attributeTypeDAO
						.getAttributeType(attributeTypeDescription
								.getAttributeTypeName());
			} catch (AttributeTypeNotFoundException e) {
				throw new EJBException("attribute type not found: "
						+ attributeTypeDescription.getAttributeTypeName());
			}
			this.attributeTypeDAO.addAttributeTypeDescription(attributeType,
					attributeTypeDescription);
		}
	}

	private void initSubscriptions() {
		for (Subscription subscription : this.subscriptions) {
			String login = subscription.user;
			String applicationName = subscription.application;
			SubscriptionOwnerType subscriptionOwnerType = subscription.subscriptionOwnerType;
			SubjectEntity subject = this.subjectService
					.findSubjectFromUserName(login);
			ApplicationEntity application = this.applicationDAO
					.findApplication(applicationName);
			SubscriptionEntity subscriptionEntity = this.subscriptionDAO
					.findSubscription(subject, application);
			if (null != subscriptionEntity) {
				continue;
			}
			this.subscriptionDAO.addSubscription(subscriptionOwnerType,
					subject, application);
		}
	}

	private void initApplicationOwners() {
		for (Map.Entry<String, String> applicationOwnerAndLogin : this.applicationOwnersAndLogin
				.entrySet()) {
			String name = applicationOwnerAndLogin.getKey();
			String login = applicationOwnerAndLogin.getValue();
			if (null != this.applicationOwnerDAO.findApplicationOwner(name)) {
				continue;
			}
			SubjectEntity adminSubject = this.subjectService
					.findSubjectFromUserName(login);
			this.applicationOwnerDAO.addApplicationOwner(name, adminSubject);
		}
	}

	private void initApplications() {
		for (Application application : this.registeredApplications) {
			String applicationName = application.name;
			ApplicationEntity existingApplication = this.applicationDAO
					.findApplication(applicationName);
			if (null != existingApplication) {
				if (null != application.certificate) {
					existingApplication.setCertificate(application.certificate);
				}
				continue;
			}
			ApplicationOwnerEntity applicationOwner = this.applicationOwnerDAO
					.findApplicationOwner(application.owner);
			long identityVersion = ApplicationIdentityPK.INITIAL_IDENTITY_VERSION;
			long usageAgreementVersion = UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION;
			ApplicationEntity newApplication = this.applicationDAO
					.addApplication(applicationName, null, applicationOwner,
							application.allowUserSubscription,
							application.removable, application.description,
							application.applicationUrl,
							application.certificate, identityVersion,
							usageAgreementVersion);
			newApplication
					.setIdentifierMappingAllowed(application.idmappingAccess);
			newApplication.setIdScope(application.idScope);

			this.applicationIdentityDAO.addApplicationIdentity(newApplication,
					identityVersion);
		}
	}

	private void initSubjects() throws AttributeTypeNotFoundException {
		for (Map.Entry<String, AuthenticationDevice> authorizedUser : this.authorizedUsers
				.entrySet()) {
			String login = authorizedUser.getKey();
			SubjectEntity subject = this.subjectService
					.findSubjectFromUserName(login);
			if (null != subject) {
				continue;
			}
			subject = this.subjectService.addSubject(login);

			AuthenticationDevice device = authorizedUser.getValue();
			String password = device.password;
			try {
				this.passwordManager.setPassword(subject, password);
			} catch (PermissionDeniedException e) {
				throw new EJBException("could not set password");
			}
			if (null != device.weakMobiles) {
				for (String mobile : device.weakMobiles)
					addWeakMobile(mobile, subject);
			}
			if (null != device.strongMobiles) {
				for (String mobile : device.strongMobiles)
					addStrongMobile(mobile, subject);
			}
		}
	}

	private void addWeakMobile(String mobile, SubjectEntity subject) {
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(SafeOnlineConstants.WEAK_MOBILE_IDENTIFIER_DOMAIN,
						mobile);
		if (null != existingMappedSubject) {
			throw new EJBException("weak mobile " + mobile
					+ " already registered");
		}
		AttributeTypeEntity mobileAttributeType;
		try {
			mobileAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.WEAK_MOBILE_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("weak mobile attribute type not found");
		}
		this.attributeDAO.addAttribute(mobileAttributeType, subject, mobile);
		this.subjectIdentifierDAO.addSubjectIdentifier(
				SafeOnlineConstants.WEAK_MOBILE_IDENTIFIER_DOMAIN, mobile,
				subject);
	}

	private void addStrongMobile(String mobile, SubjectEntity subject) {
		SubjectEntity existingMappedSubject = this.subjectIdentifierDAO
				.findSubject(
						SafeOnlineConstants.STRONG_MOBILE_IDENTIFIER_DOMAIN,
						mobile);
		if (null != existingMappedSubject) {
			throw new EJBException("strong mobile " + mobile
					+ " already registered");
		}
		AttributeTypeEntity mobileAttributeType;
		try {
			mobileAttributeType = this.attributeTypeDAO
					.getAttributeType(SafeOnlineConstants.STRONG_MOBILE_ATTRIBUTE);
		} catch (AttributeTypeNotFoundException e) {
			throw new EJBException("strong mobile attribute type not found");
		}
		this.attributeDAO.addAttribute(mobileAttributeType, subject, mobile);
		this.subjectIdentifierDAO.addSubjectIdentifier(
				SafeOnlineConstants.STRONG_MOBILE_IDENTIFIER_DOMAIN, mobile,
				subject);
	}

	private void initIdentities() {
		for (Identity identity : this.identities) {
			try {
				this.applicationIdentityService.updateApplicationIdentity(
						identity.application, Arrays
								.asList(identity.identityAttributes));
			} catch (Exception e) {
				this.LOG.debug("Could not update application identity");
				throw new RuntimeException(
						"could not update the application identity: "
								+ e.getMessage(), e);
			}
		}
	}

	private void initUsageAgreements() {
		for (UsageAgreement usageAgreement : this.usageAgreements) {
			ApplicationEntity application = this.applicationDAO
					.findApplication(usageAgreement.application);
			UsageAgreementEntity usageAgreementEntity = this.usageAgreementDAO
					.addUsageAgreement(application,
							UsageAgreementPK.INITIAL_USAGE_AGREEMENT_VERSION);
			for (UsageAgreementText usageAgreementText : usageAgreement.usageAgreementTexts) {
				this.usageAgreementDAO.addUsageAgreementText(
						usageAgreementEntity, usageAgreementText.text,
						usageAgreementText.language);
			}
			try {
				this.usageAgreementManager.setUsageAgreement(application,
						UsageAgreementPK.INITIAL_USAGE_AGREEMENT_VERSION);
			} catch (UsageAgreementNotFoundException e) {
				this.LOG
						.debug("could not set usage agreement for application: "
								+ application.getName());
				throw new RuntimeException(
						"could not set usage agreement for application: "
								+ application.getName() + " : "
								+ e.getMessage(), e);
			}
		}
	}

	private void initDevices() {
		for (Device device : this.devices.keySet()) {
			DeviceEntity deviceEntity = this.deviceDAO
					.findDevice(device.deviceName);
			if (deviceEntity == null) {
				deviceEntity = this.deviceDAO.addDevice(device.deviceName,
						device.deviceType);
			}
			deviceEntity.setAttributeTypes(this.devices.get(device.deviceName));
		}
	}

	@EJB
	private AllowedDeviceDAO allowedDeviceDAO;

	private void initAllowedDevices() throws ApplicationNotFoundException,
			DeviceNotFoundException {
		for (String applicationName : this.allowedDevices.keySet()) {
			ApplicationEntity application = this.applicationDAO
					.getApplication(applicationName);
			application.setDeviceRestriction(true);
			List<String> deviceNames = this.allowedDevices.get(applicationName);
			for (String deviceName : deviceNames) {
				DeviceEntity device = this.deviceDAO.getDevice(deviceName);
				AllowedDeviceEntity allowedDevice = this.allowedDeviceDAO
						.findAllowedDevice(application, device);
				if (null == allowedDevice) {
					this.allowedDeviceDAO.addAllowedDevice(application, device,
							0);
				}
			}
		}
	}
}
