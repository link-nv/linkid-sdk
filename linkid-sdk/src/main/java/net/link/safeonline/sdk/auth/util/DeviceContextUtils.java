package net.link.safeonline.sdk.auth.util;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.auth.device.DeviceContextConstants;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 06/06/14
 * Time: 14:32
 */
public abstract class DeviceContextUtils {

    public static Map<String, String> generate(@Nullable final String authenticationMessage, @Nullable final String finishedMessage,
                                               @Nullable final List<String> identityProfiles) {

        Map<String, String> deviceContextMap = Maps.newHashMap();

        if (null != authenticationMessage) {
            deviceContextMap.put( DeviceContextConstants.AUTHENTICATION_MESSAGE, authenticationMessage );
        }
        if (null != finishedMessage) {
            deviceContextMap.put( DeviceContextConstants.FINISHED_MESSAGE, finishedMessage );
        }

        if (null != identityProfiles && !identityProfiles.isEmpty()) {

            int i = 0;
            for (String identityProfile : identityProfiles) {

                deviceContextMap.put( String.format( "%s.%d", DeviceContextConstants.IDENTITY_PROFILE_PREFIX, i ), identityProfile );
            }
        }

        return deviceContextMap;
    }
}