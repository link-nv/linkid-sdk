/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.configuration;

/**
 * linkID Configuration WS Client.
 * <p/>
 * Via this interface, public application configuration can be fetched
 */
public interface LinkIDConfigurationServiceClient {

    /**
     * Fetch the application's themes
     *
     * @throws LinkIDThemesException something went wrong, check the error code in the exception
     */
    LinkIDThemes getThemes(String applicationName)
            throws LinkIDThemesException;
}
