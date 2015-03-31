/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import net.link.util.logging.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;


/**
 * <h2>{@link LinkIDProtocolContext}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>08 17, 2010</i> </p>
 *
 * @author lhunath
 */
public class LinkIDProtocolContext implements Serializable {

    private static final Logger logger = Logger.get( LinkIDProtocolContext.class );

    public static final String SESSION_CONTEXTS = LinkIDProtocolContext.class.getName() + ".contexts";

    private final String id;

    /**
     * @param id A unique ID that will match the response to this request.
     */
    public LinkIDProtocolContext(String id) {

        this.id = id;
    }

    /**
     * @return A unique ID that will match the response to this request.
     */
    public String getId() {

        return id;
        //        return checkNotNull( id, "Id not set for %s", this );
    }

    public static void addContext(HttpSession session, LinkIDProtocolContext linkIDProtocolContext) {

        getContexts( session ).put( linkIDProtocolContext.getId(), linkIDProtocolContext );
        logger.dbg( "Added protocol context to session: %s", linkIDProtocolContext );
    }

    @SuppressWarnings("unchecked")
    public static Map<String, LinkIDProtocolContext> getContexts(HttpSession session) {

        Map<String, LinkIDProtocolContext> contexts = (Map<String, LinkIDProtocolContext>) session.getAttribute( SESSION_CONTEXTS );
        if (contexts == null)
            session.setAttribute( SESSION_CONTEXTS, contexts = new HashMap<String, LinkIDProtocolContext>() );

        return contexts;
    }

    @SuppressWarnings("unchecked")
    public static <P extends LinkIDProtocolContext> P findContext(HttpSession session, String id) {

        return (P) getContexts( session ).get( id );
    }

    @Override
    public String toString() {

        return String.format( "{%s: %s}", getClass().getSimpleName(), id );
    }
}
