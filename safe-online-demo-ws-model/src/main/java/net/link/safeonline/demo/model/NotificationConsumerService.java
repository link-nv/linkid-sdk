package net.link.safeonline.demo.model;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.notification.consumer.ws.AbstractNotificationConsumerService;


@Local
public interface NotificationConsumerService extends SafeOnlineService, AbstractNotificationConsumerService {

}
