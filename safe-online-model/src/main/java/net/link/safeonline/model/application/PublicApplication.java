/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.application;

import java.awt.Color;
import java.net.URL;

import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.service.PublicApplicationService;


/**
 * <h2>{@link PublicApplication} - An {@link ApplicationEntity} stub containing only public information.</h2>
 * <p>
 * A stub that represents the publicly available information from an {@link ApplicationEntity}. This object can be used for providing
 * unauthenticated users access to public application data.
 * </p>
 * <p>
 * <i>Dec 7, 2007</i>
 * </p>
 * 
 * @see PublicApplicationService#findPublicApplication(String)
 * 
 * @author mbillemo
 */
public class PublicApplication {

    private Color  color;
    private byte[] logo;
    private URL    url;
    private String friendly;
    private String name;
    private String description;


    public PublicApplication(ApplicationEntity application) {

        this.friendly = application.getFriendlyName();
        this.name = application.getName();

        this.color = application.getApplicationColor();
        this.logo = application.getApplicationLogo();

        this.description = application.getDescription();
        this.url = application.getApplicationUrl();
    }

    public Color getColor() {

        return this.color;
    }

    public byte[] getLogo() {

        return this.logo;
    }

    public URL getUrl() {

        return this.url;
    }

    public String getFriendly() {

        return this.friendly;
    }

    public String getName() {

        return this.name;
    }

    public String getDescription() {

        return this.description;
    }
}
