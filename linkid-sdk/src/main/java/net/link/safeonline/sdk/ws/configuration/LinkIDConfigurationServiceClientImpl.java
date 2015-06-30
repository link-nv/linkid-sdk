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
import net.lin_k.safe_online.configuration.Localization;
import net.lin_k.safe_online.configuration.LocalizationRequest;
import net.lin_k.safe_online.configuration.LocalizationResponse;
import net.lin_k.safe_online.configuration.LocalizationValue;
import net.lin_k.safe_online.configuration.LocalizedImage;
import net.lin_k.safe_online.configuration.LocalizedImages;
import net.lin_k.safe_online.configuration.Themes;
import net.lin_k.safe_online.configuration.ThemesRequest;
import net.lin_k.safe_online.configuration.ThemesResponse;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDConfigurationServiceClient;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDLocalization;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDLocalizationErrorCode;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDLocalizationException;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDLocalizedImage;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDLocalizedImages;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDTheme;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDThemes;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDThemesErrorCode;
import net.link.safeonline.sdk.api.ws.configuration.LinkIDThemesException;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.configuration.LinkIDConfigurationServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;


public class LinkIDConfigurationServiceClientImpl extends AbstractWSClient<ConfigurationServicePort> implements LinkIDConfigurationServiceClient {

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDConfigurationServiceClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the mandate web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDConfigurationServiceClientImpl(final String location, final X509Certificate[] sslCertificates,
                                                final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDConfigurationServiceClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDConfigurationServiceFactory.newInstance().getConfigurationServicePort(), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.configuration.path" ) ) );
    }

    @Override
    public LinkIDThemes getThemes(final String applicationName)
            throws LinkIDThemesException {

        ThemesRequest request = new ThemesRequest();
        request.setApplicationName( applicationName );

        // operate
        ThemesResponse response = getPort().themes( request );

        if (null != response.getError()) {
            throw new LinkIDThemesException( convert( response.getError().getErrorCode() ) );
        }

        // all good...
        List<LinkIDTheme> linkIDThemes = Lists.newLinkedList();
        for (Themes themes : response.getSuccess().getThemes()) {

            linkIDThemes.add( new LinkIDTheme( themes.getName(), themes.isDefaultTheme(), convert( themes.getLogo() ), convert( themes.getAuthLogo() ),
                    convert( themes.getBackground() ), convert( themes.getTabletBackground() ), convert( themes.getAlternativeBackground() ),
                    themes.getBackgroundColor(), themes.getTextColor() ) );
        }

        return new LinkIDThemes( linkIDThemes );
    }

    @Override
    public List<LinkIDLocalization> getLocalization(final List<String> keys)
            throws LinkIDLocalizationException {

        LocalizationRequest request = new LocalizationRequest();
        request.getKey().addAll( keys );

        // operate
        LocalizationResponse response = getPort().localization( request );

        if (null != response.getError()) {
            throw new LinkIDLocalizationException( convert( response.getError().getErrorCode() ) );
        }

        // all good
        List<LinkIDLocalization> localizations = Lists.newLinkedList();
        for (Localization localization : response.getSuccess().getLocalization()) {
            Map<String, String> values = Maps.newHashMap();
            for (LocalizationValue localizationValue : localization.getValues()) {
                values.put( localizationValue.getLanguageCode(), localizationValue.getLocalized() );
            }
            localizations.add( new LinkIDLocalization( localization.getKey(), values ) );
        }
        return localizations;
    }

    // Helper methods

    private LinkIDLocalizedImages convert(final LocalizedImages localizedImages) {

        if (null == localizedImages)
            return null;

        Map<String, LinkIDLocalizedImage> imageMap = Maps.newHashMap();
        for (LocalizedImage localizedImage : localizedImages.getImages()) {
            imageMap.put( localizedImage.getLanguage(), new LinkIDLocalizedImage( localizedImage.getUrl(), localizedImage.getLanguage() ) );
        }
        return new LinkIDLocalizedImages( imageMap );
    }

    private LinkIDThemesErrorCode convert(final net.lin_k.safe_online.configuration.ThemesErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNKNOWN_APPLICATION:
                return LinkIDThemesErrorCode.ERROR_UNKNOWN_APPLICATION;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }

    private LinkIDLocalizationErrorCode convert(final net.lin_k.safe_online.configuration.LocalizationErrorCode errorCode) {

        switch (errorCode) {

            case ERROR_UNEXPECTED:
                return LinkIDLocalizationErrorCode.ERROR_UNEXPECTED;
        }

        throw new InternalInconsistencyException( String.format( "Unexpected error code %s!", errorCode.name() ) );
    }
}
