package net.link.safeonline.sdk.configuration;

import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * Sets properties for OAuth2 protocol
 * <p/>
 * Date: 10/05/12
 * Time: 16:09
 *
 * @author: sgdesmet
 */

@Group(prefix = "oauth2")
public interface OAuth2ProtocolConfig {

    /**
     * Path for the authorization endpoint
     * @return
     */
    @Property(required = true, unset = "/entry")
    String authorizationPath();

    /**
     * Token endpoint path
     * @return
     */
    @Property(required = true, unset = "/token")
    String tokenPath();


    @Property(required = true, unset = "/token_validate")
    String validationPath();

    @Property(required = true, unset = "/attributes")
    String attributesPath();

    @Property(required = true, unset = "POST")
    String binding();

    @Property( required = false)
    String clientSecret();


}

