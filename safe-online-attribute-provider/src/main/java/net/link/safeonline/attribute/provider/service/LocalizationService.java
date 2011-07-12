/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.service;

import java.util.Locale;
import net.link.safeonline.attribute.provider.AttributeProvider;


/**
 * LinkID Localization Service. <p/>
 * <p/>
 * Offers use of the LinkID localization layer towards {@link AttributeProvider}s.
 */
public interface LocalizationService {

    /**
     * @param key    localization key
     * @param locale locale
     *
     * @return text for specified key and locale. Returns {@code null} if not found.
     */
    String findText(String key, Locale locale);
}
