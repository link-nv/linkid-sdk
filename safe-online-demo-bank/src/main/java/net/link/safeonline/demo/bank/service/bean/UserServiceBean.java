/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
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
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
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

    private static final String WS_LOCATION = "WsLocation";


    /**
     * {@inheritDoc}
     */
    public BankUserEntity getBankUser(String bankId) {

        return (BankUserEntity) this.em.createNamedQuery(BankUserEntity.getByBankId).setParameter("bankId", bankId)
                .getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    public BankUserEntity getOLASUser(String olasId) {

        try {
            return (BankUserEntity) this.em.createNamedQuery(BankUserEntity.getByOlasId).setParameter("olasId", olasId)
                    .getSingleResult();
        } catch (NoResultException e) {

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
            AttributeClientImpl attributeClient = getOLASAttributeService(loginRequest);
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
     * Retrieve a proxy to the OLAS attribute web service.
     */
    private AttributeClientImpl getOLASAttributeService(HttpServletRequest loginRequest) {

        // Find the location of the OLAS web services to use.
        String wsLocation = loginRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

        // Find the key and certificate of the bank application.
        PrivateKeyEntry privateKeyEntry = DemoBankKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankAccountEntity> getAccounts(BankUserEntity user) {

        return this.em.createNamedQuery(BankAccountEntity.getByUser).setParameter("user", attach(user)).getResultList();
    }
}
