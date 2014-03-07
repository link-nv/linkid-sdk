/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile.x509;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.device.DeviceContextConstants;
import net.link.safeonline.sdk.auth.servlet.InitiateLoginServlet;
import net.link.safeonline.sdk.configuration.AuthenticationContext;


public class ExampleMobileLoginServlet extends InitiateLoginServlet {

    @Override
    protected void configureAuthenticationContext(final AuthenticationContext authenticationContext, final HttpServletRequest request,
                                                  final HttpServletResponse response) {

        authenticationContext.getDeviceContext().put( DeviceContextConstants.CONTEXT_TITLE, "Optional custom mobile login context" );

        //        authenticationContext.setPaymentContext( new PaymentContextDO( 200, Currency.EUR ) );
    }
}
