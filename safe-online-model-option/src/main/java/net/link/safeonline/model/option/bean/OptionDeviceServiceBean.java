/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.model.option.OptionDeviceServiceRemote;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;
import net.link.safeonline.service.SubjectService;


/**
 * <h2>{@link OptionDeviceServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@Stateless
public class OptionDeviceServiceBean implements OptionDeviceService, OptionDeviceServiceRemote {

    @EJB
    private SubjectIdentifierDAO subjectIdentifierDAO;

    @EJB
    private AttributeDAO         attributeDAO;

    @EJB
    private AttributeTypeDAO     attributeTypeDAO;

    @EJB
    private SecurityAuditLogger  securityAuditLogger;

    @EJB
    private SubjectService       subjectService;

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager        entityManager;


    /**
     * {@inheritDoc}
     */
    public String authenticate(String imei, String pin) throws SubjectNotFoundException, OptionAuthenticationException,
            OptionRegistrationException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        authenticate(subject, imei, pin);

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void register(String userId, String imei, String pin) throws OptionAuthenticationException,
            OptionRegistrationException {

        SubjectEntity subject = this.subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null != subject) {
            authenticate(subject, imei, pin);
            removeRegistration(subject);
            this.entityManager.flush();
            this.entityManager.clear();
        }

        subject = this.subjectService.findSubject(userId);
        if (null == subject) {
            subject = this.subjectService.addSubjectWithoutLogin(userId);
        }

        AttributeTypeEntity imeiType = this.attributeTypeDAO.findAttributeType(OptionConstants.IMEI_OPTION_ATTRIBUTE);
        AttributeTypeEntity pinType = this.attributeTypeDAO.findAttributeType(OptionConstants.PIN_OPTION_ATTRIBUTE);

        AttributeEntity imeiAttribute = this.attributeDAO.findAttribute(imeiType, subject);
        if (null == imeiAttribute) {
            imeiAttribute = this.attributeDAO.addAttribute(imeiType, subject);
        }
        imeiAttribute.setStringValue(imei);

        AttributeEntity pinAttribute = this.attributeDAO.findAttribute(pinType, subject);
        if (null == pinAttribute) {
            pinAttribute = this.attributeDAO.addAttribute(pinType, subject);
        }
        pinAttribute.setStringValue(pin);

        this.subjectIdentifierDAO.addSubjectIdentifier(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei, subject);

    }

    /**
     * {@inheritDoc}
     */
    public void remove(String userId, String imei, String pin) throws OptionAuthenticationException,
            OptionRegistrationException, SubjectNotFoundException {

        String assignedSubject = authenticate(imei, pin);

        if (!assignedSubject.equals(userId))
            throw new OptionRegistrationException();

        SubjectEntity subject = this.subjectService.findSubject(userId);
        removeRegistration(subject);
    }

    private void authenticate(SubjectEntity subject, String givenImei, String givenPin)
            throws OptionRegistrationException, OptionAuthenticationException {

        AttributeEntity storedImei = this.attributeDAO.findAttribute(OptionConstants.IMEI_OPTION_ATTRIBUTE, subject);
        AttributeEntity storedPin = this.attributeDAO.findAttribute(OptionConstants.PIN_OPTION_ATTRIBUTE, subject);

        if (null == storedImei || null == storedPin)
            throw new OptionRegistrationException();
        if (!storedImei.getStringValue().equals(givenImei))
            throw new OptionRegistrationException();
        if (!storedPin.getStringValue().equals(givenPin)) {
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, subject.getUserId(),
                    "incorrect PIN");
            throw new OptionAuthenticationException();
        }
    }

    private void removeRegistration(SubjectEntity subject) {

        AttributeEntity storedImei = this.attributeDAO.findAttribute(OptionConstants.IMEI_OPTION_ATTRIBUTE, subject);
        AttributeEntity storedPin = this.attributeDAO.findAttribute(OptionConstants.PIN_OPTION_ATTRIBUTE, subject);

        this.attributeDAO.removeAttribute(storedImei);
        this.attributeDAO.removeAttribute(storedPin);

        this.subjectIdentifierDAO.removeSubjectIdentifier(subject, OptionConstants.OPTION_IDENTIFIER_DOMAIN, storedImei
                .getStringValue());
    }

}
