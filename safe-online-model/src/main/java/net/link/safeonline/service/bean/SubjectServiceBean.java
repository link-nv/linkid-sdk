/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.IdGenerator;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class SubjectServiceBean implements SubjectService {

	@EJB
	private SubjectDAO subjectDAO;

	@EJB
	private AttributeDAO attributeDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@EJB
	private SubjectIdentifierDAO subjectIdentifierDAO;

	@EJB
	private IdGenerator idGenerator;

	private static final Log LOG = LogFactory.getLog(SubjectServiceBean.class);

	public SubjectEntity addSubject(String login)
			throws AttributeTypeNotFoundException {
		LOG.debug("add subject: " + login);

		String userId = idGenerator.generateId();

		SubjectEntity subject = this.subjectDAO.addSubject(userId);

		this.subjectIdentifierDAO.addSubjectIdentifier(
				SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, login, subject);

		AttributeTypeEntity attributeType = this.attributeTypeDAO
				.getAttributeType(SafeOnlineConstants.LOGIN_ATTRIBTUE);

		this.attributeDAO.addAttribute(attributeType, subject, login);

		return subject;
	}

	public SubjectEntity findSubject(String userId) {
		LOG.debug("find subject user ID: " + userId);
		return this.subjectDAO.findSubject(userId);
	}

	public SubjectEntity findSubjectFromUserName(String login) {
		LOG.debug("find subject login: " + login);
		return this.subjectIdentifierDAO.findSubject(
				SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, login);
	}

	public SubjectEntity getSubject(String userId)
			throws SubjectNotFoundException {
		LOG.debug("get subject user id: " + userId);
		return this.subjectDAO.getSubject(userId);
	}

	public String getSubjectLogin(String userId) {
		LOG.debug("get subject user id: " + userId);
		SubjectEntity subject = this.subjectDAO.findSubject(userId);
		if (null == subject)
			return null;
		AttributeEntity loginAttribute;
		try {
			loginAttribute = this.attributeDAO.getAttribute(
					SafeOnlineConstants.LOGIN_ATTRIBTUE, subject.getUserId());
		} catch (AttributeNotFoundException e) {
			LOG.debug("login attribute not found", e);
			return null;
		}
		return loginAttribute.getStringValue();
	}

	public SubjectEntity getSubjectFromUserName(String login)
			throws SubjectNotFoundException {
		LOG.debug("get subject login: " + login);
		return this.subjectIdentifierDAO.findSubject(
				SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, login);

	}

	public List<String> listUsers() {
		List<String> userList = new LinkedList<String>();
		List<String> userIdList = this.subjectDAO.listUsers();
		for (String userId : userIdList) {
			String user = this.getSubjectLogin(userId);
			userList.add(user);
		}
		return userList;
	}
}
