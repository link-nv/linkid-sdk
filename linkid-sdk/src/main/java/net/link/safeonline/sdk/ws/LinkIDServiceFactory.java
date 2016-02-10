/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.security.cert.X509Certificate;
import net.link.safeonline.sdk.api.LinkIDConstants;
import net.link.safeonline.sdk.api.configuration.LinkIDConfigService;
import net.link.safeonline.sdk.api.ws.attrib.LinkIDAttributeClient;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.api.ws.idmapping.LinkIDNameIdentifierMappingClient;
import net.link.safeonline.sdk.api.ws.linkid.LinkIDServiceClient;
import net.link.safeonline.sdk.api.ws.sts.LinkIDSecurityTokenServiceClient;
import net.link.safeonline.sdk.api.ws.xkms2.LinkIDXkms2Client;
import net.link.safeonline.sdk.ws.attrib.LinkIDAttributeClientImpl;
import net.link.safeonline.sdk.ws.data.LinkIDDataClientImpl;
import net.link.safeonline.sdk.ws.idmapping.LinkIDNameIdentifierMappingClientImpl;
import net.link.safeonline.sdk.ws.linkid.LinkIDServiceClientImpl;
import net.link.safeonline.sdk.ws.sts.LinkIDSecurityTokenServiceClientImpl;
import net.link.safeonline.sdk.ws.xkms2.LinkIDXkms2ClientImpl;
import net.link.util.keyprovider.KeyProvider;
import net.link.util.ws.security.username.AbstractWSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDServiceFactory}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Jan 15, 2009</i> </p>
 *
 * @author lhunath
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDServiceFactory {

    // LinkID WS

    public static LinkIDServiceClient getLinkIDService(final LinkIDConfigService linkIDConfigService) {

        return new LinkIDServiceClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( linkIDConfigService.username(), linkIDConfigService.password() ) );
    }

    public static LinkIDServiceClient getLinkIDService(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDServiceClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ),
                new LinkIDSDKWSSecurityConfiguration( linkIDConfigService, keyProvider ) );
    }

    public static LinkIDServiceClient getLinkIDService(final LinkIDConfigService linkIDConfigService, final String username, final String password) {

        return new LinkIDServiceClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( username, password ) );

    }

    // Attribute WS

    public static LinkIDAttributeClient getAttributeService(final LinkIDConfigService linkIDConfigService) {

        return new LinkIDAttributeClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( linkIDConfigService.username(), linkIDConfigService.password() ) );
    }

    public static LinkIDAttributeClient getAttributeService(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDAttributeClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ),
                new LinkIDSDKWSSecurityConfiguration( linkIDConfigService, keyProvider ) );
    }

    public static LinkIDAttributeClient getAttributeService(final LinkIDConfigService linkIDConfigService, final String username, final String password) {

        return new LinkIDAttributeClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( username, password ) );

    }

    // Data WS

    public static LinkIDDataClient getDataService(final LinkIDConfigService linkIDConfigService) {

        return new LinkIDDataClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( linkIDConfigService.username(), linkIDConfigService.password() ) );
    }

    public static LinkIDDataClient getDataService(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDDataClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ),
                new LinkIDSDKWSSecurityConfiguration( linkIDConfigService, keyProvider ) );
    }

    public static LinkIDDataClient getDataService(final LinkIDConfigService linkIDConfigService, final String username, final String password) {

        return new LinkIDDataClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( username, password ) );

    }

    // ID Mapping WS

    public static LinkIDNameIdentifierMappingClient getIdMappingService(final LinkIDConfigService linkIDConfigService) {

        return new LinkIDNameIdentifierMappingClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( linkIDConfigService.username(), linkIDConfigService.password() ) );
    }

    public static LinkIDNameIdentifierMappingClient getIdMappingService(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDNameIdentifierMappingClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ),
                new LinkIDSDKWSSecurityConfiguration( linkIDConfigService, keyProvider ) );
    }

    public static LinkIDNameIdentifierMappingClient getIdMappingService(final LinkIDConfigService linkIDConfigService, final String username,
                                                                        final String password) {

        return new LinkIDNameIdentifierMappingClientImpl( getWsUsernameBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ),
                getUsernameTokenCallback( username, password ) );

    }

    // STS WS

    public static LinkIDSecurityTokenServiceClient getStsService(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDSecurityTokenServiceClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ),
                new LinkIDSDKWSSecurityConfiguration( linkIDConfigService, keyProvider ) );
    }

    // XKMS2 WS

    public static LinkIDXkms2Client getXkms2Client(final LinkIDConfigService linkIDConfigService) {

        return new LinkIDXkms2ClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, null ) );
    }

    public static LinkIDXkms2Client getXkms2Client(final LinkIDConfigService linkIDConfigService, final KeyProvider keyProvider) {

        return new LinkIDXkms2ClientImpl( getWsBase( linkIDConfigService ), getSSLCertificates( linkIDConfigService, keyProvider ) );
    }

    // Helper methods

    private static String getWsBase(final LinkIDConfigService linkIDConfigService) {

        return String.format( "%s/%s", linkIDConfigService.linkIDBase(), LinkIDConstants.LINKID_PATH_WS_BASE );
    }

    private static String getWsUsernameBase(final LinkIDConfigService linkIDConfigService) {

        return String.format( "%s/%s", linkIDConfigService.linkIDBase(), LinkIDConstants.LINKID_PATH_WS_USERNAME_BASE );
    }

    private static WSSecurityUsernameTokenCallback getUsernameTokenCallback(final String username, final String password) {

        return new AbstractWSSecurityUsernameTokenCallback() {
            @Override
            public String getUsername() {

                return username;
            }

            @Override
            public String getPassword() {

                return password;
            }

            @Nullable
            @Override
            public String handle(final String username) {

                return null;
            }

            @Override
            public boolean isInboundHeaderOptional() {

                return true;
            }

        };
    }

    @Nullable
    private static X509Certificate[] getSSLCertificates(final LinkIDConfigService linkIDConfigService, @Nullable final KeyProvider keyProvider) {

        X509Certificate sslCertificate = keyProvider != null? keyProvider.getTrustedCertificate( LinkIDConstants.SSL_ALIAS ): null;
        if (null != keyProvider && null != sslCertificate) {
            return new X509Certificate[] { sslCertificate };
        } else {
            return linkIDConfigService.sslCertificates();
        }
    }

}
