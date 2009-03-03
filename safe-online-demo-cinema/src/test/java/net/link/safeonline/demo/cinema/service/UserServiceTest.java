/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import static org.junit.Assert.assertEquals;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.test.DummyAttributeClient;
import net.link.safeonline.sdk.test.DummyServiceFactory;

import org.apache.ws.security.util.UUIDGenerator;
import org.junit.Test;


/**
 * <h2>{@link UserServiceTest}<br>
 * <sub>Unit tests for {@link SeatService}.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class UserServiceTest extends AbstractCinemaServiceTest {

    @EJB
    private InitializationService initializationService;

    @EJB
    private UserService           userService;


    /**
     * {@inheritDoc}
     */
    @Override
    public void setup()
            throws Exception {

        super.setup();

        DummyServiceFactory.install();

        initializationService.buildEntities();
    }

    @Test
    public void testCreateUser()
            throws Exception {

        // Test data.
        String testUserOlasId = UUIDGenerator.getUUID(), testUserName = "testCinemaUser", testUserNrn = "0123456789";

        // Create our user.
        CinemaUserEntity sampleUser = userService.getUser(testUserOlasId);

        // Verify && OLAS Id assigned correctly.
        String sampleUserOlasId = sampleUser.getOlasId();
        assertEquals(String.format("user id mismatch: test: %s - sample: %s", testUserOlasId, sampleUserOlasId), //
                testUserOlasId, sampleUserOlasId);

        // Set up the dummy OLAS attribute service and update our cinema user with the attributes.
        DummyAttributeClient.setAttribute(testUserOlasId, BeIdConstants.BEID_NRN_ATTRIBUTE, new String[] { testUserNrn });
        DummyAttributeClient.setAttribute(testUserOlasId, DemoConstants.DEMO_LOGIN_ATTRIBUTE_NAME, testUserName);
        userService.updateUser(sampleUser, null);

        // Verify && attributes assigned correctly.
        String sampleUserName = sampleUser.getName();
        String sampleUserNrn = sampleUser.getNrn();
        assertEquals(String.format("user name mismatch: test: %s - sample: %s", testUserName, sampleUserName), //
                testUserName, sampleUserName);
        assertEquals(String.format("user nrn mismatch: test: %s - sample: %s", testUserNrn, sampleUserNrn), //
                testUserNrn, sampleUserNrn);

        // Verify whether ticketService.getUser() returns the correct user.
        CinemaUserEntity testUser = sampleUser;
        sampleUser = userService.getUser(sampleUserOlasId);
        assertEquals(String.format("user mismatch: test: %s - sample: %s", testUser, sampleUser), //
                testUser, sampleUser);
    }
}
