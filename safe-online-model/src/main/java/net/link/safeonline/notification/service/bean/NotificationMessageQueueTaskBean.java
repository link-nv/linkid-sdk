/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import net.link.safeonline.Task;
import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;
import net.link.safeonline.entity.notification.NotificationMessageEntity;
import net.link.safeonline.notification.dao.NotificationMessageDAO;
import net.link.safeonline.notification.service.NotificationProducerService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * Task for pushed failed notification messages back into the notifications queue.
 * 
 * @author wvdhaute
 * 
 */
@Stateless
@Configurable
@Interceptors(ConfigurationInterceptor.class)
@Local(Task.class)
@LocalBinding(jndiBinding = NotificationMessageQueueTaskBean.JNDI_BINDING)
public class NotificationMessageQueueTaskBean implements Task {

    public static final String          JNDI_BINDING   = Task.JNDI_PREFIX + "NotificationMessageQueueTaskBean/local";

    private static final String         name           = "Failed Notifications Handler";

    @EJB
    private NotificationMessageDAO      notificationMessageDAO;

    @EJB
    private NotificationProducerService notificationProducerService;

    @Configurable(name = "Maximum Attempts", group = "Failed Notifications Handler")
    private Integer                     configAttempts = 5;


    public String getName() {

        return name;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void perform()
            throws Exception {

        List<NotificationMessageEntity> notifications = this.notificationMessageDAO.listNotificationMessages();
        for (NotificationMessageEntity notification : notifications) {
            if (notification.getAttempts() >= this.configAttempts) {
                this.notificationMessageDAO.removeNotificationMessage(notification);
            } else {
                this.notificationProducerService.sendNotification(notification);
            }
        }

    }
}
