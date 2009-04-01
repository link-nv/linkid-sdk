/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.test;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;


/**
 * <h2>{@link TestStringResourceLoader}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class TestStringResourceLoader implements IStringResourceLoader {

    /**
     * {@inheritDoc}
     */
    public String loadStringResource(Component component, String keySuffix) {

        return getStringResource(keySuffix, component.getLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String loadStringResource(Class<?> clazz, String keySuffix, Locale locale, String style) {

        return getStringResource(keySuffix, locale);
    }

    private String getStringResource(@SuppressWarnings("unused") String key, @SuppressWarnings("unused") Locale locale) {

        return key;
    }
}
