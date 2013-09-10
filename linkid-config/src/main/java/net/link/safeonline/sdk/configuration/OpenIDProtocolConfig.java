package net.link.safeonline.sdk.configuration;

import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link OpenIDProtocolConfig}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "openid")
public interface OpenIDProtocolConfig {

    /**
     * @return The URL that the OpenID provider will ask the user to trust for the purpose of this authentication. The authentication is
     *         only valid when returned to a URL under this one, so it should probably be your application's base URL.
     *         <p/>
     *         <i>[optional, default: {@link ConfigUtils#getApplicationConfidentialURL()}]</i>
     *         <p/>
     *         <p> Must be an absolute URL (eg. <code>http://my.host.net/myapp</code>) </p>
     */
    @Property(required = false)
    String realm();

    /**
     * @return The path within the linkID authentication application where OpenID discovery can be performed.
     *         <p/>
     *         <i>[required, default: /openid]</i>
     */
    @Property(required = true, unset = "/openid")
    String discoveryPath();
}
