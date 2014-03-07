/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.wicket;

import net.link.safeonline.wicket.component.linkid.LinkIDPageAuthenticationListener;
import net.link.util.common.ApplicationMode;
import net.link.util.j2ee.NamingStrategy;
import net.link.util.wicket.util.WicketUtils;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.time.Duration;


public abstract class LinkIDApplication extends WebApplication {

    public static LinkIDApplication get() {

        return (LinkIDApplication) WebApplication.get();
    }

    @Override
    protected void internalInit() {

        // override the "ApplicationKey", if not overridden, the name of the WicketFilter in web.xml will be used which causes issues for cleanup of expires sessions.
        setApplicationKey( getClass().getSimpleName() );

        super.internalInit();
    }

    @Override
    protected void init() {

        // Java EE annotations injector.
        NamingStrategy namingStrategy = findNamingStrategy();
        if (null != namingStrategy)
            WicketUtils.addInjector( this, namingStrategy );

        // Poll resources, even in PRODUCTION.
        getResourceSettings().setResourcePollFrequency( Duration.ONE_MINUTE );

        // Strip wicket markup
        getMarkupSettings().setStripWicketTags( true );

        // LinkID Authentication.
        LinkIDPageAuthenticationListener linkIDAuthenticationListener = new LinkIDPageAuthenticationListener();
        addPreComponentOnBeforeRenderListener( linkIDAuthenticationListener );
        addComponentInstantiationListener( linkIDAuthenticationListener );

        switch (ApplicationMode.get()) {
            case DEBUG:
            case DEMO:
                getDebugSettings().setOutputComponentPath( false );
                break;
            case DEPLOYMENT:
                break;
        }
    }

    @Override
    public String getConfigurationType() {

        switch (ApplicationMode.get()) {
            case DEBUG:
                return Application.DEVELOPMENT;
            case DEMO:
            case DEPLOYMENT:
                return Application.DEPLOYMENT;
        }

        return Application.DEPLOYMENT;
    }

    /**
     * @return optional naming strategy to be used for injection of EJB's
     */
    protected abstract NamingStrategy findNamingStrategy();
}
