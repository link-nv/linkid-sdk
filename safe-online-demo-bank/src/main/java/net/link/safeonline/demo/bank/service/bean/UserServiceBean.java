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

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.UserEntity;
import net.link.safeonline.demo.bank.service.UserService;
import net.link.safeonline.demo.cinema.keystore.DemoCinemaKeyStoreUtils;
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
    public UserEntity getBankUser(String bankId) {

        return (UserEntity) this.em.createNamedQuery(UserEntity.getById).setParameter("bankId", bankId)
                .getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    public UserEntity getOLASUser(String olasId) {

        try {
            return (UserEntity) this.em.createNamedQuery(UserEntity.getById).setParameter("olasId", olasId)
                    .getSingleResult();
        } catch (NoResultException e) {

            String bankId = olasId;
            while (getBankUser(bankId) != null) {
                bankId += "_";
            }
            
            UserEntity userEntity = new UserEntity(bankId, olasId);
            this.em.persist(userEntity);

            return userEntity;
        }
    }

    /**
     * {@inheritDoc}
     */
    public UserEntity updateUser(UserEntity user, HttpServletRequest loginRequest) {

        try {
            AttributeClientImpl attributeClient = getOLASAttributeService(loginRequest);
            UserEntity userEntity = attach(user);

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

        // Find the key and certificate of the cinema application.
        PrivateKeyEntry privateKeyEntry = DemoCinemaKeyStoreUtils.getPrivateKeyEntry();
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        // Create the attribute service client.
        return new AttributeClientImpl(wsLocation, certificate, privateKey);
    }

    /**
     * {@inheritDoc}
     */
    public UserEntity attach(UserEntity user) {

        if (user == null)
            return null;

        return (UserEntity) this.em.createNamedQuery(UserEntity.getById).setParameter("id", user.getBankId())
                .getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<AccountEntity> getAccounts(UserEntity user) {

        return this.em.createNamedQuery(AccountEntity.getByUser).setParameter("user", attach(user)).getResultList();
    }
}
