package net.link.safeonline.sdk.example.mobile;

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
