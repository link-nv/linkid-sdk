package net.link.safeonline.sdk.ws.wssecurity;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.link.util.ws.pkix.wssecurity.AbstractWSSecurityBodyHandler;


/**
 * <h2>{@link WSSecurityBodyHandler}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>10 19, 2010</i> </p>
 *
 * @author lhunath
 */
public class WSSecurityBodyHandler extends AbstractWSSecurityBodyHandler {

    private final boolean inboudSignatureOptional;

    public WSSecurityBodyHandler() {

        try {
            Context ctx = new InitialContext();
            Context env = (Context) ctx.lookup( "java:comp/env" );
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
}
