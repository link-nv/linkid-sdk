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
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

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
@LocalBinding(jndiBinding = UserService.BINDING)
public class UserServiceBean extends AbstractBankServiceBean implements UserService {

    /**
     * {@inheritDoc}
     */
    public BankUserEntity getBankUser(String bankId) {

        try {
            return (BankUserEntity) this.em.createNamedQuery(BankUserEntity.getByBankId).setParameter("bankId", bankId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity getOLASUser(String olasId) {

        try {
            return (BankUserEntity) this.em.createNamedQuery(BankUserEntity.getByOlasId).setParameter("olasId", olasId)
                    .getSingleResult();
        }

        catch (NoResultException e) {
            String bankId = olasId;
            while (true) {
                try {
                    getBankUser(bankId);
                    bankId += "_";
                } catch (NoResultException ee) {
                    break;
                }
            }

            BankUserEntity userEntity = new BankUserEntity(bankId, olasId, olasId);
            this.em.persist(userEntity);

            return userEntity;
        }
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity linkOLASUser(BankUserEntity user, String olasId) {

        BankUserEntity userEntity = attach(user);
        userEntity.setOlasId(olasId);

        return userEntity;
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity updateUser(BankUserEntity user, HttpServletRequest loginRequest) {

        try {
            AttributeClient attributeClient = WicketUtil.getOLASAttributeService(loginRequest, DemoBankKeyStoreUtils
                    .getPrivateKeyEntry());

            BankUserEntity userEntity = attach(user);
            if (userEntity.getOlasId() == null)
                return userEntity;

            // OLAS username of the user.
            String name = attributeClient.getAttributeValue(userEntity.getOlasId(),
                    DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
            userEntity.setName(name);

            return userEntity;
        }

        catch (AttributeNotFoundException e) {
            this.LOG.error("attribute not found: ", e);
            throw new RuntimeException(e);
        } catch (RequestDeniedException e) {
            this.LOG.error("request denied: ", e);
            throw new RuntimeException(e);
        } catch (WSClientTransportException e) {
            this.LOG.error("Connection error. Check your SSL setup.", e);
            throw new RuntimeException(e);
        } catch (AttributeUnavailableException e) {
            this.LOG.error("Attribute unavailable", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankAccountEntity> getAccounts(BankUserEntity user) {

        return this.em.createNamedQuery(BankAccountEntity.getByUser).setParameter("user", attach(user)).getResultList();
    }
}
