/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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
import net.link.safeonline.service.SubjectServiceRemote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
@LocalBinding(jndiBinding = SubjectService.JNDI_BINDING)
public class SubjectServiceBean implements SubjectService, SubjectServiceRemote {

    @EJB
    private SubjectDAO           subjectDAO;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB
    private IdGenerator          idGenerator;

    private static final Log     LOG = LogFactory.getLog(SubjectServiceBean.class);


    public SubjectEntity addSubject(String login) throws AttributeTypeNotFoundException {

        LOG.debug("add subject: " + login);

        String userId = this.idGenerator.generateId();
        SubjectEntity subject = this.subjectDAO.addSubject(userId);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.LOGIN_ATTRIBTUE);

        this.subjectIdentifierDAO.addSubjectIdentifier(SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, login, subject);
        this.attributeDAO.addAttribute(attributeType, subject, login);

        return subject;
    }

    public SubjectEntity addSubjectWithoutLogin(String userId) {

        LOG.debug("add subject without login attribute: " + userId);
        return this.subjectDAO.addSubject(userId);

    }

    public SubjectEntity findSubject(String userId) {

        LOG.debug("find subject user ID: " + userId);
        return this.subjectDAO.findSubject(userId);
    }

    public SubjectEntity findSubjectFromUserName(String login) {

        LOG.debug("find subject login: " + login);
        return this.subjectIdentifierDAO.findSubject(SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN, login);
    }

    public SubjectEntity getSubject(String userId) throws SubjectNotFoundException {

        LOG.debug("get subject user id: " + userId);
        return this.subjectDAO.getSubject(userId);
    }

    /*
     * This can be called from e.g. HelpdeskLogger after an exception has been thrown so needs a transaction created
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String getExceptionSubjectLogin(String userId) {

        return getSubjectLogin(userId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SubjectEntity findExceptionSubject(String userId) {

        return findSubject(userId);
    }

    public String getSubjectLogin(String userId) {

        LOG.debug("get subject user login: " + userId);
        SubjectEntity subject = this.subjectDAO.findSubject(userId);
        if (null == subject)
            return null;

        try {
            AttributeEntity loginAttribute = this.attributeDAO.getAttribute(SafeOnlineConstants.LOGIN_ATTRIBTUE, subject);
            return loginAttribute.getStringValue();
        }

        catch (AttributeNotFoundException e) {
            LOG.debug("login attribute not found", e);
        }

        return null;
    }

    public SubjectEntity getSubjectFromUserName(String login) throws SubjectNotFoundException {

        LOG.debug("get subject login: " + login);
        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(SafeOnlineConstants.LOGIN_IDENTIFIER_DOMAIN,
                login);
        if (null == subject)
            throw new SubjectNotFoundException();

        return subject;
    }

    public List<String> listUsers(String prefix) throws AttributeTypeNotFoundException {

        List<String> userList = new LinkedList<String>();
        AttributeTypeEntity loginAttributeType = this.attributeTypeDAO.getAttributeType(SafeOnlineConstants.LOGIN_ATTRIBTUE);
        List<AttributeEntity> loginAttributes = this.attributeDAO.listAttributes(prefix, loginAttributeType);

        for (AttributeEntity loginAttribute : loginAttributes) {
            userList.add(loginAttribute.getStringValue());
        }

        return userList;
    }
}
