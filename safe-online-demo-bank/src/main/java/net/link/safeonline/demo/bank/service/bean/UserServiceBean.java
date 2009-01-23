/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.keystore.DemoBankKeyStoreUtils;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link UserServiceBean}<br>
 * <sub>Service bean for {@link UserService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = UserService.JNDI_BINDING)
public class UserServiceBean extends AbstractBankServiceBean implements UserService {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankUserEntity> getUsers() {

        return em.createNamedQuery(BankUserEntity.getAll).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity addUser(String bankId, String userName) {

        BankUserEntity userEntity = new BankUserEntity(bankId, userName);
        em.persist(userEntity);

        return userEntity;
    }

    /**
     * {@inheritDoc}
     */
    public void removeUser(BankUserEntity user) {

        em.remove(attach(user));
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity getBankUser(String bankId) {

        return (BankUserEntity) em.createNamedQuery(BankUserEntity.getByBankId).setParameter("bankId", bankId).getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity findBankUser(String bankId) {

        try {
            return getBankUser(bankId);
        }

        catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity getOLASUser(String olasId) {

        BankUserEntity userEntity = findOLASUser(olasId);

        if (userEntity == null) {
            String bankId = olasId;
            while (true) {
                if (findBankUser(bankId) == null) {
                    break;
                }

                bankId += "_";
            }

            userEntity = new BankUserEntity(bankId, olasId, olasId);
            em.persist(userEntity);
        }

        return userEntity;
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity findOLASUser(String olasId) {

        try {
            return (BankUserEntity) em.createNamedQuery(BankUserEntity.getByOlasId).setParameter("olasId", olasId).getSingleResult();
        }

        catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity linkOLASUser(BankUserEntity user, String olasId, HttpServletRequest httpRequest) {

        BankUserEntity olasEntity = findOLASUser(olasId);
        if (olasEntity != null) {
            if (olasEntity.getBankId() == user.getBankId())
                // Already linked. Odd but meh.
                return olasEntity;

            throw new IllegalStateException("The OLAS user we're linking with is already registered with the bank.");
        }

        BankUserEntity userEntity = attach(user);
        userEntity.setOlasId(olasId);

        return updateUser(userEntity, httpRequest);
    }

    /**
     * {@inheritDoc}
     */
    public void unlinkOLASUser(BankUserEntity user) {

        attach(user).setOlasId(null);
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity updateUser(BankUserEntity user, HttpServletRequest httpRequest) {

        try {
            AttributeClient attributeClient = WicketUtil.getOLASAttributeService(httpRequest, DemoBankKeyStoreUtils.getPrivateKeyEntry());

            BankUserEntity userEntity = attach(user);
            if (userEntity.getOlasId() == null)
                return userEntity;

            // OLAS username of the user.
            String name = attributeClient.getAttributeValue(userEntity.getOlasId(), DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
            userEntity.setName(name);

            return userEntity;
        }

        catch (AttributeNotFoundException e) {
            LOG.error("attribute not found: ", e);
            throw new RuntimeException(e);
        } catch (RequestDeniedException e) {
            LOG.error("request denied: ", e);
            throw new RuntimeException(e);
        } catch (WSClientTransportException e) {
            LOG.error("Connection error. Check your SSL setup.", e);
            throw new RuntimeException(e);
        } catch (AttributeUnavailableException e) {
            LOG.error("Attribute unavailable", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankAccountEntity> getAccounts(BankUserEntity user) {

        return em.createNamedQuery(BankAccountEntity.getByUser).setParameter("user", attach(user)).getResultList();
    }
}
