/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import net.lin_k.safe_online.configuration.ConfigurationServicePort;
import net.lin_k.safe_online.configuration.LocalizedImage;
import net.lin_k.safe_online.configuration.LocalizedImages;
import net.lin_k.safe_online.configuration.Themes;
import net.lin_k.safe_online.configuration.ThemesRequest;
import net.lin_k.safe_online.configuration.ThemesResponse;
import net.link.safeonline.sdk.api.ws.configuration.ConfigurationServiceClient;
import net.link.safeonline.sdk.api.ws.configuration.LocalizedImageDO;
import net.link.safeonline.sdk.api.ws.configuration.LocalizedImagesDO;
import net.link.safeonline.sdk.api.ws.configuration.ThemeDO;
import net.link.safeonline.sdk.api.ws.configuration.ThemesDO;
import net.link.safeonline.sdk.api.ws.configuration.ThemesErrorCode;
import net.link.safeonline.sdk.api.ws.configuration.ThemesException;
import net.link.safeonline.sdk.ws.SDKUtils;
import net.link.safeonline.ws.configuration.ConfigurationServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class ConfigurationServiceClientImpl extends AbstractWSClient<ConfigurationServicePort> implements ConfigurationServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public ConfigurationServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public ConfigurationServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                          final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private ConfigurationServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( ConfigurationServiceFactory.newInstance().getConfigurationServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.configuration.path" ) ) );
    }

    @Override
    public ThemesDO getThemes(final String applicationName)
            throws ThemesException {

        ThemesRequest request = new ThemesRequest();
        request.setApplicationName( applicationName );

        // operate
        ThemesResponse response = getPort().themes( request );

        if (null != response.getError()) {
            throw new ThemesException( convert( response.getError().getErrorCode() ) );
        }

        // all good...
        List<ThemeDO> themeDOs = Lists.newLinkedList();
        for (Themes themes : response.getSuccess().getThemes()) {

            themeDOs.add( new ThemeDO( themes.getName(), themes.isDefaultTheme(), convert( themes.getLogo() ), convert( themes.getAuthLogo() ),
                    convert( themes.getBackground() ), convert( themes.getTabletBackground() ), convert( themes.getAlternativeBackground() ),
                    themes.getBackgroundColor(), themes.getTextColor() ) );
        }

        return new ThemesDO( themeDOs );
    }

    // Helper methods

    private LocalizedImagesDO convert(final LocalizedImages localizedImages) {

        if (null == localizedImages)
            return null;

        Map<String, LocalizedImageDO> imageMap = Maps.newHashMap();
        for (LocalizedImage localizedImage : localizedImages.getImages()) {
            imageMap.put( localizedImage.getLanguage(), new LocalizedImageDO( localizedImage.getUrl(), localizedImage.getLanguage() ) );
        }
        return new LocalizedImagesDO( imageMap );
    }

    private ThemesErrorCode convert(final net.lin_k.safe_online.configuration.ThemesErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_APPLICATION:
                return ThemesErrorCode.ERROR_UNKNOWN_APPLICATION;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
