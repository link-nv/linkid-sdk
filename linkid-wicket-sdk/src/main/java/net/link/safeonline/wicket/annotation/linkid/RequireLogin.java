/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.wicket.annotation.linkid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.link.util.wicket.component.WicketPage;
import org.apache.wicket.Page;


/**
 * <h2>{@link RequireLogin}<br>
 * <sub>Putting this annotation on a {@link WicketPage} makes sure users can only access it after having authenticated.</sub></h2>
 *
 * <p>
 * <i>Dec 31, 2008</i>
 * </p>
 *
 * @author lhunath
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {

    Class<? extends Page> loginPage() default Page.class;
}
