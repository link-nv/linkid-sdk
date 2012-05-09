package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.generators;

import java.util.Date;
import java.util.UUID;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.TokenGenerator;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.*;


/**
 * Generate oauth tokens. In this case, simple bearer tokens using random UUIDs.
 * <p/>
 * Date: 23/03/12
 * Time: 13:45
 *
 * @author: sgdesmet
 */
public class SimpleUUIDCodeGenerator implements TokenGenerator {

    protected String generateCode() {

        return UUID.randomUUID().toString();
    }

    @Override
    public CodeToken createCode(final ClientAccess access) {

        return new CodeToken( generateCode(),
                new Date( System.currentTimeMillis() + access.getClient().getDefaultCodeLifeTime() * 1000 ) );
    }

    @Override
    public AccessToken createAccessToken(final ClientAccess access) {

        return new AccessToken( generateCode(),
                new Date( System.currentTimeMillis() + access.getClient().getDefaultAccessTokenLifeTime() * 1000 ) );
    }

    @Override
    public RefreshToken createRefreshToken(final ClientAccess access) {

        return new RefreshToken( generateCode(),
                new Date( System.currentTimeMillis() + access.getClient().getDefaultRefreshTokenLifeTime() * 1000 ) );
    }
}
