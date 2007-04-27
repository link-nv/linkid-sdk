/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.security.cert.X509Certificate;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationServiceRemote;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.model.PkiProvider;
import net.link.safeonline.model.PkiProviderManager;
import net.link.safeonline.model.PkiValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.loginCounter;

;

/**
 * Implementation of authentication service interface. This component does not
 * live within the SafeOnline core security domain (chicken-egg problem).
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class AuthenticationServiceBean implements AuthenticationService,
		AuthenticationServiceRemote {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationServiceBean.class);

	@EJB
	private SubjectDAO entityDAO;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private SubscriptionDAO subscriptionDAO;

	@EJB
	private HistoryDAO historyDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private PkiProviderManager pkiProviderManager;

	@EJB
	private PkiValidator pkiValidator;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private StatisticDAO statisticDAO;

	@EJB
	private StatisticDataPointDAO statisticDataPointDAO;

	public boolean authenticate(String applicationName, String login,
			String password) throws SubjectNotFoundException,
			ApplicationNotFoundException, SubscriptionNotFoundException {
		LOG.debug("authenticate \"" + login + "\" for \"" + applicationName
				+ "\"");

		// TODO: aspectize the input validation
		if (null == login) {
			throw new IllegalArgumentException("login is null");
		}

		if (null == password) {
			throw new IllegalArgumentException("password is null");
		}

		SubjectEntity subject = this.entityDAO.getSubject(login);

		AttributeEntity passwordAttribute = this.attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login);
		if (null == passwordAttribute) {
			String event = "password attribute not present for subject "
					+ login;
			addHistoryEntry(subject, event);
			throw new EJBException(event);
		}

		String expectedPassword = passwordAttribute.getStringValue();
		if (null == expectedPassword) {
			String event = "actual password is null for subject " + login;
			addHistoryEntry(subject, event);
			return false;
		}

		if (!expectedPassword.equals(password)) {
			String event = "incorrect password for application: "
					+ applicationName;
			addHistoryEntry(subject, event);
			return false;
		}

		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationName);
		if (null == application) {
			String event = "application not found: " + applicationName;
			addHistoryEntry(subject, event);
			throw new ApplicationNotFoundException();
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			String event = "subscription not found for application: "
					+ applicationName;
			addHistoryEntry(subject, event);
			throw new SubscriptionNotFoundException();
		}

		addHistoryEntry(subject, "authenticated for application "
				+ applicationName);

		this.subscriptionDAO.loggedIn(subscription);
		this.addLoginTick(application);

		return true;
	}

	private void addHistoryEntry(SubjectEntity subject, String event) {
		Date now = new Date();
		this.historyDAO.addHistoryEntry(now, subject, event);
	}

	public String authenticate(String sessionId,
			byte[] authenticationStatementData)
			throws ArgumentIntegrityException, TrustDomainNotFoundException,
			SubjectNotFoundException, SubscriptionNotFoundException,
			ApplicationNotFoundException {
		LOG.debug("authenticate session: " + sessionId);
		AuthenticationStatement authenticationStatement = new AuthenticationStatement(
				authenticationStatementData);

		X509Certificate certificate = authenticationStatement.verifyIntegrity();
		if (null == certificate) {
			throw new ArgumentIntegrityException();
		}

		PkiProvider pkiProvider = this.pkiProviderManager
				.findPkiProvider(certificate);
		if (null == pkiProvider) {
			throw new ArgumentIntegrityException();
		}
		TrustDomainEntity trustDomain = pkiProvider.getTrustDomain();
		boolean validationResult = this.pkiValidator.validateCertificate(
				trustDomain, certificate);
		if (false == validationResult) {
			throw new ArgumentIntegrityException();
		}

		if (false == sessionId.equals(authenticationStatement.getSessionId())) {
			throw new ArgumentIntegrityException();
		}

		String identifierDomainName = pkiProvider.getIdentifierDomainName();
		String identifier = pkiProvider.getSubjectIdentifier(certificate);
		SubjectEntity subject = this.subjectIdentifierDAO.findSubject(
				identifierDomainName, identifier);
		if (null == subject) {
			String event = "no subject was found for the given user certificate";
			LOG.warn(event);
			throw new SubjectNotFoundException();
		}
		LOG.debug("subject: " + subject);

		String applicationId = authenticationStatement.getApplicationId();
		ApplicationEntity application = this.applicationDAO
				.findApplication(applicationId);
		if (null == application) {
			String event = "application not found: " + applicationId;
			addHistoryEntry(subject, event);
			throw new ApplicationNotFoundException();
		}

		SubscriptionEntity subscription = this.subscriptionDAO
				.findSubscription(subject, application);
		if (null == subscription) {
			String event = "subscription not found for application: "
					+ applicationId;
			addHistoryEntry(subject, event);
			throw new SubscriptionNotFoundException();
		}

		addHistoryEntry(subject, "authenticated subject " + subject
				+ " for application " + applicationId);

		this.subscriptionDAO.loggedIn(subscription);
		this.addLoginTick(application);

		return subject.getLogin();
	}

	public String authenticate(X509Certificate certificate)
			throws ApplicationNotFoundException {
		ApplicationEntity application = this.applicationDAO
				.getApplication(certificate);
		String applicationName = application.getName();
		LOG.debug("authenticated application: " + applicationName);
		return applicationName;
	}

	private void addLoginTick(ApplicationEntity application) {
		StatisticEntity statistic = this.statisticDAO
				.findOrAddStatisticByNameDomainAndApplication(statisticName,
						statisticDomain, application);

		StatisticDataPointEntity dp = this.statisticDataPointDAO
				.findOrAddStatisticDataPoint(loginCounter, statistic);

		long count = dp.getX();
		dp.setX(count + 1);
	}

}
