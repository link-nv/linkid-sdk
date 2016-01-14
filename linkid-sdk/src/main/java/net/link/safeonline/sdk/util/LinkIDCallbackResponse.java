package net.link.safeonline.sdk.util;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.api.callback.LinkIDCallbackConstants;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 14/01/16
 * Time: 11:21
 */
public class LinkIDCallbackResponse implements Serializable {

    private final String applicationName;
    private final String responseId;
    private final String language;
    @Nullable
    private final String appSessionId;

    public LinkIDCallbackResponse(final HttpServletRequest request) {

        this.applicationName = request.getParameter( LinkIDCallbackConstants.PARAM_APPLICATION_NAME );
        this.responseId = request.getParameter( LinkIDCallbackConstants.PARAM_RESPONSE_ID );
        this.language = request.getParameter( LinkIDCallbackConstants.PARAM_LANGUAGE );
        this.appSessionId = request.getParameter( LinkIDCallbackConstants.PARAM_APP_SESSION_ID );

    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDCallbackResponse{" +
               "applicationName='" + applicationName + '\'' +
               ", responseId='" + responseId + '\'' +
               ", language='" + language + '\'' +
               ", appSessionId='" + appSessionId + '\'' +
               '}';
    }

    // Accessors

    public String getApplicationName() {

        return applicationName;
    }

    public String getResponseId() {

        return responseId;
    }

    public String getLanguage() {

        return language;
    }

    @Nullable
    public String getAppSessionId() {

        return appSessionId;
    }
}
