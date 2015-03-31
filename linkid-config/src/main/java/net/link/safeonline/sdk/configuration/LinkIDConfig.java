/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import java.util.Locale;
import net.link.util.config.Group;
import net.link.util.config.Property;


/**
 * <h2>{@link LinkIDConfig}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 15, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "linkID")
public interface LinkIDConfig {

    /**
     * @return Application linkID identity configuration.  Identity, keys, etc.
     */
    LinkIDAppConfig app();

    /**
     * Specifies the language that the linkID service should localize its interaction with the user in.
     * <p/>
     * <i>[optional, default: Use the locale of the current browser request]</i>
     */
    @Property(required = false, unset = Property.AUTO)
    Locale language();
}
