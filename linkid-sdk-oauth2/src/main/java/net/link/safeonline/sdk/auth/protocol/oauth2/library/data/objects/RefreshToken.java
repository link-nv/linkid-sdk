/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects;

import java.util.Date;


/**
 * <p/>
 * Date: 21/03/12
 * Time: 11:29
 *
 * @author sgdesmet
 */
public class RefreshToken extends Token {

    public RefreshToken(final String tokenData, final Date expirationDate, final boolean invalid) {

        super( tokenData, expirationDate, invalid );
    }

    public RefreshToken(final String tokenData, final Date expirationDate) {

        super( tokenData, expirationDate );
    }
}
