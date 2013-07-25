package net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects;

import java.util.Date;


/**
 * <p/>
 * Date: 03/05/12
 * Time: 15:46
 *
 * @author sgdesmet
 */
public class CodeToken extends Token {

    public CodeToken(final String tokenData, final Date expirationDate, final boolean invalid) {

        super( tokenData, expirationDate, invalid );
    }

    public CodeToken(final String tokenData, final Date expirationDate) {

        super( tokenData, expirationDate );
    }
}
