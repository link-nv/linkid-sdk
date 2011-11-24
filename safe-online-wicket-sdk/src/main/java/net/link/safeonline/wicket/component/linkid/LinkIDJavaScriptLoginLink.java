/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.auth.RequestConstants;
import net.link.safeonline.sdk.auth.util.AuthenticationUtils;
import net.link.safeonline.sdk.configuration.AuthenticationContext;
import net.link.safeonline.sdk.configuration.LoginMode;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.*;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.opensaml.saml2.core.Attribute;


/**
 * <h2>{@link LinkIDJavaScriptLoginLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.</sub></h2>
 * <p/>
 * <p> <i>Sep 22, 2008</i> </p>
 *
 * @author lhunath
 */
public class LinkIDJavaScriptLoginLink extends AbstractLinkIDAuthLink {

    protected boolean addJS = false;

    public LinkIDJavaScriptLoginLink(String id) {

        this( id, null, false );
    }

    public LinkIDJavaScriptLoginLink(String id, Class<? extends Page> target) {

        this( id, target, false );
    }

    public LinkIDJavaScriptLoginLink(String id, boolean addJS) {

        this( id, null, addJS );
    }
    
    public LinkIDJavaScriptLoginLink(String id, Class<? extends Page> target, boolean addJS) {

        super( id, target );
        this.addJS = addJS;
        this.add( new AttributeAppender("class", new Model<String>("linkid-login"), " ") );
    }

    public boolean isAddJS() {

        return addJS;
    }

    public void setAddJS(final boolean addJS) {

        this.addJS = addJS;
    }

    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();
        if (addJS){
            //LinkID JavaScript which handles login look
            add( new HeaderContributor( new IHeaderContributor() {
                    @Override
                    public void renderHead(IHeaderResponse response) {

                        response.renderJavascriptReference( config().web().authBase() + "/resources/common/js/linkid.login-min.js", "linkid-login-script" );
                    }
            } ) );
        }

    }

    public void delegate(final HttpServletRequest request, final HttpServletResponse response, final Class<? extends Page> target,
                         final PageParameters targetPageParameters) {

        AuthenticationUtils.login( request, response, newContext( target, targetPageParameters ) );
    }

    /**
     * Override this if you want to provide a custom authentication context.
     * <p/>
     * The default context uses the page class and parameters provided by this component to build the URL the user will be sent to after
     * the
     * process has been completed.
     *
     * @param target               The page where the user should end up after delegation.
     * @param targetPageParameters The parameters to pass to the page on construction.
     *
     * @return A new logout context.
     */
    protected AuthenticationContext newContext(final Class<? extends Page> target, final PageParameters targetPageParameters) {

        WebRequest request = getWebRequest();
        String targetURL = request.getParameter( RequestConstants.TARGETURI_REQUEST_PARAM );
        String modeParam = request.getParameter( RequestConstants.LOGINMODE_REQUEST_PARAM );
        LoginMode mode = null;
        if (modeParam != null){
            for (LoginMode val : LoginMode.values()){
                if (modeParam.trim().equalsIgnoreCase( val.name() )){
                    mode = val;
                    break;
                }
            }
        }

        if (target == null){
            targetURL = RequestCycle.get().urlFor( target, targetPageParameters ).toString();
        }

        return new AuthenticationContext(null, null, null, targetURL, mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }


}
