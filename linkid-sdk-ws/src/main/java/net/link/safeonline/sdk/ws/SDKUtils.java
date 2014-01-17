package net.link.safeonline.sdk.ws;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.util.*;
import javax.xml.datatype.*;


public abstract class SDKUtils {

    public static String getSDKProperty(final String key) {

        ResourceBundle properties = ResourceBundle.getBundle( "sdk_config" );
        return properties.getString( key );
    }

    public static XMLGregorianCalendar convert(final Date date) {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime( date );
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( c );
        }
        catch (DatatypeConfigurationException e) {
            throw new InternalInconsistencyException( e );
        }
    }
}
