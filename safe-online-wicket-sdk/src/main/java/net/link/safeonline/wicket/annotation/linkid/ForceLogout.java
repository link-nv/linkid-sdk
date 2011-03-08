/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.annotation.linkid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.link.util.wicket.component.WicketPage;


/**
 * <h2>{@link ForceLogout}<br>
 * <sub>Putting this annotation on a {@link WicketPage} causes the user to get logged out before the page is rendered.</sub></h2>
 *
 * <p>
 * <i>Dec 31, 2008</i>
 * </p>
 *
 * @author lhunath
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForceLogout {

}
