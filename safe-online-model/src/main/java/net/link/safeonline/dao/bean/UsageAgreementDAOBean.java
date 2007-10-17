/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.dao.bean;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.UsageAgreementNotFoundException;
import net.link.safeonline.dao.UsageAgreementDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.entity.UsageAgreementTextEntity;
import net.link.safeonline.jpa.QueryObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class UsageAgreementDAOBean implements UsageAgreementDAO {

	private static final Log LOG = LogFactory
			.getLog(UsageAgreementDAOBean.class);

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	private UsageAgreementEntity.QueryInterface queryObject;

	@PostConstruct
	public void postConstructCallback() {
		this.queryObject = QueryObjectFactory.createQueryObject(
				this.entityManager, UsageAgreementEntity.QueryInterface.class);
	}

	public UsageAgreementEntity addUsageAgreement(
			ApplicationEntity application, Long usageAgreementVersion) {
		LOG.debug("add application usage agreement: " + application.getName()
				+ " version=" + usageAgreementVersion);
		UsageAgreementEntity usageAgreement = new UsageAgreementEntity(
				application, usageAgreementVersion);
		this.entityManager.persist(usageAgreement);
		return usageAgreement;
	}

	public UsageAgreementEntity getUsageAgreement(
			ApplicationEntity application, Long usageAgreementVersion)
			throws UsageAgreementNotFoundException {
		UsageAgreementPK usageAgreementPK = new UsageAgreementPK(application
				.getName(), usageAgreementVersion);
		UsageAgreementEntity usageAgreement = this.entityManager.find(
				UsageAgreementEntity.class, usageAgreementPK);
		if (null == usageAgreement)
			throw new UsageAgreementNotFoundException(usageAgreementVersion);
		return usageAgreement;
	}

	public List<UsageAgreementEntity> listUsageAgreements(
			ApplicationEntity application) {
		LOG.debug("list usage agreements for application: "
				+ application.getName());
		return this.queryObject.listUsageAgreements(application);
	}

	public void removeusageAgreement(UsageAgreementEntity usageAgreement) {
		this.entityManager.remove(usageAgreement);
	}

	public UsageAgreementTextEntity addUsageAgreementText(
			UsageAgreementEntity usageAgreement, String text, String language) {
		LOG.debug("add usage agreement text: language=" + language
				+ " version=" + usageAgreement.getUsageAgreementVersion());
		UsageAgreementTextEntity usageAgreementText = new UsageAgreementTextEntity(
				usageAgreement, text, language);
		this.entityManager.persist(usageAgreementText);
		usageAgreement.getUsageAgreementTexts().add(usageAgreementText);
		return usageAgreementText;
	}

	public void removeUsageAgreementText(
			UsageAgreementTextEntity usageAgreementText) {
		usageAgreementText.getUsageAgreement().getUsageAgreementTexts().remove(
				usageAgreementText);
		this.entityManager.remove(usageAgreementText);
	}
}
