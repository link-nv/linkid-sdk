/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service.bean;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.keystore.DemoCinemaKeyStoreUtils;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.model.beid.BeIdConstants;
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
public class UserServiceBean extends AbstractCinemaServiceBean implements UserService {

    /**
     * {@inheritDoc}
     */
    public CinemaUserEntity getUser(String olasId) {

        CinemaUserEntity user;
        try {
            user = (CinemaUserEntity) em.createNamedQuery(CinemaUserEntity.getByOlasId).setParameter("olasId", olasId)
                                             .getSingleResult();
        }

        catch (NoResultException e) {
            user = new CinemaUserEntity(olasId);
            em.persist(user);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    public CinemaUserEntity updateUser(CinemaUserEntity user, HttpServletRequest loginRequest) {

        try {
            AttributeClient attributeClient = WicketUtil
                                                        .getOLASAttributeService(loginRequest, DemoCinemaKeyStoreUtils.getPrivateKeyEntry());
            CinemaUserEntity userEntity = attach(user);

            // National registry number of user.
            String nrns[] = attributeClient.getAttributeValue(userEntity.getOlasId(), BeIdConstants.NRN_ATTRIBUTE, String[].class);
            if (nrns != null && nrns.length > 0) {
                userEntity.setNrn(nrns[0]);
            }

            // OLAS username of the user.
            String name = attributeClient.getAttributeValue(userEntity.getOlasId(), DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
            userEntity.setName(name);

            // Does user have a junior account?
            Boolean juniorValue = attributeClient.getAttributeValue(userEntity.getOlasId(), DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME,
                    Boolean.class);
            userEntity.setJunior(juniorValue != null && juniorValue.booleanValue() == true);

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
}
