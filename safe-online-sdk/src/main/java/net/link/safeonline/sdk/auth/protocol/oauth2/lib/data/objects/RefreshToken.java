package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects;

import java.util.Date;


/**
 * TODO description
 * <p/>
 * Date: 21/03/12
 * Time: 11:29
 *
 * @author: sgdesmet
 */
public class RefreshToken extends Token {

    public RefreshToken() {

    }

    public RefreshToken(final String tokenData, final Date expirationDate, final boolean invalid) {

        super( tokenData, expirationDate, invalid );
    }

    public RefreshToken(final String tokenData, final Date expirationDate) {

        super( tokenData, expirationDate );
    }
}
