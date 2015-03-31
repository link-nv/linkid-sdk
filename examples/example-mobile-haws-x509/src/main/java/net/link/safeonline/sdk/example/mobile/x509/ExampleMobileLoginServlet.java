/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile.x509;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.servlet.LinkIDInitiateLoginServlet;
import net.link.safeonline.sdk.configuration.LinkIDAuthenticationContext;


public class ExampleMobileLoginServlet extends LinkIDInitiateLoginServlet {

    @Override
    protected void configureAuthenticationContext(final LinkIDAuthenticationContext linkIDAuthenticationContext, final HttpServletRequest request,
                                                  final HttpServletResponse response) {

        linkIDAuthenticationContext.setAuthenticationMessage( "Custom authentication message" );
        linkIDAuthenticationContext.setFinishedMessage( "Custom finished message" );

        //        authenticationContext.setPaymentContext( new PaymentContextDO( 200, Currency.EUR ) );
    }
}
