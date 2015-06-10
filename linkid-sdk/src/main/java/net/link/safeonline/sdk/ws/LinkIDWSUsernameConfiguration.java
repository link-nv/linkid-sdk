package net.link.safeonline.sdk.ws;

import java.security.cert.X509Certificate;


/**
 * Created by wvdhaute
 * Date: 22/07/14
 * Time: 14:26
 */
public interface LinkIDWSUsernameConfiguration {

    /**
     * @return the application name as known to linkID
     */
    String getApplicationName();

    String getUsername();

    String getPassword();

    /**
     * @return If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    X509Certificate[] getSSLCertificates();

    /**
     * @return the base URL to linkID. ( e.g. https://demo.linkid.be )
     */
    String getLinkIDBase();
}
