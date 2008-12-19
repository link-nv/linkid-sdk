/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.sdk.ws.auth.AuthenticationClient;
import net.link.safeonline.sdk.ws.auth.AuthenticationClientImpl;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClient;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClientImpl;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;


/**
 * Integration tests for SafeOnline authentication web service.
 * 
 * @author wvdhaute
 * 
 */
public class AuthenticationWebServiceTest {

    private static final Log        LOG = LogFactory.getLog(DataWebServiceTest.class);

    private GetAuthenticationClient getAuthenticationClient;

    private AuthenticationClient    authenticationClient;


    @Before
    public void setUp()
            throws Exception {

        this.getAuthenticationClient = new GetAuthenticationClientImpl("https://localhost:8443");

    }

    @Test
    public void authenticate()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();

        // operate: get instance of stateful authentication web service
        W3CEndpointReference endpoint = this.getAuthenticationClient.getInstance();
        this.authenticationClient = new AuthenticationClientImpl("https://localhost:8443", endpoint);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put("Login", "admin");
        nameValuePairs.put("Password", "admin");

        // operate: authenticate admin user to olas-user application via web service
        this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, PasswordConstants.PASSWORD_DEVICE_ID,
                nameValuePairs, keyPair.getPublic());

        // operate: authenticate admin user to olas-user application via web service
        this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME, PasswordConstants.PASSWORD_DEVICE_ID,
                null, keyPair.getPublic());

        // operate: authenticate admin user to olas-user application via web service, should fail as stateful ws is released on second ws
        // call
        try {
            this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                    PasswordConstants.PASSWORD_DEVICE_ID, null, keyPair.getPublic());
        } catch (Exception e) {
            // success
            return;
        }
        fail();

    }
}
