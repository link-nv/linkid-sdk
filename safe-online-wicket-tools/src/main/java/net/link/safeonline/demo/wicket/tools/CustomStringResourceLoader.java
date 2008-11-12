/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.tools;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.wicket.Component;
import org.apache.wicket.resource.loader.IStringResourceLoader;


/**
 * <h2>{@link CustomStringResourceLoader}<br>
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
public class CustomStringResourceLoader implements IStringResourceLoader {

    private String resourceBase;


    public CustomStringResourceLoader(String resourceBase) {

        super();
        this.resourceBase = resourceBase;
    }

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

    private String getStringResource(String key, Locale locale) {

        ResourceBundle messages = ResourceBundle.getBundle(this.resourceBase, locale);
        String resource;
        try {
            resource = messages.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
        return resource;

    }

}
