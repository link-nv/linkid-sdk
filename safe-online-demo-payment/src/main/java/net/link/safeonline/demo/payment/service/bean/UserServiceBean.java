/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.service.bean;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.demo.payment.service.UserService;
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
public class UserServiceBean extends AbstractPaymentServiceBean implements UserService {

    /**
     * {@inheritDoc}
     */
    public PaymentUserEntity getUser(String olasId) {

        PaymentUserEntity user = em.find(PaymentUserEntity.class, olasId);
        if (user == null) {
            user = new PaymentUserEntity(olasId, olasId);
            em.persist(user);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     */
    public PaymentUserEntity updateUser(PaymentUserEntity user, HttpServletRequest loginRequest) {

        try {
            AttributeClient attributeClient = WicketUtil.getOLASAttributeService(loginRequest,
                    DemoPaymentKeyStoreUtils.getPrivateKeyEntry());

            PaymentUserEntity userEntity = attach(user);

            // OLAS username of the user.
            String name = attributeClient.getAttributeValue(userEntity.getOlasId(), DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, String.class);
            userEntity.setOlasName(name);

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
