package net.link.safeonline.demo.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.notification.consumer.ws.AbstractNotificationConsumerService;


@Local
public interface NotificationConsumerService extends SafeOnlineService, AbstractNotificationConsumerService {

    public static final String JNDI_BINDING = DemoConstants.DEMO_JNDI_PREFIX + "NotificationConsumerServiceBean/local";
}
