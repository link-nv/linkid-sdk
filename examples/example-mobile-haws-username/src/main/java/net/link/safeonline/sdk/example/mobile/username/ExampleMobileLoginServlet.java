/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile.username;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.payment.LinkIDCurrency;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet;
import net.link.safeonline.sdk.configuration.AuthenticationContext;


public class ExampleMobileLoginServlet extends InitiateLoginServlet {

    @Override
    protected void configureAuthenticationContext(final AuthenticationContext authenticationContext, final HttpServletRequest request,
                                                  final HttpServletResponse response) {

        authenticationContext.setAuthenticationMessage( "Custom authentication message" );
        authenticationContext.setFinishedMessage( "Custom finished message" );

        authenticationContext.setIdentityProfiles( Collections.singletonList( "linkid_payment" ) );
        authenticationContext.setPaymentContext( new PaymentContextDO( 200, LinkIDCurrency.EUR ) );

        //        authenticationContext.setCallback( new CallbackDO( "https://yourdomain.be/callback", null, true ) );
    }
}
