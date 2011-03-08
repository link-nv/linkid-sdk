package net.link.safeonline.wicket.component.linkid;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
     * <h2>{@link LinkIDAuthDelegate}<br>
     * <sub>Simple interface to provide delegation of linkID authentication requests.</sub></h2>
     *
     * <p>
     * <i>Apr 3, 2009</i>
     * </p>
     *
     * @author lhunath
     */
    public interface LinkIDAuthDelegate extends Serializable {

        /**
         * @param link The link that invokes this delegate.
         */
        void delegate(HttpServletRequest request, HttpServletResponse response, AbstractLinkIDAuthLink link);
    }
