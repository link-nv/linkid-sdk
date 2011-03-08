package net.link.safeonline.sdk.ws.wssecurity;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.link.util.j2ee.EJBUtils;
import net.link.util.ws.pkix.wssecurity.AbstractWSSecurityServerHandler;
import net.link.util.ws.pkix.wssecurity.WSSecurityConfigurationService;


/**
 * <h2>{@link WSSecurityServerHandler}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 19, 2010</i> </p>
 *
 * @author lhunath
 */

public class WSSecurityServerHandler extends AbstractWSSecurityServerHandler {

    private final String  configurationServiceJndiName;
    private final boolean inboudSignatureOptional;

    public WSSecurityServerHandler() {

        try {
            Context ctx = new InitialContext();
            Context env = (Context) ctx.lookup( "java:comp/env" );
            configurationServiceJndiName = (String) env.lookup( "wsSecurityConfigurationServiceJndiName" );
            inboudSignatureOptional = (Boolean) env.lookup( "wsSecurityOptionalInboudSignature" );
        }
        catch (NamingException e) {
            throw new RuntimeException( "'wsSecurityConfigurationServiceJndiName' or 'wsSecurityOptionalInboudSignature' not specified",
                                        e );
        }
    }

    @Override
    protected boolean isInboundSignatureOptional() {

        return inboudSignatureOptional;
    }

    @Override
    protected WSSecurityConfigurationService getConfiguration() {

        return EJBUtils.getEJB( configurationServiceJndiName, WSSecurityConfigurationService.class );
    }
}
