/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;


/**
 * <h2>{@link UserService}<br>
 * <sub>Service bean for {@link PaymentUserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link PaymentUserEntity}s for logged in users.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface UserService extends PaymentService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "UserServiceBean/local";


    /**
     * @return All known {@link PaymentUserEntity}s.
     */
    public List<PaymentUserEntity> getUsers();

    /**
     * NOTE: If no {@link PaymentUserEntity} with the given OLAS ID exists yet, one will be created and returned.
     * 
     * @return The {@link PaymentUserEntity} with the given OLAS ID.
     */
    public PaymentUserEntity getUser(String olasId);

    /**
     * Update the given user's attributes from OLAS.
     */
    public PaymentUserEntity updateUser(PaymentUserEntity user);
}
