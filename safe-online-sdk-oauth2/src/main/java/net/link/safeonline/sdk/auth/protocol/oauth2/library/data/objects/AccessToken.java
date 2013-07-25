package net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects;

import java.util.Date;
import org.jetbrains.annotations.Nullable;


/**
 * <p/>
 * Date: 21/03/12
 * Time: 11:29
 *
 * @author sgdesmet
 */
public class AccessToken extends Token {

    public static final String TYPE = "Bearer";

    public AccessToken(final String tokenData, final Date expirationDate, final boolean invalid) {

        super( tokenData, expirationDate, invalid );
    }

    public AccessToken(final String tokenData, @Nullable final Date expirationDate) {

        super( tokenData, expirationDate );
    }

    public AccessToken(final String tokenData) {

        this( tokenData, null );
    }

    public String getAccessTokenType() {

        return TYPE;
    }
}
