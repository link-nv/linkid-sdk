package net.link.safeonline.sdk.auth.protocol.saml2;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.util.common.URLUtils;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;


public class LinkIDHttpServletResponseAdapter extends HttpServletResponseAdapter {

    private static final Logger logger = Logger.get( LinkIDHttpServletResponseAdapter.class );

    private final HttpServletResponse httpServletResponse;

    private final Locale    language;
    private final String    themeName;
    private final LoginMode loginMode;
    private final StartPage startPage;

    /**
     * Constructor.
     *
     * @param httpServletResponse servlet response to adapt
     * @param isSecure            whether the outbound connection is protected by SSL/TLS
     */
    public LinkIDHttpServletResponseAdapter(final HttpServletResponse httpServletResponse, final boolean isSecure, final Locale language,
                                            final String themeName, final LoginMode loginMode, final StartPage startPage) {

        super( httpServletResponse, isSecure );

        this.httpServletResponse = httpServletResponse;

        this.language = language;
        this.themeName = themeName;
        this.loginMode = loginMode;
        this.startPage = startPage;
    }

    @Override
    public void sendRedirect(String location) {

        // append linkID's request params
        if (null != language)
            location = URLUtils.addParameter( location, RequestConstants.LANGUAGE_REQUEST_PARAM, language.getLanguage() );
        if (null != themeName)
            location = URLUtils.addParameter( location, RequestConstants.THEME_REQUEST_PARAM, themeName );
        if (null != loginMode)
            location = URLUtils.addParameter( location, RequestConstants.LOGINMODE_REQUEST_PARAM, loginMode.name() );
        if (null != startPage)
            location = URLUtils.addParameter( location, RequestConstants.START_PAGE_REQUEST_PARAM, startPage.name() );

        logger.dbg( "location=%s", location );

        try {
            httpServletResponse.sendRedirect( location );
        }
        catch (IOException e) {
            logger.err( e, "Unable to send redirect message" );
        }
    }
}
