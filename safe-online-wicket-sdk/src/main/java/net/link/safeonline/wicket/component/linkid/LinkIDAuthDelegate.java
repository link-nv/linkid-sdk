package net.link.safeonline.wicket.component.linkid;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;


/**
 * <h2>{@link LinkIDAuthDelegate}<br> <sub>Simple interface to provide delegation of linkID authentication requests.</sub></h2>
 *
 * <p> <i>Apr 3, 2009</i> </p>
 *
 * @author lhunath
 */
public interface LinkIDAuthDelegate extends Serializable {

    /**
     * @param request
     * @param response
     * @param target               The page where the user should end up after delegation.
     * @param targetPageParameters The parameters to pass to the page on construction.
     */
    void delegate(HttpServletRequest request, HttpServletResponse response, Class<? extends Page> target, PageParameters targetPageParameters);
}
