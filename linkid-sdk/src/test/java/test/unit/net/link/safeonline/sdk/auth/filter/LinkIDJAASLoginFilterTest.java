/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.auth.filter;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.spi.LoginModule;
import javax.servlet.FilterChain;
import javax.servlet.http.*;
import net.link.safeonline.sdk.auth.filter.LinkIDJAASLoginFilter;
import net.link.safeonline.sdk.auth.filter.LinkIDLoginManager;
import net.link.safeonline.sdk.configuration.LinkIDTestConfigHolder;
import net.link.util.test.session.JaasTestUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit test for the generic JAAS login module. This unit test also demonstrates the JAAS login/logout workflow.
 *
 * @author fcorneli
 */
public class LinkIDJAASLoginFilterTest {

    private LinkIDJAASLoginFilter testedInstance;

    private HttpServletRequest mockHttpServletRequest;

    private HttpServletResponse mockHttpServletResponse;

    private FilterChain mockFilterChain;

    private HttpSession mockHttpSession;

    @Before
    public void setUp()
            throws Exception {

        testedInstance = new LinkIDJAASLoginFilter();

        mockHttpServletRequest = createMock( HttpServletRequest.class );
        mockHttpServletResponse = createMock( HttpServletResponse.class );
        mockFilterChain = createMock( FilterChain.class );
        mockHttpSession = createMock( HttpSession.class );
        expect( mockHttpServletRequest.getSession() ).andStubReturn( mockHttpSession );

        new LinkIDTestConfigHolder( null, null ).install();
        JaasTestUtils.initJaasLoginModule( TestLoginModule.class );
    }

    public static class TestLoginModule implements LoginModule {

        public TestLoginModule() {

        }

        public boolean abort() {

            return true;
        }

        public boolean commit() {

            return true;
        }

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

            NameCallback nameCallback = new NameCallback( "name" );

            Callback[] callbacks = new Callback[] { nameCallback };
            try {
                callbackHandler.handle( callbacks );
            }
            catch (IOException e) {
                throw new RuntimeException( "IO error: " + e.getMessage(), e );
            }
            catch (UnsupportedCallbackException e) {
                throw new RuntimeException( "callback error: " + e.getMessage(), e );
            }

            String name = nameCallback.getName();
            assertNotNull( name );
        }

        public boolean login() {

            return true;
        }

        public boolean logout() {

            return true;
        }
    }

    @Test
    public void doFilter()
            throws Exception {

        // Setup Data
        String testPasswordAttributeName = "test-password";

        // Setup Mocks
        expect( mockHttpSession.getAttribute( LinkIDLoginManager.USERID_SESSION_ATTRIBUTE ) ).andStubReturn( UUID.randomUUID().toString() );
        expect( mockHttpSession.getAttribute( testPasswordAttributeName ) ).andStubReturn( "test-password" );

        expect( mockHttpServletRequest.getRequestURL() ).andStubReturn( new StringBuffer( "test-url" ) );

        expect( mockHttpServletRequest.getSession( false ) ).andStubReturn( mockHttpSession );

        expect( mockHttpSession.getAttribute( "FlushJBossCredentialCache" ) ).andStubReturn( null );

        // expectation
        mockHttpServletRequest.setAttribute( EasyMock.eq( LinkIDJAASLoginFilter.JAAS_LOGIN_CONTEXT_SESSION_ATTRIB ), EasyMock.anyObject() );
        LoginContext mockLoginContext = createMock( LoginContext.class );
        expect( mockHttpServletRequest.getAttribute( LinkIDJAASLoginFilter.JAAS_LOGIN_CONTEXT_SESSION_ATTRIB ) ).andStubReturn( mockLoginContext );

        mockLoginContext.logout();

        mockFilterChain.doFilter( mockHttpServletRequest, mockHttpServletResponse );

        replay( mockHttpServletRequest, mockHttpServletResponse, mockFilterChain, mockHttpSession );
        replay( mockLoginContext );

        // Test
        testedInstance.doFilter( mockHttpServletRequest, mockHttpServletResponse, mockFilterChain );
        testedInstance.destroy();

        // Verify
        verify( mockHttpServletRequest, mockHttpServletResponse, mockFilterChain, mockHttpSession );
        verify( mockLoginContext );
    }
}
