package net.link.safeonline.sdk.auth.util;

import com.google.common.collect.Maps;
import java.util.Map;
import net.link.safeonline.sdk.api.auth.device.LinkIDDeviceContextConstants;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 06/06/14
 * Time: 14:32
 */
public abstract class LinkIDDeviceContextUtils {

    public static Map<String, String> generate(@Nullable final String authenticationMessage, @Nullable final String finishedMessage,
                                               @Nullable final String identityProfile, @Nullable final Long sessionExpiryOverride, @Nullable final String theme,
                                               @Nullable final String mobileLandingSuccess, @Nullable final String mobileLandingError,
                                               @Nullable final String mobileLandingCancel) {

        Map<String, String> deviceContextMap = Maps.newHashMap();

        if (null != authenticationMessage) {
            deviceContextMap.put( LinkIDDeviceContextConstants.AUTHENTICATION_MESSAGE, authenticationMessage );
        }
        if (null != finishedMessage) {
            deviceContextMap.put( LinkIDDeviceContextConstants.FINISHED_MESSAGE, finishedMessage );
        }

        if (null != identityProfile) {
            deviceContextMap.put( LinkIDDeviceContextConstants.IDENTITY_PROFILE, identityProfile );
        }

        if (null != sessionExpiryOverride) {
            deviceContextMap.put( LinkIDDeviceContextConstants.SESSION_EXPIRY_OVERRIDE, Long.toString( sessionExpiryOverride ) );
        }

        if (null != theme) {
            deviceContextMap.put( LinkIDDeviceContextConstants.THEME, theme );
        }

        if (null != mobileLandingSuccess) {
            deviceContextMap.put( LinkIDDeviceContextConstants.MOBILE_LANDING_SUCCESS_URL, mobileLandingSuccess );
        }
        if (null != mobileLandingError) {
            deviceContextMap.put( LinkIDDeviceContextConstants.MOBILE_LANDING_ERROR_URL, mobileLandingError );
        }
        if (null != mobileLandingCancel) {
            deviceContextMap.put( LinkIDDeviceContextConstants.MOBILE_LANDING_CANCEL_URL, mobileLandingCancel );
        }

        return deviceContextMap;
    }
}
