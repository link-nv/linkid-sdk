/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.cinema.entity.UserEntity;
import net.link.safeonline.demo.cinema.keystore.DemoCinemaKeyStoreUtils;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
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
public class UserServiceBean extends AbstractCinemaServiceBean implements UserService {

    private static final String WS_LOCATION = "WsLocation";


    /**
     * {@inheritDoc}
     */
    public UserEntity getUser(String id) {

        UserEntity user;
        try {
            user = (UserEntity) this.em.createNamedQuery(UserEntity.getById).setParameter("id", id).getSingleResult();
        }

        catch (NoResultException e) {
            user = new UserEntity(id);
            this.em.persist(user);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    public void updateUser(UserEntity user, HttpServletRequest loginRequest) {

        try {
            AttributeClientImpl attributeClient = getOLASAttributeService(loginRequest);
            UserEntity userEntity = attach(user);

            // National registry number of user.
            String nrn = attributeClient.getAttributeValue(userEntity.getId(), BeIdConstants.NRN_ATTRIBUTE,
                    String[].class)[0];
            userEntity.setNrn(nrn);

            // National registry number of user.
            String name = attributeClient.getAttributeValue(userEntity.getId(),
                    DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
            userEntity.setName(name);

            // Does user have a junior account?
            Boolean juniorValue = attributeClient.getAttributeValue(userEntity.getId(),
                    DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, Boolean.class);
            userEntity.setJunior(juniorValue != null && juniorValue.booleanValue() == true);
        }

        catch (AttributeNotFoundException e) {
            LOG.error("attribute not found: ", e);
        } catch (RequestDeniedException e) {
            LOG.error("request denied: ", e);
        } catch (WSClientTransportException e) {
            LOG.error("Connection error. Check your SSL setup.", e);
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

        return (UserEntity) this.em.createNamedQuery(UserEntity.getById).setParameter("id", user.getId())
                .getSingleResult();
    }
}
