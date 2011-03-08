package net.link.safeonline.sdk.auth.protocol;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link ProtocolContext}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>08 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class ProtocolContext implements Serializable {

    private static final Log LOG = LogFactory.getLog( ProtocolContext.class );

    public static final String SESSION_CONTEXTS = ProtocolContext.class.getName() + ".contexts";

    private final String id;

    /**
     * @param id A unique ID that will match the response to this request.
     */
    public ProtocolContext(String id) {

        this.id = id;
    }

    /**
     * @return A unique ID that will match the response to this request.
     */
    public String getId() {

        return checkNotNull( id, "Id not set for %s", this );
    }

    public static void addContext(HttpSession session, ProtocolContext protocolContext) {

        getContexts( session ).put( protocolContext.getId(), protocolContext );
        LOG.debug( "Added protocol context to session: " + protocolContext );
    }

    @SuppressWarnings( { "unchecked" })
    public static Map<String, ProtocolContext> getContexts(HttpSession session) {

        Map<String, ProtocolContext> contexts = (Map<String, ProtocolContext>) session.getAttribute( SESSION_CONTEXTS );
        if (contexts == null)
            session.setAttribute( SESSION_CONTEXTS, contexts = new HashMap<String, ProtocolContext>() );

        return contexts;
    }

    @SuppressWarnings( { "unchecked" })
    public static <P extends ProtocolContext> P findContext(HttpSession session, String id) {

        return (P) getContexts( session ).get( id );
    }

    @Override
    public String toString() {

        return String.format( "{%s: %s}", getClass().getSimpleName(), id );
    }
}
