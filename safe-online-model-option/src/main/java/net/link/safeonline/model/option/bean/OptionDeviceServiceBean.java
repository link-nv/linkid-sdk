/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option.bean;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link OptionDeviceServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 4, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
@Stateless
@LocalBinding(jndiBinding = OptionDeviceService.JNDI_BINDING)
public class OptionDeviceServiceBean implements OptionDeviceService {

    @PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
    private EntityManager          entityManager;

    @EJB(mappedName = HistoryDAO.JNDI_BINDING)
    private HistoryDAO             historyDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService     nodeMappingService;

    @EJB(mappedName = SubjectIdentifierDAO.JNDI_BINDING)
    private SubjectIdentifierDAO   subjectIdentifierDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    /**
     * {@inheritDoc}
     */
    public String authenticate(String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException {

        // check registration exists
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        if (getDisableAttribute(subject, imei).getBooleanValue())
            throw new DeviceDisabledException();

        return subject.getUserId();
    }

    /**
     * {@inheritDoc}
     */
    public void enable(String userId, String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);
        getDisableAttribute(subject, imei).setValue(false);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_ENABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                OptionConstants.OPTION_DEVICE_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void disable(String userId, String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        SubjectEntity subject = subjectService.getSubject(userId);
        getDisableAttribute(subject, imei).setValue(true);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_DISABLE, Collections.singletonMap(SafeOnlineConstants.DEVICE_PROPERTY,
                OptionConstants.OPTION_DEVICE_ID));
    }

    private AttributeEntity getDisableAttribute(SubjectEntity subject, String imei)
            throws DeviceRegistrationNotFoundException {

        try {
            AttributeEntity device = attributeManager.getCompoundWhere(subject, OptionConstants.OPTION_DEVICE_ATTRIBUTE,
                    OptionConstants.OPTION_IMEI_ATTRIBUTE, imei);

            return attributeManager.getCompoundMember(device, OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Option device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void register(String nodeName, String userId, String imei)
            throws NodeNotFoundException {

        /*
         * Check through node mapping if subject exists, if not, it is created.
         */
        SubjectEntity subject = nodeMappingService.getSubject(userId, nodeName);

        setAttributes(subject, imei);
        subjectIdentifierDAO.addSubjectIdentifier(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei, subject);

        historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REGISTRATION, Collections.singletonMap(
                SafeOnlineConstants.DEVICE_PROPERTY, OptionConstants.OPTION_DEVICE_ID));
    }

    private void setAttributes(SubjectEntity subject, String imei) {

        try {
            CompoundAttributeDO deviceAttribute = attributeManager.newCompound(OptionConstants.OPTION_DEVICE_ATTRIBUTE, subject);
            deviceAttribute.addAttribute(OptionConstants.OPTION_IMEI_ATTRIBUTE, imei);
            deviceAttribute.addAttribute(OptionConstants.OPTION_DEVICE_DISABLE_ATTRIBUTE, false);
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Option device not defined.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String imei)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException {

        // check registration exists
        SubjectEntity subject = subjectIdentifierDAO.findSubject(OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);
        if (null == subject)
            throw new SubjectNotFoundException();

        try {
            attributeManager.removeCompoundWhere(subject, OptionConstants.OPTION_DEVICE_ATTRIBUTE, OptionConstants.OPTION_IMEI_ATTRIBUTE,
                    imei);

            subjectIdentifierDAO.removeSubjectIdentifier(subject, OptionConstants.OPTION_IDENTIFIER_DOMAIN, imei);

            // flush and clear to commit and release the removed entities.
            entityManager.flush();
            entityManager.clear();

            historyDAO.addHistoryEntry(subject, HistoryEventType.DEVICE_REMOVAL, Collections.singletonMap(
                    SafeOnlineConstants.DEVICE_PROPERTY, OptionConstants.OPTION_DEVICE_ID));
        }

        catch (AttributeTypeNotFoundException e) {
            throw new InternalInconsistencyException("Attribute types for Option device not defined.", e);
        } catch (AttributeNotFoundException e) {
            throw new DeviceRegistrationNotFoundException(e);
        }
    }
}
