/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.util;

import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.idmapping.client.NameIdentifierMappingClient;


/**
 * <h2>{@link DummyNameIdentifierMappingClient}<br>
 * <sub>An linkID id mapping service client used inside unit tests.</sub></h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 *
 * @author wvdhaute
 */
public class DummyNameIdentifierMappingClient implements NameIdentifierMappingClient {

    private static String userId;

    public static void setUserId(String userId) {

        DummyNameIdentifierMappingClient.userId = userId;
    }

    public static String getUserId() {

        return userId;
    }

    @Override
    public String getUserId(String attributeTypeName, String identifier)
            throws SubjectNotFoundException, RequestDeniedException, WSClientTransportException {

        return userId;
    }
}
