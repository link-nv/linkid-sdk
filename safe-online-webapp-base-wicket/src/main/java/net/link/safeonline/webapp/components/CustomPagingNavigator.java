/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;


/**
 * <h2>{@link CustomPagingNavigator}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 20, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class CustomPagingNavigator extends PagingNavigator {

    private static final long  serialVersionUID = 1L;

    public static final String FIRST_IMAGE_ID   = "first_image";
    public static final String PREV_IMAGE_ID    = "prev_image";
    public static final String NEXT_IMAGE_ID    = "next_image";
    public static final String LAST_IMAGE_ID    = "last_image";


    public CustomPagingNavigator(String id, IPageable pageable, IPagingLabelProvider labelProvider) {

        super(id, pageable, labelProvider);
    }

    public CustomPagingNavigator(String id, IPageable pageable) {

        super(id, pageable);

    }

    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();

        if (get("first") != null) {

            Image firstImage = new Image(FIRST_IMAGE_ID, "override");
            firstImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                    + "/images/icons/control_start.png"));
            ((Link<?>) get("first")).add(firstImage);

            Image prevImage = new Image(PREV_IMAGE_ID, "override");
            prevImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                    + "/images/icons/control_rewind.png"));
            ((Link<?>) get("prev")).add(prevImage);

            Image nextImage = new Image(NEXT_IMAGE_ID, "override");
            nextImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                    + "/images/icons/control_fastforward.png"));
            ((Link<?>) get("next")).add(nextImage);

            Image lastImage = new Image(LAST_IMAGE_ID, "override");
            lastImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                    + "/images/icons/control_end.png"));
            ((Link<?>) get("last")).add(lastImage);

        }

    }
}
